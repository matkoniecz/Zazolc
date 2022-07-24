import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jsoup.Jsoup
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.system.exitProcess

/*
parsing source code to automatically generate documentation for am open source program (StreetComplete, an OpenStreetMap editor)

I posted a big chunk of code here - but feel free to review only a small section, I will upvote all
answers that tried to help, will award bounties to especially useful and will actually use that code to improve
this deployed open source software ( tracked in https://github.com/streetcomplete/StreetComplete/issues/4225 )

If you make a nontrivial suggestions: please add note that you license your work on GPLv3 license.
It is not needed for trivial changes, but for substantial I need this to be able to use your work.

For anyone making comments: please specify preferred name and email if you want (can be also anonymous@example.com), in such case I will
credit you in the commit (using commited by/authored by functionality). Or request anonymous contribution.
If you will not specify anything I will mention author in the commit message and link your answer.

Back to explaining the program: [StreetComplete](https://github.com/westnordost/StreetComplete/) is an editor of [OpenStreetMap](https://www.openstreetmap.org/) database. Usually its is the complex
task that requires at least 10 minute tutorial to start contributing anything at all.
StreetComplete is intended to be usable by regular humans and asks simple to answer questions.

OpenStreetMap is an openly licended geographic database where objects are represented by

(1) geometries - lines/ways/areas/etc.
(2) tags describing type of object. For example
    - `highway=motorway` marking line as a [motorway carriageway centerline](https://www.openstreetmap.org/way/235119521#map=19/53.86441/18.63070)
    - `waterway=river` marking line as a [river centerline](https://www.openstreetmap.org/way/633484436#map=14/64.0951/-19.9610)
    - `highway=bus_stop` marking point or areas as a [bus stop](https://www.openstreetmap.org/node/810564891#map=19/52.51346/13.40611)
    - `amenity=place_of_worship` marking, well, [place of worship](https://www.openstreetmap.org/relation/3374342#map=19/41.89862/12.47687)
    - `religion=sikh` typically added to place of worhip, marking it as used by a given religion
    - `barrier=gate` marking gate
    - and so on with https://wiki.openstreetmap.org/wiki/ and https://taginfo.openstreetmap.org/

StreetComplete, like other OSM editors is editing tags. Typical edit is adding `surface=asphalt` to mark road surface or `name=Żółta` to mark street name, based on what someone surveyed.
This happens in a way a bit invisible to humans, as tags are not exposed prominently to mappers.

But other more experience mappers may want to know what kind of edits StreetComplete is doing!

One of standard ways to document this is using Taginfo project listing - projects can publish a [simple .json file](https://wiki.openstreetmap.org/wiki/Taginfo/Projects),
with results presented at Taginfo site (used by more experienced OSM mappers and developers).

See for example https://taginfo.openstreetmap.org/keys/addr%3Aplace#projects listing projects which
listed add:place key as relevant.

So, this is project will parse source code files of StreetComplete project to detect what kind of changes this editor will make to OpenStreetMap database,
and will generate .json file understendable by Taginfo project.

The goal is to list all tags which can be added or edited by this editor (tags which can be removed or are used in filtering are skipped).

For example java/de/westnordost/streetcomplete/quests/toilets_fee/AddToiletsFee.kt defines quest asking whether public toilet is paid.
There are many parts defined there, for example that this quest is disable in USA and Canada. But for documenting tags
function relevant

```
    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["fee"] = answer.toYesNo()
    }
```

in this case key `fee` can be set to either `fee=yes` or `fee=no`

There are over 100 other quests, some significantly more complex.

This code relies on editing functionality being defined in applyAnswerTo function
and always involving modification of tags variable and several other assumptions

This is NOT a pure parsing, in several places shortcuts were taken to reduce implementation effort
while still working.

As this is tightly coupled to StreetComplete, many assumptions can be made.

In some cases I gave up completely and hardcoded answers - I am not asking here
to change this, some places remain with TODOs but overall code works fine and outputs correct answers.

This is the third attempt!

Maintaining such list manually is too time-consuming and too boring. There was an attempt to do this but it failed. See https://github.com/goldfndr/StreetCompleteJSON

There was also attempt to do this with regular expressions. It also failed. See https://github.com/streetcomplete/StreetComplete/pull/2754

This is the third attempt, it works, but code likely can be far better.
*/

// getEnumValuesDefinedInThisFilepath should return list of field -> setOfPossibleValues
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

@OptIn(ExperimentalSerializationApi::class) // needed by explicitNulls = false
open class UpdateTaginfoListingTask : DefaultTask() {

    @get:Input var targetDir: String? = null

