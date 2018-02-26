package de.westnordost.streetcomplete.quests.leaf_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.TextListQuestAnswerFragment;

public class AddForestLeafCycleForm extends TextListQuestAnswerFragment {
	private static final OsmItem[] LEAF_TYPES = new OsmItem[]{
			new OsmItem("deciduous", R.string.quest_forestLeaf_decidous_answer),
			new OsmItem("evergreen", R.string.quest_forestLeaf_evergreen_answer),
			new OsmItem("mixed", R.string.quest_forestLeaf_mixed_answer),
			new OsmItem("semi_deciduous", R.string.quest_forestLeaf_semi_deciduous_answer),
	};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		textSelector.setCellLayout(R.layout.text_select_cell);
		return view;
	}

	@Override protected int getMaxSelectableItems()
	{
		return 1;
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return LEAF_TYPES.length;
	}

	@Override protected OsmItem[] getItems()
	{
		return LEAF_TYPES;
	}
}
