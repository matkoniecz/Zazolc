package de.westnordost.streetcomplete.util.dialogs

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.presets.EditTypePreset
import de.westnordost.streetcomplete.data.presets.EditTypePresetsController
import de.westnordost.streetcomplete.util.ktx.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun showProfileSelectionDialog(context: Context, editTypePresetsController: EditTypePresetsController, prefs: Preferences) {
    val presets = mutableListOf<EditTypePreset>()
    presets.add(EditTypePreset(0, context.getString(R.string.quest_presets_default_name)))
    presets.addAll(editTypePresetsController.getAll())
    var selected = -1
    (presets).forEachIndexed { index, questPreset ->
        if (questPreset.id == editTypePresetsController.selectedId)
            selected = index
    }
    var dialog: AlertDialog? = null
    val array = presets.map { it.name }.toTypedArray()
    val builder = AlertDialog.Builder(context)
        .setTitle(R.string.quest_presets_preset_name)
        .setSingleChoiceItems(array, selected) { _, i ->
            if (prefs.getBoolean(Prefs.QUEST_SETTINGS_PER_PRESET, false)) {
                OsmQuestController.reloadQuestTypes()
                if (!prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false))
                    context.toast(R.string.quest_settings_per_preset_rescan, Toast.LENGTH_LONG)
            }
            // launch in background, because this can block for quite a while if database is occupied
            GlobalScope.launch(Dispatchers.IO) { editTypePresetsController.selectedId = presets[i].id }
            dialog?.dismiss()
        }
        .setNegativeButton(android.R.string.cancel, null)
    dialog = builder.create()
    dialog.show()
}
