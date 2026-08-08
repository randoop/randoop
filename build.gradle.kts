/*
 * build.gradle.kts for Randoop
 *
 * Quick instructions: in project directory run with command
 *   ./gradlew build
 */

import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.File
import java.util.concurrent.Callable
import net.ltgt.gradle.errorprone.errorprone
import org.aim42.htmlsanitycheck.HtmlSanityCheckTask
import org.apache.tools.ant.taskdefs.condition.Os
import org.checkerframework.plugin.gradle.CheckerFrameworkCompileExtension
import org.checkerframework.plugin.gradle.CheckerFrameworkExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  alias(libs.plugins.com.gradleup.shadow)
  id("java")
  id("eclipse")
  id("idea")

  alias(libs.plugins.com.diffplug.spotless) apply false

  alias(libs.plugins.net.ltgt.errorprone)

  alias(libs.plugins.org.checkerframework)

  alias(libs.plugins.org.aim42.htmlSanityCheck)

  // run: ./gradlew <taskname> taskTree
  alias(libs.plugins.com.dorongold.task.tree)

  // https://github.com/n0mer/gradle-git-properties ; target is: generateGitProperties
  alias(libs.plugins.com.gorylenko.gradle.git.properties)

  id("pmd")
}

val isJava11orHigher = JavaVersion.current() >= JavaVersion.VERSION_11
val isJava17orHigher = JavaVersion.current() >= JavaVersion.VERSION_17
val isJava21orHigher = JavaVersion.current() >= JavaVersion.VERSION_21

// The JDK for running tests: the jdkTestVersion property, or if not set, JavaVersion.current().
val testJdkVersionString =
  providers.gradleProperty("jdkTestVersion").getOrElse(JavaVersion.current().majorVersion)
val testJdkVersion = Integer.valueOf(testJdkVersionString)

/* Common build configuration for Randoop and agents */
allprojects {
  apply(plugin = "java")
  apply(plugin = "com.gradleup.shadow")
  apply(plugin = "net.ltgt.errorprone")
  apply(plugin = "jacoco")

  val javaToolchains = the<JavaToolchainService>()

  configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain {
      languageVersion = JavaLanguageVersion.of(testJdkVersion)
    }
  }
  tasks.withType<JavaExec>().configureEach {
    javaLauncher = javaToolchains.launcherFor {
      languageVersion = JavaLanguageVersion.of(testJdkVersion)
    }
  }

  tasks.withType<JavaCompile>().configureEach {
    javaCompiler = javaToolchains.compilerFor {
      languageVersion = JavaLanguageVersion.of(JavaVersion.current().majorVersion.toInt())
    }
  }

  repositories {
    mavenCentral()
    maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") }
    mavenLocal()
  }

  group = "com.github.randoop"
  /* Randoop version number - added to generated jar files */
  version = "4.3.4"

  configurations {
    // A collection of all plumelib dependencies required, so that all
    // sub-projects use the same versions.
    create("plumelib")
    create("testInput")
    create("fatJar")
  }

  dependencies {
    // https://mvnrepository.com/artifact/org.plumelib/bcel-util
    "plumelib"(rootProject.libs.bcel.util)
    // https://mvnrepository.com/artifact/org.plumelib/options
    if (JavaVersion.current() == JavaVersion.VERSION_1_8) {
      "plumelib"("org.plumelib:options:1.0.6")
    } else {
      "plumelib"(rootProject.libs.options)
    }
    // https://mvnrepository.com/artifact/org.plumelib/plume-util
    "plumelib"(rootProject.libs.plume.util)
    // https://mvnrepository.com/artifact/org.plumelib/reflection-util
    "plumelib"(rootProject.libs.reflection.util)
  }
  configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
  }

  apply(plugin = "org.checkerframework")
  configure<CheckerFrameworkExtension> {
    version = rootProject.libs.versions.checker.get()
    if (project.hasProperty("cfNullness")) {
      checkers =
        listOf(
          "org.checkerframework.checker.nullness.NullnessChecker",
          // These are included to avoid this failure:
          //   The following options were not recognized by any processor: "[permitStaticOwning]"
          "org.checkerframework.checker.regex.RegexChecker",
          "org.checkerframework.checker.resourceleak.ResourceLeakChecker",
          "org.checkerframework.checker.signedness.SignednessChecker",
          "org.checkerframework.checker.signature.SignatureChecker",
        )
    } else {
      checkers =
        listOf(
          // "org.checkerframework.checker.nullness.NullnessChecker",
          "org.checkerframework.checker.regex.RegexChecker",
          "org.checkerframework.checker.resourceleak.ResourceLeakChecker",
          "org.checkerframework.checker.signedness.SignednessChecker",
          "org.checkerframework.checker.signature.SignatureChecker",
        )
    }
    extraJavacArgs =
      listOf(
        // Uncomment -proc:none to disable all annotation processing and speed up the build.
        // "-proc:none",

        "-Werror",
        "-Awarns",
        "-Xmaxwarns",
        "10000",
        // "-AcheckPurityAnnotations",
        "-ArequirePrefixInWarningSuppressions",
        "-AwarnUnneededSuppressions",
        "-AwarnRedundantAnnotations",
        "-ApermitStaticOwning",
        // -processing: suppresses "No processor claimed any of these annotations ..."
        // -options: suppresses "target value 8 is obsolete and will be removed in a future release"
        "-Xlint:-processing,-options",
      )
  }

  configure<JacocoPluginExtension> {
    toolVersion = rootProject.libs.versions.jacoco.get()
  }

  tasks.named<JacocoReport>("jacocoTestReport") {
    group = "Report"
    dependsOn(tasks.named("test"))
    reports {
      xml.required = true
      html.required = true
    }
  }

  tasks.named("check") { dependsOn(tasks.named("jacocoTestReport")) }
} // // end allprojects

