package de.westnordost.streetcomplete.quests.drinking_water

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class ShowFixmeForm : AImageListQuestAnswerFragment<String, List<String>>() {

    override val items get() = listOf(
        Item("drinking_water:legal=yes", R.drawable.ic_religion_christian, R.string.quest_drinkable_water_officially_drinkable),
        Item("drinking_water:legal=no", R.drawable.ic_religion_christian, R.string.quest_drinkable_water_officially_not_drinkable),
        Item("drinking_water:legal=unknown", R.drawable.ic_religion_christian, R.string.quest_drinkable_water_officiall_unknown)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below

    }

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems)
    }
}
