package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.getLastCheckDateKeys

enum class Surface(val osmValue: String) {
    ASPHALT("asphalt"),
    CONCRETE("concrete"),
    CONCRETE_PLATES("concrete:plates"),
    CONCRETE_LANES("concrete:lanes"),
    FINE_GRAVEL("fine_gravel"),
    PAVING_STONES("paving_stones"),
    COMPACTED("compacted"),
    DIRT("dirt"),
    SETT("sett"),
    UNHEWN_COBBLESTONE("unhewn_cobblestone"),
    GRASS_PAVER("grass_paver"),
    WOOD("wood"),
    WOODCHIPS("woodchips"),
    METAL("metal"),
    GRAVEL("gravel"),
    PEBBLES("pebblestone"),
    GRASS("grass"),
    SAND("sand"),
    ROCK("rock"),
    CLAY("clay"),
    ARTIFICIAL_TURF("artificial_turf"),
    TARTAN("tartan"),
    PAVED_ROAD("paved"),
    UNPAVED_ROAD("unpaved"),
    GROUND_ROAD("ground"),
    PAVED_AREA("paved"),
    UNPAVED_AREA("unpaved"),
    GROUND_AREA("ground"),

    // extra values, recording duplicates
    SOIL("soil"),
    EARTH("earth");

    companion object {
        val invalidSurfaces: Set<String> = setOf(
            "cobblestone", // https://wiki.openstreetmap.org/wiki/Tag%3Asurface%3Dcobblestone
            "cement", // https://community.openstreetmap.org/t/mysterious-surface-cement/5158 TODO: after 2012-11-23 document that it is an unwanted value at https://wiki.openstreetmap.org/wiki/Tag:surface%3Dconcrete
            "trail", // https://wiki.openstreetmap.org/wiki/Tag:surface%3Dtrail TODO after 2022-12-01 confirm this deprecation with community (lets wait till to give time for response, remove that from PR if I will finish PR before that happens)
        )
        // TODO what about mud? https://github.com/streetcomplete/StreetComplete/discussions/4300
        // TODO what about metal_grid?
        // Maybe start supporting them as a full blown value TODO
        // brick, bricks, paving_stones:30, cobblestone:flattened are for now not supperted at all and will leave object uneditable
    }
}

sealed class SurfaceInfo
sealed class SingleSurfaceInfo : SurfaceInfo()

data class SingleSurface(val surface: Surface) : SingleSurfaceInfo()
data class SingleSurfaceWithNote(val surface: Surface, val note: String) : SingleSurfaceInfo()
object SurfaceMissing : SingleSurfaceInfo()
data class SurfaceMissingWithNote(val note: String) : SingleSurfaceInfo()
data class CyclewayFootwaySurfaces(val main: Surface?, val cycleway: Surface?, val footway: Surface?) : SurfaceInfo()
data class CyclewayFootwaySurfacesWithNote(val main: Surface?, val note: String?, val cycleway: Surface?, val cyclewayNote: String?, val footway: Surface?, val footwayNote: String?) : SurfaceInfo()

fun createSurfaceStatus(tags: Map<String, String>): SurfaceInfo {
    val surface = surfaceTextValueToSurfaceEnum(tags["surface"])
    val surfaceNote = tags["surface:note"]
    val cyclewaySurfaceNote = tags["cycleway:surface:note"]
    val footwaySurfaceNote = tags["footway:surface:note"]
    val cyclewaySurface = surfaceTextValueToSurfaceEnum(tags["cycleway:surface"])
    val footwaySurface = surfaceTextValueToSurfaceEnum(tags["footway:surface"])
    val hasDedicatedFootwayCyclewayData = cyclewaySurface != null || footwaySurface != null || tags["segregated"] == "yes" || cyclewaySurfaceNote != null || footwaySurfaceNote != null
    if (cyclewaySurfaceNote != null || footwaySurfaceNote != null || (hasDedicatedFootwayCyclewayData && surfaceNote != null)) {
        return CyclewayFootwaySurfacesWithNote(surface, surfaceNote, cyclewaySurface, cyclewaySurfaceNote, footwaySurface, footwaySurfaceNote)
    }
    if (hasDedicatedFootwayCyclewayData) {
        return CyclewayFootwaySurfaces(surface, cyclewaySurface, footwaySurface)
    }
    if (surface != null && surfaceNote != null ) {
        return SingleSurfaceWithNote(surface, surfaceNote)
    }
    if (surface == null && surfaceNote != null ) {
        return SurfaceMissingWithNote(surfaceNote)
    }
    if (surface != null) {
        return SingleSurface(surface)
    }
    return SurfaceMissing
}

