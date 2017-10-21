package de.westnordost.streetcomplete.quests.road_surface;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.Map;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddServiceRoadSurface extends SimpleOverpassQuestType
{
	public AddServiceRoadSurface(OverpassMapDataDao overpassServer) {
		super(overpassServer);
	}

	@Override protected String getTagFilters()
	{
		return " ways with highway=service and" +
				" !surface and service != parking_aisle and (access !~ private|no or (foot and foot !~ private|no))";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddRoadSurfaceForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.add("surface", answer.getString(AddRoadSurfaceForm.SURFACE));
	}

	@Override public String getCommitMessage() { return "Add highway=* surfaces"; }
	@Override public int getIcon() { return R.drawable.ic_quest_street_surface; }
	@Override public int getTitle(Map<String, String> tags)
	{
		boolean hasName = tags.containsKey("name");
		if(hasName) return R.string.quest_streetSurface_name_title;
		else        return R.string.quest_streetSurface_title;
	}
}
