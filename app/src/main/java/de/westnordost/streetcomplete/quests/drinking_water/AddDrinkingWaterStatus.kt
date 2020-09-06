package de.westnordost.streetcomplete.quests.drinking_water

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi


class AddDrinkingWaterStatus(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<List<String>>(o) {

    override val tagFilters =
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
        } else if ("drinking_water:signed=no" == value) {
            changes.add("drinking_water:signed", "no")
        }
    }
}
