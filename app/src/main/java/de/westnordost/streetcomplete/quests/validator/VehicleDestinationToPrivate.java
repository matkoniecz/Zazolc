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

public class VehicleDestinationToPrivate extends SimpleOverpassQuestType {
	@Inject
	public VehicleDestinationToPrivate(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes, ways, relations with vehicle=destination";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
	if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)){
		changes.modify("vehicle", "private");
	}}


	@Override public String getCommitMessage() {
		return "change wrong vehicle=destination to correct vehicle=private - *=destination is for 'local traffic only', *=private is for 'residents only'";
	}
	@Override public int getIcon() { return R.drawable.ic_quest_power; }
	@Override public int getTitle(Map<String,String> tags)
	{
		return R.string.quest_convert_vehicle_destination_to_vehicle_private;
	}
}

