package de.westnordost.streetcomplete.quests.road_surface;

import android.os.Bundle;
import android.text.TextUtils;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class DetailPavedRoadSurface extends SimpleOverpassQuestType {
	@Inject public DetailPavedRoadSurface(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return " ways with ( highway ~ " + TextUtils.join("|", RoadSurfaceConfig.ROADS_WITH_SURFACES) + " and" +
				" surface=paved and !cycleway:surface and !footway:surface)";
		// cycleway:surface, footway:surface - it means that single highway=* represents
		// multiple parts of roads, with different surfaces. In such case using more detailed
		// surface tag is likely to be impossible
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new DetailPavedRoadSurfaceForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.modify("surface", answer.getString(DetailPavedRoadSurfaceForm.SURFACE));
	}

	@Override public String getCommitMessage()
	{
		return "Detail highway=* surfaces";
	}

	@Override public String getIconName()
	{
		return "street_surface";
	}
}
