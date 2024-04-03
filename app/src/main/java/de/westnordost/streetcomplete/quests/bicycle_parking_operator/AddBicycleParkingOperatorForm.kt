package de.westnordost.streetcomplete.quests.bicycle_parking_operator
import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm

class AddBicycleParkingOperatorForm : ANameWithSuggestionsForm<String>() {
    override val suggestions: List<String> get() = listOf("ZTP Kraków", "ZZM Kraków")

    override fun onClickOk() {
        applyAnswer(name!!)
    }
}
