package de.westnordost.streetcomplete.quests.validator;

import android.os.Bundle;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;


public class DetectHistoricRailwayTagging extends SimpleOverpassQuestType {
	@Inject
	public DetectHistoricRailwayTagging(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes, ways, relations with railway=abandoned";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{}


	@Override public String getCommitMessage() { return null; }
	@Override public int getIcon() { return R.drawable.ic_quest_railway; }
	@Override public int getTitle(Map<String,String> tags)
	{
		return R.string.quest_railway_abandoned_detected;
	}
}

