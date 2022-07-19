import kotlinx.ast.common.AstSource
import kotlinx.ast.common.ast.Ast
import kotlinx.ast.common.ast.AstNode
import kotlinx.ast.common.ast.AstWithAstInfo
import kotlinx.ast.common.ast.DefaultAstNode
import kotlinx.ast.common.ast.DefaultAstTerminal
import kotlinx.ast.common.ast.astInfoOrNull
import kotlinx.ast.common.klass.KlassDeclaration
import kotlinx.ast.common.klass.KlassIdentifier
import kotlinx.ast.common.klass.KlassString
import kotlinx.ast.common.klass.StringComponentRaw
import kotlinx.ast.grammar.kotlin.common.KotlinGrammarParserType
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.system.exitProcess

/*
This program is parsing files of StreetComplete project
to detect what kind of changes this editor will make to OpenStreetMap database

It relies on editing functionality being defined in applyAnswerTo function
and always involving modification of tags variable

Alternative - maintaining such list manually is too time-consuming and too boring.
There was an attempt to do this but it failed.
*/

// git clone https://github.com/matkoniecz/StreetComplete.git
// git checkout taginfo
// ./gradlew updateTaginfoListing

// https://codereview.stackexchange.com/
// https://stackoverflow.com/questions/58100739/how-to-generate-a-json-object-in-kotlin
// https://wiki.openstreetmap.org/wiki/Taginfo/Projects
// https://github.com/goldfndr/StreetCompleteJSON/blob/master/taginfo.json
// https://github.com/taginfo/taginfo-projects/blob/master/project_list.txt

// following pages were useful to jumpstart coding:
// https://github.com/kotlinx/ast/blob/a96e681f906f1ec1ab4db8a81ffbbcbbe529317f/grammar-kotlin-parser-test/src/jvmMain/kotlin/kotlinx/ast/grammar/kotlin/test/AbstractKotlinGrammarParserTestDataTest.kt
// https://github.com/2bad2furious/kotlinx-ast-demo
// https://github.com/peternewman/StreetComplete/blob/a388043854bf04545dfbc0beb7decda5208a750e/.github/generate-quest-metadata.main.kts

