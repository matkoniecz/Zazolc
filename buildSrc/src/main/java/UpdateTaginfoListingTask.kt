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

// following pages were useful to jumpstart coding:
// https://github.com/kotlinx/ast/blob/a96e681f906f1ec1ab4db8a81ffbbcbbe529317f/grammar-kotlin-parser-test/src/jvmMain/kotlin/kotlinx/ast/grammar/kotlin/test/AbstractKotlinGrammarParserTestDataTest.kt
// https://github.com/2bad2furious/kotlinx-ast-demo
// https://github.com/peternewman/StreetComplete/blob/a388043854bf04545dfbc0beb7decda5208a750e/.github/generate-quest-metadata.main.kts

open class UpdateTaginfoListingTask : DefaultTask() {
    companion object {
        const val NAME_OF_FUNCTION_EDITING_TAGS = "applyAnswerTo"
        const val QUEST_ROOT = "app/src/main/java/de/westnordost/streetcomplete/quests/"
        const val SURVEY_MARK_KEY = "check_date" // TODO: is it possible to use directly SC constant?
        val EXPECTED_TAG_PER_QUEST = mapOf(
            "accepts_cards/AddAcceptsCards.kt" to setOf(Tag("payment:debit_cards", "yes"), Tag("payment:debit_cards", "no"), Tag("payment:credit_cards", "yes"), Tag("payment:credit_cards", "no")),
            "accepts_cash/AddAcceptsCash.kt" to setOf(Tag("payment:cash", "yes"), Tag("payment:cash", "no")),
            "address/AddHousenumber.kt" to setOf(Tag("addr:conscriptionnumber", null), Tag("addr:streetnumber", null), Tag("addr:housenumber", null), Tag("addr:block_number", null), Tag("addr:housename", null), Tag("nohousenumber", "yes"), Tag("building", "yes")),
            "atm_operator/AddAtmOperator.kt" to setOf(Tag("operator", null)),
            "air_conditioning/AddAirConditioning.kt" to setOf(Tag("air_conditioning", "yes"), Tag("air_conditioning", "no")),
            "baby_changing_table/AddBabyChangingTable.kt" to setOf(Tag("changing_table", "yes"), Tag("changing_table", "no")),
            "bench_backrest/AddBenchBackrest.kt" to setOf(Tag("leisure", "picnic_table"), Tag("backrest", "yes"), Tag("backrest", "no")),
            "bike_parking_capacity/AddBikeParkingCapacity.kt" to setOf(Tag("capacity", null)),
            "bike_parking_cover/AddBikeParkingCover.kt" to setOf(Tag("covered", "yes"), Tag("covered", "no")),
            "bike_rental_capacity/AddBikeRentalCapacity.kt" to setOf(Tag("capacity", null)),
            "building_entrance_reference/AddEntranceReference.kt" to setOf(Tag("addr:flats", null), Tag("ref", null), Tag("ref:signed", "no")),
            "building_levels/AddBuildingLevels.kt" to setOf(Tag("building:levels", null), Tag("roof:levels", null)),
            "building_underground/AddIsBuildingUnderground.kt" to setOf(Tag("location", "underground"), Tag("location", "surface")),
            "bus_stop_ref/AddBusStopRef.kt" to setOf(Tag("ref:signed", "no"), Tag("ref", null)),
            "charging_station_capacity/AddChargingStationCapacity.kt" to setOf(Tag("capacity", null)),
            "charging_station_operator/AddChargingStationOperator.kt" to setOf(Tag("operator", null)),
            "clothing_bin_operator/AddClothingBinOperator.kt" to setOf(Tag("operator", null)),
            "construction/MarkCompletedBuildingConstruction.kt" to setOf(Tag("opening_date", null), Tag("building", null)),
            "construction/MarkCompletedHighwayConstruction.kt" to setOf(Tag("opening_date", null), Tag("highway", null)),
            "crossing_island/AddCrossingIsland.kt" to setOf(Tag("crossing:island", "yes"), Tag("crossing:island", "no")),
            "defibrillator/AddIsDefibrillatorIndoor.kt" to setOf(Tag("indoor", "yes"), Tag("indoor", "no")),
            "ferry/AddFerryAccessMotorVehicle.kt" to setOf(Tag("motor_vehicle", "yes"), Tag("motor_vehicle", "no")),
            "ferry/AddFerryAccessPedestrian.kt" to setOf(Tag("foot", "yes"), Tag("foot", "no")),
            "fire_hydrant_diameter/AddFireHydrantDiameter.kt" to setOf(Tag("fire_hydrant:diameter", null), Tag("fire_hydrant:diameter:signed", "no")),
            "foot/AddProhibitedForPedestrians.kt" to setOf(Tag("foot", "no"), Tag("foot", "yes"), Tag("sidewalk", "separate"), Tag("highway", "living_street")),
            "fuel_service/AddFuelSelfService.kt" to setOf(Tag("self_service", "yes"), Tag("self_service", "no")),
            "general_fee/AddGeneralFee.kt" to setOf(Tag("fee", "yes"), Tag("fee", "no")),
            "level/AddLevel.kt" to setOf(Tag("level", null)),
            "lanes/AddLanes.kt" to setOf(Tag("lanes", null), Tag("lane_markings", "yes"), Tag("lane_markings", "no"), Tag("lanes:both_ways", "1"), Tag("turn:lanes:both_ways", "left"), Tag("lanes:forward", null), Tag("lanes:backward", null)),
            "max_height/AddMaxPhysicalHeight.kt" to setOf(Tag("maxheight", null), Tag("maxheight:signed", "no"), Tag("source:maxheight", "ARCore")),
            "motorcycle_parking_capacity/AddMotorcycleParkingCapacity.kt" to setOf(Tag("capacity", null)),
            "motorcycle_parking_cover/AddMotorcycleParkingCover.kt" to setOf(Tag("covered", "yes"), Tag("covered", "no")),
            "oneway/AddOneway.kt" to setOf(Tag("oneway", "yes"), Tag("oneway", "-1"), Tag("oneway", "no")),
            "opening_hours/AddOpeningHours.kt" to setOf(Tag("opening_hours:signed", "no"), Tag("check_date:opening_hours", null), Tag("opening_hours", null)),
            "opening_hours_signed/CheckOpeningHoursSigned.kt" to setOf(Tag("opening_hours:signed", "no"), Tag("check_date:opening_hours", null)),
            "picnic_table_cover/AddPicnicTableCover.kt" to setOf(Tag("covered", "yes"), Tag("covered", "no")),
            "postbox_ref/AddPostboxRef.kt" to setOf(Tag("ref:signed", "no"), Tag("ref", null)),
            "postbox_collection_times/AddPostboxCollectionTimes.kt" to setOf(Tag("collection_times:signed", "no"), Tag("collection_times", null)),
            "recycling/AddRecyclingType.kt" to setOf(Tag("recycling_type", "centre"), Tag("recycling_type", "container"), Tag("location", "overground"), Tag("location", "underground")),
            "recycling_glass/DetermineRecyclingGlass.kt" to setOf(Tag("recycling:glass_bottles", "yes"), Tag("recycling:glass", "no")),
            "seating/AddSeating.kt" to setOf(Tag("outdoor_seating", "yes"), Tag("outdoor_seating", "no"), Tag("indoor_seating", "yes"), Tag("indoor_seating", "no")),
            "self_service/AddSelfServiceLaundry.kt" to setOf(Tag("self_service", "no"), Tag("laundry_service", "yes"), Tag("self_service", "yes"), Tag("laundry_service", "no")),
            "step_count/AddStepCount.kt" to setOf(Tag("step_count", null)),
            "step_count/AddStepCountStile.kt" to setOf(Tag("step_count", null)),
            "steps_incline/AddStepsIncline.kt" to setOf(Tag("incline", "up"), Tag("incline", "down")),
            "toilets_fee/AddToiletsFee.kt" to setOf(Tag("fee", "yes"), Tag("fee", "no")),
            "traffic_signals_button/AddTrafficSignalsButton.kt" to setOf(Tag("button_operated", "yes"), Tag("button_operated", "no")),
            "width/AddRoadWidth.kt" to setOf(Tag("width", null), Tag("source:width", "ARCore")),
        )
    }
    @TaskAction fun run() {
        var processed = 0
        var failed = 0
        val foundTags = mutableListOf<TagQuestInfo>()
        val folderGenerator = questFolderGenerator()
        while (folderGenerator.hasNext()) {
            val folder = folderGenerator.next()
            var foundQuestFile = false
            File(folder.toString()).walkTopDown().forEach {
                if (isQuestFile(it)) {
                    foundQuestFile = true
                    val fileSourceCode = loadFileFromPath(it.toString())
                    val got = addedOrEditedTags(it.name, fileSourceCode)
                    reportResultOfScanInSingleQuest(got, it.toString().removePrefix(QUEST_ROOT), fileSourceCode)
                    if (got != null) {
                        processed += 1
                        got.forEach { tags -> foundTags.add(TagQuestInfo(tags, it.name)) }
                    } else {
                        failed += 1
                    }
                }
            }
            break
            if (!foundQuestFile && folder.name != "note_discussion") {
                throw ParsingInterpretationException("not found quest file for $folder")
            }
        }
        reportResultOfDataCollection(foundTags, processed, failed)
    }

