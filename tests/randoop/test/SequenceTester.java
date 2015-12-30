package randoop.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import randoop.DummyVisitor;
import randoop.EqualsHashcode;
import randoop.EqualsReflexive;
import randoop.EqualsSymmetric;
import randoop.EqualsToNullRetFalse;
import randoop.Globals;
import randoop.ObjectContract;
import randoop.main.GenInputsAbstract.BehaviorType;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.predicate.AlwaysFalseExceptionPredicate;
import randoop.test.predicate.ExceptionBehaviorPredicate;
import randoop.test.predicate.ExceptionPredicate;
import randoop.util.Util;

import junit.framework.Assert;

public class SequenceTester {

  Sequence sequence;
  Properties properties; 
  String testId;

  private static String SEQUENCE;
  private static String CODE;
  private static String EXECUTE;
  private static String CONTRACTS;
  private static String REGRESSION;
  private static String PURITY;
  private static String LINEREMOVER;
  private static List<String> legalProperties;

  private boolean oneOrMoreFailures;
  private static StringBuilder messageBuilder = new StringBuilder();


  static {
    legalProperties = new ArrayList<String>();
    SEQUENCE = "SEQUENCE";
    legalProperties.add(SEQUENCE);
    CODE = "CODE";
    legalProperties.add(CODE);
    EXECUTE = "EXECUTE";
    legalProperties.add(EXECUTE);
    CONTRACTS = "CONTRACTS";
    legalProperties.add(CONTRACTS);
    REGRESSION = "REGRESSION";
    legalProperties.add(REGRESSION);
    PURITY = "PURITY";
    legalProperties.add(PURITY);
    LINEREMOVER = "LINEREMOVER";
    legalProperties.add(LINEREMOVER);
  }

  @SuppressWarnings("deprecation") // See http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4094886
  public SequenceTester(String s, String testId) {
    this.testId = testId;
    try {
      properties = new Properties();
      properties.load(new java.io.StringBufferInputStream(s));
      if (properties.getProperty("SEQUENCE") == null) {
        throw new RuntimeException("No sequence specified: " + s);
      }
      sequence = Sequence.parse(Arrays.asList(properties.getProperty(SEQUENCE).split(";")));
    } catch (Exception e) {
      throw new Error(e);
    }

    new ContractCheckingVisitor(Collections.<ObjectContract>emptyList(), new AlwaysFalseExceptionPredicate());
  }

  public static void test(InputStream stream) throws Exception {
    if (stream == null) throw new IllegalStateException();
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

    boolean oneOrMoreFailures = false;

    String line = reader.readLine();
    while (line != null) {
      line = line.trim();
      if (line.equals("") || line.startsWith("#")) {
        // do nothing.
      } else if (line.startsWith("START TEST")) {
        String s = readOneTest(reader);
        if (!SequenceTester.test(s, line.substring("START TEST".length())))
          oneOrMoreFailures = true;
      } else {
        throw new RuntimeException(line);
      }
      line = reader.readLine();
    }

    if (oneOrMoreFailures) {
      // Print the failure message directly to stdout because
      // sometimes Assert.fail prints incorrectly. In particular, it
      // can omit printing long lines, and this can cause confusion
      // when trying to determine the cause of a failure.
      System.out.println(messageBuilder.toString());
      Assert.fail("RANDOOP TEST FAILURES. (See console output for details).");
    }
  }

  private static String readOneTest(BufferedReader reader) throws IOException {
    StringBuilder test = new StringBuilder();
    String line = reader.readLine();
    while (line != null && !line.equals("END TEST")) {
      line = line.trim();
      test.append(line + Util.newLine);
      line = reader.readLine();
    }
    return test.toString();
  }

  private static boolean test(String s, String testId) {
    SequenceTester tester = new SequenceTester(s, testId);
    tester.oneOrMoreFailures = false;
    tester.test();
    if (tester.oneOrMoreFailures) {
      return false;
    }
    return true;
  }

