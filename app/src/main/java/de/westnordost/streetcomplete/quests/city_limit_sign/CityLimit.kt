package de.westnordost.streetcomplete.quests.city_limit_sign

enum class CityLimit(val signCode: String) {
    CITY_LIMIT_START_BUILT_UP_AREA_START("PL:D-42;PL:E-17a"),
    CITY_LIMIT_START("PL:E-17a"),
    CITY_LIMIT_END("PL:E-18a"),
    CITY_LIMIT_BOTH("PL:E-17a;PL:E-18a"),
    BUILT_UP_AREA_START("PL:D-42"),
    BUILT_UP_AREA_END("PL:D-43"),
}
