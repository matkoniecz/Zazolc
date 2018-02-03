package de.westnordost.streetcomplete.quests.leaf_detail;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddForestLeafType extends SimpleOverpassQuestType
{
	@Inject public AddForestLeafType(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "ways, relations with (natural=wood or landuse=forest) and !leaf_type";
	}

	public AbstractQuestAnswerFragment createForm()
	{
        return new AddForestLeafTypeForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(AddForestLeafTypeForm.OSM_VALUES);
		if(values != null  && values.size() == 1)
		{
			changes.add("leaf_type", values.get(0));
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add leaf_type";
	}

	@Override
	public int getTitle(@NonNull Map<String, String> tags) {
		return R.string.quest_forestLeaf_title;
	}

	@Override public int getIcon() { return R.drawable.ic_quest_leaf; }
}