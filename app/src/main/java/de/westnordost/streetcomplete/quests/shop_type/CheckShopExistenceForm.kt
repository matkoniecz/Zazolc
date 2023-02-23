package de.westnordost.streetcomplete.quests.existence

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class CheckShopExistenceForm : AbstractOsmQuestForm<Unit>() {
    override val buttonPanelAnswers get() =
        if (isAskingAboutExistence) {
            listOf(
                AnswerItem(R.string.quest_generic_hasFeature_no) {
                    replaceShop()
                },
                AnswerItem(R.string.quest_generic_hasFeature_yes) {
                    applyAnswer(Unit)
                }
            )
        } else {
            emptyList()
        }

    private var isAskingAboutExistence: Boolean = true
}
