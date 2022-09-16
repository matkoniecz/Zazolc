package de.westnordost.streetcomplete.quests.drinking_water_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.SPRING
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.WATER_FOUNTAIN_BOTTLE_REFILL_ONLY
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.WATER_FOUNTAIN_GENERIC
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.WATER_FOUNTAIN_JET
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.WATER_TAP
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.WATER_TAP_UNDRINKABLE
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.WATER_WELL
import de.westnordost.streetcomplete.quests.drinking_water_type.DrinkingWaterType.DISUSED_DRINKING_WATER
import de.westnordost.streetcomplete.view.image_select.Item

fun DrinkingWaterType.asItem() = Item(this, iconResId, titleResId)

private val DrinkingWaterType.titleResId: Int get() = when (this) {
    WATER_FOUNTAIN_GENERIC -> R.string.quest_drinking_water_type_generic_water_fountain
    WATER_FOUNTAIN_JET -> R.string.quest_drinking_water_type_jet_water_fountain
    WATER_FOUNTAIN_BOTTLE_REFILL_ONLY -> R.string.quest_drinking_water_type_bottle_refill_only_fountain
    WATER_TAP -> R.string.quest_drinking_water_type_tap
    WATER_TAP_UNDRINKABLE -> R.string.quest_drinking_water_type_tap_without_drinking_water
    WATER_WELL -> R.string.quest_drinking_water_type_water_well
    SPRING -> R.string.quest_drinking_water_type_spring
    DISUSED_DRINKING_WATER -> R.string.quest_drinking_water_type_disused
}

private val DrinkingWaterType.iconResId: Int get() = when (this) {
    WATER_FOUNTAIN_GENERIC -> R.drawable.traffic_calming_bump
    WATER_FOUNTAIN_JET -> R.drawable.traffic_calming_bump
    WATER_FOUNTAIN_BOTTLE_REFILL_ONLY -> R.drawable.traffic_calming_bump
    WATER_TAP -> R.drawable.traffic_calming_bump
    WATER_TAP_UNDRINKABLE -> R.drawable.traffic_calming_bump
    WATER_WELL -> R.drawable.traffic_calming_bump
    SPRING -> R.drawable.traffic_calming_bump
    DISUSED_DRINKING_WATER -> R.drawable.traffic_calming_bump
}
