package de.westnordost.streetcomplete.quests.piste_difficulty

import android.content.Context
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.fullElementSelectionDialog
import de.westnordost.streetcomplete.quests.getPrefixedFullElementSelectionPref
import de.westnordost.streetcomplete.util.isWinter

class AddPisteDifficulty : OsmElementQuestType<PisteDifficulty> {

    val elementFilter = """
        ways, relations with
          piste:type ~ downhill|nordic
          and !piste:difficulty
    """
    private val filter by lazy { elementFilter.toElementFilterExpression() }

    override val changesetComment = "Add piste difficulty"
    override val wikiLink = "Key:piste:difficulty"
    override val icon = R.drawable.ic_quest_piste_difficulty
    override val defaultDisabledMessage: Int = R.string.default_disabled_msg_ee

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return if (isWinter(mapData.nodes.firstOrNull()?.position)) mapData.filter(filter).asIterable()
            else emptyList()
    }

    override fun isApplicableTo(element: Element) = if (filter.matches(element)) null else false

    override fun getTitle(tags: Map<String, String>) = R.string.quest_piste_difficulty_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry): Sequence<Element> {
        val mapData = getMapData()
        return mapData.filter("ways, relations with piste:type")
    }

    override fun createForm() = AddPisteDifficultyForm()

    override fun applyAnswerTo(answer: PisteDifficulty, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["piste:difficulty"] = answer.osmValue
    }

    override val hasQuestSettings: Boolean = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        fullElementSelectionDialog(context, prefs, this.getPrefixedFullElementSelectionPref(prefs), R.string.quest_settings_element_selection, elementFilter)
}
