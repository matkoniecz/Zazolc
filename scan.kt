import java.io.File
import java.io.InputStream


fun main(args: Array<String>) {
    val firstLocation = "/home/mateusz/Documents/StreetComplete/res/authors.txt" // some noxious to process
    val secondLocation = "/home/mateusz/Documents/StreetComplete/app/src/main/assets/authors.txt" //bunch of special cases
    val thirdLocation = "/home/mateusz/Documents/StreetComplete/app/src/main/res/authors.txt"
    val inputStream: InputStream = File(thirdLocation).inputStream()
    val inputString = inputStream.bufferedReader().use { it.readText() }
    for(line in inputString.split("\n").drop(3)) { // remove header lines
        if(line.length == 0) {
            continue
        }
        var licenceFound: String? = null
        for(licence in arrayOf("Public Domain", "CC0", "CC-BY-SA 1.0", "CC-BY-SA 2.0", "CC-BY-SA 2.5", "CC-BY-SA 3.0", "CC-BY-SA 4.0", "CC-BY 2.0", "CC-BY 3.0", "CC-BY 4.0")) {
            val splitted = line.split(licence)
            if(splitted.size == 2) {
                println(line)
                println(licence)
                println(splitted[0].trim())
                println(splitted[1].trim())
                licenceFound = licence
            }
        }
        if (licenceFound == null) {
                println("-------------------------")
                println(line)
                println(line)
                println(line)
                println(line)
                println(line)
        }
    }
} 
