package de.westnordost.streetcomplete.quests.religion

enum class ChristianDenomination(val osmValue: String) {
    // sorted by worldwide usages, *minus* country specific ones
    // https://wiki.openstreetmap.org/wiki/Key:denomination#Christian_denominations
    ROMAN_CATHOLIC("roman_catholic"),
    UNSPECIFIC_CATHOLIC("catholic"),
    UNSPECIFIC_PROTESTANT("protestant"),
}
