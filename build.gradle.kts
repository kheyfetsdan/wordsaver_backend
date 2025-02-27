plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.wordsaver"
version = "0.0.1"

application {
    mainClass.set("com.wordsaver.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.ktor.server.cio)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation("com.h2database:h2:2.2.224")
    //implementation("org.jetbrains.exposed:exposed-dao")
    implementation("io.ktor:ktor-server-auth:3.0.3")
    implementation("io.ktor:ktor-server-auth-jwt:3.0.3")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("org.jetbrains.exposed:exposed-java-time:${libs.versions.exposed.get()}")
    implementation("io.ktor:ktor-server-status-pages:3.0.3")
}
