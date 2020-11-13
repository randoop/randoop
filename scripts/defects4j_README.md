Randoop coverage on the Defects4J defects
----------------
This section describes how to calculate Randoop code coverage over the Defects4J
defects (see the top-level
[README](https://github.com/rjust/defects4j/blob/master/README.md) for
more details about the defects and requirements).

1. Follow steps 1-4 under
   [Steps to set up
   Defects4J](https://github.com/rjust/defects4j/blob/master/README.md#steps-to-set-up-defects4j)
   in the top-level README.

2. Optionally, use a different version of Randoop.

   By default, the system uses the version of Randoop at
   $D4J_HOME/framework/lib/test_generation/generation/randooop-current.jar.

   * You can indicate a different directory that contains Randoop (note that the `.jar` files must be suffixed `-current.jar`):
     ```export TESTGEN_LIB_DIR="path-to-directory-containing-randoop-current.jar"```
   * You can link `.jar` files from a local version of Randoop:
     ```
     export randoop=MY_RANDOOP_DIRECTORY && (cd $randoop && rm -rf build/libs/ && ./gradlew assemble) && (cd $D4J_HOME/framework/lib/test_generation/generation && $randoop/scripts/replace-randoop-jars.sh "-current")
     ```

3. Link the defect4j testing scripts from this directory to the defects4j test directory:
   `(cd $D4J_HOME/framework/test && ln -s $randoop/scripts/defects4j_* .)`

4. Change directory to `$D4J_HOME/framework/test` and run the test generation and coverage analysis:
    - `./defects4j_randoop.sh`

    Currently, this does not generate tests for all the defects, just five in
    each of six different projects for a total of 30 tests. It takes about 90
    minutes to run. If you wish to override these defaults you may supply
    an optional project list argument followed by an optional bid (bug id)
    list argument. You may use the string 'all' for the bid list argument to
    run all the active tests in a project.  Finally, you may add the optional
    argument 'debug' at the end to get lots of additional diagnostic output.
    This script sets `TMP_DIR` to a unique subdirectory of the current directory
    (test). To change this, you will need to modify `./defects4j_randoop.sh`.

5. The end of the defects4j_randoop.sh script invokves `./defects4j_coverage.pl` to
   display the coverage data.  You may rerun this script to review the results.
    - `./defects4j_coverage.pl`

    The raw coverage data is found at `$TMP_DIR/output/coverage`.
    This script will accept an optional argument of an alternative file location.
    Invoke the script with `-help` for a full list of options.
