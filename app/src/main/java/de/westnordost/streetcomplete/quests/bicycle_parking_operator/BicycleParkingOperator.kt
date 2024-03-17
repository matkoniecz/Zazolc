package de.westnordost.streetcomplete.quests.bicycle_parking_operator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags

class BicycleParkingOperator : OsmFilterQuestType<String>() {
    override val elementFilter = """
        nodes with amenity = bicycle_parking
         and !operator
         and operator:signed != no
    """

    override val changesetComment = "Specify bicycle parking operators"
    override val wikiLink = "Tag:amenity=recycling"
    override val icon = R.drawable.ic_quest_bicycle
    override val isDeleteElementEnabled = true
    override val achievements = emptyList<EditTypeAchievement>()

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bicycle_parking_operator_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = recycling")

    override fun createForm() = AddBicycleParkingOperatorForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["operator"] = answer
    }
}
