package de.westnordost.streetcomplete.quests.street_parking_permission

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.cycleway.createCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.isAvailableAsSelection
import de.westnordost.streetcomplete.osm.street_parking.FreeParking
import de.westnordost.streetcomplete.osm.street_parking.IncompleteStreetParking
import de.westnordost.streetcomplete.osm.street_parking.LeftAndRightStreetParkingPermission
import de.westnordost.streetcomplete.osm.street_parking.NoParking
import de.westnordost.streetcomplete.osm.street_parking.NoStreetParking
import de.westnordost.streetcomplete.osm.street_parking.PaidParking
import de.westnordost.streetcomplete.osm.street_parking.PrivateParking
import de.westnordost.streetcomplete.osm.street_parking.ResidentsOnlyParking
import de.westnordost.streetcomplete.osm.street_parking.StreetParking
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingPermission
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingPositionAndOrientation
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingProhibited
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingSeparate
import de.westnordost.streetcomplete.osm.street_parking.StreetStandingProhibited
import de.westnordost.streetcomplete.osm.street_parking.StreetStoppingProhibited
import de.westnordost.streetcomplete.osm.street_parking.TimeLimit
import de.westnordost.streetcomplete.osm.street_parking.UnknownStreetParking
import de.westnordost.streetcomplete.osm.street_parking.createStreetParkingSides
import de.westnordost.streetcomplete.quests.AStreetSideSelectFragment
import de.westnordost.streetcomplete.quests.StreetSideDisplayItem
import de.westnordost.streetcomplete.quests.StreetSideItem

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
        if(leftSide is NoParking != areTagsIndicatingNoParkingOnLeft(osmElement!!.tags)) {
            // dammit, handle this somehow! TODO
        }
        if(rightSide is NoParking != areTagsIndicatingNoParkingOnRight(osmElement!!.tags)) {
            // dammit, handle this somehow! TODO
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
            left = NoParking
        }
        if(areTagsIndicatingNoParkingOnRight(osmElement!!.tags)) {
            right = NoParking
        }
    }
}
