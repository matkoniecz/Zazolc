package de.westnordost.streetcomplete.quests.bikeway;

import android.os.Bundle;

import java.util.Map;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddCyclewayBoolean extends AddCycleway {
    public AddCyclewayBoolean(OverpassMapDataDao overpassServer) {
        super(overpassServer);
    }

    public AbstractQuestAnswerFragment createForm()
    {
        return new YesNoQuestAnswerFragment();
    }

    public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
    {
        if(answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)){
            changes.add("cycleway", "opposite");
            changes.add("oneway:bicycle", "no");

        } else {
            changes.add("cycleway", "no");
        }
    }

    @Override public int getTitle(Map<String,String> tags)
    {
        return R.string.quest_cycleway_boolean_title;
    }

    @Override public int getDefaultDisabledMessage() { return 0; }
}