    companion object {
        const val NAME_OF_FUNCTION_EDITING_TAGS = "applyAnswerTo"
        const val KOTLIN_IMPORT_ROOT_WITH_SLASH_ENDING = "app/src/main/java/"
        const val QUEST_ROOT_WITH_SLASH_ENDING = "app/src/main/java/de/westnordost/streetcomplete/quests/"
        const val COUNTRY_METADATA_PATH_WITH_SLASH_ENDING = "app/src/main/assets/country_metadata/"
        // is it possible to use directly SC constant?
        // import de.westnordost.streetcomplete.osm.SURVEY_MARK_KEY
        const val SURVEY_MARK_KEY = "check_date"
        const val VIBRATING_BUTTON = "traffic_signals:vibration"
        private const val SOUND_SIGNALS = "traffic_signals:sound"
        val EXPECTED_TAG_PER_QUEST = mapOf(
            "accepts_cards/AddAcceptsCards.kt" to setOf(Tag("payment:debit_cards", "yes"), Tag("payment:debit_cards", "no"), Tag("payment:credit_cards", "yes"), Tag("payment:credit_cards", "no")),
            "accepts_cash/AddAcceptsCash.kt" to setOf(Tag("payment:cash", "yes"), Tag("payment:cash", "no")),
            "address/AddHousenumber.kt" to setOf(Tag("addr:conscriptionnumber", null), Tag("addr:streetnumber", null), Tag("addr:housenumber", null), Tag("addr:block_number", null), Tag("addr:housename", null), Tag("nohousenumber", "yes"), Tag("building", "yes")),
            "address/AddAddressStreet.kt" to setOf(Tag("addr:street", null), Tag("addr:place", null)),
            "atm_operator/AddAtmOperator.kt" to setOf(Tag("operator", null)),
            "air_conditioning/AddAirConditioning.kt" to setOf(Tag("air_conditioning", "yes"), Tag("air_conditioning", "no")),
            "air_pump/AddBicyclePump.kt" to setOf(Tag("check_date:service:bicycle:pump", null), Tag("service:bicycle:pump", "yes"), Tag("service:bicycle:pump", "no")),
            "air_pump/AddAirCompressor.kt" to setOf(Tag("check_date:compressed_air", null), Tag("compressed_air", "yes"), Tag("compressed_air", "no")),
            "baby_changing_table/AddBabyChangingTable.kt" to setOf(Tag("changing_table", "yes"), Tag("changing_table", "no")),
            "barrier_bicycle_barrier_type/AddBicycleBarrierType.kt" to setOf(Tag("cycle_barrier", "single"), Tag("cycle_barrier", "double"), Tag("cycle_barrier", "triple"), Tag("cycle_barrier", "diagonal"), Tag("cycle_barrier", "tilted"), Tag("barrier", "yes")),
            "barrier_type/AddBarrierOnPath.kt" to setOf(Tag("barrier", "entrance"), Tag("barrier", "gate"), Tag("barrier", "lift_gate"), Tag("barrier", "swing_gate"), Tag("barrier", "bollard"), Tag("barrier", "chain"), Tag("barrier", "rope"), Tag("barrier", "hampshire_gate"), Tag("barrier", "cattle_grid"), Tag("barrier", "block"), Tag("barrier", "jersey_barrier"), Tag("barrier", "log"), Tag("barrier", "kerb"), Tag("barrier", "height_restrictor"), Tag("barrier", "full-height_turnstile"), Tag("barrier", "turnstile"), Tag("barrier", "debris"), Tag("barrier", "stile"), Tag("barrier", "kissing_gate"), Tag("barrier", "cycle_barrier"), Tag("stile", "squeezer"), Tag("stile", "ladder"), Tag("stile", "stepover"), Tag("material", "wood"), Tag("material", "stone")),
            "barrier_type/AddBarrierOnRoad.kt" to setOf(Tag("barrier", "entrance"), Tag("barrier", "gate"), Tag("barrier", "lift_gate"), Tag("barrier", "swing_gate"), Tag("barrier", "bollard"), Tag("barrier", "chain"), Tag("barrier", "rope"), Tag("barrier", "hampshire_gate"), Tag("barrier", "cattle_grid"), Tag("barrier", "block"), Tag("barrier", "jersey_barrier"), Tag("barrier", "log"), Tag("barrier", "kerb"), Tag("barrier", "height_restrictor"), Tag("barrier", "full-height_turnstile"), Tag("barrier", "turnstile"), Tag("barrier", "debris"), Tag("barrier", "stile"), Tag("barrier", "kissing_gate"), Tag("barrier", "cycle_barrier"), Tag("stile", "squeezer"), Tag("stile", "ladder"), Tag("stile", "stepover"), Tag("material", "wood"), Tag("material", "stone")),
            "barrier_type/AddBarrierType.kt" to setOf(Tag("barrier", "entrance"), Tag("barrier", "gate"), Tag("barrier", "lift_gate"), Tag("barrier", "swing_gate"), Tag("barrier", "bollard"), Tag("barrier", "chain"), Tag("barrier", "rope"), Tag("barrier", "hampshire_gate"), Tag("barrier", "cattle_grid"), Tag("barrier", "block"), Tag("barrier", "jersey_barrier"), Tag("barrier", "log"), Tag("barrier", "kerb"), Tag("barrier", "height_restrictor"), Tag("barrier", "full-height_turnstile"), Tag("barrier", "turnstile"), Tag("barrier", "debris"), Tag("barrier", "stile"), Tag("barrier", "kissing_gate"), Tag("barrier", "cycle_barrier"), Tag("stile", "squeezer"), Tag("stile", "ladder"), Tag("stile", "stepover"), Tag("material", "wood"), Tag("material", "stone")),
            "barrier_type/AddStileType.kt" to setOf(Tag("barrier", "kissing_gate"), Tag("barrier", "entrance"), Tag("barrier", "gate"), Tag("stile", "squeezer"), Tag("stile", "ladder"), Tag("stile", "stepover"), Tag("material", "wood"), Tag("material", "stone")),
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
            "building_type/AddBuildingType.kt" to setOf(Tag("building", "house"), Tag("building", "apartments"), Tag("building", "detached"), Tag("building", "semidetached_house"), Tag("building", "terrace"), Tag("building", "hotel"), Tag("building", "dormitory"), Tag("building", "houseboat"), Tag("building", "bungalow"), Tag("building", "static_caravan"), Tag("building", "hut"), Tag("building", "industrial"), Tag("building", "retail"), Tag("building", "office"), Tag("building", "warehouse"), Tag("building", "kiosk"), Tag("man_made", "storage_tank"), Tag("building", "kindergarten"), Tag("building", "school"), Tag("building", "college"), Tag("building", "sports_centre"), Tag("building", "hospital"), Tag("building", "stadium"), Tag("building", "grandstand"), Tag("building", "train_station"), Tag("building", "transportation"), Tag("building", "fire_station"), Tag("building", "university"), Tag("building", "government"), Tag("building", "church"), Tag("building", "chapel"), Tag("building", "cathedral"), Tag("building", "mosque"), Tag("building", "temple"), Tag("building", "pagoda"), Tag("building", "synagogue"), Tag("building", "shrine"), Tag("building", "carport"), Tag("building", "garage"), Tag("building", "garages"), Tag("building", "parking"), Tag("building", "farm"), Tag("building", "farm_auxiliary"), Tag("man_made", "silo"), Tag("building", "greenhouse"), Tag("building", "shed"), Tag("building", "allotment_house"), Tag("building", "roof"), Tag("building", "bridge"), Tag("building", "toilets"), Tag("building", "service"), Tag("building", "hangar"), Tag("building", "bunker"), Tag("building", "boathouse"), Tag("historic", "yes"), Tag("abandoned", "yes"), Tag("ruins", "yes"), Tag("building", "residential"), Tag("building", "commercial"), Tag("building", "civic"), Tag("building", "religious"), Tag("building", "construction")),
            "building_underground/AddIsBuildingUnderground.kt" to setOf(Tag("location", "underground"), Tag("location", "surface")),
            "bus_stop_bench/AddBenchStatusOnBusStop.kt" to setOf(Tag("check_date:bench", null), Tag("bench", "yes"), Tag("bench", "no")),
            "bus_stop_bin/AddBinStatusOnBusStop.kt" to setOf(Tag("check_date:bin", null), Tag("bin", "yes"), Tag("bin", "no")),
            "bus_stop_lit/AddBusStopLit.kt" to setOf(Tag("check_date:lit", null), Tag("lit", "yes"), Tag("lit", "no")),
            "bus_stop_name/AddBusStopName.kt" to setOf(Tag("name:signed", "no"), Tag("name", null), Tag("int_name", null), Tag("name:ar", null), Tag("name:en", null), Tag("name:kha", null), Tag("name:grt", null), Tag("name:uz", null), Tag("name:ru", null), Tag("name:mnk", null), Tag("name:ff", null), Tag("name:cy", null), Tag("name:gd", null), Tag("name:fr", null), Tag("name:snk", null), Tag("name:wo", null), Tag("name:sw", null), Tag("name:fil", null), Tag("name:es", null), Tag("name:ceb", null), Tag("name:ilo", null), Tag("name:hil", null), Tag("name:nl", null), Tag("name:pap", null), Tag("name:mg", null), Tag("name:ms", null), Tag("name:hi", null), Tag("name:pa", null), Tag("name:zh", null), Tag("name:adx", null), Tag("name:kk", null), Tag("name:tpi", null), Tag("name:ho", null), Tag("name:pt", null), Tag("name:pov", null), Tag("name:my", null), Tag("name:pih", null), Tag("name:bn", null), Tag("name:te", null), Tag("name:mr", null), Tag("name:ta", null), Tag("name:ur", null), Tag("name:gu", null), Tag("name:kn", null), Tag("name:ml", null), Tag("name:or", null), Tag("name:as", null), Tag("name:mai", null), Tag("name:ks", null), Tag("name:emk", null), Tag("name:sus", null), Tag("name:gyn", null), Tag("name:it", null), Tag("name:de", null), Tag("name:bo", null), Tag("name:mfe", null), Tag("name:qu", null), Tag("name:ny", null), Tag("name:nb", null), Tag("name:no", null), Tag("name:af", null), Tag("name:so", null), Tag("name:mkw", null), Tag("name:ln", null), Tag("name:lua", null), Tag("name:et", null), Tag("name:fan", null), Tag("name:doi", null), Tag("name:tvl", null), Tag("name:oc", null), Tag("name:ca", null), Tag("name:br", null), Tag("name:id", null), Tag("name:jv", null), Tag("name:su", null), Tag("name:mad", null), Tag("name:min", null), Tag("name:sq", null), Tag("name:sr", null), Tag("name:trp", null), Tag("name:ug", null), Tag("name:hy", null), Tag("name:ber", null), Tag("name:tkl", null), Tag("name:tn", null), Tag("name:myn", null), Tag("name:ky", null), Tag("name:mn", null), Tag("name:gv", null), Tag("name:el", null), Tag("name:tr", null), Tag("name:mos", null), Tag("name:dyu", null), Tag("name:st", null), Tag("name:aa", null), Tag("name:bg", null), Tag("name:mh", null), Tag("name:fa", null), Tag("name:ps", null), Tag("name:eu", null), Tag("name:bs", null), Tag("name:hr", null), Tag("name:to", null), Tag("name:lb", null), Tag("name:ay", null), Tag("name:na", null), Tag("name:heb", null), Tag("name:gil", null), Tag("name:jam", null), Tag("name:brx", null), Tag("name:za", null), Tag("name:ko", null), Tag("name:ko-Latn", null), Tag("name:kok", null), Tag("name:mk", null), Tag("name:niu", null), Tag("name:fo", null), Tag("name:dk", null), Tag("name:bi", null), Tag("name:gug", null), Tag("name:pau", null), Tag("name:gl", null), Tag("name:tg", null), Tag("name:bem", null), Tag("name:ha", null), Tag("name:rm", null), Tag("name:nn", null), Tag("name:bzj", null), Tag("name:kri", null), Tag("name:fi", null), Tag("name:sv", null), Tag("name:rw", null), Tag("name:rn", null), Tag("name:km", null), Tag("name:sg", null), Tag("name:kl", null), Tag("name:da", null), Tag("name:am", null), Tag("name:om", null), Tag("name:ti", null), Tag("name:ak", null), Tag("name:ee", null), Tag("name:crs", null), Tag("name:bm", null), Tag("name:sn", null), Tag("name:nd", null), Tag("name:fj", null), Tag("name:hif", null), Tag("name:rar", null), Tag("name:ss", null), Tag("name:tk", null), Tag("name:kea", null), Tag("name:bci", null), Tag("name:any", null), Tag("name:lo", null), Tag("name:swb", null), Tag("name:wni", null), Tag("name:zdj", null), Tag("name:wlc", null), Tag("name:tet", null), Tag("name:ja", null), Tag("name:ja-Latn", null), Tag("name:sm", null), Tag("name:lus", null), Tag("name:ch", null), Tag("name:ka", null), Tag("name:dgr", null), Tag("name:den", null), Tag("name:ne", null), Tag("name:fon", null), Tag("name:be", null), Tag("name:ht", null), Tag("name:ga", null), Tag("name:mt", null), Tag("name:chk", null), Tag("name:pon", null), Tag("name:uk", null), Tag("name:zu", null), Tag("name:xh", null), Tag("name:nso", null), Tag("name:ts", null), Tag("name:si", null), Tag("name:iu", null), Tag("name:th", null), Tag("name:th-Latn", null), Tag("name:sid", null), Tag("name:wal", null), Tag("name:ast", null)),
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
            "cycleway/AddCycleway.kt" to setOf(Tag("sidewalk", "both"), Tag("cycleway:both", "no"), Tag("cycleway:both", "lane"), Tag("cycleway:both:oneway", "yes"), Tag("cycleway:both:lane", "exclusive"), Tag("cycleway:both:lane", "advisory"), Tag("cycleway:both", "track"), Tag("cycleway:both:segregated", "yes"), Tag("cycleway:both:oneway", "no"), Tag("cycleway:both:segregated", "no"), Tag("cycleway:both", "shared_lane"), Tag("cycleway:both:lane", "pictogram"), Tag("cycleway:both", "share_busway"), Tag("cycleway:both", "separate"), Tag("cycleway:both:oneway", "-1"), Tag("cycleway:left", "no"), Tag("cycleway:left", "lane"), Tag("cycleway:left:oneway", "yes"), Tag("cycleway:left:lane", "exclusive"), Tag("cycleway:left:lane", "advisory"), Tag("cycleway:left", "track"), Tag("cycleway:left:segregated", "yes"), Tag("cycleway:left:oneway", "no"), Tag("cycleway:left:segregated", "no"), Tag("cycleway:left", "shared_lane"), Tag("cycleway:left:lane", "pictogram"), Tag("cycleway:left", "share_busway"), Tag("cycleway:left", "separate"), Tag("cycleway:left:oneway", "-1"), Tag("cycleway:right", "no"), Tag("cycleway:right", "lane"), Tag("cycleway:right:oneway", "yes"), Tag("cycleway:right:lane", "exclusive"), Tag("cycleway:right:lane", "advisory"), Tag("cycleway:right", "track"), Tag("cycleway:right:segregated", "yes"), Tag("cycleway:right:oneway", "no"), Tag("cycleway:right:segregated", "no"), Tag("cycleway:right", "shared_lane"), Tag("cycleway:right:lane", "pictogram"), Tag("cycleway:right", "share_busway"), Tag("cycleway:right", "separate"), Tag("cycleway:right:oneway", "-1"), Tag("oneway:bicycle", "no"), Tag("check_date:cycleway", null)),
            "defibrillator/AddIsDefibrillatorIndoor.kt" to setOf(Tag("indoor", "yes"), Tag("indoor", "no")),
            "diet_type/AddVegan.kt" to setOf(Tag("food", "no"), Tag("check_date:diet:vegan", null), Tag("diet:vegan", "yes"), Tag("diet:vegan", "no"), Tag("diet:vegan", "only")),
            "diet_type/AddVegetarian.kt" to setOf(Tag("food", "no"), Tag("check_date:diet:vegetarian", null), Tag("diet:vegetarian", "yes"), Tag("diet:vegetarian", "no"), Tag("diet:vegetarian", "only")),
            "diet_type/AddHalal.kt" to setOf(Tag("food", "no"), Tag("check_date:diet:halal", null), Tag("diet:halal", "yes"), Tag("diet:halal", "no"), Tag("diet:halal", "only")),
            "diet_type/AddKosher.kt" to setOf(Tag("food", "no"), Tag("check_date:diet:kosher", null), Tag("diet:kosher", "yes"), Tag("diet:kosher", "no"), Tag("diet:kosher", "only")),
            "drinking_water/AddDrinkingWater.kt" to setOf(Tag("drinking_water", "no"), Tag("drinking_water", "yes"), Tag("drinking_water:legal", "no"), Tag("drinking_water:legal", "yes")),
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
            "max_speed/AddMaxSpeed.kt" to setOf(Tag("maxspeed", null), Tag("maxspeed:type", "sign"), Tag("maxspeed:type", null), Tag("maxspeed:advisory", null), Tag("maxspeed:type:advisory", "sign"), Tag("highway", "living_street"), Tag("lit", "yes"), Tag("lit", "no")),
            "max_weight/AddMaxWeight.kt" to setOf(Tag("maxweight:signed", "no"), Tag("maxweight", null), Tag("maxweightrating", null), Tag("maxaxleload", null), Tag("maxbogieweight", "null")),
            "memorial_type/AddMemorialType.kt" to setOf(Tag("memorial", "statue"), Tag("memorial", "bust"), Tag("memorial", "plaque"), Tag("memorial", "war_memorial"), Tag("memorial", "stone"), Tag("memorial", "obelisk"), Tag("memorial", "stele"), Tag("memorial", "sculpture"), Tag("material", "wood"), Tag("material", "stone")),
            "motorcycle_parking_capacity/AddMotorcycleParkingCapacity.kt" to setOf(Tag("check_date:capacity", null), Tag("capacity", null)),
            "motorcycle_parking_cover/AddMotorcycleParkingCover.kt" to setOf(Tag("covered", "yes"), Tag("covered", "no")),
            "oneway/AddOneway.kt" to setOf(Tag("oneway", "yes"), Tag("oneway", "-1"), Tag("oneway", "no")),
            "opening_hours/AddOpeningHours.kt" to setOf(Tag("opening_hours:signed", "no"), Tag("check_date:opening_hours", null), Tag("opening_hours", null)),
            "opening_hours_signed/CheckOpeningHoursSigned.kt" to setOf(Tag("opening_hours:signed", "no"), Tag("check_date:opening_hours", null)),
            "orchard_produce/AddOrchardProduce.kt" to setOf(Tag("produce", "sisal"), Tag("produce", "grape"), Tag("produce", "agave"), Tag("produce", "almond"), Tag("produce", "apple"), Tag("produce", "apricot"), Tag("produce", "areca_nut"), Tag("produce", "avocado"), Tag("produce", "banana"), Tag("produce", "sweet_pepper"), Tag("produce", "blueberry"), Tag("produce", "brazil_nut"), Tag("produce", "cacao"), Tag("produce", "cashew"), Tag("produce", "cherry"), Tag("produce", "chestnut"), Tag("produce", "chilli_pepper"), Tag("produce", "coconut"), Tag("produce", "coffee"), Tag("produce", "cranberry"), Tag("produce", "date"), Tag("produce", "fig"), Tag("produce", "grapefruit"), Tag("produce", "guava"), Tag("produce", "hazelnut"), Tag("produce", "hop"), Tag("produce", "jojoba"), Tag("produce", "kiwi"), Tag("produce", "kola_nut"), Tag("produce", "lemon"), Tag("produce", "lime"), Tag("produce", "mango"), Tag("produce", "mangosteen"), Tag("produce", "mate"), Tag("produce", "nutmeg"), Tag("produce", "olive"), Tag("produce", "orange"), Tag("produce", "palm_oil"), Tag("produce", "papaya"), Tag("produce", "peach"), Tag("produce", "pear"), Tag("produce", "pepper"), Tag("produce", "persimmon"), Tag("produce", "pineapple"), Tag("produce", "pistachio"), Tag("produce", "plum"), Tag("produce", "raspberry"), Tag("produce", "rubber"), Tag("produce", "strawberry"), Tag("produce", "tea"), Tag("produce", "tomato"), Tag("produce", "tung_nut"), Tag("produce", "vanilla"), Tag("produce", "walnut"), Tag("produce", null), Tag("landuse", "farmland"), Tag("landuse", "vineyard")),
            "picnic_table_cover/AddPicnicTableCover.kt" to setOf(Tag("covered", "yes"), Tag("covered", "no")),
            "parking_access/AddParkingAccess.kt" to setOf(Tag("access", "yes"), Tag("access", "customers"), Tag("access", "private")),
            "parking_access/AddBikeParkingAccess.kt" to setOf(Tag("access", "yes"), Tag("access", "customers"), Tag("access", "private")),
            "parking_fee/AddBikeParkingFee.kt" to setOf(Tag("fee:conditional", null), Tag("check_date:fee", null), Tag("fee", "yes"), Tag("fee", "no"), Tag("maxstay:conditional", null), Tag("check_date:maxstay", null), Tag("maxstay", null), Tag("maxstay", "no")),
            "parking_fee/AddParkingFee.kt" to setOf(Tag("fee:conditional", null), Tag("check_date:fee", null), Tag("fee", "yes"), Tag("fee", "no"), Tag("maxstay:conditional", null), Tag("check_date:maxstay", null), Tag("maxstay", null), Tag("maxstay", "no")),
            "parking_type/AddParkingType.kt" to setOf(Tag("parking", "surface"), Tag("parking", "street_side"), Tag("parking", "lane"), Tag("parking", "underground"), Tag("parking", "multi-storey")),
            "pitch_lit/AddPitchLit.kt" to setOf(Tag("check_date:lit", null), Tag("lit", "yes"), Tag("lit", "no")),
            "place_name/AddPlaceName.kt" to setOf(Tag("name:signed", "no"), Tag("name", null), Tag("int_name", null), Tag("name:ar", null), Tag("name:en", null), Tag("name:kha", null), Tag("name:grt", null), Tag("name:uz", null), Tag("name:ru", null), Tag("name:mnk", null), Tag("name:ff", null), Tag("name:cy", null), Tag("name:gd", null), Tag("name:fr", null), Tag("name:snk", null), Tag("name:wo", null), Tag("name:sw", null), Tag("name:fil", null), Tag("name:es", null), Tag("name:ceb", null), Tag("name:ilo", null), Tag("name:hil", null), Tag("name:nl", null), Tag("name:pap", null), Tag("name:mg", null), Tag("name:ms", null), Tag("name:hi", null), Tag("name:pa", null), Tag("name:zh", null), Tag("name:adx", null), Tag("name:kk", null), Tag("name:tpi", null), Tag("name:ho", null), Tag("name:pt", null), Tag("name:pov", null), Tag("name:my", null), Tag("name:pih", null), Tag("name:bn", null), Tag("name:te", null), Tag("name:mr", null), Tag("name:ta", null), Tag("name:ur", null), Tag("name:gu", null), Tag("name:kn", null), Tag("name:ml", null), Tag("name:or", null), Tag("name:as", null), Tag("name:mai", null), Tag("name:ks", null), Tag("name:emk", null), Tag("name:sus", null), Tag("name:gyn", null), Tag("name:it", null), Tag("name:de", null), Tag("name:bo", null), Tag("name:mfe", null), Tag("name:qu", null), Tag("name:ny", null), Tag("name:nb", null), Tag("name:no", null), Tag("name:af", null), Tag("name:so", null), Tag("name:mkw", null), Tag("name:ln", null), Tag("name:lua", null), Tag("name:et", null), Tag("name:fan", null), Tag("name:doi", null), Tag("name:tvl", null), Tag("name:oc", null), Tag("name:ca", null), Tag("name:br", null), Tag("name:id", null), Tag("name:jv", null), Tag("name:su", null), Tag("name:mad", null), Tag("name:min", null), Tag("name:sq", null), Tag("name:sr", null), Tag("name:trp", null), Tag("name:ug", null), Tag("name:hy", null), Tag("name:ber", null), Tag("name:tkl", null), Tag("name:tn", null), Tag("name:myn", null), Tag("name:ky", null), Tag("name:mn", null), Tag("name:gv", null), Tag("name:el", null), Tag("name:tr", null), Tag("name:mos", null), Tag("name:dyu", null), Tag("name:st", null), Tag("name:aa", null), Tag("name:bg", null), Tag("name:mh", null), Tag("name:fa", null), Tag("name:ps", null), Tag("name:eu", null), Tag("name:bs", null), Tag("name:hr", null), Tag("name:to", null), Tag("name:lb", null), Tag("name:ay", null), Tag("name:na", null), Tag("name:heb", null), Tag("name:gil", null), Tag("name:jam", null), Tag("name:brx", null), Tag("name:za", null), Tag("name:ko", null), Tag("name:ko-Latn", null), Tag("name:kok", null), Tag("name:mk", null), Tag("name:niu", null), Tag("name:fo", null), Tag("name:dk", null), Tag("name:bi", null), Tag("name:gug", null), Tag("name:pau", null), Tag("name:gl", null), Tag("name:tg", null), Tag("name:bem", null), Tag("name:ha", null), Tag("name:rm", null), Tag("name:nn", null), Tag("name:bzj", null), Tag("name:kri", null), Tag("name:fi", null), Tag("name:sv", null), Tag("name:rw", null), Tag("name:rn", null), Tag("name:km", null), Tag("name:sg", null), Tag("name:kl", null), Tag("name:da", null), Tag("name:am", null), Tag("name:om", null), Tag("name:ti", null), Tag("name:ak", null), Tag("name:ee", null), Tag("name:crs", null), Tag("name:bm", null), Tag("name:sn", null), Tag("name:nd", null), Tag("name:fj", null), Tag("name:hif", null), Tag("name:rar", null), Tag("name:ss", null), Tag("name:tk", null), Tag("name:kea", null), Tag("name:bci", null), Tag("name:any", null), Tag("name:lo", null), Tag("name:swb", null), Tag("name:wni", null), Tag("name:zdj", null), Tag("name:wlc", null), Tag("name:tet", null), Tag("name:ja", null), Tag("name:ja-Latn", null), Tag("name:sm", null), Tag("name:lus", null), Tag("name:ch", null), Tag("name:ka", null), Tag("name:dgr", null), Tag("name:den", null), Tag("name:ne", null), Tag("name:fon", null), Tag("name:be", null), Tag("name:ht", null), Tag("name:ga", null), Tag("name:mt", null), Tag("name:chk", null), Tag("name:pon", null), Tag("name:uk", null), Tag("name:zu", null), Tag("name:xh", null), Tag("name:nso", null), Tag("name:ts", null), Tag("name:si", null), Tag("name:iu", null), Tag("name:th", null), Tag("name:th-Latn", null), Tag("name:sid", null), Tag("name:wal", null), Tag("name:ast", null)),
            "playground_access/AddPlaygroundAccess.kt" to setOf(Tag("access", "yes"), Tag("access", "customers"), Tag("access", "private")),
            "police_type/AddPoliceType.kt" to setOf(Tag("operator", "Arma dei Carabinieri"), Tag("operator", "Polizia di Stato"), Tag("operator", "Guardia di Finanza"), Tag("operator", "Polizia Municipale"), Tag("operator", "Polizia Locale"), Tag("operator", "Guardia Costiera"), Tag("operator:wikidata", "Q54852"), Tag("operator:wikidata", "Q897817"), Tag("operator:wikidata", "Q1552861"), Tag("operator:wikidata", "Q1431981"), Tag("operator:wikidata", "Q61634147"), Tag("operator:wikidata", "Q1552839")),
            "postbox_ref/AddPostboxRef.kt" to setOf(Tag("ref:signed", "no"), Tag("ref", null)),
            "postbox_collection_times/AddPostboxCollectionTimes.kt" to setOf(Tag("collection_times:signed", "no"), Tag("check_date:collection_times", null), Tag("collection_times", null)),
            "postbox_royal_cypher/AddPostboxRoyalCypher.kt" to setOf(Tag("royal_cypher", "VR"), Tag("royal_cypher", "EVIIR"), Tag("royal_cypher", "GR"), Tag("royal_cypher", "EVIIIR"), Tag("royal_cypher", "GVIR"), Tag("royal_cypher", "EIIR"), Tag("royal_cypher", "scottish_crown"), Tag("royal_cypher", "no")),
            "powerpoles_material/AddPowerPolesMaterial.kt" to setOf(Tag("material", "wood"), Tag("material", "steel"), Tag("material", "concrete")),
            "railway_crossing/AddRailwayCrossingBarrier.kt" to setOf(Tag("crossing:chicane", "yes"), Tag("check_date:crossing:barrier", null), Tag("crossing:barrier", "no"), Tag("crossing:barrier", "half"), Tag("crossing:barrier", "double_half"), Tag("crossing:barrier", "full"), Tag("crossing:barrier", "gate")),
            "recycling/AddRecyclingType.kt" to setOf(Tag("recycling_type", "centre"), Tag("recycling_type", "container"), Tag("location", "overground"), Tag("location", "underground")),
            "recycling_glass/DetermineRecyclingGlass.kt" to setOf(Tag("recycling:glass_bottles", "yes"), Tag("recycling:glass", "no")),
            "recycling_material/AddRecyclingContainerMaterials.kt" to setOf(Tag("recycling:glass_bottles", "yes"), Tag("recycling:glass", "yes"), Tag("recycling:paper", "yes"), Tag("recycling:plastic", "yes"), Tag("recycling:plastic_packaging", "yes"), Tag("recycling:plastic_bottles", "yes"), Tag("recycling:beverage_cartons", "yes"), Tag("recycling:cans", "yes"), Tag("recycling:scrap_metal", "yes"), Tag("recycling:clothes", "yes"), Tag("recycling:shoes", "yes"), Tag("recycling:small_electrical_appliances", "yes"), Tag("recycling:batteries", "yes"), Tag("recycling:green_waste", "yes"), Tag("recycling:cooking_oil", "yes"), Tag("recycling:engine_oil", "yes"), Tag("amenity", "waste_disposal"), Tag("recycling:plastic", "no"), Tag("recycling:plastic_packaging", "no"), Tag("recycling:plastic_bottles", "no"), Tag("recycling:beverage_cartons", "no"), Tag("check_date:recycling", null)),
            "religion/AddReligionToPlaceOfWorship.kt" to setOf(Tag("religion", "christian"), Tag("religion", "muslim"), Tag("religion", "buddhist"), Tag("religion", "hindu"), Tag("religion", "jewish"), Tag("religion", "chinese_folk"), Tag("religion", "animist"), Tag("religion", "bahai"), Tag("religion", "sikh"), Tag("religion", "taoist"), Tag("religion", "jain"), Tag("religion", "shinto"), Tag("religion", "caodaism"), Tag("religion", "multifaith")),
            "religion/AddReligionToWaysideShrine.kt" to setOf(Tag("religion", "christian"), Tag("religion", "muslim"), Tag("religion", "buddhist"), Tag("religion", "hindu"), Tag("religion", "jewish"), Tag("religion", "chinese_folk"), Tag("religion", "animist"), Tag("religion", "bahai"), Tag("religion", "sikh"), Tag("religion", "taoist"), Tag("religion", "jain"), Tag("religion", "shinto"), Tag("religion", "caodaism"), Tag("religion", "multifaith")),
            "road_name/AddRoadName.kt" to setOf(Tag("name", null), Tag("int_name", null), Tag("name:ar", null), Tag("name:en", null), Tag("name:kha", null), Tag("name:grt", null), Tag("name:uz", null), Tag("name:ru", null), Tag("name:mnk", null), Tag("name:ff", null), Tag("name:cy", null), Tag("name:gd", null), Tag("name:fr", null), Tag("name:snk", null), Tag("name:wo", null), Tag("name:sw", null), Tag("name:fil", null), Tag("name:es", null), Tag("name:ceb", null), Tag("name:ilo", null), Tag("name:hil", null), Tag("name:nl", null), Tag("name:pap", null), Tag("name:mg", null), Tag("name:ms", null), Tag("name:hi", null), Tag("name:pa", null), Tag("name:zh", null), Tag("name:adx", null), Tag("name:kk", null), Tag("name:tpi", null), Tag("name:ho", null), Tag("name:pt", null), Tag("name:pov", null), Tag("name:my", null), Tag("name:pih", null), Tag("name:bn", null), Tag("name:te", null), Tag("name:mr", null), Tag("name:ta", null), Tag("name:ur", null), Tag("name:gu", null), Tag("name:kn", null), Tag("name:ml", null), Tag("name:or", null), Tag("name:as", null), Tag("name:mai", null), Tag("name:ks", null), Tag("name:emk", null), Tag("name:sus", null), Tag("name:gyn", null), Tag("name:it", null), Tag("name:de", null), Tag("name:bo", null), Tag("name:mfe", null), Tag("name:qu", null), Tag("name:ny", null), Tag("name:nb", null), Tag("name:no", null), Tag("name:af", null), Tag("name:so", null), Tag("name:mkw", null), Tag("name:ln", null), Tag("name:lua", null), Tag("name:et", null), Tag("name:fan", null), Tag("name:doi", null), Tag("name:tvl", null), Tag("name:oc", null), Tag("name:ca", null), Tag("name:br", null), Tag("name:id", null), Tag("name:jv", null), Tag("name:su", null), Tag("name:mad", null), Tag("name:min", null), Tag("name:sq", null), Tag("name:sr", null), Tag("name:trp", null), Tag("name:ug", null), Tag("name:hy", null), Tag("name:ber", null), Tag("name:tkl", null), Tag("name:tn", null), Tag("name:myn", null), Tag("name:ky", null), Tag("name:mn", null), Tag("name:gv", null), Tag("name:el", null), Tag("name:tr", null), Tag("name:mos", null), Tag("name:dyu", null), Tag("name:st", null), Tag("name:aa", null), Tag("name:bg", null), Tag("name:mh", null), Tag("name:fa", null), Tag("name:ps", null), Tag("name:eu", null), Tag("name:bs", null), Tag("name:hr", null), Tag("name:to", null), Tag("name:lb", null), Tag("name:ay", null), Tag("name:na", null), Tag("name:heb", null), Tag("name:gil", null), Tag("name:jam", null), Tag("name:brx", null), Tag("name:za", null), Tag("name:ko", null), Tag("name:ko-Latn", null), Tag("name:kok", null), Tag("name:mk", null), Tag("name:niu", null), Tag("name:fo", null), Tag("name:dk", null), Tag("name:bi", null), Tag("name:gug", null), Tag("name:pau", null), Tag("name:gl", null), Tag("name:tg", null), Tag("name:bem", null), Tag("name:ha", null), Tag("name:rm", null), Tag("name:nn", null), Tag("name:bzj", null), Tag("name:kri", null), Tag("name:fi", null), Tag("name:sv", null), Tag("name:rw", null), Tag("name:rn", null), Tag("name:km", null), Tag("name:sg", null), Tag("name:kl", null), Tag("name:da", null), Tag("name:am", null), Tag("name:om", null), Tag("name:ti", null), Tag("name:ak", null), Tag("name:ee", null), Tag("name:crs", null), Tag("name:bm", null), Tag("name:sn", null), Tag("name:nd", null), Tag("name:fj", null), Tag("name:hif", null), Tag("name:rar", null), Tag("name:ss", null), Tag("name:tk", null), Tag("name:kea", null), Tag("name:bci", null), Tag("name:any", null), Tag("name:lo", null), Tag("name:swb", null), Tag("name:wni", null), Tag("name:zdj", null), Tag("name:wlc", null), Tag("name:tet", null), Tag("name:ja", null), Tag("name:ja-Latn", null), Tag("name:sm", null), Tag("name:lus", null), Tag("name:ch", null), Tag("name:ka", null), Tag("name:dgr", null), Tag("name:den", null), Tag("name:ne", null), Tag("name:fon", null), Tag("name:be", null), Tag("name:ht", null), Tag("name:ga", null), Tag("name:mt", null), Tag("name:chk", null), Tag("name:pon", null), Tag("name:uk", null), Tag("name:zu", null), Tag("name:xh", null), Tag("name:nso", null), Tag("name:ts", null), Tag("name:si", null), Tag("name:iu", null), Tag("name:th", null), Tag("name:th-Latn", null), Tag("name:sid", null), Tag("name:wal", null), Tag("name:ast", null), Tag("noname", "yes"), Tag("highway", "service"), Tag("highway", "track"), Tag("ref", null)),
            "roof_shape/AddRoofShape.kt" to setOf(Tag("roof:shape", "gabled"), Tag("roof:shape", "hipped"), Tag("roof:shape", "flat"), Tag("roof:shape", "pyramidal"), Tag("roof:shape", "half-hipped"), Tag("roof:shape", "skillion"), Tag("roof:shape", "gambrel"), Tag("roof:shape", "round"), Tag("roof:shape", "double_saltbox"), Tag("roof:shape", "saltbox"), Tag("roof:shape", "mansard"), Tag("roof:shape", "dome"), Tag("roof:shape", "quadruple_saltbox"), Tag("roof:shape", "round_gabled"), Tag("roof:shape", "onion"), Tag("roof:shape", "cone"), Tag("roof:shape", "many")),
            "seating/AddSeating.kt" to setOf(Tag("outdoor_seating", "yes"), Tag("outdoor_seating", "no"), Tag("indoor_seating", "yes"), Tag("indoor_seating", "no")),
            "segregated/AddCyclewaySegregation.kt" to setOf(Tag("check_date:segregated", null), Tag("segregated", "yes"), Tag("segregated", "no")),
            "self_service/AddSelfServiceLaundry.kt" to setOf(Tag("self_service", "no"), Tag("laundry_service", "yes"), Tag("self_service", "yes"), Tag("laundry_service", "no")),
            "shop_type/CheckShopType.kt" to setOf(Tag("check_date", null)), // NSI tags ignored, see https://github.com/streetcomplete/StreetComplete/issues/4225#issuecomment-1190487094
            "shoulder/AddShoulder.kt" to setOf(Tag("shoulder", "both"), Tag("shoulder", "left"), Tag("shoulder", "right"), Tag("shoulder", "no")),
            "sidewalk/AddSidewalk.kt" to setOf(Tag("sidewalk", "no"), Tag("sidewalk", "both"), Tag("sidewalk", "left"), Tag("sidewalk", "right"), Tag("sidewalk", "separate"), Tag("check_date:sidewalk", null), Tag("sidewalk:left", "no"), Tag("sidewalk:left", "yes"), Tag("sidewalk:left", "separate"), Tag("sidewalk:right", "no"), Tag("sidewalk:right", "yes"), Tag("sidewalk:right", "separate")),
            "smoking/AddSmoking.kt" to setOf(Tag("check_date:smoking", null), Tag("smoking", "yes"), Tag("smoking", "outside"), Tag("smoking", "no"), Tag("smoking", "separated")),
            "smoothness/AddPathSmoothness.kt" to setOf(Tag("highway", "steps"), Tag("check_date:smoothness", null), Tag("smoothness", "excellent"), Tag("smoothness", "good"), Tag("smoothness", "intermediate"), Tag("smoothness", "bad"), Tag("smoothness", "very_bad"), Tag("smoothness", "horrible"), Tag("smoothness", "very_horrible"), Tag("smoothness", "impassable")),
            "smoothness/AddRoadSmoothness.kt" to setOf(Tag("check_date:smoothness", null), Tag("smoothness", "excellent"), Tag("smoothness", "good"), Tag("smoothness", "intermediate"), Tag("smoothness", "bad"), Tag("smoothness", "very_bad"), Tag("smoothness", "horrible"), Tag("smoothness", "very_horrible"), Tag("smoothness", "impassable")),
            "sport/AddSport.kt" to setOf(Tag("sport", "multi"), Tag("sport", "soccer"), Tag("sport", "tennis"), Tag("sport", "basketball"), Tag("sport", "golf"), Tag("sport", "volleyball"), Tag("sport", "beachvolleyball"), Tag("sport", "skateboard"), Tag("sport", "shooting"), Tag("sport", "baseball"), Tag("sport", "athletics"), Tag("sport", "table_tennis"), Tag("sport", "gymnastics"), Tag("sport", "boules"), Tag("sport", "handball"), Tag("sport", "field_hockey"), Tag("sport", "ice_hockey"), Tag("sport", "american_football"), Tag("sport", "equestrian"), Tag("sport", "archery"), Tag("sport", "roller_skating"), Tag("sport", "badminton"), Tag("sport", "cricket"), Tag("sport", "rugby"), Tag("sport", "bowls"), Tag("sport", "softball"), Tag("sport", "racquet"), Tag("sport", "ice_skating"), Tag("sport", "paddle_tennis"), Tag("sport", "australian_football"), Tag("sport", "canadian_football"), Tag("sport", "netball"), Tag("sport", "gaelic_games"), Tag("sport", "sepak_takraw"), Tag("sport", null)),
            "step_count/AddStepCount.kt" to setOf(Tag("step_count", null)),
            "step_count/AddStepCountStile.kt" to setOf(Tag("step_count", null)),
            "steps_incline/AddStepsIncline.kt" to setOf(Tag("incline", "up"), Tag("incline", "down")),
            "steps_ramp/AddStepsRamp.kt" to setOf(Tag("ramp", "no"), Tag("ramp", "yes"), Tag("sidewalk", "separate"), Tag("check_date:ramp", null), Tag("ramp:bicycle", "yes"), Tag("ramp:bicycle", "no"), Tag("ramp:stroller", "yes"), Tag("ramp:stroller", "no"), Tag("ramp:wheelchair", "yes"), Tag("ramp:wheelchair", "no"), Tag("ramp:wheelchair", "separate")),
            "street_parking/AddStreetParking.kt" to setOf(Tag("parking:lane:both", "parallel"), Tag("parking:lane:left", "parallel"), Tag("parking:lane:right", "parallel"), Tag("parking:condition:both", "no_parking"), Tag("parking:condition:left", "no_parking"), Tag("parking:condition:right", "no_parking"), Tag("parking:condition:both", "no_stopping"), Tag("parking:condition:left", "no_stopping"), Tag("parking:condition:right", "no_stopping"), Tag("parking:condition:both", "no_standing"), Tag("parking:condition:left", "no_standing"), Tag("parking:condition:right", "no_standing"), Tag("parking:lane:both", "diagonal"), Tag("parking:lane:left", "diagonal"), Tag("parking:lane:right", "diagonal"), Tag("parking:lane:both", "perpendicular"), Tag("parking:lane:left", "perpendicular"), Tag("parking:lane:right", "perpendicular"), Tag("parking:lane:both", "no"), Tag("parking:lane:left", "no"), Tag("parking:lane:right", "no"), Tag("parking:lane:both", "separate"), Tag("parking:lane:left", "separate"), Tag("parking:lane:right", "separate")),
            "surface/AddCyclewayPartSurface.kt" to setOf(Tag("check_date:cycleway:surface", null), Tag("cycleway:surface:note", null), Tag("cycleway:surface", "asphalt"), Tag("cycleway:surface", "concrete"), Tag("cycleway:surface", "concrete:plates"), Tag("cycleway:surface", "concrete:lanes"), Tag("cycleway:surface", "paving_stones"), Tag("cycleway:surface", "sett"), Tag("cycleway:surface", "unhewn_cobblestone"), Tag("cycleway:surface", "grass_paver"), Tag("cycleway:surface", "wood"), Tag("cycleway:surface", "metal"), Tag("cycleway:surface", "compacted"), Tag("cycleway:surface", "fine_gravel"), Tag("cycleway:surface", "gravel"), Tag("cycleway:surface", "pebblestone"), Tag("cycleway:surface", "woodchips"), Tag("cycleway:surface", "dirt"), Tag("cycleway:surface", "grass"), Tag("cycleway:surface", "sand"), Tag("cycleway:surface", "rock"), Tag("cycleway:surface", "paved"), Tag("cycleway:surface", "unpaved"), Tag("cycleway:surface", "ground")),
            "surface/AddFootwayPartSurface.kt" to setOf(Tag("check_date:footway:surface", null), Tag("footway:surface:note", null), Tag("footway:surface", "asphalt"), Tag("footway:surface", "concrete"), Tag("footway:surface", "concrete:plates"), Tag("footway:surface", "concrete:lanes"), Tag("footway:surface", "paving_stones"), Tag("footway:surface", "sett"), Tag("footway:surface", "unhewn_cobblestone"), Tag("footway:surface", "grass_paver"), Tag("footway:surface", "wood"), Tag("footway:surface", "metal"), Tag("footway:surface", "compacted"), Tag("footway:surface", "fine_gravel"), Tag("footway:surface", "gravel"), Tag("footway:surface", "pebblestone"), Tag("footway:surface", "woodchips"), Tag("footway:surface", "dirt"), Tag("footway:surface", "grass"), Tag("footway:surface", "sand"), Tag("footway:surface", "rock"), Tag("footway:surface", "paved"), Tag("footway:surface", "unpaved"), Tag("footway:surface", "ground")),
            "surface/AddPathSurface.kt" to setOf(Tag("highway", "steps"), Tag("indoor", "yes"), Tag("check_date:surface", null), Tag("surface:note", null), Tag("surface", "asphalt"), Tag("surface", "concrete"), Tag("surface", "concrete:plates"), Tag("surface", "concrete:lanes"), Tag("surface", "paving_stones"), Tag("surface", "sett"), Tag("surface", "unhewn_cobblestone"), Tag("surface", "grass_paver"), Tag("surface", "wood"), Tag("surface", "metal"), Tag("surface", "compacted"), Tag("surface", "fine_gravel"), Tag("surface", "gravel"), Tag("surface", "pebblestone"), Tag("surface", "woodchips"), Tag("surface", "dirt"), Tag("surface", "grass"), Tag("surface", "sand"), Tag("surface", "rock"), Tag("surface", "paved"), Tag("surface", "unpaved"), Tag("surface", "ground")),
            "surface/AddPitchSurface.kt" to setOf(Tag("check_date:surface", null), Tag("surface:note", null), Tag("surface", "grass"), Tag("surface", "asphalt"), Tag("surface", "sand"), Tag("surface", "concrete"), Tag("surface", "clay"), Tag("surface", "artificial_turf"), Tag("surface", "tartan"), Tag("surface", "dirt"), Tag("surface", "fine_gravel"), Tag("surface", "paving_stones"), Tag("surface", "compacted"), Tag("surface", "sett"), Tag("surface", "unhewn_cobblestone"), Tag("surface", "grass_paver"), Tag("surface", "wood"), Tag("surface", "metal"), Tag("surface", "gravel"), Tag("surface", "pebblestone"), Tag("surface", "rock"), Tag("surface", "paved"), Tag("surface", "unpaved"), Tag("surface", "ground")),
            "surface/AddRoadSurface.kt" to setOf(Tag("check_date:surface", null), Tag("surface:note", null), Tag("surface", "asphalt"), Tag("surface", "concrete"), Tag("surface", "concrete:plates"), Tag("surface", "concrete:lanes"), Tag("surface", "paving_stones"), Tag("surface", "sett"), Tag("surface", "unhewn_cobblestone"), Tag("surface", "grass_paver"), Tag("surface", "wood"), Tag("surface", "metal"), Tag("surface", "compacted"), Tag("surface", "fine_gravel"), Tag("surface", "gravel"), Tag("surface", "pebblestone"), Tag("surface", "woodchips"), Tag("surface", "dirt"), Tag("surface", "grass"), Tag("surface", "sand"), Tag("surface", "rock"), Tag("surface", "paved"), Tag("surface", "unpaved"), Tag("surface", "ground")),
            "surface/AddSidewalkSurface.kt" to setOf(Tag("sidewalk:both:surface", "asphalt"), Tag("sidewalk:both:surface:note", null), Tag("sidewalk:both:surface", "concrete"), Tag("sidewalk:both:surface", "concrete:plates"), Tag("sidewalk:both:surface", "concrete:lanes"), Tag("sidewalk:both:surface", "paving_stones"), Tag("sidewalk:both:surface", "sett"), Tag("sidewalk:both:surface", "unhewn_cobblestone"), Tag("sidewalk:both:surface", "grass_paver"), Tag("sidewalk:both:surface", "wood"), Tag("sidewalk:both:surface", "metal"), Tag("sidewalk:both:surface", "compacted"), Tag("sidewalk:both:surface", "fine_gravel"), Tag("sidewalk:both:surface", "gravel"), Tag("sidewalk:both:surface", "pebblestone"), Tag("sidewalk:both:surface", "woodchips"), Tag("sidewalk:both:surface", "dirt"), Tag("sidewalk:both:surface", "grass"), Tag("sidewalk:both:surface", "sand"), Tag("sidewalk:both:surface", "rock"), Tag("sidewalk:both:surface", "paved"), Tag("sidewalk:both:surface", "unpaved"), Tag("sidewalk:both:surface", "ground"), Tag("sidewalk:left:surface", "asphalt"), Tag("sidewalk:left:surface:note", null), Tag("sidewalk:left:surface", "concrete"), Tag("sidewalk:left:surface", "concrete:plates"), Tag("sidewalk:left:surface", "concrete:lanes"), Tag("sidewalk:left:surface", "paving_stones"), Tag("sidewalk:left:surface", "sett"), Tag("sidewalk:left:surface", "unhewn_cobblestone"), Tag("sidewalk:left:surface", "grass_paver"), Tag("sidewalk:left:surface", "wood"), Tag("sidewalk:left:surface", "metal"), Tag("sidewalk:left:surface", "compacted"), Tag("sidewalk:left:surface", "fine_gravel"), Tag("sidewalk:left:surface", "gravel"), Tag("sidewalk:left:surface", "pebblestone"), Tag("sidewalk:left:surface", "woodchips"), Tag("sidewalk:left:surface", "dirt"), Tag("sidewalk:left:surface", "grass"), Tag("sidewalk:left:surface", "sand"), Tag("sidewalk:left:surface", "rock"), Tag("sidewalk:left:surface", "paved"), Tag("sidewalk:left:surface", "unpaved"), Tag("sidewalk:left:surface", "ground"), Tag("sidewalk:right:surface", "asphalt"), Tag("sidewalk:right:surface:note", null), Tag("sidewalk:right:surface", "concrete"), Tag("sidewalk:right:surface", "concrete:plates"), Tag("sidewalk:right:surface", "concrete:lanes"), Tag("sidewalk:right:surface", "paving_stones"), Tag("sidewalk:right:surface", "sett"), Tag("sidewalk:right:surface", "unhewn_cobblestone"), Tag("sidewalk:right:surface", "grass_paver"), Tag("sidewalk:right:surface", "wood"), Tag("sidewalk:right:surface", "metal"), Tag("sidewalk:right:surface", "compacted"), Tag("sidewalk:right:surface", "fine_gravel"), Tag("sidewalk:right:surface", "gravel"), Tag("sidewalk:right:surface", "pebblestone"), Tag("sidewalk:right:surface", "woodchips"), Tag("sidewalk:right:surface", "dirt"), Tag("sidewalk:right:surface", "grass"), Tag("sidewalk:right:surface", "sand"), Tag("sidewalk:right:surface", "rock"), Tag("sidewalk:right:surface", "paved"), Tag("sidewalk:right:surface", "unpaved"), Tag("sidewalk:right:surface", "ground"), Tag("check_date:sidewalk:surface", null)),
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
            "way_lit/AddWayLit.kt" to setOf(Tag("lit", "no"), Tag("lit", "yes"), Tag("lit", "automatic"), Tag("lit", "24/7"), Tag("check_date:lit", null), Tag("highway", "steps")),
            "wheelchair_access/AddWheelchairAccessBusiness.kt" to setOf(Tag("wheelchair", "yes"), Tag("wheelchair", "limited"), Tag("wheelchair", "no")),
            "wheelchair_access/AddWheelchairAccessOutside.kt" to setOf(Tag("check_date:wheelchair", null), Tag("wheelchair", "yes"), Tag("wheelchair", "limited"), Tag("wheelchair", "no")),
            "wheelchair_access/AddWheelchairAccessPublicTransport.kt" to setOf(Tag("check_date:wheelchair", null), Tag("wheelchair", "yes"), Tag("wheelchair", "limited"), Tag("wheelchair", "no")),
            "wheelchair_access/AddWheelchairAccessToilets.kt" to setOf(Tag("check_date:wheelchair", null), Tag("wheelchair", "yes"), Tag("wheelchair", "limited"), Tag("wheelchair", "no")),
            "wheelchair_access/AddWheelchairAccessToiletsPart.kt" to setOf(Tag("check_date:toilets:wheelchair", null), Tag("toilets:wheelchair", "yes"), Tag("toilets:wheelchair", "limited"), Tag("toilets:wheelchair", "no")),
            "width/AddCyclewayWidth.kt" to setOf(Tag("width", null), Tag("source:width", "ARCore"), Tag("cycleway:width", null), Tag("source:cycleway:width", "ARCore")),
            "width/AddRoadWidth.kt" to setOf(Tag("width", null), Tag("source:width", "ARCore")),
        )
    }

