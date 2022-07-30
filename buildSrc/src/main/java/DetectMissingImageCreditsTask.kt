import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStream
import kotlin.system.exitProcess

/*
This will try to report entries missing in StreetComplete image authorship file

./gradlew detectMissingImageCreditsTask
 */

open class DetectMissingImageCreditsTask : DefaultTask() {
    private fun filesWithKnownProblemsAndSkipped(): Array<String> {
        // TODO: should be empty
        return arrayOf(
            //////////////////////////////////////////////
            //////////////////////////////////////////////
            // issue on the StreetComplete bug tracker was created
            //////////////////////////////////////////////
            //////////////////////////////////////////////


            //////////////////////////////////////////////
            //////////////////////////////////////////////
            // uploaders contacted recently and/or media is safe but with not specified source
            //////////////////////////////////////////////
            //////////////////////////////////////////////

            // contacted FloEdelmann on https://github.com/streetcomplete/StreetComplete/commit/b0dc6a9d0e10eb195fb566ee832e15bdb1790c0d#commitcomment-77199705
            // https://github.com/streetcomplete/StreetComplete/commit/b0dc6a9d0e10eb195fb566ee832e15bdb1790c0d#commitcomment-77199705
            "bin.svg", // pin - https://github.com/streetcomplete/StreetComplete/commit/b0dc6a9d0e10eb195fb566ee832e15bdb1790c0d#commitcomment-77199705
            "picnic_table_cover.svg", // https://github.com/streetcomplete/StreetComplete/commit/b0dc6a9d0e10eb195fb566ee832e15bdb1790c0d#commitcomment-77199705
            "bicycle_parking_access.svg", // https://github.com/streetcomplete/StreetComplete/commit/b0dc6a9d0e10eb195fb566ee832e15bdb1790c0d#commitcomment-77199705
            "bicycle_rental_capacity",// https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/bicycle_rental_capacity.svg https://github.com/streetcomplete/StreetComplete/commit/396dfbb7b6805f28de893eaf1e77960f85e177e2#diff-8cecc5df0db2b095b9deff1360855887da42441340b1d83e75648f39f2971a09

            // Contacted Naposm on https://github.com/streetcomplete/StreetComplete/pull/2675#issuecomment-1168967696
            "costiera.svg", // https://github.com/streetcomplete/StreetComplete/commit/3717423dd6c2597440112801f32db5814abbe281 https://commons.wikimedia.org/wiki/File:Guardia_Costiera.svg
             "police.svg",  // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/police.svg https://github.com/streetcomplete/StreetComplete/commit/3717423dd6c2597440112801f32db5814abbe281
            "fuel_self_service.svg",  // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/fuel_self_service.svg https://github.com/streetcomplete/StreetComplete/commit/dbc19c8651cd987acb4044343c5d07c5d2ff56e6

            // https://github.com/streetcomplete/StreetComplete/commit/e651adad062d283bd4a3399556fe413401ab272d
            "overlay.svg",

            //////////////////////////////////////////////
            //////////////////////////////////////////////
            // safe, but failed to track down actual source
            //////////////////////////////////////////////
            //////////////////////////////////////////////

            // https://github.com/streetcomplete/StreetComplete/pull/1641#issuecomment-554031078
            "plop0.wav",
            "plop1.wav",
            "plop2.wav",
            "plop3.wav",

            //////////////////////////////////////////////
            //////////////////////////////////////////////
            // ongoing processing
            //////////////////////////////////////////////
            //////////////////////////////////////////////


            /*
            https://github.com/streetcomplete/StreetComplete/discussions/new
            More copyright botherings
            @westnordost

Sorry for bothering you about this, but some of media that were likely created fully by you are missing copyright info. Can you confirm that you took this photos?

            */

            // res/graphics/pins/
            "clock.svg",
            "picnic_table.svg",
            "book.svg",
            "crossing.svg",

            // res/graphics/cycleway/
            // what is the source of bicycle icon?
            // Seems copyrightable? See https://commons.wikimedia.org/wiki/Category:Bicycle_icons
            "lane_norway.svg",
            "none_no_oneway_l.svg",
            "none_no_oneway.svg",
            "shared_lane_france.svg",
            "lane_france.svg",

            // res/graphics/quest - TODO ask Tobias, check sources

            "halal.svg", // see https://github.com/streetcomplete/StreetComplete/commits/6e419923e6732030a7d41196676230b242c92ece/res/graphics/quest%20icons/halal.svg?browsing_rename_history=true&new_path=res/graphics/quest/halal.svg&original_branch=master for ping

            // Tobias - sole or used something else?
            /*

I have the next authorship question as I review files (let me know if it will become too annoying) - is it necessary to credit anyone else except you for following ones (quest icons unless mentioned otherwise):

footway_surface.svg (added in https://github.com/streetcomplete/StreetComplete/commit/b6d9fc144c38dae74c7362b56bbdad993f48b27c#diff-85f51c694a65e4eae5e63b8bb238cd69e38a76cf38677c2c730c78636bddca9d ) is solely you work, right?

*/

//------------------
//new questions
            "footway_surface.svg", // https://github.com/streetcomplete/StreetComplete/blob/master/res/graphics/quest/footway_surface.svg https://github.com/streetcomplete/StreetComplete/commit/b6d9fc144c38dae74c7362b56bbdad993f48b27c#diff-85f51c694a65e4eae5e63b8bb238cd69e38a76cf38677c2c730c78636bddca9d

// https://github.com/streetcomplete/StreetComplete/blob/master/res/graphics/quest/bicycleway_surface_detail.svg
// https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/board_type.svg

            "bicycleway_surface_detail.svg", // https://github.com/streetcomplete/StreetComplete/blob/master/res/graphics/quest/bicycleway_surface_detail.svg

            "sidewalk_surface.svg", // https://github.com/streetcomplete/StreetComplete/blob/master/res/graphics/quest/bicycleway_surface_detail.svg

            "check_shop.svg",  // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/check_shop.svg

            "max_height_measure.svg",// https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/max_height_measure.svg

            // res/graphics/recycling
            // confirm
            // https://github.com/streetcomplete/StreetComplete/commits/fa8bcd6ee5fa58334a8b844ae1c12522135fda8d/res/recycling%20icons/glass_bottles.svg?browsing_rename_history=true&new_path=res/graphics/recycling/glass_bottles.svg&original_branch=master
            // https://github.com/streetcomplete/StreetComplete/commit/575424b3a3009b75927d0f3c5a9c6f4c26ebea11#diff-6bf317e40f3d87cd7ecce87cca6d4d5f7d191303725529369cafe6728971bca3
            "glass.svg",
            "glass_bottles.svg",

            // res/graphics/living street
            // definitely fine, just not sure why (traffic signs transformed by Tobias)
            "mexico.svg", // https://github.com/streetcomplete/StreetComplete/commit/d457a6d737020bbd8ded4e994895fe98633e8944#diff-5cfacbd57f9f933ea18b64f99f291230e36d968531bb21af6e114324dd9c22b6
            // should it be also "Tobias Zwick CC-BY-SA 4.0 (based on public domain traffic sign design)"
            // BTW, is "Tobias Zwick CC-BY-SA 4.0 (based on public domain traffic sign design)" OK for other files in https://github.com/streetcomplete/StreetComplete/tree/master/res/graphics/living%20street ?

            // res/graphics/ar/
            "camera_measure_24dp.svg",
            "start_over.svg", // also PD-shape anyway
            "hand_phone.svg",

            // res/graphics/street parking
            "street_parking_bays_parallel.svg",
            "parking_and_stopping_signs_overview.xcf",
            "street_marked_parking_diagonal.svg",
            "street_marked_parking_perpendicular.svg",
            "street_parking_bays_diagonal.svg",
            "street_parking_bays_perpendicular.svg",
            "street_marked_parking_parallel.svg",

            // res/documentation
            // skipped for now
            "get_poeditor_cookie.png",
            "how-it-handles edits.odp",
            "overview_data_flow.svg",
            "overview_data_flow.drawio",
        )
    }

