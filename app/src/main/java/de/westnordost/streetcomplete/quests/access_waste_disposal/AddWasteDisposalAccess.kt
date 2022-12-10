package de.westnordost.streetcomplete.quests.access_waste_disposal

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.quests.YesNoQuestForm

class AddWasteDisposalAccess : OsmFilterQuestType<Boolean>() {

    override val elementFilter = "nodes, ways, relations with amenity = waste_disposal and (!access or access = unknown)"
    override val changesetComment = "Add garbage dumpster access"
    override val wikiLink = "Key:access"
    override val icon = R.drawable.ic_quest_bin_public_transport

    override fun getTitle(tags: Map<String, String>) = R.string.quest_waste_disposal_access_title

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["access"] = if (answer) "yes" else "private"
    }

    override val achievements: List<EditTypeAchievement> = listOf()
}
