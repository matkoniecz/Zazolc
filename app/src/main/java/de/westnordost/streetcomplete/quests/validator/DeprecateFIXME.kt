package de.westnordost.streetcomplete.quests.validator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm

class DeprecateFIXME() : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "nodes, ways, relations with FIXME and !fixme"
    override val changesetComment = "convert FIXME to fixme"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_convert_FIXME_to_fixme

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer) {
            tags["fixme"] = tags["FIXME"]!!
            tags.remove("FIXME")
        }
    }

    override val wikiLink = "Key:fixme"

    override val achievements: List<EditTypeAchievement> = listOf()
}
