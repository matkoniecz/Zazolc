package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao


class DetailSurface(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        ways, relations with surface ~ paved|unpaved
        and segregated != yes 
        and !cycleway:surface and !surface:cycleway 
        and !footway:surface and !surface:footway 
        and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val commitMessage = "Add more detailed surfaces"
    override val icon = R.drawable.ic_quest_street
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val isSquare = tags["area"] == "yes"
        return if (hasName) {
            if (isSquare)
                R.string.quest_streetSurface_square_name_title
            else
                R.string.quest_streetSurface_name_title
        } else {
            if (isSquare)
                R.string.quest_streetSurface_square_title
            else
                R.string.quest_streetSurface_title
        }
    }

    override fun createForm() = AddRoadSurfaceForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        if(answer == "paved") {
            return  // and crash
        }
        if(answer == "unpaved") {
            return  // and crash
        }
        changes.modify("surface", answer)
    }
}
