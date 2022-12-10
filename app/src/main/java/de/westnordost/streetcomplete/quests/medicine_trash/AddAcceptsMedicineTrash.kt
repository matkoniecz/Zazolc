package de.westnordost.streetcomplete.quests.medicine_trash

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo
import java.util.concurrent.FutureTask

public class AddAcceptsMedicineTrash(
        private val featureDictionaryFuture: FutureTask<FeatureDictionary>
    ) : OsmFilterQuestType<Boolean>() {

        override val elementFilter: String get() {
            return "nodes, ways, relations with amenity=pharmacy and !trash_accepted:medicines" }

        override val changesetComment = "Add whether this place accepts medicines as a trash"
        // override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside
        override val wikiLink = "Key:trash_accepted:medicines"
        override val icon = R.drawable.ic_quest_bin_public_transport
        override val isReplaceShopEnabled = true

        // override val enabledInCountries = NoCountriesExcept("PL")

        override fun getTitle(tags: Map<String, String>) = R.string.quest_accepts_medicine_trash_title

        override fun createForm() = YesNoQuestForm()

        override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
            tags["trash_accepted:medicines"] = answer.toYesNo()
        }

        private fun hasFeatureName(tags: Map<String, String>): Boolean =
            featureDictionaryFuture.get().byTags(tags).isSuggestion(false).find().isNotEmpty()

    override val achievements: List<EditTypeAchievement> = listOf()
}
