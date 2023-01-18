Version 4.3.2 (January 8, 2023)
-------------------------------

Randoop supports Java 19 (and still supports Java 8, Java 11, and Java 17).


Version 4.3.1 (July 13, 2022)
-----------------------------

Several bug fixes.

Minor documentation improvements.


Version 4.3.0 (January 31, 2022)
--------------------------------

Randoop supports Java 17 (and still supports Java 8 and Java 11).

Removed command-line argiments `--omitmethods` and `--omitmethods-file`,
which were deprecated two years ago.

Support escaping dollar sign from variable name.


Version 4.2.7 (December 17, 2021)
---------------------------------

New `--test-package` command-line option means to test all classes on the
classpath within the given.  Thanks to Ivan Kocherhin.

Randoop tests public static methods in classes that cannot be instantiated.

Bug fixes.
 * Fixed a problem when calling Randoop twice.

Minor documentation improvements.


Version 4.2.6 (May 3, 2021)
---------------------------

Bug fixes.  The most important are:
 * Fix `NullPointerException` that may occur when using JDK 8.
 * Use correct jar path for Windows hosts


Version 4.2.5 (December 2, 2020)
--------------------------------

New command-line options:
 * `--clear_memory`
 * `--omit-classes-no-defaults`

Randoop will call methods in noninstantiable classes.

Improved documentation about diagnosing when Randoop produces no tests

Better handling of Mockito-generated class files.


Version 4.2.4 (July 14, 2020)
-----------------------------

Bug fixes.
No user-visible behavior changes.


Version 4.2.3 (March 31, 2020)
------------------------------

In generated tests, Randoop produces fewer verbose comments and uses better
assertion style (e.g., assertEquals rather than assertTrue).

Bug fixes.
One example is proper treatment of resource paths on Windows.


Version 4.2.2 (February 29, 2020)
---------------------------------

Implemented --omit-classes and --omit-classes-file command-line options.

Renamed command-line options:
 * --omitmethods => --omit-methods
 * --omitmethods-file => --omit-methods-file
 * --omit-fields-list => --omit-fields-file
The old versions still work temporarily.

Reduced default for --string-maxlen to 1000.

Bug fixes.


Version 4.2.1 (October 27, 2019)
--------------------------------

Bug fixes.
No user-visible behavior changes.


Version 4.2.0 (August 22, 2019)
-------------------------------

Randoop supports Java 11 (and still supports Java 8).

Randoop reports methods under test that may be nondeterministic or
dependent on side-effected global state.  New command-line option
--nondeterministic-methods-to-output controls how many suspicious
methods Randoop reports.

Randoop builds in a set of omitted methods that are nondeterministic or
long-running.

Randoop produces more assertions, by calling observer methods.  This only
works if you supply the --side-effect-free-methods command-line argument.

New command-line arguments:
 * --testjar, --omit-classes, and --omit-classes-file
   make it easy to test all classes in a .jar file.
 * --jvm-max-memory indicates how much memory to use when starting a new JVM.

Command-line argument changes:
 * the --omit-methods-file argument can be repeated
 * default for --flaky-test-behavior argument changed from HALT to OUTPUT
 * renamed --print-erroneous-file to --print-non-compiling-file


Version 4.1.2 (April 21, 2019)
------------------------------

Bug fixes, improve documentation and diagnostics.
No user-visible behavior changes.


Version 4.1.1 (February 3, 2019)
--------------------------------

Minor bug fixes; no user-visible behavior changes.


Version 4.1.0 (September 5, 2018)
---------------------------------

Randoop requires Java 8; it no longer runs on a Java 7 JVM.

New technique, "Bloodhound", for randomly selecting methods to call in a
test case.  It weights each method under test based on the method's branch
coverage and the number of times the method is chosen for a new sequence.
Enable it via --method-selection=BLOODHOUND .

Command-line argument changes:
 * renamed --small-tests to --input-selection=small-tests


Version 4.0.4 (May 23, 2018)
----------------------------