    private fun publicDomainAsSimpleShapesFilenames(): Array<String> {
        return arrayOf(
            "speech_bubble_top.9.png",
            "speech_bubble_top.9.svg",
            "speech_bubble_start.9.png",
            "speech_bubble_end.9.png",
            "speech_bubble_none.9.png",
            "speech_bubble_none.9.svg",
            "speech_bubble_bottom.9.png",
            "speech_bubble_bottom_center.9.png",
            "speech_bubble_bottom_center.9.svg",
            "speech_bubble_left.9.svg",
            "crosshair_marker.png", "location_direction.png",
            "building_levels_illustration_bg_left.png",
            "building_levels_illustration_bg_right.png",
            "background_housenumber_frame_slovak.9.png", "pin.png",
            "ic_star_white_shadow_32dp.png",
            "ic_none.png",

            // res/graphics/pins/
            // TODO still worth asking Tobias but with a lower priority
            "parking.svg",
            "phone.svg",
            "bollard.svg",
            "pedestrian_traffic_light.svg",

            // res/graphics/achievement
            "shine.svg",

            // res/graphics/lanes
            "lanes_marked_odd.svg",
            "lanes_marked.svg",
            "lanes_unmarked.svg",

            // res/graphics/oneway/no entry signs
            "arrow.svg",
            "default.svg",
            "do_not_enter.svg",
            "no_entre.svg",
            "no_entry.svg",
            "no_entry_on_white.svg",
            "yellow.svg",

            // res/graphics/oneway
            "oneway_no.svg",
            "oneway_yes_reverse.svg",
            "oneway_yes.svg",

            // res/graphics/pin
            "pin.svg",
            "pin.xcf",
            "pin_bubble.svg",
            "pin_pointer.svg",
            "pin_dot.xcf",

            // res/graphics/street parking
            "street_shoulder.svg",
            "street_shoulder_broad.svg",
            "street_very_narrow.svg",
            "street_none.svg",
            "street_narrow.svg",
            "street.svg",
            "street_broad.svg",
            "fat_footnote.svg",

            // res/graphics/street parking/bi-weekly no parking sign/
            "no_parking_first_half_of_month.svg",
            "no_parking_second_half_of_month.svg",

            // res/graphics/street parking/street edge marking
            "yellow_dashes.svg",
            "yellow_dash_x.svg",
            "yellow_zig_zag.svg",
            "double_yellow_zig_zag.svg",
            "yellow_on_curb.svg",
            "yellow_dashes_on_curb.svg",
            "yellow.svg",
            "red_double.svg",
            "red_on_curb.svg",
            "white_on_curb.svg",
            "yellow_white_dashes_on_curb.svg",
            "red.svg",
            "yellow_double.svg",
            "red_white_dashes_on_curb.svg",

            // res/graphics/street parking/no parking sign
            "australia.svg",
            "mutcd_latin.svg",
            "mutcd.svg",
            "mutcd_text_spanish.svg",
            "mutcd_text.svg",
            "sadc.svg",
            "taiwan.svg",
            "vienna.svg",
            "vienna_variant.svg",

            // res/graphics/street parking/no stopping sign
            "australia.svg",
            "canada.svg",
            "colombia.svg",
            "israel.svg",
            "mutcd_latin.svg",
            "mutcd.svg",
            "mutcd_text_spanish.svg",
            "mutcd_text.svg",
            "vienna.svg",

            // res/graphics/street parking/no standing sign
            "mutcd_text_waiting.svg",
            "mutcd_text_standing.svg",

            // res/graphics/street parking/alternate side parking sign
            "alternate_parking_on_days.svg",
            "no_parking_on_even_days.svg",
            "no_parking_on_odd_days.svg",

            // res/graphics/shoulder
            "no.svg",
            "white_line.svg",
            "short_white_dashes.svg",
            "white_dashes.svg",
            "yellow_line.svg",
            "two_yellow_lines.svg",
            "short_yellow_dashes.svg",

            // res/graphics/cycleway
            "none.svg",

            // res/graphics/religion
            "christian.svg",

            // res/graphics/sidewalk
            "illustration_yes.svg",
            "illustration_no.svg",
            "separate.svg",
            "separate_floating.svg",

            // res/graphics/undo
            "delete.svg",

            // app/src/main/assets/map_theme/jawg/images
            "oneway_arrow@2x.png",
            "pin_dot@2x.png",

            // res/graphics
            "compass_needle.svg",
            "1x1-transparent.png", // such small can be skipped automatically, probably

            // res/graphics/ar
            "start_over.svg",
            )
    }

