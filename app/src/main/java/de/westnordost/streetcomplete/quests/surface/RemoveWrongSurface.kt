package de.westnordost.streetcomplete.quests.surface
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder


class RemoveWrongSurface : OsmFilterQuestType<WrongSurfaceAnswer>() {
    override val elementFilter = """
        ways with
          tracktype = grade1
          and surface ~ sand|gravel|fine_gravel|compacted|grass|earth|dirt|mud|pebbles
          and !surface:note
          and !note:surface
    """

    override val commitMessage = "Remove wrong surface info"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_way_surface
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wrong_surface_title

    override fun createForm() = RemoveWrongSurfaceForm()

    override fun applyAnswerTo(answer: WrongSurfaceAnswer, changes: StringMapChangesBuilder) {
        when (answer) {
            is TracktypeIsWrong -> {
                changes.delete("tracktype")
            }
            is SpecificSurfaceIsWrong -> {
                changes.delete("surface")
            }
        }
    }
}