open class UpdateTaginfoListingTask : DefaultTask() {
    companion object {
        const val NAME_OF_FUNCTION_EDITING_TAGS = "applyAnswerTo"
        const val KOTLIN_IMPORT_ROOT_WITH_SLASH_ENDING = "app/src/main/java/"
        const val QUEST_ROOT_WITH_SLASH_ENDING = "app/src/main/java/de/westnordost/streetcomplete/quests/"
        const val SURVEY_MARK_KEY = "check_date" // TODO: is it possible to use directly SC constant?
        const val VIBRATING_BUTTON = "traffic_signals:vibration"
        private const val SOUND_SIGNALS = "traffic_signals:sound"
        val EXPECTED_TAG_PER_QUEST = mapOf(
            "accepts_cards/AddAcceptsCards.kt" to setOf(Tag("payment:debit_cards", "yes"), Tag("payment:debit_cards", "no"), Tag("payment:credit_cards", "yes"), Tag("payment:credit_cards", "no")),
            "accepts_cash/AddAcceptsCash.kt" to setOf(Tag("payment:cash", "yes"), Tag("payment:cash", "no")),
            "address/AddHousenumber.kt" to setOf(Tag("addr:conscriptionnumber", null), Tag("addr:streetnumber", null), Tag("addr:housenumber", null), Tag("addr:block_number", null), Tag("addr:housename", null), Tag("nohousenumber", "yes"), Tag("building", "yes")),
            "atm_operator/AddAtmOperator.kt" to setOf(Tag("operator", null)),
            "air_conditioning/AddAirConditioning.kt" to setOf(Tag("air_conditioning", "yes"), Tag("air_conditioning", "no")),
            "air_pump/AddBicyclePump.kt" to setOf(Tag("check_date:service:bicycle:pump", null), Tag("service:bicycle:pump", "yes"), Tag("service:bicycle:pump", "no")),
            "air_pump/AddAirCompressor.kt" to setOf(Tag("check_date:compressed_air", null), Tag("compressed_air", "yes"), Tag("compressed_air", "no")),
            "baby_changing_table/AddBabyChangingTable.kt" to setOf(Tag("changing_table", "yes"), Tag("changing_table", "no")),
            "barrier_bicycle_barrier_type/AddBicycleBarrierType.kt" to setOf(Tag("cycle_barrier", "single"), Tag("cycle_barrier", "double"), Tag("cycle_barrier", "triple"), Tag("cycle_barrier", "diagonal"), Tag("cycle_barrier", "tilted"), Tag("barrier", "yes")),
            "bench_backrest/AddBenchBackrest.kt" to setOf(Tag("leisure", "picnic_table"), Tag("backrest", "yes"), Tag("backrest", "no")),
            "bike_parking_capacity/AddBikeParkingCapacity.kt" to setOf(Tag("check_date:capacity", null), Tag("capacity", null)),
            "bike_parking_cover/AddBikeParkingCover.kt" to setOf(Tag("covered", "yes"), Tag("covered", "no")),
            "bike_parking_type/AddBikeParkingType.kt" to setOf(Tag("bicycle_parking", "stands"), Tag("bicycle_parking", "wall_loops"), Tag("bicycle_parking", "shed"), Tag("bicycle_parking", "lockers"), Tag("bicycle_parking", "building"), Tag("bicycle_parking", "handlebar_holder")),
            "bike_rental_type/AddBikeRentalType.kt" to setOf(Tag("bicycle_rental", "docking_station"), Tag("bicycle_rental", "dropoff_point"), Tag("bicycle_rental", "shop"), Tag("shop", "rental"), Tag("shop", "bicycle"), Tag("service:bicycle:rental", "yes")),
            "bike_rental_capacity/AddBikeRentalCapacity.kt" to setOf(Tag("check_date:capacity", null), Tag("capacity", null)),
            "bike_shop/AddBikeRepairAvailability.kt" to setOf(Tag("check_date:service:bicycle:repair", null), Tag("service:bicycle:repair", "yes"), Tag("service:bicycle:repair", "no")),
            "bike_shop/AddSecondHandBicycleAvailability.kt" to setOf(Tag("service:bicycle:second_hand", "no"), Tag("service:bicycle:second_hand", "yes"), Tag("service:bicycle:second_hand", "only"), Tag("check_date:service:bicycle:retail", null), Tag("service:bicycle:retail", "no"), Tag("service:bicycle:retail", "yes")),
            "board_type/AddBoardType.kt" to setOf(Tag("information", "map"), Tag("board_type", "history"), Tag("board_type", "geology"), Tag("board_type", "plants"), Tag("board_type", "wildlife"), Tag("board_type", "nature"), Tag("board_type", "public_transport"), Tag("board_type", "notice"), Tag("board_type", "map"), Tag("board_type", "sport")),
            "bollard_type/AddBollardType.kt" to setOf(Tag("bollard", "rising"), Tag("bollard", "removable"), Tag("bollard", "foldable"), Tag("bollard", "flexible"), Tag("bollard", "fixed"), Tag("barrier", "yes")),
            "bridge_structure/AddBridgeStructure.kt" to setOf(Tag("bridge:structure", "beam"), Tag("bridge:structure", "suspension"), Tag("bridge:structure", "arch"), Tag("bridge:structure", "truss"), Tag("bridge:structure", "cable-stayed"), Tag("bridge:structure", "humpback"), Tag("bridge:structure", "simple-suspension"), Tag("bridge:structure", "floating")),
            "building_entrance/AddEntrance.kt" to setOf(Tag("noexit", "yes"), Tag("entrance", "main"), Tag("entrance", "staircase"), Tag("entrance", "service"), Tag("entrance", "emergency"), Tag("entrance", "exit"), Tag("entrance", "shop"), Tag("entrance", "yes")),
            "building_entrance_reference/AddEntranceReference.kt" to setOf(Tag("addr:flats", null), Tag("ref", null), Tag("ref:signed", "no")),
            "building_levels/AddBuildingLevels.kt" to setOf(Tag("building:levels", null), Tag("roof:levels", null)),
            "building_underground/AddIsBuildingUnderground.kt" to setOf(Tag("location", "underground"), Tag("location", "surface")),
            "bus_stop_bench/AddBenchStatusOnBusStop.kt" to setOf(Tag("check_date:bench", null), Tag("bench", "yes"), Tag("bench", "no")),
            "bus_stop_bin/AddBinStatusOnBusStop.kt" to setOf(Tag("check_date:bin", null), Tag("bin", "yes"), Tag("bin", "no")),
            "bus_stop_lit/AddBusStopLit.kt" to setOf(Tag("check_date:lit", null), Tag("lit", "yes"), Tag("lit", "no")),
            "bus_stop_ref/AddBusStopRef.kt" to setOf(Tag("ref:signed", "no"), Tag("ref", null)),
            "bus_stop_shelter/AddBusStopShelter.kt" to setOf(Tag("covered", "yes"), Tag("check_date:shelter", null), Tag("shelter", "yes"), Tag("shelter", "no")),
            "camera_type/AddCameraType.kt" to setOf(Tag("camera:type", "dome"), Tag("camera:type", "fixed"), Tag("camera:type", "panning")),
            "car_wash_type/AddCarWashType.kt" to setOf(Tag("automated", "yes"), Tag("automated", "no"), Tag("self_service", "only"), Tag("self_service", "yes"), Tag("self_service", "no")),
            "charging_station_capacity/AddChargingStationCapacity.kt" to setOf(Tag("check_date:capacity", null), Tag("capacity", null)),
            "charging_station_operator/AddChargingStationOperator.kt" to setOf(Tag("operator", null)),
            "clothing_bin_operator/AddClothingBinOperator.kt" to setOf(Tag("operator", null)),
            "construction/MarkCompletedBuildingConstruction.kt" to setOf(Tag("opening_date", null), Tag("building", null), Tag("check_date", null)),
            "construction/MarkCompletedHighwayConstruction.kt" to setOf(Tag("opening_date", null), Tag("highway", null), Tag("check_date", null)),
            "crossing/AddCrossing.kt" to setOf(Tag("highway", "crossing"), Tag("check_date:kerb", null), Tag("kerb", "raised"), Tag("kerb", "lowered"), Tag("kerb", "flush"), Tag("kerb", "no")),
            "crossing_island/AddCrossingIsland.kt" to setOf(Tag("crossing:island", "yes"), Tag("crossing:island", "no")),
            "crossing_type/AddCrossingType.kt" to setOf(Tag("crossing:island", "yes"), Tag("check_date:crossing", null), Tag("crossing", "traffic_signals"), Tag("crossing", "marked"), Tag("crossing", "unmarked")),
            "defibrillator/AddIsDefibrillatorIndoor.kt" to setOf(Tag("indoor", "yes"), Tag("indoor", "no")),
            "diet_type/AddVegan.kt" to setOf(Tag("food", "no"), Tag("check_date:diet:vegan", null), Tag("diet:vegan", "yes"), Tag("diet:vegan", "no"), Tag("diet:vegan", "only")),
            "diet_type/AddVegetarian.kt" to setOf(Tag("food", "no"), Tag("check_date:diet:vegetarian", null), Tag("diet:vegetarian", "yes"), Tag("diet:vegetarian", "no"), Tag("diet:vegetarian", "only")),
            "diet_type/AddHalal.kt" to setOf(Tag("food", "no"), Tag("check_date:diet:halal", null), Tag("diet:halal", "yes"), Tag("diet:halal", "no"), Tag("diet:halal", "only")),
            "diet_type/AddKosher.kt" to setOf(Tag("food", "no"), Tag("check_date:diet:kosher", null), Tag("diet:kosher", "yes"), Tag("diet:kosher", "no"), Tag("diet:kosher", "only")),
            "existence/CheckExistence.kt" to setOf(Tag("check_date", null)),
            "ferry/AddFerryAccessMotorVehicle.kt" to setOf(Tag("motor_vehicle", "yes"), Tag("motor_vehicle", "no")),
            "ferry/AddFerryAccessPedestrian.kt" to setOf(Tag("foot", "yes"), Tag("foot", "no")),
            "fire_hydrant_diameter/AddFireHydrantDiameter.kt" to setOf(Tag("fire_hydrant:diameter", null), Tag("fire_hydrant:diameter:signed", "no")),
            "fire_hydrant_position/AddFireHydrantPosition.kt" to setOf(Tag("fire_hydrant:position", "green"), Tag("fire_hydrant:position", "lane"), Tag("fire_hydrant:position", "sidewalk"), Tag("fire_hydrant:position", "parking_lot")),
            "fire_hydrant/AddFireHydrantType.kt" to setOf(Tag("fire_hydrant:type", "pillar"), Tag("fire_hydrant:type", "underground"), Tag("fire_hydrant:type", "wall"), Tag("fire_hydrant:type", "pond")),
            "foot/AddProhibitedForPedestrians.kt" to setOf(Tag("foot", "no"), Tag("foot", "yes"), Tag("sidewalk", "separate"), Tag("highway", "living_street")),
            "fuel_service/AddFuelSelfService.kt" to setOf(Tag("self_service", "yes"), Tag("self_service", "no")),
            "general_fee/AddGeneralFee.kt" to setOf(Tag("fee", "yes"), Tag("fee", "no")),
            "handrail/AddHandrail.kt" to setOf(Tag("check_date:handrail", null), Tag("handrail", "yes"), Tag("handrail", "no")),
            "internet_access/AddInternetAccess.kt" to setOf(Tag("check_date:internet_access", null), Tag("internet_access", "wlan"), Tag("internet_access", "no"), Tag("internet_access", "terminal"), Tag("internet_access", "wired")),
            "kerb_height/AddKerbHeight.kt" to setOf(Tag("barrier", "kerb"), Tag("check_date:kerb", null), Tag("kerb", "raised"), Tag("kerb", "lowered"), Tag("kerb", "flush"), Tag("kerb", "no")),
            "leaf_detail/AddForestLeafType.kt" to setOf(Tag("leaf_type", "needleleaved"), Tag("leaf_type", "broadleaved"), Tag("leaf_type", "mixed")),
            "level/AddLevel.kt" to setOf(Tag("level", null)),
            "lanes/AddLanes.kt" to setOf(Tag("lanes", null), Tag("lane_markings", "yes"), Tag("lane_markings", "no"), Tag("lanes:both_ways", "1"), Tag("turn:lanes:both_ways", "left"), Tag("lanes:forward", null), Tag("lanes:backward", null)),
            "max_height/AddMaxHeight.kt" to setOf(Tag("maxheight", null), Tag("maxheight", "default"), Tag("maxheight", "below_default")),
            "max_height/AddMaxPhysicalHeight.kt" to setOf(Tag("maxheight", null), Tag("maxheight:signed", "no"), Tag("source:maxheight", "ARCore")),
            "motorcycle_parking_capacity/AddMotorcycleParkingCapacity.kt" to setOf(Tag("check_date:capacity", null), Tag("capacity", null)),
            "motorcycle_parking_cover/AddMotorcycleParkingCover.kt" to setOf(Tag("covered", "yes"), Tag("covered", "no")),
            "oneway/AddOneway.kt" to setOf(Tag("oneway", "yes"), Tag("oneway", "-1"), Tag("oneway", "no")),
            "opening_hours/AddOpeningHours.kt" to setOf(Tag("opening_hours:signed", "no"), Tag("check_date:opening_hours", null), Tag("opening_hours", null)),
            "opening_hours_signed/CheckOpeningHoursSigned.kt" to setOf(Tag("opening_hours:signed", "no"), Tag("check_date:opening_hours", null)),
            "picnic_table_cover/AddPicnicTableCover.kt" to setOf(Tag("covered", "yes"), Tag("covered", "no")),
            "parking_access/AddParkingAccess.kt" to setOf(Tag("access", "yes"), Tag("access", "customers"), Tag("access", "private")),
            "parking_access/AddBikeParkingAccess.kt" to setOf(Tag("access", "yes"), Tag("access", "customers"), Tag("access", "private")),
            "parking_type/AddParkingType.kt" to setOf(Tag("parking", "surface"), Tag("parking", "street_side"), Tag("parking", "lane"), Tag("parking", "underground"), Tag("parking", "multi-storey")),
            "pitch_lit/AddPitchLit.kt" to setOf(Tag("check_date:lit", null), Tag("lit", "yes"), Tag("lit", "no")),
            "playground_access/AddPlaygroundAccess.kt" to setOf(Tag("access", "yes"), Tag("access", "customers"), Tag("access", "private")),
            "postbox_ref/AddPostboxRef.kt" to setOf(Tag("ref:signed", "no"), Tag("ref", null)),
            "postbox_collection_times/AddPostboxCollectionTimes.kt" to setOf(Tag("collection_times:signed", "no"), Tag("check_date:collection_times", null), Tag("collection_times", null)),
            "postbox_royal_cypher/AddPostboxRoyalCypher.kt" to setOf(Tag("royal_cypher", "VR"), Tag("royal_cypher", "EVIIR"), Tag("royal_cypher", "GR"), Tag("royal_cypher", "EVIIIR"), Tag("royal_cypher", "GVIR"), Tag("royal_cypher", "EIIR"), Tag("royal_cypher", "scottish_crown"), Tag("royal_cypher", "no")),
            "powerpoles_material/AddPowerPolesMaterial.kt" to setOf(Tag("material", "wood"), Tag("material", "steel"), Tag("material", "concrete")),
            "railway_crossing/AddRailwayCrossingBarrier.kt" to setOf(Tag("crossing:chicane", "yes"), Tag("check_date:crossing:barrier", null), Tag("crossing:barrier", "no"), Tag("crossing:barrier", "half"), Tag("crossing:barrier", "double_half"), Tag("crossing:barrier", "full"), Tag("crossing:barrier", "gate")),
            "recycling/AddRecyclingType.kt" to setOf(Tag("recycling_type", "centre"), Tag("recycling_type", "container"), Tag("location", "overground"), Tag("location", "underground")),
            "recycling_glass/DetermineRecyclingGlass.kt" to setOf(Tag("recycling:glass_bottles", "yes"), Tag("recycling:glass", "no")),
            "religion/AddReligionToPlaceOfWorship.kt" to setOf(Tag("religion", "christian"), Tag("religion", "muslim"), Tag("religion", "buddhist"), Tag("religion", "hindu"), Tag("religion", "jewish"), Tag("religion", "chinese_folk"), Tag("religion", "animist"), Tag("religion", "bahai"), Tag("religion", "sikh"), Tag("religion", "taoist"), Tag("religion", "jain"), Tag("religion", "shinto"), Tag("religion", "caodaism"), Tag("religion", "multifaith")),
            "religion/AddReligionToWaysideShrine.kt" to setOf(Tag("religion", "christian"), Tag("religion", "muslim"), Tag("religion", "buddhist"), Tag("religion", "hindu"), Tag("religion", "jewish"), Tag("religion", "chinese_folk"), Tag("religion", "animist"), Tag("religion", "bahai"), Tag("religion", "sikh"), Tag("religion", "taoist"), Tag("religion", "jain"), Tag("religion", "shinto"), Tag("religion", "caodaism"), Tag("religion", "multifaith")),
            "roof_shape/AddRoofShape.kt" to setOf(Tag("roof:shape", "gabled"), Tag("roof:shape", "hipped"), Tag("roof:shape", "flat"), Tag("roof:shape", "pyramidal"), Tag("roof:shape", "half-hipped"), Tag("roof:shape", "skillion"), Tag("roof:shape", "gambrel"), Tag("roof:shape", "round"), Tag("roof:shape", "double_saltbox"), Tag("roof:shape", "saltbox"), Tag("roof:shape", "mansard"), Tag("roof:shape", "dome"), Tag("roof:shape", "quadruple_saltbox"), Tag("roof:shape", "round_gabled"), Tag("roof:shape", "onion"), Tag("roof:shape", "cone"), Tag("roof:shape", "many")),
            "seating/AddSeating.kt" to setOf(Tag("outdoor_seating", "yes"), Tag("outdoor_seating", "no"), Tag("indoor_seating", "yes"), Tag("indoor_seating", "no")),
            "segregated/AddCyclewaySegregation.kt" to setOf(Tag("check_date:segregated", null), Tag("segregated", "yes"), Tag("segregated", "no")),
            "self_service/AddSelfServiceLaundry.kt" to setOf(Tag("self_service", "no"), Tag("laundry_service", "yes"), Tag("self_service", "yes"), Tag("laundry_service", "no")),
            "smoking/AddSmoking.kt" to setOf(Tag("check_date:smoking", null), Tag("smoking", "yes"), Tag("smoking", "outside"), Tag("smoking", "no"), Tag("smoking", "separated")),
            "step_count/AddStepCount.kt" to setOf(Tag("step_count", null)),
            "step_count/AddStepCountStile.kt" to setOf(Tag("step_count", null)),
            "steps_incline/AddStepsIncline.kt" to setOf(Tag("incline", "up"), Tag("incline", "down")),
            "summit/AddSummitCross.kt" to setOf(Tag("check_date:summit:cross", null), Tag("summit:cross", "yes"), Tag("summit:cross", "no")),
            "summit/AddSummitRegister.kt" to setOf(Tag("check_date:summit:register", null), Tag("summit:register", "yes"), Tag("summit:register", "no")),
            "tactile_paving/AddTactilePavingBusStop.kt" to setOf(Tag("check_date:tactile_paving", null), Tag("tactile_paving", "yes"), Tag("tactile_paving", "no")),
            "tactile_paving/AddTactilePavingCrosswalk.kt" to setOf(Tag("check_date:tactile_paving", null), Tag("tactile_paving", "yes"), Tag("tactile_paving", "no"), Tag("tactile_paving", "incorrect")),
            "tactile_paving/AddTactilePavingKerb.kt" to setOf(Tag("barrier", "kerb"), Tag("check_date:tactile_paving", null), Tag("tactile_paving", "yes"), Tag("tactile_paving", "no")),
            "toilet_availability/AddToiletAvailability.kt" to setOf(Tag("toilets", "yes"), Tag("toilets", "no")),
            "toilets_fee/AddToiletsFee.kt" to setOf(Tag("fee", "yes"), Tag("fee", "no")),
            "tourism_information/AddInformationToTourism.kt" to setOf(Tag("information", "office"), Tag("information", "board"), Tag("information", "terminal"), Tag("information", "map"), Tag("information", "guidepost")),
            "tracktype/AddTracktype.kt" to setOf(Tag("check_date:tracktype", null), Tag("tracktype", "grade1"), Tag("tracktype", "grade2"), Tag("tracktype", "grade3"), Tag("tracktype", "grade4"), Tag("tracktype", "grade5")),
            "traffic_calming_type/AddTrafficCalmingType.kt" to setOf(Tag("traffic_calming", "bump"), Tag("traffic_calming", "hump"), Tag("traffic_calming", "table"), Tag("traffic_calming", "cushion"), Tag("traffic_calming", "island"), Tag("traffic_calming", "choker"), Tag("traffic_calming", "chicane"), Tag("traffic_calming", "rumble_strip")),
            "traffic_signals_button/AddTrafficSignalsButton.kt" to setOf(Tag("button_operated", "yes"), Tag("button_operated", "no")),
            "traffic_signals_sound/AddTrafficSignalsSound.kt" to setOf(Tag("check_date:traffic_signals:sound", null), Tag("traffic_signals:sound", "yes"), Tag("traffic_signals:sound", "no")),
            "traffic_signals_vibrate/AddTrafficSignalsVibration.kt" to setOf(Tag("check_date:traffic_signals:vibration", null), Tag("traffic_signals:vibration", "yes"), Tag("traffic_signals:vibration", "no")),
            "oneway_suspects/AddSuspectedOneway.kt" to setOf(Tag("oneway", "no"), Tag("oneway", "yes"), Tag("oneway", "-1")),
            "wheelchair_access/AddWheelchairAccessBusiness.kt" to setOf(Tag("wheelchair", "yes"), Tag("wheelchair", "limited"), Tag("wheelchair", "no")),
            "wheelchair_access/AddWheelchairAccessOutside.kt" to setOf(Tag("check_date:wheelchair", null), Tag("wheelchair", "yes"), Tag("wheelchair", "limited"), Tag("wheelchair", "no")),
            "wheelchair_access/AddWheelchairAccessPublicTransport.kt" to setOf(Tag("check_date:wheelchair", null), Tag("wheelchair", "yes"), Tag("wheelchair", "limited"), Tag("wheelchair", "no")),
            "wheelchair_access/AddWheelchairAccessToilets.kt" to setOf(Tag("check_date:wheelchair", null), Tag("wheelchair", "yes"), Tag("wheelchair", "limited"), Tag("wheelchair", "no")),
            "wheelchair_access/AddWheelchairAccessToiletsPart.kt" to setOf(Tag("check_date:toilets:wheelchair", null), Tag("toilets:wheelchair", "yes"), Tag("toilets:wheelchair", "limited"), Tag("toilets:wheelchair", "no")),
            "width/AddRoadWidth.kt" to setOf(Tag("width", null), Tag("source:width", "ARCore")),
        )
    }
    @TaskAction fun run() {
        var processed = 0
        val failedQuests = mutableSetOf<String>()
        val foundTags = mutableListOf<TagQuestInfo>()
        val folderGenerator = questFolderGenerator()

        /*
        val path = QUEST_ROOT_WITH_SLASH_ENDING + "crossing/AddCrossing.kt"
        val fileSourceCode = loadFileFromPath(path)
        showEntire(path, fileSourceCode)
        */

        while (folderGenerator.hasNext()) {
            val folder = folderGenerator.next()
            var foundQuestFile = false

            val suspectedAnswerEnumFiles = mutableListOf<File>()
            File(folder.toString()).walkTopDown().forEach {
                if (isLikelyAnswerEnumFile(it.toString())) {
                    suspectedAnswerEnumFiles.add(it)
                }
            }

            File(folder.toString()).walkTopDown().forEach {
                if(it.isFile) {
                    val suspectedAnswerEnumFilesForThisFile = suspectedAnswerEnumFiles + candidatesForEnumFilesBasedOnImports(it.path)
                    if (isQuestFile(it.name)) {
                        foundQuestFile = true
                        val fileSourceCode = loadFileFromPath(it.toString())
                        val got = addedOrEditedTags(it.name, fileSourceCode, suspectedAnswerEnumFilesForThisFile)
                        reportResultOfScanInSingleQuest(got, it.toString().removePrefix(QUEST_ROOT_WITH_SLASH_ENDING), fileSourceCode)
                        if (got != null) {
                            processed += 1
                            got.forEach { tags -> foundTags.add(TagQuestInfo(tags, it.name)) }
                        } else {
                            failedQuests.add(it.toString())
                        }
                    }
                }
            }
            if (!foundQuestFile && folder.name != "note_discussion") {
                throw ParsingInterpretationException("not found quest file for $folder")
            }
        }
        reportResultOfDataCollection(foundTags, processed, failedQuests)
    }

