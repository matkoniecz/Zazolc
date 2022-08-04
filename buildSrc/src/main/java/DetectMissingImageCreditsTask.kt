import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStream
import java.util.Locale
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

            // Contacted Naposm on https://github.com/streetcomplete/StreetComplete/pull/2675#issuecomment-1168967696
            "costiera.svg", // https://github.com/streetcomplete/StreetComplete/commit/3717423dd6c2597440112801f32db5814abbe281 https://commons.wikimedia.org/wiki/File:Guardia_Costiera.svg
            "police.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/police.svg https://github.com/streetcomplete/StreetComplete/commit/3717423dd6c2597440112801f32db5814abbe281
            "fuel_self_service.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/fuel_self_service.svg https://github.com/streetcomplete/StreetComplete/commit/dbc19c8651cd987acb4044343c5d07c5d2ff56e6

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

            // res/graphics/undo/
            "visibility.svg",

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

            "check_shop.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/check_shop.svg

            "max_height_measure.svg", // https://github.com/streetcomplete/StreetComplete/commits/master/res/graphics/quest/max_height_measure.svg

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
            "overview_data_flow.drawio"
        )
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
            "app/src/main/res/drawable/ic_roof_round_skillion.xml", // tried res/graphics/roof shape/round_skillion.svg
            "app/src/main/res/drawable/ic_roof_skillion.xml", // tried res/graphics/roof shape/skillion.svg
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
            "start_over.svg"
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
            val foldersWithSpaces = File("res/graphics/").listFiles().filter { it.isDirectory && it.name.contains(" ") }
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

    private fun svgOfDrawableFromElements(drawableFile: File, removeFromFilename: String, guessedFolder: String): File {
        var guessedFile = drawableFile.name.replace(removeFromFilename, "").replace(".xml", ".svg")
        guessedFile = guessedFile.replace("beachvolleyball", "beach_volleyball")
        guessedFile = guessedFile.replace("simple_suspension", "simple-suspension")
        guessedFile = guessedFile.replace("cablestayed", "cable-stayed")
        guessedFile = guessedFile.replace("type_panning.svg", "camera_panning.svg")
        guessedFile = guessedFile.replace("type_dome.svg", "camera_dome.svg")
        guessedFile = guessedFile.replace("type_fixed.svg", "camera.svg")
        guessedFile = guessedFile.replace("ic_living_street.svg", "default.svg")
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

    private class LicenceData(val licence: String, val folderPathFilter: String, val file: String, val source: String) {
        override fun toString(): String {
            return "LicensedData(\"$licence\", \"$folderPathFilter\", \"$file\", \"$source\")"
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
        knownLicenced += licensedMediaGreedyScan(secondLocation, 3)
        return knownLicenced + licencedMediaInApplicationResourceFile()
    }

    private fun licensedMediaGreedyScan(folderPath: String, skippedLines: Int): MutableList<LicenceData> {
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
        val inputStream: InputStream = File("$location/authors.txt").inputStream()
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
                    val source = splitted[1].trim()
                    licenceFound = licence
                    if (file.isNotEmpty() && source.isNotEmpty()) {
                        knownLicenced += LicenceData(licence, location, file, source)
                    } else if (entire_line.indexOf("                               ") == 0 && source.isNotEmpty()) {
                        // TODO: update license info as file is combination of multiple ones
                        // for now this is fine as this program only checks is license info present,
                        // it is not actually used
                        // knownLicenced[knownLicenced.size-1]
                    } else {
                        println("either file or source is empty, so skipping the entire line")
                        println("line: <$line>")
                        println("file: <$file>")
                        println("source: <$source>")
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
            mapOf("filename" to "text", "licencedIdentifier" to "text")
        )
        for (pair in matchingPairs) {
            if (!fileMatchesLicenceDeclaration(File("path/" + pair["filename"]!!), LicenceData("license", "path", pair["licencedIdentifier"]!!, "source"))) { // TODO: !! should be not needed here
                throw Exception("$pair failed to match")
            }
        }
    }
}
