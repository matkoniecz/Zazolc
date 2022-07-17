import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStream
import kotlin.system.exitProcess

open class UpdateTaginfoListingTask : DefaultTask() {
    // ./gradlew updateTaginfoListing
    @TaskAction fun run() {
        println("Taginfo!!!!")
    }
}
