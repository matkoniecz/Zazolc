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

            // Contacted Naposm
            // https://github.com/streetcomplete/StreetComplete/pull/2675#issuecomment-1168967696
            "costiera.svg", // https://github.com/streetcomplete/StreetComplete/commit/3717423dd6c2597440112801f32db5814abbe281 https://commons.wikimedia.org/wiki/File:Guardia_Costiera.svg
            "police.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/police.svg https://github.com/streetcomplete/StreetComplete/commit/3717423dd6c2597440112801f32db5814abbe281
            "fuel_self_service.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/fuel_self_service.svg https://github.com/streetcomplete/StreetComplete/commit/dbc19c8651cd987acb4044343c5d07c5d2ff56e6

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

            Sorry for bothering you about this, but some of media that were likely created fully by you are missing copyright info. Can you confirm that you took this photos or made this icons?

            footway_surface.svg ( https://github.com/streetcomplete/StreetComplete/commit/b6d9fc144c38dae74c7362b56bbdad993f48b27c right side, first row reaching right side, next to banned pedestrians)

            check_shop.svg https://github.com/streetcomplete/StreetComplete/commit/d2eab470e510247ed1c3fd121680746a05d8b315 (that was only adding missing .svg)
            drawable added in https://github.com/streetcomplete/StreetComplete/commit/5a3774e84ec6ab4ddf01bfad38d86c9bb303bd8f
            it may be not even qualifying for copyright

            recycling/glass.svg added in https://github.com/streetcomplete/StreetComplete/commit/575424b3a3009b75927d0f3c5a9c6f4c26ebea11#diff-6bf317e40f3d87cd7ecce87cca6d4d5f7d191303725529369cafe6728971bca3
            this one seems clearly with @westnordost as sole author, but I prefer to ask
            quest/recycling_glass.svg is listed as using Twemoji: U+267B ( https://commons.wikimedia.org/wiki/File:Twemoji_267b.svg - recycling icon ), otherwise done by @westnordost
            recycling/glass_bottles.svg is listed in credits with @westnordost as sole author

            picnic_table.svg pin https://github.com/streetcomplete/StreetComplete/commit/373a56f365ddfc2e521be74c2d8f8e6dd06c84b9
            book.svg pin https://github.com/streetcomplete/StreetComplete/commit/73ad3779652ae84f7f6fc35855c00c7dfab4d033#diff-010fa615562ca845e316379051de7f2fac42cc2b78d9a44cfad7935891f34cf8
            */
            "footway_surface.svg",
            "check_shop.svg",
            "glass.svg",

            // res/graphics/pins/
            // https://github.com/streetcomplete/StreetComplete/tree/master/res/graphics/pins
            "picnic_table.svg",
            "book.svg",

            // end of WNO questions


            // res/graphics/undo/
            "visibility.svg",
            "split.svg",

            // Tobias - sole or used something else?
            /*

I have the next authorship question as I review files (let me know if it will become too annoying) - is it necessary to credit anyone else except you for following ones (quest icons unless mentioned otherwise):

footway_surface.svg (added in https://github.com/streetcomplete/StreetComplete/commit/b6d9fc144c38dae74c7362b56bbdad993f48b27c#diff-85f51c694a65e4eae5e63b8bb238cd69e38a76cf38677c2c730c78636bddca9d ) is solely you work, right?

*/

//------------------
//new questions

// https://github.com/streetcomplete/StreetComplete/blob/master/res/graphics/quest/bicycleway_surface_detail.svg

            "footway_surface_detail.svg",

            "sidewalk_surface.svg", // https://github.com/streetcomplete/StreetComplete/blob/master/res/graphics/quest/bicycleway_surface_detail.svg

            "max_height_measure.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/max_height_measure.svg

            // res/graphics/recycling
            // confirm
            // https://github.com/streetcomplete/StreetComplete/commits/fa8bcd6ee5fa58334a8b844ae1c12522135fda8d/res/recycling%20icons/glass_bottles.svg?browsing_rename_history=true&new_path=res/graphics/recycling/glass_bottles.svg&original_branch=master
            // https://github.com/streetcomplete/StreetComplete/commit/575424b3a3009b75927d0f3c5a9c6f4c26ebea11#diff-6bf317e40f3d87cd7ecce87cca6d4d5f7d191303725529369cafe6728971bca3

            // res/graphics/living street
            // definitely fine, just not sure why (traffic signs transformed by Tobias)
            "mexico.svg", // https://github.com/streetcomplete/StreetComplete/commit/d457a6d737020bbd8ded4e994895fe98633e8944#diff-5cfacbd57f9f933ea18b64f99f291230e36d968531bb21af6e114324dd9c22b6
            // should it be also "Tobias Zwick CC-BY-SA 4.0 (based on public domain traffic sign design)"
            // BTW, is "Tobias Zwick CC-BY-SA 4.0 (based on public domain traffic sign design)" OK for other files in https://github.com/streetcomplete/StreetComplete/tree/master/res/graphics/living%20street ?

            // res/graphics/step count
            "step.svg",

            // res/graphics/ar/
            "camera_measure_24dp.svg",
            "hand_phone.svg",

            // res/graphics/street parking
            "street_parking_bays_parallel.svg",
            "parking_and_stopping_signs_overview.xcf",
            "street_marked_parking_diagonal.svg",
            "street_marked_parking_perpendicular.svg",
            "street_parking_bays_diagonal.svg",
            "street_parking_bays_perpendicular.svg",
            "street_marked_parking_parallel.svg",

            // note
            "add_photo_24dp.svg",
            "attach_gpx_24dp.svg",

            // res/documentation
            // skipped for now
            "get_poeditor_cookie.png",
            "how-it-handles edits.odp",
            "overview_data_flow.svg",
            "overview_data_flow.drawio",

            // mess of zero importance
            // png file does not exist somehow, only xml one exists
            // but it is fair use, should we even have png file
            // no reason to spend time on that unless it is blocking merge
            // of that checker into main StreetComplete
            "ic_link_cyclosm",

            // rare case where drawable in a specific country is placed in
            // for example filepath
            // ./app/src/main/res/drawable-mcc505-xhdpi/vibrating_button_illustration.jpg
            // what overrides
            // ./app/src/main/res/drawable-xhdpi/vibrating_button_illustration.jpg
            // in specific area via special handling
            //
            // rare, there is no need to handle this specially
            "vibrating_button_i... (MCC234)",
            "vibrating_button_i... (MCC505)",

            // not even recognized as media
            "*.json",
            "fonts/*.*",
            "ic_trophy.xml",
        )
    }

    private fun publicDomainAsSimpleShapesFilenames(): Array<String> {
        // would be marked with PD-shape on Wikimedia Commons or OSM Wiki
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

            // res/graphics/oneway/no entry signs
            "arrow.svg",
            "default.svg",
            "do_not_enter.svg",
            "no_entre.svg",
            "no_entry.svg",
            "no_entry_on_white.svg",
            "yellow.svg",

            // res/graphics/ar/
            "start_over.svg",

            // res/graphics/cycleway/
            "shared_lane_france.svg",
            // in case that bicycle is above threshold of originality, see https://github.com/streetcomplete/StreetComplete/discussions/4321#discussioncomment-3503703
            "lane_norway.svg",
            "none_no_oneway_l.svg",
            "none_no_oneway.svg",
            "lane_france.svg",

            // res/graphics/pins/
            // TODO still worth asking Tobias but with a lower priority
            "parking.svg",
            "phone.svg",
            "bollard.svg",
            "pedestrian_traffic_light.svg",
            "clock.svg", // https://github.com/streetcomplete/StreetComplete/blob/master/res/graphics/pins/clock.svg
            "crossing.svg", // https://github.com/streetcomplete/StreetComplete/blob/master/res/graphics/pins/crossing.svg

            // res/graphics/achievement
            "shine.svg",


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

            // res/graphics/quest
            "halal.svg", // it is simply text, see https://github.com/streetcomplete/StreetComplete/commit/699478ef9c6eb6f36ed84dbaca723d13323e345c#commitcomment-80477882

        )
    }

    private fun validLicences(): Array<String> {
        // entries from https://spdx.org/licenses/
        // and "SIL OFL-1.1" as alias for "OFL-1.1"
        // and "fair use"
        return arrayOf(
            "fair use", "SIL OFL-1.1",

            // codes matching spdx codes
            "Public Domain", "CC0", "CC-BY-SA 1.0", "CC-BY-SA 2.0", "CC-BY-SA 2.5", "CC-BY-SA 3.0",
            "CC-BY-SA 4.0", "CC-BY 2.0", "CC-BY 3.0", "CC-BY 4.0", "OFL-1.1", "GPL-2.0-only",
            "GPL-3.0-only", "ISC", "ODbL-1.0", "WTFPL"
        )
    }

    @TaskAction fun run() {
        selfTest()

        val knownLicenced = licencedMedia()
        val mediaFiles = mediaNeedingLicences()
        val usedLicenced = mutableListOf<LicenceData>()
        val billOfMaterials = mutableListOf<LicencedFile>()

        for (licenced in knownLicenced) {
            if (licenced.licence == "fair use") {
                continue
            }
            if (licenced.licence == "?") {
                // TODO: ideally would not happen and would trigger warning but for now...
                continue
            }
            var validLicenseStatus = false
            var licenseLink = ""
            val linksInSource = licenced.mediaSource?.split(" ")?.filter { it.startsWith("http") }
            if (linksInSource == null || linksInSource.isEmpty()) {
                continue
            }
            linksInSource.forEach {
                if (it.startsWith("https://github.com/streetcomplete/StreetComplete/")) {
                    // assumed to be reviewed
                    validLicenseStatus = true
                    licenseLink = it
                } else if (it.startsWith("https://commons.wikimedia.org/")) {
                    licenseLink = it
                    // should be reviewed
                    // TODO
                    validLicenseStatus = true
                    val fileName = licenseLink.replace("https://commons.wikimedia.org/wiki/File:", "")
                } else if (it.startsWith("https://wiki.openstreetmap.org/wiki/")) {
                    licenseLink = it
                    // should be reviewed
                    // TODO
                    validLicenseStatus = true
                    val fileName = licenseLink.replace("https://commons.wikimedia.org/wiki/File:", "")
                } else if (it.startsWith("https://www.geograph.org.uk")) {
                    validLicenseStatus = true
                    // TODO handle
                } else if (it.startsWith("https://www.geograph.ie")) {
                    validLicenseStatus = true
                    // TODO handle
                } else if (it.startsWith("https://pixabay.com")) {
                    validLicenseStatus = true
                    // TODO handle
                } else if (it.startsWith("https://flickr.com")) {
                    validLicenseStatus = true
                    // TODO handle
                } else {
                    // ???
                }
            }
            if (!validLicenseStatus) {
                println("-----------")
                println("suspected invalid license state...")
                println(licenced.file)
                println(licenced.licence)
                println(licenced.mediaSource)
                println(licenced.filepathToCreditSource)
            }
        }

        /*
        println("---identifiers")
        println("---------")
        println("---------")
        for (licenced in knownLicenced) {
            println("-----------")
            println(licenced.file)
        }
        return
        println("---file names")
        println("---------")
        println("---------")
        for (file in mediaFiles) {
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
                        System.err.println(file.filePath.toString() + " matched to " + licenced.file + " with path filter " + licenced.folderPathFilter + " but was matched already! License info should not be ambiguous and matching to multiple files!")
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
                        println("skipping unmatched $name as listed on files with known problems")
                        skippedProblemsFoundCount += 1
                    } else {
                        System.err.println(file.filePath.toString() + ",")
                        System.err.println("$name remained unmatched")
                        System.err.println()
                        problemsFoundCount += 1
                    }
                } else {
                    val licenced = LicenceData("public domain", null, "not actually applicable", name, "listed in publicDomainAsSimpleShapesFilenames() as simple enough to not be copyrightable")
                    billOfMaterials += LicencedFile(licenced, file)
                }
            }
        }
        for (licenced in knownLicenced) {
            if (licenced !in usedLicenced) {
                if (containsSkippedFile(licenced.file)) {
                    System.err.println(licenced.file + " with path filter <" + licenced.folderPathFilter + "> from <" + licenced.filepathToCreditSource + "> credit that has not matched anything was skipped.")
                    skippedProblemsFoundCount += 1
                } else {
                    System.err.println(licenced.file + " with path filter <" + licenced.folderPathFilter + "> from <" + licenced.filepathToCreditSource + "> appears to be credit for nonexisting file, either there is some typo or this file was deleted and credit also should be removed.")
                    problemsFoundCount += 1
                }
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

    private class LicenceData(val licence: String, val mediaSource: String?, val folderPathFilter: String, val file: String, val filepathToCreditSource: String) {
        override fun toString(): String {
            return "LicensedData(\"$licence\", \"$folderPathFilter\", \"$file\", \"$filepathToCreditSource\")"
        }
    }

    private class MediaFile(val filePath: File) {
        override fun toString(): String {
            return "MediaFile(\"$filePath\")"
        }
    }

    private class LicencedFile(val licence: LicenceData, val file: MediaFile) {
        override fun toString(): String {
            return "LicencedFile(\"$licence\", \"$file\")"
        }
    }

    private fun containsSkippedFile(pattern: String): Boolean {
        for (file in filesWithKnownProblemsAndSkipped()) {
            if (pattern.contains(file)) {
                //println("skipping $file as listed on files with known problems")
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
        knownLicenced += licensedMediaGreedyScan(secondLocation, 3)
        return knownLicenced + licencedMediaInApplicationResourceFile()
    }

    private fun licensedMediaGreedyScan(folderPath: String, skippedLines: Int): MutableList<LicenceData> {
        val filepathToCreditSource = "$folderPath/authors.txt"
        val inputStream: InputStream = File(filepathToCreditSource).inputStream()
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
            if (line.isEmpty()) {
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
            val mediaSource = "?"
            if (splitted.size == 1) {
                println()
                println(filepathToCreditSource)
                println(entire_line)
                println("is it safe to assume Westnordost as the author?")
                println()
            } else {
                println(entire_line + "HOW TO EXTRACT LICENSE DATA HERE?")
            }
            val filter = if (folder == null) {
                folderPath
            } else {
                "$folderPath/$folder"
            }
            knownLicenced += LicenceData(licence, mediaSource, filter, file, filepathToCreditSource)
        }
        return knownLicenced
    }

    private fun licencedMediaInApplicationResourceFile(): MutableList<LicenceData> {
        val location = "app/src/main/res"
        val knownLicenced = mutableListOf<LicenceData>()
        val authorsFilePath = "$location/authors.txt"
        val inputStream: InputStream = File(authorsFilePath).inputStream()
        val inputString = inputStream.bufferedReader().use { it.readText() }
        for (entire_line in inputString.split("\n").drop(3)) { // remove header lines
            var skipped = false
            val line = entire_line.trim()
            if (line.isEmpty()) {
                continue
            }
            var licenceFound: String? = null
            for (licence in validLicences()) {
                val splitted = line.split(licence)
                if (splitted.size == 2) {
                    val file = splitted[0].trim()
                    val filepathToCreditSource = splitted[1].trim()
                    licenceFound = licence
                    if (file.isNotEmpty() && filepathToCreditSource.isNotEmpty()) {
                        knownLicenced += LicenceData(licence, filepathToCreditSource, location, file, authorsFilePath)
                    } else if (entire_line.indexOf("                               ") == 0 && filepathToCreditSource.isNotEmpty()) {
                        // TODO: update license info as file is combination of multiple ones
                        // for now this is fine as this program only checks is license info present,
                        // it is not actually used
                        // knownLicenced[knownLicenced.size-1]
                    } else {
                        println("either file or source is empty, so skipping the entire line")
                        println("line: <$line>")
                        println("file: <$file>")
                        println("source: <$filepathToCreditSource>")
                        throw Exception()
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
        val filterTakenIntoAccount = actuallyUsedPathFilter(licencedData.folderPathFilter)

        if (filterTakenIntoAccount !in file.path) {
            // completely missing and mismatching, there is no hope here
            return false
        }

        // detect case where /res/graphics/pins/mail.svg matches something with
        // folderPathFilter /res/graphics/mail.svg
        if (!filenameMatchesFilenameInLicenceDeclaration(file.name, licencedData.file)) {
            return false
        }
        if (filterTakenIntoAccount == "app/" + file.parentFile.path) {
            return true
        }
        if (filterTakenIntoAccount == file.parentFile.path + "/") {
            return true
        }
        suffixesForDrawablePaths().forEach { suffix ->
            if ("$filterTakenIntoAccount$suffix" == file.parentFile.path) {
                return true
            }
        }
        /*
        println("app/$filterTakenIntoAccount != ${file.parentFile.path}")
        println("$filterTakenIntoAccount != ${file.parentFile.path}/")
        suffixesForDrawablePaths().forEach { suffix ->
            println("$filterTakenIntoAccount$suffix != ${file.parentFile.path}")
        }
        */
        return false
    }

    private fun actuallyUsedPathFilter(folderPathFilter: String): String {
        var filterTakenIntoAccount = folderPathFilter
        // handle relative paths such as ../../app/src/main/
        while (filterTakenIntoAccount.indexOf("..") != -1) {
            val cutStart = filterTakenIntoAccount.indexOf("..") + 3
            filterTakenIntoAccount = filterTakenIntoAccount.substring(cutStart, filterTakenIntoAccount.length)
            // once such relative paths will start having conflict something smart will need to be implemented
            // no need for that for now, only part after last /../ is processed
        }
        return filterTakenIntoAccount
    }

    private fun suffixesForDrawablePaths(): List<String> {
        val allowedAdditionalSuffixes = mutableListOf<String>()
        allowedAdditionalSuffixes.add("")
        allowedAdditionalSuffixes.add("/drawable-nodpi")
        allowedAdditionalSuffixes.add("/raw")
        listOf("l", "m", "h", "xh", "xxh", "xxxh").forEach { resolution ->
            allowedAdditionalSuffixes.add("/mipmap-${resolution}dpi")
            allowedAdditionalSuffixes.add("/drawable-mcc234-${resolution}dpi")
            allowedAdditionalSuffixes.add("/drawable-mcc505-${resolution}dpi")
            allowedAdditionalSuffixes.add("/drawable-${resolution}dpi")
        }
        return allowedAdditionalSuffixes
    }

    private fun filenameMatchesFilenameInLicenceDeclaration(fileName: String, licencedFile: String): Boolean {
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
            mapOf("filename" to "text", "licencedIdentifier" to "text")
        )
        for (pair in matchingPairs) {
            if (!fileMatchesLicenceDeclaration(File("path" + "/" + pair["filename"]!!), LicenceData("license", "file source", "path", pair["licencedIdentifier"]!!, "selfTest artificial data"))) { // TODO: !! should be not needed here
                throw Exception("$pair failed to match")
            }
        }
    }
}
