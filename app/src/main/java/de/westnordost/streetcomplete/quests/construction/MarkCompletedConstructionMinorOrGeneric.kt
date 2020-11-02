package de.westnordost.streetcomplete.quests.construction

import de.westnordost.streetcomplete.data.meta.ALL_ROADS

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.SURVEY_MARK_KEY
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import java.util.*

class MarkCompletedConstructionMinorOrGeneric() : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        ways with construction = yes or construction = minor
         and (!opening_date or opening_date < today)
         and older today -1 months
    """
    override val commitMessage = "Determine whether construction is now completed"
    override val wikiLink = "Tag:construction=yes"
    override val icon = R.drawable.ic_quest_building_construction

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

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (answer) {
            deleteTagsDescribingConstruction(changes) //includes deletion of construction=yes/minor
        } else {
            changes.addOrModify(SURVEY_MARK_KEY, Date().toCheckDateString())
        }
    }
}