// ****** Configuration specific to Randoop and not agents. *****
// ****** Configuration for agent FOO appears in FOO/build.gradle.kts . *****

description = "Randoop automated test generation"

/* Root for working directories for system test generated files */
val workingDirectories = layout.buildDirectory.get().toString() + "/working-directories"

sourceSets {
  /* JUnit tests are run in nondeterministic order, but system tests are
  run in deterministic order. */
  /* system tests */
  create("systemTest") {
    resources {
      srcDir("src/testInput/resources")
    }
    output.dir(mapOf<String, Any>("builtBy" to "generateWorkingDirs"), workingDirectories)
  }

  /* Code sets used by system tests. There are no actual tests here. */
  create("testInput")

  named("test") {
    resources {
      srcDir("src/testInput/resources")
    }
  }
}

configurations {
  /*
   * Used to manage javaagent jar file
   */
  create("jacocoagent")

  create("junit")

  named("implementation") {
    extendsFrom(configurations["plumelib"], configurations["junit"])
  }

  named("systemTestImplementation") {
    extendsFrom(configurations["plumelib"], configurations["junit"])
  }

  named("testInputImplementation") {
    extendsFrom(configurations["plumelib"])
  }
}

dependencies {
  "junit"(libs.junit)

  implementation(project(":replacecall"))
  implementation(libs.javaparser.core)
  implementation(libs.gson)
  implementation(libs.commons.exec)
  implementation(libs.commons.lang3)
  implementation(libs.commons.io)

  compileOnly(libs.require.javadoc)

  /* Jacoco measures coverage of classes and methods under test. */
  implementation(libs.org.jacoco.core)
  implementation(variantOf(libs.org.jacoco.agent) { classifier("runtime") })

  /* sourceSet test uses JUnit and some use testInput source set */
  testImplementation(libs.commons.codec)
  testImplementation(libs.hamcrest.all)
  configurations["junit"].dependencies.forEach { testImplementation(it) }
  testImplementation(sourceSets["testInput"].output)

  "jacocoagent"(libs.org.jacoco.agent)

  /*
   * source set systemTest
   */
  "systemTestImplementation"(libs.org.jacoco.core)
  "systemTestImplementation"(libs.org.jacoco.report)
  "systemTestImplementation"(libs.commons.exec)
  "systemTestImplementation"(libs.commons.io)
  "systemTestImplementation"(libs.hamcrest.all)
  "systemTestRuntimeOnly"(sourceSets["testInput"].output)

  /*
   * sourceSet testInput depends on output of main.
   * Also, src/testInput/java/ps1/RatPolyTest.java uses JUnit
   */
  "testInputImplementation"(sourceSets["main"].output)
  configurations["junit"].dependencies.forEach { "testInputImplementation"(it) }

  errorprone(libs.error.prone.core)
}

/*
 * Configuration for compilation.
 */
tasks.withType<JavaCompile>().configureEach {
  if (name == "compileTestInputJava") {
    options.compilerArgs =
      mutableListOf(
        "-g",
        "-nowarn",
        "-Xlint:-classfile,-options",
      )
  } else {
    options.compilerArgs =
      mutableListOf(
        "-g",
        "-Werror",
        "-Xlint",
        "-Xlint:-classfile,-options",
      )
  }
}

val compileAll =
  tasks.register("compileAll") {
    dependsOn("compileJava")
    dependsOn("compileTestJava")
    dependsOn(":covered-class:compileJava")
    dependsOn(":covered-class:compileTestJava")
    dependsOn(":replacecall:compileJava")
    dependsOn(":replacecall:compileTestJava")
    dependsOn(":replacecall:compileAgentTestJava")
    dependsOn("compileSystemTestJava")
  }

// Get early notification of compilation failures, when running assemble.
tasks.assemble { mustRunAfter(compileAll) }

tasks.build { dependsOn(compileAll) }

