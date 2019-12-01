require_relative "licence_data"

root_folder = "/home/mateusz/Documents/Archiwum/StreetComplete-ngi-zero"
pd_simple = ["#{root_folder}/app/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker",
             "#{root_folder}/app/proguard-rules.pro",
             "#{root_folder}/build.gradle",
             "#{root_folder}/settings.gradle",
             "#{root_folder}/.gitattributes",
             "#{root_folder}/.editorconfig"
            ]
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

manually_licenced = licence_data()
manually_licenced << {licence: "Apache-2.0", file: "#{root_folder}/gradle/wrapper/gradle-wrapper.jar", author: "Gradle project authors"}
manually_licenced << {licence: "Apache-2.0", file: "#{root_folder}/gradlew.bat", author: "Gradle project authors"}
manually_licenced << {licence: "Apache-2.0", file: "#{root_folder}/gradle.properties", author: "Gradle project authors"}
manually_licenced << {licence: "Apache-2.0", file: "#{root_folder}/gradle/wrapper/gradle-wrapper.properties", author: "Gradle project authors"}

manually_licenced << {licence: "ODbL-1.0", file: "#{root_folder}/app/src/main/assets/boundaries.ser", author: "OpenStreetMap contributors"}

Dir["#{root_folder}/app/src/main/assets/map_theme/fonts/*"].each do |file|
    puts file
    manually_licenced << {licence: "OFL-1.1", file: file, author: "The Montserrat Project Authors"}
end

Dir["#{root_folder}/app/src/main/assets/osmfeatures/*.json"].each do |file|
    puts file
    manually_licenced << {licence: "ISC", file: file, author: "The iD Project Authors"}
end

manually_licenced.each do |entry|
    licence = entry[:licence]
    if ["pd", "public domain"].include?(licence.downcase)
        licence = "CC0-1.0" # https://github.com/fsfe/reuse-docs/issues/46
    end
    licence = licence.gsub(" ", "-")
    if licence_translation.include?(licence)
        licence = licence_translation[licence]
    end
    if entry[:file] =~ /\.json$/
        puts `reuse addheader --explicit-license --copyright="#{entry[:author]}" --license="#{licence}" "#{entry[:file]}"`
    else
        puts `reuse addheader --copyright="#{entry[:author]}" --license="#{licence}" "#{entry[:file]}"`
    end
end

`rm "#{root_folder}/COPYING"`
`rm "#{root_folder}/app/src/main/res/authors.txt"`
`rm "#{root_folder}/app/src/main/assets/map_theme/fonts/Apache License.txt"`
Dir.glob("#{root_folder}/**/*").reject{ |e| File.directory? e }.each do |file|
    if file =~ /\.license$/
        next
    end
    if pd_simple.include?(file)
        `reuse addheader --copyright="noone, file too simple to be covered by copyright" --license=CC0 --explicit-license "#{file}"`
        next
    end
    for rule in gitignore_rules
        if File.fnmatch(rule, file.delete_prefix(root_folder))
            puts "#{file} skipped as matching gitignore rules"
            next
        end
    end
    explicit_extension_command = ""
    if file =~ /\.kt$/
        `reuse addheader --copyright="Tobias Zwick and contributors" --license=GPL-3.0-only --style c "#{file}"`
    elsif file =~ /\.java$/
        `reuse addheader --copyright="Tobias Zwick and contributors" --license=GPL-3.0-only --style c "#{file}"`
    elsif file =~ /\.(yml|yaml)$/
        `reuse addheader --copyright="Tobias Zwick and contributors" --license=GPL-3.0-only --style python "#{file}"`
    elsif file =~ /\.xml$/
        `reuse addheader --copyright="Tobias Zwick and contributors" --license=GPL-3.0-only --style html "#{file}"`
    elsif file =~ /\.json$/ # https://github.com/fsfe/reuse-tool/issues/116
        # json has no comments :(
        next # broken due to https://github.com/fsfe/reuse-tool/issues/113
        explicit_extension_command = "--explicit-license"
    elsif file =~ /\.txt$/ # https://github.com/fsfe/reuse-tool/issues/116
        next # broken due to https://github.com/fsfe/reuse-tool/issues/113
        explicit_extension_command = "--explicit-license"
    end
end