package randoop.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import randoop.ContractCheckingVisitor;
import randoop.EqualsHashcode;
import randoop.EqualsSymmetric;
import randoop.EqualsToItself;
import randoop.EqualsToNull;
import randoop.ExecutableSequence;
import randoop.ExecutionVisitor;
import randoop.Globals;
import randoop.MultiVisitor;
import randoop.ObjectContract;
import randoop.RecordListReader;
import randoop.RecordProcessor;
import randoop.RegressionCaptureVisitor;
import randoop.Sequence;
import randoop.SequenceParseException;
import randoop.util.Util;

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
      public void processRecord(List<String> lines) {
        
        parseRecord(lines);

      }
    };
    

    RecordListReader reader = new RecordListReader("TEST", processor);
    reader.parse(SequenceTests.class.getResource("resources/sequence_tests_script.txt").getFile());

  }
  

  /**
   * The "default" set of visitors that Randoop uses during execution. 
   */
  private static final List<ExecutionVisitor> visitors;
  static {
    List<ObjectContract> contracts = new ArrayList<ObjectContract>();
    contracts.add(new EqualsToItself());
    contracts.add(new EqualsToNull());
    contracts.add(new EqualsHashcode());
    contracts.add(new EqualsSymmetric());
    
    visitors = new ArrayList<ExecutionVisitor>();
    visitors.add(new ContractCheckingVisitor(contracts, false));
    visitors.add(new RegressionCaptureVisitor());
  }

  @SuppressWarnings("deprecation")
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

    StringBuilder errorMsg = new StringBuilder();
    
    checkListsEqual(sequenceLines, Arrays.asList(sequence.toParseableString().split(Globals.lineSep)), testId);
    
    ExecutableSequence ds = new ExecutableSequence(sequence);
    ds.execute(new MultiVisitor(visitors));
    checkListsEqual(expectedCode, Arrays.asList(ds.toCodeString().split(Globals.lineSep)), testId);
  }
  
  private static void checkListsEqual(List<String> expected, List<String> actual, String testId) {

    expected = trimmedLines(expected);
    actual = trimmedLines(actual);

    if (expected.size() != actual.size()) {
      fail(failureMessage(testId, "List lengths differ: expected " + expected.size() + " but got " + actual.size(), expected, actual));
    }

    for (int i = 0; i < expected.size(); i++) {
      Assert.assertEquals(failureMessage(testId, "(lists differ at index " + i + ")", expected, actual), expected.get(i), actual.get(i));
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
