package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.surface.CyclewayFootwaySurfaces
import de.westnordost.streetcomplete.osm.surface.CyclewayFootwaySurfacesWithNote
import de.westnordost.streetcomplete.osm.surface.SingleSurface
import de.westnordost.streetcomplete.osm.surface.SingleSurfaceWithNote
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.Surface.PAVED_AREA
import de.westnordost.streetcomplete.osm.surface.Surface.PAVED_ROAD
import de.westnordost.streetcomplete.osm.surface.Surface.UNPAVED_AREA
import de.westnordost.streetcomplete.osm.surface.Surface.UNPAVED_ROAD
import de.westnordost.streetcomplete.osm.surface.SurfaceMissing
import de.westnordost.streetcomplete.osm.surface.SurfaceMissingWithNote
import de.westnordost.streetcomplete.osm.surface.associatedKeysToBeRemovedOnChange
import de.westnordost.streetcomplete.osm.surface.createSurfaceStatus
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface

// remove before it gets to the PR TODO
class UniversalSurfaceOverlay : Overlay {

    private val parentQuest = AddRoadSurface()
    override val title = R.string.overlay_universal_surface
    override val icon = R.drawable.ic_quest_power
    override val changesetComment = parentQuest.changesetComment
    override val wikiLink: String = parentQuest.wikiLink
    override val achievements = parentQuest.achievements
    override val hidesQuestTypes = setOf(parentQuest::class.simpleName!!, AddPathSurface::class.simpleName!!)

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> {
        val handledSurfaces = Surface.values().map { it.osmValue }.toSet()
        return mapData
           .filter( """ways, relations with
               (
                    (surface and highway != construction)
                    or leisure ~ pitch|playground
                    or highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}
                    or aeroway ~ taxiway|runway|helipad|apron|taxilane
                )
               and (!surface or surface ~ ${handledSurfaces.joinToString("|") })
               and (!cycleway:surface or cycleway:surface ~ ${handledSurfaces.joinToString("|") })
               and (!footway:surface or footway:surface ~ ${handledSurfaces.joinToString("|") })
               and (segregated = yes or (!cycleway:surface and !footway:surface))
               and (!surface:note or surface)
               and (!cycleway:surface:note or cycleway:surface)
               and (!footway:surface:note or footway:surface)
               """)
           .filter { element -> tagsHaveOnlyAllowedSurfaceKeys(element.tags) }.map { it to getStyle(it) }
    }

    private fun tagsHaveOnlyAllowedSurfaceKeys(tags: Map<String, String>): Boolean {
        return tags.keys.none {
            "surface" in it && it !in allowedTagWithSurfaceInKey
        }
    }
    // https://taginfo.openstreetmap.org/search?q=surface
    val supportedSurfaceKeys = listOf(
        // supported here
        "footway:surface", "cycleway:surface", // roads with THAT tags will end with surface/footway display, but as it will happen in forkonly it is fine
        // really rare, but added by StreetComplete so also should be supported by it to allow editing added data
        "cycleway:surface:note", "footway:surface:note", // TODO: verify support

        // supported in this overlay, but not in all overlays
        // or more specifically: it can be safely ignored here, I think
        "sidewalk:both:surface", "sidewalk:right:surface", "sidewalk:left:surface", "sidewalk:surface",

        "surface",
        "surface:note"
    ) + associatedKeysToBeRemovedOnChange("surface") +
        associatedKeysToBeRemovedOnChange("cycleway:surface") + associatedKeysToBeRemovedOnChange("fotway:surface")

    private val allowedTagWithSurfaceInKey = supportedSurfaceKeys + listOf(
        "proposed:surface", // does not matter
    )

    override fun createForm(element: Element?) =
        if (element != null) PathSurfaceOverlayForm()
        else null
}

private fun getStyle(element: Element): Style {
    val originalSurfaceStatus = createSurfaceStatus(element.tags)
    val badSurfaces = listOf(null, PAVED_ROAD, PAVED_AREA, UNPAVED_ROAD, UNPAVED_AREA)
    var dominatingSurface: Surface? = null
    var noteProvided: String? = null
    when (originalSurfaceStatus) {
        is SingleSurfaceWithNote -> {
            dominatingSurface = originalSurfaceStatus.surface
            noteProvided = originalSurfaceStatus.note
        }
        is CyclewayFootwaySurfacesWithNote -> if (originalSurfaceStatus.cycleway in badSurfaces && originalSurfaceStatus.cyclewayNote == null) {
            // the worst case: so lets present it
            dominatingSurface = originalSurfaceStatus.cycleway
            noteProvided = originalSurfaceStatus.cyclewayNote
        } else if (originalSurfaceStatus.footway in badSurfaces) {
            // cycleway surface either has as bad data (also bad surface) or a bit better (bad surface with note)
            dominatingSurface = originalSurfaceStatus.footway
            noteProvided = originalSurfaceStatus.footwayNote
        } else {
            // cycleway is arbitrarily taken as dominating here
            // though for bicycles surface is a bit more important
            dominatingSurface = originalSurfaceStatus.cycleway
            noteProvided = originalSurfaceStatus.cyclewayNote
        }
        is SingleSurface -> {
            dominatingSurface = originalSurfaceStatus.surface
        }
        is CyclewayFootwaySurfaces -> if (originalSurfaceStatus.footway in badSurfaces) {
            dominatingSurface = originalSurfaceStatus.footway
        } else {
            // cycleway is arbitrarily taken as dominating here
            // though for bicycles surface is a bit more important
            dominatingSurface = originalSurfaceStatus.cycleway
        }
        is SurfaceMissing -> {
            // no action needed
        }
        is SurfaceMissingWithNote -> {
            noteProvided = originalSurfaceStatus.note
        }
    }
    // not set but indoor or private -> do not highlight as missing
    val isNotSet = dominatingSurface in badSurfaces
    val isNotSetButThatsOkay = isNotSet && (isIndoor(element.tags) || isPrivateOnFoot(element)) || element.tags["leisure"] == "playground"

    val color = if (isNotSetButThatsOkay) {
        Color.INVISIBLE
    } else if (isNotSet && noteProvided != null) {
        Color.BLACK
    } else {
        dominatingSurface.color
    }
    return if (element.tags["area"] == "yes") PolygonStyle(color) else PolylineStyle(StrokeStyle(color))
}

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"