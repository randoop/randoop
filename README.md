# THIS IS A PARTIALLY IMPLEMENTED BRANCH

This branch introduces the use of a "symbol" table for managing the type
parameters from the declaration of a generic class, method or constructor.
This corrects an error involving how type parameters are dealt with in the Type
hierarchy that is known to result in generation of uncompilable code for certain
inputs, and likely affects a number of other open issues.

This table should be created when the generic is created. Classes are
created in `randoop.reflection.DeclarationExtractor.visitBefore(Class<?>)`.
A method operation is created in `randoop.reflection.OperationExtractor.visit(Method)`.
And, a constructor operation is created in `randoop.reflection.OperationExtractor.visit(Constructor)`.


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
* `devnotes` - developer notes, including the change log
* `docs` - the [GitHub site]("https://randoop.github.io/randoop/") contents, including the manual (*Should not be edited*)
* `gradle` - the Gradle wrapper directory (*Should not be edited*)
* `lib` - jar files for local copies of libraries not available via Maven
* `scripts` - git hook scripts
* `src` - source directories for Randoop, including
    * `coveredtest` - source for JUnit tests of the covered-class Java agent
    * `distribution` - resource files for creating the distribution zip file
    * `docs` - all editable documentation and resources
    * `javadoc` - resource files for creating [docs/api](docs/api)
    * `main` - Randoop source code
    * `mapcalltest` - source for JUnit tests of the mapcall Java agent
    * `systemtest` - source for Randoop system tests
    * `test` - source for JUnit tests of Randoop
    * `testinput` - source for libraries used in Randoop testing

The source directories follow the conventions of the Gradle Java plugin, where
each directory has a _java_ subdirectory containing Java source, and,
in some cases, a _resources_ subdirectory containing other files.
