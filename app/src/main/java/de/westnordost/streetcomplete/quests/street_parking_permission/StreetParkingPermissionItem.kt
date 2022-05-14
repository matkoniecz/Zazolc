package de.westnordost.streetcomplete.quests.street_parking_permission

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.street_parking.FreeParking
import de.westnordost.streetcomplete.osm.street_parking.IncompleteStreetParking
import de.westnordost.streetcomplete.osm.street_parking.NoParking
import de.westnordost.streetcomplete.osm.street_parking.TimeLimit
import de.westnordost.streetcomplete.osm.street_parking.NoStreetParking
import de.westnordost.streetcomplete.osm.street_parking.ResidentsOnlyParking
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.HALF_ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.PAINTED_AREA_ONLY
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STREET_SIDE
import de.westnordost.streetcomplete.osm.street_parking.PaidParking
import de.westnordost.streetcomplete.osm.street_parking.PrivateParking
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingPermission
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingPermissionSeparate
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingPositionAndOrientation
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingProhibited
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingSeparate
import de.westnordost.streetcomplete.osm.street_parking.StreetStandingProhibited
import de.westnordost.streetcomplete.osm.street_parking.StreetStoppingProhibited
import de.westnordost.streetcomplete.osm.street_parking.UnknownStreetParking
import de.westnordost.streetcomplete.osm.street_parking.UnknownStreetParkingPermission
import de.westnordost.streetcomplete.quests.StreetSideItem
import de.westnordost.streetcomplete.util.ktx.noParkingLineStyleResId
import de.westnordost.streetcomplete.util.ktx.noParkingSignDrawableResId
import de.westnordost.streetcomplete.util.ktx.noStandingLineStyleResId
import de.westnordost.streetcomplete.util.ktx.noStandingSignDrawableResId
import de.westnordost.streetcomplete.util.ktx.noStoppingLineStyleResId
import de.westnordost.streetcomplete.util.ktx.noStoppingSignDrawableResId
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.image_select.Item2

/** Functions to display a (parsed) street parking in the UI */

/* context: Context, countryInfo: CountryInfo, isUpsideDown: Boolean
* were parameters - ignored for now
* TODO: check left-handed countries
*  */
fun StreetParkingPermission.asItem() =
    Item2(this, getDialogIcon(), titleResId?.let { ResText(it) })

fun StreetParkingPermission.asStreetSideItem() =
    StreetSideItem(
        this,
        getIconResource()!!,
        titleResId,
    )



val StreetParkingPermission.titleResId: Int? get() = when (this) {
    FreeParking -> R.string.street_parking_permission_free
    TimeLimit -> R.string.street_parking_permission_time_limit
    ResidentsOnlyParking -> R.string.street_parking_permission_residents_only
    PaidParking -> R.string.street_parking_permission_paid
    PrivateParking -> R.string.street_parking_permission_private
    StreetParkingPermissionSeparate, UnknownStreetParkingPermission -> null
    else -> R.string.street_parking_permission_no_parking
}

/** Image that should be shown in the street side select puzzle */
fun StreetParkingPermission.getIcon(): Image = when (this) {
    FreeParking ->
        ResImage(R.drawable.ic_quest_leaf)
    PaidParking ->
        ResImage(R.drawable.ic_pin_money)
    PrivateParking ->
        ResImage(R.drawable.ic_shoulder_two_yellow_lines)
    ResidentsOnlyParking ->
        ResImage(R.drawable.ic_shoulder_two_yellow_lines)
    StreetParkingPermissionSeparate ->
        ResImage(R.drawable.ic_shoulder_two_yellow_lines)
    TimeLimit ->
        ResImage(R.drawable.ic_shoulder_two_yellow_lines)
    NoParking ->
        ResImage(R.drawable.ic_parking_no)
    UnknownStreetParkingPermission ->
        ResImage(R.drawable.ic_shoulder_two_yellow_lines)
}

/** Icon that should be shown as the icon in a selection dialog */
fun StreetParkingPermission.getDialogIcon(): Image = when (this) {
    FreeParking ->
        ResImage(R.drawable.ic_quest_leaf)
    PaidParking ->
        ResImage(R.drawable.ic_pin_money)
    PrivateParking ->
        ResImage(R.drawable.ic_shoulder_two_yellow_lines)
    ResidentsOnlyParking ->
        ResImage(R.drawable.ic_shoulder_two_yellow_lines)
    StreetParkingPermissionSeparate ->
        ResImage(R.drawable.ic_shoulder_two_yellow_lines)
    NoParking ->
        ResImage(R.drawable.ic_parking_no)
    TimeLimit ->
        ResImage(R.drawable.ic_shoulder_two_yellow_lines)
    UnknownStreetParkingPermission ->
        ResImage(R.drawable.ic_shoulder_two_yellow_lines)
}

/** Icon that should be shown as the floating icon in the street side select puzzle */
fun StreetParkingPermission.getFloatingIcon(): Image? = when (this) {
    FreeParking -> R.drawable.ic_quest_leaf
    PaidParking -> R.drawable.ic_pin_money
    PrivateParking -> R.drawable.ic_shoulder_two_yellow_lines
    ResidentsOnlyParking -> R.drawable.ic_shoulder_two_yellow_lines
    StreetParkingPermissionSeparate -> R.drawable.ic_shoulder_two_yellow_lines
    TimeLimit -> R.drawable.ic_shoulder_two_yellow_lines
    NoParking -> R.drawable.ic_parking_no
    UnknownStreetParkingPermission -> null
}?.let { ResImage(it) }

/** Icon that should be shown as the floating icon in the street side select puzzle */
fun StreetParkingPermission.getIconResource(): Int? = when (this) {
    FreeParking -> R.drawable.ic_quest_leaf
    PaidParking -> R.drawable.ic_pin_money
    PrivateParking -> R.drawable.ic_shoulder_two_yellow_lines
    ResidentsOnlyParking -> R.drawable.ic_shoulder_two_yellow_lines
    StreetParkingPermissionSeparate -> R.drawable.ic_shoulder_two_yellow_lines
    TimeLimit -> R.drawable.ic_shoulder_two_yellow_lines
    NoParking -> R.drawable.ic_parking_no
    UnknownStreetParkingPermission -> null
}