    private fun validLicences(): Array<String> {
        // entries from https://spdx.org/licenses/
        // and "SIL OFL-1.1" as alias for "OFL-1.1"
        // and "fair use"
        return arrayOf("Public Domain", "CC0", "CC-BY-SA 1.0", "CC-BY-SA 2.0", "CC-BY-SA 2.5", "CC-BY-SA 3.0", "CC-BY-SA 4.0", "CC-BY 2.0", "CC-BY 3.0", "CC-BY 4.0", "SIL OFL-1.1", "OFL-1.1", "GPL-2.0-only", "WTFPL", "fair use")
    }

    private fun areThereMatchingSvgsForDrawables() {
        /*
        is each app/src/main/res/drawable having a matching SVG being stored?
            ./app/src/main/res/drawable/ic_quest_lantern.xml
            ./res/graphics/quest/lantern.svg

            ./app/src/main/res/drawable/ic_quest_pitch_lantern.xml
            ./res/graphics/quest/pitch_lantern.svg
         */
        File("app/src/main/res/drawable/").walkTopDown().filter { it.extension == "xml" }.forEach {
            val guessedFile = svgOfDrawable(it)
            if (guessedFile != null) {
                if (!guessedFile.isFile) {
                    println(it.path + " has not found match " + guessedFile.path)
                }
            }
        }
    }

