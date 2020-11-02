package de.westnordost.streetcomplete.quests.validator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class FixBogusGallery() : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "nodes, ways, relations with tourism=gallery and fee=no and shop!=art and name"
    override val commitMessage = "fix art shop mistagged as tourism=gallery"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_fix_mistagged_art_shop

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (answer){
            changes.add("shop", "art");
            changes.delete("tourism");
        }
    }
}
