package de.westnordost.streetcomplete.quests.show_fixme;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment;
import de.westnordost.streetcomplete.view.Item;

public class ShowFixmeForm extends ImageListQuestAnswerFragment {
    protected static final int ALL_OF_THEM = 1000;

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		imageSelector.setCellLayout(R.layout.cell_icon_select_with_label_below);
	}

    @Override protected int getMaxSelectableItems()
    {
        return 1;
    }

    @Override protected int getMaxNumberOfInitiallyShownItems()
    {
        return ALL_OF_THEM;
    }

	@Override protected Item[] getItems() {
		return new Item[]{
			new Item("fixme:solved", R.drawable.ic_religion_christian, R.string.quest_ShowFixme_solved_answer),
			new Item("fixme:requires_aerial_image", R.drawable.ic_religion_christian, R.string.quest_ShowFixme_requiresAerial_answer),
			new Item("fixme:use_better_tagging_scheme", R.drawable.ic_religion_christian, R.string.quest_ShowFixme_pure_taggery_answer),
			new Item("fixme:3d_tagging", R.drawable.ic_religion_christian, R.string.quest_ShowFixme_3d_tagging_answer),
		};
	}}
