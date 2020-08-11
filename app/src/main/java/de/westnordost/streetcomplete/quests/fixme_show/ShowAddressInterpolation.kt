package de.westnordost.streetcomplete.quests.fixme_show

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment


class ShowAddressInterpolation(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters =
        "nodes, ways, relations with addr:interpolation"

    override val commitMessage = "unused commit message"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_show_address_interpolation

    override fun createForm() = YesNoQuestAnswerFragment();

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        TODO("Not yet implemented")
    }
}
