require_relative "licence_data"
require "open3"

def root_folder
    return "/home/mateusz/Documents/Archiwum/StreetComplete-ngi-zero"
end

def pd_simple_files
    return ["#{root_folder}/app/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker",
        "#{root_folder}/app/proguard-rules.pro",
        "#{root_folder}/build.gradle",
        "#{root_folder}/settings.gradle",
        "#{root_folder}/.gitattributes",
        "#{root_folder}/res/maxheight_sign.svg",
        "#{root_folder}/res/ic_axleload.svg",
        "#{root_folder}/res/ic_bogieweight.svg",
        "#{root_folder}/res/ic_create_note_black_24dp.svg",
        "#{root_folder}/res/ic_truck.svg",
        "#{root_folder}/res/quest_dot.xcf",
        "#{root_folder}/app/src/main/assets/map_theme/images/quest_dot@2x.png",
        "#{root_folder}/app/src/main/assets/map_theme/images/oneway_arrow@2x.png",
        "#{root_folder}/app/src/main/res/drawable-mdpi/crosshair_marker.png",
        "#{root_folder}/app/src/main/res/drawable-hdpi/crosshair_marker.png",
        "#{root_folder}/app/src/main/res/drawable-xhdpi/crosshair_marker.png",
        "#{root_folder}/app/src/main/res/drawable-xxhdpi/crosshair_marker.png",
        "#{root_folder}/app/src/main/res/drawable-mdpi/location_direction.png",
        "#{root_folder}/app/src/main/res/drawable-hdpi/location_direction.png",
        "#{root_folder}/app/src/main/res/drawable-xhdpi/location_direction.png",
        "#{root_folder}/app/src/main/res/drawable-xxhdpi/location_direction.png",
        "#{root_folder}/app/src/main/res/drawable-mdpi/background_housenumber_frame.9.png",
        "#{root_folder}/app/src/main/res/drawable-hdpi/background_housenumber_frame.9.png",
        "#{root_folder}/app/src/main/res/drawable-xhdpi/background_housenumber_frame.9.png",
        "#{root_folder}/app/src/main/res/drawable-xxhdpi/background_housenumber_frame.9.png",
        "#{root_folder}/app/src/main/res/drawable-mdpi/background_housenumber_frame_slovak.9.png",
        "#{root_folder}/app/src/main/res/drawable-hdpi/background_housenumber_frame_slovak.9.png",
        "#{root_folder}/app/src/main/res/drawable-xhdpi/background_housenumber_frame_slovak.9.png",
        "#{root_folder}/app/src/main/res/drawable-xxhdpi/background_housenumber_frame_slovak.9.png",
        "#{root_folder}/app/src/debug/res/mipmap-ldpi/building_levels_illustration_bg_left.png",
        "#{root_folder}/app/src/debug/res/mipmap-mdpi/building_levels_illustration_bg_left.png",
        
        "#{root_folder}/app/src/debug/res/mipmap-hdpi/building_levels_illustration_bg_left.png",
        "#{root_folder}/app/src/debug/res/mipmap-xhdpi/building_levels_illustration_bg_left.png",
        "#{root_folder}/app/src/debug/res/mipmap-xxhdpi/building_levels_illustration_bg_left.png",
        "#{root_folder}/app/src/debug/res/mipmap-ldpi/building_levels_illustration_bg_right.png",
        "#{root_folder}/app/src/debug/res/mipmap-mdpi/building_levels_illustration_bg_right.png",
        "#{root_folder}/app/src/debug/res/mipmap-hdpi/building_levels_illustration_bg_right.png",
        "#{root_folder}/app/src/debug/res/mipmap-xhdpi/building_levels_illustration_bg_right.png",
        "#{root_folder}/app/src/debug/res/mipmap-xxhdpi/building_levels_illustration_bg_right.png",
        "#{root_folder}/app/src/main/res/drawable-hdpi/building_levels_illustration_bg_left.png",
        "#{root_folder}/app/src/main/res/drawable-hdpi/building_levels_illustration_bg_right.png",
        "#{root_folder}/app/src/main/res/drawable-mdpi/building_levels_illustration_bg_left.png",
        "#{root_folder}/app/src/main/res/drawable-mdpi/building_levels_illustration_bg_right.png",
        "#{root_folder}/app/src/main/res/drawable-xhdpi/building_levels_illustration_bg_left.png",
        "#{root_folder}/app/src/main/res/drawable-xhdpi/building_levels_illustration_bg_right.png",
        "#{root_folder}/app/src/main/res/drawable-xxhdpi/building_levels_illustration_bg_left.png",
        "#{root_folder}/app/src/main/res/drawable-xxhdpi/building_levels_illustration_bg_right.png",
            ]
