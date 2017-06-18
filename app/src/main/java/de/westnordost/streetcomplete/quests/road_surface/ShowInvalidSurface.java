package de.westnordost.streetcomplete.quests.road_surface;

import android.os.Bundle;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class ShowInvalidSurface extends SimpleOverpassQuestType
{
	private static final String[] VALID_SURFACES = {"unpaved", "compacted", "dirt", "earth", "fine_gravel",
			"grass", "grass_paver", "gravel", "ground", "mud", "pebblestone", "salt", "sand",
			"woodchips", "clay", "paved", "asphalt", "cobblestone", "cobblestone:flattened",
			"sett", "concrete", "concrete:lanes", "concrete:plates",
			"paving_stones", "metal", "wood",
			"tartan", "artificial_turf", "decoturf", "metal_grid",};

	@Inject public ShowInvalidSurface(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		String query = "nodes, ways with surface ";
		for (String surface: VALID_SURFACES) {
			query += " and surface!=" + surface;
		}
		//not using regexp match is deliberate to catch problems like surface=paved;unpaved
		return query;
	}

	@Override
	public int importance()
	{
		return QuestImportance.ERROR;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new showInvalidSurfaceForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
	}

	@Override public String getCommitMessage()
	{
		return null;
	}

	@Override public String getIconName()
	{
		return "quest";
	}
}
