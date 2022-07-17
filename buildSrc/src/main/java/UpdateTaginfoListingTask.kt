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
import kotlin.system.exitProcess
import java.lang.Exception
import java.net.URL

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

// following pages were useful to jumpstart coding:
// https://github.com/kotlinx/ast/blob/a96e681f906f1ec1ab4db8a81ffbbcbbe529317f/grammar-kotlin-parser-test/src/jvmMain/kotlin/kotlinx/ast/grammar/kotlin/test/AbstractKotlinGrammarParserTestDataTest.kt
// https://github.com/2bad2furious/kotlinx-ast-demo
// https://github.com/peternewman/StreetComplete/blob/a388043854bf04545dfbc0beb7decda5208a750e/.github/generate-quest-metadata.main.kts

open class UpdateTaginfoListingTask : DefaultTask() {
    @TaskAction fun run() {
        var processed = 0
        var failed = 0
        val foundTags = mutableListOf<TagQuestInfo>()
        val folderGenerator = questFolderGenerator()
        while (folderGenerator.hasNext()) {
            val folder = folderGenerator.next()
            var foundQuestFile = false
            File(folder.toString()).walkTopDown().forEach {
                if (".kt" in it.name && "Form" !in it.name) {
                    if ("Add" in it.name || "Check" in it.name || "Determine" in it.name || "MarkCompleted" in it.name) {
                        println(it)
                        println(it.name)
                        foundQuestFile = true
                        val got = addedOrEditedTags(it.name, loadFileFromPath(it.toString()))
                        if (got != null) {
                            println(got)
                            println()
                            processed += 1
                            got.forEach { tags -> foundTags.add(TagQuestInfo(tags, it.name)) }
                        } else {
                            println("failed")
                            println()
                            println()
                            failed += 1
                        }
                    }
                }
            }
            if (!foundQuestFile && folder.name != "note_discussion") {
                throw ParsingInterpretationException("not found quest file for $folder")
            }
        }
        foundTags.forEach { println(it) }
        val specificTags = foundTags.filter { it.tag.value != null }.size
        println("${foundTags.size} entries registered, $specificTags have specific tags, $processed quests processed, $failed failed")
        val tagsFoundPreviously = 223
        if (foundTags.size != tagsFoundPreviously) {
            println("Something changed in processing! foundTags count ${foundTags.size} vs $tagsFoundPreviously previously")
        }
        val specificTagsFoundPreviously = 99
        if (specificTags != specificTagsFoundPreviously) {
            println("Something changed in processing! specificTags count $specificTags vs $specificTagsFoundPreviously previously")
        }
        val processedQuestsPreviously = 122
        if (processed != processedQuestsPreviously) {
            println("Something changed in processing! processed count $processed vs $processedQuestsPreviously previously")
        }
        val failedQuestsPreviously = 24
        if (failed != failedQuestsPreviously) {
            println("Something changed in processing! failed count $failed vs $failedQuestsPreviously previously")
        }
        foundTags.forEach {
            if(!isPageExisting("https://wiki.openstreetmap.org/w/index.php?title=Key:${it.tag.key}")) {
                println("$it has no OSM Wiki page")
            }
            if(it.tag.value != null && it.tag.key !in freeformKeys()) {
                if(!isPageExisting("https://wiki.openstreetmap.org/w/index.php?title=Tag:${it.tag.key}=${it.tag.value}")) {
                    println("$it has no OSM Wiki page")
                }
            }
        }
        val test = URL("https://wiki.openstreetmap.org/w/index.php?title=Key:office").openStream().bufferedReader().use { it.readText() }
        println(test)
        val test2 = URL("https://wiki.openstreetmap.org/w/index.php?title=Key:nonexistingpageeeee").openStream().bufferedReader().use { it.readText() }
        println(test2)
    }

    private fun isPageExisting(url : String): Boolean {
        try {
            URL(url).openStream().bufferedReader().use { it.readText() }
        }catch (e: java.io.FileNotFoundException) {
            return false
        }
        return true
    }

    private fun questFolderGenerator() = iterator {
        val root = "app/src/main/java/de/westnordost/streetcomplete/quests"
        File(root).walkTopDown().maxDepth(1).forEach { folder ->
            if (folder.isDirectory && folder.toString() != root) {
                yield(folder)
            }
        }
    }

