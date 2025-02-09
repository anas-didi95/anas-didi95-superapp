import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("com.diffplug.spotless") version "7.0.2"
}

group = "com.anasdidi"

version = "1.0.0-SNAPSHOT"

repositories { mavenCentral() }

val vertxVersion = "4.5.12"
val junitJupiterVersion = "5.9.1"
val log4j2Version = "2.12.4"
val jacksonVersion = "2.18.2"

val mainVerticleName = "com.anasdidi.superapp.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application { mainClass.set(launcherClassName) }

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")

  implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
  implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest { attributes(mapOf("Main-Verticle" to mainVerticleName)) }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging { events = setOf(PASSED, SKIPPED, FAILED) }
}

tasks.withType<JavaExec> {
  args =
      listOf(
          "run",
          mainVerticleName,
          "--redeploy=$watchForChange",
          "--launcher-class=$launcherClassName",
          "--on-redeploy=$doOnChange")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  format("misc") {
    target(".gitattributes", ".gitignore")
    trimTrailingWhitespace()
    leadingTabsToSpaces(2)
    endWithNewline()
  }
  java {
    importOrder()
    removeUnusedImports()
    cleanthat()
    googleJavaFormat()
    formatAnnotations()
    licenseHeader("/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */")
  }
  groovyGradle {
    target("*.gradle") // default target of groovyGradle
    greclipse()
  }
  kotlinGradle {
    target("*.gradle.kts") // default target for kotlinGradle
    ktfmt()
    // ktlint() // or ktfmt() or prettier()
  }
}
