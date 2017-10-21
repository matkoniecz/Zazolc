package de.westnordost.streetcomplete.quests.road_surface;

public class RoadSurfaceConfig {
	// well, all roads have surfaces, what I mean is that not all ways with highway key are
	// "something with a surface"
	static final String[] ROADS_WITH_SURFACES = {
			"trunk","trunk_link","motorway","motorway_link",
			"primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
			"unclassified", "residential", "bicycle_road", "living_street", "pedestrian",
			"track", "road", "footway", "cycleway", "path", "steps",
	};
	static final String[] ROADS_WITH_DETAILED_SURFACES = {
			"trunk","trunk_link","motorway","motorway_link",
			"primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
			"unclassified", "residential", "bicycle_road", "living_street", "pedestrian",
			"track", "road", "service", "cycleway",
	};
}
