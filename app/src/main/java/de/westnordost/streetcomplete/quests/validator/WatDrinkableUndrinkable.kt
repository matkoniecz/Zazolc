package de.westnordost.streetcomplete.quests.validator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class WatUndrinkableDrinkable() : OsmFilterQuestType<Boolean>() {
    override val elementFilter = "nodes, ways, relations with amenity=drinking_water and drinking_water=no"
    override val commitMessage = ""
    override val icon = R.drawable.ic_quest_railway

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wat_drinkability_status_detected

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {}

    override val wikiLink = "Tag:drinking_water=no"

    override val questTypeAchievements: List<QuestTypeAchievement>
        get() = listOf()
}
