package de.westnordost.streetcomplete.quests.surface
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement

class SurfaceMismatchesTracktypeWhichClaimsUnpaved : OsmFilterQuestType<WrongSurfaceAnswer>() {
    override val elementFilter = """
        ways with
        (
                (
                  tracktype = grade2
                  and surface ~ sand|grass|earth|dirt|mud
                )
                or
                (
                  tracktype ~ grade3|grade4|grade5
                  and surface ~ asphalt|concrete|paving_stones|paved
                )
            )
        )
        and !surface:note
        and !note:surface
    """

    override val changesetComment = "Remove wrong surface info"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_way_surface
    override val isSplitWayEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wrong_surface_title_unpaved_according_to_tracktype

    override fun createForm() = SurfaceMismatchesTracktypeWhichClaimsUnpavedForm()

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

    override val questTypeAchievements: List<QuestTypeAchievement>
        get() = listOf()
}
