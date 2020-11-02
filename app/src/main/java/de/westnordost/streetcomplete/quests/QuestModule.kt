package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.quests.barrier_specify.SpecifyBarrier
import de.westnordost.streetcomplete.quests.construction.MarkCompletedConstructionMinorOrGeneric
import de.westnordost.streetcomplete.quests.drinking_water.AddDrinkingWaterStatus
import de.westnordost.streetcomplete.quests.fixme_show.ShowAddressInterpolation
import de.westnordost.streetcomplete.quests.fixme_show.ShowFixme
import de.westnordost.streetcomplete.quests.shop_type.AddShopType
import de.westnordost.streetcomplete.quests.validator.*

import dagger.Module
import dagger.Provides
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.quests.accepts_cash.AddAcceptsCash
import de.westnordost.streetcomplete.quests.address.AddAddressStreet
import de.westnordost.streetcomplete.quests.housenumber.AddHousenumber
import de.westnordost.streetcomplete.quests.baby_changing_table.AddBabyChangingTable
import de.westnordost.streetcomplete.quests.bench_backrest.AddBenchBackrest
import de.westnordost.streetcomplete.quests.bike_parking_capacity.AddBikeParkingCapacity
import de.westnordost.streetcomplete.quests.bike_parking_cover.AddBikeParkingCover
import de.westnordost.streetcomplete.quests.bike_parking_type.AddBikeParkingType
import de.westnordost.streetcomplete.quests.bikeway.AddCycleway
import de.westnordost.streetcomplete.quests.board_type.AddBoardType
import de.westnordost.streetcomplete.quests.bridge_structure.AddBridgeStructure
import de.westnordost.streetcomplete.quests.building_levels.AddBuildingLevels
import de.westnordost.streetcomplete.quests.building_type.AddBuildingType
import de.westnordost.streetcomplete.quests.building_underground.AddIsBuildingUnderground
import de.westnordost.streetcomplete.quests.bus_stop_bench.AddBenchStatusOnBusStop
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter
import de.westnordost.streetcomplete.quests.car_wash_type.AddCarWashType
import de.westnordost.streetcomplete.quests.construction.MarkCompletedBuildingConstruction
import de.westnordost.streetcomplete.quests.construction.MarkCompletedHighwayConstruction
import de.westnordost.streetcomplete.quests.crossing_type.AddCrossingType
import de.westnordost.streetcomplete.quests.crossing_island.AddCrossingIsland
import de.westnordost.streetcomplete.quests.defibrillator.AddIsDefibrillatorIndoor
import de.westnordost.streetcomplete.quests.diet_type.AddVegan
import de.westnordost.streetcomplete.quests.diet_type.AddVegetarian
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessMotorVehicle
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessPedestrian
import de.westnordost.streetcomplete.quests.fire_hydrant.AddFireHydrantType
import de.westnordost.streetcomplete.quests.foot.AddProhibitedForPedestrians
import de.westnordost.streetcomplete.quests.general_fee.AddGeneralFee
import de.westnordost.streetcomplete.quests.handrail.AddHandrail
import de.westnordost.streetcomplete.quests.step_count.AddStepCount
import de.westnordost.streetcomplete.quests.internet_access.AddInternetAccess
import de.westnordost.streetcomplete.quests.leaf_detail.AddForestLeafType
import de.westnordost.streetcomplete.quests.bus_stop_name.AddBusStopName
import de.westnordost.streetcomplete.quests.bus_stop_ref.AddBusStopRef
import de.westnordost.streetcomplete.quests.road_name.AddRoadName
import de.westnordost.streetcomplete.quests.road_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.quests.max_height.AddMaxHeight
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed
import de.westnordost.streetcomplete.quests.max_weight.AddMaxWeight
import de.westnordost.streetcomplete.quests.motorcycle_parking_capacity.AddMotorcycleParkingCapacity
import de.westnordost.streetcomplete.quests.motorcycle_parking_cover.AddMotorcycleParkingCover
import de.westnordost.streetcomplete.quests.oneway.AddOneway
import de.westnordost.streetcomplete.quests.oneway_suspects.AddSuspectedOneway
import de.westnordost.streetcomplete.quests.oneway_suspects.data.TrafficFlowSegmentsApi
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowDao
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours
import de.westnordost.streetcomplete.quests.atm_operator.AddAtmOperator
import de.westnordost.streetcomplete.quests.charging_station_capacity.AddChargingStationCapacity
import de.westnordost.streetcomplete.quests.charging_station_operator.AddChargingStationOperator
import de.westnordost.streetcomplete.quests.clothing_bin_operator.AddClothingBinOperator
import de.westnordost.streetcomplete.quests.orchard_produce.AddOrchardProduce
import de.westnordost.streetcomplete.quests.parking_access.AddParkingAccess
import de.westnordost.streetcomplete.quests.parking_fee.AddParkingFee
import de.westnordost.streetcomplete.quests.parking_type.AddParkingType
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.quests.playground_access.AddPlaygroundAccess
import de.westnordost.streetcomplete.quests.postbox_collection_times.AddPostboxCollectionTimes
import de.westnordost.streetcomplete.quests.postbox_ref.AddPostboxRef
import de.westnordost.streetcomplete.quests.powerpoles_material.AddPowerPolesMaterial
import de.westnordost.streetcomplete.quests.railway_crossing.AddRailwayCrossingBarrier
import de.westnordost.streetcomplete.quests.summit_register.AddSummitRegister
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType
import de.westnordost.streetcomplete.quests.recycling_glass.DetermineRecyclingGlass
import de.westnordost.streetcomplete.quests.recycling_material.AddRecyclingContainerMaterials
import de.westnordost.streetcomplete.quests.religion.AddReligionToPlaceOfWorship
import de.westnordost.streetcomplete.quests.religion.AddReligionToWaysideShrine
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShape
import de.westnordost.streetcomplete.quests.segregated.AddCyclewaySegregation
import de.westnordost.streetcomplete.quests.self_service.AddSelfServiceLaundry
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.sport.AddSport
import de.westnordost.streetcomplete.quests.steps_incline.AddStepsIncline
import de.westnordost.streetcomplete.quests.steps_ramp.AddStepsRamp
import de.westnordost.streetcomplete.quests.surface.*
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee
import de.westnordost.streetcomplete.quests.tourism_information.AddInformationToTourism
import de.westnordost.streetcomplete.quests.tracktype.AddTracktype
import de.westnordost.streetcomplete.quests.traffic_signals_button.AddTrafficSignalsButton
import de.westnordost.streetcomplete.quests.traffic_signals_vibrate.AddTrafficSignalsVibration
import de.westnordost.streetcomplete.quests.traffic_signals_sound.AddTrafficSignalsSound
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit
import de.westnordost.streetcomplete.quests.wheelchair_access.*
import java.util.concurrent.FutureTask
import javax.inject.Singleton


