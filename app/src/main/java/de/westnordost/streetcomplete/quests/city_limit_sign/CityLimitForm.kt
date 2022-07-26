package de.westnordost.streetcomplete.quests.city_limit_sign

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddCityLimitForm : AListQuestForm<CityLimit>() {

    override val items = listOf(
        TextItem(CityLimit.CITY_LIMIT_START_BUILT_UP_AREA_START, R.string.quest_city_limit_sign_both_start),
        TextItem(CityLimit.CITY_LIMIT_START, R.string.quest_city_limit_sign_of_city_limit_start),
        TextItem(CityLimit.CITY_LIMIT_END, R.string.quest_city_limit_sign_of_city_limit_end),
        TextItem(CityLimit.CITY_LIMIT_BOTH, R.string.quest_city_limit_sign_of_city_limit_start_and_end),
        TextItem(CityLimit.BUILT_UP_AREA_END, R.string.quest_city_limit_sign_of_builtup_area_end),
        TextItem(CityLimit.BUILT_UP_AREA_START, R.string.quest_city_limit_sign_of_builtup_area_start),
        TextItem(CityLimit.BUILT_UP_AREA_END, R.string.quest_city_limit_sign_of_builtup_area_end),
    )
}
