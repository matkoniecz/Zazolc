package de.westnordost.streetcomplete.quests.validator;

import android.os.Bundle;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class multidesignatedFootwayToPath extends SimpleOverpassQuestType
{
	@Inject public multidesignatedFootwayToPath(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "ways with highway=footway and bicycle=designated";
	}

	@Override
	public int importance()
	{
		return QuestImportance.ERROR;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		AbstractQuestAnswerFragment form =  new YesNoQuestAnswerFragment();
		form.setTitle(R.string.quest_multidesignatedFootway_title);
		return form;
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)){
			changes.modify("highway", "path");
			changes.add("foot", "designated");
		} else {
			//TODO: handle nonexisting pedestrian + cyclist route marked on the map
		}
	}

	@Override public String getCommitMessage()
	{
		return "fix misused highway=footway, confirmation that route for both pedestrian and cyclists exists";
	}

	@Override public String getIconName() {	return "bicycle"; }
}