    private fun freeformKeys(): List<String> {
        // most have own syntax and limitations obeyed by SC
        return listOf("name", "maxheight", "ref", "addr:flats", "addr:housenumber",
            "collection_times", "opening_hours", "surface:note", "capacity",
            "maxspeed",
            "operator" // technically not fully, but does ot make sense to list all that autocomplete values
        )
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

    private fun addedOrEditedTags(description: String, fileSourceCode: String): Set<Tag>? {
        val ast = AstSource.String(description, fileSourceCode)
        val found = ast.parse().extractFunctionByName("applyAnswerTo")
        if (found.isEmpty()) {
            println("applyAnswerTo not found in $description")
            return null
        }
        if (found.size != 1) {
            println("unexpected function count found")
            exitProcess(1)
        }
        val appliedTags = mutableSetOf<Tag>()
        var failedExtraction = false
        val relevantFunction = found[0]
        relevantFunction.showRelatedSourceCode(fileSourceCode, "inspected function")
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
                        appliedTags += extractValuesForKnownKey(key, valueHolder, fileSourceCode, key in freeformKeys())
                    } else if (potentialVariable != null) {
                        expression.showHumanReadableTree()
                        expression.showRelatedSourceCode(fileSourceCode, "expression")
                        println(KotlinGrammarParserType.identifier.toString() + " identified as accessing index as a variable (potentialTexts.size = ${potentialTexts.size})")
                        return null
                    } else if (complexPotentialVariable.size == 1) {
                        expression.showHumanReadableTree()
                        expression.showRelatedSourceCode(fileSourceCode, "expression")
                        println(complexPotentialVariable[0].relatedSourceCode(fileSourceCode) + " identified as accessing index as a complex variable (potentialTexts.size = ${potentialTexts.size})")
                        return null
                    } else {
                        expression.showRelatedSourceCode(fileSourceCode, "expression")
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
        } else if (valueHolder.relatedSourceCode(fileSourceCode) in listOf("answer.toYesNo()", "it.toYesNo()", "answer.credit.toYesNo()", "answer.debit.toYesNo()")) {
            // should it be treated as a hack?
            // parse code and detect toYesNo() at the end?
            appliedTags.add(Tag(key, "yes"))
            appliedTags.add(Tag(key, "no"))
        } else {
            appliedTags.add(Tag(key, null)) // TODO - get also value...
            if (!freeformValueExpected) {
                valueHolder.showHumanReadableTreeWithSourceCode(fileSourceCode)
                valueHolder.showRelatedSourceCode(fileSourceCode, "get value (key is known: $key) from this somehow... valueIfItIsSimpleText is $valueIfItIsSimpleText")
            }
        }
        return appliedTags
    }

    private fun extractValuesForKnownKeyFromWhenExpression(key: String, whenExpression: AstNode, fileSourceCode: String, freeformValueExpected: Boolean): MutableSet<Tag> {
        val appliedTags = mutableSetOf<Tag>()
        whenExpression.showRelatedSourceCode(fileSourceCode, "expression")
        whenExpression.showHumanReadableTreeWithSourceCode(fileSourceCode)
        whenExpression.locateByDescription("whenEntry").forEach { it ->
            it.showRelatedSourceCode(fileSourceCode, "expression")
            it.showHumanReadableTreeWithSourceCode(fileSourceCode)
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
                    possibleDotAndFunction[0].locateSingleByDescriptionDirectChild("navigationSuffix")
                if (dotAndFunction !is AstNode) {
                    throw ParsingInterpretationException("unexpected!")
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
                    throw ParsingInterpretationException("unexpected! expectedFunctionIdentifier.root() is ${primary::class}")
                }
                val functionName = (expectedFunctionIdentifier.root() as KlassIdentifier).identifier
                if (functionName in listOf(
                        "updateWithCheckDate", // TODO: check date is also an affected key!
                        "setCheckDateForKey",
                        "updateCheckDateForKey"
                    )
                ) {
                    val arguments = it.locateByDescriptionDirectChild("postfixUnarySuffix")[1]
                        .locateSingleByDescriptionDirectChild("callSuffix")
                        .locateSingleByDescriptionDirectChild("valueArguments")
                    val argumentList = arguments.locateByDescription("valueArgument")
                    val key = argumentList[0].locateSingleByDescription("primaryExpression")
                    if (key.children.size == 1) {
                        if (key.children[0].description == "stringLiteral") {
                            val stringObject = (key.children[0].root() as KlassString).children[0]
                            val keyString = (stringObject as StringComponentRaw).string
                            appliedTags.add(Tag(keyString, null)) // TODO which value
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
