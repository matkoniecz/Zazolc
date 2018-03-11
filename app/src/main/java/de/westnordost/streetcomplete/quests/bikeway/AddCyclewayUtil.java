package de.westnordost.streetcomplete.quests.bikeway;

import android.text.TextUtils;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.data.meta.OsmTaggings;
import de.westnordost.streetcomplete.data.osm.tql.OverpassQLUtil;

class AddCyclewayUtil {
	private static final int MIN_DIST_TO_CYCLEWAYS = 15; //m

	/** @return overpass query string to get streets without cycleway info not near paths for
	 *  bicycles. */
	static String getOverpassQuery(BoundingBox bbox, boolean enableMaxspeedFilter)
	{
		int d = MIN_DIST_TO_CYCLEWAYS;
		String query = OverpassQLUtil.getGlobalOverpassBBox(bbox) +
			"way[highway ~ \"^(primary|secondary|tertiary|unclassified|residential)$\"]" +
			"[area != yes]" +
			// only without cycleway tags
			"[!cycleway][!\"cycleway:left\"][!\"cycleway:right\"][!\"cycleway:both\"]" +
			"[!\"sidewalk:bicycle\"][!\"sidewalk:both:bicycle\"][!\"sidewalk:left:bicycle\"][!\"sidewalk:right:bicycle\"]";
			if(enableMaxspeedFilter){
				// not any with low speed limit because they not very likely to have cycleway infrastructure
				query += "[maxspeed !~ \"^(30|25|20|15|10|8|7|6|5|20 mph|15 mph|10 mph|5 mph|walk)$\"]";
			}
			// not any unpaved because they not very likely to have cycleway infrastructure
			query += "[surface !~ \"^("+ TextUtils.join("|", OsmTaggings.ANYTHING_UNPAVED)+")$\"]" +
			// not any explicitly tagged as no bicycles
			"[bicycle != no]" +
			"[access !~ \"^private|no$\"]" +
			" -> .streets;" +
			"(" +
			"way[highway=cycleway](around.streets: "+d+");" +
			"way[highway ~ \"^(path|footway)$\"][bicycle ~ \"^(yes|designated)$\"](around.streets: "+d+");" +
			") -> .cycleways;" +
			"way.streets(around.cycleways: "+d+") -> .streets_near_cycleways;" +
			"(.streets; - .streets_near_cycleways;);" +
			"out meta geom;";
			return query;
	}
}
