package de.westnordost.streetcomplete.quests;

import java.util.Arrays;
import java.util.concurrent.FutureTask;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.westnordost.osmfeatures.FeatureDictionary;
import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.QuestTypeRegistry;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType;
import de.westnordost.streetcomplete.quests.baby_changing_table.AddBabyChangingTable;
import de.westnordost.streetcomplete.quests.bench_backrest.AddBenchBackrest;
import de.westnordost.streetcomplete.quests.bike_parking_capacity.AddBikeParkingCapacity;
import de.westnordost.streetcomplete.quests.bike_parking_cover.AddBikeParkingCover;
import de.westnordost.streetcomplete.quests.bike_parking_type.AddBikeParkingType;
import de.westnordost.streetcomplete.quests.bikeway.AddCycleway;
import de.westnordost.streetcomplete.quests.bridge_structure.AddBridgeStructure;
import de.westnordost.streetcomplete.quests.building_type.AddBuildingType;
import de.westnordost.streetcomplete.quests.building_underground.AddIsBuildingUnderground;
import de.westnordost.streetcomplete.quests.fixme_show.ShowFixme;
import de.westnordost.streetcomplete.quests.foot.AddProhibitedForPedestrians;
import de.westnordost.streetcomplete.quests.leaf_detail.AddForestLeafType;
import de.westnordost.streetcomplete.quests.localized_name.AddBusStopName;
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter;
import de.westnordost.streetcomplete.quests.car_wash_type.AddCarWashType;
import de.westnordost.streetcomplete.quests.construction.MarkCompletedBuildingConstruction;
import de.westnordost.streetcomplete.quests.construction.MarkCompletedHighwayConstruction;
import de.westnordost.streetcomplete.quests.crossing_type.AddCrossingType;
import de.westnordost.streetcomplete.quests.diet_type.AddVegan;
import de.westnordost.streetcomplete.quests.diet_type.AddVegetarian;
import de.westnordost.streetcomplete.quests.fire_hydrant.AddFireHydrantType;
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber;
import de.westnordost.streetcomplete.quests.internet_access.AddInternetAccess;
import de.westnordost.streetcomplete.quests.localized_name.AddRoadName;
import de.westnordost.streetcomplete.quests.localized_name.data.PutRoadNameSuggestionsHandler;
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao;
import de.westnordost.streetcomplete.quests.max_height.AddMaxHeight;
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed;
import de.westnordost.streetcomplete.quests.motorcycle_parking_capacity.AddMotorcycleParkingCapacity;
import de.westnordost.streetcomplete.quests.motorcycle_parking_cover.AddMotorcycleParkingCover;
import de.westnordost.streetcomplete.quests.oneway.AddOneway;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours;
import de.westnordost.streetcomplete.quests.orchard_produce.AddOrchardProduce;
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegmentsDao;
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowDao;
import de.westnordost.streetcomplete.quests.parking_access.AddParkingAccess;
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFee;
import de.westnordost.streetcomplete.quests.parking_type.AddParkingType;
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName;
import de.westnordost.streetcomplete.quests.playground_access.AddPlaygroundAccess;
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddPostboxCollectionTimes;
import de.westnordost.streetcomplete.quests.powerpoles_material.AddPowerPolesMaterial;
import de.westnordost.streetcomplete.quests.railway_crossing.AddRailwayCrossingBarrier;
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType;
import de.westnordost.streetcomplete.quests.religion.AddReligionToPlaceOfWorship;
import de.westnordost.streetcomplete.quests.religion.AddReligionToWaysideShrine;
import de.westnordost.streetcomplete.quests.segregated.AddCyclewaySegregation;
import de.westnordost.streetcomplete.quests.shop_type.AddShopType;
import de.westnordost.streetcomplete.quests.sport.AddSport;
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk;
import de.westnordost.streetcomplete.quests.surface.AddCyclewayPartSurface;
import de.westnordost.streetcomplete.quests.surface.AddPathSurface;
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface;
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop;
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk;
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability;
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee;
import de.westnordost.streetcomplete.quests.tracktype.AddTracktype;
import de.westnordost.streetcomplete.quests.traffic_signals_button.AddTrafficSignalsButton;
import de.westnordost.streetcomplete.quests.traffic_signals_sound.AddTrafficSignalsSound;
import de.westnordost.streetcomplete.quests.validator.AddAlsoShopForInsurance;
import de.westnordost.streetcomplete.quests.validator.DeprecateFIXME;
import de.westnordost.streetcomplete.quests.validator.DetectHistoricRailwayTagging;
import de.westnordost.streetcomplete.quests.validator.MultidesignatedFootwayToPath;
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelChairAccessPublicTransport;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelChairAccessToilets;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusiness;
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessOutside;
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessMotorVehicle;
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessPedestrian;

