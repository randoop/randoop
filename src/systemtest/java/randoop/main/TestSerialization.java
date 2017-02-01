package randoop.main;

import org.junit.Test;
import randoop.DummyVisitor;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.test.DummyCheckGenerator;
import randoop.types.*;
import randoop.util.SimpleList;

import javax.swing.plaf.nimbus.State;
import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests serialization from files and deserialization by isolating the deserialization in a different JVM process
 */
public class TestSerialization {

  private static String FILE_PATH = "src/systemtest/resources/serialized_sequence.txt";

  private void writeToFile(ByteArrayOutputStream out) throws IOException {
    File file = new File(FILE_PATH);

    if (!file.exists()) {
      file.createNewFile();
    }

    byte[] contentInBytes = out.toByteArray();

    try (FileOutputStream fop = new FileOutputStream(file)) {
      fop.write(contentInBytes);
      fop.flush();
      fop.close();
    }
  }

  /**
   * Tests the serialization mechanisms of Randoop by serializing and de-serializing an Executable Sequence
   *
   */
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

  private ExecutableSequence getExecutionFileFromFile() throws IOException, ClassNotFoundException {
    ExecutableSequence es = null;
    ObjectInputStream ois = null;
    FileInputStream fin = null;
    try {
      fin = new FileInputStream(FILE_PATH);
      ois = new ObjectInputStream(fin);
      Object des = ois.readObject();
      es = (ExecutableSequence) des;
    } finally {
      if (fin != null) {
        fin.close();
      }
      if (ois != null) {
        ois.close();
      }
    }
    return es;
  }

  @Test
  public void testSerializationToFile()
      throws NoSuchMethodException, IOException, ClassNotFoundException {
    final int SIZE_STATEMENT_INDEX = 3;
    ExecutableSequence es = createExecutableSequenceForSerialization();
    //es.execute(new DummyVisitor(), new DummyCheckGenerator());
    // ExecutionOutcome result = es.getResult(SIZE_STATEMENT_INDEX);

    // Object valueBeforeSerialization = ((NormalExecution) result).getRuntimeValue();
    String codeBeforeSerialization = es.toCodeString();

    ByteArrayOutputStream out = getSerializedExecutableSequence(es);
    writeToFile(out);

    //        if (result instanceof NormalExecution) {
    //
    //        } else {
    //            fail("Sequence not resulting in normal execution.");
    //        }
  }

  @Test
  public void testDeserializationFromFile()
      throws NoSuchMethodException, IOException, ClassNotFoundException {
    final int SIZE_STATEMENT_INDEX = 3;

    ExecutableSequence actual = getExecutionFileFromFile();

    if (actual == null) {
      fail("Deserialezed to null sequence.");
    } else {
      ExecutableSequence expected = createExecutableSequenceForSerialization();

      actual.execute(new DummyVisitor(), new DummyCheckGenerator());
      ExecutionOutcome result2 = actual.getResult(SIZE_STATEMENT_INDEX);

      if (result2 instanceof NormalExecution) {
        Object valueAfterSerialization = ((NormalExecution) result2).getRuntimeValue();

        SimpleList<Statement> expectedStatements = expected.sequence.statements;
        SimpleList<Statement> actualStatements = actual.sequence.statements;

        if (actualStatements.size() != expectedStatements.size()) {
          fail("Deserialized sequence does not contain all the original statements.");
        } else {
          for (int i = 0; i < expectedStatements.size(); i++) {
            Statement es = expectedStatements.get(i);
            Statement as = actualStatements.get(i);

            assertTrue(es.equals(as));
          }
        }
        // assertEquals(1, valueAfterSerialization);
        // assertEquals(esDeserialized.toCodeString(), expected.toCodeString());
      } else {
        fail("Deserialized sequence not resulting in normal execution.");
      }
    }
  }

  /***
   * Produces the following sequence found on Randoop Developer's manual :
   *  LinkedList<String> l = new LinkedList<>();
   * String str = "hi!";
   * l.addFirst(str);
   * int i = l.size();
   */
  private Sequence createSequence() throws NoSuchMethodException {
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

  private ExecutableSequence createExecutableSequenceForSerialization()
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

  public ExecutableSequence getDeserializedExecutableSequence(ByteArrayInputStream input)
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
