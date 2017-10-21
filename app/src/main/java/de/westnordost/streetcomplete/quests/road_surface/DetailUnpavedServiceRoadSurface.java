package de.westnordost.streetcomplete.quests.road_surface;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class DetailUnpavedServiceRoadSurface extends SimpleOverpassQuestType {
	@Inject public DetailUnpavedServiceRoadSurface(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return " ways with highway=service and" +
				" surface=unpaved and service != parking_aisle and !cycleway:surface and !footway:surface";
		// cycleway:surface, footway:surface - it means that single highway=* represents
		// multiple parts of roads, with different surfaces. In such case using more detailed
		// surface tag is likely to be impossible
		// mostly theory for surface=unpaved, but it may happen...
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new DetailUnpavedRoadSurfaceForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.modify("surface", answer.getString(DetailUnpavedRoadSurfaceForm.SURFACE));
	}

	@Override
	public String getCommitMessage()
	{
		return "Detail highway=* surfaces";
	}

	@Override public int getIcon() { return R.drawable.ic_quest_street_surface; }
	@Override public int getTitle(Map<String, String> tags)
	{
		boolean hasName = tags.containsKey("name");
		if(hasName) return R.string.quest_streetSurface_name_title;
		else        return R.string.quest_streetSurface_title;
	}
}
