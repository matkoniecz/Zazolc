package de.westnordost.streetcomplete.quests.leaf_detail;

import android.os.Bundle;

import java.util.ArrayList;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddTreeLeafCycle extends SimpleOverpassQuestType
{
	@Inject public AddTreeLeafCycle(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "nodes with natural=tree and !leaf_cycle";
	}

	@Override
	public int importance()
	{
		return QuestImportance.INSIGNIFICANT;
	}

	public AbstractQuestAnswerFragment createForm()
	{
        return new AddTreeLeafCycleForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(AddTreeLeafCycleForm.OSM_VALUES);
		if(values != null  && values.size() == 1)
		{
			changes.add("leaf_cycle", values.get(0));
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add leaf_cycle";
	}

	@Override public String getIconName() {	return "leaf"; }
}