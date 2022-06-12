package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.image_select.Item

class RemoveWrongSurfaceForm : AImageListQuestAnswerFragment<WrongSurfaceOrTRacktype, WrongSurfaceAnswer>() {
    //*
    override val items =
        listOf(
            Item(WrongSurfaceOrTRacktype.WRONG_TRACKTYPE, R.drawable.surface_gravel, R.string.quest_surface_value_gravel),
            Item(WrongSurfaceOrTRacktype.WRONG_SURFACE, R.drawable.surface_paved, R.string.quest_surface_value_surface_is_solid),
        )
    //*/
    /*
    override val items = if (pavedAccordingToTracktype()) {
        listOf(
            getSurfaceItem(),
            Item(WrongSurfaceOrTRacktype.WRONG_SURFACE, R.drawable.surface_paved, R.string.quest_surface_value_surface_is_not_solid),
        )
    } else {
        listOf(
            getSurfaceItem(),
            Item(WrongSurfaceOrTRacktype.WRONG_SURFACE, R.drawable.surface_paved, R.string.quest_surface_value_surface_is_solid),
        )
    }
    // */

    private fun pavedAccordingToTracktype(): Boolean {
        val t = null
        val e = osmElement!!
        val tags = osmElement!!.tags
        val surface = osmElement!!.tags["surface"]!!
        return osmElement!!.tags["tracktype"]!! == "grade1"
    }

    private fun getSurfaceItem(): Item<WrongSurfaceOrTRacktype> {
        val surface = osmElement!!.tags["surface"]!!
        for (item in PAVED_SURFACES + UNPAVED_SURFACES + GROUND_SURFACES + GENERIC_ROAD_SURFACES) {
            if (item.osmValue == surface) {
                return Item(WrongSurfaceOrTRacktype.WRONG_TRACKTYPE, item.asItem().drawableId, item.asItem().titleId)
            }
        }
        throw Exception("this should never happen")
    }

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<WrongSurfaceOrTRacktype>) {
        if (selectedItems.single() == WrongSurfaceOrTRacktype.WRONG_TRACKTYPE) {
            applyAnswer(TracktypeIsWrong())
        }
        if (selectedItems.single() == WrongSurfaceOrTRacktype.WRONG_SURFACE) {
            applyAnswer(SpecificSurfaceIsWrong())
        }
    }
}
