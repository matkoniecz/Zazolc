package de.westnordost.streetcomplete.quests.service_times

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddReligiousServiceTimes : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes, ways, relations with
          amenity = place_of_worship
          and !service_times
          and !service_times:mass
          and !service_times:signed
    """
    override val changesetComment = "Add info about service times"
    override val wikiLink = "Key:service_times"
    override val icon = R.drawable.ic_quest_religion
    override val isDeleteElementEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_service_times

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        if(!answer){
            tags["service_times:signed"] = answer.toYesNo()
            tags["service_times:mass:signed"] = answer.toYesNo()
        }
    }

    override val achievements: List<EditTypeAchievement> = listOf()
}
