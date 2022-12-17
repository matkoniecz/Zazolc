package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.surface.ALIASED_SURFACE_VALUES
import de.westnordost.streetcomplete.osm.surface.SurfaceAnswer
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

fun SidewalkSurfaceAnswer.applyTo(tags: StringMapChangesBuilder) {
    when (this) {
        is SidewalkIsDifferent -> {
            deleteSmoothnessKeys(SidewalkSurfaceAnswer.Side.LEFT, tags)
            deleteSmoothnessKeys(SidewalkSurfaceAnswer.Side.RIGHT, tags)
            deleteSmoothnessKeys(SidewalkSurfaceAnswer.Side.BOTH, tags)
            deleteSidewalkSurfaceAnswerIfExists(SidewalkSurfaceAnswer.Side.LEFT, tags)
            deleteSidewalkSurfaceAnswerIfExists(SidewalkSurfaceAnswer.Side.RIGHT, tags)
            deleteSidewalkSurfaceAnswerIfExists(SidewalkSurfaceAnswer.Side.BOTH, tags)
            tags.remove("sidewalk:left")
            tags.remove("sidewalk:right")
            tags.remove("sidewalk:both")
            tags.remove("sidewalk")
        }
        is SidewalkSurface -> {
            val leftChanged = this.left?.let { sideSurfaceChanged(it, SidewalkSurfaceAnswer.Side.LEFT, tags) }
            val rightChanged = this.right?.let { sideSurfaceChanged(it, SidewalkSurfaceAnswer.Side.RIGHT, tags) }

            if (leftChanged == true) {
                deleteSmoothnessKeys(SidewalkSurfaceAnswer.Side.LEFT, tags)
                deleteSmoothnessKeys(SidewalkSurfaceAnswer.Side.BOTH, tags)
            }
            if (rightChanged == true) {
                deleteSmoothnessKeys(SidewalkSurfaceAnswer.Side.RIGHT, tags)
                deleteSmoothnessKeys(SidewalkSurfaceAnswer.Side.BOTH, tags)
            }

            if (this.left == this.right) {
                this.left?.let { applySidewalkSurfaceAnswerTo(it, SidewalkSurfaceAnswer.Side.BOTH, tags) }
                deleteSidewalkSurfaceAnswerIfExists(SidewalkSurfaceAnswer.Side.LEFT, tags)
                deleteSidewalkSurfaceAnswerIfExists(SidewalkSurfaceAnswer.Side.RIGHT, tags)
            } else {
                this.left?.let { applySidewalkSurfaceAnswerTo(it, SidewalkSurfaceAnswer.Side.LEFT, tags) }
                this.right?.let { applySidewalkSurfaceAnswerTo(it, SidewalkSurfaceAnswer.Side.RIGHT, tags) }
                deleteSidewalkSurfaceAnswerIfExists(SidewalkSurfaceAnswer.Side.BOTH, tags)
            }
        }
    }

    deleteSidewalkSurfaceAnswerIfExists(null, tags)

    // only set the check date if nothing was changed or if check date was already set
    if (!tags.hasChanges || tags.hasCheckDateForKey("sidewalk:surface")) {
        tags.updateCheckDateForKey("sidewalk:surface")
    }
}

private fun applySidewalkSurfaceAnswerTo(surface: SurfaceAnswer, side: SidewalkSurfaceAnswer.Side, tags: Tags) {
    val sidewalkKey = "sidewalk:" + side.value
    val sidewalkSurfaceKey = "$sidewalkKey:surface"

    var osmValue = surface.value.osmValue
    val previousOsmValue = tags[sidewalkSurfaceKey]
    if(previousOsmValue != null) {
        if(ALIASED_SURFACE_VALUES[previousOsmValue]?.osmValue == osmValue) {
            osmValue = previousOsmValue
        }
    }
    tags[sidewalkSurfaceKey] = osmValue

    // add/remove note - used to describe generic surfaces
    if (surface.note != null) {
        tags["$sidewalkSurfaceKey:note"] = surface.note
    } else {
        tags.remove("$sidewalkSurfaceKey:note")
    }
    // clean up old source tags - source should be in changeset tags
    tags.remove("source:$sidewalkSurfaceKey")
}

/** clear smoothness tags for the given side*/
private fun deleteSmoothnessKeys(side: SidewalkSurfaceAnswer.Side, tags: Tags) {
    val sidewalkKey = "sidewalk:" + side.value
    tags.remove("$sidewalkKey:smoothness")
    tags.remove("$sidewalkKey:smoothness:date")
    tags.removeCheckDatesForKey("$sidewalkKey:smoothness")
}

/** clear previous answers for the given side */
private fun deleteSidewalkSurfaceAnswerIfExists(side: SidewalkSurfaceAnswer.Side?, tags: Tags) {
    val sideVal = if (side == null) "" else ":" + side.value
    val sidewalkSurfaceKey = "sidewalk$sideVal:surface"

    // only things are cleared that are set by this quest
    // for example cycleway:surface should only be cleared by a cycleway surface quest etc.
    tags.remove(sidewalkSurfaceKey)
    tags.remove("$sidewalkSurfaceKey:note")
}

private fun sideSurfaceChanged(surface: SurfaceAnswer, side: SidewalkSurfaceAnswer.Side, tags: Tags): Boolean {
    val previousSideOsmValue = tags["sidewalk:${side.value}:surface"]
    val previousBothOsmValue = tags["sidewalk:both:surface"]
    val osmValue = surface.value.osmValue

    return previousSideOsmValue != null && previousSideOsmValue != osmValue
        || previousBothOsmValue != null && previousBothOsmValue != osmValue
}
