package de.westnordost.streetcomplete.quests.fixme_show

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment


class ShowAddressInterpolation() : OsmFilterQuestType<Boolean>() {

    override val elementFilter =
        "nodes, ways, relations with addr:interpolation"

    override val changesetComment = "unused commit message"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_show_address_interpolation

    override fun createForm() = YesNoQuestAnswerFragment();

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        TODO("Not yet implemented")
    }

    override val wikiLink: String?
        get() = TODO("Not yet implemented")

    override val questTypeAchievements: List<QuestTypeAchievement>
        get() = listOf()
}
