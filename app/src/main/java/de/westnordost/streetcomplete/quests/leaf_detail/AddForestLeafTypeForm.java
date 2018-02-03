package de.westnordost.streetcomplete.quests.leaf_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class AddForestLeafTypeForm extends ImageListQuestAnswerFragment
{
	protected static final int MORE_THAN_99_PERCENT_COVERED = 2;

	private static final Item[] LEAF_TYPES = new Item[]{
            new Item("needleleaved", R.drawable.leaf_type_needleleaved, R.string.quest_forestLeaf_needleleaved_answer),
            new Item("mixed", R.drawable.ic_arrow_drop_down_white_24dp, R.string.quest_forestLeaf_mixed_answer),
            new Item("broadleaved", R.drawable.leaf_type_broadleaved, R.string.quest_forestLeaf_broadleaved_answer),
	};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		imageSelector.setCellLayout(R.layout.cell_icon_select_with_label_below);
		return view;
	}


	@Override protected int getMaxSelectableItems()
	{
		return 1;
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return MORE_THAN_99_PERCENT_COVERED;
	}

	@Override protected Item[] getItems()
	{
		return LEAF_TYPES;
	}
}