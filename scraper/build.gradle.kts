plugins {
    kotlin("jvm") version "2.3.21"
    application
}

application {
    mainClass.set("com.sumup.scraper.PlacesScraperKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // SQLite & DB
    implementation("org.xerial:sqlite-jdbc:3.45.2.0")
    implementation("org.jetbrains.exposed:exposed-core:0.49.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.49.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.49.0")

    // HTTP Client (OkHttp is lightweight for simple scraping)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON Parsing
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Logging (Required by Exposed)
    implementation("org.slf4j:slf4j-simple:2.0.12")
}