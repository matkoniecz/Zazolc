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

fun StreetParkingPermission.asItem(context: Context, countryInfo: CountryInfo, isUpsideDown: Boolean) =
    Item2(this, getDialogIcon(context, countryInfo, isUpsideDown), titleResId?.let { ResText(it) })

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
fun StreetParkingPermission.getIcon(context: Context, countryInfo: CountryInfo, isUpsideDown: Boolean): Image = when (this) {
    FreeParking ->
        ResImage(R.drawable.ic_parking_no)
    PaidParking ->
        ResImage(R.drawable.ic_parking_no)
    PrivateParking ->
        ResImage(R.drawable.ic_parking_no)
    ResidentsOnlyParking ->
        ResImage(R.drawable.ic_parking_no)
    StreetParkingPermissionSeparate ->
        ResImage(R.drawable.ic_parking_no)
    TimeLimit ->
        ResImage(R.drawable.ic_parking_no)
    NoParking ->
        ResImage(R.drawable.ic_parking_no)
    UnknownStreetParkingPermission ->
        ResImage(R.drawable.ic_parking_no)
}

/** Icon that should be shown as the icon in a selection dialog */
fun StreetParkingPermission.getDialogIcon(context: Context, countryInfo: CountryInfo, isUpsideDown: Boolean): Image = when (this) {
    FreeParking ->
        ResImage(R.drawable.ic_parking_no)
    PaidParking ->
        ResImage(R.drawable.ic_parking_no)
    PrivateParking ->
        ResImage(R.drawable.ic_parking_no)
    ResidentsOnlyParking ->
        ResImage(R.drawable.ic_parking_no)
    StreetParkingPermissionSeparate ->
        ResImage(R.drawable.ic_parking_no)
    NoParking ->
        ResImage(R.drawable.ic_parking_no)
    TimeLimit ->
        ResImage(R.drawable.ic_parking_no)
    UnknownStreetParkingPermission ->
        ResImage(R.drawable.ic_parking_no)
}

/** Icon that should be shown as the floating icon in the street side select puzzle */
fun StreetParkingPermission.getFloatingIcon(countryInfo: CountryInfo): Image? = when (this) {
    FreeParking -> R.drawable.ic_parking_no
    PaidParking -> R.drawable.ic_parking_no
    PrivateParking -> R.drawable.ic_parking_no
    ResidentsOnlyParking -> R.drawable.ic_parking_no
    StreetParkingPermissionSeparate -> R.drawable.ic_parking_no
    TimeLimit -> R.drawable.ic_parking_no
    NoParking -> R.drawable.ic_parking_no
    UnknownStreetParkingPermission -> null
}?.let { ResImage(it) }

