package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.QuestGenericListBinding
import de.westnordost.streetcomplete.util.takeFavorites
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.GroupedImageSelectAdapter
import org.koin.android.ext.android.inject

/**
 * Abstract class for quests with a grouped list of images and one to select.
 *
 * Saving and restoring state is not implemented
 */
abstract class AGroupedImageListQuestForm<I, T> : AbstractOsmQuestForm<T>() {

    final override val contentLayoutResId = R.layout.quest_generic_list
    private val binding by contentViewBinding(QuestGenericListBinding::bind)

    private val prefs: Preferences by inject()

    override val defaultExpanded = false

    protected lateinit var imageSelector: GroupedImageSelectAdapter<I>

    /** all items to display. May not be accessed before onCreate */
    protected abstract val allItems: List<GroupableDisplayItem<I>>
    /** items to display that are shown on the top. May not be accessed before onCreate */
    protected abstract val topItems: List<GroupableDisplayItem<I>>

    private val selectedItem get() = imageSelector.selectedItem

    protected open val itemsPerRow = 3

    private lateinit var itemsByString: Map<String, GroupableDisplayItem<I>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector = GroupedImageSelectAdapter()
        itemsByString = allItems
            .mapNotNull { it.items }
            .flatten()
            .associateBy { it.value.toString() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = GridLayoutManager(activity, itemsPerRow)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                if (imageSelector.items[position].isGroup) layoutManager.spanCount else 1
        }
        binding.list.layoutManager = layoutManager
        binding.list.isNestedScrollingEnabled = false

        binding.selectHintLabel.setText(R.string.quest_select_hint_most_specific)

        imageSelector.listeners.add { checkIsFormComplete() }
        imageSelector.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                scrollTo(positionStart - 1)
            }
        })
        checkIsFormComplete()

        imageSelector.items = getInitialItems() + allItems

        binding.list.adapter = imageSelector
    }

    private fun scrollTo(index: Int) {
        val item = binding.list.layoutManager?.findViewByPosition(index) ?: return
        val itemPos = IntArray(2)
        item.getLocationInWindow(itemPos)
        val scrollViewPos = IntArray(2)
        scrollView.getLocationInWindow(scrollViewPos)

        scrollView.postDelayed(250) {
            scrollView.smoothScrollTo(0, itemPos[1] - scrollViewPos[1])
        }
    }

    private fun getInitialItems(): List<GroupableDisplayItem<I>> =
        prefs.getLastPicked(this::class.simpleName!!)
            .map { itemsByString[it] }
            .takeFavorites(n = 6, history = 50, first = 1, pad = topItems)

    override fun onClickOk() {
        val item = selectedItem!!
        val itemValue = item.value

        if (itemValue == null) {
            context?.let {
                AlertDialog.Builder(it)
                    .setMessage(R.string.quest_generic_item_invalid_value)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        } else {
            /*
            if (item.isGroup) {
                context?.let {
                    AlertDialog.Builder(it)
                        .setMessage(R.string.quest_generic_item_confirmation)
                        .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                        .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ ->
                            prefs.addLastPicked(this::class.simpleName!!, item.value.toString())
                            onClickOk(itemValue)
                        }
                        .show()
                }
            } else {
             */
                prefs.addLastPicked(this::class.simpleName!!, item.value.toString())
                onClickOk(itemValue)
            //}
        }
    }

    abstract fun onClickOk(value: I)

    override fun isFormComplete() = selectedItem?.value != null
}
