package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk_surface.createSidewalkSurface
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.UNDERSPECIFED_SURFACES
import de.westnordost.streetcomplete.osm.surface.createMainSurfaceStatus
import de.westnordost.streetcomplete.osm.surface.createSurfaceStatus
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.StrokeStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface

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
           .map { it to getStyle(it) }
    }

    private fun getStyle(element: Element): Style {
        if (element.tags["highway"] in ALL_ROADS) {
            return getRoadStyle(element)
        }
        return getStyleForStandalonePath(element)
    }

    override fun createForm(element: Element?) =
        if (element != null) {
            if (element.tags["highway"] in ALL_PATHS) PathSurfaceOverlayForm()
            else if (element.tags["highway"] in ALL_ROADS) RoadSurfaceOverlayForm()
            else null
        } else null
}

private fun getRoadStyle(element: Element): Style {
    val surfaceStatus = createMainSurfaceStatus(element.tags)
    var dominatingSurface: Surface? = surfaceStatus.value
    val noteProvided: String? = surfaceStatus.note
    // not set but indoor or private -> do not highlight as missing
    val isNotSet = dominatingSurface in UNDERSPECIFED_SURFACES
    val isNotSetButThatsOkay = isNotSet && (isIndoor(element.tags) || isPrivateOnFoot(element))
    val color = if (isNotSetButThatsOkay) {
        Color.INVISIBLE
    } else if (isNotSet && noteProvided != null) {
        Color.BLACK
    } else {
        dominatingSurface.color
    }
    return if (element.tags["area"] == "yes") PolygonStyle(color) else PolylineStyle(StrokeStyle(color), null, null)
}

private fun getStyleForStandalonePath(element: Element): Style {
    val surfaceStatus = createSurfaceStatus(element.tags)
    var dominatingSurface: Surface? = null
    var noteProvided: String? = null
    if (element.tags["segregated"] == "yes") {
        // filters guarantee that otherwise there is actually no split
        if (surfaceStatus.cycleway in UNDERSPECIFED_SURFACES && surfaceStatus.cyclewayNote == null) {
            // the worst case possible - bad surface without note: so lets present it
            dominatingSurface = surfaceStatus.cycleway
            noteProvided = surfaceStatus.cyclewayNote
        } else if (surfaceStatus.footway in UNDERSPECIFED_SURFACES) {
            // cycleway surface either has
            // data as bad as this one (also bad surface, without note)
            // or even worse (bad surface without note, while here maybe there is a note)
            dominatingSurface = surfaceStatus.footway
            noteProvided = surfaceStatus.footwayNote
        } else if (surfaceStatus.cycleway in UNDERSPECIFED_SURFACES) {
            // so footway has no bad surface, while cycleway has bad surface
            // lets take worse one
            dominatingSurface = surfaceStatus.cycleway
            noteProvided = surfaceStatus.cyclewayNote
        } else {
            // cycleway is arbitrarily taken as dominating here
            // though for bicycles surface is a bit more important
            dominatingSurface = surfaceStatus.cycleway
        }
    } else {
        dominatingSurface = surfaceStatus.main
        noteProvided = surfaceStatus.note
    }
    // not set but indoor or private -> do not highlight as missing
    val isNotSet = dominatingSurface in UNDERSPECIFED_SURFACES
    val isNotSetButThatsOkay = isNotSet && (isIndoor(element.tags) || isPrivateOnFoot(element))

    val color = if (isNotSetButThatsOkay) {
        Color.INVISIBLE
    } else if (isNotSet && noteProvided != null) {
        Color.BLACK
    } else {
        dominatingSurface.color
    }
    return if (element.tags["area"] == "yes") PolygonStyle(color) else PolylineStyle(StrokeStyle(color))
}

private fun getStyleForSidewalkAsProperty(element: Element): PolylineStyle {
    val sidewalkSides = createSidewalkSides(element.tags)

    // sidewalk data is not set on road -> do not highlight as missing
    if (sidewalkSides == null) {
        return PolylineStyle(StrokeStyle(Color.INVISIBLE))
    }

    val sidewalkSurface = createSidewalkSurface(element.tags)
    val leftColor =
        if (sidewalkSides.left != Sidewalk.YES) Color.INVISIBLE
        else sidewalkSurface?.left.color
    val rightColor =
        if (sidewalkSides.right != Sidewalk.YES) Color.INVISIBLE
        else sidewalkSurface?.right.color

    if (leftColor == Color.DATA_REQUESTED || rightColor == Color.DATA_REQUESTED) {
        // yes, there is an edge case where one side has data set, one unset
        // and it will be not shown
        if (isPrivateOnFoot(element)) {
            return PolylineStyle(StrokeStyle(Color.INVISIBLE))
        }
    }

    return PolylineStyle(
        stroke = null,
        strokeLeft = StrokeStyle(leftColor),
        strokeRight = StrokeStyle(rightColor)
    )
}

private val SurfaceAndNote?.color: String get() =
    if (this?.value in UNDERSPECIFED_SURFACES && this?.note != null) Color.BLACK
    else this?.value.color

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"
