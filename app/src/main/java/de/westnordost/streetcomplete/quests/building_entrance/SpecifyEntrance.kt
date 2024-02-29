package de.westnordost.streetcomplete.quests.building_entrance

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN

class SpecifyEntrance : OsmFilterQuestType<EntranceAnswer>() {

    override val elementFilter = """
         nodes with
          entrance=yes and !barrier and noexit != yes and access != private and amenity != parking_entrance
    """

    override val changesetComment = "Add entrance info"
    override val wikiLink = "Key:entrance"
    override val icon = R.drawable.ic_quest_apple
    override val achievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_building_entrance_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with entrance and entrance != no")

    override fun createForm() = AddEntranceForm()

    override fun applyAnswerTo(answer: EntranceAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            DeadEnd -> tags["noexit"] = "yes"
            Private -> tags["access"] = "private"
            is EntranceExistsAnswer -> tags["entrance"] = answer.osmValue
        }
    }
}