    private fun generateReport(questData: List<TagQuestInfo>) {
        println(targetDir)
        val format = Json { encodeDefaults = true; explicitNulls = false; prettyPrint = true  }

        @Serializable
        data class TagWithDescriptionForTaginfoListing(val key: String, val value: String?, val description: String)

        @Serializable
        data class Project(val name: String, val description: String, val project_url: String, val doc_url:String, val icon_url:String, val contact_name: String, val contact_email: String)

        @Serializable
        data class TaginfoReport(val data_format:Int = 1, val data_url: String, val project: Project, val tags: List<TagWithDescriptionForTaginfoListing>)


        // https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/json.md
        val project = Project("StreetComplete", "Surveyor app for Android",
            "https://github.com/westnordost/StreetComplete",
            "https://wiki.openstreetmap.org/wiki/StreetComplete",
            "https://raw.githubusercontent.com/westnordost/StreetComplete/master/app/src/main/res/mipmap-xhdpi/ic_launcher.png",
            "Mateusz Konieczny",
            "matkoniecz@tutanota.com",
        )
        val report = TaginfoReport(1, "TODOfixdataURL", project,
            questData.map{TagWithDescriptionForTaginfoListing(it.tag.key, it.tag.value, "added or edited tag in ${it.quest} quest")}
            )
        val jsonText = format.encodeToString(report)
        val targetFile = File(targetDir, "taginfo_listing_of_tags_added_or_edited_by_StreetComplete.json")
        if(targetFile.exists()) {
            val oldText = targetFile.readText()
            val oldReport = format.decodeFromString<TaginfoReport>(oldText)
            if(report.tags != oldReport.tags) {
                println("new tags are different! verify that")
                report.tags.forEach {
                    if(it !in oldReport.tags) {
                        println("new entry: $it")
                    }
                }
                oldReport.tags.forEach {
                    if(it !in report.tags) {
                        println("removed entry: $it")
                    }
                }
                // TODO: replace entire manual listing by comparing here
            }
        }
        val fileWriter = targetFile.writer()
        fileWriter.write(jsonText)
        fileWriter.close()
    }

