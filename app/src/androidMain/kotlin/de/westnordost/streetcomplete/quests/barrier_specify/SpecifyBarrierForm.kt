package de.westnordost.streetcomplete.quests.barrier_specify

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.view.image_select.Item

class SpecifyBarrierForm : AImageListQuestForm<String, String>() {

    override val items = listOf(
        Item("fence", R.drawable.ic_quest_power, R.string.quest_specify_barier_fence),
        Item("wall", R.drawable.ic_quest_power, R.string.quest_specify_barier_wall),
        Item("bollard", R.drawable.ic_quest_power, R.string.quest_specify_barier_bollard)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.single())
    }
}
