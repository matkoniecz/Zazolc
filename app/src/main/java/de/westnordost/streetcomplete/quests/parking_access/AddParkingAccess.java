package de.westnordost.streetcomplete.quests.parking_access;

import android.os.Bundle;

import java.util.ArrayList;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddParkingAccess extends SimpleOverpassQuestType
{
	@Inject public AddParkingAccess(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "nodes, ways, relations with amenity=parking and !access";
	}

	public AbstractQuestAnswerFragment createForm()
	{
        return new AddParkingAccessForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(AddParkingAccessForm.OSM_VALUES);
		if(values != null  && values.size() == 1)
		{
			changes.add("access", values.get(0));
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add parking access";
	}

	@Override public int getIcon() { return R.drawable.ic_quest_parking; }
}