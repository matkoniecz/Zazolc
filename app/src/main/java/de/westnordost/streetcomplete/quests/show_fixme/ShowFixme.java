package de.westnordost.streetcomplete.quests.show_fixme;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class ShowFixme extends SimpleOverpassQuestType
{
	@Inject public ShowFixme(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "nodes, ways, relations with fixme and fixme!=continue " +
                "and !fixme:requires_aerial_image " +
                "and !fixme:use_better_tagging_scheme ";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new ShowFixmeForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		ArrayList<String> values = answer.getStringArrayList(ShowFixmeForm.OSM_VALUES);
		if(values != null  && values.size() == 1)
		{
			if("fixme:solved".equals(values.get(0))){
				//TODO: handle it without magic values
				changes.delete("fixme");
			} else {
				changes.add(values.get(0), "yes");
			}
		}
	}

	@Override public String getCommitMessage()
	{
		return "processing fixme tags";
	}

	@Override
	public int getTitle(@NonNull Map<String, String> tags) {
		return R.string.empty;
	}

	@Override
	public String getTitleSuffixHack(@NonNull Map<String, String> tags) {
		return "fixme=" + tags.get("fixme");
	}

	@Override public int getIcon() { return R.drawable.ic_quest_notes; }
}