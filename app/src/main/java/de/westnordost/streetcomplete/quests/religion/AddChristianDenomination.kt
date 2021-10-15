package de.westnordost.streetcomplete.quests.religion

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN

class AddChristianDenomination : OsmFilterQuestType<Denomination>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
            amenity = place_of_worship
            or
            amenity = monastery
        )
        and religion=christian
        and !denomination or denomination=catholic
    """
    override val commitMessage = "Add christian denomination for place of worship"
    override val wikiLink = "Key:denomination"
    override val icon = R.drawable.ic_quest_christian

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>): Int =
        if (tags.containsKey("name"))
            R.string.quest_denomination_for_christian_place_of_worship_name_title
        else
            R.string.quest_denomination_for_christian_place_of_worship_title


    override fun createForm() = AddChristianDenominationForm()

    override fun applyAnswerTo(answer: Denomination, changes: StringMapChangesBuilder) {
        changes.addOrModify("denomination", answer.osmValue)
    }
}