@Module
public class QuestModule
{
	@Provides @Singleton public static QuestTypeRegistry questTypeRegistry(
		OsmNoteQuestType osmNoteQuestType, OverpassMapDataDao o,
		RoadNameSuggestionsDao roadNameSuggestionsDao,
		PutRoadNameSuggestionsHandler putRoadNameSuggestionsHandler,
		TrafficFlowSegmentsDao trafficFlowSegmentsDao, WayTrafficFlowDao trafficFlowDao,
		FutureTask<FeatureDictionary> featureDictionaryFuture)
	{
		QuestType[] questTypesOrderedByImportance = {
			new ShowFixme(o),
			new AddFerryAccessPedestrian(o),
			new AddFerryAccessMotorVehicle(o),
			new AddPlaceName(o, featureDictionaryFuture),
			osmNoteQuestType,
			new AddOneway(o, trafficFlowSegmentsDao, trafficFlowDao),
			new AddWayLit(o), //frequent enable/disable cycle (enable for night)
			new AddRoadSurface(o),
			new AddBikeParkingType(o),
			new AddShopType(o),
			new AddTracktype(o),
			new MarkCompletedHighwayConstruction(o),
			new MarkCompletedBuildingConstruction(o),
			new AddAlsoShopForInsurance(o),
			new AddBikeParkingCapacity(o),
			new AddBikeParkingCover(o),
			new AddReligionToPlaceOfWorship(o),
			new AddToiletsFee(o),
			new AddBabyChangingTable(o),
			new AddFireHydrantType(o),
			new AddParkingAccess(o),
			new AddParkingFee(o),
			new AddParkingType(o),
			new AddBusStopName(o),
			new AddToiletAvailability(o),
			new AddPathSurface(o),
			new AddCyclewayPartSurface(o),
			new MultidesignatedFootwayToPath(o), //my own validator quest
			new AddCyclewaySegregation(o),
			new AddPlaygroundAccess(o), //late as in many areas all needed access=private is already mapped
			new AddMaxHeight(o),
			new DetectHistoricRailwayTagging(o),
			new AddProhibitedForPedestrians(o),
			new AddForestLeafType(o), // used by OSM Carto
			new AddSidewalk(o),

			//boring
			new DeprecateFIXME(o),
			new AddOpeningHours(o),
			new AddBusStopShelter(o),
			new AddReligionToWaysideShrine(o),
			new AddMaxSpeed(o),
			new AddBuildingType(o),
			new AddInternetAccess(o),
			new AddCrossingType(o),
			new AddTactilePavingCrosswalk(o),
			new AddRecyclingType(o),
			new AddSport(o),
			new AddIsBuildingUnderground(o),
			new AddHousenumber(o),
			new AddRoadName(o, roadNameSuggestionsDao, putRoadNameSuggestionsHandler),
			new AddPowerPolesMaterial(o),
			new AddVegetarian(o),
			new AddVegan(o),
			new AddCarWashType(o),
			new AddBenchBackrest(o),
			new AddWheelChairAccessPublicTransport(o),
			new AddWheelChairAccessToilets(o),
			new AddWheelchairAccessBusiness(o),
			new AddMotorcycleParkingCapacity(o),
			new AddTrafficSignalsSound(o),
			new AddTrafficSignalsButton(o),
			new AddWheelchairAccessOutside(o),
			new AddMotorcycleParkingCover(o),
			new AddPostboxCollectionTimes(o),
			new AddTactilePavingBusStop(o),
			new AddBridgeStructure(o),
			new AddOrchardProduce(o),
			new AddRailwayCrossingBarrier(o),
			new AddCycleway(o),
		};

		return new QuestTypeRegistry(Arrays.asList(questTypesOrderedByImportance));
	}

	@Provides @Singleton public static OsmNoteQuestType osmNoteQuestType()
	{
		return new OsmNoteQuestType();
	}
}