    private fun questFolderGenerator() = iterator {
        File(QUEST_ROOT_WITH_SLASH_ENDING).walkTopDown().maxDepth(1).forEach { folder ->
            if (folder.isDirectory && "$folder/" != QUEST_ROOT_WITH_SLASH_ENDING) {
                yield(folder)
            }
        }
    }

    private fun candidatesForEnumFilesBasedOnImports(path:String): List<File> {
        // initially just files from folder were taken as a base
        // due to cases like AddCrossing reaching across folders
        // it was not working well and require this extra parsing
        //
        // also, just parsing imports is not sufficient
        // see AddBikeParkingType which is not explicitly
        // importing the enum
        //
        // note: importedByFile may have false negatives that require extra parsing
        // to handle this
        return importedByFile(path).filter { isLikelyAnswerEnumFile(it) && "/quests/" in it }.map {File(it)}.filter{it.isFile}
    }

    private fun importedByFile(path:String): Set<String> {
        val returned = mutableSetOf<String>()
        val fileSourceCode = loadFileFromPath(path)
        val ast = AstSource.String(path, fileSourceCode)
        ast.parse().locateByDescription("importList").forEach {
            it.locateByDescription("importHeader").forEach {
                if(it is DefaultAstNode) {
                    areDirectChildrenMatchingStructureThrowExceptionIfNot(listOf(listOf("IMPORT", "WS", "identifier", "semi")), it, fileSourceCode, eraseWhitespace=false)
                    val imported = it.locateSingleOrExceptionByDescriptionDirectChild("identifier")
                    //println(imported.locateByDescriptionDirectChild("simpleIdentifier").size.toString() + "  ddddddd")
                    val importedPath = KOTLIN_IMPORT_ROOT_WITH_SLASH_ENDING + imported.locateByDescriptionDirectChild("simpleIdentifier").map {
                        (it.tree() as KlassIdentifier).identifier
                    }.joinToString("/") + ".kt"
                    if(File(importedPath).isFile) {
                        // WARNING: false positives here can be expected
                        // WARNING: this will treat
                        // import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
                        // as import of PEDESTRIAN.kt file
                        // not as import of PEDESTRIAN from EditTypeAchievement.kt file

                        // and this check will result in false negatives in turn...
                        returned.add(importedPath)
                    }
                }
            }
        }
        //in case that it is actually needed
        //println("packageHeader")
        //println(ast.parse().locateSingleOrExceptionByDescription("packageHeader").relatedSourceCode(fileSourceCode))
        //ast.parse().locateSingleOrExceptionByDescription("packageHeader").showHumanReadableTreeWithSourceCode(fileSourceCode)
        return returned
    }

