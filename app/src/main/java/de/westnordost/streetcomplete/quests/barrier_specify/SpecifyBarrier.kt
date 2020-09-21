package de.westnordost.streetcomplete.quests.barrier_specify

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType

class SpecifyBarrier(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = "ways with barrier=yes"
    override val commitMessage = "Specify barrier type"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_specify_fence

    override fun createForm() = SpecifyBarrierForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.modify("barrier", answer)
    }
}
