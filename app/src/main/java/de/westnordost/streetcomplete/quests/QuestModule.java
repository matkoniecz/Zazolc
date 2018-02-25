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
import de.westnordost.streetcomplete.quests.bikeway.AddCyclewayBoolean;
import de.westnordost.streetcomplete.quests.bridge_structure.AddBridgeStructure;
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels;
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter;
import de.westnordost.streetcomplete.quests.car_wash_type.AddCarWashType;
import de.westnordost.streetcomplete.quests.crossing_type.AddCrossingType;
import de.westnordost.streetcomplete.quests.diet_type.AddVegan;
import de.westnordost.streetcomplete.quests.diet_type.AddVegetarian;
import de.westnordost.streetcomplete.quests.fire_hydrant.AddFireHydrantType;
import de.westnordost.streetcomplete.quests.leaf_detail.AddForestLeafType;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours;
import de.westnordost.streetcomplete.quests.internet_access.AddInternetAccess;
import de.westnordost.streetcomplete.quests.parking_access.AddParkingAccess;
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFee;
import de.westnordost.streetcomplete.quests.parking_type.AddParkingType;
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName;
import de.westnordost.streetcomplete.quests.powerpoles_material.AddPowerPolesMaterial;
import de.westnordost.streetcomplete.quests.orchard_produce.AddOrchardProduce;
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType;
import de.westnordost.streetcomplete.quests.religion.AddReligionToPlaceOfWorship;
import de.westnordost.streetcomplete.quests.religion.AddReligionToWaysideShrine;
import de.westnordost.streetcomplete.quests.road_name.data.PutRoadNameSuggestionsHandler;
import de.westnordost.streetcomplete.quests.road_name.data.RoadNameSuggestionsDao;
import de.westnordost.streetcomplete.quests.separate_sidewalk.AddWaySidewalk;
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop;
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk;
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability;
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee;
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber;
import de.westnordost.streetcomplete.quests.leaf_detail.AddForestLeafCycle;
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed;
import de.westnordost.streetcomplete.quests.road_name.AddRoadName;
import de.westnordost.streetcomplete.quests.road_surface.AddRoadSurface;
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShape;
import de.westnordost.streetcomplete.quests.show_fixme.ShowFixme;
import de.westnordost.streetcomplete.quests.sport.AddSport;
import de.westnordost.streetcomplete.quests.validator.AccessPublicToYes;
import de.westnordost.streetcomplete.quests.validator.multidesignatedFootwayToPath;
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelChairAccessPublicTransport;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelChairAccessToilets;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusiness;
import de.westnordost.streetcomplete.quests.bench_backrest.AddBenchBackrest;

@Module
public class QuestModule
{
	@Provides @Singleton public static QuestTypeRegistry questTypeRegistry(
			OsmNoteQuestType osmNoteQuestType, OverpassMapDataDao o,
			RoadNameSuggestionsDao roadNameSuggestionsDao,
			PutRoadNameSuggestionsHandler putRoadNameSuggestionsHandler)
	{
		QuestType[] questTypesOrderedByImportance = {
				osmNoteQuestType,
				new multidesignatedFootwayToPath(o), //my own quest
                new AddCyclewayBoolean(o), //my own quest, disabled by default but after enabling should be a top one
                new AddWayLit(o), //frequent enable/disable cycle (enable for night)
				new AddBikeParkingType(o),
                new ShowFixme(o), //my own quest
				new AddForestLeafCycle(o), //my own quest
				new AddForestLeafType(o), //my own quest
				new AddBikeParkingCapacity(o),
				new AddBikeParkingCover(o),
				new AddReligionToPlaceOfWorship(o),
				new AddRoadSurface(o),
				new AddMaxSpeed(o), // should best be after road surface because it excludes unpaved roads
				new AddOrchardProduce(o),
				new AddWheelChairAccessPublicTransport(o),
				new AddWheelchairAccessBusiness(o),
				new AddToiletAvailability(o),
				new AddBridgeStructure(o),
				new AddWheelChairAccessToilets(o),
				new AddBusStopShelter(o), // at least OsmAnd
				new AddTactilePavingBusStop(o),
				new AddReligionToWaysideShrine(o),
				new AddToiletsFee(o),
				new AddBabyChangingTable(o),
				new AddFireHydrantType(o),
				new AddParkingAccess(o),
				new AddParkingFee(o),
				new AddParkingType(o),
				new AccessPublicToYes(o),
				new AddPlaceName(o), //works with my horrible hack

                //boring
                new AddInternetAccess(o),
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
        		new AddPowerPolesMaterial(o),
				new AddVegetarian(o), //reduced importance
				new AddVegan(o), //reduced importance
				new AddWaySidewalk(o), //my own quest
				new AddCarWashType(o),
				new AddBenchBackrest(o),
		};

		return new QuestTypeRegistry(Arrays.asList(questTypesOrderedByImportance));
	}

	@Provides @Singleton public static OsmNoteQuestType osmNoteQuestType()
	{
		return new OsmNoteQuestType();
	}
}
