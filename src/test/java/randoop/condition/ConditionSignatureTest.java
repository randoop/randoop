package randoop.condition;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import randoop.DummyVisitor;
import randoop.compile.SequenceClassLoader;
import randoop.compile.SequenceCompiler;
import randoop.condition.specification.Identifiers;
import randoop.condition.specification.OperationSpecification;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.test.DummyCheckGenerator;
import randoop.test.PostConditionCheck;
import randoop.types.JavaTypes;

public class ConditionSignatureTest {

  //cases: static method, non-static method, check return, parameters, and receiver

  @Test
  public void testPrintWriterAppend() {
    Method method = getPrintWriterAppendMethod();
    List<String> parameterList = new ArrayList<>();
    parameterList.add("c");
    Identifiers identifiers = new Identifiers(parameterList);
    ConditionSignature sig = ConditionSignature.create(method, identifiers);

    assertThat(
        "presignature is just receiver and parameters",
        sig.getPreConditionSignature(),
        is(equalTo("(java.io.PrintWriter receiver, char c)")));
    assertThat(
        "postsignature is receiver, parameters and result",
        sig.getPostConditionSignature(),
        is(equalTo("(java.io.PrintWriter receiver, char c, java.io.PrintWriter result)")));

    assertThat(
        "receiver should be x0", sig.replaceWithDummyVariables("receiver"), is(equalTo("x0")));
    assertThat("param should be x1", sig.replaceWithDummyVariables("c"), is(equalTo("x1")));
    assertThat("result should be x2", sig.replaceWithDummyVariables("result"), is(equalTo("x2")));

    assertThat(
        "receiver and results should be replaced",
        sig.replaceWithDummyVariables("result.equals(receiver)"),
        is(equalTo("x2.equals(x0)")));

    String conditionText = "result.equals(receiver)";
    Sequence sequence = createPrintWriterSequence(TypedOperation.forMethod(method));
    PostConditionCheck check = createCheck(sequence, sig, conditionText);
    assertThat("pre-statement should be empty", check.toCodeStringPreStatement(), is(equalTo("")));
    String expectedPost =
        "// Checks the post-condition: returns this writer\n"
            + "org.junit.Assert.assertTrue( "
            + "\"Post-condition: returns this writer\","
            + "printWriter3.equals(printWriter1));\n";
    assertThat("poststatement", check.toCodeStringPostStatement(), is(equalTo(expectedPost)));
  }

  private PostConditionCheck createCheck(
      Sequence sequence, ConditionSignature sig, String conditionText) {
    List<PostCondition> postConditions = new ArrayList<>();
    PostCondition condition = createPostCondition(sig, conditionText);
    postConditions.add(condition);

    List<Variable> inputList = new ArrayList<>(sequence.getInputs(sequence.size() - 1));
    inputList.add(sequence.getLastVariable());

    return new PostConditionCheck(postConditions, inputList);
  }

  private Method getPrintWriterAppendMethod() {
    Class<?> c = PrintWriter.class;
    Method method = null;
    try {
      method = c.getDeclaredMethod("append", char.class);
    } catch (NoSuchMethodException e) {
      fail("could not load PrintWriter.append(char)");
    }
    return method;
  }

  private Sequence createPrintWriterSequence(TypedClassOperation appendOp) {
    /*
    PrintWriter pw = new PrintWriter("not-really-a-file");
    PrintWriter pw1 = pw.append('a');
    assertTrue(pw1.equals(pw));
    */

    Sequence sequence = new Sequence();
    try {
      sequence =
          sequence.extend(
              TypedOperation.createPrimitiveInitialization(
                  JavaTypes.STRING_TYPE, "not-really-a-file"));
      sequence = sequence.extend(getPrintWriterConstructorOperation(), sequence.getLastVariable());
      sequence =
          sequence.extend(TypedOperation.createPrimitiveInitialization(JavaTypes.CHAR_TYPE, 'a'));
      sequence = sequence.extend(appendOp, sequence.getVariable(1), sequence.getLastVariable());
    } catch (IllegalArgumentException e) {
      fail("bad sequence construction: " + e);
    }

    return sequence;
  }

