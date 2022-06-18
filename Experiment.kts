@file:JvmName("MyScript")
@file:CompilerOptions("-jvm-target", "11")
@file:Repository("https://repo.maven.apache.org/maven2")
// @file:DependsOn("com.example:library:1.2.3")

import java.io.File

val input = File("README.md") // Assuming you ran checkout before
val output = File("result.txt")
val readmeFirstLine = input.readLines().first()
output.writeText(readmeFirstLine)