  private void test() {
    for (Enumeration<?> e = properties.propertyNames() ; e.hasMoreElements() ; ) {
      String propertyName = (String)e.nextElement();
      String propertyVariable = properties.getProperty(propertyName);
      if (propertyName.equals(PURITY)) {
        // do nothing.
      } else if (propertyName.equals(SEQUENCE)) {
        testToParseableString(propertyVariable);
      } else if (propertyName.equals(CODE)) {
        testCode(propertyVariable);
      } else if (propertyName.equals(EXECUTE)) {
        testExecute(propertyVariable);
      } else if (propertyName.equals(CONTRACTS)) {
        testContracts(propertyVariable);
      } else if (propertyName.equals(REGRESSION)) {
        testRegression(propertyVariable);
      } else {
        throw new RuntimeException("illegal property: " + propertyName + " = " + propertyVariable);
      }
    }
  }

  private void testToParseableString(String expected) {
    String parseableString = sequence.toString();
    checkEqualStatements(expected, parseableString, "testing toString()");
  }

  private void testRegression(String expected) {
    ExecutableSequence ds = new ExecutableSequence(sequence);
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ExceptionPredicate isExpected = new ExceptionBehaviorPredicate(BehaviorType.EXPECTED);
    ExpectedExceptionCheckGen expectation; 
    expectation = new ExpectedExceptionCheckGen(visibility, isExpected);
    ds.execute(new DummyVisitor(), new RegressionCaptureVisitor(expectation,true));
    checkEqualStatements(expected, ds.toString(), "testing RegressionCaptureVisitor");
  }
  
  
  private static final TestCheckGenerator testGen;
  static {
    List<ObjectContract> contracts = new ArrayList<ObjectContract>();
    contracts.add(new EqualsReflexive());
    contracts.add(new EqualsToNullRetFalse());
    contracts.add(new EqualsHashcode());
    contracts.add(new EqualsSymmetric());
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ExceptionPredicate isExpected = new ExceptionBehaviorPredicate(BehaviorType.EXPECTED);
    ExpectedExceptionCheckGen expectation; 
    expectation = new ExpectedExceptionCheckGen(visibility, isExpected);
    testGen = new ExtendGenerator(
        new ContractCheckingVisitor(contracts, new ExceptionBehaviorPredicate(BehaviorType.ERROR)),
        new RegressionCaptureVisitor(expectation, true));
  }

  private void testContracts(String expected) {
    return;
  }

  private void testExecute(String expected) {
    ExecutableSequence ds = new ExecutableSequence(sequence);
    ds.execute(new DummyVisitor(), testGen);
    checkEqualStatements(expected, ds.toCodeString(), "testing execution");
  }

  private void testCode(String expected) {
    String codeString = sequence.toCodeString();
    checkEqualStatements(expected, codeString, "testing toCodeString()");
  }

  private void checkEqualStatements(String expectedString,
      String actualString, String context) {
    String[] expected = trimmedStatements(expectedString);
    String[] actual = trimmedStatements(actualString);
    if (expected.length != actual.length) {
      failWithMessage("when " + context + ", lengths differ", expected,
          actual);
      return;
    } else {
      for (int i = 0; i < expected.length; i++) {
        if (!expected[i].equals(actual[i])) {
          failWithMessage("when " + context + ", statement " + i
              + " differ", expected, actual);
        }
      }
    }
  }

  private void failWithMessage(String string, String[] expected, String[] actual) {
    oneOrMoreFailures = true;
    StringBuilder b = new StringBuilder();
    b.append("Failure in test " + testId + " " + string);
    b.append("" + Globals.lineSep + "Expected:" + Globals.lineSep + "");
    for (int i = 0 ; i < expected.length ; i++)
      b.append(i + ": " + expected[i] + Util.newLine);
    b.append("" + Globals.lineSep + "Actual:" + Globals.lineSep + "");
    for (int i = 0 ; i < actual.length ; i++) {
      b.append(i + ": " + actual[i] + Util.newLine);
    }
    messageBuilder.append(b.toString() + Util.newLine);
  }

  private String[] trimmedStatements(String s) {
    String[] statements = s.split(";");
    List<String> trimmed = new ArrayList<String>();
    for (int i = 0 ; i < statements.length ; i++) {
      String trimmedLine = statements[i].trim();
      if (!trimmedLine.equals("")) {
        trimmed.add(trimmedLine);
      }
    }
    return trimmed.toArray(new String[0]);
  }
}
