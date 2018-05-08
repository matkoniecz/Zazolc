package de.westnordost.streetcomplete.quests.bikeway;

import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;


public class AddCyclewayBooleanAggressive extends AddCyclewayBoolean {
	public AddCyclewayBooleanAggressive(OverpassMapDataDao overpassServer) {
		super(overpassServer);
	}

	/** @return overpass query string to get streets without cycleway info not near paths for
	 *  bicycles. */
	private static String getOverpassQuery(BoundingBox bbox)
	{
		return AddCyclewayUtil.getOverpassQuery(bbox, true, true);
	}

	@Override public int getTitle() { return R.string.quest_cycleway_boolean_aggressive_title; }

	@Override public int getDefaultDisabledMessage() { return R.string.default_disabled_msg_special; }
}
