package de.westnordost.streetcomplete.quests.drinking_water_type

enum class DrinkingWaterType(val osmKey: String, val osmValue: String, actuallyNotDrinkingWater: Boolean=false) {
    // https://wiki.openstreetmap.org/wiki/Key:fountain
    WATER_FOUNTAIN_GENERIC("fountain", "drinking"),
    WATER_FOUNTAIN_JET("fountain", "bubbler"),
    WATER_FOUNTAIN_BOTTLE_REFILL_ONLY("fountain", "bottle_refill"),
    WATER_TAP("man_made", "water_tap"),
    WATER_TAP_UNDRINKABLE("man_made", "water_tap", true),
    WATER_WELL("man_made", "water_well"),
    SPRING("natural", "spring"), // https://en.wikipedia.org/wiki/File:Nacentemackinac.jpg
    DISUSED_DRINKING_WATER("disued:amenity", "drinking_water", true),
}
