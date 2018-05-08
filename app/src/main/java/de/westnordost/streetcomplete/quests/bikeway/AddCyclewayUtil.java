package de.westnordost.streetcomplete.quests.bikeway;

import android.text.TextUtils;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.data.meta.OsmTaggings;
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil;

class AddCyclewayUtil {
	private static final int MIN_DIST_TO_CYCLEWAYS = 15; //m

	/** @return overpass query string to get streets without cycleway info not near paths for
	 *  bicycles. */
	static String getOverpassQuery(BoundingBox bbox, boolean disableMaxspeedFilter, boolean moreRoadTypes)
	{
		int d = MIN_DIST_TO_CYCLEWAYS;
		String query = OverpassQLUtil.getGlobalOverpassBBox(bbox);
		if(moreRoadTypes){
			query += "way[highway ~ \"^(primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|service|track|pedestrian)$\"]";
		}else{
			query += "way[highway ~ \"^(primary|secondary|tertiary|unclassified)$\"]";
		}
			query += "[area != yes]" +
			// only without cycleway tags
			"[!cycleway][!\"cycleway:left\"][!\"cycleway:right\"][!\"cycleway:both\"]" +
			"[!\"sidewalk:bicycle\"][!\"sidewalk:both:bicycle\"][!\"sidewalk:left:bicycle\"][!\"sidewalk:right:bicycle\"]";
			if(!disableMaxspeedFilter){
				// not any with low speed limit because they not very likely to have cycleway infrastructure
				query += "[maxspeed !~ \"^(20|15|10|8|7|6|5|20 mph|15 mph|10 mph|5 mph|walk)$\"]";
			}
			// not any unpaved because of the same reason
			query += "[surface !~ \"^("+ TextUtils.join("|", OsmTaggings.ANYTHING_UNPAVED)+")$\"]" +
			// not any explicitly tagged as no bicycles
			"[bicycle != no]" +
			"[access !~ \"^private|no$\"]" +
			" -> .streets;" +
			"(" +
			"way[highway=cycleway](around.streets: "+d+");" +
			// See #718: If a separate way exists, it may be that the user's answer should
			// correctly be tagged on that separate way and not on the street -> this app would
			// tag data on the wrong elements. So, don't ask at all for separately mapped ways.
			// :-(
			"way[highway ~ \"^(path|footway)$\"](around.streets: "+d+");" +
			") -> .cycleways;" +
			"way.streets(around.cycleways: "+d+") -> .streets_near_cycleways;" +
			"(.streets; - .streets_near_cycleways;);" +
			"out meta geom;";
		return query;
	}
}
