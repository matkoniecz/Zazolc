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

public class AccessDestinationToPrivate extends SimpleOverpassQuestType {
	@Inject
	public AccessDestinationToPrivate(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes, ways, relations with access=destination";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
	if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)){
		changes.modify("access", "private");
	}}


	@Override public String getCommitMessage() {
		return "change wrong access=destination to correct access=private - *=destination is for 'local traffic only', *=private is for 'residents only'";
	}
	@Override public int getIcon() { return R.drawable.ic_quest_power; }
	@Override public int getTitle(Map<String,String> tags)
	{
		return R.string.quest_convert_access_destination_to_access_private;
	}
}

