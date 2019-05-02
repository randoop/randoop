# Randoop

Randoop is a unit test generator for Java.
It automatically creates unit tests for your classes, in JUnit format.

## Learn about Randoop:

* [Randoop homepage](https://randoop.github.io/randoop/)
* [Randoop manual](https://randoop.github.io/randoop/manual/index.html)
* [Randoop release](https://github.com/randoop/randoop/releases/latest)
* [Randoop developer's manual](https://randoop.github.io/randoop/manual/dev.html)
* [Randoop Javadoc](https://randoop.github.io/randoop/api/)

## Directory structure

* `agent` - subprojects for Java agents (load-time bytecode rewriting)
* `gradle` - the Gradle wrapper directory (*Should not be edited*)
* `lib` - jar files for local copies of libraries not available via Maven
* `scripts` - git hook scripts
* `src` - source directories for Randoop, including
    * `coveredTest` - source for JUnit tests of the covered-class Java agent
    * `distribution` - resource files for creating the distribution zip file
    * `docs` - [documentation]("https://randoop.github.io/randoop/"), including the manual and resources
    * `javadoc` - resource files for creating [API documentation](https://randoop.github.io/randoop/api/)
    * `main` - Randoop source code
    * `replacecallTest` - source for JUnit tests of the replacecall Java agent
    * `systemTest` - source for Randoop system tests
    * `test` - source for JUnit tests of Randoop
    * `testInput` - source for libraries used in Randoop testing

The source directories follow the conventions of the Gradle Java plugin, where
each directory has a _java_ subdirectory containing Java source, and,
in some cases, a _resources_ subdirectory containing other files.
