import java.io.File
import java.io.InputStream

class LicenceData(val licence: String, val file: String, val source: String) {}

fun validLicences() : Array<String> {
    return arrayOf("Public Domain", "CC0", "CC-BY-SA 1.0", "CC-BY-SA 2.0", "CC-BY-SA 2.5", "CC-BY-SA 3.0", "CC-BY-SA 4.0", "CC-BY 2.0", "CC-BY 3.0", "CC-BY 4.0")
}

fun main(args: Array<String>) {
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
} 
