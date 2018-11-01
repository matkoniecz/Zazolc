package de.westnordost.streetcomplete.quests.leaf_detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class AddForestLeafCycleForm extends ImageListQuestAnswerFragment {
	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		imageSelector.setCellLayout(R.layout.cell_icon_select_with_label_below);
	}

	@Override protected int getMaxSelectableItems()
	{
		return 1;
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return getItems().length;
	}

	@Override protected Item[] getItems() {
		return new Item[]{
			new Item("deciduous", R.drawable.ic_religion_christian, R.string.quest_forestLeaf_decidous_answer),
			new Item("evergreen", R.drawable.ic_religion_christian, R.string.quest_forestLeaf_evergreen_answer),
			new Item("mixed", R.drawable.ic_religion_christian, R.string.quest_forestLeaf_mixed_answer),
			new Item("semi_deciduous", R.drawable.ic_religion_christian, R.string.quest_forestLeaf_semi_deciduous_answer),
		};
	}
}