  private PostCondition createPostCondition(ConditionSignature sig, String conditionText) {
    Method conditionMethod = null;
    SequenceCompiler compiler =
        new SequenceCompiler(
            new SequenceClassLoader(getClass().getClassLoader()), new ArrayList<String>());

    conditionMethod =
        ConditionMethodCreator.create(
            sig.getPackageName(), sig.getPostConditionSignature(), conditionText, compiler);
    String comment = "returns this writer";
    String postConditionText = sig.replaceWithDummyVariables(conditionText);
    return new PostCondition(conditionMethod, comment, postConditionText);
  }

  private TypedOperation getPrintWriterConstructorOperation() {
    Class<?> c = PrintWriter.class;
    Constructor<?> constructor = null;
    try {
      constructor = c.getDeclaredConstructor(String.class);
    } catch (NoSuchMethodException e) {
      fail("could not load constructor");
    }
    return TypedOperation.forConstructor(constructor);
  }

  @Test
  public void testSignatureFromFile() {
    String specFileName = "test/randoop/condition/java-io-PrintWriter.json";
    File specFile = new File(specFileName);
    List<File> specList = new ArrayList<>();
    specList.add(specFile);
    OperationSpecification specification = readSpecifications(specFile);
    Method method = getPrintWriterAppendMethod();
    ConditionSignature sig = ConditionSignature.create(method, specification.getIdentifiers());

    assertThat(
        "presignature is just receiver and parameters",
        sig.getPreConditionSignature(),
        is(equalTo("(java.io.PrintWriter target, char c)")));
    assertThat(
        "postsignature is receiver, parameters and result",
        sig.getPostConditionSignature(),
        is(equalTo("(java.io.PrintWriter target, char c, java.io.PrintWriter result)")));

    assertThat("receiver should be x0", sig.replaceWithDummyVariables("target"), is(equalTo("x0")));
    assertThat("param should be x1", sig.replaceWithDummyVariables("c"), is(equalTo("x1")));
    assertThat("result should be x2", sig.replaceWithDummyVariables("result"), is(equalTo("x2")));

    assertThat(
        "receiver and results should be replaced",
        sig.replaceWithDummyVariables("result.equals(target)"),
        is(equalTo("x2.equals(x0)")));

    SpecificationCollection collection = SpecificationCollection.create(specList);
    OperationConditions condition = collection.getOperationConditions(method);
    TypedClassOperation appendOp = TypedOperation.forMethod(method);
    appendOp.addConditions(condition);
    Sequence sequence = createPrintWriterSequence(appendOp);
    ExecutableSequence eseq = new ExecutableSequence(sequence);
    eseq.execute(new DummyVisitor(), new DummyCheckGenerator());
    String expectedCode =
        "java.lang.String str0 = \"not-really-a-file\";\n"
            + "java.io.PrintWriter printWriter1 = new java.io.PrintWriter(str0);\n"
            + "char char2 = 'a';\n"
            + "java.io.PrintWriter printWriter3 = printWriter1.append(char2);\n"
            + "// Checks the post-condition: This writer\n"
            + "org.junit.Assert.assertTrue( \"Post-condition: This writer\",printWriter3.equals(printWriter1));\n\n";
    assertThat("sequence code", eseq.toCodeString(), is(equalTo(expectedCode)));
  }

  @SuppressWarnings("unchecked")
  private OperationSpecification readSpecifications(File specFile) {
    List<OperationSpecification> specificationList = new ArrayList<>();
    Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    TypeToken<List<OperationSpecification>> typeToken =
        (new TypeToken<List<OperationSpecification>>() {});
    try (BufferedReader reader = new BufferedReader(new FileReader(specFile))) {
      specificationList.addAll(
          (List<OperationSpecification>) gson.fromJson(reader, typeToken.getType()));
    } catch (FileNotFoundException e) {
      fail("could not find spec file");
    } catch (IOException e) {
      fail("exception while loading spec file");
    }
    assertThat("spec file has 8 specs", specificationList.size(), is(equalTo(8)));
    assertThat(
        "8th is right one",
        specificationList.get(7).getOperation().getName(),
        is(equalTo("append")));
    return specificationList.get(7);
  }
}
