package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoadSurfaceOverlayKtTest {
    @Test
    fun `matching way is found`() {
        val data = way(tags = mapOf(
            "highway" to "motorway",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(1, RoadSurfaceOverlay().getStyledElements(mapData).toList().size)
    }

    @Test
    fun `way with surface note and surface tag is eligible`() {
        val data = way(tags = mapOf(
            "highway" to "tertiary",
            "surface" to "paved",
            "surface:note" to "patches of concrete and asphalt within sett",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(1, RoadSurfaceOverlay().getStyledElements(mapData).toList().size)
    }

    @Test
    fun `adding surface note to unspecified surface changes line colour`() {
        val withNoteData = way(tags = mapOf(
            "highway" to "track",
            "surface" to "paved",
            "surface:note" to "patches of concrete and asphalt within sett",
        ))
        val withoutNoteData = way(tags = mapOf(
            "highway" to "track",
            "surface" to "paved",
        ))
        val mapDataWithNote = TestMapDataWithGeometry(listOf(withNoteData))
        val mapDataWithoutNote = TestMapDataWithGeometry(listOf(withoutNoteData))
        val styleWithNote = RoadSurfaceOverlay().getStyledElements(mapDataWithNote).first().second
        val styleWithoutNote = RoadSurfaceOverlay().getStyledElements(mapDataWithoutNote).first().second
        assertTrue(styleWithNote is PolylineStyle)
        assertTrue(styleWithoutNote is PolylineStyle)
        if (styleWithNote is PolylineStyle && styleWithoutNote is PolylineStyle) {
            assertNotEquals(styleWithNote.stroke!!.color, styleWithoutNote.stroke!!.color)
        }
    }

    @Test
    fun `way with surface note without surface tag is ineligible`() {
        val data = way(tags = mapOf(
            "highway" to "tertiary",
            "surface:note" to "explanation for missing surface tag",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(0, RoadSurfaceOverlay().getStyledElements(mapData).toList().size)
    }

    @Test
    fun `way with unsupported surface is ineligible`() {
        val data = way(tags = mapOf(
            "highway" to "track",
            "surface" to "https://en.wikipedia.org/wiki/Stone_frigate",
            "surface:note" to "patches of concrete and asphalt within sett",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(0, RoadSurfaceOverlay().getStyledElements(mapData).toList().size)
    }

    @Test
    fun `way with check date is eligible`() {
        val data = way(tags = mapOf(
            "highway" to "track",
            "surface" to "asphalt",
            "check_date:surface" to "2022-10-11",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(1, RoadSurfaceOverlay().getStyledElements(mapData).toList().size)
    }

    @Test
    fun `way with surface tags being replaced is eligible`() {
        val data = way(tags = mapOf(
            "highway" to "track",
            "surface" to "cobblestone",
        ))
        val mapData = TestMapDataWithGeometry(listOf(data))
        assertEquals(1, RoadSurfaceOverlay().getStyledElements(mapData).toList().size)
    }

    @Test
    fun `surfaces for motorways can be assumed as concrete and asphalt is shown in the same way`() {
        // if not then
        // if (dominatingSurface == null && (element.tags["highway"] == "motorway" || element.tags["highway"] == "motorway_link")) {
        // in getStyle of RoadSurfaceOverlay needs to be replaced by putting motorway into isNotSetButThatsOkay
        assertEquals(Surface.ASPHALT.color, Surface.PAVING_STONES.color)
    }
}