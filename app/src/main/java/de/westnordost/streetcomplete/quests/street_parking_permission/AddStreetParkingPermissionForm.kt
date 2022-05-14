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
    private var leftWasNoParking: Boolean = false //TODO: keep it in memory!
    private var rightWasNoParking: Boolean = false //TODO: keep it in memory!

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
        Log.wtf("aaaaaa", "leftWasNoParking $leftWasNoParking")
        Log.wtf("aaaaaa", "rightWasNoParking $rightWasNoParking")
        if(leftSide is NoParking != leftWasNoParking) {
            // dammit, handle this somehow!
        }
        if(rightSide is NoParking != rightWasNoParking) {
            // dammit, handle this somehow!
        }
        applyAnswer(LeftAndRightStreetParkingPermission(leftSide, rightSide))
    }

    override fun initStateFromTags() {
        // not checking parking:condition:both as in such cases there is no parking to ask about
        // in case of enabling resurvey it would need to be checked
        val parsed = createStreetParkingSides(osmElement!!.tags)
        Log.wtf("aaaaaa", "parsed left " + parsed?.left)
        Log.wtf("aaaaaa", "parsed right " + parsed?.right)
        if(parsed?.left is NoStreetParking
            || parsed?.left is StreetParkingProhibited
            || parsed?.left is StreetStandingProhibited
            || parsed?.left is StreetStoppingProhibited) {
            leftWasNoParking = true
            left = NoParking
        }
        if(parsed?.right is NoStreetParking
            || parsed?.right is StreetParkingProhibited
            || parsed?.right is StreetStandingProhibited
            || parsed?.right is StreetStoppingProhibited) {
            right = NoParking
        }
    }
}
