package de.westnordost.streetcomplete.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.westnordost.streetcomplete.R;

public class TextSelectAdapter extends RecyclerView.Adapter<TextSelectAdapter.ViewHolder>
{
	private ArrayList<TextSelectAdapter.Item> items;
	private Set<Integer> selectedIndices;
	private int maxSelectableIndices;
	private int cellLayoutId = R.layout.text_select_cell;

	public interface OnItemSelectionListener
	{
		void onIndexSelected(int index);
		void onIndexDeselected(int index);
	}
	private TextSelectAdapter.OnItemSelectionListener onItemSelectionListener;

	public TextSelectAdapter()
	{
		selectedIndices = new HashSet<>();
		this.maxSelectableIndices = -1;
	}

	public TextSelectAdapter(int maxSelectableIndices)
	{
		selectedIndices = new HashSet<>();
		this.maxSelectableIndices = maxSelectableIndices;
	}

	public void setOnItemSelectionListener(
			TextSelectAdapter.OnItemSelectionListener onItemSelectionListener)
	{
		this.onItemSelectionListener = onItemSelectionListener;
	}

	public void setCellLayout(int cellLayoutId)
	{
		this.cellLayoutId = cellLayoutId;
	}

	public ArrayList<Integer> getSelectedIndices()
	{
		return new ArrayList<>(selectedIndices);
	}

	public void selectIndices(List<Integer> indices)
	{
		for(Integer index : indices)
		{
			selectIndex(index);
		}
	}

	public void setItems(List<TextSelectAdapter.Item> items)
	{
		this.items = new ArrayList<>(items);
		notifyDataSetChanged();
	}

	public void addItems(Collection<TextSelectAdapter.Item> items)
	{
		int len = this.items.size();
		this.items.addAll(items);
		notifyItemRangeInserted(len, items.size());
	}

	@Override public TextSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).
				inflate(cellLayoutId, parent, false);
		return new TextSelectAdapter.ViewHolder(view);
	}

	public boolean isIndexSelected(int index)
	{
		return selectedIndices.contains(index);
	}

	public void selectIndex(int index)
	{
		checkIndexRange(index);
		// special case: toggle-behavior if only one index can be selected
		if(maxSelectableIndices == 1 && selectedIndices.size() == 1)
		{
			deselectIndex(selectedIndices.iterator().next());
		}
		else if(maxSelectableIndices > -1 && maxSelectableIndices <= selectedIndices.size())
		{
			return;
		}

		selectedIndices.add(index);

		notifyItemChanged(index);
		if(onItemSelectionListener != null)
		{
			onItemSelectionListener.onIndexSelected(index);
		}
	}

	public void deselectIndex(int index)
	{
		checkIndexRange(index);
		selectedIndices.remove(index);

		notifyItemChanged(index);
		if(onItemSelectionListener != null)
		{
			onItemSelectionListener.onIndexDeselected(index);
		}
	}

	public void toggleIndex(int index)
	{
		checkIndexRange(index);
		if(!isIndexSelected(index))
		{
			selectIndex(index);
		}
		else
		{
			deselectIndex(index);
		}
	}

	private void checkIndexRange(int index)
	{
		if(index < 0 || index >= items.size())
			throw new ArrayIndexOutOfBoundsException(index);
	}

	@Override public void onBindViewHolder(TextSelectAdapter.ViewHolder holder, int position)
	{
		TextSelectAdapter.Item item = items.get(position);
		holder.itemView.setSelected(isIndexSelected(position));
		holder.textView.setText(item.textId);
	}

	@Override public int getItemCount()
	{
		if(items == null) return 0;
		return items.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		TextView textView;

		public ViewHolder(View v)
		{
			super(v);
			textView = (TextView) itemView.findViewById(R.id.textView);
			textView.setOnClickListener(this);
		}

		@Override public void onClick(View v)
		{
			toggleIndex(getAdapterPosition());
		}
	}

	public static class Item
	{
		public final int textId;

		public Item(int textId)
		{
			this.textId = textId;
		}
	}
}
