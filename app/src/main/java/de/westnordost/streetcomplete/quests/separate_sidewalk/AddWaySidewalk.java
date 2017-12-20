package de.westnordost.streetcomplete.quests.separate_sidewalk;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddWaySidewalk extends SimpleOverpassQuestType
{
	private static final String[] WAYS_WITH_SIDEWALKS = { "primary", "primary_link", "secondary",
			"secondary_link", "tertiary", "tertiary_link", "unclassified", "residential" };

	@Inject public AddWaySidewalk(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "ways with " +
				" highway ~ " + TextUtils.join("|", WAYS_WITH_SIDEWALKS) +
				" and !sidewalk" +
				" and foot != no " +
				" and (access !~ private|no or (foot and foot !~ private|no))"; // not private roads
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		changes.add("sidewalk", answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "separate" : "no");
	}

	@Override public String getCommitMessage() { return "Add sidewalk tag"; }
	@Override public int getIcon() { return R.drawable.ic_quest_pedestrian; }
	@Override public int getTitle(Map<String,String> tags)
	{
		String type = tags.get("highway");
		boolean hasName = tags.containsKey("name");
		if (hasName) return R.string.quest_way_sidewalk_named_road_title;
		else         return R.string.quest_way_sidewalk_road_title;
	}
}
