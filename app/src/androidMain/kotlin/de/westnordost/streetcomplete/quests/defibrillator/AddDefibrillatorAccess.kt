package de.westnordost.streetcomplete.quests.defibrillator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.parking_access.AddParkingAccessForm

class AddDefibrillatorAccess : OsmFilterQuestType<DefibrillatorAccess>() {

    override val elementFilter = """
        nodes, ways with
         emergency = defibrillator
         and !access
    """
    override val changesetComment = "Specify aed access"
    override val wikiLink = "Key:access"
    override val icon = R.drawable.ic_quest_defibrillator
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parking_access_title2

    override fun createForm() = AddDefibrillatorAccessForm()

    override fun applyAnswerTo(answer: DefibrillatorAccess, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["access"] = answer.osmValue
    }
}
