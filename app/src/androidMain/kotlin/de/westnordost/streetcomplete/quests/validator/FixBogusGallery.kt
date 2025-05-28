package de.westnordost.streetcomplete.quests.validator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm

class FixBogusGallery() : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "nodes, ways, relations with tourism=gallery and fee=no and shop!=art and name"
    override val changesetComment = "fix art shop mistagged as tourism=gallery"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_fix_mistagged_art_shop

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer) {
            tags["shop"] = "art"
            tags.remove("tourism")
        }
    }

    override val wikiLink = "Tag:shop=art"

    override val achievements: List<EditTypeAchievement> = listOf()
}
