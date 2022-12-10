package de.westnordost.streetcomplete.quests.validator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm

class AddAlsoShopForInsurance() : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "nodes, ways, relations with office=insurance and !shop and name"
    override val changesetComment = "better tagging for insurance shop"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_add_also_shop_variant_for_insurance_shop

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer){
            tags["shop"] = "insurance"
        }
    }

    override val wikiLink = "Tag:shop=insurance"

    override val achievements: List<EditTypeAchievement> = listOf()
}
