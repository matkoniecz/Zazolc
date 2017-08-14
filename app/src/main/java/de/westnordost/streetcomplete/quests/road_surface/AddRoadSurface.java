package de.westnordost.streetcomplete.quests.road_surface;

import android.os.Bundle;
import android.text.TextUtils;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddRoadSurface extends SimpleOverpassQuestType {
	@Inject public AddRoadSurface(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return " ways with ( highway ~ " + TextUtils.join("|", RoadSurfaceConfig.ROADS_WITH_SURFACES) + " and" +
			   " !surface and access!=private and access!=no)";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new AddRoadSurfaceForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.add("surface", answer.getString(AddRoadSurfaceForm.SURFACE));
	}

	@Override public String getCommitMessage()
	{
		return "Add highway=* surfaces";
	}

	@Override public String getIconName() {	return "street_surface"; }
}