    private fun isLikelyAnswerEnumFile(filename: String): Boolean {
        if (".kt" !in filename) {
            return false
        }
        if ("Form" in filename) {
            return false
        }
        if ("Adapter" in filename) {
            return false
        }
        if ("Util" in filename) {
            return false
        }
        if ("Drawable" in filename) {
            return false
        }
        if ("Dao" in filename) {
            return false
        }
        if ("Dialog" in filename) {
            return false
        }
        if ("Item" in filename) {
            return false
        }
        return !isQuestFile(filename)
    }

    private fun isQuestFile(filename: String): Boolean {
        if (".kt" !in filename) {
            return false
        }
        if ("Form" in filename) {
            return false
        }
        if ("Adapter" in filename) {
            return false
        }
        if ("Utils" in filename) {
            return false
        }
        if (filename == "AddressStreetAnswer.kt") {
            return false
        }
        if ("Add" in filename || "Check" in filename || "Determine" in filename || "MarkCompleted" in filename) {
            return true
        }
        return false
    }

    private fun functionParsingSkippedBasedOnSourceCode(sourceCodeOfFunction: String): Boolean {
        // Complex code constructs not supported for now
        // TODO: implement their support
        if ("answer.applyTo(" in sourceCodeOfFunction) {
            return true
        }
        if (".applyTo(tags)" in sourceCodeOfFunction) {
            return true
        }
        if ("applySidewalkSurfaceAnswerTo" in sourceCodeOfFunction) {
            return true
        }
        if ("applyWasteContainerAnswer" in sourceCodeOfFunction) {
            return true
        }
        if ("replaceShop" in sourceCodeOfFunction) {
            return true
        }
        if ("answer.countryCode + \":\" + answer.roadType" in sourceCodeOfFunction) {
            return true
        }
        if ("osmKey" in sourceCodeOfFunction) {
            // key also needs parsing - TODO, this should be solvable
            return true
        }
        if ("applyAnswerRoadName" in sourceCodeOfFunction) {
            return true
        }
        if ("applyRampAnswer" in sourceCodeOfFunction) {
            return true
        }
        if ("applySidewalkAnswerTo" in sourceCodeOfFunction) {
            return true
        }
        if ("tags[\"material\"] = newMaterial" in sourceCodeOfFunction) {
            return true
        }
        if ("answer.joinToString" in sourceCodeOfFunction) {
            return true
        }
        if("\"name:\$languageTag\"" in sourceCodeOfFunction) {
            return true
        }
        if ("tags[\"parking:lane:left:\$laneLeft\"]" in sourceCodeOfFunction) {
            return true
        }
        if("\$key" in sourceCodeOfFunction) {
            return true
        }
        if("val key = when" in sourceCodeOfFunction) {
            return true
        }
        return false
    }

    private fun reportResultOfScanInSingleQuest(got: Set<Tag>?, filepath: String, fileSourceCode: String) {
        var mismatch = false
        if (filepath in EXPECTED_TAG_PER_QUEST) {
            if (got == EXPECTED_TAG_PER_QUEST[filepath]) {
                return
            } else {
                mismatch = true
            }
        }
        val ast = AstSource.String(filepath, fileSourceCode)
        val relevantFunction = getAstTreeForFunctionEditingTags(filepath, ast)
        if (mismatch) {
            println()
            println("-----------------")
            println(filepath)
            println()
            println(got)
            println(EXPECTED_TAG_PER_QUEST[filepath])
            println(tagSetToReproducibleCode(got, filepath))
            println("MISMATCH")
            println("MISMATCH")
            println("MISMATCH")
            println("MISMATCH")
            println("MISMATCH")
            println("MISMATCH")
            println("MISMATCH")
            throw Exception("MISMATCH")
        }
        if (functionParsingSkippedBasedOnSourceCode(relevantFunction.relatedSourceCode(fileSourceCode))) {
            return
        }
        if (got == null) {
            println()
            println("-----------------")
            println(filepath)
            println("EMPTY RESULT, FAILED")
            println("EMPTY RESULT, FAILED")
            println("EMPTY RESULT, FAILED")
            println("EMPTY RESULT, FAILED")
        }
        relevantFunction.showRelatedSourceCode(fileSourceCode, "inspected function")
        if (got != null) {
            println(got)
            println(tagSetToReproducibleCode(got, filepath))
        }
        println("-----------------")
        println()
    }

    private fun tagSetToReproducibleCode(got: Set<Tag>?, filepath: String): String {
        val classesReadyToCreate = got?.joinToString(", ") { it.reproduceCode() }
        return "\"$filepath\" to setOf($classesReadyToCreate),"
    }

    private fun reportResultOfDataCollection(foundTags: MutableList<TagQuestInfo>, processed: Int, failedQuests: MutableSet<String>) {
        // foundTags.forEach { println("$it ${if (it.tag.value == null && !freeformKey(it.tag.key)) {"????????"} else {""}}") }
        val tagsThatShouldBeMoreSpecific = foundTags.filter { it.tag.value == null && !freeformKey(it.tag.key) }.size
        println("${foundTags.size} entries registered, $tagsThatShouldBeMoreSpecific should be more specific, $processed quests processed, ${failedQuests.size} failed")
        val tagsFoundPreviously = 428
        if (foundTags.size != tagsFoundPreviously) {
            println("Something changed in processing! foundTags count ${foundTags.size} vs $tagsFoundPreviously previously")
        }
        val tagsThatShouldBeMoreSpecificFoundPreviously = 8
        if (tagsThatShouldBeMoreSpecific != tagsThatShouldBeMoreSpecificFoundPreviously) {
            println("Something changed in processing! tagsThatShouldBeMoreSpecific count $tagsThatShouldBeMoreSpecific vs $tagsThatShouldBeMoreSpecificFoundPreviously previously")
        }
        val processedQuestsPreviously = 114
        if (processed != processedQuestsPreviously) {
            println("Something changed in processing! processed count $processed vs $processedQuestsPreviously previously")
        }
        val realFailed = failedQuests.size
        val knownFailed = setOf("app/src/main/java/de/westnordost/streetcomplete/quests/barrier_type/AddStileType.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/max_speed/AddMaxSpeed.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/road_name/AddRoadName.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/orchard_produce/AddOrchardProduce.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/street_parking/AddStreetParking.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/sport/AddSport.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/building_type/AddBuildingType.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/steps_ramp/AddStepsRamp.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/cycleway/AddCycleway.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/way_lit/AddWayLit.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/width/AddCyclewayWidth.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/sidewalk/AddSidewalk.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/bus_stop_name/AddBusStopName.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/parking_fee/AddParkingFee.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/parking_fee/AddBikeParkingFee.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/place_name/AddPlaceName.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/address/AddAddressStreet.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/barrier_type/AddBarrierOnPath.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/barrier_type/AddBarrierType.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/barrier_type/AddBarrierOnRoad.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/max_weight/AddMaxWeight.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/recycling_material/AddRecyclingContainerMaterials.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/shop_type/CheckShopType.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/memorial_type/AddMemorialType.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/surface/AddRoadSurface.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/surface/AddFootwayPartSurface.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/surface/AddCyclewayPartSurface.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/surface/AddSidewalkSurface.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/surface/AddPathSurface.kt", "app/src/main/java/de/westnordost/streetcomplete/quests/surface/AddPitchSurface.kt")
        if (realFailed != knownFailed.size) {
            println("Something changed in processing! failed count $realFailed vs ${knownFailed.size} previously")
        }
        if ((failedQuests - knownFailed).isNotEmpty()) {
            println("new failed quests")
            println((failedQuests - knownFailed).joinToString("\", \"", "\"", "\""))
            throw Exception("new failed quests")
        }
        if ((knownFailed - failedQuests).isNotEmpty()) {
            println("new working quests")
            println((knownFailed - failedQuests).joinToString("\", \"", "\"", "\""))
            throw Exception("some failed quests are now working")
        }
        println()
        println()
        foundTags.forEach {
            if (it.tag.key.startsWith("$SURVEY_MARK_KEY:")) {
                return@forEach // compound key with generated explanation, see https://wiki.openstreetmap.org/w/index.php?title=Key:check_date:cycleway
            }
            if (it.tag.key.startsWith("source:")) {
                return@forEach // should have generated explanation (but is missing for now at least!), see https://wiki.openstreetmap.org/w/index.php?title=Key:check_date:cycleway
            }
            if (!isPageExisting("https://wiki.openstreetmap.org/w/index.php?title=Key:${it.tag.key}")) {
                if (it.tag.value != null) {
                    // if value page exists, then it is likely fine
                    if (!isPageExisting("https://wiki.openstreetmap.org/w/index.php?title=Tag:${it.tag.key}=${it.tag.value}")) {
                        println("${it.tag.key}= has no OSM Wiki page and has no value page")
                    }
                } else {
                    println("${it.tag.key}= has no OSM Wiki page")
                }
            }
            if (it.tag.value !in listOf(null, "no", "yes") && !freeformKey(it.tag.key)) {
                if (it.tag.key in listOf("crossing:barrier", "bicycle_rental", "roof:shape", "material", "royal_cypher", "camera:type",
                        "bollard", "board_type", "cycle_barrier", "bicycle_parking", "location",
                        "fire_hydrant:type", // TODO: what about fire_hydrant:type=pond? According to wiki it should not be used
                        // https://wiki.openstreetmap.org/wiki/Tag:emergency%3Dfire_hydrant
                    )) {
                    // this values should be described at the key page
                    // not ideal as
                    // - StreetComplete can be using bogus values
                    // - some of this values may actually have pages

                    // alternative would be creation of OSM wiki pages for them
                    // but I am not entirely sure is it a good idea
                    return@forEach
                }
                if (!isPageExisting("https://wiki.openstreetmap.org/w/index.php?title=Tag:${it.tag.key}=${it.tag.value}")) {
                    println("$it has no OSM Wiki page")
                }
            }
        }
    }

