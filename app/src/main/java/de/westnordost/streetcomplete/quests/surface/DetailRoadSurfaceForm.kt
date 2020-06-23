package de.westnordost.streetcomplete.quests.surface

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AGroupedImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.util.TextChangedWatcher
import de.westnordost.streetcomplete.view.Item

class DetailRoadSurfaceForm  : AGroupedImageListQuestAnswerFragment<String, String>() {

    ////////
    //copied from AddRoadSurfaceForm
    override val topItems get() =
            // tracks often have different surfaces than other roads
        if (osmElement!!.tags["highway"] == "track")
            listOf(Surface.DIRT, Surface.GRASS, Surface.PEBBLES, Surface.FINE_GRAVEL, Surface.COMPACTED, Surface.ASPHALT).toItems()
        else
            listOf(Surface.ASPHALT, Surface.CONCRETE, Surface.SETT, Surface.PAVING_STONES, Surface.COMPACTED, Surface.DIRT).toItems()

    override val allItems = listOf(
            Item("paved", R.drawable.panorama_surface_paved, R.string.quest_surface_value_paved, null, listOf(
                    Surface.ASPHALT, Surface.CONCRETE, Surface.PAVING_STONES,
                    Surface.SETT, Surface.UNHEWN_COBBLESTONE, Surface.GRASS_PAVER,
                    Surface.WOOD, Surface.METAL
            ).toItems()),
            Item("unpaved", R.drawable.panorama_surface_unpaved, R.string.quest_surface_value_unpaved, null, listOf(
                    Surface.COMPACTED, Surface.FINE_GRAVEL, Surface.GRAVEL,
                    Surface.PEBBLES
            ).toItems()),
            Item("ground", R.drawable.panorama_surface_ground, R.string.quest_surface_value_ground, null, listOf(
                    Surface.DIRT, Surface.GRASS, Surface.SAND
            ).toItems())
    )
    ////////



    private var isInExplanationMode = false;
    private var explanationInput: EditText? = null

    override val otherAnswers = listOf(
            OtherAnswer(R.string.ic_quest_surface_road_detailed_answer_impossible) { confirmSwitchToNoDetailedTagPossible() }
    )

    private fun setLayout(layoutResourceId: Int) {
        val view = setContentView(layoutResourceId)

        val onChanged = TextChangedWatcher {
            checkIsFormComplete()
        }
        explanationInput = view.findViewById(R.id.noteInput)
        explanationInput?.addTextChangedListener(onChanged)
    }

    private val explanation: String get() = explanationInput?.text?.toString().orEmpty().trim()

    override fun isFormComplete(): Boolean {
        if(isInExplanationMode) {
            return true
        } else {
            return super.isFormComplete()
        }
    }

    override fun onClickOk(value: String) {
        if(isInExplanationMode) {
            applyAnswer(DetailingImpossibleAnswer(explanation))
        } else {
            applyAnswer(SurfaceAnswer(value))
        }
    }

    private fun confirmSwitchToNoDetailedTagPossible() {
        AlertDialog.Builder(activity!!)
                .setMessage(R.string.ic_quest_surface_road_detailed_answer_impossible_confirmation)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) {
                    _, _ -> switchToExplanationLayout()
                }
                .setNegativeButton(R.string.quest_generic_cancel, null)
                .show()

    }

    private fun switchToExplanationLayout(){
        isInExplanationMode = true
        setLayout(R.layout.quest_surface_detailed_answer_impossible)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        isInExplanationMode = savedInstanceState?.getBoolean(IS_IN_EXPLANATION_MODE) ?: false
        setLayout(if (isInExplanationMode) R.layout.quest_surface_detailed_answer_impossible else R.layout.quest_generic_list)

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_IN_EXPLANATION_MODE, isInExplanationMode)
    }

    companion object {
        private const val IS_IN_EXPLANATION_MODE = "is_in_explanation_mode"
    }

}