allprojects {
  apply(plugin = "maven-publish")
  apply(plugin = "signing")
  if (isJava21orHigher) {
    apply(plugin = "com.diffplug.spotless")
    configure<SpotlessExtension> {
      format("misc") {
        // define the files to apply `misc` to
        target("*.md", ".gitignore")

        // define the steps to apply to those files
        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()
      }
      java {
        // Use fileTree per https://github.com/diffplug/spotless/issues/399
        targetExclude(fileTree("$projectDir/src/testInput") { include("**/*.java") })

        googleJavaFormat("1.32.0")
        // Add Modifiability Checker annotations.  TODO: move them into Spotless itself.
        formatAnnotations()
          .addTypeAnnotation("BottomGrow")
          .addTypeAnnotation("BottomReplace")
          .addTypeAnnotation("BottomShrink")
          .addTypeAnnotation("Growable")
          .addTypeAnnotation("IteratorPolyMod")
          .addTypeAnnotation("Modifiable")
          .addTypeAnnotation("PolyGrow")
          .addTypeAnnotation("PolyModifiable")
          .addTypeAnnotation("PolyReplace")
          .addTypeAnnotation("PolyShrink")
          .addTypeAnnotation("Replaceable")
          .addTypeAnnotation("Shrinkable")
          .addTypeAnnotation("Ungrowable")
          .addTypeAnnotation("MaybeGrow")
          .addTypeAnnotation("MaybeIteratorPolyMod")
          .addTypeAnnotation("MaybeModifiable")
          .addTypeAnnotation("MaybeReplace")
          .addTypeAnnotation("MaybeShrink")
          .addTypeAnnotation("Unmodifiable")
          .addTypeAnnotation("UnmodParam")
          .addTypeAnnotation("Unreplaceable")
          .addTypeAnnotation("Unshrinkable")
      }
    }
  }
}

// Format the build logic itself.  This is configured only on the root project: the root project's
// target patterns already reach the subprojects' and buildSrc's files, so configuring this within
// `allprojects` would make three tasks in three projects rewrite the same files.
if (isJava21orHigher) {
  configure<SpotlessExtension> {
    kotlinGradle {
      target("**/*.gradle.kts")
      ktfmt().googleStyle()
      leadingTabsToSpaces(2)
      trimTrailingWhitespace()
      // endWithNewline() // Don't want to end empty files with a newline
    }
    kotlin {
      // The custom task classes used by the build scripts.
      target("buildSrc/src/main/kotlin/**/*.kt")
      ktfmt().googleStyle()
      leadingTabsToSpaces(2)
      trimTrailingWhitespace()
    }
  }
}

// Error Prone linter

allprojects {
  dependencies {
    "errorprone"(rootProject.libs.error.prone.core)
  }
}

allprojects {
  tasks.withType<JavaCompile>().configureEach {
    if (
      name == "compileTestInputJava" || name == "compileTestJava" || name == "compileSystemTestJava"
    ) {
      options.errorprone.enabled = false
      (options as ExtensionAware)
        .extensions
        .getByType(CheckerFrameworkCompileExtension::class.java)
        .enabled = false
    } else {
      options.compilerArgs.addAll(listOf("-Xlint:all,-processing", "-Werror", "-Xlint:-options"))

      options.errorprone {
        enabled = isJava21orHigher
        // TODO: uncomment once we run the Interning Checker on Randoop.
        // disable("ReferenceEquality") // Use Interning Checker instead.
        disable("StringSplitter") // Obscure case isn't likely.
        disable("AnnotateFormatMethod") // Error Prone doesn't use Checker Framework @FormatMethod.
        // Removing "public" from private class members loses information about the abstraction.
        disable("EffectivelyPrivate")

        excludedPaths = ".*/testInput/.*"
      }
    }
  }
}

// allprojects {
//   apply(plugin = "pmd")
//   pmd {
//     toolVersion = "7.25.0"
//     consoleOutput = true
//     ruleSetFiles = files("$rootDir/.pmd-ruleset.xml")
//     ruleSets = listOf()
//   }
//   tasks.withType<Pmd>().configureEach {
//     // Exclude an entire generated folder path
//     exclude("**/mock/**")
//   }
// }

val testInputJar =
  tasks.register<Jar>("testInputJar") {
    from(sourceSets["testInput"].output)
    archiveBaseName = "testInput"
  }

artifacts {
  add("testInput", testInputJar)
  add("fatJar", tasks.shadowJar)
}

/*
 * Configuration for clean
 */
tasks.clean {
  dependsOn(":replacecall:clean")
  dependsOn(":covered-class:clean")
}

tasks.compileTestJava {
  dependsOn(":replacecall:shadowJar")
}

/*
 * Configuration of test task from Java plugin.
 * Runs all tests in test sourceSet except those excluded below.
 */
