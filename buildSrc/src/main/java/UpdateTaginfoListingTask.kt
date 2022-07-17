import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStream
import kotlin.system.exitProcess
import kotlinx.ast.common.AstSource

open class UpdateTaginfoListingTask : DefaultTask() {
    // ./gradlew updateTaginfoListing
    @TaskAction fun run() {
        println("Taginfo!!!!")
        val ast = AstSource.String("description", "val content = null")
    }
}
