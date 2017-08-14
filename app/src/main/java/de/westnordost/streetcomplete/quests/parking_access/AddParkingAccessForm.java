package de.westnordost.streetcomplete.quests.parking_access;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.TextListQuestAnswerFragment;

public class AddParkingAccessForm extends TextListQuestAnswerFragment {
	protected static final int MORE_THAN_96_PERCENT_COVERED = 3;

	// access=* 448k https://taginfo.openstreetmap.org/tags/amenity=parking#combinations
	// access=private 187k https://taginfo.openstreetmap.org/tags/access=private#combinations
	// access=yes 48k https://taginfo.openstreetmap.org/tags/access=yes#combinations
	// access=no 7k https://taginfo.openstreetmap.org/tags/access=no#combinations
	// access=permissive 42k https://taginfo.openstreetmap.org/tags/access=permissive#combinations
	// access=destination 8k https://taginfo.openstreetmap.org/tags/access=destination#combinations
	// access=customers 125k https://taginfo.openstreetmap.org/tags/access=customers#combinations
	// access=agricultural 0k https://taginfo.openstreetmap.org/tags/access=agricultural#combinations
	// access=forestry 0k https://taginfo.openstreetmap.org/tags/access=forestry#combinations
	// amenity=designated 1k https://taginfo.openstreetmap.org/tags/access=designated#combinations
	// access=public 9k https://taginfo.openstreetmap.org/tags/access=public#combinations
	// access=unknown 8k https://taginfo.openstreetmap.org/tags/access=unknown#combinations

	//* with 449k in total
	//yes, permissive, public are de facto equivalent with 99k
	//private, no are de facto equivalent with 195k
	//customers, destination are de facto equivalent with 133k
	//so these three have 432/449, more than 96%
	//access=designated is invalid tagging
	//access=unknown makes no sense whatsoever - why not just leave it blank?
	//access=agricultural/forestry is basically unused

	private static final OsmItem[] PARKING_TYPES = new OsmItem[]{
			new OsmItem("yes", R.string.quest_parkingAccess_yes_answer),
			new OsmItem("private", R.string.quest_parkingAccess_private_answer),
			new OsmItem("customers", R.string.quest_parkingAccess_customers_answer),
	};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setTitle(R.string.quest_parkingAccess_title);
		textSelector.setCellLayout(R.layout.text_select_cell);
		return view;
	}

	@Override protected int getMaxSelectableItems()
	{
		return 1;
	}

	@Override protected int getMaxNumberOfInitiallyShownItems()
	{
		return MORE_THAN_96_PERCENT_COVERED;
	}

	@Override protected OsmItem[] getItems()
	{
		return PARKING_TYPES;
	}
}
