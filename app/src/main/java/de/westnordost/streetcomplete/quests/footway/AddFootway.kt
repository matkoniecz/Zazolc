package de.westnordost.streetcomplete.quests.footway

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.tagfilters.getQuestPrintStatement
import de.westnordost.streetcomplete.data.tagfilters.toGlobalOverpassBBox

class AddFootway(private val overpassServer: OverpassMapDataAndGeometryApi) : OsmElementQuestType<String> {

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpassServer.query(getOverpassQuery(bbox), handler)
    }

    private fun getOverpassQuery(bbox: BoundingBox) = """
        ${bbox.toGlobalOverpassBBox()}
        way[!covered][!tunnel]["highway"~"motorway|motorway_link|trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|service|residential|unclassified|living_street|track|pedestrian|road"]["access"!~"no|private"][!area];
        (way(around:5)["highway"~"footway|path"]["footway"!="crossing"][!tunnel];);
        ${getQuestPrintStatement()}""".trimIndent()

    //override val tagFilters = "ways with highway ~ footway|path and !footway"
    override val commitMessage = "Add footway tag"
    override val icon = R.drawable.ic_quest_sidewalk

    override fun getTitle(tags: Map<String, String>) = R.string.quest_footway_title

    override fun createForm() = AddFootwayForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.addOrModify("footway", answer)
    }

    override fun isApplicableTo(element: Element):Boolean? = null
}
