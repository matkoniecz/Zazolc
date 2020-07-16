package de.westnordost.streetcomplete.quests.construction

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.meta.SURVEY_MARK_KEY
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.tagfilters.getQuestPrintStatement
import de.westnordost.streetcomplete.data.tagfilters.toGlobalOverpassBBox
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

open class MarkCompletedConstructionMinorOrGeneric(private val overpass: OverpassMapDataAndGeometryApi)
    : OsmElementQuestType<Boolean> {

    override val commitMessage = "Determine whether construction is now completed"
    override val wikiLink = "Tag:construction=yes"
    override val icon = R.drawable.ic_quest_road_construction
    override val hasMarkersAtEnds = true

    override fun getTitle(tags: Map<String, String>): Int {
        val isRoad = ALL_ROADS.contains(tags["construction"])
        val isCycleway = tags["construction"] == "cycleway"
        val isFootway = tags["construction"] == "footway"
        val isBridge = tags["man_made"] == "bridge"

        return when {
            isRoad -> R.string.quest_construction_road_title
            isCycleway -> R.string.quest_construction_cycleway_title
            isFootway -> R.string.quest_construction_footway_title
            isBridge -> R.string.quest_construction_bridge_title
            else -> R.string.quest_construction_even_more_generic_title
        }
    }

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpass.query(getOverpassQuery(bbox), handler)
    }

    /** @return overpass query string to get streets marked as under construction but excluding ones
     * - with invalid construction tag
     * - with tagged opening date that is in future
     * - recently edited (includes adding/updating check_date tags)
     */
    private fun getOverpassQuery(bbox: BoundingBox): String {
        return bbox.toGlobalOverpassBBox() + """
            (
            way[construction=yes]${isNotInFuture("opening_date")};
            node[construction=yes]${isNotInFuture("opening_date")};
            relation[construction=yes]${isNotInFuture("opening_date")};
            way[construction=minor]${isNotInFuture("opening_date")};
            node[construction=minor]${isNotInFuture("opening_date")};
            relation[construction=minor]${isNotInFuture("opening_date")};
            )-> .with_unknown_state;
            (
            way[construction=yes]${hasRecentlyBeenEdited(40)};
            node[construction=yes]${hasRecentlyBeenEdited(40)};
            relation[construction=yes]${hasRecentlyBeenEdited(40)};
            way[construction=minor]${hasRecentlyBeenEdited(40)};
            node[construction=minor]${hasRecentlyBeenEdited(40)};
            relation[construction=minor]${hasRecentlyBeenEdited(40)};
            )-> .recently_edited;
            (.with_unknown_state; - .recently_edited;);
        """.trimIndent() + "\n" + getQuestPrintStatement()
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (answer) {
            deleteTagsDescribingConstruction(changes) //includes deletion of construction=yes/minor
        } else {
            changes.addOrModify(SURVEY_MARK_KEY, getCurrentDateString())
        }
    }
}
