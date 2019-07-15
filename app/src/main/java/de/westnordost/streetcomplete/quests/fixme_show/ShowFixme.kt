package de.westnordost.streetcomplete.quests.fixme_show

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao



class ShowFixme(o: OverpassMapDataDao) : SimpleOverpassQuestType<List<String>>(o) {

    override val tagFilters =
                "nodes, ways, relations with fixme and fixme!=continue and highway!=proposed and railway!=proposed" +
                " and !fixme:requires_aerial_image " +
                " and !fixme:use_better_tagging_scheme " +
                " and !fixme:3d_tagging "

    override val commitMessage = "Handle fixme tag"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_show_fixme

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String?> {
        val name = tags["fixme"]
        return if (name != null) arrayOf(name) else arrayOf()
    }

    override fun createForm() = ShowFixmeForm()

    override fun applyAnswerTo(answer: List<String>, changes: StringMapChangesBuilder) {
        val value = answer.first()
        if ("fixme:solved" == value) {
            //TODO: handle it without magic values
            changes.delete("fixme")
        } else {
            changes.add(value, "yes")
        }
    }
}
