package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddShopType(o: OverpassMapDataDao) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = "nodes, ways, relation with shop=yes and !amenity and !leisure"
    override val commitMessage = "Specify shop type"
    override val icon = R.drawable.ic_quest_label

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name") || tags.containsKey("brand")
        return if (hasName) R.string.quest_shopType_name_title
        else         R.string.quest_shopType_title
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
    }
}