end

def made_by_main_author
    return [
        "#{root_folder}/res/roof_shape_icons.svg",
        "#{root_folder}/res/living_street.svg",
        "#{root_folder}/res/building_levels_illustration.svg",
        "#{root_folder}/res/scissors.svg",
        "#{root_folder}/res/slow_zone.svg",
        "#{root_folder}/res/slow_zone_us.svg",
        "#{root_folder}/res/export_svgs.py",
        "#{root_folder}/res/living_street_australia.svg",
        "#{root_folder}/res/living_street_france.svg",
        "#{root_folder}/res/living_street_russian.svg",
        "#{root_folder}/res/living_street_sadc.svg",
        "#{root_folder}/res/slow_zone_us.svg",
        "#{root_folder}/res/building_levels_illustration.svg",
        "#{root_folder}/res/ic_building_levels_illustration.svg",
        "#{root_folder}/res/cycleways.svg",
        "#{root_folder}/res/cycleway_segregation.svg",
        "#{root_folder}/res/osm_anon_avatar.svg",
        "#{root_folder}/app/generateCountryMetadata.py",
        "#{root_folder}/app/src/main/res/raw/plop0.wav",
        "#{root_folder}/app/src/main/res/raw/plop1.wav",
        "#{root_folder}/app/src/main/res/raw/plop2.wav",
        "#{root_folder}/app/src/main/res/raw/plop3.wav",
        "#{root_folder}/app/src/test/resources/hai_phong_street.jpg",
        "#{root_folder}/app/src/test/resources/mandalay_market.jpg",
        "#{root_folder}/res/roadtype_dual_carriageway.svg",
        "#{root_folder}/res/roadtype_lit.svg",
        "#{root_folder}/res/roadtype_lit_no.svg",
        "#{root_folder}/res/roadtype_urban.svg",
        "#{root_folder}/app/src/main/res/drawable-hdpi/quest_pin.png",
        "#{root_folder}/app/src/main/res/drawable-mdpi/quest_pin.png",
        "#{root_folder}/app/src/main/res/drawable-xhdpi/quest_pin.png",
        "#{root_folder}/app/src/main/res/drawable-xxhdpi/quest_pin.png",
        "#{root_folder}/res/quest_pin.xcf",
        "#{root_folder}/res/speech_bubble_left.9.xcf",
        "#{root_folder}/res/speech_bubble_none.9.xcf",
        "#{root_folder}/res/speech_bubble_top.9.xcf",
        "#{root_folder}/app/src/main/res/drawable-ldrtl-night-xxhdpi/speech_bubble_end.9.png",
        "#{root_folder}/app/src/main/res/drawable-ldrtl-night-xxhdpi/speech_bubble_end.9.png",
        "#{root_folder}/app/src/main/res/drawable-ldrtl-night-xxhdpi/speech_bubble_start.9.png",
        "#{root_folder}/app/src/main/res/drawable-ldrtl-night-xxhdpi/speech_bubble_top.9.png",
        "#{root_folder}/app/src/main/res/drawable-ldrtl-xxhdpi/speech_bubble_end.9.png",
        "#{root_folder}/app/src/main/res/drawable-ldrtl-xxhdpi/speech_bubble_start.9.png",
        "#{root_folder}/app/src/main/res/drawable-ldrtl-xxhdpi/speech_bubble_top.9.png",
        "#{root_folder}/app/src/main/res/drawable-night-xxhdpi/speech_bubble_end.9.png",
        "#{root_folder}/app/src/main/res/drawable-night-xxhdpi/speech_bubble_none.9.png",
        "#{root_folder}/app/src/main/res/drawable-night-xxhdpi/speech_bubble_start.9.png",
        "#{root_folder}/app/src/main/res/drawable-night-xxhdpi/speech_bubble_top.9.png",
        "#{root_folder}/app/src/main/res/drawable-xxhdpi/speech_bubble_end.9.png",
        "#{root_folder}/app/src/main/res/drawable-xxhdpi/speech_bubble_none.9.png",
        "#{root_folder}/app/src/main/res/drawable-xxhdpi/speech_bubble_start.9.png",
        "#{root_folder}/app/src/main/res/drawable-xxhdpi/speech_bubble_top.9.png",
        "#{root_folder}/res/speech_bubble_left.9.svg",
        "#{root_folder}/res/speech_bubble_none.9.svg",
        "#{root_folder}/res/speech_bubble_top.9.svg",
        "#{root_folder}/res/speech_bubble_none.9.png",
    ]
