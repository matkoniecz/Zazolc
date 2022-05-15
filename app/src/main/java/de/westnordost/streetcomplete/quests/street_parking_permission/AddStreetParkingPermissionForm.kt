package de.westnordost.streetcomplete.quests.street_parking_permission

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.osm.street_parking.FreeParking
import de.westnordost.streetcomplete.osm.street_parking.LeftAndRightStreetParkingPermission
import de.westnordost.streetcomplete.osm.street_parking.AnswerInconsistentWithExistingTagging
import de.westnordost.streetcomplete.osm.street_parking.GenericNoStreetParking
import de.westnordost.streetcomplete.osm.street_parking.NoStreetParking
import de.westnordost.streetcomplete.osm.street_parking.PaidParking
import de.westnordost.streetcomplete.osm.street_parking.PrivateParking
import de.westnordost.streetcomplete.osm.street_parking.ResidentsOnlyParking
import de.westnordost.streetcomplete.osm.street_parking.StreetParking
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingPermission
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingProhibited
import de.westnordost.streetcomplete.osm.street_parking.StreetStandingProhibited
import de.westnordost.streetcomplete.osm.street_parking.StreetStoppingProhibited
import de.westnordost.streetcomplete.osm.street_parking.TimeLimit
import de.westnordost.streetcomplete.osm.street_parking.createStreetParkingSides
import de.westnordost.streetcomplete.quests.AStreetSideSelectFragment
import de.westnordost.streetcomplete.quests.StreetSideDisplayItem

class AddStreetParkingPermissionForm : AStreetSideSelectFragment<StreetParkingPermission, LeftAndRightStreetParkingPermission>() {
    override val items = listOf(FreeParking, PaidParking, TimeLimit, ResidentsOnlyParking, PrivateParking)

    override fun getDisplayItem(value: StreetParkingPermission): StreetSideDisplayItem<StreetParkingPermission> = value.asStreetSideItem()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //view.findViewById<TextView>(R.id.descriptionLabel)
        //    .setText(R.string.quest_dietType_explanation) // TODO!!!!
        if (savedInstanceState == null) {
            initStateFromTags()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onClickOk(leftSide: StreetParkingPermission?, rightSide: StreetParkingPermission?) {
        if(leftSide is GenericNoStreetParking != areTagsIndicatingNoParkingOnLeft(osmElement!!.tags)) {
            applyAnswer(AnswerInconsistentWithExistingTagging)
        }
        if(rightSide is GenericNoStreetParking != areTagsIndicatingNoParkingOnRight(osmElement!!.tags)) {
            applyAnswer(AnswerInconsistentWithExistingTagging)
        }
        applyAnswer(LeftAndRightStreetParkingPermission(leftSide, rightSide))
    }

    private fun StreetParking.isIndicatingNoParking(): Boolean {
        if(this is NoStreetParking
            || this is StreetParkingProhibited
            || this is StreetStandingProhibited
            || this is StreetStoppingProhibited) {
            return true
        }
        return false
    }
    private fun areTagsIndicatingNoParkingOnLeft(tags: Map<String, String>): Boolean {
        val parsed = createStreetParkingSides(tags)
        return if(parsed?.left == null) {
            false
        } else {
            parsed.left.isIndicatingNoParking()
        }
    }

    private fun areTagsIndicatingNoParkingOnRight(tags: Map<String, String>): Boolean {
        val parsed = createStreetParkingSides(tags)
        return if(parsed?.right == null) {
            false
        } else {
            parsed.right.isIndicatingNoParking()
        }
    }
    override fun initStateFromTags() {
        //TODO : test in left sided countries

        // not checking parking:condition:both as in such cases there is no parking to ask about
        // in case of enabling resurvey it would need to be checked
        if(areTagsIndicatingNoParkingOnLeft(osmElement!!.tags)) {
            left = GenericNoStreetParking
        }
        if(areTagsIndicatingNoParkingOnRight(osmElement!!.tags)) {
            right = GenericNoStreetParking
        }
    }
}
