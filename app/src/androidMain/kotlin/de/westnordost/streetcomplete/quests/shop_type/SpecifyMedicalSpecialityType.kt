package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.removeCheckDates

class SpecifyMedicalSpecialistType : OsmFilterQuestType<ShopTypeAnswer>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with (
         amenity = doctors and !healthcare:speciality
         and !man_made
         and !historic
         and !military
         and !power
         and !tourism
         and !attraction
         and !leisure
         and !aeroway
         and !railway
         and !craft
         and (!healthcare or healthcare = doctor)
         and !office
         and !shop
        )
    """ // add test to protect against future me adding !amenity here, see similar test for shop=yes quest, the same for healthcare = doctor

    override val changesetComment = "Survey specialities of medical practitioners"
    override val wikiLink = "Key:healthcare:speciality"
    override val icon = R.drawable.ic_quest_crown // TODO
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_medical_speciality_type_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlaceOrDisusedPlace() }

    override fun createForm() = MedicalSpecialityTypeForm()

    override fun applyAnswerTo(answer: ShopTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.removeCheckDates()
        when (answer) {
            is IsShopVacant -> {
                tags.remove("amenity")
                if (tags.containsKey("healthcare")) {
                    tags.remove("healthcare")
                }
                tags["disused:shop"] = "yes"
            }
            is ShopType -> {
                if (!answer.feature.tags.containsKey("amenity")) {
                    tags.remove("amenity")
                }
                if (!answer.feature.tags.containsKey("healthcare")) {
                    if (tags.containsKey("healthcare")) {
                        tags.remove("healthcare")
                    }
                }
                for ((key, value) in answer.feature.tags) {
                    tags[key] = value
                }
            }
        }
    }
}