/*
* to be used when only surface and surface:note tag is relevant
* for example if we want to tag road surface and we are free to skip sidewalk surface info
* */
fun createMainSurfaceStatus(tags: Map<String, String>): SingleSurfaceInfo {
    val surface = surfaceTextValueToSurfaceEnum(tags["surface"])
    val surfaceNote = tags["surface:note"]
    if (surface != null && surfaceNote != null ) {
        return SingleSurfaceWithNote(surface, surfaceNote)
    }
    if (surface == null && surfaceNote != null ) {
        return SurfaceMissingWithNote(surfaceNote)
    }
    if (surface != null) {
        return SingleSurface(surface)
    }
    return SurfaceMissing
}

fun surfaceTextValueToSurfaceEnum(surfaceValue: String?): Surface? {
    val foundSurface = Surface.values().find { it.osmValue == surfaceValue }

    // PAVED_AREA and UNPAVED_AREA are more generic - and this can be also asked
    // for objects which are not roads
    if (foundSurface == Surface.PAVED_ROAD) {
        return Surface.PAVED_AREA
    }
    if (foundSurface == Surface.UNPAVED_ROAD) {
        return Surface.UNPAVED_AREA
    }
    return foundSurface
}

fun commonSurfaceDescription(surfaceA: String?, surfaceB: String?): String? {
    if (surfaceA == null || surfaceB == null) {
        return null
    }
    if (surfaceA == surfaceB) {
        return surfaceA
    }
    if (surfaceA in ANYTHING_PAVED && surfaceB in ANYTHING_PAVED) {
        return "paved"
    }
    if (surfaceA in ANYTHING_UNPAVED && surfaceB in ANYTHING_UNPAVED) {
        return "unpaved"
    }
    return null
}

fun commonSurfaceObject(surfaceA: String?, surfaceB: String?): Surface? {
    val shared = commonSurfaceDescription(surfaceA, surfaceB) ?: return null
    if (shared == "paved") {
        return Surface.PAVED_AREA
    }
    if (shared == "unpaved") {
        return Surface.UNPAVED_AREA
    }
    if (shared == "ground") {
        return Surface.GROUND_AREA
    }
    return Surface.values().firstOrNull { it.osmValue == shared }
}

val SOFT_SURFACES = setOf("ground", "earth", "dirt", "grass", "sand", "mud", "ice", "salt", "snow", "woodchips")

val ANYTHING_UNPAVED = SOFT_SURFACES + setOf(
    "unpaved", "compacted", "gravel", "fine_gravel", "pebblestone", "grass_paver",
)

val ANYTHING_FULLY_PAVED = setOf(
    "paved", "asphalt", "cobblestone", "cobblestone:flattened", "sett",
    "concrete", "concrete:plates", "paving_stones",
    "metal", "wood", "unhewn_cobblestone"
)

val Surface.titleResId: Int get() = when (this) {
    Surface.ASPHALT -> R.string.quest_surface_value_asphalt
    Surface.CONCRETE -> R.string.quest_surface_value_concrete
    Surface.CONCRETE_PLATES -> R.string.quest_surface_value_concrete_plates
    Surface.CONCRETE_LANES -> R.string.quest_surface_value_concrete_lanes
    Surface.FINE_GRAVEL -> R.string.quest_surface_value_fine_gravel
    Surface.PAVING_STONES -> R.string.quest_surface_value_paving_stones
    Surface.COMPACTED -> R.string.quest_surface_value_compacted
    Surface.DIRT, Surface.SOIL, Surface.EARTH -> R.string.quest_surface_value_dirt
    Surface.SETT -> R.string.quest_surface_value_sett
    Surface.UNHEWN_COBBLESTONE -> R.string.quest_surface_value_unhewn_cobblestone
    Surface.GRASS_PAVER -> R.string.quest_surface_value_grass_paver
    Surface.WOOD -> R.string.quest_surface_value_wood
    Surface.WOODCHIPS -> R.string.quest_surface_value_woodchips
    Surface.METAL -> R.string.quest_surface_value_metal
    Surface.GRAVEL -> R.string.quest_surface_value_gravel
    Surface.PEBBLES -> R.string.quest_surface_value_pebblestone
    Surface.GRASS -> R.string.quest_surface_value_grass
    Surface.SAND -> R.string.quest_surface_value_sand
    Surface.ROCK -> R.string.quest_surface_value_rock
    Surface.CLAY -> R.string.quest_surface_value_clay
    Surface.ARTIFICIAL_TURF -> R.string.quest_surface_value_artificial_turf
    Surface.TARTAN -> R.string.quest_surface_value_tartan
    Surface.PAVED_ROAD -> R.string.quest_surface_value_paved
    Surface.UNPAVED_ROAD -> R.string.quest_surface_value_unpaved
    Surface.GROUND_ROAD -> R.string.quest_surface_value_ground
    Surface.PAVED_AREA -> R.string.quest_surface_value_paved
    Surface.UNPAVED_AREA -> R.string.quest_surface_value_unpaved
    Surface.GROUND_AREA -> R.string.quest_surface_value_ground
}

