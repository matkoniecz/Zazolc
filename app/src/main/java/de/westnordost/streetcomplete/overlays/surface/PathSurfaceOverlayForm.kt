package de.westnordost.streetcomplete.overlays.surface

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.databinding.FragmentOverlayPathSurfaceSelectBinding
import de.westnordost.streetcomplete.osm.surface.COMMON_SPECIFIC_PAVED_SURFACES
import de.westnordost.streetcomplete.osm.surface.COMMON_SPECIFIC_UNPAVED_SURFACES
import de.westnordost.streetcomplete.osm.surface.CyclewayFootwaySurfacesWithNote
import de.westnordost.streetcomplete.osm.surface.GENERIC_AREA_SURFACES
import de.westnordost.streetcomplete.osm.surface.GROUND_SURFACES
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.surface.asItem
import de.westnordost.streetcomplete.osm.surface.commonSurfaceObject
import de.westnordost.streetcomplete.osm.surface.createSurfaceStatus
import de.westnordost.streetcomplete.osm.surface.shouldBeDescribed
import de.westnordost.streetcomplete.osm.surface.toItems
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.AnswerItem
import de.westnordost.streetcomplete.overlays.IAnswerItem
import de.westnordost.streetcomplete.quests.surface.DescribeGenericSurfaceDialog
import de.westnordost.streetcomplete.util.getFeatureName
import de.westnordost.streetcomplete.util.getLocalesForFeatureDictionary
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class PathSurfaceOverlayForm : AbstractOverlayForm() {
    override val contentLayoutResId = R.layout.fragment_overlay_path_surface_select
    private val binding by contentViewBinding(FragmentOverlayPathSurfaceSelectBinding::bind)

    private val itemsPerRow = 2
    /** items to display. May not be accessed before onCreate */
    val items: List<DisplayItem<Surface>> = (COMMON_SPECIFIC_PAVED_SURFACES + COMMON_SPECIFIC_UNPAVED_SURFACES + GROUND_SURFACES + GENERIC_AREA_SURFACES).toItems()
    private val cellLayoutId: Int = R.layout.cell_labeled_icon_select
    private var originalSurfaceStatus: CyclewayFootwaySurfacesWithNote? = null
    private var isSegregatedLayout = false

    private var selectedStatusForMainSurface: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }
    private var selectedStatusForCyclewaySurface: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }
    private var selectedStatusForFootwaySurface: DisplayItem<Surface>? = null
        set(value) {
            field = value
            updateSelectedCell()
        }

        override val otherAnswers: List<IAnswerItem> get() {
            return if (isSegregatedLayout) {
                listOf()
                // removing info about separate cycleway is too complicated
                //
                // Typically it requires editing not only surface info but
                // also an access info as it happens in cases
                // where bicycle access is gone.
                // May require also removal of cycleway=separate,
                // bicycle=use_sidepath from the road.
                //
                // And in cases where there is a segregated cycleway with
                // the same surface as footway then StreetComplete will
                // anyway ask for cycleway:surface and footway:surface.
                //
                // Fortunately need for this change are really rare.
                // Notes can be left as usual.
            } else if (bothFootAndBicycleTraffic(element!!.tags)) {
                // Only where bicycle access is already present
                // because adding bicycle access typically requires
                // adding proper access tags, interconnections with roads
                // and often also other geometry changes.
                listOf(
                    AnswerItem(R.string.overlay_path_surface_segregated) {
                        // reset previous data
                        selectedStatusForMainSurface = null
                        binding.explanationInputMainSurface.text = null
                        switchToFootwayCyclewaySurfaceLayout()
                    }
                )
            } else {
                listOf()
            }
        }

        private fun bothFootAndBicycleTraffic(tags: Map<String, String>): Boolean {
            // in case where path is not clearly marked as carrying both foot and bicycle traffic
            // mapper can leave a note
            if (tags["highway"] == "footway") {
                return tags["bicycle"] == "yes" || tags["bicycle"] == "designated"
            }
            if (tags["highway"] == "cycleway") {
                return tags["foot"] == "yes" || tags["foot"] == "designated"
            }
            if (tags["highway"] == "path") {
                return (tags["bicycle"] == "yes" || tags["bicycle"] == "designated") &&
                    (tags["foot"] == "yes" || tags["foot"] == "designated")
            }
            return false
        }

        private fun switchToFootwayCyclewaySurfaceLayout() {
            binding.mainSurfaceContainer.isGone = true
            isSegregatedLayout = true
            binding.cyclewaySurfaceContainer.isGone = false
            binding.footwaySurfaceContainer.isGone = false
            val locales = getLocalesForFeatureDictionary(resources.configuration)
            binding.cyclewaySurfaceLabel.text = getFeatureName( Way(1, listOf(), mapOf("highway" to "cycleway")), featureDictionary, locales)
            binding.footwaySurfaceLabel.text = getFeatureName( Way(1, listOf(), mapOf("highway" to "footway")), featureDictionary, locales)
        }

        private sealed class SingleSurfaceItemInfo
        private data class SingleSurfaceItem(val surface: DisplayItem<Surface>) : SingleSurfaceItemInfo()
        private data class SingleSurfaceItemWithNote(val surface: DisplayItem<Surface>, val note: String) : SingleSurfaceItemInfo()

        private fun collectSurfaceData(callback: (SingleSurfaceItemInfo) -> Unit) {
            ImageListPickerDialog(requireContext(), items, cellLayoutId, itemsPerRow) { item ->
                val value = item.value
                if (value != null && value.shouldBeDescribed) {
                    DescribeGenericSurfaceDialog(requireContext()) { description ->
                        callback(SingleSurfaceItemWithNote(item, description))
                    }.show()
                } else {
                    callback(SingleSurfaceItem(item))
                }
            }.show()
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            binding.explanationInputMainSurface.doAfterTextChanged { checkIsFormComplete() }
            binding.explanationInputFootwaySurface.doAfterTextChanged { checkIsFormComplete() }
            binding.explanationInputCyclewaySurface.doAfterTextChanged { checkIsFormComplete() }

            binding.selectButtonMainSurface.root.setOnClickListener {
                collectSurfaceData { gathered: SingleSurfaceItemInfo ->
                    when (gathered) {
                        is SingleSurfaceItem -> {
                            selectedStatusForMainSurface = gathered.surface
                            binding.explanationInputMainSurface.text = null
                        }
                        is SingleSurfaceItemWithNote -> {
                            selectedStatusForMainSurface = gathered.surface
                            binding.explanationInputMainSurface.text = SpannableStringBuilder(gathered.note)
                        }
                    }
                    checkIsFormComplete()
                }
            }
            binding.selectButtonCyclewaySurface.root.setOnClickListener {
                collectSurfaceData { gathered: SingleSurfaceItemInfo ->
                    when (gathered) {
                        is SingleSurfaceItem -> {
                            selectedStatusForCyclewaySurface = gathered.surface
                            binding.explanationInputCyclewaySurface.text = null
                        }
                        is SingleSurfaceItemWithNote -> {
                            selectedStatusForCyclewaySurface = gathered.surface
                            binding.explanationInputCyclewaySurface.text = SpannableStringBuilder(gathered.note)
                        }
                    }
                    checkIsFormComplete()
                }
            }
            binding.selectButtonFootwaySurface.root.setOnClickListener {
                collectSurfaceData { gathered: SingleSurfaceItemInfo ->
                    when (gathered) {
                        is SingleSurfaceItem -> {
                            selectedStatusForFootwaySurface = gathered.surface
                            binding.explanationInputFootwaySurface.text = null
                        }
                        is SingleSurfaceItemWithNote -> {
                            selectedStatusForFootwaySurface = gathered.surface
                            binding.explanationInputFootwaySurface.text = SpannableStringBuilder(gathered.note)
                        }
                    }
                    checkIsFormComplete()
                }
            }

            if (savedInstanceState != null) onLoadInstanceState(savedInstanceState)

            LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectButtonMainSurface.selectedCellView, true)
            LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectButtonCyclewaySurface.selectedCellView, true)
            LayoutInflater.from(requireContext()).inflate(cellLayoutId, binding.selectButtonFootwaySurface.selectedCellView, true)
            binding.selectButtonMainSurface.root.children.first().background = null
            binding.selectButtonCyclewaySurface.root.children.first().background = null
            binding.selectButtonFootwaySurface.root.children.first().background = null

            val status = createSurfaceStatus(element!!.tags)
            originalSurfaceStatus = status
            val cyclewaySurface = status.cycleway
            val footwaySurface = status.footway
            val mainSurface = status.main
            if (mainSurface != null && !mainSurface.shouldBeDescribed) {
                selectedStatusForMainSurface = mainSurface.asItem()
            }
            if (cyclewaySurface != null && !cyclewaySurface.shouldBeDescribed) {
                selectedStatusForCyclewaySurface = cyclewaySurface.asItem()
            }
            if (footwaySurface != null && !footwaySurface.shouldBeDescribed) {
                selectedStatusForFootwaySurface = footwaySurface.asItem()
            }
            if (status.note != null) {
                binding.explanationInputMainSurface.text = SpannableStringBuilder(status.note)
                selectedStatusForMainSurface = mainSurface?.asItem() // even if paved/unpaved
            }
            if (status.cyclewayNote != null) {
                binding.explanationInputCyclewaySurface.text = SpannableStringBuilder(status.cyclewayNote)
                selectedStatusForCyclewaySurface = cyclewaySurface?.asItem() // even if paved/unpaved
            }
            if (status.footwayNote != null) {
                binding.explanationInputFootwaySurface.text = SpannableStringBuilder(status.footwayNote)
                selectedStatusForFootwaySurface = footwaySurface?.asItem() // even if paved/unpaved
            }
            if (element!!.tags["segregated"] == "yes" || cyclewaySurface != null || footwaySurface != null || status.cyclewayNote != null || status.footwayNote != null) {
                switchToFootwayCyclewaySurfaceLayout()
            }
            updateSelectedCell()
        }

        private fun updateSelectedCell() {
            val mainSurfaceItem = selectedStatusForMainSurface
            binding.selectButtonMainSurface.selectTextView.isGone = mainSurfaceItem != null
            binding.selectButtonMainSurface.selectedCellView.isGone = mainSurfaceItem == null
            if (mainSurfaceItem != null) {
                ItemViewHolder(binding.selectButtonMainSurface.selectedCellView).bind(mainSurfaceItem)
            }
            if (noteText() != null || mainSurfaceItem?.value?.shouldBeDescribed == true) {
                binding.explanationInputMainSurface.isGone = false
                binding.mainSurfaceContainer.isGone = false
            }

            val cyclewaySurfaceItem = selectedStatusForCyclewaySurface
            binding.selectButtonCyclewaySurface.selectTextView.isGone = cyclewaySurfaceItem != null
            binding.selectButtonCyclewaySurface.selectedCellView.isGone = cyclewaySurfaceItem == null
            if (cyclewaySurfaceItem != null) {
                ItemViewHolder(binding.selectButtonCyclewaySurface.selectedCellView).bind(cyclewaySurfaceItem)
            }
            if (cyclewayNoteText() != null || cyclewaySurfaceItem?.value?.shouldBeDescribed == true) {
                binding.explanationInputCyclewaySurface.isGone = false
            }

            val footwaySurfaceItem = selectedStatusForFootwaySurface
            binding.selectButtonFootwaySurface.selectTextView.isGone = footwaySurfaceItem != null
            binding.selectButtonFootwaySurface.selectedCellView.isGone = footwaySurfaceItem == null
            if (footwaySurfaceItem != null) {
                ItemViewHolder(binding.selectButtonFootwaySurface.selectedCellView).bind(footwaySurfaceItem)
            }
            if (footwayNoteText() != null || footwaySurfaceItem?.value?.shouldBeDescribed == true) {
                binding.explanationInputFootwaySurface.isGone = false
            }
        }

        /* ------------------------------------- instance state ------------------------------------- */

        private fun onLoadInstanceState(inState: Bundle) {
            val selectedMainSurfaceIndex = inState.getInt(SELECTED_MAIN_SURFACE_INDEX)
            val selectedMainSurfaceNoteText = inState.getString(SELECTED_MAIN_SURFACE_NOTE_TEXT)
            val selectedCyclewaySurfaceIndex = inState.getInt(SELECTED_CYCLEWAY_SURFACE_INDEX)
            val selectedCyclewaySurfaceNoteText = inState.getString(SELECTED_CYCLEWAY_SURFACE_NOTE_TEXT)
            val selectedFootwaySurfaceIndex = inState.getInt(SELECTED_FOOTWAY_SURFACE_INDEX)
            val selectedFootwaySurfaceNoteText = inState.getString(SELECTED_FOOTWAY_SURFACE_NOTE_TEXT)
            selectedStatusForMainSurface = if (selectedMainSurfaceIndex != -1) items[selectedMainSurfaceIndex] else null
            if (selectedMainSurfaceNoteText != null) {
                binding.explanationInputMainSurface.text = SpannableStringBuilder(selectedMainSurfaceNoteText)
            }
            selectedStatusForCyclewaySurface = if (selectedCyclewaySurfaceIndex != -1) items[selectedCyclewaySurfaceIndex] else null
            if (selectedCyclewaySurfaceNoteText != null) {
                binding.explanationInputCyclewaySurface.text = SpannableStringBuilder(selectedCyclewaySurfaceNoteText)
            }
            selectedStatusForFootwaySurface = if (selectedFootwaySurfaceIndex != -1) items[selectedFootwaySurfaceIndex] else null
            if (selectedFootwaySurfaceNoteText != null) {
                binding.explanationInputFootwaySurface.text = SpannableStringBuilder(selectedFootwaySurfaceNoteText)
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putInt(SELECTED_MAIN_SURFACE_INDEX, items.indexOf(selectedStatusForMainSurface))
            outState.putString(SELECTED_MAIN_SURFACE_NOTE_TEXT, noteText())
            outState.putInt(SELECTED_CYCLEWAY_SURFACE_INDEX, items.indexOf(selectedStatusForCyclewaySurface))
            outState.putString(SELECTED_CYCLEWAY_SURFACE_NOTE_TEXT, noteText())
            outState.putInt(SELECTED_FOOTWAY_SURFACE_INDEX, items.indexOf(selectedStatusForFootwaySurface))
            outState.putString(SELECTED_FOOTWAY_SURFACE_NOTE_TEXT, noteText())
        }

        /* -------------------------------------- apply answer -------------------------------------- */

        override fun isFormComplete(): Boolean {
            if (selectedStatusForMainSurface == null) {
                if (selectedStatusForCyclewaySurface == null || selectedStatusForFootwaySurface == null) {
                    return false
                }
            }
            return hasChanges()
        }

    fun noteText(): String? {
        return binding.explanationInputMainSurface.nonBlankTextOrNull
    }

    private fun cyclewayNoteText(): String? {
        return binding.explanationInputCyclewaySurface.nonBlankTextOrNull
    }

    private fun footwayNoteText(): String? {
        return binding.explanationInputFootwaySurface.nonBlankTextOrNull
    }

    override fun hasChanges(): Boolean {
        // originalSurfaceStatus was supposed to be set in onViewCreated - is it possible to trigger this before onViewCreated completes?
        if (selectedStatusForMainSurface?.value != originalSurfaceStatus!!.main) {
            return true
        }
        if (selectedStatusForCyclewaySurface?.value != originalSurfaceStatus!!.cycleway) {
            return true
        }
        if (selectedStatusForFootwaySurface?.value != originalSurfaceStatus!!.footway) {
            return true
        }
        if (noteText() != originalSurfaceStatus!!.note) {
            return true
        }
        if (cyclewayNoteText() != originalSurfaceStatus!!.cyclewayNote) {
            return true
        }
        if (footwayNoteText() != originalSurfaceStatus!!.footwayNote) {
            return true
        }
        return false
    }

    override fun onClickOk() {
        if (selectedStatusForCyclewaySurface != null && selectedStatusForFootwaySurface != null) {
            val cyclewaySurface = selectedStatusForCyclewaySurface!!.value!!
            val footwaySurface = selectedStatusForFootwaySurface!!.value!!
            applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element!!.tags).also {
                editTagsWithSeparateCyclewayAndFootwayAnswer(it, cyclewaySurface, cyclewayNoteText(), footwaySurface, footwayNoteText(), noteText())
            }.create()))
        } else {
            // like RoadSurfaceOverlayForm is doing this
            val surfaceObject = selectedStatusForMainSurface!!.value!!
            applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element!!.tags).also {
                editTagsWithMainSurfaceAnswer(it, surfaceObject, noteText())
            }.create()))
        }
    }

    companion object {
        fun editTagsWithMainSurfaceAnswer(changesBuilder: StringMapChangesBuilder, surfaceObject: Surface, note: String?) {
            SurfaceAndNote(surfaceObject, note).applyTo(changesBuilder)
        }

        fun editTagsWithSeparateCyclewayAndFootwayAnswer(changesBuilder: StringMapChangesBuilder, cyclewaySurface: Surface, cyclewayNote: String?, footwaySurface: Surface, footwayNote: String?, generalSurfaceNote: String?) {
            val mainSurface = commonSurfaceObject(cyclewaySurface.osmValue, footwaySurface.osmValue)
            if (mainSurface == null) {
                if (changesBuilder.containsKey("surface")) {
                    changesBuilder.remove("surface")
                }
                if (changesBuilder.containsKey("surface:note") && generalSurfaceNote == null) {
                    changesBuilder.remove("surface:note")
                }
                if (generalSurfaceNote != null && changesBuilder["surface:note"] != generalSurfaceNote) {
                    changesBuilder["surface:note"] = generalSurfaceNote
                }
                SurfaceAndNote(footwaySurface, footwayNote).applyTo(changesBuilder, prefix = "footway")
                SurfaceAndNote(cyclewaySurface, cyclewayNote).applyTo(changesBuilder, prefix = "cycleway")
            } else {
                SurfaceAndNote(mainSurface, generalSurfaceNote).applyTo(changesBuilder)
                SurfaceAndNote(footwaySurface, footwayNote).applyTo(changesBuilder, prefix = "footway")
                SurfaceAndNote(cyclewaySurface, cyclewayNote).applyTo(changesBuilder, prefix = "cycleway")
            }
            changesBuilder["segregated"] = "yes"
        }

        private const val SELECTED_MAIN_SURFACE_INDEX = "selected_main_surface_index"
        private const val SELECTED_MAIN_SURFACE_NOTE_TEXT = "selected_main_surface_note_text"
        private const val SELECTED_CYCLEWAY_SURFACE_INDEX = "selected_cycleway_surface_index"
        private const val SELECTED_CYCLEWAY_SURFACE_NOTE_TEXT = "selected_cycleway_surface_index_note_text"
        private const val SELECTED_FOOTWAY_SURFACE_INDEX = "selected_footway_surface_index"
        private const val SELECTED_FOOTWAY_SURFACE_NOTE_TEXT = "selected_footway_surface_index_note_text"
    }
}