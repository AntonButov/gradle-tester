plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
}

group = "gradle-tester"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit)
    implementation(gradleTestKit())
    testImplementation(kotlin("test"))
}
tasks.named("build") {
    dependsOn("ktlintFormat")
}

tasks.named("test") {
    dependsOn("ktlintFormat")
}

ktlint {
    debug.set(false)
    android.set(false)
    ignoreFailures.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
    }
}

tasks.test {
    useJUnitPlatform()
}
