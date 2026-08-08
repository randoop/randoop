/* build file for replacecall agent */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("java")
  id("jacoco")
}

description = "replacecall agent"

sourceSets {
  create("agentTest")
}

configurations {
  named("implementation") {
    extendsFrom(configurations["plumelib"])
  }
  named("agentTestImplementation") {
    extendsFrom(configurations["testImplementation"])
  }
}

dependencies {
  testImplementation(libs.junit)
  testImplementation(libs.hamcrest.all)
  "agentTestImplementation"(project(path = ":", configuration = "testInput"))
  "agentTestImplementation"(project(path = ":", configuration = "fatJar"))
}

tasks.javadoc {
  // Add -Werror once Javadoc warnings are resolved.
  (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all", "-quiet")
}

/*
 * Jar needs to be executable as a javaagent.
 */
tasks.jar {
  manifest {
    attributes(
      mapOf(
        "Premain-Class" to "randoop.instrument.ReplaceCallAgent",
        "Can-Redefine-Classes" to "true",
      )
    )
  }
}

tasks.test {
  testLogging {
    events("started", "passed")
    showStandardStreams = true
    exceptionFormat = TestExceptionFormat.FULL
  }
}

tasks.named("compileAgentTestJava") {
  dependsOn(":copyJars")
}

val replacecallAgentTest =
  tasks.register<Test>("replacecallAgentTest") {
    description = "Run replace-call tests"

    workingDir = sourceSets["agentTest"].output.resourcesDir!!
    testClassesDirs = sourceSets["agentTest"].output.classesDirs
    classpath =
      sourceSets["agentTest"].runtimeClasspath +
        files("${layout.buildDirectory.get()}/libs/replacecall-$version.jar")
    // use the replacecall agent using the exclusions file from agentTest/resources
    jvmArgs(
      "-javaagent:${layout.buildDirectory.get()}/libs/replacecall-$version.jar=--dont-transform=replacecall-exclusions.txt"
    )
    jvmArgs("-Xbootclasspath/a:${layout.buildDirectory.get()}/libs/replacecall-$version.jar")
    testLogging {
      showStandardStreams = true
      exceptionFormat = TestExceptionFormat.FULL
    }

    // Turn off HTML reports -- handled by testReport task.
    reports.html.required = false
  }

tasks.named<JacocoReport>("jacocoTestReport") { dependsOn(replacecallAgentTest) }

tasks.check { dependsOn(replacecallAgentTest) }

tasks.named<ShadowJar>("shadowJar") {
  // We want the jar to be named replacecall-version.jar
  archiveClassifier.set(null as String?)

  relocate("com.google", "replacecall.com.google")
  relocate("com.jcraft.jsch", "replacecall.com.jcraft.jsch")
  relocate("com.sun.javadoc", "replacecall.com.sun.javadoc")
  relocate("com.sun.jna", "replacecall.com.sun.jna")
  relocate("com.trilead.ssh2", "replacecall.com.trilead.ssh2")
  relocate("de.regnis.q.sequence", "replacecall.de.regnis.q.sequence")
  relocate("net.fortuna.ical4j", "replacecall.net.fortuna.ical4j")
  relocate("nu.xom", "replacecall.nu.xom")
  relocate("org.antlr", "replacecall.org.antlr")
  relocate("org.apache", "replacecall.org.apache")
  relocate("org.ccil.cowan.tagsoup", "replacecall.org.ccil.cowan.tagsoup")
  relocate("org.checkerframework", "replacecall.org.checkerframework")
  relocate("org.ini4j", "replacecall.org.ini4j")
  relocate("org.junit", "replacecall.org.junit")
  relocate("org.slf4j", "replacecall.org.slf4j")
  relocate("org.tigris.subversion", "replacecall.org.tigris.subversion")
  relocate("org.tmatesoft", "replacecall.org.tmatesoft")
}

apply(from = rootProject.file("gradle-mvn-push.gradle.kts"))

fun replacecallPom(publication: MavenPublication) {
  // Don't use publication.from(components.java) which would publish the skinny jar as randoop.jar.
  // Information that is in all pom files is configured in randoop/gradle-mvn-push.gradle.kts.
  publication.pom {
    name = "Randoop's replacecall agent"
    description = "Replaces certain method calls by others at run time"
  }
}

configure<PublishingExtension> {
  publications {
    named<MavenPublication>("remote") {
      replacecallPom(this)
      artifact(tasks.named<ShadowJar>("shadowJar"))
      artifact(tasks["javadocJar"])
      artifact(tasks["sourcesJar"])
    }

    named<MavenPublication>("local") {
      replacecallPom(this)
      artifact(tasks.named<ShadowJar>("shadowJar"))
      artifact(tasks["javadocJar"])
      artifact(tasks["sourcesJar"])
    }
  }
}