end

def gradle_files
    return [
        "#{root_folder}/gradle/wrapper/gradle-wrapper.jar",
        "#{root_folder}/gradle/wrapper/gradle-wrapper.properties",
        "#{root_folder}/gradlew.bat",
        "#{root_folder}/gradlew",
        "#{root_folder}/gradle.properties",
    ]
end

def app_icon_files
    return [
        "#{root_folder}/metadata/en/images/icon.png",
        "#{root_folder}/res/appicon_fancy.svg",
        "#{root_folder}/res/appicon_fancy-debug.svg",
        "#{root_folder}/res/appicon_flat.svg",
        "#{root_folder}/res/appicon_flat-debug.svg",
        "#{root_folder}/app/src/debug/res/mipmap-ldpi/ic_launcher.png",
        "#{root_folder}/app/src/debug/res/mipmap-mdpi/ic_launcher.png",
        "#{root_folder}/app/src/debug/res/mipmap-hdpi/ic_launcher.png",
        "#{root_folder}/app/src/debug/res/mipmap-hdpi/ic_launcher.png",
        "#{root_folder}/app/src/debug/res/mipmap-xhdpi/ic_launcher.png",
        "#{root_folder}/app/src/debug/res/mipmap-xxhdpi/ic_launcher.png",
        "#{root_folder}/app/src/debug/res/mipmap-xxxhdpi/ic_launcher.png",
        "#{root_folder}/app/src/main/res/mipmap-hdpi/ic_dl_notification.png",
        "#{root_folder}/app/src/main/res/mipmap-hdpi/ic_launcher.png",
        "#{root_folder}/app/src/main/res/mipmap-ldpi/ic_dl_notification.png",
        "#{root_folder}/app/src/main/res/mipmap-ldpi/ic_launcher.png",
        "#{root_folder}/app/src/main/res/mipmap-mdpi/ic_dl_notification.png",
        "#{root_folder}/app/src/main/res/mipmap-mdpi/ic_launcher.png",
        "#{root_folder}/app/src/main/res/mipmap-xhdpi/ic_dl_notification.png",
        "#{root_folder}/app/src/main/res/mipmap-xhdpi/ic_launcher.png",
        "#{root_folder}/app/src/main/res/mipmap-xxhdpi/ic_dl_notification.png",
        "#{root_folder}/app/src/main/res/mipmap-xxhdpi/ic_launcher.png",
        "#{root_folder}/app/src/main/res/mipmap-xxxhdpi/ic_dl_notification.png",
        "#{root_folder}/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
    ]
end

def the_same_authorship 
    return [
        "#{root_folder}/tools/view_db.bat",
        "#{root_folder}/tools/view_test_db.bat",
        "#{root_folder}/tools/find_popular_sports_by_country.py",
        "#{root_folder}/metadata/en/images/phoneScreenshots/screenshot01.png",
        "#{root_folder}/metadata/en/images/phoneScreenshots/screenshot02.png",
        "#{root_folder}/metadata/en/images/phoneScreenshots/screenshot03.png",
        "#{root_folder}/metadata/en/images/phoneScreenshots/screenshot04.png",
        "#{root_folder}/metadata/en/images/phoneScreenshots/screenshot05.png",
        "#{root_folder}/metadata/en/images/phoneScreenshots/screenshot06.png",
        "#{root_folder}/metadata/en/images/phoneScreenshots/screenshot07.png",
        "#{root_folder}/metadata/en/images/phoneScreenshots/screenshot08.png",
        "#{root_folder}/.github/ISSUE_TEMPLATE/bug_report.md",
        "#{root_folder}/.github/ISSUE_TEMPLATE/feature_request.md",
        "#{root_folder}/.github/ISSUE_TEMPLATE/quest-suggestion.md",
        "#{root_folder}/app/copyShopDescriptions.py",
        "#{root_folder}/.editorconfig",
        "#{root_folder}/.gitignore",
        "#{root_folder}/app/.gitignore",
    ]
end

