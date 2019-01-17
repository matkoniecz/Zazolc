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
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName;

public class ShowFixme extends SimpleOverpassQuestType
{
	@Inject public ShowFixme(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "nodes, ways, relations with fixme and fixme!=continue and highway!=proposed and railway!=proposed" +
                " and !fixme:requires_aerial_image " +
                " and !fixme:use_better_tagging_scheme " +
                " and !fixme:3d_tagging ";
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
		return R.string.fixme_title;
	}

	@Override
	public String getTitleSuffixHack(@NonNull Map<String, String> tags) {
		String returned = "";
		ArrayList<String> interestingTags = new ArrayList<>(AddPlaceName.OBJECTS_WITH_NAMES.keySet());
		if (!interestingTags.contains("shop")){
			interestingTags.add("shop");
		}
		if (!interestingTags.contains("tourism")){
			interestingTags.add("tourism");
		}
		if (!interestingTags.contains("ref")){
			interestingTags.add("ref");
		}
		if (!interestingTags.contains("location")){
			interestingTags.add("location");
		}
		if (!interestingTags.contains("addr:street")){
			interestingTags.add("addr:street");
		}
		if (!interestingTags.contains("addr:housenumber")){
			interestingTags.add("addr:housenumber");
		}
		if (!interestingTags.contains("addr:housename")){
			interestingTags.add("addr:housenumber");
		}
		if (!interestingTags.contains("level")){
			interestingTags.add("level");
		}
		if (!interestingTags.contains("highway")){
			interestingTags.add("highway");
		}
		if (!interestingTags.contains("name")){
			interestingTags.add("name");
		}
		interestingTags.add("fixme");
		for(String key: interestingTags){
			if(tags.get(key) != null){
				returned += key + "=" + tags.get(key) + " ";
			}
		}
		return returned;
	}

	@Override public int getIcon() { return R.drawable.ic_quest_notes; }
}
