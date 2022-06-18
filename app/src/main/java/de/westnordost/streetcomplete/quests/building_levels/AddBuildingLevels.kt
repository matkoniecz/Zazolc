package de.westnordost.streetcomplete.quests.building_levels

import android.content.Context
import android.content.SharedPreferences
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.quests.singleTypeElementSelectionDialog
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BUILDING
import de.westnordost.streetcomplete.quests.questPrefix

class AddBuildingLevels(private val prefs: SharedPreferences) : OsmFilterQuestType<BuildingLevelsAnswer>() {

    override val elementFilter = """
        ways, relations with
         building ~ ${prefs.getString(questPrefix(prefs) + PREF_BUILDING_LEVELS_SELECTION, BUILDINGS_WITH_LEVELS)}
         and !building:levels
         and !man_made
         and location != underground
         and ruins != yes
    """
    override val changesetComment = "Add building and roof levels"
    override val wikiLink = "Key:building:levels"
    override val icon = R.drawable.ic_quest_building_levels
    override val questTypeAchievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("building:part"))
            R.string.quest_buildingLevels_title_buildingPart2
        else
            R.string.quest_buildingLevels_title2

    override fun createForm() = AddBuildingLevelsForm()

    override fun applyAnswerTo(answer: BuildingLevelsAnswer, tags: Tags, timestampEdited: Long) {
        tags["building:levels"] = answer.levels.toString()
        answer.roofLevels?.let { tags["roof:levels"] = it.toString() }
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context) =
        singleTypeElementSelectionDialog(context, prefs, questPrefix(prefs) + PREF_BUILDING_LEVELS_SELECTION, BUILDINGS_WITH_LEVELS, R.string.quest_settings_building_levels_message)
}

private val BUILDINGS_WITH_LEVELS = arrayOf(
    "house", "residential", "apartments", "detached", "terrace", "dormitory", "semi",
    "semidetached_house", "bungalow", "school", "civic", "college", "university", "public",
    "hospital", "kindergarten", "transportation", "train_station", "hotel", "retail",
    "commercial", "office", "manufacture", "parking", "farm", "farm_auxiliary",
    "cabin"
).joinToString("|")

private const val PREF_BUILDING_LEVELS_SELECTION = "qs_AddBuildingLevels_element_selection"