tasks.test {
  dependsOn(":replacecall:shadowJar")

  /*
   * Set the working directory for JUnit tests to the resources directory
   * instead of the project directory.
   */

  setWorkingDir(layout.buildDirectory.file("resources"))

  /*
   * Show as much as possible to console.
   */
  testLogging {
    events("started", "passed")
    showStandardStreams = true
    exceptionFormat = TestExceptionFormat.FULL
  }

  /* Turn off HTML reports -- handled by testReport task */
  reports.html.required = false

  /*
   * Temporary exclusion b/c script file uses generics as raw types and conflicts with
   * other uses of parsing.
   */
  exclude("**/randoop/test/SequenceTests.*")

  /*
   * Temporary exclusion b/c incomplete.
   */
  exclude("**/randoop/output/JUnitCreatorTest.*")

  /*
   * Problematic tests excluded during Gradle setup that need to be evaluated.
   * Unless otherwise noted, these are tests that were not previously run by
   * Makefile. However, some included tests were also not run, but are not
   * failing.
   */
  exclude("randoop/test/NonterminatingInputTest.*")
  exclude("randoop/test/EmptyTest.*")
  exclude("randoop/test/RandoopPerformanceTest.*") /* had target but not run */
  exclude("randoop/test/ForwardExplorerPerformanceTest.*")
  exclude("randoop/test/ForwardExplorerTests2.*") /* sporadic heap space issue */
  exclude("randoop/test/Test_SomeDuplicates.*")
  exclude("randoop/test/Test_SomePass.*")
  exclude("randoop/operation/OperationParserTests.*")
}

/*
 * Task to build the root directory of working directories used by the
 * JUnit tests in the systemTest task.
 * If the directory exists then cleans out the contents.
 */
tasks.register("generateWorkingDirs") {
  doLast {
    val generated = file(workingDirectories)
    if (!generated.exists()) {
      generated.mkdirs()
    } else {
      val workingFiles =
        fileTree(workingDirectories) {
          include("**/*.java")
          include("**/*.class")
          include("**/*.exec")
          include("**/*.txt")
        }
      delete(workingFiles)
    }
  }
}

/*
 * Extracts JaCoCo javaagent into build/jacocoagent
 */
val extractJacocoAgent =
  tasks.register<Copy>("extractJacocoAgent") {
    from(Callable { configurations["jacocoagent"].map { zipTree(it) } })
    into(layout.buildDirectory.dir("jacocoagent"))
  }

/** The JaCoCo javaagent jar file, which distributionZip includes. */
val jacocoAgentJar = extractJacocoAgent.map { it.destinationDir.resolve("jacocoagent.jar") }

/*
 * Runs JUnit over all classes in systemTest sourceSet.
 * JUnit tests assume that working directories can be found in the build directory.
 */
val systemTest =
  tasks.register<Test>("systemTest") {
    dependsOn(
      "extractJacocoAgent",
      "generateWorkingDirs",
      "assemble",
    )
    group = "Verification"
    description = "Run system tests"

    /*
     * Set system properties for jar paths, used by randoop.main.SystemTestEnvironment
     */
    doFirst {
      systemProperty("jar.randoop", tasks.named<ShadowJar>("shadowJar").get().archiveFile.get())
      systemProperty(
        "jar.replacecall.agent",
        project(":replacecall").tasks.named<ShadowJar>("shadowJar").get().archiveFile.get(),
      )
      systemProperty(
        "jar.covered.class.agent",
        project(":covered-class").tasks.named<ShadowJar>("shadowJar").get().archiveFile.get(),
      )
    }

    setWorkingDir(layout.buildDirectory)
    testClassesDirs = sourceSets["systemTest"].output.classesDirs
    classpath = sourceSets["systemTest"].runtimeClasspath

    /*
     * Show as much as possible to console.
     */
    testLogging {
      showStandardStreams = true
      exceptionFormat = TestExceptionFormat.FULL
    }

    /* Turn off HTML reports -- handled by testReport task */
    reports.html.required = false
  }

systemTest { dependsOn(tasks.shadowJar) }

/*
 * Link the systemTest task into the project check task.
 * Includes system tests into the project build task.
 */
tasks.check { dependsOn(systemTest) }

tasks.named<JacocoReport>("jacocoTestReport") { dependsOn(systemTest) }

tasks.withType<Test>().configureEach {
  /*
   * Set the destination directory for JUnit XML output files
   */
  reports.junitXml.outputLocation = file("${layout.buildDirectory.get()}/test-results/$name")
  /*
   * Set the heap size and GC for running tests.
   */
  jvmArgs("-Xmx3000m")

  /*
   * Pass along any system properties that begin with "randoop."
   * Used by randoop.main.RandoopOptions in systemTest.
   */
  System.getProperties().forEach { k, v ->
    if (k.toString().startsWith("randoop.")) {
      systemProperty(k.toString(), v)
    }
  }
}

/*
 * Write HTML reports into build/reports/allTests for all tests.
 * [
 *   Note that this may not work correctly if different Test tasks use the same
 *   test classes. Fine here because sourceSets use different packages for test
 *   classes.
 * ]
 */
tasks.register<TestReport>("testReport") {
  group = "Report"
  description = "Creates HTML reports for tests results"
  destinationDirectory = file("${layout.buildDirectory.get()}/reports/allTests")

  // The Callable defers realizing the Test tasks, so tasks registered after this one are included.
  testResults.from(Callable { tasks.withType<Test>().map { it.binaryResultsDirectory } })
}

tasks.register("printJunitJarPath") {
  description = "Print the path to junit.jar"
  doFirst { println(configurations["junit"].asPath) }
}

