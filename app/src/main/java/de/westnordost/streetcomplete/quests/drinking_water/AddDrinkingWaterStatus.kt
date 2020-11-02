package de.westnordost.streetcomplete.quests.drinking_water

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType


class AddDrinkingWaterStatus() : OsmFilterQuestType<List<String>>() {

    override val elementFilter =
        """nodes, ways, relations with
           (amenity=drinking_water or drinking_water=yes)
           and drinking_water != no
           and !drinking_water:legal
           and !drinking_water:signed
           """.trimMargin()

    override val commitMessage = "Add drinking water status"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_drinking_water_status

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["fixme"]
        return if (name != null) arrayOf(name) else arrayOf()
    }

    override fun createForm() = ShowFixmeForm()

    override fun applyAnswerTo(answer: List<String>, changes: StringMapChangesBuilder) {
        val value = answer.first()
        if ("drinking_water:legal=yes" == value) {
             changes.add("drinking_water:legal", "yes")
        } else if ("drinking_water:legal=no" == value) {
            changes.add("drinking_water:legal", "no")
        } else if ("drinking_water:legal=unknown" == value) {
            changes.add("drinking_water:legal", "unknown")
        }
    }
}
