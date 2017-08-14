package de.westnordost.streetcomplete.quests.show_fixme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.westnordost.osmapi.map.data.OsmElement;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.TextListQuestAnswerFragment;

public class ShowFixmeForm extends TextListQuestAnswerFragment {
    protected static final int ALL_OF_THEM = 1000;

    private static final OsmItem[] REFUSAL_TYPES = new OsmItem[]{
            new OsmItem("fixme:requires_aerial_image", R.string.quest_ShowFixme_requiresAerial_answer),
            new OsmItem("fixme:solved", R.string.quest_ShowFixme_solved_answer),
    };

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        OsmElement element = getOsmElement();
        String fixme = element != null && element.getTags() != null ? element.getTags().get("fixme") : null;
        if(fixme != null && !fixme.trim().isEmpty()) {
            setTitle(R.string.fixme_title, fixme);
        } else {
            setTitle(R.string.fixme_title_on_missing_fixme_text);
        }
        textSelector.setCellLayout(R.layout.text_select_cell);
        return view;
    }

    @Override protected int getMaxSelectableItems()
    {
        return 1;
    }

    @Override protected int getMaxNumberOfInitiallyShownItems()
    {
        return ALL_OF_THEM;
    }

    @Override protected TextListQuestAnswerFragment.OsmItem[] getItems()
    {
        return REFUSAL_TYPES;
    }
}