Command-line argument changes:
 * the --specifications command-line argument can now be a .zip file
 * renamed --print-file-system-state to --print-erroneous-file

Randoop's replacecalls agent can now replace constructors.

More methods are now permitted as observers.

Improved diagnostics when user supplies incorrect input to Randoop.

Bug fixes.

Improvements to documentation.


Version 4.0.3 (April 10, 2018)
------------------------------

Fixed a bug caused by order of execution of variable initializers.


Version 4.0.2 (April 8, 2018)
-----------------------------

Adds the contract c.toArray().length == c.size() for Collections c.

Make --omitmethods work for constructors as well as methods.

Minor documentation improvements.


Version 4.0.1 (March 25, 2018)
------------------------------

This release makes minor documentation fixes.  Two examples are:
 * The manual provides a link to a newer Maven plug-in.
 * The documentation of the test minimizer is more prominent.

There are also code cleanups and minor bug fixes.


Version 4.0.0 (February 28, 2018)
---------------------------------

Many improvements to the manual, including improved instructions about
debugging when Randoop does not produce good test suites.

Randoop contains a test suite minimizer.  Given a test suite containing
failing tests, it creates a test suite with smaller tests that fail the
same way.
 * You can run this minimizer on any test suite, whether it was created by
   Randoop or not.
 * Command-line option --minimize-error-test makes Randoop automatically
   run the minimizer on fault-revealing tests before it outputs them; this
   makes the failures easier to understand.
 * There are other command-line options that control the minimization process.

Randoop's mapcall agent has been renamed replacecall and has been improved.
The replacecall agent prevents Randoop, or a test suite, from calling certain
methods such as System.exit(); instead, at run time a replacement method is
called.
 * The file format for specifying replacements has changed, and the file
   argument is now named --replacement-file.
 * The replacecall agent applies default replacements for System.exit() and
   display of AWT/Swing components.
 * Randoop does not make direct calls to methods whose calls are replaced.
   You can disable this with option --dont-omit-replaced-methods.

New command-line arguments:

--operation-history-log writes a table of operations used during generation.

--print-file-system-state (default = false) writes any generated tests with
compilation errors to standard out.

--deterministic (default = false) makes Randoop is deterministic, producing
the same test suite when run twice.  When set to false, Randoop may or may
not produce the same test suite when run twice.  Use --randomseed to
produce a different test suite.

--cm-exception and --ncdf-exception control how Randoop classifies a test that
throws a ConcurrentModificationException or NoClassDefFoundError exception.

--attempted-limit is a new way to limit test generation.

Changed command-line arguments:

Some command-line options have been renamed:
 * --timelimit => --time-limit
 * --inputlimit => --generated-limit
 * --outputlimit => --output-limit
 * --timeout => --call-timout.
 * --include-if-classname-appears => --require-classname-in-test.
 * --include-if-class-exercised => --require-covered-classes.
     File "exercised-class.jar" is now "covered-class.jar".
 * --noprogressdisplay => --progressdisplay=false.
 * --ignore-flaky-tests option => --flaky-test-behavior.  It is now an enum:
     * HALT: Halt and give a diagnostic message (default).
     * DISCARD: Discard the flaky test.
     * OUTPUT: Output the flaky test.

Command-line argument --omitmethods can now be supplied multiple times.  New
argument --omitmethods-file enables providing many patterns in a file.

Command-line arguments --omitmethods and --methodlist now match against a
method/constructor signature rather than java.lang.reflect.Method.toString().

--omitmethods omits the method throughout the inheritance hierarchy.  See
the documentation of --omitmethods in the user manual for details.

The handling of methods from --methodlist is now consistent with what is
done for methods of classes in --testclass or --classlist.  See the
documentation of --methodlist in the user manual for details.

The --usethreads command-line option defaults to false.  This makes Randoop
run faster and generate more tests, but Randoop will hang if it calls a
method that does not terminate and return.

Notable bug fixes:

Fixed a problem in progress reporting where the wrong count was being used for
error-revealing sequences.