def manually_licenced_files
    manually_licenced = licence_data()
    manually_licenced << {licence: "CC-BY-SA-4.0", file: "#{root_folder}/res/sport_icons.svg", author: "Tobias Zwick (american_football, athletics, australian_football, baseball, boules, bowls, canadian_football, handball, ice_stock, racquetball, paddle_tennis, sepak_takraw, skateboard, softball, skating) on CC-BY-SA 4.0, Twemoji (archery, equestrian, golf, shooting) on MIT, EmojiOne 2 (badminton, basketball, cricket, field_hockey, gymnastics, ice_hockey, ice_skating, rugby, soccer, table_tennis, tennis, volleyball) on CC-BY-4.0 license, gaelic_games (EmojiOne 2 and Tobias Zwick on CC-BY-4.0)"}

    manually_licenced << {licence: "CC-BY-SA-4.0", file: "#{root_folder}/res/religion_icons.svg", author: "buddhist, christian, christian, confucian, taoism, caodai on CC-BY-SA-4.0 by Tobias Zwick, jain symbol on PD by Amakukha, sikh symbol on PD, star of David on PD by Zscout370 Islam symbol on PD, bahai star on PD, toii on CC-BY-SA-3.0 by MesserWoland, chinese folk religion symbol on PD by Aethelwolf Emsworth"}

    manually_licenced << {licence: "CC-BY-SA-4.0", file: "#{root_folder}/res/building_icons.svg", author: "train_station, transportation, car, carport, garage, garages, houseboat, parking by Tobias Zwick based on Twemoji (MIT), stadium is from EmojiOne 2, other icons are by Tobias Zwick"}    

    manually_licenced << {licence: "CC-BY-SA-4.0", file: "#{root_folder}/res/quest_icons.svg", author: "baby, beer, blind_bus, bus_shelter, bus_stop_name, car, car_wash, car_charger, ferry, ferry_pedestrian, recycling, restaurant, restaurant_vegan, restaurant_vegetarian, money, parking_access, parking_fee, Twemoji (MIT) derivative work, made by Tobias Zwick. sport icon is EmojiOne 2 (CC-BY 4 license) derivative work. All other made by Tobias Zwick (apple, bench, bicycle, bicycle_parking, bicycle_parking_capacity, bicycle_parking_cover, bicycleway, bicycleway_surface, bicycleway_width, blind, blind_pedestrian_crossing, blind_traffic_lights, bridge, building, building_construction, building_height, building_levels, building_underground, clock, fee, fire_hydrant, fuel, guidepost, housenumber, housenumber_street, label, lantern, leaf, mail, max_width, max_height, max_speed, max_weight, museum, note_create, notes, oneway, parking, parking_maxstay, pedestrian, pedestrian_crossing, pedestrian_segregated, phone, power, quest, railway, road_construction, roof_shape, religion, smoke, steps, street, street_lanes, street_name, street_surface, street_surface_paved_detail, street_turn_lanes, street_width, toilets, toilets_fee, toilets_wheelchair, tractor, traffic_lights, way_surface, way_width, wifi, wheelchair, wheelchair_shop)"}

    manually_licenced << {licence: "CC-BY-SA-4.0", file: "#{root_folder}/res/recycling_icons.svg", author: "Tobias Zwick: paper_only, paper_and_cartons, plastic, plastic_bottles, batteries, cans, carton, glass_bottles, lego, tetra, Adrien Pavie, modified from EmojiOne 2: clothes, EmojiOne 2: small_electrical_appliances, EmojiOne 2 and Tobias Zwick: newspaper, shoes; Twemoji and Tobias Zwick: green_waste"}    

    manually_licenced << {licence: "CC-BY-SA-4.0", file: "#{root_folder}/res/sport_icons.svg", author: "Tobias Zwick: american_football, athletics, australian_football, baseball, boules, bowls, canadian_football, handball, ice_stock, racquetball, sepak_takraw, paddle_tennis, skateboard, softball, skating; "}    

    manually_licenced << {licence: "CC-BY-SA-2.5", file: "#{root_folder}/metadata/en/images/featureGraphic.png", author: "Maximilian Dörrbecker (photo), Tobias Zwick (icons)"}
    manually_licenced << {licence: "CC-BY-SA-2.5", file: "#{root_folder}/res/feature_graphic.xcf", author: "Maximilian Dörrbecker (photo), Tobias Zwick (icons)"}
    

    gradle_files.each do |filepath|
        manually_licenced << {licence: "Apache-2.0", file: filepath, author: "Gradle project authors"}
    end

    app_icon_files.each do |filepath|
        manually_licenced << {licence: "GPL-2.0-only", file: filepath, author: "Elegant Themes"}
    end

    manually_licenced << {licence: "CC0-1.0", file: "#{root_folder}/res/railway_crossing_double_half.svg", author: "ServusWorld"}
    manually_licenced << {licence: "CC0-1.0", file: "#{root_folder}/res/railway_crossing_full.svg", author: "ServusWorld"}
    manually_licenced << {licence: "CC0-1.0", file: "#{root_folder}/res/railway_crossing_half.svg", author: "ServusWorld"}
    manually_licenced << {licence: "CC0-1.0", file: "#{root_folder}/res/railway_crossing_none.svg", author: "ServusWorld"}

    manually_licenced << {licence: "ODbL-1.0", file: "#{root_folder}/app/src/main/assets/boundaries.ser", author: "OpenStreetMap contributors"}

    Dir["#{root_folder}/app/src/main/assets/map_theme/fonts/*"].each do |file|
        puts file
        manually_licenced << {licence: "OFL-1.1", file: file, author: "The Montserrat Project Authors"}
    end

    Dir["#{root_folder}/app/src/main/assets/osmfeatures/*.json"].each do |file|
        puts file
        manually_licenced << {licence: "ISC", file: file, author: "The iD Project Authors"}
    end

    made_by_main_author.each do |filepath|
        manually_licenced << {licence: "GPL-3.0-only", file: filepath, author: "Tobias Zwick"}
    end
    return manually_licenced
