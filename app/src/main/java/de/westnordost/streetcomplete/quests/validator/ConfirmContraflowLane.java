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

public class ConfirmContraflowLane extends SimpleOverpassQuestType {
	@Inject
	public ConfirmContraflowLane(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "ways with cycleway=opposite_lane and !oneway:bicycle and !fixme";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
	if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)){
		changes.add("oneway:bicycle", "no");
	} else {
		changes.add("fixme", "this road was reported to not have a contraflow with bicycle lane");
	}
	}


	@Override public String getCommitMessage() {
		return "handle cycleway=opposite_lane without oneway:bicycle tag";
	}
	@Override public int getIcon() { return R.drawable.ic_quest_bicycle; }
	@Override public int getTitle(Map<String,String> tags)
	{
		return R.string.quest_confirm_contraflow_lane;
	}
}

