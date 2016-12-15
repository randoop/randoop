package randoop.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.*;

import randoop.DummyVisitor;
import randoop.ExecutionOutcome;
import randoop.Globals;
import randoop.NormalExecution;
import randoop.contract.EqualsHashcode;
import randoop.contract.EqualsReflexive;
import randoop.contract.EqualsSymmetric;
import randoop.contract.EqualsToNullRetFalse;
import randoop.main.GenInputsAbstract;
import randoop.main.GenInputsAbstract.BehaviorType;
import randoop.main.OptionsCache;
import randoop.operation.TypedOperation;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceParseException;
import randoop.test.predicate.ExceptionBehaviorPredicate;
import randoop.test.predicate.ExceptionPredicate;
import randoop.types.*;
import randoop.util.MultiMap;
import randoop.util.RecordListReader;
import randoop.util.RecordProcessor;
import randoop.util.Util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/*
Note: I disabled this test in the build script because the test-script includes the use of
ArrayList as a rawtype. Capturing operations using rawtypes conflicts with the use of parsing to
add methods when populating the model, which recognizes that the type is generic and adds it to the
pool of types to be instantiated. Since this test is the only reason for parsing sequences,
and we aren't trying to parse parameterized types, I'm kicking this down the road.
*/

public class SequenceTests {

  private static OptionsCache optionsCache;

  @BeforeClass
  public static void setup() {
    optionsCache = new OptionsCache();
    optionsCache.saveState();
  }

  @AfterClass
  public static void restore() {
    optionsCache.restoreState();
  }

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
   * 1. (Test parsing code) s.toParsableString() can be parsed back into an equivalent sequence
   * 2. (Test execution and test generation code) sequence is executed and the resulting test code is
   *    compared with the expected code from the "EXPECTED_CODE" field in the record.
   */
  @Test
  public void test1() throws Exception {

    RecordProcessor processor =
        new RecordProcessor() {
          @Override
          public void processRecord(List<String> lines) {

            parseRecord(lines);
          }
        };

    RecordListReader reader = new RecordListReader("TEST", processor);
    InputStream stream = SequenceTests.class.getResourceAsStream("/sequence_tests_script.txt");
    BufferedReader b = new BufferedReader(new InputStreamReader(stream));
    reader.parse(b);
  }

  /**
   * The "default" set of visitors that Randoop uses during execution.
   */
  private static final TestCheckGenerator testGen;

  static {
    ContractSet contracts = new ContractSet();
    contracts.add(EqualsReflexive.getInstance());
    contracts.add(EqualsToNullRetFalse.getInstance());
    contracts.add(EqualsHashcode.getInstance());
    contracts.add(EqualsSymmetric.getInstance());

    GenInputsAbstract.unchecked_exception = BehaviorType.EXPECTED;
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ExceptionPredicate isExpected = new ExceptionBehaviorPredicate(BehaviorType.EXPECTED);
    ExpectedExceptionCheckGen expectation;
    expectation = new ExpectedExceptionCheckGen(visibility, isExpected);
    testGen =
        new ExtendGenerator(
            new ContractCheckingVisitor(
                contracts, new ExceptionBehaviorPredicate(BehaviorType.ERROR)),
            new RegressionCaptureVisitor(
                expectation,
                new MultiMap<Type, TypedOperation>(),
                new LinkedHashSet<TypedOperation>(),
                true));
  }

  // See http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4094886
  private static void parseRecord(List<String> lines) {

    String testId;
    if (!lines.get(0).equals("TEST_ID")) {
      throw new RuntimeException(
          "Malformed test record (does not have a \"TEST_ID\" field): " + lines.toString());
    }
    testId = lines.get(1);

    if (!lines.get(2).equals("SEQUENCE")) {
      throw new RuntimeException(
          "Malformed test record (does not have a \"SEQUENCE\" field): " + lines.toString());
    }

    int currIdx = 3;
    List<String> sequenceLines = new ArrayList<>();
    while (currIdx < lines.size() && !lines.get(currIdx).equals("EXPECTED_CODE")) {
      sequenceLines.add(lines.get(currIdx));
      currIdx++;
    }
    if (currIdx == lines.size()) {
      throw new IllegalArgumentException(
          "Malformed test record (missing \"EXPECTED_CODE\" record): " + lines.toString());
    }
    if (sequenceLines.size() == 0) {
      throw new IllegalArgumentException("Empty sequence found.");
    }

    currIdx++;
    List<String> expectedCode = new ArrayList<>();
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

    checkListsEqual(
        sequenceLines, Arrays.asList(sequence.toParsableString().split(Globals.lineSep)), testId);

    ExecutableSequence ds = new ExecutableSequence(sequence);
    ds.execute(new DummyVisitor(), testGen);
    checkListsEqual(expectedCode, Arrays.asList(ds.toCodeString().split(Globals.lineSep)), testId);
  }

