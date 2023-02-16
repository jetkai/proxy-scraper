plugins {
    kotlin("jvm") version "1.8.0"
}

group = "scraper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    //Jackson - Serialize/Deserialize
    implementation("com.fasterxml.jackson.core:jackson-core:2.14.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")

    //Emulate Web Browser - Some proxy sites require JavaScript
    implementation("org.seleniumhq.selenium:selenium-firefox-driver:4.8.0")

    //Reflection for Plugins
    implementation("org.reflections:reflections:0.10.2")

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    //Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.5")
}

tasks.test {
    useJUnitPlatform()
}

lateinit var jarFile: File

tasks.withType<Jar> {
    archiveFileName.set("proxy-scraper.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    // Otherwise you'll get a "No main manifest attribute" error
    manifest {
        attributes["Main-Class"] = "scraper/Main"
    }

    // To add all of the dependencies otherwise a "NoClassDefFoundError" error
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    jarFile = archiveFile.get().asFile
}

kotlin {
    jvmToolchain(18)
}