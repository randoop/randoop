# Randoop Unit Test Minimizer Tests

Test cases for the Randoop Unit Test Minimizer

## Format
`MinimizerTests.java` is the main entry point.

To add a test, copy and paste `test1()` and rename the method.

Each test case needs:

1. An input Java file to be minimized
2. A file representing what the expected output should be
3. The jar file dependencies needed to compile and run the input Java file

Update the `inputFilePath`, `outputFilePath`, and `expectedFilePath` variables to point to the correct input and expected files.

Also provide the dependencies needed to compile and run the input Java file.

Finally, specify the `timeoutLimit`, the maximum number of seconds allowed for any unit test case to execute.