    private fun svgOfDrawable(it: File): File? {
        if (it.name.startsWith("ic_")) {
            val likelyFolder = it.name.split("_")[1]
            var removeFromFilename = likelyFolder
            var guessedFolder = likelyFolder
            val initial = svgOfDrawableFromElements(it, removeFromFilename, guessedFolder)
            if(initial.isFile) {
                return initial
            }
            if (guessedFolder == "roof") {
                guessedFolder = "roof shape"
            }
            if (guessedFolder == "flag") {
                return null
            }
            if (guessedFolder == "pin") {
                guessedFolder = "pins"
            }
            listOf("stopping", "standing", "parking").forEach { code ->
                if ("no_$code" in it.name) {
                    guessedFolder = "street parking/no $code sign"
                    removeFromFilename = "no_$code"
                }
            }
            val foldersWithSpaces = File("res/graphics/").listFiles().filter { it.isDirectory && it.name.contains(" ") }
            foldersWithSpaces.forEach { folder ->
                if (folder.name.replace(" ", "_") in it.name) {
                    guessedFolder = folder.name
                    removeFromFilename = folder.name.replace(" ", "_")
                }
            }
            if (it.name.startsWith("ic_postbox_royal_cypher")) {
                guessedFolder = "royal cypher"
                removeFromFilename = "postbox_royal_cypher"
            }
            if (it.name.startsWith("ic_street_marking")) {
                guessedFolder = "street parking/street edge marking"
                removeFromFilename = "street_marking"
            }
            return svgOfDrawableFromElements(it, removeFromFilename, guessedFolder)
        }
        return null
    }

    private fun svgOfDrawableFromElements(drawableFile: File, removeFromFilename: String, guessedFolder: String): File {
        var guessedFile = drawableFile.name.replace("ic_${removeFromFilename}_", "").replace(".xml", ".svg")
        guessedFile = guessedFile.replace("beachvolleyball", "beach volleyball")
        guessedFile = guessedFile.replace("simple suspension", "simple-suspension")
        guessedFile = guessedFile.replace("cablestayed", "cable-stayed")
        return File("res/graphics/$guessedFolder/$guessedFile")
    }

