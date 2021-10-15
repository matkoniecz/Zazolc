package de.westnordost.streetcomplete.quests.religion

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.religion.Denomination.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddChristianDenominationForm : AImageListQuestAnswerFragment<Denomination, Denomination>() {

    /*
    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_religion_for_place_of_worship_answer_multi) { applyAnswer(MULTIFAITH) }
    )
    */

    override val items get() = listOf(
        // sorted by worldwide usages, *minus* country specific ones
        Item(ROMAN_CATHOLIC,    R.drawable.ic_religion_christian, R.string.quest_religion_roman_catholic),
    ).sortedBy { countryInfo.popularChristianDenominations.indexOf(it.value!!.osmValue) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<Denomination>) {
        applyAnswer(selectedItems.single())
    }
}

