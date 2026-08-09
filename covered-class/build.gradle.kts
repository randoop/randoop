// this is a Java project

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("java")
  id("jacoco")
}

description = "covered-class instrumentation agent"

configurations {
  create("javassist")
  named("implementation") {
    extendsFrom(configurations["javassist"])
    extendsFrom(configurations["plumelib"])
  }
}

dependencies {
  "javassist"("org.javassist:javassist:3.+")
  implementation(project(path = ":", configuration = "shadow"))
  implementation(project(path = ":"))
}

tasks.javadoc {
  (options as StandardJavadocDocletOptions).addStringOption("Xwerror", "-Xdoclint:all")
}

tasks.jar {
  manifest {
    attributes(
      mapOf(
        "Premain-Class" to "randoop.instrument.CoveredClassAgent",
        "Can-Redefine-Classes" to "true",
      )
    )
  }
}

tasks.named<ShadowJar>("shadowJar") {
  // Name the jar file covered-class-version.jar
  archiveClassifier.set(null as String?)

  exclude("**/pom*")

  relocate("com.github.javaparser", "coveredclass.org.github.javaparser")
  relocate("com.google.common", "coveredclass.com.google.common")
  relocate("com.google.gson", "coveredclass.com.google.gson")
  relocate("com.google.thirdparty", "coveredclass.com.google.thirdparty")
  relocate("com.jcraft.jsch", "coveredclass.com.jcraft.jsch")
  relocate("com.sun.javadoc", "coveredclass.com.sun.javadoc")
  relocate("com.sun.jna", "coveredclass.com.sun.jna")
  relocate("com.trilead.ssh2", "coveredclass.com.trilead.ssh2")
  relocate("de.regnis.q.sequence", "coveredclass.de.regnis.q.sequence")
  relocate("javassist", "coveredclass.javassist")
  relocate("net.fortuna.ical4j", "coveredclass.net.fortuna.ical4j")
  relocate("nu.xom", "coveredclass.nu.xom")
  relocate("org.antlr", "coveredclass.org.antlr")
  relocate("org.apache", "coveredclass.org.apache")
  relocate("org.ccil.cowan.tagsoup", "coveredclass.org.ccil.cowan.tagsoup")
  relocate("org.checkerframework", "coveredclass.org.checkerframework")
  relocate("org.ini4j", "coveredclass.org.ini4j")
  relocate("org.slf4j", "coveredclass.org.slf4j")
  relocate("org.tigris.subversion", "coveredclass.org.tigris.subversion")
  relocate("org.tmatesoft", "coveredclass.org.tmatesoft")
}

tasks.compileTestJava {
  dependsOn(
    ":shadowJar",
    ":copyJars",
  )
}

tasks.test {
  jvmArgs("-javaagent:${layout.buildDirectory.get()}/libs/covered-class-$version.jar")

  // Show as much as possible to console.
  testLogging {
    showStandardStreams = true
    exceptionFormat = TestExceptionFormat.FULL
  }

  // Turn off HTML reports -- handled by testReport task.
  reports.html.required = false

  doFirst {
    // Set the working directory for JUnit tests to the resources directory
    // instead of the project directory.
    workingDir = sourceSets["test"].output.resourcesDir!!
  }
}

apply(from = rootProject.file("gradle-mvn-push.gradle.kts"))

fun coveredClassPom(publication: MavenPublication) {
  // Don't use publication.from(components.java) which would publish the skinny jar as randoop.jar.
  // Information that is in all pom files is configured in randoop/gradle-mvn-push.gradle.kts.
  publication.pom {
    name = "Randoop's covered-class agent"
    description = "Requires Randoop's tests to execute (cover) certain classes"
  }
}

configure<PublishingExtension> {
  publications {
    named<MavenPublication>("remote") {
      coveredClassPom(this)
      artifact(tasks.named<ShadowJar>("shadowJar"))
      artifact(tasks["javadocJar"])
      artifact(tasks["sourcesJar"])
    }

    named<MavenPublication>("local") {
      coveredClassPom(this)
      artifact(tasks.named<ShadowJar>("shadowJar"))
      artifact(tasks["javadocJar"])
      artifact(tasks["sourcesJar"])
    }
  }
}
