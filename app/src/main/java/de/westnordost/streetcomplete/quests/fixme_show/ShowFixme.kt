package de.westnordost.streetcomplete.quests.fixme_show

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement

class ShowFixme() : OsmFilterQuestType<List<String>>() {

    override val elementFilter =
                "nodes, ways, relations with fixme and fixme!=continue and highway!=proposed and railway!=proposed" +
                " and !fixme:requires_aerial_image " +
                " and !fixme:use_better_tagging_scheme " +
                " and !fixme:3d_tagging "

    override val changesetComment = "Handle fixme tag"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_show_fixme

    override fun createForm() = ShowFixmeForm()

    override fun applyAnswerTo(answer: List<String>, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        val value = answer.first()
        if ("fixme:solved" == value) {
            //TODO: handle it without magic values
            tags.remove("fixme")
        } else {
            tags[value] = "yes"
        }
    }

    override val wikiLink = "Key:fixme"

    override val achievements: List<EditTypeAchievement> = listOf()
}
