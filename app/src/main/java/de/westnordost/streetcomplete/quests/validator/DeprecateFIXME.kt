package de.westnordost.streetcomplete.quests.validator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class DeprecateFIXME() : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "nodes, ways, relations with FIXME and !fixme"
    override val commitMessage = "convert FIXME to fixme"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_convert_FIXME_to_fixme

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["FIXME"]
        return if (name != null) arrayOf(name) else arrayOf()
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (answer){
            val fixme = changes.getPreviousValue("FIXME")!!
            changes.delete("FIXME")
            changes.add("fixme", fixme)
        }
    }

    override val wikiLink = "Key:fixme"
}
