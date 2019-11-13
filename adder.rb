require_relative "licence_data"
# TODOs
# reuse addheader: error: '/home/mateusz/Documents/Archiwum/StreetComplete-ngi-zero/gradle/wrapper/gradle-wrapper.properties' does not have a recognised file extension, please use --style
# reuse addheader: error: '/home/mateusz/Documents/Archiwum/StreetComplete-ngi-zero/build.gradle' does not have a recognised file extension, please use --style
# reuse addheader: error: '/home/mateusz/Documents/Archiwum/StreetComplete-ngi-zero/tools/view_test_db.bat' does not have a recognised file extension, please use --style
# reuse addheader: error: '/home/mateusz/Documents/Archiwum/StreetComplete-ngi-zero/tools/view_db.bat' does not have a recognised file extension, please use --style
# reuse addheader: error: '/home/mateusz/Documents/Archiwum/StreetComplete-ngi-zero/settings.gradle' does not have a recognised file extension, please use --style
# reuse addheader: error: '/home/mateusz/Documents/Archiwum/StreetComplete-ngi-zero/gradlew.bat' does not have a recognised file extension, please use --style
#handle /home/mateusz/Documents/Archiwum/StreetComplete-ngi-zero/app/src/main/assets/map_theme/fonts

root_folder = "/home/mateusz/Documents/Archiwum/StreetComplete-ngi-zero"
pd_simple = ["#{root_folder}/app/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker",
             "#{root_folder}/app/proguard-rules.pro",
             "#{root_folder}/gradle.properties"
            ]

gitignore_rules = File.open("#{root_folder}/.gitignore").read.split("\n")

licence_data().each do |entry|
    `reuse addheader --copyright="#{entry[:author]}" --license=#{entry[:license]} "#{entry[:file]}"`
end

`rm "#{root_folder}/COPYING"`
`rm "#{root_folder}/app/src/main/assets/map_theme/fonts/Apache License.txt"`
Dir.glob('#{root_folder}/**/*').reject{ |e| File.directory? e }.each do |file|
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