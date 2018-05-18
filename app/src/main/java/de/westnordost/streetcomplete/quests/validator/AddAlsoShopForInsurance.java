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


public class AddAlsoShopForInsurance extends SimpleOverpassQuestType {
	@Inject
	public AddAlsoShopForInsurance(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		return "nodes, ways, relations with office=insurance and !shop and name";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new YesNoQuestAnswerFragment();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)){
			changes.add("shop", "insurance");
		}}


	@Override public String getCommitMessage() { return "better tagging for insurance shop"; }
	@Override public int getIcon() { return R.drawable.ic_quest_power; }
	@Override public int getTitle(Map<String,String> tags)
	{
		return R.string.quest_add_also_shop_variant_for_insurance_shop;
	}
}

