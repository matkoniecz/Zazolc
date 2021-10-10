package de.westnordost.streetcomplete.quests.access_waste_disposal

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddWasteDisposalAccess : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "nodes, ways, relations with amenity = waste_disposal and (!access or access = unknown)"
    override val commitMessage = "Add garbage dumpster access"
    override val wikiLink = "Key:access"
    override val icon = R.drawable.ic_quest_bin_public_transport

    override fun getTitle(tags: Map<String, String>) = R.string.quest_waste_disposal_access_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("access", if (answer) "yes" else "private")
    }

    override val questTypeAchievements: List<QuestTypeAchievement>
        get() = listOf()
}