    @TaskAction fun run() {
        selfTest()

        val knownLicenced = licencedMedia()
        val mediaFiles = mediaNeedingLicences()
        val usedLicenced = mutableListOf<LicenceData>()
        val billOfMaterials = mutableListOf<LicencedFile>()

        areThereMatchingSvgsForDrawables()

        /*
        println("---identifiers")
        println("---------")
        println("---------")
        for(licenced in knownLicenced) {
            println("-----------")
            println(licenced.file)
        }
        return
        println("---file names")
        println("---------")
        println("---------")
        for(file in mediaFiles) {
            println("-----------")
            println(file.filePath.getName())
        }
        return
        */

        var problemsFoundCount = 0
        var skippedProblemsFoundCount = 0
        for (file in mediaFiles) {
            var matched = false
            for (licenced in knownLicenced) {
                if (fileMatchesLicenceDeclaration(file.filePath, licenced)) {
                    if (matched) {
                        System.err.println(file.filePath.toString() + " matched to " + licenced.file + " but was matched already! License info should not be ambiguous and matching to multiple files!")
                        problemsFoundCount += 1
                    } else {
                        billOfMaterials += LicencedFile(licenced, file)
                        matched = true
                        usedLicenced += licenced
                    }
                    // println(file.filePath.toString() + " matched to " + licenced.file)
                }
            }
            if (!matched) {
                val name = file.filePath.name
                if (name !in publicDomainAsSimpleShapesFilenames()) {
                    if (containsSkippedFile(name)) {
                        println("skipping $name as listed on files with known problems")
                        skippedProblemsFoundCount += 1
                    } else {
                        System.err.println(file.filePath.toString() + ",")
                        System.err.println("$name remained unmatched")
                        System.err.println()
                        problemsFoundCount += 1
                    }
                } else {
                    val licenced = LicenceData("public domain", "not actually applicable", name, "listed in publicDomainAsSimpleShapesFilenames() as simple enough to not be copyrightable")
                    billOfMaterials += LicencedFile(licenced, file)
                }
            }
        }
        for (licenced in knownLicenced) {
            if (licenced !in usedLicenced) {
                System.err.println(licenced.file + " with path filter <" + licenced.folderPathFilter + "> from <" + licenced.source + "> appears to be credit for nonexisting file, either there is some typo or this file was deleted and credit also should be removed.")
                problemsFoundCount += 1
            }
        }
        if (problemsFoundCount > 0) {
            System.err.println((problemsFoundCount + skippedProblemsFoundCount).toString() + " problems, including $problemsFoundCount not even skipped problems, found with licensing - will exit with an error now")
            exitProcess(10)
        } else if (skippedProblemsFoundCount > 0) {
            System.err.println("$skippedProblemsFoundCount problems found with licensing - but only ones silensed (still should be fixed)")
        } else {
            System.err.println("No problems found with licensing")
        }
    }

    private class LicenceData(val licence: String, val folderPathFilter: String, val file: String, val source: String)

    private class MediaFile(val filePath: File)

    private class LicencedFile(val licence: LicenceData, val file: MediaFile)

    private fun containsSkippedFile(pattern: String): Boolean {
        for (file in filesWithKnownProblemsAndSkipped()) {
            if (pattern.contains(file)) {
                println("skipping $file as listed on files with known problems")
                return true
            }
        }
        return false
    }

    private fun licencedMedia(): List<LicenceData> {
        val knownLicenced = mutableListOf<LicenceData>()
        val firstLocation = "res/graphics"
        val secondLocation = "app/src/main/assets"

        knownLicenced += licensedMediaGreedyScan(firstLocation, 8)
        knownLicenced += licensedMediaGreedyScan(secondLocation, 8)
        return knownLicenced + licencedMediaInApplicationResourceFile()
    }

    private fun licensedMediaGreedyScan(folderPath: String, skippedLines: Int):  MutableList<LicenceData> {
        val source = "$folderPath/authors.txt"
        val inputStream: InputStream = File(source).inputStream()
        val inputString = inputStream.bufferedReader().use { it.readText() }
        val lines: List<String> = inputString.split("\n").drop(skippedLines)
        val knownLicenced = mutableListOf<LicenceData>()
        var folder: String? = null
        for (entire_line in lines) { // remove header lines
            if (entire_line.indexOf("                                ") == 0) {
                // continuation of previous line due to overly long line - should be skipped
                // TODO: merge such line with previous ones for processing
                continue
            }
            val line = entire_line.trim()
            if (line.length == 0) {
                folder = null
                continue
            }
            if (line[line.length - 1] == '/') {
                folder = line
                continue
            }
            val splitted = line.split(" ")
            val file = splitted[0].trim()
            val licence = "?"
            val filter = if (folder == null) {
                folderPath
            } else {
                "$folderPath/$folder"
            }
            knownLicenced += LicenceData(licence, filter, file, source)
        }
        return knownLicenced
    }

    private fun licencedMediaInApplicationResourceFile(): MutableList<LicenceData> {
        val location = "app/src/main/res"
        val knownLicenced = mutableListOf<LicenceData>()
        val inputStream: InputStream = File(location + "/authors.txt").inputStream()
        val inputString = inputStream.bufferedReader().use { it.readText() }
        for (entire_line in inputString.split("\n").drop(3)) { // remove header lines
            var skipped = false
            val line = entire_line.trim()
            if (line.length == 0) {
                continue
            }
            var licenceFound: String? = null
            for (licence in validLicences()) {
                val splitted = line.split(licence)
                if (splitted.size == 2) {
                    val file = splitted[0].trim()
                    val source = splitted[1].trim()
                    licenceFound = licence
                    if (file.length > 0 && source.length > 0) {
                        knownLicenced += LicenceData(licence, location, file, source)
                    } else {
                        println("either file or source is empty, so skipping the entire line: <$line> file: <$file> source: <$source>")
                    }
                }
            }
            if (containsSkippedFile(line)) {
                skipped = true
                println("skipping $line as listed on files with known problems")
            }
            if (licenceFound == null && !skipped) {
                throw Exception("unexpected licence in the input file was encountered, on $line")
            }
        }
        return knownLicenced
    }

