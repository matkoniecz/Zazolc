package de.westnordost.streetcomplete.quests.street_parking_permission

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.street_parking.LeftAndRightStreetParkingPermission
import de.westnordost.streetcomplete.osm.street_parking.toOsmConditionValue

class AddStreetParkingPermission : OsmFilterQuestType<LeftAndRightStreetParkingPermission>() {

    override val elementFilter = """
        ways with
          (
            (highway ~ ${ALL_ROADS.joinToString("|")} and area != yes)
          and
            (
                (
                    parking:lane:left ~ parallel|diagonal|perpendicular|marked
                    and
                    parking:lane:right ~ parallel|diagonal|perpendicular|marked
                    and
                    !parking:condition:left
                    and
                    !parking:condition:right
                    and
                    !parking:condition:both
                )
                or
                (
                    parking:lane:both ~ parallel|diagonal|perpendicular
                    and
                    !parking:condition:left
                    and
                    !parking:condition:right
                    and
                    !parking:condition:both
                )
                or
                (
                    parking:lane:left ~ parallel|diagonal|perpendicular|marked
                    and
                    !parking:condition:left
                    and
                    (
                        parking:condition:right ~ no|no_stopping|no_parking|no_standing
                        or
                        ACTUALLYNOSUPPORTparking:lane:right ~ no|no_stopping|no_parking|no_standing
                    )
                )
                or
                (
                    (
                        parking:condition:left ~ no|no_stopping|no_parking|no_standing
                        or
                        ACTUALLYNOSUPPORTparking:lane:left ~ no|no_stopping|no_parking|no_standing
                    )
                    and
                    parking:lane:right ~ parallel|diagonal|perpendicular|marked
                    and
                    !parking:condition:right
                )
            )
          and !parking:condition
          and (
            access !~ private|no
            or foot and foot !~ private|no
            )
          )
    """

    override val changesetComment = "Add which cars are allowed to park here"
    override val wikiLink = "Key:parking:condition"
    override val icon = R.drawable.ic_quest_parking_lane
    override val isSplitWayEnabled = true
    override val questTypeAchievements = listOf(CAR)
    override val defaultDisabledMessage = R.string.default_disabled_msg_difficult_and_time_consuming

    override fun getTitle(tags: Map<String, String>) = R.string.quest_street_parking_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("ways, relations with amenity = parking")

    override fun createForm() = AddStreetParkingPermissionForm()

    override fun applyAnswerTo(answer: LeftAndRightStreetParkingPermission, tags: Tags, timestampEdited: Long) {
        /*
           TODO: copied - is applicable?
           Note: If a resurvey is implemented, old
           parking:lane:*:(parallel|diagonal|perpendicular|...) values must be cleaned up */

        // parking:condition:<left/right/both>
        val conditionRight = answer.right!!.toOsmConditionValue() ?: throw IllegalArgumentException()
        val conditionLeft = answer.left!!.toOsmConditionValue() ?: throw IllegalArgumentException()

        if (conditionLeft == conditionRight) {
            tags["parking:condition:both"] = conditionRight
        } else {
            tags["parking:condition:left"] = conditionLeft
            tags["parking:condition:right"] = conditionRight
        }
    }
}
