package de.westnordost.streetcomplete.data.externalsource

import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.QuestHiddenKey
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.ExternalSourceQuestKey
import de.westnordost.streetcomplete.data.quest.Quest

data class ExternalSourceQuest(
    /** Each quest must be uniquely identified by the [id] and [source] */
    val id: String,
    override val geometry: ElementGeometry,
    override val type: ExternalSourceQuestType,
    override val position: LatLon = geometry.center // allow setting position to arbitrary LatLon, because e.g. Osmose issues are often located at outline of building instead of in center
) : Quest() {
    override val key = ExternalSourceQuestKey(id, source)
    override val markerLocations: Collection<LatLon> get() = listOf(position)
    val source get() = type.source

    /** an element can be linked to the quest, but this is not necessary */
    var elementKey: ElementKey? = null
}

data class ExternalSourceQuestHidden(
    val id: String,
    val questType: ExternalSourceQuestType,
    override val position: LatLon,
    override val createdTimestamp: Long
) : Edit {
    val questKey get() = ExternalSourceQuestKey(id, questType.source)
    override val key: QuestHiddenKey get() = QuestHiddenKey(questKey)
    override val isUndoable: Boolean get() = true
    override val isSynced: Boolean? get() = null
}
