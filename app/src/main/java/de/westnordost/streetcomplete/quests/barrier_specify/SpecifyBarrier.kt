package de.westnordost.streetcomplete.quests.barrier_specify

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
class SpecifyBarrier() : OsmFilterQuestType<String>() {

    override val elementFilter = "ways with barrier=yes"
    override val changesetComment = "Specify barrier type"
    override val wikiLink = "Key:barrier"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_specify_fence

    override fun createForm() = SpecifyBarrierForm()

    override fun applyAnswerTo(answer: String, tags: Tags, timestampEdited: Long) {
        tags["barrier"] = answer
    }

    override val achievements: List<EditTypeAchievement> = listOf()
}