@Module object QuestModule
{
    @Provides @Singleton fun questTypeRegistry(
        osmNoteQuestType: OsmNoteQuestType,
        roadNameSuggestionsDao: RoadNameSuggestionsDao,
        trafficFlowSegmentsApi: TrafficFlowSegmentsApi, trafficFlowDao: WayTrafficFlowDao,
        featureDictionaryFuture: FutureTask<FeatureDictionary>
    ): QuestTypeRegistry = QuestTypeRegistry(listOf(
        //modified--
        ShowFixme(), // my quest
        ShowAddressInterpolation(), // my quest
        AddWayLit(), //frequent enable/disable cycle (enable for night) - moved
        //--modified

        //kept--
        // ↓ 1. notes
        osmNoteQuestType,

        // ↓ 2. important data that is used by many data consumers
        AddRoadSurface(),
        AddRoadName(roadNameSuggestionsDao),
        AddPlaceName(featureDictionaryFuture),
        AddOneway(),
        AddSuspectedOneway(trafficFlowSegmentsApi, trafficFlowDao),
        AddBusStopName(),
        AddIsBuildingUnderground(), //to avoid asking AddHousenumber and other for underground buildings
        AddHousenumber(),
        AddAddressStreet(roadNameSuggestionsDao),
        MarkCompletedHighwayConstruction(),
        AddReligionToPlaceOfWorship(), // icons on maps are different - OSM Carto, mapy.cz, OsmAnd, Sputnik etc
        AddParkingAccess(), //OSM Carto, mapy.cz, OSMand, Sputnik etc
        //--kept

        //modified--
        AddShopType(), // my hackish quest
        AddAlsoShopForInsurance(), // my hackish quest
        MultidesignatedFootwayToPath(), //my own validator quest
        DetectHistoricRailwayTagging(), //my own validator quest
        FixBogusGallery(), //my own validator quest
        //--modified

        // ↓ 3. useful data that is used by some data consumers
        AddStepsRamp(),
        AddRecyclingType(),
        AddRecyclingContainerMaterials(),
        AddSport(),
        //AddMaxSpeed(), //moved to boring
        AddMaxHeight(),
        //AddRailwayCrossingBarrier(), //moved to boring
        //AddPostboxCollectionTimes(), //moved to boring
        //AddOpeningHours(featureDictionaryFuture), //moved to boring
        AddBikeParkingCapacity(), // cycle map layer on osm.org
        //AddOrchardProduce(), //moved to boring
        AddBuildingType(),
        //AddCycleway(), //moved to boring
        AddSidewalk(), // SLOW QUERY
        AddProhibitedForPedestrians(), // uses info from AddSidewalk quest, should be after it
        AddCrossingType(),
        //AddBuildingLevels(), removed as waste of time
        AddBusStopShelter(), // at least OsmAnd
        //AddVegetarian(), //moved to boring
        //AddVegan(), //moved to boring
        //AddInternetAccess(), //moved to boring
        AddParkingFee(),
        //AddMotorcycleParkingCapacity(),  //moved to boring (as waste of overpass query)
        AddPathSurface(),
        AddTracktype(),
        AddMaxWeight(),
        AddForestLeafType(), // used by OSM Carto
        AddBikeParkingType(), // used by OsmAnd
        //AddWheelchairAccessToilets(),  //moved to boring
        AddPlaygroundAccess(), //late as in many areas all needed access=private is already mapped
        //AddWheelchairAccessBusiness(), //moved to boring
        AddToiletAvailability(), //OSM Carto, shown in OsmAnd descriptions
        AddFerryAccessPedestrian(),
        AddFerryAccessMotorVehicle(),
        //AddAcceptsCash(), forcefully disabled as Sweden only
        WatUndrinkableDrinkable(),
        AddDrinkingWaterStatus(),
        SpecifyBarrier(),

        // ↓ 5. may be shown as missing in QA tools
        DetermineRecyclingGlass(), // because most recycling:glass=yes is a tagging mistake

        // ↓ 6. may be shown as possibly missing in QA tools

        // ↓ 7. data useful for only a specific use case
        AddToiletsFee(), // used by OsmAnd in the object description
        AddBabyChangingTable(), // used by OsmAnd in the object description
        AddBikeParkingCover(), // used by OsmAnd in the object description
        //AddTrafficSignalsSound(), // moved to boring
        //AddRoofShape(), removed as boring and tricky to get right
        //AddWheelchairAccessPublicTransport(), // moved to boring
        //AddWheelchairAccessOutside(), // moved to boring
        //AddTactilePavingBusStop(), // moved to boring
        //AddBridgeStructure(), // moved to boring
        AddReligionToWaysideShrine(),
        AddCyclewaySegregation(),
        MarkCompletedBuildingConstruction(),
        AddGeneralFee(),
        AddSelfServiceLaundry(),
        AddHandrail(), // for accessibility of pedestrian routing
        AddCrossingIsland(),
        AddAtmOperator(),
        AddChargingStationCapacity(),
        AddChargingStationOperator(),
        AddClothingBinOperator(),

        // ↓ 8. defined in the wiki, but not really used by anyone yet. Just collected for
        //      the sake of mapping it in case it makes sense later
        AddCyclewayPartSurface(),
        AddFootwayPartSurface(),
        MarkCompletedConstructionMinorOrGeneric(),
        //AddMotorcycleParkingCover(), //moved to boring
        AddFireHydrantType(),
        AddParkingType(),
        AddPostboxRef(),
        AddWheelchairAccessToiletsPart(),
        //AddPowerPolesMaterial(), disabled as waste of time and is encouraging me to spend time on mapping power networks
        AddCarWashType(),
        //AddBenchBackrest(), disabled as waste of time
        //AddTrafficSignalsButton() //moved to boring
        AddBoardType(),
        AddIsDefibrillatorIndoor(),
        AddInformationToTourism(),
        AddTrafficSignalsVibration(),

        // boring/lame/etc

        DeprecateFIXME(), // my own validator quest
        AddMaxSpeed(), //moved to boring
        AddRailwayCrossingBarrier(), //moved to boring
        AddPostboxCollectionTimes(), //moved to boring
        AddOpeningHours(featureDictionaryFuture), //moved to boring
        AddOrchardProduce(), //moved to boring
        AddCycleway(), //moved to boring
        AddVegetarian(), //moved to boring
        AddVegan(), //moved to boring
        AddInternetAccess(), //moved to boring
        AddMotorcycleParkingCapacity(),  //moved to boring (as waste of overpass query)
        AddWheelchairAccessToilets(),  //moved to boring
        AddWheelchairAccessBusiness(featureDictionaryFuture), //moved to boring
        AddTrafficSignalsSound(), // moved to boring
        AddWheelchairAccessPublicTransport(), // moved to boring
        AddWheelchairAccessOutside(), // moved to boring
        AddTactilePavingCrosswalk(), // moved to boring (is disabled by default
        AddTactilePavingBusStop(), // moved to boring
        AddBridgeStructure(), // moved to boring
        AddMotorcycleParkingCover(), //moved to boring
        AddTrafficSignalsButton(), //moved to boring
        AddSummitRegister(), // only in some countries
        AddBenchStatusOnBusStop(),
        AddStepsIncline(), // can be gathered while walking perpendicular to the way e.g. the other side of the road or when running/cycling past
        AddStepCount(), // can only be gathered when walking along this way, also needs the most effort and least useful
        AddBusStopRef() // not in Poland
    ))

    @Provides @Singleton fun osmNoteQuestType(): OsmNoteQuestType = OsmNoteQuestType()
}
