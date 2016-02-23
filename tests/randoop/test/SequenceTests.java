package randoop.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import randoop.sequence.SequenceParseException;
import randoop.test.predicate.ExceptionBehaviorPredicate;
import randoop.test.predicate.ExceptionPredicate;
import randoop.util.RecordListReader;
import randoop.util.RecordProcessor;
import randoop.util.Util;

import junit.framework.TestCase;

public class SequenceTests extends TestCase {

  /**
   * Tests the sequence execution and code generation aspects of Randoop.
   *
   * Reads in a file describing a collection of sequences along with the test
   * code that Randoop should generate after executing them. The file consists
   * of a collection of records, each of the form:
   *
   * START TEST
   * TEST_ID
   * <string identifying this test for debugging>
   * SEQUENCE
   * <parseable sequence description>
   * EXPECTED_CODE
   * <Expected Java code resulting from sequence execution>
   * END RECORD
   *
   * Each sequence is parsed, then two checks performed:
   *
   * 1. (Test parsing code) s.toParseableString() can be parsed back into an equivalent sequence
   * 2. (Test execution and test generation code) sequence is executed and the resulting test code is
   *    compared with the expected code from the "EXPECTED_CODE" field in the record.
   */
  public void test1() throws Exception {

    RecordProcessor processor = new RecordProcessor() {
      @Override
      public void processRecord(List<String> lines) {

        parseRecord(lines);

      }
    };

    RecordListReader reader = new RecordListReader("TEST", processor);
    InputStream stream = SequenceTests.class.getResourceAsStream("resources/sequence_tests_script.txt");
    BufferedReader b = new BufferedReader(new InputStreamReader(stream));
    reader.parse(b);
  }


  /**
   * The "default" set of visitors that Randoop uses during execution.
   */
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
        new RegressionCaptureVisitor(expectation,true));
  }

  // See http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4094886
  private static void parseRecord(List<String> lines) {

    String testId = null;
    if (!lines.get(0).equals("TEST_ID")) {
      throw new RuntimeException("Malformed test record (does not have a \"TEST_ID\" field): " + lines.toString());
    }
    testId = lines.get(1);

    if (!lines.get(2).equals("SEQUENCE")) {
      throw new RuntimeException("Malformed test record (does not have a \"SEQUENCE\" field): " + lines.toString());
    }

    int currIdx = 3;
    List<String> sequenceLines = new ArrayList<String>();
    while (currIdx < lines.size() && !lines.get(currIdx).equals("EXPECTED_CODE")) {
      sequenceLines.add(lines.get(currIdx));
      currIdx++;
    }
    if (currIdx == lines.size()) {
      throw new IllegalArgumentException("Malformed test record (missing \"EXPECTED_CODE\" record): " + lines.toString());
    }
    if (sequenceLines.size() == 0) {
      throw new IllegalArgumentException("Empty sequence found.");
    }

    currIdx++;
    List<String> expectedCode = new ArrayList<String>();
    while (currIdx < lines.size()) {
      expectedCode.add(lines.get(currIdx));
      currIdx++;
    }

    if (expectedCode.size() == 0) {
      throw new IllegalArgumentException("Expected code is empty.");
    }

    Sequence sequence;
    try {
      sequence = Sequence.parse(sequenceLines);
    } catch (SequenceParseException e) {
      throw new RuntimeException(e);
    }

    checkListsEqual(sequenceLines, Arrays.asList(sequence.toParseableString().split(Globals.lineSep)), testId);

    ExecutableSequence ds = new ExecutableSequence(sequence);
    ds.execute(new DummyVisitor(), testGen);
    checkListsEqual(expectedCode, Arrays.asList(ds.toCodeString().split(Globals.lineSep)), testId);
  }

  private static void checkListsEqual(List<String> expected, List<String> actual, String testId) {

    expected = trimmedLines(expected);
    actual = trimmedLines(actual);

    if (expected.size() != actual.size()) {
      fail(failureMessage(testId, "List lengths differ: expected " + expected.size() + " but got " + actual.size(), expected, actual));
    }

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(failureMessage(testId, "(lists differ at index " + i + ")", expected, actual), expected.get(i), actual.get(i));
    }
  }

  private static String failureMessage(String testId, String msg, List<String> expected, List<String> actual) {
    StringBuilder b = new StringBuilder();
    b.append("Failure in test " + testId + ": " + msg + ".");
    b.append("" + Globals.lineSep + "Expected:" + Globals.lineSep + "");
    for (int i = 0 ; i < expected.size() ; i++)
      b.append(i + ": " + expected.get(i) + Util.newLine);
    b.append("" + Globals.lineSep + "Actual:" + Globals.lineSep + "");
    for (int i = 0 ; i < actual.size() ; i++) {
      b.append(i + ": " + actual.get(i) + Util.newLine);
    }
    // For debugging.
    //     for (int i = 0 ; i < actual.size() ; i++)
    //       System.out.println(actual.get(i));
    //     System.out.println();
    return b.toString();
  }

  // Skips empty lines.
  private static List<String> trimmedLines(List<String> l) {
    List<String> trimmed = new ArrayList<String>();
    for (int i = 0 ; i < l.size() ; i++) {
      String t = l.get(i).trim();
      if (t.isEmpty()) {
        continue;
      }
      trimmed.add(t);
    }
    return trimmed;
  }

}
