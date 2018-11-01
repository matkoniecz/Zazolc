package de.westnordost.streetcomplete.quests.validator;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class multidesignatedFootwayToPath extends SimpleOverpassQuestType
{
	@Inject public multidesignatedFootwayToPath(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "ways with highway=footway and bicycle=designated and (foot=designated or !foot)";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		AbstractQuestAnswerFragment form = new multidesignatedFootwayToPathForm();
		return form;
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(!answer.getBoolean(multidesignatedFootwayToPathForm.ANSWER))
		{
			//TODO: handle nonexisting pedestrian + cyclist route marked on the map
			return;
		}
		changes.addOrModify("foot", "designated");
		changes.modify("highway", "path");
	}

	@Override public String getCommitMessage()
	{
		return "fix misused highway=footway, confirmation that route for both pedestrian and cyclists exists";
	}

	@Override
	public int getTitle(@NonNull Map<String, String> tags) {
		return R.string.quest_multidesignatedFootway_title;
	}

	@Override public int getIcon() { return R.drawable.ic_quest_bicycle; }}