    private fun mediaNeedingLicences(): MutableList<MediaFile> {
        val mediaFiles = mutableListOf<MediaFile>()
        for (path in arrayOf("app/", "res/")) {
            File(path).walkTopDown().forEach {
                if (isMediaFile(it)) {
                    mediaFiles += MediaFile(it)
                }
            }
        }
        return mediaFiles
    }

    private fun isMediaFile(it: File): Boolean {
        if ("app/build/" in it.path) {
            return false
        }
        if (it.isDirectory) {
            return false
        }
        if (it.name.contains(".")) {
            if (it.extension in listOf("yaml", "yml", "xml", "txt", "json", "jar", "kt", "kts", "bin", "md", "gitignore", "MockMaker", "pro")) {
                return false
            }
            if (it.extension in listOf("jpg", "svg", "png", "wav", "xcf", "ser", "odp", "drawio")) {
                return true
            } else {
                println("not recognised extension: ${it.extension}")
                return false
            }
        } else {
            return false
        }
    }

    private fun fileMatchesLicenceDeclaration(file: File, licencedData: LicenceData): Boolean {
        /*
        if ("cable-stayed" in file.name || "flask" in file.name) {
            if (licencedData.folderPathFilter !in file.path) {
                println("<<<<<<<<<<<<<<<<<<")
                println(licencedData.folderPathFilter)
                println("is not in")
                println(file.path)
                println(">>>>>>>>>>>>>>>>>>>>>>>>")
            } else {
                println("<<-----")
                println(licencedData.folderPathFilter)
                println("is in")
                println(file.path)
                println(">>-----")
            }
        }
         */
        var filterTakenIntoAccount = licencedData.folderPathFilter
        while (filterTakenIntoAccount.indexOf("..") != -1) {
            val cutStart = filterTakenIntoAccount.indexOf("..") + 3
            filterTakenIntoAccount = filterTakenIntoAccount.substring(cutStart, filterTakenIntoAccount.length - 1)
            // once such relative paths will start having conflict something smart will need to be implemented
            // no need for that for now, only part after last /../ is processed
        }
        if (filterTakenIntoAccount !in file.path) {
            return false
        }
        val fileName = file.name
        val licencedFile = licencedData.file
        if (licencedFile[licencedFile.length - 1] == '…') {
            return fileMatchesShortenedLicenceDeclaration(fileName, licencedFile.substring(0, licencedFile.length - 1))
        }
        if (licencedFile.substring(licencedFile.length - 3, licencedFile.length) == "...") {
            return fileMatchesShortenedLicenceDeclaration(fileName, licencedFile.substring(0, licencedFile.length - 3))
        }
        return fileName == licencedFile
    }

    private fun fileMatchesShortenedLicenceDeclaration(fileName: String, licencedFile: String): Boolean {
        if (licencedFile.length > fileName.length) {
            return false
        }
        return fileName.substring(0, licencedFile.length) == licencedFile
    }

    private fun selfTest() {
        val matchingPairs = arrayOf(
            mapOf("filename" to "surface_paving_stones_bad.jpg", "licencedIdentifier" to "surface_paving_stones_bad.jpg"),
            mapOf("filename" to "recycling_container_underground.jpg", "licencedIdentifier" to "recycling_container_undergr..."),
            mapOf("filename" to "barrier_passage.jpg", "licencedIdentifier" to "barrier_passage.jpg"),
            mapOf("filename" to "text", "licencedIdentifier" to "text"),
        )
        for (pair in matchingPairs) {
            if (!fileMatchesLicenceDeclaration(File("path/" + pair["filename"]!!), LicenceData("license", "path", pair["licencedIdentifier"]!!, "source"))) { // TODO: !! should be not needed here
                throw Exception("$pair failed to match")
            }
        }
    }
}
