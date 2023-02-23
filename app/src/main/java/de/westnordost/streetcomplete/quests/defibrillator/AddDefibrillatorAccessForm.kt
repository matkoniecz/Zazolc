package de.westnordost.streetcomplete.quests.defibrillator

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem
import de.westnordost.streetcomplete.quests.defibrillator.DefibrillatorAccess.CUSTOMERS
import de.westnordost.streetcomplete.quests.defibrillator.DefibrillatorAccess.PRIVATE
import de.westnordost.streetcomplete.quests.defibrillator.DefibrillatorAccess.YES

class AddDefibrillatorAccessForm : AListQuestForm<DefibrillatorAccess>() {

    override val items = listOf(
        TextItem(YES, R.string.quest_access_yes),
        TextItem(CUSTOMERS, R.string.quest_access_customers),
        TextItem(PRIVATE, R.string.quest_access_private),
    )
}