    private fun isPageExisting(url: String): Boolean {
        try {
            URL(url).openStream().bufferedReader().use { it.readText() }
        } catch (e: java.io.FileNotFoundException) {
            return false
        }
        return true
    }

    private fun freeformKey(key: String): Boolean {
        // most have own syntax and limitations obeyed by SC
        // maybe move to general StreetComplete file about OSM tagging?
        if (key in listOf("name", "ref",
                "addr:flats", "addr:housenumber", "addr:street", "addr:place", "addr:block_number", "addr:streetnumber",
                "addr:conscriptionnumber", "addr:housename",
                "building:levels", "roof:levels", "level",
                "collection_times", "opening_hours", "opening_date", "check_date",
                "fire_hydrant:diameter", "maxheight", "width",
                "surface:note", "source:width", "source:maxheight",
                "maxspeed",
                "capacity", "step_count",
                "lanes", "lanes:forward", "lanes:backward", "lanes:both_ways",
                "turn:lanes:both_ways", "turn:lanes", "turn:lanes:forward", "turn:lanes:backward",
                "operator", // technically not fully, but does ot make sense to list all that autocomplete values
                "brand",
            )) {
            return true
        }
        if (SURVEY_MARK_KEY in key) {
            return true
        }
        if (key.endsWith(":note")) {
            return true
        }
        if (key.startsWith("lanes:")) {
            return true
        }
        if (key.startsWith("source:")) {
            return true
        }
        return false
    }

    private fun loadFileFromPath(filepath: String): String {
        val inputStream: InputStream = File(filepath).inputStream()
        return inputStream.bufferedReader().use { it.readText() }
    }

    private fun showEntire(description: String, fileSourceCode: String) {
        val ast = AstSource.String(description, fileSourceCode)
        println("============================here is the entire content (source code tree)==<")
        ast.parse().showHumanReadableTreeWithSourceCode(fileSourceCode)
        println(">---------------------------here is the entire content (source code tree)>===")
        println("----------------------------here is the entire content (source code)==<")
        println(fileSourceCode)
        println(">===========================here is the entire content (source code)>===")
    }

    class Tag(val key: String, val value: String?) {
        override fun toString(): String {
            if (value == null) {
                return "$key=*"
            }
            return "$key=$value"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Tag) return false
            if (key != other.key) return false
            if (value != other.value) return false
            return true
        }

        override fun hashCode(): Int {
            var result = key.hashCode()
            result = 31 * result + (value?.hashCode() ?: 0)
            return result
        }

