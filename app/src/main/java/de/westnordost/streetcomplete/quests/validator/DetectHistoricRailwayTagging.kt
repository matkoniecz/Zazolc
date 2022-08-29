package de.westnordost.streetcomplete.quests.validator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm

class DetectHistoricRailwayTagging() : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """nodes, ways, relations with railway~abandoned|dismantled|historic|razed
        or abandoned:railway
        or razed:railway
        or was:railway
        or removed:railway
        or historic:railway
        or demolished:railway""".trimMargin()
    override val changesetComment = ""
    override val icon = R.drawable.ic_quest_railway

    override fun getTitle(tags: Map<String, String>) = R.string.quest_railway_abandoned_detected

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
    }

    override val wikiLink = "Tag:railway=abandoned"

    override val achievements: List<EditTypeAchievement> = listOf()
}