Fixes a bug that required input using --testclass or --classlist, preventing
exclusive use of --methodlist.


Version 3.1.5 (April 28, 2017)
------------------------------

Fixes a bug where a call to a method with a type variable return type should
result in a ClassCastException does not throw the exception, but instead leads
to a later failure when invoking a class member.

Fixes and improves error handling.


Version 3.1.4 (April 11, 2017)
------------------------------

Fixes a bug uncovered in obscure cases when package accessibility is used.


Version 3.1.3 (April 11, 2017)
------------------------------

Fixes a bug where package accessibility checks fail to work correctly.


Version 3.1.2 (March 21, 2017)
------------------------------

The exercised-class jar now includes dependencies.


Version 3.1.1 (March 15, 2017)
------------------------------

Fix to make the numbering of test classes consistent with previous versions.


Version 3.1.0 (March 14, 2017)
------------------------------

Filters generated test sequences that don't compile. There are a small number of
known input cases for which Randoop produces uncompilable tests. This check will
prevent these tests from being created, but has a run-time penalty. In cases where
generating uncompilable tests is not a concern this check can be disabled by
setting --check-compilable=false.

Adds new command-line option --sof-exception that controls how Randoop
classifies a test that throws a StackOverflowError exception.


Version 3.0.10 (March 6, 2017)
------------------------------

Avoids Randoop failures due to issues that arise during test generation.


Version 3.0.9 (February 22, 2017)
---------------------------------

New command-line option --stop-on-error-test stops test generation once
an error-revealing test is generated.  This is useful during cycles of
fixing errors discovered by Randoop.

Classpath error message explains why a class is not found.

Fixes
- Many fixes, mostly related to Java generics, including the following.
- Fixed issues: 123, 128, 132, 139, 141, 143, 144, 145.
- Fixed assignability for parameterized types with wildcard arguments.
- Fixed failure when a generic method or constructor has an
  argument type that has a wildcard with a bound that includes the type
  parameter of the generic (e.g., Iterable<? extends T>).

Thanks to Marko Dimjasevic for issue reports.


Version 3.0.8 (December 8, 2016)
--------------------------------

Fixes
- a failure when instantiating the generic containing type of a non-generic
  member class.
- generation of different tests between runs over the same classes
  where the classes were given in different order.
- generation of invalid calls to a static member of an inaccessible class when
  inherited by an accessible subclass.
- a failure when checking type instantiations against certain kinds of bounds
  on type variables.
- generation of uncompilable code when static final fields with same name are
  inherited by a class under test from more than one class or interface.
- a failure due to an assertion enforcing a property on type bounds that is
  valid for type parameters, but not for wildcard bounds.
- generation of uncompilable code due to improper instantiation when a type
  parameter has a bound that is an intersection type that includes a generic
  type.

Thanks to Mark Roberts and Marko Dimjasevic for issue reports.


Version 3.0.7 (November 28, 2016)
---------------------------------

Randoop now
- instantiates generic classes and operations on-the-fly using the
  types in previously generated test sequences. As a result, Randoop is able to
  generate more diverse instantiations of generics, as well as multidimensional
  arrays.
- supports insertion of Java code into test fixtures when tests are written to
  disk. Given as filename to new command-line arguments: --junit-before-each,
  --junit-after-each, --junit-before-all, and --junit-after-each.


Version 3.0.6 (October 14, 2016)
--------------------------------

Randoop now uses an approach to selecting instantiations of generic types (including
type arguments of generic methods/constructors) that is less likely to lead to
an OutOfMemoryError.

Fixed bug that would lead to failure involving a particular case of a static
member class.


Version 3.0.5 (October 5, 2016)
-------------------------------

Randoop now ignores generic operations or classes for which it is unable to
assign a type to a type argument. These are included in log output.

Fixed bugs resulting in run-time failures:
- a member class of a generic class was being constructed so that type
  parameters were not tracked.
- an exception during loading of a class would result in a failure, now ignored.
- an enum has an override of toString() that returns null.

