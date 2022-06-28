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

            // res/graphics/pins/
            "clock.svg",
            "parking.svg",
            "motorcycle_parking.svg",
            "money.svg",
            "toilets.svg",
            "car_charger.svg",
            "bicycle_parking.svg",
            "phone.svg",
            "fire_hydrant.svg",
            "bollard.svg",
            "recycling_container.svg",
            "picnic_table.svg",
            "power.svg",
            "book.svg",
            "crossing.svg",
            "defibrillator.svg",

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
            "crown.svg",
            "no_bicycles.svg", // https://github.com/streetcomplete/StreetComplete/commit/756433648af92b17e319c01b85f815b24766c114
            "board_type.svg",
            "surveillance_camera.svg",
            "steps_count.svg",
            "steps_count_brown.svg",
            "way_surface_detail.svg",
            "bicycleway_surface_detail.svg",
            "footway_surface.svg",
            "sidewalk_surface.svg",
            "mail.svg",
            "bus_stop_lit.svg",
            "surveillance.svg",
            "police.svg",
            "car_air_compressor.svg",
            "bicycle_second_hand.svg",
            "fuel_self_service.svg",
            "check_shop.svg",
            "pitch_lantern.svg",
            "picnic_table_cover.svg",
            "recycling_clothes.svg",
            "door_address.svg",
            "kerb_tactile_paving.svg",
            "bicycle_parking_fee.svg",
            "fire_hydrant_grass.svg",
            "bicycle_rental.svg",
            "bicycle_repair.svg",
            "bicycle_parking_access.svg",
            "fee.svg",
            "max_height_measure.svg",

            // res/graphics/living street
            // definitely fine, just not sure why (traffic signs transformed by Tobias)
            "australia.svg",
            "default.svg",
            "france.svg",
            "mexico.svg",
            "portrait.svg",
            "sadc.svg",

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
                }
            }
        }
        for (licenced in knownLicenced) {
            if (licenced !in usedLicenced) {
                System.err.println(licenced.file + " with path filter " + licenced.folderPathFilter + " from " + licenced.source + " appears to be credit for nonexisting file, either there is some typo or this file was deleted and credit also should be removed.")
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
        for(path in arrayOf("app/", "res/")) {
            File(path).walkTopDown().forEach {
                if (!("app/build/" in it.path)) {
                    if (it.name.contains(".")) {
                        val splitExtension = it.name.split(".")
                        if (splitExtension.size > 1) {
                            val extension = splitExtension.last()
                            if (extension !in listOf("yaml", "yml", "xml", "txt", "json", "jar", "kt", "kts", "bin", "md", "gitignore", "MockMaker", "pro")) {
                                if (extension in listOf("jpg", "svg", "png", "wav", "xcf", "ser", "odp", "drawio")) {
                                    mediaFiles += MediaFile(it)
                                } else {
                                    println("not recognised extension: $extension")
                                }
                            }
                        }
                    }
                }
            }
        }
        return mediaFiles
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
        if (licencedData.folderPathFilter !in file.path) {
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
