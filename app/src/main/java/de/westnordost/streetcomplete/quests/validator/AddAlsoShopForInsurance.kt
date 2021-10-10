package de.westnordost.streetcomplete.quests.validator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddAlsoShopForInsurance() : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "nodes, ways, relations with office=insurance and !shop and name"
    override val commitMessage = "better tagging for insurance shop"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_add_also_shop_variant_for_insurance_shop

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (answer){
            changes.add("shop", "insurance");
        }
    }

    override val wikiLink = "Tag:shop=insurance"

    override val questTypeAchievements: List<QuestTypeAchievement>
        get() = listOf()
}
