import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.ByteArrayOutputStream
import java.util.Properties
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("com.diffplug.spotless") version "7.0.2"
}

group = "com.anasdidi"

version = "0.1.0"

repositories { mavenCentral() }

val vertxVersion = "4.5.12"
val junitJupiterVersion = "5.9.1"
val log4j2Version = "2.12.4"
val jacksonVersion = "2.18.2"
val liquibaseVersion = "4.31.0"
val h2Version = "2.1.210"

val mainVerticleName = "com.anasdidi.superapp.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application { mainClass.set(launcherClassName) }

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-web-openapi-router")
  implementation("io.vertx:vertx-config")
  implementation("io.vertx:vertx-config-yaml")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")

  implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
  implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
  implementation("org.liquibase:liquibase-core:$liquibaseVersion")
  implementation("com.h2database:h2:$h2Version")
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

val commitId: String by lazy {
  val stdout = ByteArrayOutputStream()
  rootProject.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
    standardOutput = stdout
  }
  stdout.toString().trim()
}

tasks.register("createProperties") {
  dependsOn(tasks.processResources)

  doLast {
    val versionFile = file("$buildDir/resources/main/version.properties")
    versionFile.parentFile.mkdirs() // Ensure that the parent directories exist

    versionFile.printWriter().use { writer ->
      val properties = Properties()
      properties["version"] = project.version.toString()
      properties["commitId"] = commitId
      properties.store(writer, null)
    }
  }
}

tasks.classes { dependsOn(tasks.named("createProperties")) }
