package de.westnordost.streetcomplete.quests;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.view.TextSelectAdapter;

/**
 * Abstract class for quests with a list of text entries and one to select.
 */

public abstract class TextListQuestAnswerFragment extends AbstractQuestFormAnswerFragment {

	public static final String OSM_VALUES = "osm_values";

    private static final String
			SELECTED_INDICES = "selected_indices",
			EXPANDED = "expanded";

	protected TextSelectAdapter textSelector;
    private Button showMoreButton;

	private int maxInitiallyShownItems;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View contentView = setContentView(R.layout.quest_generic_list);

		RecyclerView valueList = (RecyclerView) contentView.findViewById(R.id.listSelect);
        GridLayoutManager lm = new GridLayoutManager(getActivity(), 1);
        valueList.setLayoutManager(lm);
		valueList.setNestedScrollingEnabled(false);

		showMoreButton = (Button) view.findViewById(R.id.buttonShowMore);
		showMoreButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				List<TextSelectAdapter.Item> all = Arrays.<TextSelectAdapter.Item>asList(getItems());
				textSelector.addItems(all.subList(textSelector.getItemCount(), all.size()));
				showMoreButton.setVisibility(View.GONE);
			}
		});

		int selectableItems = getMaxSelectableItems();
		TextView selectHint = (TextView) view.findViewById(R.id.selectHint);
		selectHint.setText(selectableItems == 1 ? R.string.quest_roofShape_select_one : R.string.quest_select_hint);

		textSelector = new TextSelectAdapter(selectableItems);
		int initiallyShow = getMaxNumberOfInitiallyShownItems();
		if(savedInstanceState != null)
		{
			if(savedInstanceState.getBoolean(EXPANDED)) initiallyShow = -1;
			showInitialItems(initiallyShow);

			List<Integer> selectedIndices = savedInstanceState.getIntegerArrayList(SELECTED_INDICES);
			textSelector.selectIndices(selectedIndices);
		}
		else
		{
			showInitialItems(initiallyShow);
		}
		valueList.setAdapter(textSelector);

        return view;
    }


    /** return -1 for any number*/
    protected abstract int getMaxSelectableItems();
	/** return -1 for showing all items at once */
	protected abstract int getMaxNumberOfInitiallyShownItems();
	protected abstract OsmItem[] getItems();

	private void showInitialItems(int initiallyShow)
	{
		List<TextSelectAdapter.Item> all = Arrays.<TextSelectAdapter.Item>asList(getItems());
		if(initiallyShow == -1 || initiallyShow >= all.size())
		{
			textSelector.setItems(all);
			showMoreButton.setVisibility(View.GONE);
		}
		else
		{
			textSelector.setItems(all.subList(0, initiallyShow));
		}
	}

	@Override protected void onClickOk()
	{
		applyAnswer();
	}

	protected void applyAnswer()
	{
		Bundle answer = new Bundle();

		ArrayList<String> osmValues = new ArrayList<>();
		for(Integer selectedIndex : textSelector.getSelectedIndices())
		{
			osmValues.add(getItems()[selectedIndex].osmValue);
		}
		if(!osmValues.isEmpty())
		{
			answer.putStringArrayList(OSM_VALUES, osmValues);
		}
		applyAnswer(answer);
	}

    @Override public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
		outState.putIntegerArrayList(SELECTED_INDICES, textSelector.getSelectedIndices());
		outState.putBoolean(EXPANDED, showMoreButton.getVisibility() == View.GONE);
    }

    public boolean isRejectingClose()
    {
        return isFormComplete();
    }

	public boolean isFormComplete()
	{
		return !textSelector.getSelectedIndices().isEmpty();
	}

    protected static class OsmItem extends TextSelectAdapter.Item
    {
        public final String osmValue;

        public OsmItem(String osmValue, int textId)
        {
            super(textId);
            this.osmValue = osmValue;
        }
    }
}
