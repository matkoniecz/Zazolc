package de.westnordost.streetcomplete.quests.street_parking_permission

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryAdd
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryDelete
import de.westnordost.streetcomplete.osm.street_parking.FreeParking
import de.westnordost.streetcomplete.osm.street_parking.LeftAndRightStreetParkingPermission
import de.westnordost.streetcomplete.osm.street_parking.GenericNoStreetParking
import de.westnordost.streetcomplete.osm.street_parking.PaidParking
import de.westnordost.streetcomplete.osm.street_parking.TimeLimit
import de.westnordost.streetcomplete.quests.verifyAnswer
import org.junit.Test

class AddStreetParkingPermissionTest {

    private val questType = AddStreetParkingPermission()

    @Test fun `marking parking as free`() {
        questType.verifyAnswer(
            mapOf("parking:lane:both" to "parallel"),
            LeftAndRightStreetParkingPermission(FreeParking, FreeParking),
            StringMapEntryAdd("parking:condition:both", "free"),
        )
    }

    @Test fun `marking one side of parking as free, while other remains marked as no stopping`() {
        questType.verifyAnswer(
            mapOf(
                "parking:condition:left" to "no",
                "parking:lane:right" to "parallel"
            ),
            LeftAndRightStreetParkingPermission(GenericNoStreetParking, FreeParking),
            StringMapEntryAdd("parking:condition:right", "free"),
        )
    }

    @Test fun `marking one side of parking as free, while other remains marked as no stopping in old style tagging`() {
        questType.verifyAnswer(
            mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:right" to "no"
            ),
            LeftAndRightStreetParkingPermission(PaidParking, GenericNoStreetParking),
            StringMapEntryAdd("parking:condition:left", "ticket"),
        )
    }

    @Test fun `marking one side of parking as for residents, while other remain marked as no standing`() {
        questType.verifyAnswer(
            mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:right" to "no_standing"
            ),
            LeftAndRightStreetParkingPermission(FreeParking, GenericNoStreetParking),
            StringMapEntryAdd("parking:condition:left", "residents"),
        )
    }

    @Test fun `answer inconsistent with parking layout blanks tags, when no parking claimed where parking allowed`() {
        questType.verifyAnswer(
            mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:right" to "no_standing"
            ),
            LeftAndRightStreetParkingPermission(GenericNoStreetParking, GenericNoStreetParking),
            StringMapEntryDelete("parking:condition:left", "parallel"),
            StringMapEntryDelete("parking:condition:right", "no_standing"),
        )
    }

    @Test fun `answer inconsistent with parking layout blanks tags, when parking claimed where no parking is mapped`() {
        questType.verifyAnswer(
            mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:right" to "no_standing"
            ),
            LeftAndRightStreetParkingPermission(TimeLimit, TimeLimit),
            StringMapEntryDelete("parking:condition:left", "parallel"),
            StringMapEntryDelete("parking:condition:right", "no_standing"),
        )
    }

    @Test fun `answer inconsistent with parking layout blanks tags, also when allowing to tag parking condition`() {
        questType.verifyAnswer(
            mapOf(
                "parking:lane:left" to "parallel",
                "parking:lane:right" to "no_standing"
            ),
            LeftAndRightStreetParkingPermission(FreeParking, FreeParking),
            StringMapEntryDelete("parking:condition:left", "parallel"),
            StringMapEntryDelete("parking:condition:right", "no_standing"),
        )
    }
}