Fixed bug in which flaky tests could be generated involving NaN. Thanks to
Naljorpa Chagmed for reporting this issue.


Version 3.0.4 (August 29, 2016)
-------------------------------

Randoop now:
- collects all member types accessible by generated tests (fixes issue #88).
- generates EnumSet objects for an Enum that is an input class (fixes issue #100)
- generates assertions on Enum values (fixes issue #87)
- generates Collection objects with parameterized element type (fixes issue #115)
- generates arrays with parameterized element type (fixes issue #114)

Fixed a bug in which uncompilable code was being generated for a call to a static
method of a generic class. Also, fixes a bug where uncompilable code to initialize
a generic array was being generated.

Fixed a bug where test code was being generated that would result in an NPE when
an inner class constructor is called with the first parameter null. (Due to an
apparent bug in Java reflection classes.)

Fixed a bug that was preventing contracts over compareTo from being tested.
Issue thanks to Waylon Huang.


Version 3.0.3 (July 28, 2016)
-----------------------------

Fixed bugs that resulted in run-time errors. One involving failure to instantiate
type parameters with dependencies among type bounds. Another resulting in a
failure during capture conversion.
Also, fixed a case where uncompilable tests involving bridge methods could be
generated.


Version 3.0.2 (July 20, 2016)
-----------------------------

Fixed a bug related to recursive type bounds of wildcard arguments. Thanks to
Huascar Sanchez for reporting this issue.

Fixed a bug resulting in tests that threw an NPE when an expected anonymous
exception was caught. Thanks to Matias Martinez for reporting this issue.

Fixed a bug in reading input class names and an empty string was read.

The command-line argument --init-routine has been removed.


Version 3.0.1 (June 16, 2016)
-----------------------------

Randoop now includes all of the contracts described in the user manual.
It also uses a new contract checking heuristic that avoids exhaustive
enumeration over the new ternary contracts.

Fixed a bug that prevented generation of collections.


Version 3.0.0 (June 6, 2016)
----------------------------

Randoop now tracks types independently of the Java reflection classes. This
means for generic classes it will select a type argument for each type parameter
and use that selection consistently. This will avoid compile and run time type
errors related to inconsistent use of a parameterized type.

Randoop will now generate longer arrays for reference types. Element types must
be primitive, boxed primitive, or be input with --testclass.

Fixed a bug where the opening parenthesis was not being generated for a call to
the constructor of a non-static inner class.

The following command-line arguments have been removed: -output-tests-serialized,
--componentfile-ser, --componentfile-txt, --output-components

For developers: Randoop is now built using Gradle (with the Gradle Wrapper).
File organization has changed to the default used by Gradle Java plugin.
See the developer manual for details.


Version 2.1.4 (February 24, 2016)
---------------------------------

Fixed a bug in --include-if-class-exercised that was including too many tests.
The option now requires that Randoop be run with -javaagent:exercised_agent.jar.
Also, modified the option so that allow any class, and not just those specified
by --testclass or --classlist. This change allows abstract classes, which cannot
be given to --testclass.

The Java agent jar files are now part of distribution files and can be found in
dist after running 'make distribution-files'. The agent jar randoop_agent.jar
has been renamed to mapcall_agent.jar.

Fixed code generation in --junit_reflection_allowed so that generated
driver file will compile.

Renamed command-line arguments --{error,regression}-test-filename to
--{error,regression}-test-basename.

Documented that Randoop may create dependent tests whose outcome depends
on the order in which they are run, and what to do about it.


Version 2.1.3 (February 5, 2016)
--------------------------------

Renamed command-line argument --include-only-classes to
--include-if-classname-appears.  Clarified that a test is output only if
the test's source code textually uses some member from the classes.

New command-line argument --include-if-class-exercised causes Randoop to
output only tests that execute some method in the given classes, whether or
not the method appears in the source code of the test.

Changed command-line argument defaults to forbid-null=false and
--null-ratio=0.05.

Fixed a bug in which a primitive type would be selected as a receiver of a
method call.


Version 2.1.2 (January 26, 2016)
--------------------------------

Fixed issues related to filtering of classes and members so that Randoop ignores:
- public methods/constructors with package private parameter types, and
- synthetic constructor with anonymous parameter (generated for
  private constructor of outer class used by inner class).
Thanks to Rene Just and Gordon Fraser.


Version 2.1.1 (January 22, 2016)
--------------------------------

Randoop now prints nicer error information when it halts after encountering a
flaky test (a test in which an exception is thrown elsewhere than at the last
statement).  This flaky behavior is usually due to nondeterminism or to side
effects on global state.  So, the best solution is to not run Randoop on
methods that are nondeterministic or that side-effect global state.
Alternately, you can use the --ignore-flaky-tests option to make Randoop discard
flaky tests and proceed.

Randoop is now package-access-aware in determining which class members to
include in a test.  If --junit-package-name is set to the package of a
class under test, the tests will include non-private members of the
class. Restrict tests to only public members by setting
--only-test-public-members.

Fixed issue #78:  Number objects were tested for equality with Double.Nan
or Float.Nan without a cast.
Fixed issue #80: package access fields can now be set and read.
Fixed issue where evaluating a contract resulted in an IllegalArgumentException.
Thanks to Juan Pablo Galeotti, Rene Just, and Gordon Fraser.


Version 2.1.0 (December 30, 2015)
---------------------------------

Randoop now splits generated tests into three categories: error-revealing,
regression, and invalid. Error-revealing and regression are now output as
separate test suites rather than combining all tests into a single suite.
(Invalid tests are discarded.) Also, new command-line options allow the
classification of exceptions to control how tests are categorized.

Both error-revealing and regression tests are output by default. Each category
is output only if a test of that category is generated. Use the command-line
arguments --no-error-revealing-tests and --no-regression-tests to disable
generation of one or the other of the suites. Use --error-test-filename and
--regression-test-filename to set the file names. These command-line
arguments replace the arguments --check-object-contracts,
--check-regression-behavior and --junit-classname that have been removed.

Tests where exceptions are thrown are classified by how exceptions are
assigned types of behavior. A test with with an exception that is INVALID
behavior is invalid, one with an exception that is ERROR behavior (and none
that are INVALID) is an error-revealing test, and one with no INVALID or ERROR
behaviors and an exception that is an EXPECTED behavior is a regression test.
Customize the classification of exceptions by setting the behavior type for
exceptions using the command-line arguments --checked-exception,
--unchecked-exception, --npe-on-null-input, --npe-on-non-null-input,
and --oom-exception.
(Issues #20, #69 and #72)

Additional changes:

You can control whether assertions appear in regression tests with the new
command-line argument --no-regression-assertions.

The command line --test-classes has been changed to --include-only-classes to
distance meaning from --testclass argument. (Addresses #66)

The command-line arguments --simplify-failed-tests, --remove-subsequences,
--compare-checks, --clean-checks, --print-diff-obs and --output-nonexec have
been removed. The command option exec has been removed.

Support for the Eclipse plugin has been removed. The option --comp-port has been
removed.

The interface for ExecutionVisitor has been changed to visitAfterStatement and
visitBeforeStatement to clarify that the methods are invoked relative to the
execution of each statement. Note that an ExecutionVisitor may no longer modify
the Checks for a Sequence.

This release fixes a bug in which a public method inherited from a package
private class was being excluded from testing. (Thanks to Alberto Goffi for
raising the issue and providing a concise example.)


Version 2.0.1 (November 30, 2015)
---------------------------------

Fixed a bug in the variable names for generated array declarations.

Randoop now generates tests involving fields (fixes issues #21 and #47).
A Randoop-generated test previously only invoked methods; now the
generated tests may access and set fields as well.  Use command-line
option --omit-field-list to make Randoop ignore certain fields.

Randoop now includes enums in generated tests (fixes issue #17).

JUnit now executes Randoop-generated tests in a deterministic order:
in ascending order by name.

The Randoop Eclipse plugin has moved into its own repository:
https://github.com/randoop/randoop-eclipse-plugin


Version 2.0 (October 13, 2015)
------------------------------

The Randoop homepage is now http://randoop.github.io/randoop/ .
Moved the Randoop version control repository to its own GitHub organization:
it now appears at https://github.com/randoop/randoop
(previously https://github.com/mernst/randoop).

Randoop now outputs JUnit 4 tests (previously JUnit 3 tests).
These can be run from the command line using a command such as

  java -classpath .:$JUNITPATH org.junit.runner.JUnitCore RandoopTest

If command-line argument --junit-reflection-allowed=false is set when
running Randoop, then Randoop instead produces a Main class that calls each
of the test methods individually, without using reflection.

Created a new mailing list, randoop-discuss@googlegroups.com, for
discussions with Randoop users.  It is open to join and should be easier to
search than the existing randoop-developers@googlegroups.com mailing list.


Version 1.3.6 (August 10, 2015)
-------------------------------

Command-line-argument --junit-reflection-allowed=false causes Randoop to
avoid use of JUnit's reflective test invocation.  Use of direct method
calls instead can make Randoop's generated tests easier to analyze.

Changed default for --literals-level to CLASS.

Improvements in treatment of observer methods:  put observer results in the
pool, but not receivers after observer invocations.  If you are not using
observers when you run Randoop, then you are not using Randoop as intended
and Randoop may be generating sub-optimal test suites.

Fix bugs, notably:
 * #18 Handle non-accessible thrown exceptions
 * #51 Use better variable names in generated tests
 * Use equals, not ==, to compare NaN

Many documentation improvements, including clarifications and
restructuring.  The manual mentions the Maven plug-in and explains that
Randoop is deterministic but your sequential Java program may not be.

Not directly related to Randoop, but possibly of interest to Randoop users:
ABB Corporation has released an improved version of Randoop.NET.  Compared to
the original version of Randoop.NET from Microsoft Research:
 * It fixes some bugs.
 * It adds new features, notably regression assertions for more effective
   regression testing, method transformers to delete or replace calls to
   specific methods in the assembly, and richer debug information collection.
 * It adds a GUI, as a VS2010 add-in.
The Java implementation of Randoop continues to have more features than
Randoop.NET, but ABB's improvements make the .NET tool more usable.


Version 1.3.5 (May 28, 2015)
----------------------------

Command-line argument --check-regression-behavior controls whether the tests
include assertions about the current behavior.

Command-line argument --observers lets you provide a file listing observer
functions.

The Randoop homepage is now http://mernst.github.io/randoop/ .

Moved the Randoop version control repository from Google Code to GitHub,
and from the Mercurial version control system to Git.

Randoop now compiles, and its tests pass, under Java 8.


Version 1.3.4 (January 1, 2014)
-------------------------------

Bug fixes:
 * String lengths are checked correctly
 * No global timeout when per-thread timeout is in force
 * Better agent flag parsing
 * Don't output code like "catch (null e)".

Documentation:
 * Add troubleshooting section about no tests being created
 * Tips on reporting a bug
 * Documentation for building a release


Version 1.3.3 (Novmember 21, 2012)
----------------------------------

Bugfixes
  - Workaround for openjdk bug (http://bugs.sun.com/view_bug.do?bug_id=6973831)
  - Fix for PrettyPrinter
  - Unquoted character constants make Randoop generate uncompilable tests
  - Problem of using compare_checks in eclipse plugin by adding

Features
  - Suppress generics-related compiler warnings
  - Renaming sequence variables for a better name, and minimizing faulty sequence
  - Generated JUnit tests are compatible with Java 1.4
  - TimeoutExceeded and TimeOutException unified into TimeoutExceededException

Documentation enhanced


Version 1.3.2 (August 22, 2010)
-------------------------------

(No changelog entry available.)


Version 1.3.1
-------------

Added @TestValue annotation, allowing users to specify additional
primitive values to Randoop programmatically.

Added checks for NullPointerException (in the absence of null inputs)
as a default contract.
