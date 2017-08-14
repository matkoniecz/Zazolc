package de.westnordost.streetcomplete.quests.bike_parking_type;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.TextListQuestAnswerFragment;

public class AddBikeParkingTypeForm extends TextListQuestAnswerFragment {
	protected static final int MORE_THAN_90_PERCENT_COVERED = 4;

	private static final OsmItem[] PARKING_TYPES = new OsmItem[]{
			new OsmItem("stand", R.string.quest_bicycleParkingType_stand_answer),
			new OsmItem("wall_loop", R.string.quest_bicycleParkingType_wall_loop_answer),
	};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle(R.string.quest_bicycleParkingType_title);
		textSelector.setCellLayout(R.layout.text_select_cell);
		return view;
	}

	@Override protected int getMaxSelectableItems()
	{
		return 1;
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return MORE_THAN_90_PERCENT_COVERED;
	}

	@Override protected TextListQuestAnswerFragment.OsmItem[] getItems()
	{
		return PARKING_TYPES;
	}
}
