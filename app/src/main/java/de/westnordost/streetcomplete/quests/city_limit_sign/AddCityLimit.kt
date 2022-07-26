package de.westnordost.streetcomplete.quests.city_limit_sign

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.city_limit.AddCityLimitForm

class AddCityLimit : OsmFilterQuestType<CityLimit>() {

    override val elementFilter = """
        nodes with traffic_sign=city_limit and !traffic_sign:code
    """
    override val changesetComment = "traffic_sign=city_limit gdzie brak traffic_sign=* - uzupełnienia czy poprawki"
    override val wikiLink = "Pl:Tag:traffic_sign=city_limit"
    override val icon = R.drawable.ic_quest_card
    override val achievements = listOf<EditTypeAchievement>()

    override fun getTitle(tags: Map<String, String>) = R.string.quest_traffic_sign

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) = getMapData().filter("nodes with traffic_sign=city_limit")

    override fun createForm() = AddCityLimitForm()

    override fun applyAnswerTo(answer: CityLimit, tags: Tags, timestampEdited: Long) {
        when(answer) {
            CityLimit.CITY_LIMIT_START -> {
                tags["traffic_sign:code"] = "PL:E-17a"
                tags["city_limit"] = "begin"
            }
            CityLimit.CITY_LIMIT_END -> {
                tags["traffic_sign:code"] = "PL:E-18a"
                tags["city_limit"] = "end"
            }
            CityLimit.BUILT_UP_AREA_START -> tags["traffic_sign"] = "PL:D-42"
            CityLimit.BUILT_UP_AREA_END -> tags["traffic_sign"] = "PL:D-43"
        }
    }
}
