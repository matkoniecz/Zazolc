package de.westnordost.streetcomplete.quests.service_times

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddReligiousServiceTimes : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways, relations with
          amenity = place_of_worship
          and !service_times
          and !service_times:mass
          and !service_times:signed
    """
    override val commitMessage = "Add info about service times"
    override val wikiLink = "Key:service_times"
    override val icon = R.drawable.ic_quest_religion
    override val isDeleteElementEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_service_times

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if(!answer){
            changes.add("service_times:signed", answer.toYesNo())
            changes.add("service_times:mass:signed", answer.toYesNo())
        }
    }
}
