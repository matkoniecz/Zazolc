package de.westnordost.streetcomplete.quests.surface
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
class SurfaceMismatchesTracktypeWhichClaimsPaved : OsmFilterQuestType<WrongSurfaceAnswer>() {
    override val elementFilter = """
        ways with
          tracktype = grade1
          and surface ~ sand|gravel|fine_gravel|compacted|grass|earth|dirt|mud|pebbles
          and !surface:note
          and !note:surface
    """

    override val changesetComment = "Remove wrong surface info"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_way_surface

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wrong_surface_title_paved_according_to_tracktype

    override fun createForm() = SurfaceMismatchesTracktypeWhichClaimsPavedForm()

    override fun applyAnswerTo(answer: WrongSurfaceAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is TracktypeIsWrong -> {
                tags.remove("tracktype")
            }
            is SpecificSurfaceIsWrong -> {
                tags.remove("surface")
            }
        }
    }

    override val achievements: List<EditTypeAchievement> = listOf()
}
