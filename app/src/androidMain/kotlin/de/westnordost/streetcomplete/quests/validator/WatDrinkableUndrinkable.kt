package de.westnordost.streetcomplete.quests.validator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm

class WatUndrinkableDrinkable() : OsmFilterQuestType<Boolean>() {
    override val elementFilter = "nodes, ways, relations with amenity=drinking_water and drinking_water=no"
    override val changesetComment = ""
    override val icon = R.drawable.ic_quest_railway

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wat_drinkability_status_detected

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {}

    override val wikiLink = "Tag:drinking_water=no"

    override val achievements: List<EditTypeAchievement> = listOf()
}