val Surface.iconResId: Int get() = when (this) {
    Surface.ASPHALT -> R.drawable.surface_asphalt
    Surface.CONCRETE -> R.drawable.surface_concrete
    Surface.CONCRETE_PLATES -> R.drawable.surface_concrete_plates
    Surface.CONCRETE_LANES -> R.drawable.surface_concrete_lanes
    Surface.FINE_GRAVEL -> R.drawable.surface_fine_gravel
    Surface.PAVING_STONES -> R.drawable.surface_paving_stones
    Surface.COMPACTED -> R.drawable.surface_compacted
    Surface.DIRT, Surface.SOIL, Surface.EARTH -> R.drawable.surface_dirt
    Surface.SETT -> R.drawable.surface_sett
    Surface.UNHEWN_COBBLESTONE -> R.drawable.surface_cobblestone
    Surface.GRASS_PAVER -> R.drawable.surface_grass_paver
    Surface.WOOD -> R.drawable.surface_wood
    Surface.WOODCHIPS -> R.drawable.surface_woodchips
    Surface.METAL -> R.drawable.surface_metal
    Surface.GRAVEL -> R.drawable.surface_gravel
    Surface.PEBBLES -> R.drawable.surface_pebblestone
    Surface.GRASS -> R.drawable.surface_grass
    Surface.SAND -> R.drawable.surface_sand
    Surface.ROCK -> R.drawable.surface_rock
    Surface.CLAY -> R.drawable.surface_tennis_clay
    Surface.ARTIFICIAL_TURF -> R.drawable.surface_artificial_turf
    Surface.TARTAN -> R.drawable.surface_tartan
    Surface.PAVED_ROAD -> R.drawable.path_surface_paved
    Surface.UNPAVED_ROAD -> R.drawable.path_surface_unpaved
    Surface.GROUND_ROAD -> R.drawable.surface_ground
    Surface.PAVED_AREA -> R.drawable.surface_paved_area
    Surface.UNPAVED_AREA -> R.drawable.surface_unpaved_area
    Surface.GROUND_AREA -> R.drawable.surface_ground_area
}

val ANYTHING_PAVED = ANYTHING_FULLY_PAVED + setOf(
    "concrete:lanes"
)

val INVALID_SURFACES_FOR_TRACKTYPES = mapOf(
    "grade1" to ANYTHING_UNPAVED,
    "grade2" to SOFT_SURFACES,
    "grade3" to ANYTHING_FULLY_PAVED,
    "grade4" to ANYTHING_FULLY_PAVED,
    "grade5" to ANYTHING_FULLY_PAVED,
)

fun isSurfaceAndTracktypeMismatching(surface: String, tracktype: String): Boolean {
    return INVALID_SURFACES_FOR_TRACKTYPES[tracktype]?.contains(surface) == true
}

fun associatedKeysToBeRemovedOnChange(prefix: String): Set<String> {
    return setOf("${prefix}surface:grade", "${prefix}smoothness", "${prefix}smoothness:date",
        "${prefix}smoothness", "${prefix}surface:colour", "source:${prefix}surface") +
        getLastCheckDateKeys("${prefix}surface") + getLastCheckDateKeys("${prefix}smoothness")
}