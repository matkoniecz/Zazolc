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

public class RemoveFootYesFromFootways extends SimpleOverpassQuestType {
	@Inject
	public RemoveFootYesFromFootways(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "ways with highway=footway and foot=yes and !access";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
	if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)){
		changes.delete("foot");
	}}


	@Override public String getCommitMessage() {
		return "remove foot=yes from highway=footway without access tag. foot=yes is pointless or even a bit harmful, highway=footway clearly implies foot=designated";
	}
	@Override public int getIcon() { return R.drawable.ic_quest_power; }
	@Override public int getTitle(Map<String,String> tags)
	{
		return R.string.quest_remove_foot_yes_from_footway;
	}
}

