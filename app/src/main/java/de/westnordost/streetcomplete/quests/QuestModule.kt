package de.westnordost.streetcomplete.quests

import dagger.Module
import dagger.Provides
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.quests.accepts_cash.AddAcceptsCash
import de.westnordost.streetcomplete.quests.address.AddAddressStreet
import de.westnordost.streetcomplete.quests.address.AddHousenumber
import de.westnordost.streetcomplete.quests.baby_changing_table.AddBabyChangingTable
import de.westnordost.streetcomplete.quests.bench_backrest.AddBenchBackrest
import de.westnordost.streetcomplete.quests.bike_parking_capacity.AddBikeParkingCapacity
import de.westnordost.streetcomplete.quests.bike_parking_cover.AddBikeParkingCover
import de.westnordost.streetcomplete.quests.bike_parking_type.AddBikeParkingType
import de.westnordost.streetcomplete.quests.bikeway.AddCycleway
import de.westnordost.streetcomplete.quests.board_type.AddBoardType
import de.westnordost.streetcomplete.quests.bridge_structure.AddBridgeStructure
import de.westnordost.streetcomplete.quests.building_type.AddBuildingType
import de.westnordost.streetcomplete.quests.building_underground.AddIsBuildingUnderground
import de.westnordost.streetcomplete.quests.bus_stop_shelter.AddBusStopShelter
import de.westnordost.streetcomplete.quests.car_wash_type.AddCarWashType
import de.westnordost.streetcomplete.quests.construction.MarkCompletedBuildingConstruction
import de.westnordost.streetcomplete.quests.construction.MarkCompletedHighwayConstruction
import de.westnordost.streetcomplete.quests.crossing_type.AddCrossingType
import de.westnordost.streetcomplete.quests.diet_type.AddVegan
import de.westnordost.streetcomplete.quests.diet_type.AddVegetarian
import de.westnordost.streetcomplete.quests.fire_hydrant.AddFireHydrantType
import de.westnordost.streetcomplete.quests.foot.AddProhibitedForPedestrians
import de.westnordost.streetcomplete.quests.general_fee.AddGeneralFee
import de.westnordost.streetcomplete.quests.handrail.AddHandrail
import de.westnordost.streetcomplete.quests.internet_access.AddInternetAccess
import de.westnordost.streetcomplete.quests.leaf_detail.AddForestLeafType
import de.westnordost.streetcomplete.quests.localized_name.AddBusStopName
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.quests.max_height.AddMaxHeight
import de.westnordost.streetcomplete.quests.max_weight.AddMaxWeight
import de.westnordost.streetcomplete.quests.motorcycle_parking_capacity.AddMotorcycleParkingCapacity
import de.westnordost.streetcomplete.quests.motorcycle_parking_cover.AddMotorcycleParkingCover
import de.westnordost.streetcomplete.quests.oneway.AddOneway
import de.westnordost.streetcomplete.quests.oneway.data.TrafficFlowSegmentsApi
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowDao
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
import de.westnordost.streetcomplete.quests.recycling.AddRecyclingType
import de.westnordost.streetcomplete.quests.recycling_glass.DetermineRecyclingGlass
import de.westnordost.streetcomplete.quests.recycling_material.AddRecyclingContainerMaterials
import de.westnordost.streetcomplete.quests.religion.AddReligionToPlaceOfWorship
import de.westnordost.streetcomplete.quests.religion.AddReligionToWaysideShrine
import de.westnordost.streetcomplete.quests.roof_shape.AddRoofShape
import de.westnordost.streetcomplete.quests.segregated.AddCyclewaySegregation
import de.westnordost.streetcomplete.quests.self_service.AddSelfServiceLaundry
import de.westnordost.streetcomplete.quests.sidewalk.AddSidewalk
import de.westnordost.streetcomplete.quests.surface.*
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingCrosswalk
import de.westnordost.streetcomplete.quests.toilet_availability.AddToiletAvailability
import de.westnordost.streetcomplete.quests.toilets_fee.AddToiletsFee
import de.westnordost.streetcomplete.quests.tourism_information.AddInformationToTourism
import de.westnordost.streetcomplete.quests.tracktype.AddTracktype
import de.westnordost.streetcomplete.quests.construction.MarkCompletedConstructionMinorOrGeneric
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessMotorVehicle
import de.westnordost.streetcomplete.quests.ferry.AddFerryAccessPedestrian
import de.westnordost.streetcomplete.quests.fixme_show.ShowAddressInterpolation
import de.westnordost.streetcomplete.quests.traffic_signals_button.AddTrafficSignalsButton
import de.westnordost.streetcomplete.quests.way_lit.AddWayLit
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessPublicTransport
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessToilets
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessBusiness
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessOutside
import de.westnordost.streetcomplete.quests.fixme_show.ShowFixme
import de.westnordost.streetcomplete.quests.footway.AddFootway
import de.westnordost.streetcomplete.quests.localized_name.AddRoadName
import de.westnordost.streetcomplete.quests.max_speed.AddMaxSpeed
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours
import de.westnordost.streetcomplete.quests.shop_type.AddShopType
import de.westnordost.streetcomplete.quests.sport.AddSport
import de.westnordost.streetcomplete.quests.surface.*
import de.westnordost.streetcomplete.quests.traffic_signals_sound.AddTrafficSignalsSound
import de.westnordost.streetcomplete.quests.validator.*
import de.westnordost.streetcomplete.quests.wheelchair_access.AddWheelchairAccessToiletsPart
import java.util.concurrent.FutureTask
import de.westnordost.streetcomplete.quests.wheelchair_access.*
import javax.inject.Singleton


