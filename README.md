# Randoop

Randoop is a unit test generator for Java.
It automatically creates unit tests for your classes, in JUnit format.

## More about Randoop:

* [Randoop homepage](https://randoop.github.io/randoop/).
* [Randoop manual](https://randoop.github.io/randoop/manual/index.html)
* [Randoop release](https://github.com/randoop/randoop/releases/latest)
* [Randoop developer's manual](https://randoop.github.io/randoop/manual/dev.html)
* [Randoop Javadoc](https://randoop.github.io/randoop/api/)

## Directory structure

* `agent` - subprojects for Java agents (load-time bytecode rewriting)
* `docs` - the [GitHub site]("https://randoop.github.io/randoop/") contents, including the manual
* `gradle` - the Gradle wrapper directory (*Should not be edited*)
* `lib` - jar files for local copies of libraries not available via Maven
* `scripts` - git hook scripts
* `src` - source directories for Randoop, including
    * `docs` - all non-website documentation and resources 
    * `main` - Randoop source code
    * `test` - source for JUnit tests of Randoop
    * `agenttest` - source for JUnit tests of the exercised-class Java agent
    * `testinput` - source for libraries used in Randoop testing
    * `systemtest` - source for Randoop system tests

The source directories follow the conventions of the Gradle Java plugin, where
each directory has a _java_ subdirectory containing Java source, and,
in some cases, a _resources_ subdirectory containing other files.
