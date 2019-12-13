package de.westnordost.streetcomplete.quests.footway

import android.os.Bundle
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import kotlinx.android.synthetic.main.quest_parking_access.*

class AddFootwayForm : AbstractQuestFormAnswerFragment<String>() {

    override val contentLayoutResId = R.layout.quest_footway

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioButtonGroup.setOnCheckedChangeListener { _, _ -> checkIsFormComplete() }
    }

    override fun onClickOk() {
        applyAnswer(when (radioButtonGroup.checkedRadioButtonId) {
            R.id.sidewalk                 -> "sidewalk"
            R.id.crossing                 -> "crossing"
            R.id.not_attached_to_the_road -> "not_attached_to_the_road"
            else -> throw NullPointerException()
        })
    }

    override fun isFormComplete() = radioButtonGroup.checkedRadioButtonId != -1
}