  private static void checkListsEqual(List<String> expected, List<String> actual, String testId) {

    expected = trimmedLines(expected);
    actual = trimmedLines(actual);

    if (expected.size() != actual.size()) {
      fail(
          failureMessage(
              testId,
              "List lengths differ: expected " + expected.size() + " but got " + actual.size(),
              expected,
              actual));
    }

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(
          failureMessage(testId, "(lists differ at index " + i + ")", expected, actual),
          expected.get(i),
          actual.get(i));
    }
  }

  private static String failureMessage(
      String testId, String msg, List<String> expected, List<String> actual) {
    StringBuilder b = new StringBuilder();
    b.append("Failure in test ").append(testId).append(": ").append(msg).append(".");
    b.append("").append(Globals.lineSep).append("Expected:").append(Globals.lineSep).append("");
    for (int i = 0; i < expected.size(); i++) {
      b.append(i).append(": ").append(expected.get(i)).append(Util.newLine);
    }
    b.append("").append(Globals.lineSep).append("Actual:").append(Globals.lineSep).append("");
    for (int i = 0; i < actual.size(); i++) {
      b.append(i).append(": ").append(actual.get(i)).append(Util.newLine);
    }
    return b.toString();
  }

  // Skips empty lines.
  private static List<String> trimmedLines(List<String> list) {
    List<String> trimmed = new ArrayList<>();
    for (String str : list) {
      String t = str.trim();
      if (!t.isEmpty()) {
        trimmed.add(t);
      }
    }
    return trimmed;
  }

  /**
   * Tests the serialization mechanisms of Randoop by serializing and de-serializing an Executable Sequence
   *
   */
  @Test
  public void testSerialization()
      throws NoSuchMethodException, IOException, ClassNotFoundException {
    final int SIZE_STATEMENT_INDEX = 3;
    ExecutableSequence es = createExecutableSequenceForSerialization();
    es.execute(new DummyVisitor(), new DummyCheckGenerator());
    ExecutionOutcome result = es.getResult(SIZE_STATEMENT_INDEX);

    if (result instanceof NormalExecution) {
      Object valueBeforeSerialization = ((NormalExecution) result).getRuntimeValue();
      String codeBeforeSerialization = es.toCodeString();

      ByteArrayOutputStream out = getSerializedExecutableSequence(es);
      ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

      ExecutableSequence esDeserialized = getDeserializedExecutableSequence(in);

      if (esDeserialized == null) {
        fail("Deserialezed to null sequence.");
      }

      esDeserialized.execute(new DummyVisitor(), new DummyCheckGenerator());
      ExecutionOutcome result2 = esDeserialized.getResult(SIZE_STATEMENT_INDEX);

      if (result2 instanceof NormalExecution) {
        Object valueAfterSerialization = ((NormalExecution) result2).getRuntimeValue();
        String codeAfterSerialization = es.toCodeString();

        assertEquals(valueBeforeSerialization, valueAfterSerialization);
        assertEquals(codeBeforeSerialization, codeAfterSerialization);
      } else {
        fail("Deserialized sequence not resulting in normal execution.");
      }

    } else {
      fail("Sequence not resulting in normal execution.");
    }
  }

  /***
   * Produces the following sequence found on Randoop Developer's manual :
   *  LinkedList<String> l = new LinkedList<>();
   * String str = "hi!";
   * l.addFirst(str);
   * int i = l.size();
   */
  private static Sequence createSequence() throws NoSuchMethodException {
    Sequence s = new Sequence();

    InstantiatedType linkedListType = JDKTypes.LINKED_LIST_TYPE.instantiate(JavaTypes.STRING_TYPE);
    Substitution<ReferenceType> substLL = linkedListType.getTypeSubstitution();
    TypedOperation newLL =
        TypedOperation.forConstructor(LinkedList.class.getConstructor()).apply(substLL);

    TypedOperation newLiteral =
        TypedOperation.createPrimitiveInitialization(JavaTypes.STRING_TYPE, "hi!");
    TypedOperation addFirst =
        TypedOperation.forMethod(LinkedList.class.getMethod("addFirst", Object.class))
            .apply(substLL);
    TypedOperation size =
        TypedOperation.forMethod(LinkedList.class.getMethod("size")).apply(substLL);

    InstantiatedType treeSetType = JDKTypes.TREE_SET_TYPE.instantiate(JavaTypes.STRING_TYPE);
    Substitution<ReferenceType> substTS = treeSetType.getTypeSubstitution();
    TypedOperation wcTS =
        TypedOperation.forConstructor(TreeSet.class.getConstructor(Collection.class))
            .apply(substTS)
            .applyCaptureConversion();
    Substitution<ReferenceType> substWC =
        Substitution.forArgs(wcTS.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    TypedOperation newTS = wcTS.apply(substWC);
    TypedOperation syncA =
        TypedOperation.forMethod(Collections.class.getMethod("synchronizedSet", Set.class));
    Substitution<ReferenceType> substA =
        Substitution.forArgs(syncA.getTypeParameters(), (ReferenceType) JavaTypes.STRING_TYPE);
    TypedOperation syncS = syncA.apply(substA);

    s = s.extend(newLL);
    s = s.extend(newLiteral);
    s = s.extend(addFirst, s.getVariable(0), s.getVariable(1));
    s = s.extend(size, s.getVariable(0));
    s = s.extend(newTS, s.getVariable(0));
    s = s.extend(syncS, s.getVariable(4));

    return s;
  }

  private static ExecutableSequence createExecutableSequenceForSerialization()
      throws NoSuchMethodException {
    Sequence sampleSeq = createSequence();

    ExecutableSequence es = new ExecutableSequence(sampleSeq);

    return es;
  }

  public static ByteArrayOutputStream getSerializedExecutableSequence(ExecutableSequence es)
      throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    ObjectOutput out;
    try {
      out = new ObjectOutputStream(bos);
      out.writeObject(es);
      out.flush();
    } finally {
      bos.close();
    }

    return bos;
  }

  public static ExecutableSequence getDeserializedExecutableSequence(ByteArrayInputStream input)
      throws IOException, ClassNotFoundException {
    ExecutableSequence es = null;

    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(input);
      Object des = ois.readObject();
      es = (ExecutableSequence) des;
    } finally {
      if (input != null) {
        input.close();
      }
      if (ois != null) {
        ois.close();
      }
    }

    return es;
  }
}