// ****************** Building distribution *****************

/*
 * Only want the jar file to include class files from main source set.
 * (Task part of build by default.)
 */
tasks.jar {
  from(sourceSets["main"].output)
}

val copyJars =
  tasks.register<Copy>("copyJars") {
    dependsOn(":covered-class:jar", ":replacecall:jar")
    from(subprojects.map { it.tasks.withType<Jar>() })
    into(layout.buildDirectory.dir("libs"))
  }

tasks.assemble { dependsOn(copyJars) }

tasks.shadowJar {
  dependsOn(":replacecall:shadowJar")
  // The jar file name is randoop-all-$version.jar
  archiveBaseName = "randoop-all"
  archiveClassifier = ""

  exclude("**/pom.*")
  exclude("**/default-*.txt")

  // don't include either mocks or agent classes
  // otherwise creates problems for replacement creation of replacecall agent
  exclude("**/randoop/mock/*")
  exclude("**/randoop/instrument/*")

  relocate("com.github.javaparser", "randoop.com.github.javaparser")
  relocate("com.google", "randoop.com.google")
  relocate("com.jcraft.jsch", "randoop.com.jcraft.jsch")
  relocate("com.sun.javadoc", "randoop.com.sun.javadoc")
  relocate("com.sun.jna", "randoop.com.sun.jna")
  relocate("com.trilead.ssh2", "randoop.com.trilead.ssh2")
  relocate("de.regnis.q.sequence", "randoop.de.regnis.q.sequence")
  relocate("net.fortuna.ical4j", "randoop.net.fortuna.ical4j")
  relocate("nu.xom", "randoop.nu.xom")
  relocate("org.antlr", "randoop.org.antlr")
  relocate("org.apache", "randoop.org.apache")
  relocate("org.ccil.cowan.tagsoup", "randoop.org.ccil.cowan.tagsoup")
  relocate("org.checkerframework", "randoop.org.checkerframework")
  relocate("org.ini4j", "randoop.org.ini4j")
  relocate("org.plumelib", "randoop.org.plumelib")
  relocate("org.slf4j", "randoop.org.slf4j")
  relocate("org.tigris.subversion", "randoop.org.tigris.subversion")
  relocate("org.tmatesoft", "randoop.org.tmatesoft")
}

tasks.assemble { dependsOn(tasks.shadowJar) }

tasks.register<Zip>("distributionZip") {
  dependsOn(
    "shadowJar",
    "copyJars",
    "manual",
  )
  group = "Publishing"
  description = "Assemble a zip file with jar files and user documentation"
  val dirName = "${base.archivesName.get()}-$version"
  from("build/libs/")
  from(jacocoAgentJar)
  from("src/docs/manual/index.html") {
    into("doc/manual")
  }
  from("src/docs/manual/stylesheets") {
    into("doc/manual/stylesheets")
  }
  from("src/distribution/resources/README.txt")
  into(dirName)
  exclude { details ->
    details.file.name.contains("randoop") && !details.file.name.contains("-all-")
  }
}

// ******************** Building manual ******************
/*
 * The "manual" gradle target creates the contents of the manual directory
 * within src/, which will eventually be moved to the gh-pages branch.
 *
 * Has structure:
 * src/
 *   docs/
 *     api/ - contains javadoc for main source set
 *     manual/
 *       dev.html - developer documentation
 *       index.html - user documentation
 *       *example.txt - example configuration files for user manual
 *       stylesheets/ - contains css file for web pages
 */

/*
 * Clone or update repositories containing executable scripts.
 *
 * Grgit has too many limitations.  Use exec instead.
 */
val cloneLibs =
  tasks.register("cloneLibs") {
    dependsOn(
      "cloneChecklink",
      "cloneHtmlTools",
      "clonePlumeScripts",
    )
  }

tasks.register<CloneTask>("cloneChecklink") {
  url.set("https://github.com/plume-lib/checklink.git")
  directory.set(file("${layout.buildDirectory.get()}/utils/checklink"))
  outputs.upToDateWhen { false }
}

tasks.register<CloneTask>("cloneHtmlTools") {
  url.set("https://github.com/plume-lib/html-tools.git")
  directory.set(file("${layout.buildDirectory.get()}/utils/html-tools"))
  outputs.upToDateWhen { false }
}

tasks.register<CloneTask>("clonePlumeScripts") {
  url.set("https://github.com/plume-lib/plume-scripts.git")
  directory.set(file("${layout.buildDirectory.get()}/utils/plume-scripts"))
  outputs.upToDateWhen { false }
}

tasks.withType<Javadoc>().configureEach {
  (options as StandardJavadocDocletOptions).isNoTimestamp = true
}

/*
 * Set destination directory to build/docs/api, and restrict to classes in
 * main sourceSet.
 */