    private fun questFolderGenerator() = iterator {
        File(QUEST_ROOT).walkTopDown().maxDepth(1).forEach { folder ->
            if (folder.isDirectory && folder.toString() != QUEST_ROOT && folder.toString() + "/" != QUEST_ROOT) {
                yield(folder)
            }
        }
    }

    private fun isQuestFile(file: File): Boolean {
        if (".kt" !in file.name) {
            return false
        }
        if ("Form" in file.name) {
            return false
        }
        if ("Adapter" in file.name) {
            return false
        }
        if ("Utils" in file.name) {
            return false
        }
        if (file.name == "AddressStreetAnswer.kt") {
            return false
        }
        if ("Add" in file.name || "Check" in file.name || "Determine" in file.name || "MarkCompleted" in file.name) {
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
        println()
        println("-----------------")
        if (mismatch) {
            println("MISMATCH")
            println("MISMATCH")
            println("MISMATCH")
            println("MISMATCH")
            println("MISMATCH")
            println("MISMATCH")
            println("MISMATCH")
        }
        if (got == null) {
            println("EMPTY RESULT, FAILED")
            println("EMPTY RESULT, FAILED")
            println("EMPTY RESULT, FAILED")
            println("EMPTY RESULT, FAILED")
        }
        println(filepath)
        val ast = AstSource.String(filepath, fileSourceCode)
        val relevantFunction = getAstTreeForFunctionEditingTags(filepath, ast)
        relevantFunction.showRelatedSourceCode(fileSourceCode, "inspected function")
        if (got != null) {
            println(got)
            val classesReadyToCreate = got.map { it.reproduceCode() }.joinToString(", ")
            println("\"$filepath\" to setOf($classesReadyToCreate),")
        }
        println("-----------------")
        println()
    }

    private fun reportResultOfDataCollection(foundTags: MutableList<TagQuestInfo>, processed: Int, failed: Int) {
        foundTags.forEach { println("$it ${if (it.tag.value == null && !freeformKey(it.tag.key)) {"????????"} else {""}}") }
        val tagsThatShouldBeMoreSpecific = foundTags.filter { it.tag.value == null && freeformKey(it.tag.key) }.size
        println("${foundTags.size} entries registered, $tagsThatShouldBeMoreSpecific should be more specific, $processed quests processed, $failed failed")
        val tagsFoundPreviously = 229
        if (foundTags.size != tagsFoundPreviously) {
            println("Something changed in processing! foundTags count ${foundTags.size} vs $tagsFoundPreviously previously")
        }
        val tagsThatShouldBeMoreSpecificFoundPreviously = 84
        if (tagsThatShouldBeMoreSpecific != tagsThatShouldBeMoreSpecificFoundPreviously) {
            println("Something changed in processing! tagsThatShouldBeMoreSpecific count $tagsThatShouldBeMoreSpecific vs $tagsThatShouldBeMoreSpecificFoundPreviously previously")
        }
        val processedQuestsPreviously = 122
        if (processed != processedQuestsPreviously) {
            println("Something changed in processing! processed count $processed vs $processedQuestsPreviously previously")
        }
        val failedQuestsPreviously = 24
        if (failed != failedQuestsPreviously) {
            println("Something changed in processing! failed count $failed vs $failedQuestsPreviously previously")
        }
        println()
        println()
        foundTags.forEach {
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

    private fun freeformKey(key : String): Boolean {
        // most have own syntax and limitations obeyed by SC
        // maybe move to general StreetComplete file about OSM tagging?
        if (key in listOf("name", "ref",
            "addr:flats", "addr:housenumber", "addr:street", "addr:place", "addr:block_number",
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

    fun showEntire(description: String, content: String) {
        val ast = AstSource.String(description, content)
        ast.parse().showHumanReadableTree()
        println("============================here is the entire content (source code)==<")
        println(content)
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

    private fun addedOrEditedTags(description: String, fileSourceCode: String): Set<Tag>? {
        val appliedTags = mutableSetOf<Tag>()
        var failedExtraction = false
        val ast = AstSource.String(description, fileSourceCode)
        val relevantFunction = getAstTreeForFunctionEditingTags(description, ast)
        var got = extractCasesWhereTagsAreAccessedWithIndex(relevantFunction, fileSourceCode)
        if (got != null) {
            appliedTags += got
        } else {
            failedExtraction = true
        }

        got = extractCasesWhereTagsAreAccessedWithFunction(relevantFunction, fileSourceCode)
        if (got != null) {
            appliedTags += got
        } else {
            failedExtraction = true
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
            if (textHolder.relatedSourceCode(fileSourceCode) == textFoundIfFillingEntireHolder.relatedSourceCode(fileSourceCode)) {
                // actual text holder is hidden inside, but it is actually the same object
                val expectedTextHolder = textFoundIfFillingEntireHolder.root()
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

    private fun extractCasesWhereTagsAreAccessedWithIndex(relevantFunction: AstNode, fileSourceCode: String): Set<Tag>? {
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
                    tagsDictAccess.children[0].root() is KlassIdentifier &&
                    ((tagsDictAccess.children[0].root() as KlassIdentifier).identifier == "tags")
                ) {
                    // this limits it to things like
                    // tags[something] = somethingElse
                    // (would it also detect tags=whatever)?
                    val indexingElement = tagsDictAccess.locateSingleByDescription("assignableSuffix")
                        .locateSingleByDescription("indexingSuffix")
                    // indexingElement is something like ["indoor"] or [key]
                    val expression = indexingElement.locateSingleByDescriptionDirectChild("expression") // drop outer [ ]
                    val potentialTexts = expression.locateByDescription("stringLiteral", debug = false) // what if it is something like "prefix" + CONSTANT ?
                    val potentialVariable = if (expression is KlassIdentifier) { expression } else { null } // tag[key] = ...
                    val complexPotentialVariable = expression.locateByDescriptionDirectChild("disjunction") // tag[answer.osmKey] = ...
                    if (potentialTexts.size == 1) {
                        val processed = potentialTexts[0].root()
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
                        val valueHolder = assignment.locateSingleByDescriptionDirectChild("expression")
                        appliedTags += extractValuesForKnownKey(key, valueHolder, fileSourceCode, freeformKey(key))
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

    private fun extractValuesForKnownKey(key: String, valueHolder: Ast, fileSourceCode: String, freeformValueExpected: Boolean): MutableSet<Tag> {
        val appliedTags = mutableSetOf<Tag>()
        val potentialWhenExpressionCandidate = valueHolder.locateByDescription("whenExpression")
        if (potentialWhenExpressionCandidate.size > 1) {
            throw ParsingInterpretationException("not handled, ${potentialWhenExpressionCandidate.size} when expressions")
        }
        if (potentialWhenExpressionCandidate.size == 1) {
            if (potentialWhenExpressionCandidate[0].relatedSourceCode(fileSourceCode) == valueHolder.relatedSourceCode(fileSourceCode)) {
                val whenExpression = potentialWhenExpressionCandidate[0]
                return extractValuesForKnownKeyFromWhenExpression(key, whenExpression, fileSourceCode, freeformValueExpected)
            } else {
                throw ParsingInterpretationException("not handled, when expressions as part of something bigger")
            }
        }

        val valueIfItIsSimpleText = extractTextFromHardcodedString(valueHolder, fileSourceCode)
        if (valueIfItIsSimpleText != null) {
            appliedTags.add(Tag(key, valueIfItIsSimpleText))
        } else if (valueHolder.relatedSourceCode(fileSourceCode) == "answer.osmValue") {
            appliedTags.add(Tag(key, null)) // TODO - get also value...
            // TODO handle this somehow - requires extra parsing, likely in another file
        } else if (valueHolder.relatedSourceCode(fileSourceCode).endsWith(".toYesNo()")) {
            // previous form of check:
            // in listOf("answer.toYesNo()", "it.toYesNo()", "answer.credit.toYesNo()", "answer.debit.toYesNo()", "isAutomated.toYesNo()")
            // TODO: fix this hack by proper parse and detect toYesNo() at the end? (low priority)
            appliedTags.add(Tag(key, "yes"))
            appliedTags.add(Tag(key, "no"))
        } else {
            appliedTags.add(Tag(key, null)) // TODO - get also value...
            if (!freeformValueExpected) {
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

    private fun extractValuesForKnownKeyFromWhenExpression(key: String, whenExpression: AstNode, fileSourceCode: String, freeformValueExpected: Boolean): MutableSet<Tag> {
        val appliedTags = mutableSetOf<Tag>()
        whenExpression.locateByDescription("whenEntry").forEach { it ->
            val structure = it.children.filter { it.description != "WS" }
            /*
            structure.forEach { child ->
                println()
                println()
                println("child")
                println(child.description)
                child.showRelatedSourceCode(fileSourceCode, "child")
            }
            */
            if (structure[0].description != "whenCondition") {
                throw ParsingInterpretationException("unexpected when structure!")
            }
            if (structure[1].description != "ARROW") {
                throw ParsingInterpretationException("unexpected when structure!")
            }
            if (structure[2].description != "controlStructureBody") {
                throw ParsingInterpretationException("unexpected when structure!")
            }
            if (structure[3].description != "semi") {
                throw ParsingInterpretationException("unexpected when structure!")
            }
            if (structure.size != 4) {
                throw ParsingInterpretationException("unexpected when structure!")
            }
            appliedTags += extractValuesForKnownKey(key, structure[2], fileSourceCode, freeformValueExpected)
        }
        return appliedTags
    }

    private fun extractCasesWhereTagsAreAccessedWithFunction(relevantFunction: AstNode, fileSourceCode: String): Set<Tag>? {
        // it is trying to detect things like
        // tags.updateWithCheckDate("smoking", answer.osmValue)
        val appliedTags = mutableSetOf<Tag>()
        relevantFunction.locateByDescription("postfixUnaryExpression").forEach {
            if (it.root() is KlassIdentifier && ((it.root() as KlassIdentifier).identifier == "tags")) {
                val primary = it.locateSingleByDescriptionDirectChild("primaryExpression")
                val rootOfExpectedTagsIdentifier = primary.root()
                if (rootOfExpectedTagsIdentifier !is KlassIdentifier) {
                    println()
                    it.showHumanReadableTree()
                    println()
                    primary.showHumanReadableTree()
                    throw ParsingInterpretationException("unexpected! primary is ${primary::class}")
                }
                if (rootOfExpectedTagsIdentifier.identifier != "tags") {
                    throw ParsingInterpretationException("unexpected!")
                }
                val possibleDotAndFunction = it.locateByDescriptionDirectChild("postfixUnarySuffix")
                if (possibleDotAndFunction.isEmpty()) {
                    // this will happen in case of say
                    // tags["key"] = value
                    // in such case we want to skip it
                    return@forEach // this is "continue" with a weird name
                }
                val dotAndFunctionScan =
                    possibleDotAndFunction[0].locateByDescriptionDirectChild("navigationSuffix")
                if (dotAndFunctionScan.isEmpty()) {
                    // maybe false positive?
                    // maybe something like
                    // .any { tags[it]?.toCheckDate() != null }
                    // where skipping is valid?
                    return@forEach
                }
                val dotAndFunction =
                    possibleDotAndFunction[0].locateSingleByDescriptionDirectChild("navigationSuffix") // TODO: why [0]
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
                    val keyString = extractKeyValueInFunctionCall(it, fileSourceCode)
                    if (keyString != null) {
                        appliedTags.add(Tag("$SURVEY_MARK_KEY:$keyString", null))
                    }
                } else if (functionName in listOf(
                        "updateWithCheckDate", // TODO: check date is also an affected key!
                    )
                ) {
                    val keyString = extractKeyValueInFunctionCall(it, fileSourceCode)
                    if (keyString != null) {
                        appliedTags.add(Tag(keyString, null)) // TODO which value
                        // dotAndFunction is without argument
                        // possibleDotAndFunction[0] also
                        it.showRelatedSourceCode(fileSourceCode, "it - value extraction possible from that?")
                    } else {
                        return null
                    }
                } else if (functionName in listOf("remove", "containsKey", "removeCheckDatesForKey", "hasChanges", "entries", "hasCheckDateForKey", "hasCheckDate")) {
                    // skip, as only added or edited tags are listed - and removed one and influencing ones are ignored
                } else if (functionName in listOf("updateCheckDate")) {
                    // edit to check date
                    // TODO load that check date somehow? or just assume for now?
                } else if (functionName == "replaceShop") {
                    // that brings basically entire NSI, right?
                    // worse - not entire, only segment of it...
                    // so NSI would be parsed in turn...
                    // TODO
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
        }
        return appliedTags
    }

    private fun extractKeyValueInFunctionCall(it: AstNode, fileSourceCode: String): String? {
        println(it.description)
        val arguments = it.locateByDescriptionDirectChild("postfixUnarySuffix")[1]
            .locateSingleByDescriptionDirectChild("callSuffix")
            .locateSingleByDescriptionDirectChild("valueArguments")
        val argumentList = arguments.locateByDescription("valueArgument")
        val key = argumentList[0].locateSingleByDescription("primaryExpression")
        if (key.children.size == 1) {
            if (key.children[0].description == "stringLiteral") {
                val stringObject = (key.children[0].root() as KlassString).children[0]
                return (stringObject as StringComponentRaw).string
            } else {
                // TODO handle this
                key.showHumanReadableTree()
                argumentList[0].showRelatedSourceCode(fileSourceCode, "unhandled key access")
                println("unhandled key access")
                return null
            }
        } else {
            // TODO handle this
            argumentList[0].showHumanReadableTree()
            argumentList[0].showRelatedSourceCode(fileSourceCode, "unhandled key access")
            println("unhandled key access")
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
        if (expectedFunctionIdentifier.root() !is KlassIdentifier) {
            throw ParsingInterpretationException("unexpected! expectedFunctionIdentifier.root() is ${expectedFunctionIdentifier.root()!!::class}")
        }
        return (expectedFunctionIdentifier.root() as KlassIdentifier).identifier
    }

    class ParsingInterpretationException(private val s: String) : Throwable() {
        override fun toString(): String {
            return s
        }
    }

    private fun Ast.relatedSourceCode(sourceCode: String): String {
        if (root() == null) {
            return "<source code not available>"
        }
        val start = root()!!.humanReadableDescriptionInfo()!!.start
        val end = root()!!.humanReadableDescriptionInfo()!!.end
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

    private fun Ast.locateSingleByDescription(filter: String, debug: Boolean = false): AstNode {
        val found = locateByDescription(filter, debug)
        if (found.size != 1) {
            throw ParsingInterpretationException("unexpected count!")
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

    private fun Ast.locateSingleByDescriptionDirectChild(filter: String): Ast {
        val found = locateByDescriptionDirectChild(filter)
        if (found.size != 1) {
            showHumanReadableTree()
            throw ParsingInterpretationException("unexpected count! Expected single matching direct child on filter $filter, got ${found.size}")
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
                    if (it.description == "simpleIdentifier" && it.root() is KlassIdentifier && ((it.root() as KlassIdentifier).identifier == functionName)) {
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

    private fun Ast.root(): Ast? {
        var returned: Ast? = null
        this.summary(false).onSuccess { returned = it.firstOrNull() }
        return returned
    }

    private fun Ast.humanReadableDescriptionInfo(): ElementInfo? {
        val current = this.root() ?: return null
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
