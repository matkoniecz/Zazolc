import java.io.File
import java.io.InputStream

class LicenceData(val licence: String, val file: String, val source: String) {}

class MediaFile(val filePath: File) {}

class LicencedFile(val licence: LicenceData, val file: MediaFile)

fun validLicences() : Array<String> {
    return arrayOf("Public Domain", "CC0", "CC-BY-SA 1.0", "CC-BY-SA 2.0", "CC-BY-SA 2.5", "CC-BY-SA 3.0", "CC-BY-SA 4.0", "CC-BY 2.0", "CC-BY 3.0", "CC-BY 4.0")
}

fun publicDomainAsSimpleShapesFilenames() : Array<String> {
    return arrayOf("speech_bubble_top.9.png", "speech_bubble_start.9.png",
                   "speech_bubble_end.9.png", "speech_bubble_none.9.png",
                   "crosshair_marker.png", "location_direction.png",
                   "building_levels_illustration_bg_left.png",
                   "building_levels_illustration_bg_right.png",
                   "background_housenumber_frame_slovak.9")
}

fun cutoff() : Int {
    return 28
}

fun licencedMedia() : MutableList<LicenceData> {
    val firstLocation = "/home/mateusz/Documents/StreetComplete/res/authors.txt" // some noxious to process
    val secondLocation = "/home/mateusz/Documents/StreetComplete/app/src/main/assets/authors.txt" //bunch of special cases
    val thirdLocation = "/home/mateusz/Documents/StreetComplete/app/src/main/res/authors.txt"
    val inputStream: InputStream = File(thirdLocation).inputStream()
    val inputString = inputStream.bufferedReader().use { it.readText() }
    val knownLicenced = mutableListOf<LicenceData>()
    for(line in inputString.split("\n").drop(3)) { // remove header lines
        if(line.length == 0) {
            continue
        }
        var licenceFound: String? = null
        for(licence in validLicences()) {
            val splitted = line.split(licence)
            if(splitted.size == 2) {
                val file = splitted[0].trim()
                val source = splitted[1].trim()
                licenceFound = licence
                knownLicenced += LicenceData(licence, file, source)
            }
        }
        if (licenceFound == null) {
                throw Exception("unexpected licence in the input file was encountered, on " + line)
        }
    }
    return knownLicenced
}

fun mediaNeedingLicenes() : MutableList<MediaFile> {
    val mediaFiles = mutableListOf<MediaFile>()
    File("/home/mateusz/Documents/StreetComplete/app/src/main/res").walkTopDown().forEach {
        if(it.getName().contains(".")) {
            val split_extension = it.getName().split(".")
            if(split_extension.size > 1) {
                var extension = split_extension.last()
                if(extension !in listOf("yaml", "yml", "xml", "txt")) {
                    mediaFiles += MediaFile(it)
                }
            }
        }
    }
    return mediaFiles
}
fun truncatedfileMatchesLicenceDeclaration(fileName : String, licencedFile : String, truncationLength: Int): Boolean {
    var truncatedFile = fileName
    if(truncatedFile.length >= truncationLength ) {
        truncatedFile = truncatedFile.substring(0, truncationLength)
    }
    var truncatedLicence = licencedFile
    if(truncatedLicence.length >= truncationLength ) {
        truncatedLicence = truncatedLicence.substring(0, truncationLength)
    }
    return truncatedFile.equals(truncatedLicence)
}

fun fileMatchesLicenceDeclaration(fileName : String, licencedFile : String): Boolean {
    for(delta in listOf(0, 1, 2)) {
        if (truncatedfileMatchesLicenceDeclaration(fileName, licencedFile, cutoff() - delta) ) {
            return true
        }
    }
    return false
}

fun main(args: Array<String>) {
    val knownLicenced = licencedMedia()
    val mediaFiles = mediaNeedingLicenes()
    val usedLicenced = mutableListOf<LicenceData>()
    val billOfMaterials = mutableListOf<LicencedFile>()
    
    for(file in mediaFiles) {
        var matched = false
        for(licenced in knownLicenced) {
            if(fileMatchesLicenceDeclaration(file.filePath.getName(), licenced.file)) {
                if(matched) {
                    println(file.filePath.toString() + " matched to " + licenced.file + " but was matched already!")
                } else {
                    billOfMaterials += LicencedFile(licenced, file)
                    matched = true
                    usedLicenced += licenced
                }
                //println(file.filePath.toString() + " matched to " + licenced.file)
            }
        }
        if(!matched){
            val name = file.filePath.getName()
            if(name !in publicDomainAsSimpleShapesFilenames()) {
                println(file.filePath.toString() + " remained unmatched")
                println(name + " remained unmatched")
                println()
            }
        }
    }
    for(licenced in knownLicenced) {
        if(licenced !in usedLicenced) {
            println(licenced.file + " appears to be unused")
        }
    }
    println("[")
    for(licenced in billOfMaterials) {
        println("{")
        println("file: \"" + licenced.file.filePath + "\",")
        println("licence: \"" + licenced.licence.licence + "\",")
        println("author: \"" + licenced.licence.source + "\",")
        println("},")
    }
    println("]")
} 