tasks.javadoc {
  dependsOn(":replacecall:shadowJar")

  destinationDir = file(layout.buildDirectory.file("docs/api"))
  source(sourceSets["main"].allJava)
  options.memberLevel = JavadocMemberLevel.PRIVATE
  // Use of Javadoc's -linkoffline command-line option makes Javadoc generation
  // much faster, especially in CI.
  if (JavaVersion.current().isJava11) {
    (options as StandardJavadocDocletOptions).linksOffline(
      "https://docs.oracle.com/en/java/javase/11/docs/api/",
      "https://docs.oracle.com/en/java/javase/11/docs/api/",
    )
  } else if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    (options as StandardJavadocDocletOptions).linksOffline(
      "https://docs.oracle.com/en/java/javase/17/docs/api/",
      "https://docs.oracle.com/en/java/javase/17/docs/api/",
    )
  }

  // Make Javadoc fail on most warnings.
  // From https://stackoverflow.com/a/49544352/173852
  //
  // These options are set here rather than in
  //   tasks.withType<Javadoc> { ... }
  // because that passes the options to OptionsDoclet which cannot interpret them.
  // How can I apply the options only to Javadoc invocations that use the standard doclet?
  //
  // Add -Werror once Javadoc warnings are resolved.
  (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all", "-quiet")
  (options as StandardJavadocDocletOptions).addStringOption("Xmaxwarns", "99999")
  isFailOnError = true // does not fail on warnings
  // "-Xwerror" requires Javadoc everywhere.  Currently, CI jobs require Javadoc only
  // on changed lines.  Enable -Xwerror in the future when all Javadoc exists.
  // (options as StandardJavadocDocletOptions).addBooleanOption("Xwerror", true)
  doLast {
    ant.withGroovyBuilder {
      "replaceregexp"(
        "match" to """@import url\('resources/fonts/dejavu.css'\);\s*""",
        "replace" to "",
        "flags" to "g",
        "byline" to true,
      ) {
        "fileset"("dir" to destinationDir)
      }
    }
    if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
      if (!File("$projectDir/src/docs/api").exists()) {
        ant.withGroovyBuilder {
          "symlink"("resource" to "../../build/docs/api", "link" to "$projectDir/src/docs/api")
        }
      }
    }
  }
}

/*
 * Strictly lints the Javadoc of every class, field, and method, including private ones.
 * Unlike the `javadoc` task, any Javadoc warning fails this task.  Its output is written to
 * build/docs/api-private, so that it does not overwrite the published documentation in
 * build/docs/api, which omits private program elements' documentation.
 */
tasks.register<Javadoc>("javadocPrivate") {
  dependsOn(":replacecall:shadowJar")
  group = "Documentation"
  description = "Strictly lints the Javadoc of all classes, fields, and methods, even private ones"

  destinationDir = file(layout.buildDirectory.file("docs/api-private"))
  source(sourceSets["main"].allJava)
  classpath = sourceSets["main"].output + sourceSets["main"].compileClasspath
  options.memberLevel = JavadocMemberLevel.PRIVATE
  // Use of Javadoc's -linkoffline command-line option makes Javadoc generation
  // much faster, especially in CI.
  if (JavaVersion.current().isJava11) {
    (options as StandardJavadocDocletOptions).linksOffline(
      "https://docs.oracle.com/en/java/javase/11/docs/api/",
      "https://docs.oracle.com/en/java/javase/11/docs/api/",
    )
  } else if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    (options as StandardJavadocDocletOptions).linksOffline(
      "https://docs.oracle.com/en/java/javase/17/docs/api/",
      "https://docs.oracle.com/en/java/javase/17/docs/api/",
    )
  }

  (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all", "-quiet")
  (options as StandardJavadocDocletOptions).addStringOption("Xmaxwarns", "99999")
  (options as StandardJavadocDocletOptions).addBooleanOption("Xwerror", true)
  isFailOnError = true
}

tasks.check { dependsOn(tasks.javadoc) }

allprojects {
  tasks.register<JavaExec>("requireJavadoc") {
    description = "Ensures that Javadoc documentation exists."
    group = "Documentation"
    mainClass = "org.plumelib.javadoc.RequireJavadoc"
    classpath = sourceSets["test"].compileClasspath
    args("agent", "src/main/java", "--exclude=replacecall/src/main/java/randoop/mock")
    jvmArgs(
      "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
      "--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
      "--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
      "--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
      "--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
    )
  }
  // This doesn't yet pass.  TODO: make it pass.  (We are incrementally approaching that goal.)
  // tasks.named("check") { dependsOn(tasks.named("requireJavadoc")) }
}

// needed for javadoc.doLast
tasks.javadoc { dependsOn(cloneLibs) }

val preplacePerl =
  tasks.register<PreplaceTask>("preplacePerl") {
    outputs.upToDateWhen { false }
  }

tasks.javadoc {
  finalizedBy(preplacePerl)
}

tasks.build { dependsOn(tasks.javadoc) }

/* Update the table-of-contents for the user documentation. */
// No group so it doesn't show up in `./gradlew tasks`
val updateUserTOC =
  tasks.register<Exec>("updateUserTOC") {
    dependsOn("cloneLibs")
    setExecutable(file("${layout.buildDirectory.get()}/utils/html-tools/html-update-toc"))
    args(file("$projectDir/src/docs/manual/index.html"))
    environment("PATH", "${System.getenv("PATH")}:${layout.buildDirectory.get()}/utils/html-tools")
  }