@Module
object QuestModule
{
    @Provides
    @Singleton
    fun questTypeRegistry(
            osmNoteQuestType: OsmNoteQuestType, o: OverpassMapDataAndGeometryApi,
            roadNameSuggestionsDao: RoadNameSuggestionsDao,
            trafficFlowSegmentsApi: TrafficFlowSegmentsApi, trafficFlowDao: WayTrafficFlowDao,
            featureDictionaryFuture: FutureTask<FeatureDictionary>
    ): QuestTypeRegistry = QuestTypeRegistry(listOf(
            //modified--
            ShowFixme(o), // my quest
            ShowAddressInterpolation(o), // my quest
            AddWayLit(o), //frequent enable/disable cycle (enable for night) - moved
            //--modified

            //kept--
            // ↓ 1. notes
            osmNoteQuestType,

            // ↓ 2. important data that is used by many data consumers
            AddRoadSurface(o),
            AddRoadName(o, roadNameSuggestionsDao),
            AddPlaceName(o, featureDictionaryFuture),
            AddOneway(o, trafficFlowSegmentsApi, trafficFlowDao),
            AddBusStopName(o),
            AddIsBuildingUnderground(o), //to avoid asking AddHousenumber and other for underground buildings
            AddHousenumber(o),
            AddAddressStreet(o, roadNameSuggestionsDao),
            MarkCompletedHighwayConstruction(o),
            AddReligionToPlaceOfWorship(o), // icons on maps are different - OSM Carto, mapy.cz, OsmAnd, Sputnik etc
            AddParkingAccess(o), //OSM Carto, mapy.cz, OSMand, Sputnik etc
            //--kept

            //modified--
            AddShopType(o), // my hackish quest
            AddAlsoShopForInsurance(o), // my hackish quest
            MultidesignatedFootwayToPath(o), //my own validator quest
            DetectHistoricRailwayTagging(o), //my own validator quest
            FixBogusGallery(o), //my own validator quest
            //--modified

            // ↓ 3. useful data that is used by some data consumers
            AddRecyclingType(o),
            AddRecyclingContainerMaterials(o),
            AddSport(o),
            //AddMaxSpeed(o), //moved to boring
            AddMaxHeight(o),
            //AddRailwayCrossingBarrier(o), //moved to boring
            //AddPostboxCollectionTimes(o), //moved to boring
            //AddOpeningHours(o, featureDictionaryFuture), //moved to boring
            AddBikeParkingCapacity(o), // cycle map layer on osm.org
            //AddOrchardProduce(o), //moved to boring
            AddBuildingType(o),
            //AddCycleway(o), //moved to boring
            AddSidewalk(o), // SLOW QUERY
            AddProhibitedForPedestrians(o), // uses info from AddSidewalk quest, should be after it
            AddCrossingType(o),
            //AddBuildingLevels(o), removed as waste of time
            AddBusStopShelter(o), // at least OsmAnd
            //AddVegetarian(o), //moved to boring
            //AddVegan(o), //moved to boring
            //AddInternetAccess(o), //moved to boring
            AddParkingFee(o),
            //AddMotorcycleParkingCapacity(o),  //moved to boring (as waste of overpass query)
            AddPathSurface(o),
            AddTracktype(o),
            AddMaxWeight(o),
            AddForestLeafType(o), // used by OSM Carto
            AddBikeParkingType(o), // used by OsmAnd
            //AddWheelchairAccessToilets(o),  //moved to boring
            AddPlaygroundAccess(o), //late as in many areas all needed access=private is already mapped
            //AddWheelchairAccessBusiness(o), //moved to boring
            AddToiletAvailability(o), //OSM Carto, shown in OsmAnd descriptions
            AddFerryAccessPedestrian(o),
            AddFerryAccessMotorVehicle(o),
            //AddAcceptsCash(o), forcefully disabled as Sweden only

            // ↓ 4. definitely shown as errors in QA tools

            // ↓ 5. may be shown as missing in QA tools
            DetermineRecyclingGlass(o), // because most recycling:glass=yes is a tagging mistake

            // ↓ 6. may be shown as possibly missing in QA tools

            // ↓ 7. data useful for only a specific use case
            AddToiletsFee(o), // used by OsmAnd in the object description
            AddBabyChangingTable(o), // used by OsmAnd in the object description
            AddBikeParkingCover(o), // used by OsmAnd in the object description
            AddTactilePavingCrosswalk(o), // Paving can be completed while waiting to cross
            //AddTrafficSignalsSound(o), // moved to boring
            //AddRoofShape(o), removed as boring and tricky to get right
            //AddWheelchairAccessPublicTransport(o), // moved to boring
            //AddWheelchairAccessOutside(o), // moved to boring
            //AddTactilePavingBusStop(o), // moved to boring
            //AddBridgeStructure(o), // moved to boring
            AddReligionToWaysideShrine(o),
            AddCyclewaySegregation(o),
            MarkCompletedBuildingConstruction(o),
            AddGeneralFee(o),
            AddSelfServiceLaundry(o),
            AddHandrail(o), // for accessibility of pedestrian routing
            AddInformationToTourism(o),

            // ↓ 8. defined in the wiki, but not really used by anyone yet. Just collected for
            //      the sake of mapping it in case it makes sense later
            AddCyclewayPartSurface(o),
            AddFootwayPartSurface(o),
            DetailRoadSurface(o),
            MarkCompletedConstructionMinorOrGeneric(o),
            //AddMotorcycleParkingCover(o), //moved to boring
            AddFireHydrantType(o),
            AddParkingType(o),
            AddPostboxRef(o),
            AddWheelchairAccessToiletsPart(o),
            //AddPowerPolesMaterial(o), disabled as waste of time and is encouraging me to spend time on mapping power networks
            AddCarWashType(o),
            //AddBenchBackrest(o), disabled as waste of time
            //AddTrafficSignalsButton(o) //moved to boring
            AddBoardType(o),

            // boring/lame/etc
            AddFootway(o), // my own hackish quest
            DeprecateFIXME(o), // my own validator quest
            AddMaxSpeed(o), //moved to boring
            AddRailwayCrossingBarrier(o), //moved to boring
            AddPostboxCollectionTimes(o), //moved to boring
            AddOpeningHours(o, featureDictionaryFuture), //moved to boring
            AddOrchardProduce(o), //moved to boring
            AddCycleway(o), //moved to boring
            AddVegetarian(o), //moved to boring
            AddVegan(o), //moved to boring
            AddInternetAccess(o), //moved to boring
            AddMotorcycleParkingCapacity(o),  //moved to boring (as waste of overpass query)
            AddWheelchairAccessToilets(o),  //moved to boring
            AddWheelchairAccessBusiness(o), //moved to boring
            AddTrafficSignalsSound(o), // moved to boring
            AddWheelchairAccessPublicTransport(o), // moved to boring
            AddWheelchairAccessOutside(o), // moved to boring
            AddTactilePavingBusStop(o), // moved to boring
            AddBridgeStructure(o), // moved to boring
            AddMotorcycleParkingCover(o), //moved to boring
            AddTrafficSignalsButton(o) //moved to boring
    ) as List<QuestType<*>>)

    @Provides @Singleton fun osmNoteQuestType(): OsmNoteQuestType = OsmNoteQuestType()
}