    @TaskAction fun run() {
        println(targetDir)

        var processed = 0
        val failedQuests = mutableSetOf<String>()
        val foundTags = mutableListOf<TagQuestInfo>()
        val folderGenerator = questFolderGenerator()

        while (folderGenerator.hasNext()) {
            val folder = folderGenerator.next()

            File(folder.toString()).walkTopDown().forEach {
                if (it.isFile) {
                    if (isQuestFile(it)) {
                        val got: Set<Tag>?
                        try {
                            got = addedOrEditedTags(it)
                        } catch (e: ParsingInterpretationException) {
                            print(it.name)
                            throw e
                        }
                        reportResultOfScanInSingleQuest(got, it)
                        if (got != null) {
                            processed += 1
                            got.forEach { tags -> foundTags.add(TagQuestInfo(tags, it.name)) }
                        } else {
                            failedQuests.add(it.toString())
                        }
                    }
                }
            }
        }
        generateReport(foundTags)
        reportResultOfDataCollection(foundTags, processed, failedQuests)
        checkOsmWikiPagesExistence(foundTags)
    }

    private fun questFolderGenerator() = iterator {
        File(QUEST_ROOT_WITH_SLASH_ENDING).walkTopDown().maxDepth(1).forEach { folder ->
            if (folder.isDirectory && "$folder/" != QUEST_ROOT_WITH_SLASH_ENDING) {
                yield(folder)
            }
        }
    }

    private fun candidatesForEnumFilesForGivenFile(file: File): List<File> {
        val suspectedAnswerEnumFilesBasedOnFolder = candidatesForEnumFilesBasedOnFolder(file.parentFile)
        return suspectedAnswerEnumFilesBasedOnFolder + candidatesForEnumFilesBasedOnImports(file)
    }

    private fun candidatesForEnumFilesBasedOnFolder(folder: File): List<File> {
        val suspectedAnswerEnumFiles = mutableListOf<File>()
        File(folder.toString()).walkTopDown().forEach {
            if (isLikelyAnswerEnumFile(it)) {
                suspectedAnswerEnumFiles.add(it)
            }
        }
        return suspectedAnswerEnumFiles
    }

    private fun candidatesForEnumFilesBasedOnImports(file: File): List<File> {
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
        return importedByFile(file)
            .filter { isLikelyAnswerEnumFile(File(it)) }
            .map { File(it) }
            .filter { it.isFile }
    }

