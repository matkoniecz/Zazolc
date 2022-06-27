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
            "ic_link_cyclosm.xml", // https://github.com/cyclosm/cyclosm-cartocss-style/issues/615#issuecomment-1152875875
            "ramp_wheelchair.jpg", "ramp_stroller.jpg", "ramp_none.jpg", "ramp_bicycle.jpg", // https://github.com/streetcomplete/StreetComplete/issues/4103
            "location_nyan.png", "car_nyan.png", // note it as a a fair use in authors file? https://en.wikipedia.org/wiki/File:Nyan_cat_250px_frame.PNG
            "ic_link_weeklyosm.png", // https://wiki.openstreetmap.org/wiki/File:Weeklyosm_red_cut.svg https://wiki.openstreetmap.org/wiki/File:Logo_weeklyOSM.svg
            "plop0.wav",
            "plop1.wav",
            "plop2.wav",
            "plop3.wav",

            // maybe just note all of that as fair use?
            "ic_link_thenandnow.png",
            "ic_link_openstreetbrowser.png",
            "ic_link_photon.png",
            "ic_link_valhalla.png",
            "ic_link_heigit.png", // https://wiki.openstreetmap.org/wiki/File:HeiGIT.svg
            "ic_link_openstreetmap.png", // https://wiki.openstreetmap.org/wiki/File:Public-images-osm_logo.svg
            "ic_link_openvegemap.png", // https://wiki.openstreetmap.org/wiki/OpenVegeMap
            "ic_link_wheelmap.png", // https://wiki.openstreetmap.org/wiki/File:Wheelmap.org_logo.svg
            "ic_link_osrm.png", // https://wiki.openstreetmap.org/wiki/File:OSRM-Logo.png
            "ic_link_wiki.png", // https://wiki.openstreetmap.org/wiki/File:Wikilogo.png
            "ic_link_graphhopper.png", // https://wiki.openstreetmap.org/wiki/GraphHopper
            "ic_link_organic_maps.png", // https://github.com/organicmaps/organicmaps/discussions/1974#discussioncomment-2980726

            // res/graphics/building - TODO ask Tobias, check sources
            "fire_truck.svg",
            "ruins.svg",
            "abandoned.svg",
            "allotment_house.svg",
            "silo.svg",
            "boathouse.svg",
            "historic.svg",

            // res/graphics/quest - TODO ask Tobias, check sources
            "no_cow.svg",
            "kerb_type.svg",
            "no_cars.svg",
            "halal.svg", // see https://github.com/streetcomplete/StreetComplete/commits/6e419923e6732030a7d41196676230b242c92ece/res/graphics/quest%20icons/halal.svg?browsing_rename_history=true&new_path=res/graphics/quest/halal.svg&original_branch=master for ping

            // Tobias - sole or used something else?
            "card.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/card.svg

            "check.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/check.svg
            "door.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/door.svg
            "seating.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/seating.svg
            "bin.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/bin.svg
            "bench.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/bench.svg
            "water.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/water.svg
            "bicycle_pump.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/bicycle_pump.svg

            // res/graphics/living street
            // definitely fine, just not sure why (traffic signs transformed by Tobias)
            "australia.svg",
            "default.svg",
            "france.svg",
            "mexico.svg",
            "portrait.svg",
            "sadc.svg",

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

            // res/graphics/oneway/no entry signs
            "arrow.svg",
            "default.svg",
            "do_not_enter.svg",
            "no_entre.svg",
            "no_entry.svg",
            "no_entry_on_white.svg",
            "yellow.svg",

            //res/graphics/street parking
            "street_shoulder.svg",
            "street_shoulder_broad.svg",
            "street_very_narrow.svg",
            "street_none.svg",
            "street_narrow.svg",
            "street.svg",
            "street_broad.svg",

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

            // res/graphics/street parking/alternate side parking sign
            "alternate_parking_on_days.svg",
            "no_parking_on_even_days.svg",
            "no_parking_on_odd_days.svg",

            // res/graphics/shoulder
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
            )
    }

    private fun validLicences(): Array<String> {
        // entries from https://spdx.org/licenses/
        // and "SIL OFL-1.1" as alias for "OFL-1.1"
        // and "fair use"
        return arrayOf("Public Domain", "CC0", "CC-BY-SA 1.0", "CC-BY-SA 2.0", "CC-BY-SA 2.5", "CC-BY-SA 3.0", "CC-BY-SA 4.0", "CC-BY 2.0", "CC-BY 3.0", "CC-BY 4.0", "SIL OFL-1.1", "OFL-1.1", "GPL-2.0-only", "WTFPL", "fair use")
    }

    @TaskAction fun run() {
        selfTest()

        val knownLicenced = licencedMedia()
        val mediaFiles = mediaNeedingLicences()
        val usedLicenced = mutableListOf<LicenceData>()
        val billOfMaterials = mutableListOf<LicencedFile>()

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

        var problemsFound = false
        for (file in mediaFiles) {
            var matched = false
            for (licenced in knownLicenced) {
                if (fileMatchesLicenceDeclaration(file.filePath.name, licenced.file)) {
                    if (matched) {
                        System.err.println(file.filePath.toString() + " matched to " + licenced.file + " but was matched already! License info should not be ambiguous and matching to multiple files!")
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
                    } else {
                        System.err.println(file.filePath.toString() + " remained unmatched! It means that this file has no specified licensing status and is not on list of ignored ones. Likely it should be listed in authors.txt file")
                        System.err.println("$name remained unmatched")
                        System.err.println()
                        problemsFound = true
                    }
                }
            }
        }
        for (licenced in knownLicenced) {
            if (licenced !in usedLicenced) {
                System.err.println(licenced.file + " appears to be unused credit line, either there is some type of this file was deleted and credit also should be removed.")
            }
        }
        if (problemsFound) {
            System.err.println("problems found with licensing - will exit with an error now")
            exitProcess(10)
        }
    }

    private class LicenceData(val licence: String, val file: String, val source: String)

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
        val firstLocation = "res/graphics/authors.txt" // some noxious to process TODO: use
        val secondLocation = "app/src/main/assets/authors.txt" // bunch of special cases  TODO: use

        val firstInputStream: InputStream = File(firstLocation).inputStream()
        val secondInputStream: InputStream = File(secondLocation).inputStream()
        val firstInputString = firstInputStream.bufferedReader().use { it.readText() }
        val secondInputString = secondInputStream.bufferedReader().use { it.readText() }
        var folder: String? = null
        for (entire_line in (firstInputString.split("\n").drop(8) + secondInputString.split("\n").drop(8))) { // remove header lines
            val line = entire_line.trim()
            if (line.length == 0) {
                continue
            }
            val splitted = line.split(" ")
            val file = splitted[0].trim()
            if("/" in file) {
                folder = file
                continue
            }
            val source = "?"
            val licence = "?"
            knownLicenced += LicenceData(licence, file, source)
        }
        return knownLicenced + licencedMediaInApplicationResourceFile()
    }

    private fun licencedMediaInApplicationResourceFile(): MutableList<LicenceData> {
        val location = "app/src/main/res/authors.txt"
        val knownLicenced = mutableListOf<LicenceData>()
        val inputStream: InputStream = File(location).inputStream()
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
                        knownLicenced += LicenceData(licence, file, source)
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
        for(path in arrayOf("app/", "res/")) {
            File(path).walkTopDown().forEach {
                if (!("app/build/" in it.path)) {
                    if (it.name.contains(".")) {
                        val splitExtension = it.name.split(".")
                        if (splitExtension.size > 1) {
                            val extension = splitExtension.last()
                            if (extension !in listOf("yaml", "yml", "xml", "txt", "json", "jar", "kt", "bin", "md")) {
                                mediaFiles += MediaFile(it)
                            }
                        }
                    }
                }
            }
        }
        return mediaFiles
    }

    private fun fileMatchesLicenceDeclaration(fileName: String, licencedFile: String): Boolean {
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
            if (!fileMatchesLicenceDeclaration(pair["filename"]!!, pair["licencedIdentifier"]!!)) { // TODO: !! should be not needed here
                throw Exception(pair.toString() + " failed to match")
            }
        }
    }
}
