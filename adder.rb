require_relative "licence_data"

def root_folder
    return "/home/mateusz/Documents/Archiwum/StreetComplete-ngi-zero"
end

def pd_simple_files
    return ["#{root_folder}/app/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker",
        "#{root_folder}/app/proguard-rules.pro",
        "#{root_folder}/build.gradle",
        "#{root_folder}/settings.gradle",
        "#{root_folder}/.gitattributes",
        "#{root_folder}/.editorconfig",
        "#{root_folder}/.gitignore",
        "#{root_folder}/res/maxheight_sign.svg",
        "#{root_folder}/res/ic_axleload.svg",
        "#{root_folder}/res/ic_bogieweight.svg",
        "#{root_folder}/res/ic_create_note_black_24dp.svg",
        "#{root_folder}/res/ic_truck.svg",
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
        "#{root_folder}/res/building_levels_illustration.svg",
        "#{root_folder}/res/ic_building_levels_illustration.svg",
        "#{root_folder}/res/cycleways.svg",
        "#{root_folder}/res/cycleway_segregation.svg",
        "#{root_folder}/res/osm_anon_avatar.svg",
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
    ]
end

def manually_licenced_files
    manually_licenced = licence_data()
    manually_licenced << {licence: "CC-BY-SA-4.0", file: "#{root_folder}/res/sport_icons.svg", author: "Tobias Zwick (american_football, athletics, australian_football, baseball, boules, bowls, canadian_football, handball, ice_stock, racquetball, paddle_tennis, sepak_takraw, skateboard, softball, skating) on CC-BY-SA 4.0, Twemoji (archery, equestrian, golf, shooting) on MIT, EmojiOne 2 (badminton, basketball, cricket, field_hockey, gymnastics, ice_hockey, ice_skating, rugby, soccer, table_tennis, tennis, volleyball) on CC-BY-4.0 license, gaelic_games (EmojiOne 2 and Tobias Zwick)"}
    manually_licenced << {licence: "CC-BY-SA-4.0", file: "#{root_folder}/res/building_icons.svg", author: "Tobias Zwick, Twemoji, EmojiOne 2"}

    manually_licenced << {licence: "CC-BY-SA-2.5", file: "#{root_folder}/metadata/en/images/featureGraphic.png", author: "Maximilian Dörrbecker (photo), Tobias Zwick (icons)"}
    manually_licenced << {licence: "CC-BY-SA-2.5", file: "#{root_folder}/res/feature_graphic.xcf", author: "Maximilian Dörrbecker (photo), Tobias Zwick (icons)"}
    

    gradle_files.each do |filepath|
        manually_licenced << {licence: "Apache-2.0", file: filepath, author: "Gradle project authors"}
    end

    app_icon_files.each do |filepath|
        manually_licenced << {licence: "GPL-2.0", file: filepath, author: "Elegant Themes"}
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
    if filepath =~ /\.(json|txt|md|bat|properties)$/
        # comment form support for .bat files requested in https://github.com/fsfe/reuse-tool/issues/118
        puts `reuse addheader --explicit-license --copyright="#{author}" --license="#{licence}" "#{filepath}"`
    else
        puts `reuse addheader --copyright="#{author}" --license="#{licence}" "#{filepath}"`
    end
end

def main()
    the_same_authorship = [
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
        "#{root_folder}/metadata/en/images/phoneScreenshots/screenshot08.png"
    ]
    gitignore_rules = File.open("#{root_folder}/.gitignore").read.split("\n")

    licence_translation = {
        "CC0" => "CC0-1.0",
    }

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
    end

    `rm "#{root_folder}/COPYING"`
    `rm "#{root_folder}/app/src/main/res/authors.txt"`
    `rm "#{root_folder}/app/src/main/assets/map_theme/fonts/Apache License.txt"`
    Dir.glob("#{root_folder}/**/*").reject{ |e| File.directory? e }.each do |file|
        if file =~ /\.license$/
            next
        end
        if pd_simple_files.include?(file)
            `reuse addheader --copyright="noone, file too simple to be covered by copyright" --license=CC0-1.0 --explicit-license "#{file}"`
            next
        end
        for rule in gitignore_rules
            if File.fnmatch(rule, file.delete_prefix(root_folder))
                puts "#{file} skipped as matching gitignore rules"
                next
            end
        end
        if the_same_authorship.include?(file) || file =~ /\.(kt|java|yaml|yml|xml|json|txt|md)$/
            add_licence_metadata("Tobias Zwick and contributors", "GPL-3.0-only", file)
        else
            puts "unhandled file #{file}"
        end
    end
end

main