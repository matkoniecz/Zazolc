package de.westnordost.streetcomplete.osm.street_parking

data class LeftAndRightStreetParkingPermission(val left: StreetParkingPermission?, val right: StreetParkingPermission?)


sealed class StreetParkingPermission

object FreeParking : StreetParkingPermission()
object PaidParking : StreetParkingPermission()
object ResidentsOnlyParking : StreetParkingPermission()
object PrivateParking : StreetParkingPermission()
object TimeLimit : StreetParkingPermission()
object NoParking : StreetParkingPermission()

/** When an unknown/unsupported value has been used */
object UnknownStreetParkingPermission : StreetParkingPermission()
/** There is street parking, but it is mapped as separate geometry */
object StreetParkingPermissionSeparate : StreetParkingPermission()

fun StreetParkingPermission.toOsmConditionValue() = when (this) {
    FreeParking -> "free"
    TimeLimit -> "disc" // add also parking:condition:side:maxstay=2 hours
    ResidentsOnlyParking -> "residents"
    PaidParking -> "ticket"
    PrivateParking -> "private"
    UnknownStreetParkingPermission, StreetParkingPermissionSeparate -> null
    else -> null
}
