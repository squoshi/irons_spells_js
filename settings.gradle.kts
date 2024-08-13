plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

val modId: String by extra
val minecraftVersion: String by extra

rootProject.name = "$modId-$minecraftVersion"
