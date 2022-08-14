import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale
import kotlin.system.exitProcess

open class DetectMissingImageInSvgFormatTask : DefaultTask() {
    @TaskAction
    fun run() {
        areThereMatchingSvgsForDrawables()
    }

    private fun drawableFileKnownToBeWithoutSvgMatch(): Array<String> {
        return arrayOf(
            // TODO: remove all that
            "app/src/main/res/drawable/ic_achievements_48dp.xml", // tried res/graphics/achievements/48dp.svg
            "app/src/main/res/drawable/ic_add_24dp.xml", // tried res/graphics/add/24dp.svg
            "app/src/main/res/drawable/ic_add_photo_24dp.xml", // tried res/graphics/add/photo_24dp.svg
            "app/src/main/res/drawable/ic_animated_checkmark_circle.xml", // tried res/graphics/animated/checkmark_circle.svg
            "app/src/main/res/drawable/ic_animated_open_mail.xml", // tried res/graphics/animated/open_mail.svg
            "app/src/main/res/drawable/ic_arrow_down_white_96.xml", // tried res/graphics/arrow/down_white_96.svg
            "app/src/main/res/drawable/ic_arrow_drop_down_24dp.xml", // tried res/graphics/arrow/drop_down_24dp.svg
            "app/src/main/res/drawable/ic_arrow_right_96dp.xml", // tried res/graphics/arrow/right_96dp.svg
            "app/src/main/res/drawable/ic_attach_gpx_24dp.xml", // tried res/graphics/attach/gpx_24dp.svg
            "app/src/main/res/drawable/ic_axleload.xml", // tried res/graphics/axleload.xml/ic_axleload.svg
            "app/src/main/res/drawable/ic_bare_road_without_feature.xml", // tried res/graphics/bare/road_without_feature.svg
            "app/src/main/res/drawable/ic_bicycle.xml", // tried res/graphics/bicycle.xml/ic_bicycle.svg
            "app/src/main/res/drawable/ic_bookmarks_48dp.xml", // tried res/graphics/bookmarks/48dp.svg
            "app/src/main/res/drawable/ic_building_levels_illustration.xml", // tried res/graphics/building levels/illustration.svg
            "app/src/main/res/drawable/ic_camera_measure_24dp.xml", // tried res/graphics/surveillance/measure_24dp.svg
            "app/src/main/res/drawable/ic_check_48dp.xml", // tried res/graphics/check/48dp.svg
            "app/src/main/res/drawable/ic_checkmark_circle.xml", // tried res/graphics/checkmark/circle.svg
            "app/src/main/res/drawable/ic_chevron_next_24dp.xml", // tried res/graphics/chevron/next_24dp.svg
            "app/src/main/res/drawable/ic_close_24dp.xml", // tried res/graphics/close/24dp.svg
            "app/src/main/res/drawable/ic_compass_needle_48dp.xml", // tried res/graphics/compass/needle_48dp.svg
            "app/src/main/res/drawable/ic_create_note_24dp.xml", // tried res/graphics/create/note_24dp.svg
            "app/src/main/res/drawable/ic_cycleway_none_in_selection.xml", // tried res/graphics/cycleway/none_in_selection.svg
            "app/src/main/res/drawable/ic_delete_24dp.xml", // tried res/graphics/delete/24dp.svg
            "app/src/main/res/drawable/ic_drag_vertical.xml", // tried res/graphics/drag/vertical.svg
            "app/src/main/res/drawable/ic_electric_car.xml", // tried res/graphics/electric/car.svg
            "app/src/main/res/drawable/ic_email_24dp.xml", // tried res/graphics/email/24dp.svg
            "app/src/main/res/drawable/ic_fat_footnote.xml", // tried res/graphics/fat/footnote.svg
            "app/src/main/res/drawable/ic_file_upload_24dp.xml", // tried res/graphics/file/upload_24dp.svg
            "app/src/main/res/drawable/ic_file_upload_48dp.xml", // tried res/graphics/file/upload_48dp.svg
            "app/src/main/res/drawable/ic_github.xml", // tried res/graphics/github.xml/ic_github.svg
            "app/src/main/res/drawable/ic_info_outline_48dp.xml", // tried res/graphics/info/outline_48dp.svg
            "app/src/main/res/drawable/ic_launcher_foreground.xml", // tried res/graphics/launcher/foreground.svg
            "app/src/main/res/drawable/ic_liberapay.xml", // tried res/graphics/liberapay.xml/ic_liberapay.svg
            "app/src/main/res/drawable/ic_location_24dp.xml", // tried res/graphics/location/24dp.svg
            "app/src/main/res/drawable/ic_location_disabled_24dp.xml", // tried res/graphics/location/disabled_24dp.svg
            "app/src/main/res/drawable/ic_location_navigation_24dp.xml", // tried res/graphics/location/navigation_24dp.svg
            "app/src/main/res/drawable/ic_location_navigation_no_location_24dp.xml", // tried res/graphics/location/navigation_no_location_24dp.svg
            "app/src/main/res/drawable/ic_location_navigation_searching_24dp.xml", // tried res/graphics/location/navigation_searching_24dp.svg
            "app/src/main/res/drawable/ic_location_no_location_24dp.xml", // tried res/graphics/location/no_location_24dp.svg
            "app/src/main/res/drawable/ic_location_searching_24dp.xml", // tried res/graphics/location/searching_24dp.svg
            "app/src/main/res/drawable/ic_location_state_24dp.xml", // tried res/graphics/location/state_24dp.svg
            "app/src/main/res/drawable/ic_mail.xml", // tried res/graphics/mail.xml/ic_mail.svg
            "app/src/main/res/drawable/ic_mail_front.xml", // tried res/graphics/mail/front.svg
            "app/src/main/res/drawable/ic_menu_24dp.xml", // tried res/graphics/menu/24dp.svg
            "app/src/main/res/drawable/ic_more_24dp.xml", // tried res/graphics/more/24dp.svg
            "app/src/main/res/drawable/ic_motorcycle.xml", // tried res/graphics/motorcycle.xml/ic_motorcycle.svg
            "app/src/main/res/drawable/ic_national_speed_limit.xml", // tried res/graphics/national/speed_limit.svg
            "app/src/main/res/drawable/ic_no_parking.xml", // tried res/graphics/street parking/no parking sign/ic_no_parking.svg
            "app/src/main/res/drawable/ic_no_parking_first_half_of_month.xml", // tried res/graphics/street parking/no parking sign/first_half_of_month.svg
            "app/src/main/res/drawable/ic_no_parking_mutcd_latin_america.xml", // tried res/graphics/street parking/no parking sign/mutcd_latin_america.svg
            "app/src/main/res/drawable/ic_no_parking_on_even_days.xml", // tried res/graphics/street parking/no parking sign/on_even_days.svg
            "app/src/main/res/drawable/ic_no_parking_on_odd_days.xml", // tried res/graphics/street parking/no parking sign/on_odd_days.svg
            "app/src/main/res/drawable/ic_no_parking_second_half_of_month.xml", // tried res/graphics/street parking/no parking sign/second_half_of_month.svg
            "app/src/main/res/drawable/ic_no_standing_mutcd_text.xml", // tried res/graphics/street parking/no standing sign/mutcd_text.svg
            "app/src/main/res/drawable/ic_no_stopping.xml", // tried res/graphics/street parking/no stopping sign/ic_no_stopping.svg
            "app/src/main/res/drawable/ic_no_stopping_mutcd_latin_america.xml", // tried res/graphics/street parking/no stopping sign/mutcd_latin_america.svg
            "app/src/main/res/drawable/ic_no_waiting_mutcd_text.xml", // tried res/graphics/no/waiting_mutcd_text.svg
            "app/src/main/res/drawable/ic_oneway_no.xml", // tried res/graphics/oneway/no.svg
            "app/src/main/res/drawable/ic_oneway_yes.xml", // tried res/graphics/oneway/yes.svg
            "app/src/main/res/drawable/ic_oneway_yes_reverse.xml", // tried res/graphics/oneway/yes_reverse.svg
            "app/src/main/res/drawable/ic_open_in_browser_24dp.xml", // tried res/graphics/open/in_browser_24dp.svg
            "app/src/main/res/drawable/ic_open_in_browser_primary_24dp.xml", // tried res/graphics/open/in_browser_primary_24dp.svg
            "app/src/main/res/drawable/ic_osm_anon_avatar.xml", // tried res/graphics/osm/anon_avatar.svg
            "app/src/main/res/drawable/ic_parking_no.xml", // tried res/graphics/parking/no.svg
            "app/src/main/res/drawable/ic_parking_separate.xml", // tried res/graphics/parking/separate.svg
            "app/src/main/res/drawable/ic_path_segregated.xml", // tried res/graphics/path/segregated.svg
            "app/src/main/res/drawable/ic_path_segregated_l.xml", // tried res/graphics/path/segregated_l.svg
            "app/src/main/res/drawable/ic_path_segregated_no.xml", // tried res/graphics/path/segregated_no.svg
            "app/src/main/res/drawable/ic_patreon.xml", // tried res/graphics/patreon.xml/ic_patreon.svg
            "app/src/main/res/drawable/ic_postbox_royal_cypher_scottish_crown.xml", // tried res/graphics/royal cypher/SCOTTISH_CROWN.svg
            "app/src/main/res/drawable/ic_quest_create_note.xml", // tried res/graphics/quest/create_note.svg
            "app/src/main/res/drawable/ic_roadtype_dual_carriageway.xml", // tried res/graphics/roadtype/dual_carriageway.svg
            "app/src/main/res/drawable/ic_roadtype_lit.xml", // tried res/graphics/roadtype/lit.svg
            "app/src/main/res/drawable/ic_roadtype_lit_no.xml", // tried res/graphics/roadtype/lit_no.svg
            "app/src/main/res/drawable/ic_roadtype_urban.xml", // tried res/graphics/roadtype/urban.svg
            "app/src/main/res/drawable/ic_roadtype_urban_no.xml", // tried res/graphics/roadtype/urban_no.svg
            "app/src/main/res/drawable/ic_overlay_48dp.xml", // tried res/graphics/overlay/48dp.svg
            "app/src/main/res/drawable/ic_profile_48dp.xml", // tried res/graphics/profile/48dp.svg
            "app/src/main/res/drawable/ic_search_24dp.xml", // tried res/graphics/search/24dp.svg
            "app/src/main/res/drawable/ic_search_48dp.xml", // tried res/graphics/search/48dp.svg
            "app/src/main/res/drawable/ic_settings_48dp.xml", // tried res/graphics/settings/48dp.svg
            "app/src/main/res/drawable/ic_search_black_128dp.xml", // tried res/graphics/search/black_128dp.svg
            "app/src/main/res/drawable/ic_sidewalk_floating_separate.xml", // tried res/graphics/sidewalk/floating_separate.svg
            "app/src/main/res/drawable/ic_slim_arrow_up_16dp.xml", // tried res/graphics/slim/arrow_up_16dp.svg
            "app/src/main/res/drawable/ic_sport_racquet.xml", // tried res/graphics/sport/racquet.svg
            "app/src/main/res/drawable/ic_star_48dp.xml", // tried res/graphics/star/48dp.svg
            "app/src/main/res/drawable/ic_start_over_48.xml", // tried res/graphics/start/over_48.svg
            "app/src/main/res/drawable/ic_step.xml", // tried res/graphics/step.xml/ic_step.svg
            "app/src/main/res/drawable/ic_stop_recording_24dp.xml", // tried res/graphics/stop/recording_24dp.svg
            "app/src/main/res/drawable/ic_street.xml", // tried res/graphics/street.xml/ic_street.svg
            "app/src/main/res/drawable/ic_street_broad.xml", // tried res/graphics/street/broad.svg
            "app/src/main/res/drawable/ic_street_marked_parking_diagonal.xml", // tried res/graphics/street/marked_parking_diagonal.svg
            "app/src/main/res/drawable/ic_street_marked_parking_parallel.xml", // tried res/graphics/street/marked_parking_parallel.svg
            "app/src/main/res/drawable/ic_street_marked_parking_perpendicular.xml", // tried res/graphics/street/marked_parking_perpendicular.svg
            "app/src/main/res/drawable/ic_street_narrow.xml", // tried res/graphics/street/narrow.svg
            "app/src/main/res/drawable/ic_street_none.xml", // tried res/graphics/street/none.svg
            "app/src/main/res/drawable/ic_street_parking_bays_diagonal.xml", // tried res/graphics/street parking/bays_diagonal.svg
            "app/src/main/res/drawable/ic_street_parking_bays_parallel.xml", // tried res/graphics/street parking/bays_parallel.svg
            "app/src/main/res/drawable/ic_street_parking_bays_perpendicular.xml", // tried res/graphics/street parking/bays_perpendicular.svg
            "app/src/main/res/drawable/ic_street_side_unknown.xml", // tried res/graphics/street/side_unknown.svg
            "app/src/main/res/drawable/ic_street_side_unknown_l.xml", // tried res/graphics/street/side_unknown_l.svg
            "app/src/main/res/drawable/ic_street_very_narrow.xml", // tried res/graphics/street/very_narrow.svg
            "app/src/main/res/drawable/ic_subtract_24dp.xml", // tried res/graphics/subtract/24dp.svg
            "app/src/main/res/drawable/ic_tandem_axleload.xml", // tried res/graphics/tandem/axleload.svg
            "app/src/main/res/drawable/ic_team_mode_24dp.xml", // tried res/graphics/team/mode_24dp.svg
            "app/src/main/res/drawable/ic_town_silhouette.xml", // tried res/graphics/town/silhouette.svg
            "app/src/main/res/drawable/ic_truck.xml", // tried res/graphics/truck.xml/ic_truck.svg
            "app/src/main/res/drawable/ic_undo_24dp.xml", // tried res/graphics/undo/24dp.svg
        )
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
            if (it.name in listOf(
                    "ic_railway_crossing_full_l.xml", // left-hand driving side flip - is it worth keeping .svg for this?
                    "ic_railway_crossing_half_l.xml", // left-hand driving side flip - is it worth keeping .svg for this?
                    "ic_religion_animist.xml", // empty graphic (maybe an explictly empty graphic would be better here?
                    "ic_car1a.xml", // recolored ic_car1, missing in svg folder
                    "ic_car1b.xml", // recolored ic_car1, missing in svg folder
                    "ic_car2a.xml", // recolored ic_car1, missing in svg folder
                    "ic_car2b.xml", // recolored ic_car1, missing in svg folder
                    "ic_car3a.xml", // recolored ic_car1, missing in svg folder
                )
            ) {
                return@forEach
            }
            val guessedFile = svgOfDrawable(it)
            if (guessedFile != null) {
                if (!guessedFile.isFile) {
                    if ("ic_link_" in it.name) {
                        // some have no matching drawables but are fair use anyway...
                        // TODO maybe ad credits for them but with directly mention .xml files?
                    } else if (it.path in drawableFileKnownToBeWithoutSvgMatch()) {
                        // TODO - the target is to remove this!
                    } else {
                        println(it.path + " // tried " + guessedFile.path)
                        exitProcess(123)
                    }
                }
            }
        }
    }

    private fun svgOfDrawable(it: File): File? {
        // part of this horribleness can be fixed by moving files to a different names
        // but I do not want to make file names weird just to make copyright compliance
        // script code nicer...
        if (it.name.startsWith("ic_")) {
            val likelyFolder = it.name.split("_")[1]
            var removeFromFilename = "ic_${likelyFolder}_"
            var guessedFolder = likelyFolder
            val initial = svgOfDrawableFromElements(it, removeFromFilename, guessedFolder)
            if (initial.isFile) {
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
            if (guessedFolder == "camera") {
                guessedFolder = "surveillance"
            }
            listOf("stopping", "standing", "parking").forEach { code ->
                if ("no_$code" in it.name) {
                    guessedFolder = "street parking/no $code sign"
                    removeFromFilename = "ic_no_${code}_"
                }
            }
            val foldersWithSpaces =
                File("res/graphics/").listFiles().filter { it.isDirectory && it.name.contains(" ") }
            foldersWithSpaces.forEach { folder ->
                if (folder.name.replace(" ", "_") in it.name) {
                    guessedFolder = folder.name
                    removeFromFilename = "ic_" + folder.name.replace(" ", "_") + "_"
                }
            }
            if (it.name.startsWith("ic_postbox_royal_cypher")) {
                guessedFolder = "royal cypher"
                removeFromFilename = "ic_postbox_royal_cypher_"
            }
            if (it.name.startsWith("ic_street_marking")) {
                guessedFolder = "street parking/street edge marking"
                removeFromFilename = "ic_street_marking_"
            }
            if (it.name.startsWith("ic_no_entry_sign")) {
                guessedFolder = "oneway/no entry signs"
                removeFromFilename = "ic_no_entry_sign_"
            }
            if (it.name.startsWith("ic_car")) {
                guessedFolder = "lanes"
                removeFromFilename = "ic_"
            }
            return svgOfDrawableFromElements(it, removeFromFilename, guessedFolder)
        }
        return null
    }

    private fun svgOfDrawableFromElements(
        drawableFile: File,
        removeFromFilename: String,
        guessedFolder: String
    ): File {
        var guessedFile = drawableFile.name.replace(removeFromFilename, "").replace(".xml", ".svg")
        guessedFile = guessedFile.replace("beachvolleyball", "beach_volleyball")
        guessedFile = guessedFile.replace("simple_suspension", "simple-suspension")
        guessedFile = guessedFile.replace("cablestayed", "cable-stayed")
        guessedFile = guessedFile.replace("type_panning.svg", "camera_panning.svg")
        guessedFile = guessedFile.replace("type_dome.svg", "camera_dome.svg")
        guessedFile = guessedFile.replace("type_fixed.svg", "camera.svg")
        guessedFile = guessedFile.replace("ic_living_street.svg", "default.svg")
        guessedFile = guessedFile.replace("skillion.svg",
            "skillion_south.svg") // one of multiple options was used

        if (guessedFolder == "royal cypher") {
            if (" " !in guessedFile) {
                val cypher = guessedFile.split(".")[0].toUpperCase(Locale.ENGLISH)
                val extension = guessedFile.split(".")[1]
                guessedFile = "$cypher.$extension"
            }
        }
        if (guessedFolder == "lanes" && "car" !in guessedFile) {
            guessedFile = "lanes_$guessedFile"
        }
        return File("res/graphics/$guessedFolder/$guessedFile")
    }
}