end

def add_licence_metadata(author, licence, filepath)
    puts "#{filepath} is not existing" if !File.file?(filepath)
    if filepath =~ /\.(json|txt|md|bat|properties|editorconfig|gitignore|gitattributes|gradle|svg|png|jpg|xcf|pro|MockMaker)$/ || filepath.include?(".") == false
        # .gradle handling requested in https://github.com/fsfe/reuse-tool/issues/136
        
        # automatically handling git config files and more (editorconfig, gitignore, gitattributes)
        # is requested in https://github.com/fsfe/reuse-tool/issues/135
        
        # comment form support for .bat files requested in https://github.com/fsfe/reuse-tool/issues/118
        add_licence_metadata_in_a_separate_file(author, licence, filepath)
    elsif filepath =~ /\.(java|kt|py|xml|html)/
        execute_command("reuse addheader --copyright=\"#{author}\" --license=\"#{licence}\" \"#{filepath}\"")
    else
        puts "unrecognised filetype for #{filepath}"
        add_licence_metadata_in_a_separate_file(author, licence, filepath)
    end
end

def add_licence_metadata_in_a_separate_file(author, licence, filepath)
    execute_command("reuse addheader --explicit-license --copyright=\"#{author}\" --license=\"#{licence}\" \"#{filepath}\"")
end

def execute_command(command)
    stdout, stderr, status = Open3.capture3(command)
    if !status.success?
        puts "command failed!"
        puts command
        puts stdout
        puts stderr
    end
end

def main()
    gitignore_rules = File.open("#{root_folder}/.gitignore").read.split("\n")

    licence_translation = {
        "CC0" => "CC0-1.0",
    }

    filenames_processed_by_manually_licenced_files = []
    manually_licenced_files.each do |entry|
        licence = entry[:licence]
        if ["pd", "public domain"].include?(licence.downcase)
            licence = "CC0-1.0" # https://github.com/fsfe/reuse-docs/issues/46
        end
        licence = licence.gsub(" ", "-")
        if licence_translation.include?(licence)
            licence = licence_translation[licence]
        end
        add_licence_metadata(entry[:author], licence, entry[:file])
        filenames_processed_by_manually_licenced_files <<  entry["file"]
    end

    execute_command("rm \"#{root_folder}/COPYING\"")
    execute_command("rm \"#{root_folder}/app/src/main/res/authors.txt\"")
    execute_command("rm \"#{root_folder}/app/src/main/assets/map_theme/fonts/Apache License.txt\"")
    Dir.glob("#{root_folder}/**/*").reject{ |e| File.directory? e }.each do |file|
        if file =~ /\.license$/
            next
        end
        if pd_simple_files.include?(file)
            author = "noone, file too simple to be covered by copyright"
            licence = "CC0-1.0"
            filepath = file
            add_licence_metadata(author, licence, filepath)
            next
        end
        for rule in gitignore_rules
            if File.fnmatch(rule, file.delete_prefix(root_folder))
                "#{file} skipped as matching gitignore rules"
                next
            end
        end
        if the_same_authorship.include?(file) || file =~ /\.(kt|java|yaml|yml|xml|json|txt|md)$/
            add_licence_metadata("Tobias Zwick and contributors", "GPL-3.0-only", file)
        else
            if filenames_processed_by_manually_licenced_files.include?(file)
                puts "unhandled file #{file}"
            end
        end
    end
end

main