/* Update table of contents in developer documentation. */
// No group so it doesn't show up in `./gradlew tasks`
val updateDevTOC =
  tasks.register<Exec>("updateDevTOC") {
    dependsOn("cloneLibs")
    setExecutable(file("${layout.buildDirectory.get()}/utils/html-tools/html-update-toc"))
    args(file("$projectDir/src/docs/manual/dev.html"))
    environment("PATH", "${System.getenv("PATH")}:${layout.buildDirectory.get()}/utils/html-tools")
  }

// No group so it doesn't show up in `./gradlew tasks`
val updateUserOptions =
  tasks.register<Javadoc>("updateUserOptions") {
    dependsOn("assemble")
    description = "Updates printed documentation of command-line arguments."

    setSource(sourceSets["main"].allJava.files.sorted())
    classpath = sourceSets["main"].compileClasspath
    options.memberLevel = JavadocMemberLevel.PRIVATE
    options.docletpath = sourceSets["main"].runtimeClasspath.toList()
    options.doclet = "org.plumelib.options.OptionsDoclet"
    (options as StandardJavadocDocletOptions).addStringOption(
      "docfile",
      "$projectDir/src/docs/manual/index.html",
    )
    (options as StandardJavadocDocletOptions).addStringOption("i", "-quiet")
    // For compatibility with JDK 8 Javadoc, whose standard doclet has no -noTimestamp option
    (options as StandardJavadocDocletOptions).isNoTimestamp = false
    title = ""
  }

if (JavaVersion.current() == JavaVersion.VERSION_1_8) {
  updateUserOptions { enabled = false }
}

updateUserTOC { onlyIf { !Os.isFamily(Os.FAMILY_WINDOWS) } }

updateDevTOC { onlyIf { !Os.isFamily(Os.FAMILY_WINDOWS) } }

/*
 * Generate/update and move documentation into build/docs directory.
 */
tasks.register<Exec>("manual") {
  dependsOn(
    "updateUserOptions",
    "updateUserTOC",
    "updateDevTOC",
  )

  group = "Documentation"
  description = "Adds options and TOC to documentation in src/docs"
  onlyIf { !Os.isFamily(Os.FAMILY_WINDOWS) }
  environment("PATH", "${System.getenv("PATH")}")
  commandLine("true")
}

// This is cross-platform and pulls in all required dependencies.  However,
// it emits false positives and cannot be run in an automated build.
// Output goes in build/reports/htmlSanityCheck/index.html .
tasks.named<HtmlSanityCheckTask>("htmlSanityCheck") {
  // sourceDir = File("$projectDir/src/docs/manual")
  sourceDir = File("$projectDir/src/docs")
  sourceDocuments =
    fileTree(sourceDir) {
      include(
        "index.html",
        "projectideas.html",
        "publications.html",
        "manual/dev.html",
        "manual/index.html",
      )
    }
  failOnErrors = true
}

tasks.build { dependsOn("manual") }

/*
 * Applies HTML5 validator to API javadoc.
 */
val validateAPI =
  tasks.register<Exec>("validateAPI") {
    dependsOn("javadoc")
    group = "Documentation"
    description = "Run html5validator to find HTML errors in API documentation"
    // Prior to Java 9, javadoc does not comply with HTML5.
    if (JavaVersion.current().isJava9Compatible) {
      environment("PATH", "${System.getenv("PATH")}")
      commandLine(
        "uvx",
        "html5validator",
        "--root",
        file("${layout.buildDirectory.get()}/docs/api"),
      )
    } else {
      commandLine("echo", "WARNING: HTML validation of API is only run in Java 9+.")
    }
  }

validateAPI { onlyIf { !Os.isFamily(Os.FAMILY_WINDOWS) } }

/*
 * WARNING: do not run this task unless you mean to wipe out the project pages
 * directory. It can be repopulated by publishSite, but the contents will be
 * based on changes to the files in src/docs and the Javadoc in the source.
 */
tasks.register("cleanSite") {
  group = null
  description = "Removes all files from the project pages directory (CAUTION!)"
  doLast {
    val siteDir = file("$projectDir/../randoop-branch-gh-pages")
    val oldSiteFiles =
      fileTree(siteDir) {
        exclude("README.md")
      }
    delete(oldSiteFiles)
  }
}

/*
 * Publishes changes to the project documentation to the project pages directory
 * (gh-pages branch). This task ensures that all old site files are removed, new API
 * documentation is generated, the manual is updated, and the HTML has been
 * validated.
 *
 * Note that the contents of any subdirectory of build/docs will be included in
 * the site. Currently, this is only the api directory generated by the javadoc
 * task.
 *
 * All site files will be read-only.
 */
