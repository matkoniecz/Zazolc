package de.westnordost.streetcomplete.quests.bike_parking_type;

import android.os.Bundle;

import java.util.ArrayList;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddBikeParkingType extends SimpleOverpassQuestType
{
	@Inject public AddBikeParkingType(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "nodes, ways with amenity=bicycle_parking and access!=private and !bicycle_parking";
	}

	@Override
	public int importance()
	{
		return QuestImportance.BICYCLE_PRIME;
	}

	public AbstractQuestAnswerFragment createForm()
	{
        return new AddBikeParkingTypeForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(AddBikeParkingTypeForm.OSM_VALUES);
		if(values != null  && values.size() == 1)
		{
			changes.add("bicycle_parking", values.get(0));
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add bicycle parkings cover";
	}

	@Override public String getIconName() {	return "bicycle_parking"; }
}