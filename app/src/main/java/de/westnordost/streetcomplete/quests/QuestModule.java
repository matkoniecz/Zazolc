package de.westnordost.streetcomplete.quests;

import java.util.Arrays;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType;
import de.westnordost.streetcomplete.quests.baby_changing_table.AddBabyChangingTable;
import de.westnordost.streetcomplete.quests.bike_parking_capacity.AddBikeParkingCapacity;
import de.westnordost.streetcomplete.quests.bike_parking_cover.AddBikeParkingCover;
import de.westnordost.streetcomplete.quests.bike_parking_type.AddBikeParkingType;
import de.westnordost.streetcomplete.quests.bikeway.AddCycleway;
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels;
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter;
import de.westnordost.streetcomplete.quests.crossing_type.AddCrossingType;
import de.westnordost.streetcomplete.quests.fire_hydrant.AddFireHydrantType;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours;
import de.westnordost.streetcomplete.quests.parking_type.AddParkingType;
import de.westnordost.streetcomplete.quests.powerpoles_material.AddPowerPolesMaterial;
import de.westnordost.streetcomplete.quests.orchard_produce.AddOrchardProduce;
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType;
import de.westnordost.streetcomplete.quests.road_name.data.PutRoadNameSuggestionsHandler;
import de.westnordost.streetcomplete.quests.road_name.data.RoadNameSuggestionsDao;
import de.westnordost.streetcomplete.quests.road_surface.AddServiceRoadSurface;
import de.westnordost.streetcomplete.quests.road_surface.DetailPavedPathSurface;
import de.westnordost.streetcomplete.quests.road_surface.DetailPavedServiceRoadSurface;
import de.westnordost.streetcomplete.quests.road_surface.DetailUnpavedPathSurface;
import de.westnordost.streetcomplete.quests.road_surface.DetailUnpavedServiceRoadSurface;
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop;
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk;
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability;
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee;
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber;
import de.westnordost.streetcomplete.quests.leaf_detail.AddTreeLeafCycle;
import de.westnordost.streetcomplete.quests.leaf_detail.AddTreeLeafType;
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed;
import de.westnordost.streetcomplete.quests.parking_access.AddParkingAccess;
import de.westnordost.streetcomplete.quests.road_name.AddRoadName;
import de.westnordost.streetcomplete.quests.road_surface.AddRoadSurface;
import de.westnordost.streetcomplete.quests.road_surface.DetailPavedRoadSurface;
import de.westnordost.streetcomplete.quests.road_surface.DetailUnpavedRoadSurface;
import de.westnordost.streetcomplete.quests.road_surface.ShowInvalidSurface;
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShape;
import de.westnordost.streetcomplete.quests.show_fixme.ShowFixme;
import de.westnordost.streetcomplete.quests.sport.AddSport;
import de.westnordost.streetcomplete.quests.validator.multidesignatedFootwayToPath;
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelChairAccessPublicTransport;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusiness;

@Module
public class QuestModule
{
	@Provides @Singleton public static QuestTypeRegistry questTypeRegistry(
			OsmNoteQuestType osmNoteQuestType, OverpassMapDataDao o,
			RoadNameSuggestionsDao roadNameSuggestionsDao,
			PutRoadNameSuggestionsHandler putRoadNameSuggestionsHandler)
	{
		QuestType[] questTypesOrderedByImportance = {
				// ↓ 1. notes
				osmNoteQuestType,
				// ↓ may be shown as missing in QA tools
				new multidesignatedFootwayToPath(o), //my own quest
                new ShowFixme(o), //my own quest
				new ShowInvalidSurface(o), //my own quest
				new AddBikeParkingCapacity(o),
				new AddBikeParkingCover(o),
				new AddBikeParkingType(o), //my own quest
				// ↓ may be shown as possibly missing in QA tools
				new AddParkingAccess(o), //my own quest
				// new AddPlaceName(o), doesn't make sense as long as the app cannot tell the generic name of elements
				new AddRoadSurface(o),
				new DetailPavedRoadSurface(o), //my own quest
				new DetailUnpavedRoadSurface(o), //my own quest
				new DetailUnpavedPathSurface(o), //my own quest
				new DetailPavedPathSurface(o), //my own quest
				new AddMaxSpeed(o),
				// ↓ useful data that is used by some data consumers
				new AddOrchardProduce(o),
				// ↓ data useful for only a specific use case
				// new AddPlaceName(), doesn't make sense as long as the app cannot tell the generic name of elements
				new AddWheelChairAccessPublicTransport(o),
				new AddWheelchairAccessBusiness(o),
				new AddToiletAvailability(o),
				new AddBusStopShelter(o), // at least OsmAnd
				new AddTactilePavingBusStop(o),
				new AddToiletsFee(o),
				new AddWayLit(o),
				new AddServiceRoadSurface(o),
				new DetailUnpavedServiceRoadSurface(o),
				new DetailPavedServiceRoadSurface(o),
				new AddBabyChangingTable(o),
				new AddFireHydrantType(o),
				new AddTreeLeafCycle(o), //my own quest
				new AddTreeLeafType(o), //my own quest
				new AddParkingType(o),
                //boring
				new AddCycleway(o), //reduced importance
				new AddCrossingType(o), //reduced importance
				new AddTactilePavingCrosswalk(o), //reduced importance
				new AddRecyclingType(o),  //reduced importance
				new AddSport(o), //reduced importance
				new AddHousenumber(o), //reduced importance
                new AddRoadName(o, roadNameSuggestionsDao, putRoadNameSuggestionsHandler), //reduced importance
                new AddRoofShape(o), //reduced importance
                new AddBuildingLevels(o), //reduced importance
                new AddOpeningHours(o), //reduced importance
        		new AddPowerPolesMaterial(o)
		};

		return new QuestTypeRegistry(Arrays.asList(questTypesOrderedByImportance));
	}

	@Provides @Singleton public static OsmNoteQuestType osmNoteQuestType()
	{
		return new OsmNoteQuestType();
	}
}
