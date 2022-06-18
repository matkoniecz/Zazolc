@file:JvmName("MyScript")
@file:CompilerOptions("-jvm-target", "11")
@file:Repository("https://repo.maven.apache.org/maven2")
// @file:DependsOn("com.example:library:1.2.3")

@file:Import("DetectMissingImageCredits.kt")

System.out.println("out")
System.err.println("err")