        fun reproduceCode(): String {
            return if (value == null) {
                "Tag(\"${key}\", $value)"
            } else {
                "Tag(\"${key}\", \"${value}\")"
            }
        }
    }

    class TagQuestInfo(val tag: Tag, private val quest: String) {
        override fun toString(): String {
            return "$tag in $quest"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is TagQuestInfo) return false
            if (tag != other.tag) return false
            if (quest != other.quest) return false
            return true
        }

        override fun hashCode(): Int {
            var result = tag.hashCode()
            result = 31 * result + quest.hashCode()
            return result
        }
    }

    private fun getAstTreeForFunctionEditingTags(description: String, ast: AstSource.String): AstNode {
        val found = ast.parse().extractFunctionByName(NAME_OF_FUNCTION_EDITING_TAGS)
        if (found.isEmpty()) {
            println("$NAME_OF_FUNCTION_EDITING_TAGS not found in $description")
            exitProcess(1)
        }
        if (found.size != 1) {
            println("unexpected function count found")
            exitProcess(1)
        }
        return found[0]
    }

    private fun addedOrEditedTags(description: String, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): Set<Tag>? {
        val appliedTags = mutableSetOf<Tag>()
        var failedExtraction = false
        val ast = AstSource.String(description, fileSourceCode)
        val relevantFunction = getAstTreeForFunctionEditingTags(description, ast)
        if (functionParsingSkippedBasedOnSourceCode(relevantFunction.relatedSourceCode(fileSourceCode))) {
            return null // NOT EVEN TRYING TO SUPPORT FOR NOW TODO
        }
        var got = extractCasesWhereTagsAreAccessedWithIndex(description, relevantFunction, fileSourceCode, suspectedAnswerEnumFiles)
        if (got != null) {
            appliedTags += got
        } else {
            failedExtraction = true
        }

        got = extractCasesWhereTagsAreAccessedWithFunction(description, relevantFunction, fileSourceCode, suspectedAnswerEnumFiles)
        if (got != null) {
            appliedTags += got
        } else {
            failedExtraction = true
        }

        if (appliedTags.size == 0) {
            println("addedOrEditedTags - failed to extract anything at all from ${description}! Will present HumanReadableTreeWithSourceCode")
            relevantFunction.showHumanReadableTreeWithSourceCode(fileSourceCode)
            println("addedOrEditedTags - failed to extract anything at all from ${description}! Presented HumanReadableTreeWithSourceCode")
        }

        if (failedExtraction) {
            return null
        }
        if (failedExtraction) {
            print("extraction known to be a partial failure")
        }
        if (appliedTags.size == 0) {
            return null // parsing definitely failed
        }
        return appliedTags
    }

    private fun extractTextFromHardcodedString(passedTextHolder: Ast, fileSourceCode: String): String? {
        var textHolder = passedTextHolder

        val plausibleText = textHolder.locateByDescription("stringLiteral")
        if (plausibleText.size == 1) {
            val textFoundIfFillingEntireHolder = plausibleText[0]
            if (textHolder.codeRange() == textFoundIfFillingEntireHolder.codeRange()) {
                // actual text holder is hidden inside, but it is actually the same object
                val expectedTextHolder = textFoundIfFillingEntireHolder.tree()
                if (expectedTextHolder is KlassString) {
                    textHolder = expectedTextHolder
                }
            }
        }
        if (textHolder is KlassString) {
            if (textHolder.children.size == 1) {
                val expectedText = textHolder.children[0]
                if (expectedText is StringComponentRaw) {
                    return expectedText.string
                }
            }
        }
        return null
    }

    private fun extractCasesWhereTagsAreAccessedWithIndex(description: String, relevantFunction: AstNode, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): Set<Tag>? {
        // it is trying to detect things like
        // tags["bollard"] = answer.osmValue

        // val appliedTags = mutableSetOf<Tag>()
        // relevantFunction.showHumanReadableTreeWithSourceCode(fileSourceCode)
        /*
          [1495..1529] [34:9..34:43]  ------ statements DefaultAstNode <tags["indoor"] = answer.toYesNo()\n>
          [1495..1528] [34:9..34:42]  -------- statement DefaultAstNode <tags["indoor"] = answer.toYesNo()>
          [1495..1528] [34:9..34:42]  ---------- assignment DefaultAstNode <tags["indoor"] = answer.toYesNo()>
          [1495..1509] [34:9..34:23]  ------------ directlyAssignableExpression DefaultAstNode <tags["indoor"]>

          we want to get entire statement, not just directlyAssignableExpression - this allows us to get info also about the assigned value
         */
        val appliedTags = mutableSetOf<Tag>()
        relevantFunction.locateByDescription("assignment").forEach { assignment ->
            assignment.children.forEach { tagsDictAccess ->
                if (tagsDictAccess.description == "directlyAssignableExpression" &&
                    tagsDictAccess is DefaultAstNode &&
                    tagsDictAccess.children[0].tree() is KlassIdentifier &&
                    ((tagsDictAccess.children[0].tree() as KlassIdentifier).identifier == "tags")
                ) {
                    // this limits it to things like
                    // tags[something] = somethingElse
                    // (would it also detect tags=whatever)?
                    val indexingElement = tagsDictAccess.locateSingleOrExceptionByDescription("assignableSuffix")
                        .locateSingleOrExceptionByDescription("indexingSuffix")
                    // indexingElement is something like ["indoor"] or [key]
                    val expression = indexingElement.locateSingleOrExceptionByDescriptionDirectChild("expression") // drop outer [ ]
                    val potentialTexts = expression.locateByDescription("stringLiteral", debug = false) // what if it is something like "prefix" + CONSTANT ?
                    val potentialVariable = if (expression is KlassIdentifier) { expression } else { null } // tag[key] = ...
                    val complexPotentialVariable = expression.locateByDescriptionDirectChild("disjunction") // tag[answer.osmKey] = ...
                    if (potentialTexts.size == 1) {
                        val processed = potentialTexts[0].tree()
                        if (processed == null) {
                            throw ParsingInterpretationException("not handled")
                        }
                        val key = extractTextFromHardcodedString(processed, fileSourceCode)
                        if (key == null) {
                            throw ParsingInterpretationException("not handled")
                        }
                        // assignment (for example tags["highway"] = "steps" ) is expected to have following children:
                        // directlyAssignableExpression ( for example tags["highway"] )
                        // WS
                        // ASSIGNMENT =
                        // WS
                        // expression ( for example: "steps" )
                        val valueHolder = assignment.locateSingleOrExceptionByDescriptionDirectChild("expression")
                        appliedTags += extractValuesForKnownKey(key, valueHolder, fileSourceCode, suspectedAnswerEnumFiles)
                    } else if (potentialVariable != null) {
                        expression.showHumanReadableTree()
                        expression.showRelatedSourceCode(fileSourceCode, "expression in identified access as a variable")
                        println(KotlinGrammarParserType.identifier.toString() + " identified as accessing index as a variable (potentialTexts.size = ${potentialTexts.size})")
                        return null
                    } else if (complexPotentialVariable.size == 1) {
                        expression.showHumanReadableTree()
                        expression.showRelatedSourceCode(fileSourceCode, "expression in identified access as a complex variable")
                        println(complexPotentialVariable[0].relatedSourceCode(fileSourceCode) + " identified as accessing index as a complex variable (potentialTexts.size = ${potentialTexts.size})")
                        return null
                    } else {
                        expression.showRelatedSourceCode(fileSourceCode, "expression - not handled")
                        expression.showHumanReadableTree()
                        println(expression::class)
                        throw ParsingInterpretationException("not handled, ${potentialTexts.size} texts, $potentialVariable variable")
                    }
                }
            }
        }
        return appliedTags
    }

    private fun getEnumValuesDefinedInThisFilepath(filepath:String):Set<String>{
        val values = mutableSetOf<String>()
        val fileMaybeContainingEnumSourceCode = loadFileFromPath(filepath)
        val ast = AstSource.String(filepath, fileMaybeContainingEnumSourceCode)
        val potentialEnumFileAst = ast.parse()
        var enumsTried = 0
        potentialEnumFileAst.locateByDescription("classDeclaration").forEach { enum ->
            if (enum.locateSingleOrExceptionByDescription("modifiers").relatedSourceCode(fileMaybeContainingEnumSourceCode) == "enum") {
                enumsTried += 1
                enum.locateByDescription("enumEntry").forEach { enumEntry ->
                    var extractedText: String? = null
                    val valueArguments = enumEntry.locateSingleOrExceptionByDescriptionDirectChild("valueArguments")
                    val arguments = valueArguments.locateByDescriptionDirectChild("valueArgument")
                    if (arguments.size == 1) {
                        extractedText = extractTextFromHardcodedString(arguments[0], fileMaybeContainingEnumSourceCode)
                        if (extractedText == null) {
                            if(arguments[0].tree() is KlassDeclaration && (arguments[0].tree() as KlassDeclaration).identifier.toString() == "null") {
                                //println("it has null as value, apparently")
                            } else {
                                println("showHumanReadableTreeWithSourceCode(fileMaybeContainingEnumSourceCode) - showing $filepath after enum extraction failed")
                                valueArguments.showHumanReadableTreeWithSourceCode(fileMaybeContainingEnumSourceCode)
                                println("showHumanReadableTreeWithSourceCode(fileMaybeContainingEnumSourceCode) - shown $filepath after enum extraction failed")
                                println(fileMaybeContainingEnumSourceCode)
                                println("source code displayed - shown $filepath after enum extraction failed")
                            }
                        } else {
                            values.add(extractedText)
                        }
                    } else {
                        // TODO above assumes that there is a single enum with a single assigned value to each enum statement...
                        // for more complex ones
                        println("more than one argument, lets try to disentagle this")
                        for(i in arguments.indices) {
                            println("argument $i out of ${arguments.size} - ${extractTextFromHardcodedString(arguments[i], fileMaybeContainingEnumSourceCode)}")
                        }
                        enum.locateSingleOrExceptionByDescription("primaryConstructor")
                            .showHumanReadableTreeWithSourceCode(fileMaybeContainingEnumSourceCode)
                        valueArguments.showRelatedSourceCode(fileMaybeContainingEnumSourceCode, "valueArguments")
                    }
                }
            }
        }
        if(values.size == 0) {
            println("enum extraction from $filepath failed! $enumsTried enumsTried")
        }
        return values
    }

    private fun extractValuesForKnownKey(key: String, valueHolder: Ast, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): MutableSet<Tag> {
        val appliedTags = mutableSetOf<Tag>()

        val whenExpression = valueHolder.locateSingleOrNullByDescription("whenExpression")
        if (whenExpression != null) {
            if (whenExpression.relatedSourceCode(fileSourceCode) == valueHolder.relatedSourceCode(fileSourceCode)) {
                return extractValuesForKnownKeyFromWhenExpression(key, whenExpression, fileSourceCode, suspectedAnswerEnumFiles)
            } else {
                throw ParsingInterpretationException("not handled, when expressions as part of something bigger")
            }
        }

        val ifExpression = valueHolder.locateSingleOrNullByDescription("ifExpression")
        if (ifExpression != null) {
            if (ifExpression.relatedSourceCode(fileSourceCode) == valueHolder.relatedSourceCode(fileSourceCode)) {
                return extractValuesForKnownKeyFromIfExpression(key, ifExpression, fileSourceCode, suspectedAnswerEnumFiles)
            } else {
                throw ParsingInterpretationException("not handled, when expressions as part of something bigger")
            }
        }

        val valueIfItIsSimpleText = extractTextFromHardcodedString(valueHolder, fileSourceCode)
        if (valueIfItIsSimpleText != null) {
            appliedTags.add(Tag(key, valueIfItIsSimpleText))
        } else if (valueHolder.relatedSourceCode(fileSourceCode) == "answer.osmValue") {
            // bad news! Answer values are not directly in this file, but defined elsewhere
            // almost always in a separate enum file
            var extractedSomething = false
            suspectedAnswerEnumFiles.forEach {
                getEnumValuesDefinedInThisFilepath(it.toString()).forEach {value ->
                    appliedTags.add(Tag(key, value))
                    extractedSomething = true
                }
            }
            if (!extractedSomething) {
                println("answer.osmValue, failed to find values for now")
                appliedTags.add(Tag(key, null)) // TODO - get also value...
            }
            // TODO handle this somehow - requires extra parsing, likely in another file
        } else if (valueHolder.relatedSourceCode(fileSourceCode).endsWith(".toYesNo()")) {
            // previous form of check:
            // in listOf("answer.toYesNo()", "it.toYesNo()", "answer.credit.toYesNo()", "answer.debit.toYesNo()", "isAutomated.toYesNo()")
            // TODO: fix this hack by proper parse and detect toYesNo() at the end? (low priority)
            appliedTags.add(Tag(key, "yes"))
            appliedTags.add(Tag(key, "no"))
        } else {
            appliedTags.add(Tag(key, null)) // TODO - get also value...
            if (!freeformKey(key)) {
                println()
                println()
                println()
                val description = "get value (key is known: $key) from <${valueHolder.relatedSourceCode(fileSourceCode)}> somehow... valueIfItIsSimpleText is $valueIfItIsSimpleText"
                println(description)
                valueHolder.showHumanReadableTreeWithSourceCode(fileSourceCode)
                valueHolder.showRelatedSourceCode(fileSourceCode, description)
            }
        }
        return appliedTags
    }

    private fun extractValuesForKnownKeyFromIfExpression(key: String, ifExpression: AstNode, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): MutableSet<Tag> {
        val appliedTags = mutableSetOf<Tag>()
        ifExpression.locateByDescription("controlStructureBody").forEach {
            appliedTags += extractValuesForKnownKey(key, it, fileSourceCode, suspectedAnswerEnumFiles)
        }
        return appliedTags
    }

    private fun extractValuesForKnownKeyFromWhenExpression(key: String, whenExpression: AstNode, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): MutableSet<Tag> {
        val appliedTags = mutableSetOf<Tag>()
        whenExpression.locateByDescription("whenEntry").forEach { it ->
            val structure = it.children.filter { it.description != "WS" }
            val structureDescriptions = structure.map{ it.description }
            /*
            structure.forEach { child ->
                println()
                println()
                println("child")
                println(child.description)
                child.showRelatedSourceCode(fileSourceCode, "child")
            }
            */
            val expectedStructureA = listOf("whenCondition", "ARROW", "controlStructureBody", "semi")
            val expectedStructureB = listOf("ELSE", "ARROW", "controlStructureBody", "semi")
            areDirectChildrenMatchingStructureThrowExceptionIfNot(listOf(expectedStructureA, expectedStructureB), it, fileSourceCode, eraseWhitespace=true)
            appliedTags += extractValuesForKnownKey(key, structure[2], fileSourceCode, suspectedAnswerEnumFiles)
        }
        return appliedTags
    }

    private fun areDirectChildrenMatchingStructureThrowExceptionIfNot(expectedStructures: List<List<String>>, expression: AstNode, fileSourceCode: String, eraseWhitespace: Boolean){
        val structure = expression.children.filter { !(eraseWhitespace && it.description == "WS") }.map{ it.description }
        expectedStructures.forEach {
            if(it == structure) {
                return
            }
        }
        var maxLength = 0
        expectedStructures.forEach { if(maxLength < it.size) {maxLength = it.size} }
        for(i in 0 until maxLength) {
            expectedStructures.forEach {
                if(it.size > i) {
                    if(it[i] != structure[i]){
                        println("STRUCTURE FAILED")
                        println("WHEN STRUCTURE FAILED")
                        expression.showHumanReadableTreeWithSourceCode(fileSourceCode)
                        expression.showRelatedSourceCode(fileSourceCode, "WHEN STRUCTURE FAILED")
                        println(expression.showRelatedSourceCode(fileSourceCode, "WHEN STRUCTURE FAILED"))
                        println()
                        structure.forEach { println(it) }
                        throw ParsingInterpretationException("unexpected structure! at $i index")
                    }
                }
            }
        }
    }

    private fun extractCasesWhereTagsAreAccessedWithFunction(description:String, relevantFunction: AstNode, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): Set<Tag>? {
        // it is trying to detect things like
        // tags.updateWithCheckDate("smoking", answer.osmValue)
        val appliedTags = mutableSetOf<Tag>()
        relevantFunction.locateByDescription("postfixUnaryExpression")
            .filter { isAccessingTagsVariableWithMemberFunction(it) }
            .forEach { accessingTagsWithFunction ->
                val dotAndFunction = accessingTagsWithFunction.locateByDescriptionDirectChild("postfixUnarySuffix")[0].locateSingleOrExceptionByDescriptionDirectChild("navigationSuffix")

                if (dotAndFunction !is AstNode) {
                    throw ParsingInterpretationException("unexpected!")
                }
                val functionName = getNameOfFunctionFromNavigationSuffix(dotAndFunction)
                if (functionName in listOf(
                        "setCheckDateForKey",
                        "updateCheckDateForKey"
                    )
                ) {
                    // only check data for
                    val keyString = extractArgumentInFunctionCall(0, accessingTagsWithFunction, fileSourceCode)
                    if (keyString != null) {
                        appliedTags.add(Tag("$SURVEY_MARK_KEY:$keyString", null))
                    }
                } else if (functionName ==  "updateWithCheckDate") {
                    var keyString = extractArgumentInFunctionCall(0, accessingTagsWithFunction, fileSourceCode)
                    val valueString = extractArgumentInFunctionCall(1, accessingTagsWithFunction, fileSourceCode) // WOMP WOPO TODO?

                    // fold it into extractArgumentInFunctionCall?
                    // try to automatically obtain this constants?
                    if (keyString == null) {
                        val keyArgumentAst = extractArgumentSyntaxTreeInFunctionCall(0, accessingTagsWithFunction, fileSourceCode).locateSingleOrNullByDescription("primaryExpression")
                        keyArgumentAst!!.relatedSourceCode(fileSourceCode)
                        keyArgumentAst.showHumanReadableTreeWithSourceCode(fileSourceCode)
                        val keyArgumentAstTree = keyArgumentAst.tree()
                        if(keyArgumentAstTree is KlassIdentifier) {
                            if(keyArgumentAstTree.identifier == "SOUND_SIGNALS") {
                                keyString = SOUND_SIGNALS
                            }
                            if(keyArgumentAstTree.identifier == "VIBRATING_BUTTON") {
                                keyString = VIBRATING_BUTTON
                            }
                        }
                    }

                    if (keyString != null) {
                        appliedTags.add(Tag("$SURVEY_MARK_KEY:$keyString", null))
                        if (valueString != null) {
                            appliedTags.add(Tag(keyString, valueString))
                        } else {
                            val valueAst = extractArgumentSyntaxTreeInFunctionCall(1, accessingTagsWithFunction, fileSourceCode)
                            if (valueAst.relatedSourceCode(fileSourceCode) == "answer.toYesNo()") {
                                // kind of hackish, fix this?
                                appliedTags.add(Tag(keyString, "yes"))
                                appliedTags.add(Tag(keyString, "no"))
                            } else if (valueAst.relatedSourceCode(fileSourceCode) == "answer.osmValue") {
                                var extractedNothing = true
                                suspectedAnswerEnumFiles.forEach {
                                    getEnumValuesDefinedInThisFilepath(it.toString()).forEach { value ->
                                        appliedTags.add(Tag(keyString, value))
                                        extractedNothing = false
                                    }
                                }
                                if (extractedNothing) {
                                    println("Enum obtaining failed!")
                                    println("Enum obtaining failed! suspectedAnswerEnumFiles $suspectedAnswerEnumFiles")
                                    println("Enum obtaining failed!")
                                    appliedTags.add(Tag(keyString, valueString))
                                    println("44444444444444<<< tags dict is accessed with updateWithCheckDate, key known ($keyString), value unknown, enum obtaining failed<")
                                    valueAst.showHumanReadableTreeWithSourceCode(fileSourceCode)
                                    valueAst.showRelatedSourceCode(fileSourceCode,
                                        "extracted valueAst in tags dict access")
                                    println(">>>44444444444>")
                                    accessingTagsWithFunction.showRelatedSourceCode(fileSourceCode,
                                        "extracted accessingTagsWithFunction in tags dict access")
                                    println(">>>33333333333>")
                                }
                            } else {
                                val valueSourceCode = valueAst.relatedSourceCode(fileSourceCode)
                                if (freeformKey(keyString) && valueSourceCode in setOf("answer.toString()", "openingHoursString", "answer.times.toString()")) {
                                    // key is freeform and it appears to not be enum - so lets skip complaining and attempting to tarck down value
                                    // individual quests can be investigated as needed
                                    appliedTags.add(Tag(keyString, null))
                                } else {
                                    appliedTags.add(Tag(keyString, valueString))
                                    println("XXXXXXXXXXXXXXXXXXXXX<<< $description tags dict is accessed with updateWithCheckDate, key known ($keyString), value unknown, obtaining data failed<")
                                    valueAst.showHumanReadableTreeWithSourceCode(fileSourceCode)
                                    valueAst.showRelatedSourceCode(fileSourceCode, "extracted valueAst in tags dict access")
                                    println(">>>VVVVVVVVVVVVVVVVVV> $description")
                                    accessingTagsWithFunction.showRelatedSourceCode(fileSourceCode, "extracted valueAst in tags dict access")
                                    println(">>>IIIIIIIIIIIIIIIIIIIII> $description")
                                    println(relevantFunction.showRelatedSourceCode(fileSourceCode, "extractCasesWhereTagsAreAccessedWithFunction - extraction failing"))
                                    println(">>>0000000000000000000> $description")
                                }
                            }
                        }
                    } else {
                        // TODO
                        println("^^^^^^^^^^^^^^^^ $description - failed to extract key from updateWithCheckDate")
                        //val keyString = extractArgumentInFunctionCall(0, accessingTagsWithFunction, fileSourceCode)
                        val keyArgumentAst = extractArgumentSyntaxTreeInFunctionCall(0, accessingTagsWithFunction, fileSourceCode).locateSingleOrNullByDescription("primaryExpression")
                        keyArgumentAst!!.relatedSourceCode(fileSourceCode)
                        keyArgumentAst.showHumanReadableTreeWithSourceCode(fileSourceCode)
                        println("^&^&^&^&")
                        return null
                    }
                } else if (functionName in listOf("remove", "containsKey", "removeCheckDatesForKey", "hasChanges", "entries", "hasCheckDateForKey", "hasCheckDate")) {
                    // skip, as only added or edited tags are listed - and removed one and influencing ones are ignored
                } else if (functionName in listOf("updateCheckDate")) {
                    appliedTags.add(Tag(SURVEY_MARK_KEY, null))
                } else if (functionName == "replaceShop") {
                    // that brings basically entire NSI, right?
                    // worse - not entire, only segment of it...
                    // so NSI would be parsed in turn...
                    // TODO
                    return null
                } else {
                    throw ParsingInterpretationException("unexpected function name $functionName")
                }
                // println("found directlyAssignableExpression with tags, not managed to parse it")
                /*
                val text = rightHand.locateSingleByDescription("lineStringLiteral") // what if it is something like "prefix" + CONSTANT ?
                val processed = text.root()
                if (processed is StringComponentRaw) {
                    println(processed.string)
                    appliedTags.add(processed.string)
                } else {
                    it.showHumanReadableTree()
                    println("found directlyAssignableExpression with tags, not managed to parse it")
                }
                */
            }
        return appliedTags
    }

    private fun isAccessingTagsVariableWithMemberFunction(ast: AstNode): Boolean {
        val root = ast.tree()
        if (root !is KlassIdentifier) {
            return false
        }
        if (root.identifier != "tags") {
            return false
        }
        val primary = ast.locateSingleOrExceptionByDescriptionDirectChild("primaryExpression")
        val rootOfExpectedTagsIdentifier = primary.tree()
        if (rootOfExpectedTagsIdentifier !is KlassIdentifier) {
            println()
            ast.showHumanReadableTree()
            println()
            primary.showHumanReadableTree()
            throw ParsingInterpretationException("unexpected! primary is ${primary::class}")
        }
        if (rootOfExpectedTagsIdentifier.identifier != "tags") {
            throw ParsingInterpretationException("unexpected!")
        }
        val possibleDotAndFunction = ast.locateByDescriptionDirectChild("postfixUnarySuffix")
        if (possibleDotAndFunction.isEmpty()) {
            // this will happen in case of say
            // tags["key"] = value
            // in such case we want to skip it
            return false
        }
        val expectedToHoldDotAndFunctionCall = possibleDotAndFunction[0].locateByDescriptionDirectChild("navigationSuffix")
        if (expectedToHoldDotAndFunctionCall.isEmpty()) {
            // maybe false positive?
            // maybe something like
            // .any { tags[it]?.toCheckDate() != null }
            // where skipping is valid?
            return false
        }
        return true
    }

    private fun extractArgumentListSyntaxTreeInFunctionCall(ast: AstNode): List<AstNode> {
        val arguments = ast.locateByDescriptionDirectChild("postfixUnarySuffix")[1]
            .locateSingleOrExceptionByDescriptionDirectChild("callSuffix")
            .locateSingleOrExceptionByDescriptionDirectChild("valueArguments")
        return arguments.locateByDescription("valueArgument")
    }

    private fun extractArgumentSyntaxTreeInFunctionCall(index: Int, ast: AstNode, fileSourceCode: String): AstNode {
        return extractArgumentListSyntaxTreeInFunctionCall(ast)[index]
    }

    private fun extractArgumentInFunctionCall(index: Int, ast: AstNode, fileSourceCode: String): String? {
        val found = extractArgumentSyntaxTreeInFunctionCall(index, ast, fileSourceCode).locateSingleOrNullByDescription("primaryExpression")
        if (found == null) {
            println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA extractArgumentInFunctionCall failed")
            ast.tree()!!.showHumanReadableTreeWithSourceCode(fileSourceCode)
            ast.tree()!!.showRelatedSourceCode(fileSourceCode, "extractArgumentInFunctionCall")
            ast.showRelatedSourceCode(fileSourceCode, "extractArgumentInFunctionCall - not found")
            ast.tree()!!.showRelatedSourceCode(fileSourceCode, "extractArgumentInFunctionCall - not found (rooted)")
            println("${extractArgumentSyntaxTreeInFunctionCall(index, ast, fileSourceCode)} - extractArgumentSyntaxTreeInFunctionCall(index, ast, fileSourceCode)")
            println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA extractArgumentInFunctionCall failed")
            return null
        }
        if (found.children.size == 1) {
            if (found.children[0].description == "stringLiteral") {
                val stringObject = (found.children[0].tree() as KlassString).children[0]
                return (stringObject as StringComponentRaw).string
            } else {
                // TODO maybe handle this?
                /*
                found.showHumanReadableTree()
                extractArgumentListSyntaxTreeInFunctionCall(ast)[index].showRelatedSourceCode(fileSourceCode, "unhandled extracting index $index - not string")
                println("unhandled key access")
                */
                return null
            }
        } else {
            // TODO handle this
            extractArgumentListSyntaxTreeInFunctionCall(ast)[index].showHumanReadableTree()
            extractArgumentListSyntaxTreeInFunctionCall(ast)[index].showRelatedSourceCode(fileSourceCode, "unhandled extracting index $index")
            println("unhandled extraction of $index function parameter - multiple children")
            return null
        }
    }

    private fun getNameOfFunctionFromNavigationSuffix(dotAndFunction: AstNode): String {
        if (dotAndFunction.description != "navigationSuffix") {
            exitProcess(1)
        }
        val expectedPackagedDot = dotAndFunction.children[0]
        if (expectedPackagedDot.description != "memberAccessOperator") {
            throw ParsingInterpretationException("unexpected!")
        }
        if (expectedPackagedDot !is AstNode) {
            throw ParsingInterpretationException("unexpected!")
        }
        val expectedDot = expectedPackagedDot.children[0]
        if (expectedDot !is DefaultAstTerminal) {
            throw ParsingInterpretationException("unexpected!")
        }
        if (expectedDot.text != ".") {
            throw ParsingInterpretationException("unexpected!")
        }
        val expectedFunctionIdentifier = dotAndFunction.children[1]
        if (expectedFunctionIdentifier.description != "simpleIdentifier") {
            throw ParsingInterpretationException("unexpected!")
        }
        if (expectedFunctionIdentifier.tree() !is KlassIdentifier) {
            throw ParsingInterpretationException("unexpected! expectedFunctionIdentifier.root() is ${expectedFunctionIdentifier.tree()!!::class}")
        }
        return (expectedFunctionIdentifier.tree() as KlassIdentifier).identifier
    }

    class ParsingInterpretationException(private val s: String) : Throwable() {
        override fun toString(): String {
            return s
        }
    }

    private fun Ast.codeRange(): Pair<Int, Int> {
        val start = tree()!!.humanReadableDescriptionInfo()!!.start
        val end = tree()!!.humanReadableDescriptionInfo()!!.end
        return Pair(start, end)
    }

    private fun Ast.relatedSourceCode(sourceCode: String): String {
        if (tree() == null) {
            return "<source code not available>"
        }
        val start = tree()!!.humanReadableDescriptionInfo()!!.start
        val end = tree()!!.humanReadableDescriptionInfo()!!.end
        if (start < 0 || end < 0) {
            return "<source code not available> - stated range was $start to $end index"
        }
        return sourceCode.subSequence(start, end).toString()
    }

    private fun Ast.showRelatedSourceCode(sourceCode: String, description: String) {
        println("--------------------here is the $description (source code)---<")
        println(relatedSourceCode(sourceCode))
        println(">---------------------------here is the $description (source code)")
    }

    private fun Ast.showHumanReadableTreeWithSourceCode(fileSourceCode: String) {
        println("---------------------------------------showHumanReadableTreeWithSourceCode")
        humanReadableTreeWithSourceCode(0, fileSourceCode).forEach { println(it) }
    }

    private fun Ast.humanReadableTreeWithSourceCode(indent: Int, fileSourceCode: String): List<String> {
        val info = ((this as? AstWithAstInfo)?.info?.toString() ?: "").padEnd(34)
        val infoHuman = humanReadableDescriptionInfo()
        val self = "$info${"--".repeat(indent)} ${infoHuman?.humanReadableDescription} <${relatedSourceCode(fileSourceCode)}>" // detachRaw()
        return if (this is AstNode) {
            listOf(self) + children.flatMap { child ->
                child.humanReadableTreeWithSourceCode(indent + 1, fileSourceCode)
            }
        } else {
            listOf(self)
        }
    }

    private fun Ast.showHumanReadableTree() {
        println("---------------------------------------")
        humanReadableTree(0).forEach { println(it) }
    }

    private fun Ast.humanReadableTree(indent: Int): List<String> {
        val info = ((this as? AstWithAstInfo)?.info?.toString() ?: "").padEnd(34)
        val self = "$info${"  ".repeat(indent)} ${humanReadableDescriptionInfo()?.humanReadableDescription} " // detachRaw()
        return if (this is AstNode) {
            listOf(self) + children.flatMap { child ->
                child.humanReadableTree(indent + 1)
            }
        } else {
            listOf(self)
        }
    }

    private fun Ast.locateSingleOrNullByDescription(filter: String, debug: Boolean = false): AstNode? {
        val found = locateByDescription(filter, debug)
        if (found.size != 1) {
            return null
        } else {
            return found[0]
        }
    }

    private fun Ast.locateSingleOrExceptionByDescription(filter: String, debug: Boolean = false): AstNode {
        val found = locateByDescription(filter, debug)
        if (found.size != 1) {
            throw ParsingInterpretationException("unexpected count! Expected single matching on filter $filter, got ${found.size}")
        } else {
            return found[0]
        }
    }

    private fun Ast.locateByDescription(filter: String, debug: Boolean = false): List<AstNode> {
        if (this is AstNode) {
            val fromChildren = children.flatMap { child ->
                child.locateByDescription(filter, debug)
            }
            return if (description == filter) {
                if (debug) {
                    println("$filter filter matching description")
                }
                listOf(this) + fromChildren
            } else {
                if (debug) {
                    println("$filter filter NOT matching description $description")
                }
                fromChildren
            }
        } else {
            return listOf()
        }
    }

    private fun Ast.locateSingleOrExceptionByDescriptionDirectChild(filter: String): Ast {
        val found = locateByDescriptionDirectChild(filter)
        if (found.size != 1) {
            showHumanReadableTree()
            throw ParsingInterpretationException("unexpected count! Expected single matching direct child on filter $filter, got ${found.size}")
        } else {
            return found[0]
        }
    }

    private fun Ast.locateSingleOrNullByDescriptionDirectChild(filter: String): Ast? {
        val found = locateByDescriptionDirectChild(filter)
        if (found.size != 1) {
            return null
        } else {
            return found[0]
        }
    }

    private fun Ast.locateByDescriptionDirectChild(filter: String): List<Ast> {
        val returned = mutableListOf<Ast>()
        if (this is AstNode) {
            for (child in children) {
                if (child.description == filter) {
                    returned.add(child)
                }
            }
        }
        return returned
    }

    private fun Ast.extractFunctionByName(functionName: String): List<AstNode> {
        if (description == "functionDeclaration") {
            if (this is AstNode) {
                children.forEach {
                    if (it.description == "simpleIdentifier" && it.tree() is KlassIdentifier && ((it.tree() as KlassIdentifier).identifier == functionName)) {
                        // this.showHumanReadableTree()
                        return listOf(this) + children.flatMap { child ->
                            child.extractFunctionByName(functionName)
                        }
                    }
                }
            } else {
                throw ParsingInterpretationException("wat")
            }
        }
        return if (this is AstNode) {
            children.flatMap { child ->
                child.extractFunctionByName(functionName)
            }
        } else {
            listOf()
        }
    }

    private fun Ast.tree(): Ast? {
        var returned: Ast? = null
        this.summary(false).onSuccess { returned = it.firstOrNull() }
        return returned
    }

    private fun Ast.humanReadableDescriptionInfo(): ElementInfo? {
        val current = this.tree() ?: return null
        val textReadable = "$description " + when (current) {
            is KlassDeclaration -> "KlassDeclaration, identifier: ${current.identifier}}"
            is StringComponentRaw -> "string<${current.string}> ${current::class}"
            is DefaultAstTerminal -> "DefaultAstTerminal, text: ${current.text}"
            is DefaultAstNode -> "DefaultAstNode"
            is KlassIdentifier -> "KlassIdentifier, identifier: ${current.identifier}"
            is KlassString -> "KlassString"
            else -> "??unknown class: ${current::class}"
        }
        return ElementInfo(textReadable, current.astInfoOrNull!!.start.index, current.astInfoOrNull!!.stop.index) // current.astInfoOrNull!!.start.line, current.astInfoOrNull!!.start.row
    }

    class ElementInfo(val humanReadableDescription: String, val start: Int, val end: Int)

    private fun AstSource.parse() = KotlinGrammarAntlrKotlinParser.parseKotlinFile(this)
}
