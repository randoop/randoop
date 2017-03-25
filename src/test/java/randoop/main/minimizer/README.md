# Randoop Unit Test Minimizer Tests

Test cases for the Randoop Unit Test Minimizer

## Format
`MinimizerTests.java` is the main entry point.

To add a test:
1. Create an input JUnit file to be minimized, in directory src/test/resources/minimizer/
2. Create a goal file containing the expected minimized output, in the same directory with extension `.java.expected`.
3. Create a new test case in file `MinimizerTests.java`
    * copy and paste `test1()` in 
    * rename the copied method.
    * Update the `inputFilePath` variable; optionally provide dependences needed to compile and run the input Java file.
