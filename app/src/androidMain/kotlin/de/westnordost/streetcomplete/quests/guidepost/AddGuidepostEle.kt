package de.westnordost.streetcomplete.quests.guidepost

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags

class AddGuidepostEle : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes with
        (information = guidepost or guidepost) and guidepost != simple
        and !ele and !~"ele:.*"
        and hiking = yes
    """
    override val changesetComment = "Specify guidepost elevation"
    override val wikiLink = "Tag:information=guidepost"
    override val icon = R.drawable.ic_quest_guidepost_ele
    override val isDeleteElementEnabled = true
    override val achievements = listOf(OUTDOORS)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_guidepostEle_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with information = guidepost")

    override val highlightedElementsRadius: Double get() = 200.0
    override val defaultDisabledMessage: Int = R.string.quest_guidepost_disabled_msg

    override fun createForm() = AddGuidepostEleForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["ele"] = answer

    }
}
