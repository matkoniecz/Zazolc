package de.westnordost.streetcomplete.quests.fixme_show

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.image_select.Item

class ShowFixmeForm : AImageListQuestAnswerFragment<String, List<String>>() {

    override val items get() = listOf(
            Item("fixme:solved", R.drawable.ic_religion_christian, R.string.quest_ShowFixme_solved_answer),
            Item("fixme:requires_aerial_image", R.drawable.ic_religion_christian, R.string.quest_ShowFixme_requiresAerial_answer),
            Item("fixme:use_better_tagging_scheme", R.drawable.ic_religion_christian, R.string.quest_ShowFixme_pure_taggery_answer),
            Item("fixme:3d_tagging", R.drawable.ic_religion_christian, R.string.quest_ShowFixme_3d_tagging_answer)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below

    }

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems)
    }
}
