package de.westnordost.streetcomplete.quests.city_limit_sign

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.city_limit_sign.CityLimit.CITY_LIMIT_START
import de.westnordost.streetcomplete.quests.city_limit_sign.CityLimit.CITY_LIMIT_START_BUILT_UP_AREA_START
import de.westnordost.streetcomplete.quests.city_limit_sign.CityLimit.CITY_LIMIT_END
import de.westnordost.streetcomplete.quests.city_limit_sign.CityLimit.CITY_LIMIT_BOTH
import de.westnordost.streetcomplete.quests.city_limit_sign.CityLimit.BUILT_UP_AREA_START
import de.westnordost.streetcomplete.quests.city_limit_sign.CityLimit.BUILT_UP_AREA_END

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

    override fun applyAnswerTo(answer: CityLimit, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            CITY_LIMIT_START_BUILT_UP_AREA_START -> {
                tags["traffic_sign:code"] = answer.signCode
                tags["city_limit"] = "begin"
            }
            CITY_LIMIT_START -> {
                tags["traffic_sign:code"] = answer.signCode
                tags["city_limit"] = "begin"
            }
            CITY_LIMIT_END -> {
                tags["traffic_sign:code"] = answer.signCode
                tags["city_limit"] = "end"
            }
            CITY_LIMIT_BOTH -> {
                tags["traffic_sign:code"] = answer.signCode
                tags["city_limit"] = "both"
            }
            BUILT_UP_AREA_START, BUILT_UP_AREA_END -> tags["traffic_sign"] = answer.signCode
        }
    }
}
