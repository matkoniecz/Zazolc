package de.westnordost.streetcomplete.quests.street_cabinet

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddStreetCabinetTypeForm : AImageListQuestForm<StreetCabinetType, StreetCabinetType>() {

    override val items = StreetCabinetType.values().map { it.asItem() }
    override val itemsPerRow = 4
    override val moveFavoritesToFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        element.tags["operator"]?.let { setTitle(resources.getString((questType as OsmElementQuestType<*>).getTitle(element.tags)) + " ($it)") }
    }

    override fun onClickOk(selectedItems: List<StreetCabinetType>) {
        applyAnswer(selectedItems.single())
    }
}
