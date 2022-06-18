@file:JvmName("MyScript")
@file:CompilerOptions("-jvm-target", "11")
@file:Repository("https://repo.maven.apache.org/maven2")
// @file:DependsOn("com.example:library:1.2.3")

@file:Import("DetectMissingImageCredits.kt")

import org.gradle.api.DefaultTask
import java.io.File
import java.io.InputStream
/*
To check whether StreetComplete authorship file contains all required credit run:

`kotlinc DetectMissingImageCreditsTask.kt -include-runtime -d out.jar` compiles Kotlin script

`java -jar out.jar` run compiled script
 */
class DetectMissingImageCreditsTask : DefaultTask() {
}
