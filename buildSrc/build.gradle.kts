/*
 * Build file for the build logic (custom task classes) in buildSrc/src/main/kotlin/.
 *
 * Without this file, Gradle would apply its default buildSrc configuration, which compiles Groovy
 * rather than Kotlin.  The `kotlin-dsl` plugin uses the Kotlin version that ships with Gradle, so no
 * Kotlin version needs to be chosen or updated here.  It also configures Gradle's `Action` interface
 * to be treated as a lambda-with-receiver, so that `execOperations.exec { ... }` blocks read the
 * same way here as in the build scripts.
 */

plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
}
