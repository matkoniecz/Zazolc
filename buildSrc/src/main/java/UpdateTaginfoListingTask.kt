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
parsing source code to generate documentation of an open source program (StreetComplete, an OpenStreetMap editor)

I posted a big chunk of code here - but feel free to review only a small section, I will upvote all
answers that tried to help, will award bounties to especially useful and will actually use that code to improve
this deployed open source software ( tracked in https://github.com/streetcomplete/StreetComplete/issues/4225 ).

I want to say that yes, this code works as expected. But likely can be significantly improved in code quality. Feedback is highly welcomed!

If you make a nontrivial suggestions: please add note that you license your work on GPLv3 license.
It is not needed for trivial changes, but for substantial I need this to be able to use your work.

For anyone making comments: please specify preferred name and email if you want (can be also anonymous@example.com or similar for email), in such case I will credit you in the commit metadata. Or request anonymous contribution.
If you will not specify anything I will mention your SO in the commit message and link your answer as thanks.

Back to explaining the program: [StreetComplete](https://github.com/westnordost/StreetComplete/) is an editor of [OpenStreetMap](https://www.openstreetmap.org/) database. Usually contributing to OSM requires at least 10 minute tutorial to start (default editor raised by edit button on the website).

StreetComplete is intended to be usable by regular humans and work by asking user simple to answer questions. And require Android phone, not being blind, able to move and ability to read as sufficient to use it.

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

StreetComplete, like other OSM editors is editing this tags. Typical edit is something like adding `surface=asphalt` or `surface=dirt` to mark road surface or `name=Żółta` to mark street name, based on what someone surveyed and answered.
This happens in a way a bit invisible to humans, as tags are not exposed directly to mappers using StreetComplete.

But other more experienced mappers may want to know what kind of edits StreetComplete is doing!

One of standard ways to document this is using Taginfo project listing - projects can publish a [simple .json file](https://wiki.openstreetmap.org/wiki/Taginfo/Projects),
with results presented at Taginfo site (used by more experienced OSM mappers and developers).

See for example https://taginfo.openstreetmap.org/keys/addr%3Aplace#projects listing projects which
listed add:place key as relevant.

So, this code presented here will parse source code files of StreetComplete project to detect what kind of changes this editor will make to OpenStreetMap database, and will generate .json file understendable by Taginfo project.

The goal is to list all tags which can be added or edited by this editor (tags which can be removed or are used in filtering are skipped).

For example [here](https://github.com/streetcomplete/StreetComplete/blob/6fd996559cb453ebec2998bef18fe6c14642f858/app/src/main/java/de/westnordost/streetcomplete/quests/toilets_fee/AddToiletsFee.kt) is defined quest asking whether public toilet is paid.

There are many parts there, for example `enabledInCountries = AllCountriesExcept("US", "CA")` marks that this quest is disabled in USA and Canada. But for documenting used tags the relevant function is

```
    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
        tags["fee"] = answer.toYesNo()
    }
```

in this case key `fee` can be set to either `fee=yes` or `fee=no` as `toYesNo()` cannot return anything else.

There are over 100 other quests, some significantly more complex.

My parsing code relies on editing functionality being defined in `applyAnswerTo` function of various quests.

This is NOT a pure parsing, in several places shortcuts were taken to reduce implementation effort while still working.

As this is tightly coupled to StreetComplete, many assumptions can be made.

Some answers are fully hardcoded as code was too complex to parse and therefore need to maintained manually. That is fine, and I am not expecting anyone here to change this.

Exceptions are often raised with nothing except "unexpected" as explanation - that is fine, this is done this way as it is not fully general parser, and parsing current StreetComplete code is good enough.

This is the third attempt!

Maintaining such list manually is too time-consuming and too boring. There was an attempt to do this but it failed. See https://github.com/goldfndr/StreetCompleteJSON

There was also attempt to do this with regular expressions. It also failed. See https://github.com/streetcomplete/StreetComplete/pull/2754

This is the third attempt, it works, but code likely can be far better.
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
            questData.map{TagWithDescriptionForTaginfoListing(it.tag.key, it.tag.value, "added or edited tag in '${it.changesetDescription}' quest")}
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

        val foundTags = mutableListOf<TagQuestInfo>()
        val folderGenerator = questFolderGenerator()

        while (folderGenerator.hasNext()) {
            val folder = folderGenerator.next()

            File(folder.toString()).walkTopDown().forEach {
                if (it.isFile) {
                    if (isQuestFile(it)) {
                        addedOrEditedTags(it)!!.forEach { tags -> foundTags.add(TagQuestInfo(tags, it.name, getChangesetComment(it))) }
                    }
                }
            }
        }
        generateReport(foundTags)
        reportResultOfDataCollection(foundTags)
        checkOsmWikiPagesExistence(foundTags)
    }

    private fun getChangesetComment(questFile: File): String {
        listOfClassPropertyDeclaration(questFile.parse()).forEach { propertyDeclaration ->
            val variableDeclaration = propertyDeclaration.locateSingleOrNullByDescription("variableDeclaration")
            if (variableDeclaration != null) {
                val identifierOfProperty = (variableDeclaration.tree() as KlassIdentifier).identifier
                if(identifierOfProperty == "changesetComment") {
                    val expression = propertyDeclaration.locateSingleOrExceptionByDescriptionDirectChild("expression")
                    return extractTextFromHardcodedString(expression)!!
                }
            }
        }
        throw ParsingInterpretationException("not supposed to happen, as processing $questFile")
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

    private fun reportResultOfDataCollection(foundTags: MutableList<TagQuestInfo>) {
        // foundTags.forEach { println("$it ${if (it.tag.value == null && !freeformKey(it.tag.key)) {"????????"} else {""}}") }
        println("${foundTags.size} entries registered")
        val tagsFoundPreviously = 1619
        if (foundTags.size != tagsFoundPreviously) {
            println("Something changed in processing! foundTags count ${foundTags.size} vs $tagsFoundPreviously previously")
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
                "operator", // technically not fully, but does not make sense to list all that autocomplete values
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
    }

    class TagQuestInfo(val tag: Tag, val quest: String, val changesetDescription: String) {
        override fun toString(): String {
            return "$tag in $quest ($changesetDescription)"
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
                appliedTags.add(Tag("amenity", "waste_disposal")) // from applyWasteContainerAnswer, hardcoded due to complexity HACK :(
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
                // maybe track assignments to the values which are later assigned to fields? This would be feasible here, I guess...")
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
                val companionObject = classMemberDeclaration.locateByDescriptionDirectChild("companionObject")
                if (companionObject.size == 1) {
                    // oh, that is just companion object declaration - lets skip it
                    return@forEach
                }
                println()
                println()
                println()
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
        val got = extractCasesWhereTagsAreAccessedWithIndex(description, relevantFunction, fileSourceCode, suspectedAnswerEnumFiles)
        if (got != null) {
            appliedTags += got
        } else {
            println("failedExtraction of $description - extractCasesWhereTagsAreAccessedWithIndex")
            failedExtraction = true
        }

        appliedTags += extractCasesWhereTagsAreAccessedWithFunction(description, relevantFunction, fileSourceCode, suspectedAnswerEnumFiles)

        val tagsThatShouldBeMoreSpecific = appliedTags
            .filter { it.value == null && !freeformKey(it.key) && !streetCompleteIsReusingAnyValueProvidedByExistingTagging(description, it.key) }
        if (tagsThatShouldBeMoreSpecific.isNotEmpty()) {
            tagsThatShouldBeMoreSpecific.forEach { println(it) }
            println("$description found tags which are not freeform but have no specified values")
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

    private fun extractCasesWhereTagsAreAccessedWithFunction(description: String, relevantFunction: AstNode, fileSourceCode: String, suspectedAnswerEnumFiles: List<File>): Set<Tag> {
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
                                        // dotAcess will have a two elements [.value, .osmValue] on "answer.value.osmValue"
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
            println()
            println("failed!")
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