    private fun importedByFile(file: File): Set<String> {
        val returned = mutableSetOf<String>()
        val fileSourceCode = loadFileText(file)
        file.parse().locateByDescription("importList").forEach { importList ->
            importList.locateByDescription("importHeader").forEach {
                if (it is DefaultAstNode) {
                    areDirectChildrenMatchingStructureThrowExceptionIfNot("checking import file structure for $path", listOf(listOf("IMPORT", "WS", "identifier", "semi")), it, fileSourceCode, eraseWhitespace = false)
                    val imported = it.locateSingleOrExceptionByDescriptionDirectChild("identifier")
                    // println(imported.locateByDescriptionDirectChild("simpleIdentifier").size.toString() + "  ddddddd")
                    val importedPath = KOTLIN_IMPORT_ROOT_WITH_SLASH_ENDING + imported.locateByDescriptionDirectChild("simpleIdentifier").map {
                        (it.tree() as KlassIdentifier).identifier
                    }.joinToString("/") + ".kt"
                    if (File(importedPath).isFile) {
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
        // in case that it is actually needed
        // println("packageHeader")
        // println(ast.parse().locateSingleOrExceptionByDescription("packageHeader").relatedSourceCode(fileSourceCode))
        // ast.parse().locateSingleOrExceptionByDescription("packageHeader").showHumanReadableTreeWithSourceCode(fileSourceCode)
        return returned
    }

    private fun isLikelyAnswerEnumFile(file: File): Boolean {
        if (".kt" !in file.name) {
            return false
        }
        val banned = listOf("SelectPuzzle.kt", "Form.kt", "Util.kt", "Utils.kt", "Adapter.kt",
            "Drawable.kt", "Dao.kt", "Dialog.kt", "Item.kt", "RotateContainer.kt")
        banned.forEach { if (it in file.name) {
                return false
            }
        }
        listOf("OsmFilterQuestType.kt", "MapDataWithGeometry.kt", "Element.kt", "Tags.kt",
            "OsmElementQuestType.kt", "CountryInfos.kt").forEach {
            if (it == file.name) {
                return false
            }
        }
        return !isQuestFile(file)
    }

    private fun isQuestFile(file: File): Boolean {
        if (".kt" !in file.name) {
            return false
        }
        listOf("Form.kt", "Adapter.kt", "Utils.kt").forEach { if (it in file.name) {
                return false
            }
        }
        if (file.name == "AddressStreetAnswer.kt") {
            return false
        }
        if ("Add" in file.name || "Check" in file.name || "Determine" in file.name || "MarkCompleted" in file.name) {
            return true
        }
        return false
    }

    // FIGURE OUT HOW TO AVOID COPYING THIS!
    @Serializable
    data class IncompleteCountryInfo(
        val additionalStreetsignLanguages: Set<String> = setOf(),
        val officialLanguages: Set<String> = setOf(),
    )

    private fun possibleLanguageKeys(): MutableSet<String> {
        val languageTags = mutableSetOf("name", "int_name")
        File(COUNTRY_METADATA_PATH_WITH_SLASH_ENDING).walkTopDown().maxDepth(1).forEach { file ->
            if (file.isFile) {
                val test = Yaml(configuration = YamlConfiguration(strictMode = false)).decodeFromString(IncompleteCountryInfo.serializer(), loadFileText(file))
                val langs = test.officialLanguages + test.additionalStreetsignLanguages
                if (langs.size > 1) {
                    // international counts for purposes of triggering multi-language support
                    // but itself is rather tagged with int_name tag
                    langs.filter { it != "international" }.forEach { languageTags.add("name:$it") }
                }
            }
        }
        return languageTags
    }

    private fun reportResultOfScanInSingleQuest(got: Set<Tag>?, file: File) {
        val fileSourceCode = loadFileText(file)
        val filepath = file.toString().removePrefix(QUEST_ROOT_WITH_SLASH_ENDING)
        var mismatch = false
        if (filepath in EXPECTED_TAG_PER_QUEST) {
            if (got == EXPECTED_TAG_PER_QUEST[filepath]) {
                return
            } else {
                mismatch = true
            }
        }
        val ast = AstSource.String(filepath, fileSourceCode).parse()
        val relevantFunction = getAstTreeForFunctionEditingTags(filepath, ast)
        if (mismatch) {
            println()
            println("----------------- $filepath")
            println("Got")
            println(got)
            println()
            println("Was expected:")
            println(EXPECTED_TAG_PER_QUEST[filepath])
            println()
            if (got == null) {
                println("got empty input")
            }
            if (EXPECTED_TAG_PER_QUEST[filepath] != null && got != null) {
                println("Expected, was missing:")
                println(EXPECTED_TAG_PER_QUEST[filepath]!!.filter { it !in got })
                println()
                println("Unexpected, was present:")
                println(got.filter { it !in EXPECTED_TAG_PER_QUEST[filepath]!! })
            }
            println()
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
        if (got == null) {
            return
        }
        relevantFunction.showRelatedSourceCode("inspected function", fileSourceCode)
        println(got)
        println(tagSetToReproducibleCode(got, filepath))
        println("-----------------")
        println()
    }

    private fun tagSetToReproducibleCode(got: Set<Tag>?, filepath: String): String {
        val classesReadyToCreate = got?.joinToString(", ") { it.reproduceCode() }
        return "\"$filepath\" to setOf($classesReadyToCreate),"
    }

    private fun reportResultOfDataCollection(foundTags: MutableList<TagQuestInfo>, processed: Int, failedQuests: MutableSet<String>) {
        // foundTags.forEach { println("$it ${if (it.tag.value == null && !freeformKey(it.tag.key)) {"????????"} else {""}}") }
        println("${foundTags.size} entries registered, $processed quests processed, ${failedQuests.size} failed")
        val tagsFoundPreviously = 1619
        if (foundTags.size != tagsFoundPreviously) {
            println("Something changed in processing! foundTags count ${foundTags.size} vs $tagsFoundPreviously previously")
        }
        val processedQuestsPreviously = 144
        if (processed != processedQuestsPreviously) {
            println("Something changed in processing! processed count $processed vs $processedQuestsPreviously previously")
        }
        val realFailed = failedQuests.size
        val knownFailed = setOf<String>()
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
    }

    private fun checkOsmWikiPagesExistence(foundTags: MutableList<TagQuestInfo>) {
        val allKeys = mutableSetOf<String>()
        foundTags.forEach { allKeys.add(it.tag.key) }
        println("${allKeys.size} different keys")
        val processedTags = mutableSetOf<Tag>()
        println()
        println()
        // note that
        // https://github.com/openstreetmap/openstreetmap-website/blob/master/config/wiki_pages.yml
        // exists and using it may be smarter than rerunning this checks every time
        // on every build
        // note that full scan of wiki lasts more than two hours
        // see https://github.com/openstreetmap/openstreetmap-website/pull/3294 for update instructions
        foundTags.map { it.tag }.forEach {
            if (it.key.startsWith("name:")) {
                // TODO a known wiki design issue, lets wait for resolving it
                // https://wiki.openstreetmap.org/w/index.php?title=Talk:Wiki&oldid=2359644#name%3Amos
                // https://wiki.openstreetmap.org/wiki/Talk:Wiki#name%3Amos
                return@forEach
            }
            if (it in processedTags) {
                return@forEach
            }

            val keyOnly = Tag(it.key, null)
            val keyWasProcessed = keyOnly in processedTags

            processedTags.add(it)
            processedTags.add(keyOnly)

            if (isCompoundDocumentationPageAllowedForKey(keyOnly.key)) {
                if (!keyWasProcessed && !isCompoundListerErrorPageExisting(keyOnly.osmWikiPageUrl())) {
                    if (!isPageExisting(keyOnly.osmWikiPageUrl())) {
                        println("${keyOnly.key}= has no expected OSM Wiki compound page at ${keyOnly.osmWikiPageUrl()} and there is no normal key page there")
                    }
                }
                return@forEach
            }

            if (!isPageExisting(keyOnly.osmWikiPageUrl())) {
                if (it.value != null) {
                    // if value page exists, then it is likely fine - but how we can link stuff?
                    if (!isPageExisting(it.osmWikiPageUrl())) {
                        println("${it.key}=${it.value} has no key OSM Wiki page at ${keyOnly.osmWikiPageUrl()} and has no value page at ${it.osmWikiPageUrl()}")
                        return@forEach
                    } else {
                        if(!isCompoundListerErrorPageExisting(keyOnly.osmWikiPageUrl())) {
                            println("${it.key}=${it.value} has no key OSM Wiki page at ${keyOnly.osmWikiPageUrl()} - ant it has no compound lister there, but it has a value page at ${it.osmWikiPageUrl()}")
                        }
                    }
                } else {
                    println("${it.key}= has no OSM Wiki page at ${keyOnly.osmWikiPageUrl()}")
                }
            }
            if (!isSkippingValuePageAllowedForTag(it) && it.value != null) {
                if (!isPageExisting(it.osmWikiPageUrl())) {
                    println("${it.key}=${it.value} has no value OSM Wiki page at ${it.osmWikiPageUrl()}")
                }
            }
        }
    }

    private fun isSkippingValuePageAllowedForTag(it: Tag): Boolean {
        // this values should be described at the key page
        // not ideal as
        // - StreetComplete can be using bogus values
        // - some of this values may actually have pages

        // alternative would be creation of OSM wiki pages for all of them
        // but I am not entirely sure is it a good idea
        if (it.value in listOf(null, "no", "yes", "only")) {
            return true
        }
        if (freeformKey(it.key)) {
            return true
        }
        if ( it.key == "fire_hydrant:type") {
            // TODO: what about fire_hydrant:type=pond? According to wiki it should not be used
            // https://wiki.openstreetmap.org/wiki/Tag:emergency%3Dfire_hydrant
            return true
        }
        if (it.key in listOf("crossing:barrier", "bicycle_rental", "roof:shape", "material", "royal_cypher", "camera:type",
                "bollard", "board_type", "cycle_barrier", "bicycle_parking", "location", "stile", "shoulder",
                "toilets:wheelchair", "ramp:wheelchair", "smoking")) {
            return true
        }
        if (it.key.startsWith("recycling:") || it.key.startsWith("parking:")
            || it.key.startsWith("cycleway:") || it.key.startsWith("footway:")) {
            return true
        }
        return false
    }

    private fun isCompoundDocumentationPageAllowedForKey(key: String): Boolean {
        //  see say https://wiki.openstreetmap.org/w/index.php?title=Key:check_date:cycleway
        if (key.startsWith("$SURVEY_MARK_KEY:")) {
            return true
        }
        if (key == "maxspeed:type:advisory") {
            return true
        }
        if (key.startsWith("name:")) {
            return true
        }
        if (key.startsWith("source:")) {
            return true
        }
        if (key.startsWith("recycling:")) {
            // https://wiki.openstreetmap.org/w/index.php?title=Key:recycling:cooking_oil
            return true
        }
        if (key.startsWith("sidewalk:")) {
            return true
        }
        if (key.startsWith("cycleway:")) {
            return true
        }
        if (key.endsWith(":note")) {
            return true
        }
        return false
    }

    private fun isPageExisting(url: String): Boolean {
        try {
            URL(url).openStream().bufferedReader().use { it.readText() }
        } catch (e: java.io.FileNotFoundException) {
            return false
        }
        return true
    }

    private fun isCompoundListerErrorPageExisting(url: String): Boolean {
        return "is a compound key consisting of" in Jsoup.connect(url).ignoreHttpErrors(true).get()
            .body().toString()
    }

    private fun streetCompleteIsReusingAnyValueProvidedByExistingTagging(questDescription: String, key: String): Boolean {
        // much too complicated and error prone and rare to get that info by parsing
        if ("MarkCompletedHighwayConstruction" in questDescription && key == "highway") {
            return true
        }
        if ("MarkCompletedBuildingConstruction" in questDescription && key == "building") {
            return true
        }
        return false
    }

    private fun freeformKey(key: String): Boolean {
        // most have own syntax and limitations obeyed by SC
        // maybe move to general StreetComplete file about OSM tagging?
        if (key in listOf("name", "int_name", "ref",
                "addr:flats", "addr:housenumber", "addr:street", "addr:place", "addr:block_number", "addr:streetnumber",
                "addr:conscriptionnumber", "addr:housename",
                "building:levels", "roof:levels", "level",
                "collection_times", "opening_hours", "opening_date", "check_date",
                "fire_hydrant:diameter", "maxheight", "width", "cycleway:width",
                "maxspeed", "maxspeed:advisory", "maxstay",
                "maxweight", "maxweightrating", "maxaxleload", "maxbogieweight",
                "maxspeed:type", // not really true, but I give up for now. TODO: remove
                "capacity", "step_count",
                "lanes", "lanes:forward", "lanes:backward", "lanes:both_ways",
                "turn:lanes:both_ways", "turn:lanes", "turn:lanes:forward", "turn:lanes:backward",
                "operator", // technically not fully, but does ot make sense to list all that autocomplete values
                "brand",
                "sport", // sport=soccer;volleyball is fully valid - doe not entirely fit here but...
                "produce", // like sport=*
            )) {
            return true
        }
        if (SURVEY_MARK_KEY in key) {
            return true
        }
        if (key.endsWith(":note")) {
            return true
        }
        if (key.endsWith(":conditional")) {
            return true
        }
        if (key.endsWith(":wikidata")) {
            return true
        }
        if (key.endsWith(":wikipedia")) {
            return true
        }
        if (key.startsWith("lanes:")) {
            return true
        }
        if (key.startsWith("name:")) {
            return true
        }
        if (key.startsWith("source:")) {
            return true
        }
        return false
    }

    private fun loadFileText(file: File): String {
        val inputStream: InputStream = file.inputStream()
        return inputStream.bufferedReader().use { it.readText() }
    }

    @Serializable
    class Tag(val key: String, val value: String?) {
        override fun toString(): String {
            if (value == null) {
                return "$key=*"
            }
            return "$key=$value"
        }

        fun osmWikiPageUrl(): String {
            if (value == null) {
                return "https://wiki.openstreetmap.org/w/index.php?title=Key:${key.replace(" ", "_")}"
            }
            return "https://wiki.openstreetmap.org/w/index.php?title=Tag:${key.replace(" ", "_")}=${value.replace(" ", "_")}"
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

    class TagQuestInfo(val tag: Tag, val quest: String) {
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

    private fun getAstTreeForFunctionEditingTags(description: String, ast: Ast): AstNode {
        val found = ast.extractFunctionByName(NAME_OF_FUNCTION_EDITING_TAGS)
        if (found == null) {
            println("$NAME_OF_FUNCTION_EDITING_TAGS not found in $description")
            exitProcess(1)
        }
        return found
    }

    private fun addedOrEditedTags(file: File): Set<Tag>? {
        val hardcodedAnswers = addedOrEditedTagsHardcodedAnswers(file)
        if(hardcodedAnswers != null) {
            return hardcodedAnswers
        }
        val suspectedAnswerEnumFiles = candidatesForEnumFilesForGivenFile(file)
        val description = file.parentFile.name + File.separator + file.name
        val fileSourceCode = loadFileText(file)
        return addedOrEditedTagsRealParsing(description, fileSourceCode, suspectedAnswerEnumFiles)
    }

    private fun addedOrEditedTagsHardcodedAnswers(file: File): Set<Tag>? {
        val fileSourceCode = loadFileText(file)
        val description = file.parentFile.name + File.separator + file.name
        var suspectedAnswerEnumFiles = candidatesForEnumFilesForGivenFile(file)
        // TODO hardcoding is ugly and ideally would be replaced
        // this function contains cases where answers are partially or fully hardcoded
        // it is done this way as in some cases parsing would extremely complex and not worth doing this
        // in some it can be actually implemented and it is likely worth doing this to avoid need
        // for manual maintenance of the code
        if("AddBarrier" in file.name) { // outside when switch to try covering also unlikely new AddBarrier quests
            // TODO argh? can it be avoided?
            // why it is present? Without this AddBarrierOnPath would pull also StileTypeAnswer
            // and claim that barrier=stepover is a thing
            // would need substantial additional parsing of import data to fix it :(
            suspectedAnswerEnumFiles = suspectedAnswerEnumFiles.filter { "StileTypeAnswer.kt" !in it.name }
            return addedOrEditedTagsRealParsing(description, fileSourceCode, suspectedAnswerEnumFiles)
        }
        when (file.name) {
            "AddAddressStreet.kt" -> {
                return setOf(Tag("addr:street", null), Tag("addr:place", null))
            }
            "AddRoadName.kt" -> {
                val appliedTags = mutableSetOf<Tag>()
                possibleLanguageKeys().forEach { appliedTags.add(Tag(it, null)) }
                appliedTags += addedOrEditedTagsRealParsing(description, fileSourceCode, suspectedAnswerEnumFiles)!!
                return appliedTags
            }
            "AddStreetParking.kt" -> {
                val appliedTags = mutableSetOf<Tag>()
                val parkingOrientations = listOf("parallel", "diagonal", "perpendicular")
                val orientations = parkingOrientations + listOf("no", "separate")
                val noConditions = listOf("no_parking", "no_stopping", "no_standing")
                var modifiedSourceCode = fileSourceCode
                modifiedSourceCode = modifiedSourceCode.replace("laneLeft\"] = positionLeft", "laneParkingLeft\"] = positionLeft")
                modifiedSourceCode = modifiedSourceCode.replace("laneRight\"] = positionRight", "laneParkingRight\"] = positionRight")
                modifiedSourceCode = modifiedSourceCode.replace("val laneRight", "val laneBlockerReplacementRight")
                modifiedSourceCode = modifiedSourceCode.replace("val laneLeft", "val laneBlockerReplacementLeft")
                parkingOrientations.forEach { parkingSuffix ->
                    orientations.forEach { orientation ->
                        noConditions.forEach { noCondition ->
                            val specificModifiedCode =  modifiedSourceCode
                                .replace("tags[\"parking:condition:both\"] = it", "tags[\"parking:condition:both\"] = \"$noCondition\"")
                                .replace("tags[\"parking:condition:left\"] = it", "tags[\"parking:condition:left\"] = \"$noCondition\"")
                                .replace("tags[\"parking:condition:right\"] = it", "tags[\"parking:condition:right\"] = \"$noCondition\"")
                                .replace("\$laneParkingLeft", parkingSuffix)
                                .replace("[laneParkingLeft]", "[\"$parkingSuffix\"]")
                                .replace("\$laneParkingRight", parkingSuffix)
                                .replace("[laneParkingRight]", "[\"$parkingSuffix\"]")
                                .replace("laneLeft", '"' + orientation + '"')
                                .replace("laneRight", '"' + orientation + '"')
                            appliedTags += addedOrEditedTagsWithGivenFunction("$description modified code", specificModifiedCode, "tags", "applyAnswerTo", suspectedAnswerEnumFiles)!!
                            // appliedTags.add(Tag("sidewalk:$side:surface", "no"),
                            // appliedTags.add(Tag("sidewalk:$side:surface:note", null),
                        }
                    }
                }
                return appliedTags
            }
            "AddMaxSpeed.kt" -> {
                val appliedTags = mutableSetOf<Tag>()
                appliedTags += addedOrEditedTagsRealParsing(description, fileSourceCode, suspectedAnswerEnumFiles)!!
                appliedTags.add(Tag("maxspeed", null))
                appliedTags.add(Tag("maxspeed:type", null)) // TODO - not really true but I give up here for now
                appliedTags.add(Tag("maxspeed:advisory", null))
                appliedTags.add(Tag("maxspeed:type:advisory", "sign"))
                return appliedTags
            }
            "AddSidewalk.kt" -> {
                return setOf(Tag("sidewalk", "no"), Tag("sidewalk", "both"), Tag("sidewalk", "left"),
                    Tag("sidewalk", "right"), Tag("sidewalk", "separate"),
                    Tag(surveyMarkKeyBasedOnKey("sidewalk"), null),
                    Tag("sidewalk:left", "no"), Tag("sidewalk:left", "yes"), Tag("sidewalk:left", "separate"),
                    Tag("sidewalk:right", "no"), Tag("sidewalk:right", "yes"), Tag("sidewalk:right", "separate"),
                )
            }
            "AddWayLit.kt" -> {
                return setOf(Tag("lit", "no"), Tag("lit", "yes"), Tag("lit", "automatic"), Tag("lit", "24/7"),
                    Tag(surveyMarkKeyBasedOnKey("lit"), null), Tag("highway", "steps")
                )
            }
            "AddMaxWeight.kt" -> {
                return setOf(Tag("maxweight:signed", "no"), Tag("maxweight", null), Tag("maxweightrating", null),
                    Tag("maxaxleload", null), Tag("maxbogieweight", "null"),
                )
            }
            "AddStepsRamp.kt" -> {
                return setOf(Tag("ramp", "no"), Tag("ramp", "yes"), Tag("sidewalk", "separate"),
                    Tag(surveyMarkKeyBasedOnKey("ramp"), null),
                    Tag("ramp:bicycle", "yes"), Tag("ramp:bicycle", "no"),
                    Tag("ramp:stroller", "yes"), Tag("ramp:stroller", "no"),
                    Tag("ramp:wheelchair", "yes"), Tag("ramp:wheelchair", "no"),
                    Tag("ramp:wheelchair", "yes"), Tag("ramp:wheelchair", "no"), Tag("ramp:wheelchair", "separate"),
                )
            }
            "AddDrinkingWater.kt" -> {
                return setOf(
                    Tag("drinking_water", "no"), Tag("drinking_water", "yes"),
                    Tag("drinking_water:legal", "no"), Tag("drinking_water:legal", "yes"),
                )
            }
            "AddRecyclingContainerMaterials.kt" -> {
                val appliedTags = mutableSetOf<Tag>()
                val recylingMaterialsFile = File(QUEST_ROOT_WITH_SLASH_ENDING + "recycling_material/RecyclingMaterial.kt")
                val materials = getEnumValuesDefinedInThisFile("RecyclingMaterial hack", recylingMaterialsFile)
                materials.forEach {
                    if (it.fields.size != 1) {
                        throw ParsingInterpretationException("expected a single value, got $it")
                    }
                    appliedTags.add(Tag("recycling:${it.fields[0].possibleValue}", "yes"))
                }
                appliedTags.add(Tag("amenity", "waste_disposal")) // from applyWasteContainerAnswer, harcoded due to complexity HACK :(
                val modifiedile = fileSourceCode.replace("tags[material] = \"yes\"", "") // HACK :(
                val got = addedOrEditedTagsWithGivenFunction("$description modified code", modifiedile, "tags", "applyRecyclingMaterialsAnswer", suspectedAnswerEnumFiles)
                if (got == null) {
                    return null
                }
                appliedTags += got
                return appliedTags
            }
            "AddBuildingType.kt" -> {
                val appliedTags = mutableSetOf<Tag>()
                val answersFile = File(QUEST_ROOT_WITH_SLASH_ENDING + "building_type/BuildingType.kt")
                val localDescription = "${answersFile.parentFile.name}/${answersFile.name} hack"
                val answers = getEnumValuesDefinedInThisFile(localDescription, answersFile)
                answers.forEach { enumGroup ->
                    enumGroup.fields.forEach {
                        if (enumGroup.fields.size != 2 || enumGroup.fields[0].identifier != "osmKey" || enumGroup.fields[1].identifier != "osmValue") {
                            throw ParsingInterpretationException("unexpected $enumGroup")
                        }
                        appliedTags.add(Tag(enumGroup.fields[0].possibleValue, enumGroup.fields[1].possibleValue))
                    }
                }
                return appliedTags
            }
            "AddStileType.kt" -> {
                val appliedTags = mutableSetOf<Tag>()
                // maybe track assigments to the values which are later assigned to fields? This would be feasible here, I guess...")
                val answersFile = File(QUEST_ROOT_WITH_SLASH_ENDING + "barrier_type/StileTypeAnswer.kt")
                val localDescription = "${answersFile.parentFile.name}/${answersFile.name} hack"
                val answers = getEnumValuesDefinedInThisFile(localDescription, answersFile)
                answers.forEach { enumGroup ->
                    enumGroup.fields.forEach {
                        when (it.identifier) {
                            "newBarrier" -> {
                                appliedTags.add(Tag("barrier", it.possibleValue))
                            }
                            "osmValue" -> {
                                appliedTags.add(Tag("stile", it.possibleValue))
                            }
                            "osmMaterialValue" -> {
                                appliedTags.add(Tag("material", it.possibleValue))
                            }
                            else -> {
                                throw ParsingInterpretationException("unexpected")
                            }
                        }
                    }
                }
                return appliedTags
            }
            "AddCyclewayWidth.kt" -> {
                val appliedTags = mutableSetOf<Tag>()
                val keys = listOf("width", "cycleway:width") // TODO: get it from parsing
                keys.forEach { key ->
                    val modifiedSourceCode = fileSourceCode.replace("\$key", key).replace("[key]", "[\"$key\"]")
                    appliedTags += addedOrEditedTagsWithGivenFunction("$description modified code", modifiedSourceCode, "tags", NAME_OF_FUNCTION_EDITING_TAGS, suspectedAnswerEnumFiles)!!
                }
                return appliedTags
            }
            "AddCycleway.kt" -> {
                val got = mutableSetOf<Tag>()
                got += addedOrEditedTagsWithGivenFunction(description, fileSourceCode, "tags", "applySidewalkAnswerTo", suspectedAnswerEnumFiles)!!
                val sides = listOf("both", "left", "right") // TODO: get it from parsing
                val directionValue  = listOf("\"yes\"", "\"-1\"") // TODO: get it from parsing
                sides.forEach { side ->
                    directionValue.forEach { direction ->
                        val modifiedSourceCode = fileSourceCode.replace("\$cyclewayKey", "cycleway:$side")
                            .replace("[cyclewayKey]", "[\"cycleway:$side\"]")
                            .replace("val directionValue", "val directionPreservedValue")
                            .replace("directionValue", direction)

                        got += addedOrEditedTagsWithGivenFunction("$description modified code", modifiedSourceCode, "tags", "applyCyclewayAnswerTo", suspectedAnswerEnumFiles)!!
                    }
                }
                return got + addedOrEditedTagsRealParsing(description, fileSourceCode, suspectedAnswerEnumFiles)!!
            }
            "AddSidewalkSurface.kt" -> {
                val appliedTags = mutableSetOf<Tag>()
                // appliedTags.add(Tag(surveyMarkKeyBasedOnKey("sidewalk:surface"), null))
                val sides = listOf("both", "left", "right") // TODO: maybe get it from parsing
                val surfaces = retrieveSurfaceValuesFromGroupIdentifiers(listOf("PAVED_SURFACES", "UNPAVED_SURFACES", "GROUND_SURFACES", "GENERIC_ROAD_SURFACES")) // todo - may be get it from parsing? Other surface quests managed somehow
                sides.forEach { side ->
                    surfaces.forEach { surface ->
                        val modifiedSourceCode = fileSourceCode.replace("\$sidewalkSurfaceKey", "sidewalk:$side:surface")
                            .replace("[sidewalkSurfaceKey]", "[\"sidewalk:$side:surface\"]")
                            .replace("surface.value.osmValue", '"' + surface + '"')
                        appliedTags += addedOrEditedTagsWithGivenFunction("$description modified code", modifiedSourceCode, "tags", "applySidewalkSurfaceAnswerTo", suspectedAnswerEnumFiles)!!
                        // appliedTags.add(Tag("sidewalk:$side:surface", "no"),
                        // appliedTags.add(Tag("sidewalk:$side:surface:note", null),
                    }
                }
                appliedTags += addedOrEditedTagsRealParsing(description, fileSourceCode, suspectedAnswerEnumFiles)!!
                return appliedTags
            }
            "AddRoadSurface.kt", "AddPathSurface.kt", "AddFootwayPartSurface.kt", "AddCyclewayPartSurface.kt", "AddPitchSurface.kt" -> {
                val appliedTags = mutableSetOf<Tag>()
                //appliedTags += addedOrEditedTagsActualParsingWithoutHardcodedAnswers(description, fileSourceCode, suspectedAnswerEnumFiles)!! // TODO - get it working
                // TODO - or at least this appliedTags += addedOrEditedTagsWithGivenFunction(description, fileSourceCode, "tags", NAME_OF_FUNCTION_EDITING_TAGS, suspectedAnswerEnumFiles)!!
                if(file.name == "AddPathSurface.kt") {
                    appliedTags.add(Tag("highway", "steps"))
                    appliedTags.add(Tag("indoor", "yes"))
                }
                val surfaces = listOfSurfaceValuesInSurfaceQuest(file)
                val key = when(file.name) {
                    "AddRoadSurface.kt" -> "surface"
                    "AddPathSurface.kt" -> "surface"
                    "AddFootwayPartSurface.kt" -> "footway:surface"
                    "AddCyclewayPartSurface.kt" -> "cycleway:surface"
                    "AddPitchSurface.kt" -> "surface"
                    else -> throw ParsingInterpretationException("should never happen due to exhausting values from upper when selector")
                }
                appliedTags.add(Tag(surveyMarkKeyBasedOnKey(key), null))
                appliedTags.add(Tag("$key:note", null))
                surfaces.forEach { surface ->
                    appliedTags.add(Tag(key, surface))
                }
                return appliedTags
            }
            "AddBikeParkingFee.kt", "AddParkingFee.kt" -> {
                val feeApplyTo = File(QUEST_ROOT_WITH_SLASH_ENDING + "parking_fee/Fee.kt")
                val fromFee = addedOrEditedTagsRealParsingFindRealEditFunctionViaApplyToFunction(description, feeApplyTo, fileSourceCode, suspectedAnswerEnumFiles)!!
                if (Tag("fee", "yes") !in fromFee) {
                    throw ParsingInterpretationException("is it even working - no, as fee=yes is missing")
                }
                val maxstayApplyTo = File(QUEST_ROOT_WITH_SLASH_ENDING + "parking_fee/Maxstay.kt")
                val fromMaxstay = addedOrEditedTagsRealParsingFindRealEditFunctionViaApplyToFunction(description, maxstayApplyTo, fileSourceCode, suspectedAnswerEnumFiles)!!
                return fromFee + fromMaxstay
            }
            else -> return null
        }
    }

    private fun listOfSurfaceValuesInSurfaceQuest(questFile: File): MutableList<String> {
        val formFile = formFileUsedInquest(questFile.parse())
        val identifiersOfFormItemsMayBeGroups = listOfIdentifiersDeclaringFormItems(formFile)
        return retrieveSurfaceValuesFromGroupIdentifiers(identifiersOfFormItemsMayBeGroups)
    }

    private fun retrieveSurfaceValuesFromGroupIdentifiers(identifiersOfFormItemsMayBeGroups: List<String>?): MutableList<String> {
        val structures = obtainSurfaceClassificationStructure()
        val returned = mutableListOf<String>()
        identifiersOfFormItemsMayBeGroups!!.forEach {
            if(it in structures) {
                structures[it]!!.forEach { surface ->
                    returned.add(surface)
                }
            } else {
                throw ParsingInterpretationException("not supported for now = $it is not in structure")
            }
        }
        return returned
    }

    private fun listOfIdentifiersDeclaringFormItems(formFile:File): MutableList<String>? {
        val astForm = formFile.parse()

        listOfClassPropertyDeclaration(astForm).forEach { propertyDeclaration ->
            val variableDeclaration = propertyDeclaration.locateSingleOrNullByDescription("variableDeclaration")
            val getter = propertyDeclaration.locateSingleOrNullByDescription("getter")
            if (variableDeclaration != null && getter != null) {
                val identifierOfProperty = (variableDeclaration.tree() as KlassIdentifier).identifier
                if(identifierOfProperty == "items") {
                    val identifiersOfElements = mutableListOf<String>()
                    getter.locateByDescription("simpleIdentifier").forEach {
                        val identifier = (it.tree() as KlassIdentifier).identifier
                        if(identifier != "toItems") { // TODO skip it via proper parsing
                            identifiersOfElements.add(identifier)
                        }
                    }
                    return identifiersOfElements
                }
            }
        }
        return null
    }

    private fun listOfClassPropertyDeclaration(ast: Ast): List<Ast> {
        val returned = mutableListOf<Ast>()
        ast.locateByDescription("classMemberDeclaration").forEach { classMemberDeclaration ->
            val declarations = classMemberDeclaration.locateByDescriptionDirectChild("declaration")
            if(declarations.size != 1) {
                classMemberDeclaration.showHumanReadableTree()
                declarations.forEach {
                    println("listOfClassPropertyDeclaration is failing, reporting declaration")
                    it.showHumanReadableTree()
                }
                throw ParsingInterpretationException("${declarations.size} multiple declarations")
            }
            val declaration = declarations[0]
            val propertyDeclaration = declaration.locateSingleOrNullByDescriptionDirectChild("propertyDeclaration")
            if (propertyDeclaration != null) {
                returned.add(propertyDeclaration)
            }
        }
        return returned
    }

    private fun formFileUsedInquest(ast: Ast): File {
        val functionToGetForm = ast.extractFunctionByName("createForm")!!
        val formUsed = (functionToGetForm.locateSingleOrExceptionByDescription("primaryExpression")
            .locateSingleOrExceptionByDescription("simpleIdentifier").tree() as KlassIdentifier).identifier
        println(formUsed)
        return File(QUEST_ROOT_WITH_SLASH_ENDING + "surface/$formUsed.kt")
    }
    class namedList(val name: String, var elements: List<String>) {
        override fun toString(): String {
            return "namedList($name, $elements)"
        }
    }
    private  fun obtainSurfaceClassificationStructure(): Map<String, List<String>> {
        val answersFile = File(QUEST_ROOT_WITH_SLASH_ENDING + "surface/Surface.kt")
        val localDescription = "${answersFile.parentFile.name}/${answersFile.name} hack"
        val surfacesIdentifierToValue = mutableMapOf<String, String>()
        getEnumValuesDefinedInThisFile(localDescription, answersFile).forEach {
            if(it.fields.size != 1) {
                throw ParsingInterpretationException("unexpected")
            }
            surfacesIdentifierToValue[it.identifier] = it.fields[0].possibleValue
        }
        val structures = mutableMapOf<String, List<String>>()
        val surfacesFileCode = loadFileText(answersFile)
        val astSurfaceGroupsDefinitions = answersFile.parse()
        astSurfaceGroupsDefinitions.locateByDescription("topLevelObject").forEach { topLevelObject ->
            val propertyDeclarations = topLevelObject.locateSingleOrExceptionByDescriptionDirectChild("declaration")
                .locateByDescriptionDirectChild("propertyDeclaration")
            if (propertyDeclarations.size == 1) {
                val propertyDeclaration = propertyDeclarations[0]
                val expressions = propertyDeclaration.locateByDescriptionDirectChild("expression")

                val nameOfDefinedGroup = (propertyDeclaration.locateSingleOrExceptionByDescriptionDirectChild("variableDeclaration")
                    .locateSingleOrExceptionByDescriptionDirectChild("simpleIdentifier").tree() as KlassIdentifier).identifier
                if (nameOfDefinedGroup !in listOf("shouldBeDescribed")) {
                    val entries = mutableListOf<String>()
                    if (expressions.size > 1) {
                        propertyDeclaration.showHumanReadableTreeWithSourceCode("multiple expressions present", surfacesFileCode)
                    } else {
                        if (expressions[0].relatedSourceCode(surfacesFileCode).startsWith("listOf(")) {
                            val list = expressions[0].locateSingleOrExceptionByDescription("callSuffix") // will fail with multiple layers of calls
                                .locateSingleOrExceptionByDescriptionDirectChild("valueArguments")
                                .locateByDescriptionDirectChild("valueArgument")
                            list.forEach {
                                entries.add(it.relatedSourceCode(surfacesFileCode))
                            }
                        } else {
                            println("<${expressions[0].relatedSourceCode(surfacesFileCode)}> is not supported, only listOf is")
                        }
                    }
                    // println()
                    // println("$nameOfDefinedGroup = $entries")
                    structures[nameOfDefinedGroup] = entries.map{ surfacesIdentifierToValue[it]!! }
                    // println()
                }
            } else {
                // val explanation = "${propertyDeclarations.size} propertyDeclarations present, for example an enum has 0"
                // println()
                // topLevelObject.showHumanReadableTreeWithSourceCode(explanation, surfacesFileCode)
                // topLevelObject.showRelatedSourceCode(explanation, surfacesFileCode)
                // println(explanation)
                // println()
                // TODO is silent skipping really OK? Maybe it can be skipped in some smarter way?
                // throw ParsingInterpretationException(explanation)
            }
        }
        return structures
    }

    private fun addedOrEditedTagsRealParsing(description: String, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): Set<Tag>? {
        val ast = AstSource.String(description, fileSourceCode).parse()
        val defaultFunction = ast.extractFunctionByName(NAME_OF_FUNCTION_EDITING_TAGS)!!
        val functionSourceCode = defaultFunction.relatedSourceCode(fileSourceCode)
        if ("answer.applyTo(" !in functionSourceCode && "answer.litStatus.applyTo" !in functionSourceCode) {
            return addedOrEditedTagsWithGivenFunction(description, fileSourceCode, "tags", NAME_OF_FUNCTION_EDITING_TAGS, suspectedAnswerEnumFiles)
        } else {
            suspectedAnswerEnumFiles.forEach { fileHopefullyWithApplyTo ->
                val found = fileHopefullyWithApplyTo.parse().extractFunctionByName("applyTo")
                if (found != null) {
                    // OK, so we found related file providing applyTo function. Great!
                    if ("ParkingFee" in description) {
                        println("$description fpund apply to file $fileHopefullyWithApplyTo")
                    }
                    val got = addedOrEditedTagsRealParsingFindRealEditFunctionViaApplyToFunction(description, fileHopefullyWithApplyTo, fileSourceCode, suspectedAnswerEnumFiles)

                    val bonusScan = addedOrEditedTagsWithGivenFunction(description, fileSourceCode, "tags", NAME_OF_FUNCTION_EDITING_TAGS, suspectedAnswerEnumFiles)
                    if (bonusScan != null && bonusScan.isNotEmpty()) {
                        println(bonusScan)
                        throw ParsingInterpretationException("turns out to be needed")
                    }

                    if (got != null) {
                        return got
                    }
                }
            }
        }
        return null
    }

    private fun addedOrEditedTagsRealParsingFindRealEditFunctionViaApplyToFunction(description: String, fileWithRedirectedFunction: File, originalFileSourceCode: String, suspectedAnswerEnumFiles: List<File>): Set<Tag>? {
        val found = fileWithRedirectedFunction.parse().extractFunctionByName("applyTo")!!
        val ast = AstSource.String(description, originalFileSourceCode).parse()
        val defaultFunction = getAstTreeForFunctionEditingTags(description, ast)
        val parameters = found.locateSingleOrExceptionByDescriptionDirectChild("functionValueParameters")
            .locateByDescriptionDirectChild("functionValueParameter")
        if (parameters.isEmpty()) {
            throw ParsingInterpretationException("unsupported")
        }
        val parametersInCalledFunction = mutableListOf<String>()
        for (element in parameters) {
            val parameter = element.locateSingleOrExceptionByDescriptionDirectChild("parameter")
            val parameterTree = parameter.tree()
            if (parameterTree is KlassIdentifier) {
                parametersInCalledFunction.add(parameterTree.identifier)
            } else {
                throw ParsingInterpretationException("should not happen")
            }
        }
        if (parameters.size > 1) {
            println("DECOMPOSITION")
            println("DECOMPOSITION")
            println("DECOMPOSITION")
            println(defaultFunction.relatedSourceCode(originalFileSourceCode))
            val statements = defaultFunction.locateByDescription("statements")
            if (statements.size > 1) {
                println("unexpectedly many statements")
                return null
            }
            defaultFunction.locateSingleOrExceptionByDescription("statements")
                .locateByDescription("statement").forEach {
                    val getDownInTree = it.locateSingleOrExceptionByDescriptionDirectChild("expression")
                        .locateSingleOrExceptionByDescriptionDirectChild("disjunction")
                        .locateSingleOrExceptionByDescriptionDirectChild("conjunction")
                        .locateSingleOrExceptionByDescriptionDirectChild("equality")
                        .locateSingleOrExceptionByDescriptionDirectChild("comparison")
                        .locateSingleOrExceptionByDescriptionDirectChild("genericCallLikeComparison")
                        // yes, it is absurd. No idea what is going on here
                        .locateSingleOrExceptionByDescriptionDirectChild("infixOperation")
                        .locateSingleOrExceptionByDescriptionDirectChild("elvisExpression")
                        .locateSingleOrExceptionByDescriptionDirectChild("infixFunctionCall")
                        .locateSingleOrExceptionByDescriptionDirectChild("rangeExpression")
                        .locateSingleOrExceptionByDescriptionDirectChild("additiveExpression")
                        // wtf
                        .locateSingleOrExceptionByDescriptionDirectChild("multiplicativeExpression")
                        .locateSingleOrExceptionByDescriptionDirectChild("asExpression")
                        .locateSingleOrExceptionByDescriptionDirectChild("prefixUnaryExpression")
                        .locateSingleOrExceptionByDescriptionDirectChild("postfixUnaryExpression")
                    val primaryExpression = getDownInTree.locateSingleOrExceptionByDescriptionDirectChild("primaryExpression")
                    val postfixUnarySuffixes = getDownInTree.locateByDescriptionDirectChild("postfixUnarySuffix")
                    if (primaryExpression.relatedSourceCode(originalFileSourceCode) != "answer") {
                        throw ParsingInterpretationException("Investigate and replace by a proper check once this is triggered")
                    }
                    if (postfixUnarySuffixes[0].relatedSourceCode(originalFileSourceCode) != ".applyTo") {
                        throw ParsingInterpretationException("Inverstogate and replace by a proper check once this is triggered")
                    }
                    if (postfixUnarySuffixes.size > 2) {
                        throw ParsingInterpretationException("No support yet")
                    }
                    postfixUnarySuffixes[postfixUnarySuffixes.size - 1]
                        .locateSingleOrExceptionByDescriptionDirectChild("callSuffix")
                        .showHumanReadableTreeWithSourceCode("AAAAAAAAAAAAAAAAAAAAA callSuffix", originalFileSourceCode)
                }
            println("DECOMPOSITION")
            println("DECOMPOSITION")
            println("DECOMPOSITION")
            println("$description - parametersInCalledFunction in file ${fileWithRedirectedFunction.name} $parametersInCalledFunction")
            println("No support yet")
            return null
            // throw ParsingInterpretationException("No support yet")
        }
        if (parametersInCalledFunction[0] == "tags") {
            val replacementParameter = "tags"
            val replacementFunctionName = "applyTo"
            val replacementSourceCode = loadFileText(fileWithRedirectedFunction)
            val replacementDescription = fileWithRedirectedFunction.toString()
            return addedOrEditedTagsWithGivenFunction(replacementDescription, replacementSourceCode, replacementParameter, replacementFunctionName, suspectedAnswerEnumFiles)
        } else {
            // unsupported TODO
            // TODO - variable is not really supported within called function
            println("redirected function, not using tags variable - unsupported TODO, exiting")
            return null
        }
    }

    private fun addedOrEditedTagsWithGivenFunction(description: String, fileSourceCode: String, variable: String, relevantFunctionName: String, suspectedAnswerEnumFiles: List<File>): Set<Tag>? {
        val ast = AstSource.String(description, fileSourceCode).parse()
        val relevantFunction = ast.extractFunctionByName(relevantFunctionName)
        if (relevantFunction == null) {
            println(description)
            println(fileSourceCode)
            throw ParsingInterpretationException("$relevantFunctionName missing in code provided via $description!")
        }
        val appliedTags = mutableSetOf<Tag>()
        var failedExtraction = false
        var got = extractCasesWhereTagsAreAccessedWithIndex(description, relevantFunction, fileSourceCode, suspectedAnswerEnumFiles)
        if (got != null) {
            appliedTags += got
        } else {
            println("failedExtraction of $description - extractCasesWhereTagsAreAccessedWithIndex")
            failedExtraction = true
        }

        got = extractCasesWhereTagsAreAccessedWithFunction(description, relevantFunction, fileSourceCode, suspectedAnswerEnumFiles)
        if (got != null) {
            appliedTags += got
        } else {
            println("failedExtraction of $description - extractCasesWhereTagsAreAccessedWithFunction")
            failedExtraction = true
        }

        val tagsThatShouldBeMoreSpecific = appliedTags
            .filter { it.value == null && !freeformKey(it.key) && !streetCompleteIsReusingAnyValueProvidedByExistingTagging(description, it.key) }
        if (tagsThatShouldBeMoreSpecific.isNotEmpty()) {
            tagsThatShouldBeMoreSpecific.forEach { println(it) }
            println("$description found tags which are not freeform but have no speicified values")
            failedExtraction = true
        }
        if (appliedTags.size == 0) {
            return null // parsing definitely failed
        }
        if (failedExtraction) {
            return null
        }
        return appliedTags
    }

    private fun extractTextFromHardcodedString(passedTextHolder: Ast): String? {
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
                if (assignsToTagsVariable(tagsDictAccess)) {
                    // this limits it to things like
                    // tags[something] = somethingElse
                    // (would it also detect tags=whatever)?
                    val indexingElement = tagsDictAccess.locateSingleOrExceptionByDescription("assignableSuffix")
                        .locateSingleOrExceptionByDescription("indexingSuffix")
                    // indexingElement is something like ["indoor"] or [key]
                    val expression = indexingElement.locateSingleOrExceptionByDescriptionDirectChild("expression") // drop outer [ ]
                    val potentialTexts = expression.locateByDescription("stringLiteral", debug = false) // what if it is something like "prefix" + CONSTANT ?
                    val potentiallyUsableExpression = if (expression is KlassIdentifier) { expression } else { null } //
                    val likelyVariable = expression.locateByDescriptionDirectChild("disjunction") // tag[key] = ... for example
                    if (potentialTexts.size == 1) {
                        val processed = potentialTexts[0].tree()
                        if (processed == null) {
                            throw ParsingInterpretationException("not handled")
                        }
                        val key = extractTextFromHardcodedString(processed)
                        if (key == null) {
                            processed.showRelatedSourceCode("***** - key not found", fileSourceCode)
                            throw ParsingInterpretationException("not handled")
                        } else {
                            // assignment (for example tags["highway"] = "steps" ) is expected to have following children:
                            // directlyAssignableExpression ( for example tags["highway"] )
                            // WS
                            // ASSIGNMENT =
                            // WS
                            // expression ( for example: "steps" )
                            val valueHolder = assignment.locateSingleOrExceptionByDescriptionDirectChild("expression")
                            appliedTags += extractValuesForKnownKey(description, key, valueHolder, fileSourceCode, suspectedAnswerEnumFiles)
                        }
                    } else if (potentiallyUsableExpression != null) {
                        expression.showHumanReadableTree()
                        expression.showRelatedSourceCode("expression in identified access as a variable", fileSourceCode)
                        println(KotlinGrammarParserType.identifier.toString() + " identified as accessing index as a variable (potentialTexts.size = ${potentialTexts.size})")
                        return null
                    } else if (likelyVariable.size == 1) {
                        if (likelyVariable[0].relatedSourceCode(fileSourceCode) == "key" && "name:\$languageTag" in fileSourceCode) {
                            // special handling for name quests
                            possibleLanguageKeys().forEach { appliedTags.add(Tag(it, null)) }
                        } else {
                            expression.showHumanReadableTree()
                            expression.showRelatedSourceCode("expression in identified access as a complex variable", fileSourceCode)
                            println(likelyVariable[0].relatedSourceCode(fileSourceCode) + " identified as accessing index as a complex variable (potentialTexts.size = ${potentialTexts.size})")
                            return null
                        }
                    } else {
                        expression.showRelatedSourceCode("expression - not handled, expression::class is ${expression::class}", fileSourceCode)
                        expression.showHumanReadableTree()
                        throw ParsingInterpretationException("not handled, ${potentialTexts.size} texts, $potentiallyUsableExpression variable")
                    }
                }
            }
        }
        return appliedTags
    }

    private fun assignsToTagsVariable(tagsDictAccess: Ast): Boolean {
        return tagsDictAccess.description == "directlyAssignableExpression" &&
            tagsDictAccess is DefaultAstNode &&
            tagsDictAccess.children[0].tree() is KlassIdentifier &&
            ((tagsDictAccess.children[0].tree() as KlassIdentifier).identifier == "tags")
    }

    class EnumFieldState(val identifier: String, val possibleValue: String) {
        // entry such as
        // osmKey = building
        // from
        // HOUSE           ("building", "house"),
        // from
        // enum class BuildingType(val osmKey: String, val osmValue: String) {
        override fun toString(): String {
            return "EnumFieldState($identifier, $possibleValue)"
        }
    }

    class EnumEntry(val identifier: String, val fields: List<EnumFieldState>) {
        // entry such as
        // HOUSE           ("building", "house"),
        // from
        // enum class BuildingType(val osmKey: String, val osmValue: String) {
        override fun toString(): String {
            return "EnumEntry($identifier, $fields)"
        }
    }

    private fun getEnumValuesDefinedInThisFile(description: String, file: File, debug: Boolean = false): Set<EnumEntry> {
        val values = mutableSetOf<EnumEntry>()
        val fileMaybeContainingEnumSourceCode = loadFileText(file)
        val potentialEnumFileAst = file.parse()
        var enumsTried = 0
        potentialEnumFileAst.locateByDescription("classDeclaration").forEach { enum ->
            val modifiers = enum.locateByDescription("modifiers")
            if (modifiers.size != 1) {
                // not expected to be enum
                // will happen if potential enum file contains rather class such as
                // class StreetSideSelectRotateContainer @JvmOverloads constructor(
                return@forEach // skip silently as heuristic being too eager
            } else if (modifiers[0].relatedSourceCode(fileMaybeContainingEnumSourceCode) == "enum") {
                enumsTried += 1
                val enumFieldNames = mutableListOf<String>()
                val constructor = enum.locateSingleOrNullByDescription("primaryConstructor")
                if (constructor == null) {
                    // may happen with helper enums being present, such as
                    // enum class FireHydrantDiameterMeasurementUnit { MILLIMETER, INCH }
                    return@forEach // skip silently as heuristic being too eager
                }
                constructor.locateSingleOrExceptionByDescriptionDirectChild("classParameters")
                    .locateByDescriptionDirectChild("classParameter")
                    .forEach {
                        // val type = it.locateSingleOrExceptionByDescriptionDirectChild("type")
                        //    .relatedSourceCode(fileMaybeContainingEnumSourceCode)
                        val simpleIdentifier = it.locateSingleOrExceptionByDescriptionDirectChild("simpleIdentifier")
                            .relatedSourceCode(fileMaybeContainingEnumSourceCode)
                        enumFieldNames.add(simpleIdentifier)
                    }
                enum.locateByDescription("enumEntry").forEach { enumEntry ->
                    /*
                    println("valueArguments of this entry follows")
                    valueArguments.showRelatedSourceCode("valueArguments", fileMaybeContainingEnumSourceCode)
                    println("primaryConstructor of entire enum follows")
                    enum.locateSingleOrExceptionByDescription("primaryConstructor")
                        .showHumanReadableTreeWithSourceCode(description, fileMaybeContainingEnumSourceCode)
                     */
                    var extractedText: String?
                    val identifier = (enumEntry.locateSingleOrNullByDescriptionDirectChild("simpleIdentifier")!!.tree() as KlassIdentifier).identifier
                    val valueArguments = enumEntry.locateSingleOrNullByDescriptionDirectChild("valueArguments")
                    if (valueArguments == null) {
                        val explanation = "parsing ${file.path} failed, valueArguments count is not 1, skipping, maybe it should be also investigated"
                        println(enum.showRelatedSourceCode(explanation, fileMaybeContainingEnumSourceCode))
                        println(explanation)
                    } else {
                        val enumFieldGroup = mutableListOf<EnumFieldState>()
                        val arguments = valueArguments.locateByDescriptionDirectChild("valueArgument")
                        for (i in arguments.indices) {
                            extractedText = extractTextFromHardcodedString(arguments[i])
                            if (extractedText == null) {
                                if (arguments[i].tree() is KlassDeclaration && (arguments[i].tree() as KlassDeclaration).identifier.toString() == "null") {
                                    // it has null as value, apparently
                                    // lest skip it silently
                                } else {
                                    println("showHumanReadableTreeWithSourceCode(fileMaybeContainingEnumSourceCode) - showing ${file.path} after enum extraction failed")
                                    valueArguments.showHumanReadableTreeWithSourceCode(description, fileMaybeContainingEnumSourceCode)
                                    println("showHumanReadableTreeWithSourceCode(fileMaybeContainingEnumSourceCode) - shown ${file.path} after enum extraction failed")
                                    println(fileMaybeContainingEnumSourceCode)
                                    println("source code displayed - shown ${file.path} after enum extraction failed")
                                }
                            } else {
                                enumFieldGroup.add(EnumFieldState(enumFieldNames[i], extractedText))
                            }
                        }
                        if (enumFieldGroup.size > 0) {
                            values.add(EnumEntry(identifier, enumFieldGroup))
                        }
                    }
                }
            }
        }
        if (values.size == 0 && debug) {
            println("enum extraction from ${file.path} failed! $enumsTried potential enums tried ($description request)")
        }
        return values
    }

    private fun extractValuesForKnownKey(description: String, key: String, valueHolder: Ast, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): MutableSet<Tag> {
        val appliedTags = mutableSetOf<Tag>()

        var scanned: MutableSet<Tag>?
        scanned = extractValuesForKnownKeyFromWhenExpressionIfSingleOneIsPresent(description, key, valueHolder, fileSourceCode, suspectedAnswerEnumFiles)
        if (scanned != null) {
            return scanned
        }

        scanned = extractValuesForKnownKeyFromIfExpressionIfSingleOneIsPresent(description, key, valueHolder, fileSourceCode, suspectedAnswerEnumFiles)
        if (scanned != null) {
            return scanned
        }

        val valueIfItIsSimpleText = extractTextFromHardcodedString(valueHolder)
        val valueHolderSourceCode = valueHolder.relatedSourceCode(fileSourceCode)
        if (valueIfItIsSimpleText != null) {
            appliedTags.add(Tag(key, valueIfItIsSimpleText))
        } else if (valueHolderSourceCode.endsWith(".toYesNo()")) {
            // previous form of check:
            // in listOf("answer.toYesNo()", "it.toYesNo()", "answer.credit.toYesNo()", "answer.debit.toYesNo()", "isAutomated.toYesNo()")
            // maybe treat this hack by proper parse and detect toYesNo() at the end?
            // or maybe this is a valid check given high coupling with StreetComplete being presenrt anyway?
            appliedTags.add(Tag(key, "yes"))
            appliedTags.add(Tag(key, "no"))
        } else if (valueHolderSourceCode.endsWith(".toCheckDateString()")) {
            appliedTags.add(Tag(key, null))
        } else if (valueHolderSourceCode == "answer.joinToString(\";\") { it.osmValue }") { // answer.joinToString(";") { it.osmValue }
            // println("answer.joinToString(\";\") { it.osmValue } investigation")
            // valueHolder.showHumanReadableTreeWithSourceCode(description, fileSourceCode)
            // valueHolder.showRelatedSourceCode("answer.joinToString(\";\") { it.osmValue } investigation", fileSourceCode)
            // println("answer.joinToString(\";\") { it.osmValue } investigation")
            val filtered = valueHolder.locateSingleOrExceptionByDescription("lambdaLiteral").locateSingleOrExceptionByDescriptionDirectChild("statements")
            appliedTags += provideTagsBasedOnAswerDataStructuresFromExternalFiles(description, key, filtered, fileSourceCode, suspectedAnswerEnumFiles)
            appliedTags.add(Tag(key, null)) // as it can be joined in basically any combination and listing all permutations would be absurd. Maybe provide comment here of taginfo listing supports this?
        } else if (valueHolderSourceCode.startsWith("answer.") || valueHolderSourceCode.startsWith("this.")) {
            appliedTags += provideTagsBasedOnAswerDataStructuresFromExternalFiles(description, key, valueHolder, fileSourceCode, suspectedAnswerEnumFiles)
        } else if (key == "landuse" && "OrchardProduce.kt" in description) {
            /*
            OrchardProduce.kt has this special

            val landuse = answer.singleOrNull()?.osmLanduseValue
            if (landuse != null) {
                tags["landuse"] = landuse
            }

            which would be obnoxious to actually support
            */
            suspectedAnswerEnumFiles.forEach {
                getEnumValuesDefinedInThisFile(description, it).forEach { enumGroup ->
                    enumGroup.fields.forEach { value ->
                        if (value.identifier == "osmLanduseValue") {
                            appliedTags.add(Tag(key, value.possibleValue))
                        }
                    }
                }
            }
        } else {
            if ( freeformKey(key) || streetCompleteIsReusingAnyValueProvidedByExistingTagging(description, key)) {
                appliedTags.add(Tag(key, null))
            } else {
                println()
                println()
                println()
                val explanation = "exact value is missing, extractValuesForKnownKey failed. $description get value (key is known: $key) from <$valueHolderSourceCode> somehow..."
                println(explanation)
                valueHolder.showHumanReadableTreeWithSourceCode(description, fileSourceCode)
                valueHolder.showRelatedSourceCode(explanation, fileSourceCode)
                println(explanation)
                throw ParsingInterpretationException(explanation)
            }
        }
        return appliedTags
    }

    private fun provideTagsBasedOnAswerDataStructuresFromExternalFiles(description: String, key: String, valueHolder: Ast, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>, debug: Boolean = false): MutableSet<Tag> {
        val appliedTags = mutableSetOf<Tag>()
        var extractedSomething = false
        suspectedAnswerEnumFiles.forEach {
            getEnumValuesDefinedInThisFile(description, it).forEach { enumGroup ->
                enumGroup.fields.forEach { value ->
                    // why redefined in each cycle?
                    // because there are cases where it would fail - but these are also cases
                    // where extracting enum also fails, so is not triggered and can be ignored
                    val postfixUnarySuffixes = valueHolder.locateByDescription("postfixUnarySuffix")
                    if (postfixUnarySuffixes.size != 1) {
                        valueHolder.showHumanReadableTreeWithSourceCode(description, fileSourceCode)
                        valueHolder.showRelatedSourceCode(description, fileSourceCode)
                        throw ParsingInterpretationException("$key values extraction in provideTagsBasedOnAswerDataStructuresFromExternalFiles - postfixUnarySuffix expected to be a single one, got ${postfixUnarySuffixes.size}")
                    }
                    val accessIdentifierAst = postfixUnarySuffixes[0]
                        .locateSingleOrExceptionByDescriptionDirectChild("navigationSuffix")
                        .locateSingleOrExceptionByDescriptionDirectChild("simpleIdentifier")
                    val identifier = (accessIdentifierAst.tree() as KlassIdentifier).identifier

                    if (value.identifier == identifier) {
                        appliedTags.add(Tag(key, value.possibleValue))
                        extractedSomething = true
                        if (debug) {
                            println("$key=${value.possibleValue} registered based on ${value.identifier} identifier matching expected $identifier - from ${it.name}")
                        }
                    }
                }
            }
        }
        if (!freeformKey(key)) {
            // with freeform keys heuristic below will just get
            // variable such as capacity and will get confused
            // It is possible to get it working but not worth it right now
            suspectedAnswerEnumFiles.forEach { file ->
                /*
                //far more parsing is possible here to avoid false positives
                //to obtain possible values from files like this:

                package de.westnordost.streetcomplete.quests.shoulder

                data class ShoulderSides(val left: Boolean, val right: Boolean)

                val ShoulderSides.osmValue: String get() = when {
                    left && right -> "both"
                    left -> "left"
                    right -> "right"
                    else -> "no"
                }
                 */
                val code = loadFileText(file)
                val ast = file.parse()
                val classDeclarations = ast.locateByDescription("classDeclaration")
                if (classDeclarations.isEmpty()) {
                    return@forEach
                }
                ast.locateByDescription("propertyDeclaration").forEach {
                    val whenExpression = it.locateSingleOrNullByDescription("whenExpression")
                    if (whenExpression != null) {
                        extractValuesForKnownKeyFromWhenExpression(description, "dummykey", whenExpression, code, listOf<File>()).forEach {
                            if (debug) {
                                println("OBTAINED FROM WHEN IN CLASS DECLARATION! $description $key=${it.value}")
                            }
                            appliedTags.add(Tag(key, it.value))
                            extractedSomething = true
                        }
                    }
                }
            }
        }

        if (!extractedSomething) {
            appliedTags.add(Tag(key, null))
            if ( freeformKey(key) || streetCompleteIsReusingAnyValueProvidedByExistingTagging(description, key)) {
                // no reason to complain
            } else {
                println("$description = ${valueHolder.relatedSourceCode(fileSourceCode)}, failed to find values for now - key is $key<")
                valueHolder.showHumanReadableTreeWithSourceCode(description, fileSourceCode)
                println("$description = ${valueHolder.relatedSourceCode(fileSourceCode)}, failed to find values for now - key is $key>")
            }
        }
        return appliedTags
    }

    private fun extractValuesForKnownKeyFromIfExpressionIfSingleOneIsPresent(description: String, key: String, valueHolder: Ast, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): MutableSet<Tag>? {
        val ifExpression = valueHolder.locateSingleOrNullByDescription("ifExpression")
        if (ifExpression != null) {
            if (ifExpression.relatedSourceCode(fileSourceCode) == valueHolder.relatedSourceCode(fileSourceCode)) {
                return extractValuesForKnownKeyFromIfExpression(description, key, ifExpression, fileSourceCode, suspectedAnswerEnumFiles)
            } else {
                throw ParsingInterpretationException("not handled, when expressions as part of something bigger")
            }
        }
        return null
    }

    private fun extractValuesForKnownKeyFromIfExpression(description: String, key: String, ifExpression: AstNode, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): MutableSet<Tag> {
        val appliedTags = mutableSetOf<Tag>()
        ifExpression.locateByDescription("controlStructureBody").forEach {
            appliedTags += extractValuesForKnownKey(description, key, it, fileSourceCode, suspectedAnswerEnumFiles)
        }
        return appliedTags
    }

    private fun extractValuesForKnownKeyFromWhenExpressionIfSingleOneIsPresent(description: String, key: String, valueHolder: Ast, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): MutableSet<Tag>? {
        val whenExpression = valueHolder.locateSingleOrNullByDescription("whenExpression")
        if (whenExpression != null) {
            if (whenExpression.relatedSourceCode(fileSourceCode) == valueHolder.relatedSourceCode(fileSourceCode)) {
                return extractValuesForKnownKeyFromWhenExpression(description, key, whenExpression, fileSourceCode, suspectedAnswerEnumFiles)
            } else {
                throw ParsingInterpretationException("not handled, when expressions as part of something bigger")
            }
        }
        return null
    }

    private fun extractValuesForKnownKeyFromWhenExpression(description: String, key: String, whenExpression: AstNode, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): MutableSet<Tag> {
        val appliedTags = mutableSetOf<Tag>()
        whenExpression.locateByDescription("whenEntry").forEach { it ->
            val structure = it.children.filter { it.description != "WS" }
            val expectedStructureA = listOf("whenCondition", "ARROW", "controlStructureBody", "semi")
            val expectedStructureB = listOf("ELSE", "ARROW", "controlStructureBody", "semi")
            areDirectChildrenMatchingStructureThrowExceptionIfNot(description, listOf(expectedStructureA, expectedStructureB), it, fileSourceCode, eraseWhitespace = true)
            appliedTags += extractValuesForKnownKey(description, key, structure[2], fileSourceCode, suspectedAnswerEnumFiles)
        }
        return appliedTags
    }

    private fun areDirectChildrenMatchingStructureThrowExceptionIfNot(description: String, expectedStructures: List<List<String>>, expression: AstNode, fileSourceCode: String, eraseWhitespace: Boolean) {
        val structure = expression.children.filter { !(eraseWhitespace && it.description == "WS") }.map { it.description }
        expectedStructures.forEach {
            if (it == structure) {
                return
            }
        }
        var maxLength = 0
        expectedStructures.forEach { if (maxLength < it.size) { maxLength = it.size } }
        for (i in 0 until maxLength) {
            expectedStructures.forEach {
                if (it.size > i) {
                    if (it[i] != structure[i]) {
                        println("STRUCTURE FAILED")
                        println("WHEN STRUCTURE FAILED")
                        expression.showHumanReadableTreeWithSourceCode(description, fileSourceCode)
                        expression.showRelatedSourceCode("WHEN STRUCTURE FAILED", fileSourceCode)
                        println(expression.showRelatedSourceCode("WHEN STRUCTURE FAILED", fileSourceCode))
                        println()
                        structure.forEach { println(it) }
                        throw ParsingInterpretationException("unexpected structure! at $i index")
                    }
                }
            }
        }
    }

    private fun surveyMarkKeyBasedOnKey(key: String): String {
        // TODO - can we directly call relevant StreetComplete code?
        return "$SURVEY_MARK_KEY:$key"
    }

    private fun extractCasesWhereTagsAreAccessedWithFunction(description: String, relevantFunction: AstNode, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): Set<Tag>? {
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
                    val keyString = extractStringLiteralArgumentInFunctionCall(description, 0, accessingTagsWithFunction, fileSourceCode)
                    if (keyString != null) {
                        appliedTags.add(Tag(surveyMarkKeyBasedOnKey(keyString), null))
                    }
                } else if (functionName ==  "updateWithCheckDate") {
                    var keyString = extractStringLiteralArgumentInFunctionCall(description, 0, accessingTagsWithFunction, fileSourceCode)
                    val valueString = extractStringLiteralArgumentInFunctionCall(description, 1, accessingTagsWithFunction, fileSourceCode)

                    // fold it into extractArgumentInFunctionCall?
                    // try to automatically obtain this constants?
                    if (keyString == null) {
                        val keyArgumentAst = extractArgumentSyntaxTreeInFunctionCall(0, accessingTagsWithFunction, fileSourceCode).locateSingleOrNullByDescription("primaryExpression")
                        if (keyArgumentAst == null) {
                            throw ParsingInterpretationException("unexpected")
                        }
                        val keyArgumentAstTree = keyArgumentAst.tree()
                        if (keyArgumentAstTree is KlassIdentifier) {
                            if (keyArgumentAstTree.identifier == "SOUND_SIGNALS") {
                                keyString = SOUND_SIGNALS
                            }
                            if (keyArgumentAstTree.identifier == "VIBRATING_BUTTON") {
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
                            val valueHolderSourceCode = valueAst.relatedSourceCode(fileSourceCode)
                            if (valueHolderSourceCode == "answer.toYesNo()") {
                                // kind of hackish, fix this?
                                appliedTags.add(Tag(keyString, "yes"))
                                appliedTags.add(Tag(keyString, "no"))
                            } else if (valueHolderSourceCode == "answer.osmValue" || valueHolderSourceCode == "answer.value.osmValue") {
                                val dotAcess = valueAst.locateByDescription("postfixUnarySuffix")
                                if (dotAcess.isEmpty()) {
                                    throw ParsingInterpretationException("hmmmmmmmm")
                                }
                                val accessIdentifierAst = dotAcess[dotAcess.size - 1].locateSingleOrExceptionByDescriptionDirectChild("navigationSuffix")
                                    .locateSingleOrExceptionByDescriptionDirectChild("simpleIdentifier")
                                val identifier = (accessIdentifierAst.tree() as KlassIdentifier).identifier
                                var extractedNothing = true
                                suspectedAnswerEnumFiles.forEach {
                                    getEnumValuesDefinedInThisFile(description, it).forEach { value ->
                                        // dotAcess will have a single element [.osmValue] on "answer.osmValue"
                                        // dotAcess will have a two elemente [.value, .osmValue] on "answer.value.osmValue"
                                        if (value.fields.size != 1) {
                                            throw ParsingInterpretationException("expected a single value, got $value")
                                        }
                                        if (value.fields[0].identifier == identifier) {
                                            appliedTags.add(Tag(keyString, value.fields[0].possibleValue))
                                        }
                                        extractedNothing = false
                                    }
                                }
                                if (extractedNothing) {
                                    appliedTags.add(Tag(keyString, valueString))
                                    println("Enum obtaining failed! suspectedAnswerEnumFiles $suspectedAnswerEnumFiles")
                                    println("44444444444444<<< tags dict is accessed with updateWithCheckDate, key known ($keyString), value unknown, enum obtaining failed<")
                                    valueAst.showHumanReadableTreeWithSourceCode(description, fileSourceCode)
                                    valueAst.showRelatedSourceCode("extracted valueAst in tags dict access", fileSourceCode)
                                    println(">>>44444444444>")
                                    accessingTagsWithFunction.showRelatedSourceCode("extracted accessingTagsWithFunction in tags dict access", fileSourceCode)
                                    println(">>>33333333333>")
                                }
                            } else {
                                val valueSourceCode = valueAst.relatedSourceCode(fileSourceCode)
                                if (freeformKey(keyString) && valueSourceCode in setOf("answer.toString()", "openingHoursString", "answer.times.toString()", "duration.toOsmValue()", "toOsmValue()")) {
                                    // key is freeform and it appears to not be enum - so lets skip complaining and attempting to tarck down value
                                    // individual quests can be investigated as needed
                                    appliedTags.add(Tag(keyString, null))
                                } else {
                                    appliedTags.add(Tag(keyString, valueString))
                                    val explanation = "extractCasesWhereTagsAreAccessedWithFunction - extraction failing: $description tags dict is accessed with updateWithCheckDate, key known ($keyString), value unknown, obtaining data failed"
                                    println("XXXXXXXXXXXXXXXXXXXXX<<< $explanation<")
                                    valueAst.showHumanReadableTreeWithSourceCode(description, fileSourceCode)
                                    valueAst.showRelatedSourceCode("extracted valueAst in tags dict access", fileSourceCode)
                                    println(">>>VVVVVVVVVVVVVVVVVV> $description")
                                    println(accessingTagsWithFunction.relatedSourceCode(fileSourceCode))
                                    println(">>>IIIIIIIIIIIIIIIIIIIII> $description")
                                    println(relevantFunction.relatedSourceCode(fileSourceCode))
                                    println(">>>0000000000000000000> $description")
                                    println(suspectedAnswerEnumFiles)
                                    println("-1 -1 -1")
                                    println(explanation)
                                    if (freeformKey(keyString)) {
                                        println("freeform, but accessed with $valueSourceCode which is not listed")
                                    }
                                    println()
                                    println()
                                }
                            }
                        }
                    } else {
                        val description = "^^^^^^^^^^^^^^^^ $description - failed to extract key from updateWithCheckDate"
                        println(description)
                        // val keyString = extractArgumentInFunctionCall(description, 0, accessingTagsWithFunction, fileSourceCode)
                        val keyArgumentAst = extractArgumentSyntaxTreeInFunctionCall(0, accessingTagsWithFunction, fileSourceCode).locateSingleOrNullByDescription("primaryExpression")
                        keyArgumentAst!!.relatedSourceCode(fileSourceCode)
                        keyArgumentAst.showHumanReadableTreeWithSourceCode(description, fileSourceCode)
                        println("^&^&^&^&")
                        throw ParsingInterpretationException(description)
                    }
                } else if (functionName in listOf("remove", "containsKey", "removeCheckDatesForKey", "hasChanges", "entries", "hasCheckDateForKey", "hasCheckDate")) {
                    // skip, as only added or edited tags are listed - and removed one and influencing ones are ignored
                } else if (functionName in listOf("updateCheckDate")) {
                    appliedTags.add(Tag(SURVEY_MARK_KEY, null))
                } else if (functionName == "replaceShop") {
                    // this is gate to use of NSI tagging ( https://github.com/osmlab/name-suggestion-index/ )
                    // worse - not entire, only segment of it...
                    // so NSI would be parsed in turn...
                    // parsing skipped per
                    // https://github.com/streetcomplete/StreetComplete/issues/4225#issuecomment-1190487094
                } else {
                    throw ParsingInterpretationException("unexpected function name $functionName in $description")
                }
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

    private fun extractStringLiteralArgumentInFunctionCall(description: String, index: Int, ast: AstNode, fileSourceCode: String): String? {
        val found = extractArgumentSyntaxTreeInFunctionCall(index, ast, fileSourceCode).locateSingleOrNullByDescription("primaryExpression")
        if (found == null) {
            println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA extractArgumentInFunctionCall failed")
            ast.tree()!!.showHumanReadableTreeWithSourceCode(description, fileSourceCode)
            ast.tree()!!.showRelatedSourceCode("extractArgumentInFunctionCall", fileSourceCode)
            ast.showRelatedSourceCode("extractArgumentInFunctionCall - not found", fileSourceCode)
            ast.tree()!!.showRelatedSourceCode("extractArgumentInFunctionCall - not found (rooted)", fileSourceCode)
            println("${extractArgumentSyntaxTreeInFunctionCall(index, ast, fileSourceCode)} - extractArgumentSyntaxTreeInFunctionCall(index, ast, fileSourceCode)")
            println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA extractArgumentInFunctionCall failed")
            return null
        }
        if (found.children.size == 1) {
            if (found.children[0].description == "stringLiteral") {
                val stringObject = (found.children[0].tree() as KlassString).children[0]
                return (stringObject as StringComponentRaw).string
            } else {
                /*
                val explanation = "$description - unhandled extraction of $index function parameter - child is not stringLiteral"
                found.showHumanReadableTree()
                extractArgumentListSyntaxTreeInFunctionCall(ast)[index].showRelatedSourceCode("unhandled extracting index $index - not string", fileSourceCode)
                println("unhandled key access")
                println(explanation)
                //throw ParsingInterpretationException(explanation)
                 */
                return null
            }
        } else {
            extractArgumentListSyntaxTreeInFunctionCall(ast)[index].showHumanReadableTree()
            extractArgumentListSyntaxTreeInFunctionCall(ast)[index].showRelatedSourceCode("unhandled extracting index $index", fileSourceCode)
            throw ParsingInterpretationException("unhandled extraction of $index function parameter - multiple children")
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
        val start = tree()!!.astInfoOrNull!!.start.index
        val end = tree()!!.astInfoOrNull!!.stop.index
        return Pair(start, end)
    }

    private fun Ast.relatedSourceCode(sourceCode: String): String {
        if (tree() == null) {
            return "<source code not available>"
        }
        val start = tree()!!.astInfoOrNull!!.start.index
        val end = tree()!!.astInfoOrNull!!.stop.index
        if (start < 0 || end < 0) {
            return "<source code not available> - stated range was $start to $end index"
        }
        return sourceCode.subSequence(start, end).toString()
    }

    private fun Ast.showRelatedSourceCode(description: String, sourceCode: String) {
        println("--------------------here is the $description (source code)---<")
        println(relatedSourceCode(sourceCode))
        println(">---------------------------here is the $description (source code)")
    }

    private fun Ast.showHumanReadableTreeWithSourceCode(description: String, fileSourceCode: String) {
        println("<---------------------------------------showHumanReadableTreeWithSourceCode--$description")
        humanReadableTreeWithSourceCode(0, fileSourceCode).forEach { println(it) }
        println(">---------------------------------------showHumanReadableTreeWithSourceCode--$description")
    }

    private fun Ast.humanReadableTreeWithSourceCode(indent: Int, fileSourceCode: String): List<String> {
        val info = ((this as? AstWithAstInfo)?.info?.toString() ?: "").padEnd(34)
        val infoHuman = humanReadableDescriptionInfo()
        val self = "$info${"--".repeat(indent)} $infoHuman <${relatedSourceCode(fileSourceCode)}>" // detachRaw()
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
        val self = "$info${"  ".repeat(indent)} ${humanReadableDescriptionInfo()} " // detachRaw()
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

    private fun Ast.listFound(found: List<AstNode>, name: String) {
        println()
        println()
        println("Found in $name:")
        found.forEach { it.showHumanReadableTree() }
    }

    private fun Ast.locateSingleOrExceptionByDescription(filter: String, debug: Boolean = false): AstNode {
        val found = locateByDescription(filter, debug)
        if (found.size != 1) {
            listFound(found, "locateSingleOrExceptionByDescription")
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

    private fun Ast.extractFunctionByName(functionName: String): AstNode? {
        val got = extractAllFunctionsByName(functionName)
        if (got.size > 1) {
            throw ParsingInterpretationException("expected one function, got multiple")
        }
        if (got.isEmpty()) {
            return null
        }
        return got[0]
    }

    private fun Ast.extractAllFunctionsByName(functionName: String): List<AstNode> {
        if (description == "functionDeclaration") {
            if (this is AstNode) {
                children.forEach {
                    if (it.description == "simpleIdentifier" && it.tree() is KlassIdentifier && ((it.tree() as KlassIdentifier).identifier == functionName)) {
                        // this.showHumanReadableTree()
                        return listOf(this) + children.flatMap { child ->
                            child.extractAllFunctionsByName(functionName)
                        }
                    }
                }
            } else {
                throw ParsingInterpretationException("wat")
            }
        }
        return if (this is AstNode) {
            children.flatMap { child ->
                child.extractAllFunctionsByName(functionName)
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

    private fun Ast.humanReadableDescriptionInfo(): String? {
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
        return textReadable
    }

    private fun AstSource.parse() = KotlinGrammarAntlrKotlinParser.parseKotlinFile(this)

    private fun File.parse(): Ast {
        val inputStream: InputStream = this.inputStream()
        val text = inputStream.bufferedReader().use { it.readText() }
        return KotlinGrammarAntlrKotlinParser.parseKotlinFile(AstSource.String(this.path, text))
    }

}
