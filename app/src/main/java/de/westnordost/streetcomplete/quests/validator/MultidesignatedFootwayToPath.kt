package de.westnordost.streetcomplete.quests.validator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class MultidesignatedFootwayToPath() : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "ways with highway=footway and bicycle=designated and (foot=designated or !foot)"
    override val changesetComment = "fix misused highway=footway, confirmation that route for both pedestrian and cyclists exists"
    override val icon = R.drawable.ic_quest_bicycle

    override fun getTitle(tags: Map<String, String>) = R.string.quest_multidesignatedFootway_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        if (!answer) {
            //TODO: handle nonexisting pedestrian + cyclist route marked on the map
            return
        }
        tags["foot"] = "designated"
        tags["highway"] = "path"
    }

    override val wikiLink = "Tag:highway=path"

    override val questTypeAchievements: List<QuestTypeAchievement>
        get() = listOf()
}
