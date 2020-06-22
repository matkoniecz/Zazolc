package de.westnordost.streetcomplete.quests.surface

import android.util.Log
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.tagfilters.getQuestPrintStatement
import de.westnordost.streetcomplete.data.tagfilters.toGlobalOverpassBBox


class DetailRoadSurface(private val overpassMapDataApi: OverpassMapDataAndGeometryApi) : OsmElementQuestType<String> {
    override val commitMessage = "Add more detailed surfaces"
    override val wikiLink = "Key:surface"
    override val icon = R.drawable.ic_quest_street_surface_paved_detail // consider changing icon or restricting to surface=paved

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val isSquare = tags["area"] == "yes"

        //TODO move to a separate quest
        /*
        if (tags["highway"] == "steps") {
            return R.string.quest_pathSurface_title_steps;
        }
        if (tags["highway"] == "footway" || tags["highway"] == "path") {
            return R.string.quest_pathSurface_title;
        }
        if (tags["highway"] == "bridleway") {
            return R.string.quest_pathSurface_title_bridleway;
        }
        */

        return if (hasName) {
            if (isSquare)
                R.string.quest_streetSurface_square_name_title
            else
                R.string.quest_streetSurface_name_title
        } else {
            if (isSquare)
                R.string.quest_streetSurface_square_title
            else
                R.string.quest_streetSurface_title
        }
    }

    override fun createForm() = AddRoadSurfaceForm()

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        Log.e("YAYAY", getOverpassQuery(bbox))
        return overpassMapDataApi.query(getOverpassQuery(bbox), handler)
    }

    override fun isApplicableTo(element: Element): Boolean? {
        TODO("Not yet implemented")
    }

    private fun getOverpassQuery(bbox: BoundingBox) =
        bbox.toGlobalOverpassBBox() + "\n" + """

          nwr[surface~"^(paved|unpaved)${'$'}"][segregated!="yes"][highway ~ "^${ ROADS_WITH_SURFACES_BROADLY_DEFINED.joinToString("|")}${'$'}"] -> .surface_without_detail;
          // https://taginfo.openstreetmap.org//search?q=%3Asurface
          // https://taginfo.openstreetmap.org//search?q=surface:
          nwr[~"(:surface|surface:)"~"."] -> .extra_tags;

          (.surface_without_detail; - .extra_tags;);
        """.trimIndent() + "\n" +
        getQuestPrintStatement()

    override val isSplitWayEnabled = true

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        if(answer == "paved") {
            return  // and crash TODO!
        }
        if(answer == "unpaved") {
            return  // and crash TODO!
        }
        changes.modify("surface", answer)
    }

    companion object {
        // well, all roads have surfaces, what I mean is that not all ways with highway key are
        // "something with a surface"
        // see https://github.com/westnordost/StreetComplete/pull/327#discussion_r121937808
        private val ROADS_WITH_SURFACES_BROADLY_DEFINED = arrayOf(
                "trunk","trunk_link","motorway","motorway_link",
                "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
                "unclassified", "residential", "living_street", "pedestrian", "track", "road",
                "service"
        )
    }
}