tasks.register<Copy>("publishSite") {
  dependsOn(
    "cleanSite",
    "javadoc",
    "validateAPI",
    "manual",
  )
  group = "Publishing"
  description = "Publish changes to site files and javadoc to the project pages directory"
  val siteDir = file("$projectDir/../randoop-branch-gh-pages")
  val buildDocsDir = file("${layout.buildDirectory.get()}/docs")
  // include any built docs (e.g., api)
  val newSiteFiles =
    fileTree("$projectDir/src/docs") {
      exclude("README.md")
      exclude("api")
    }

  from(buildDocsDir)
  from(newSiteFiles)
  into(siteDir)
  filePermissions {
    user.read = true
    user.write = false
    user.execute = false
    group.read = true
    group.write = false
    group.execute = false
    other.read = true
    other.write = false
    other.execute = false
  }
}

val thisIsReleaseFilename = "$projectDir/src/main/resources/this-is-a-randoop-release"

tasks.register("thisIsReleaseFile") {
  description = "Create a file to mark the Randoop .jar file as a release"
  doLast {
    ant.withGroovyBuilder { "touch"("file" to thisIsReleaseFilename) }
  }
}

tasks.register("assembleRelease") {
  dependsOn(
    "thisIsReleaseFile",
    "assemble",
  )
  doLast {
    delete(thisIsReleaseFilename)
  }
}

// If both tasks run, then run them in the given order.
tasks.assemble { mustRunAfter("thisIsReleaseFile") }

tasks.register<DefaultTask>("buildRelease") {
  dependsOn(
    "assembleRelease",
    "check",
    "publishSite",
    "distributionZip",
  )
  group = "Publishing"
  description = "Builds system and documentation, validates HTML, and publishes site"
}

// ************** Other tool configs ************
/* Make Emacs TAGS table */
tasks.register<Exec>("tags") {
  dependsOn("cloneLibs")
  description = "Run etags to create an Emacs TAGS table"
  group = "IDE"
  commandLine(
    "bash",
    "-c",
    "find src/ covered-class/src/ replacecall/src/ -name '*.java' | grep -v src/testInput | ${layout.buildDirectory.get()}/utils/plume-scripts/sort-directory-order | xargs etags",
  )
}

/* Make Emacs TAGS table, with only Randoop code (not test code) */
tasks.register<Exec>("tagsNoTests") {
  dependsOn("cloneLibs")
  description = "Run etags to create an Emacs TAGS table that excludes test code"
  group = "IDE"
  commandLine(
    "bash",
    "-c",
    "find src/ -name *.java | ${layout.buildDirectory.get()}/utils/plume-scripts/sort-directory-order | grep -v /testInput/ | xargs etags",
  )
}

/* Run checklink */
tasks.register<Exec>("checklink") {
  dependsOn("cloneLibs")
  description = "Run checklink on randoop.github.io/randoop/ and write output to checklink-log.txt"
  environment("PATH", "${System.getenv("PATH")}:${layout.buildDirectory.get()}/utils/checklink")
  commandLine(
    "bash",
    "-c",
    "checklink -q -r `grep -v '^#' build/utils/checklink/checklink-args.txt` https://randoop.github.io/randoop/ &> checklink-log.txt",
  )
  // The command above writes the log into the task's working directory, which is the project
  // directory.  Resolve it here rather than against the Gradle daemon's working directory.
  val checklinkLog = layout.projectDirectory.file("checklink-log.txt").asFile
  doLast {
    if (checklinkLog.length() > 0) {
      ant.withGroovyBuilder { "fail"("See link-checking failures in file checklink-log.txt") }
    }
  }
}

gitProperties {
  // Handle submodules/worktrees where .git is a pointer file, not a directory.
  val absoluteGitDir = providers.exec {
    workingDir(project.rootDir)
    commandLine("git", "rev-parse", "--absolute-git-dir")
    // Fallback keeps behavior in unusual non-git contexts.
    isIgnoreExitValue = true
  }
  dotGitDirectory =
    if (absoluteGitDir.result.get().exitValue == 0) {
      file(absoluteGitDir.standardOutput.asText.get().trim())
    } else {
      file(".git")
    }
}

tasks.named("sourcesJar") { dependsOn(tasks.named("generateGitProperties")) }

apply(from = rootProject.file("gradle-mvn-push.gradle.kts"))

fun randoopPom(publication: MavenPublication) {
  // Don't use publication.from(components.java) which would publish the skinny jar as randoop.jar.
  // Information that is in all pom files is configured in randoop/gradle-mvn-push.gradle.kts.
  publication.pom {
    name = "Randoop"
    description =
      "Randoop is a unit test generator for Java. " +
        "It automatically creates unit tests for your classes, in JUnit format."
  }
}

configure<PublishingExtension> {
  publications {
    named<MavenPublication>("remote") {
      randoopPom(this)
      artifact(tasks.shadowJar)
      artifact(tasks["javadocJar"])
      artifact(tasks["sourcesJar"])
    }

    named<MavenPublication>("local") {
      randoopPom(this)
      artifact(tasks.shadowJar)
      artifact(tasks["javadocJar"])
      artifact(tasks["sourcesJar"])
    }
  }
}
