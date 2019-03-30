package randoop.condition;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import randoop.util.Util;

public class SpecificationTranslatorTest {

  // cases: static method, non-static method, check return, parameters, and receiver

  @Test
  public void testPrintWriterAppend() {
    Method method = getPrintWriterAppendMethod();
    List<String> parameterList = new ArrayList<>();
    parameterList.add("c");
    Identifiers identifiers = new Identifiers(parameterList);
    OperationSpecification opSpec = new OperationSpecification(null, identifiers);
    SpecificationTranslator sig =
        SpecificationTranslator.createTranslator(
            method,
            opSpec,
            new SequenceCompiler(
                new SequenceClassLoader(getClass().getClassLoader()), new ArrayList<String>()));

    assertThat(
        "presignature is just receiver and parameters",
        sig.getPrestateExpressionDeclaration(),
        is(equalTo("(java.io.PrintWriter receiver, char c)")));
    assertThat(
        "postsignature is receiver, parameters and result",
        sig.getPoststateExpressionDeclarations(),
        is(equalTo("(java.io.PrintWriter receiver, char c, java.io.PrintWriter result)")));

    Map<String, String> replacements = sig.getReplacementMap();
    assertThat(
        "receiver should be x0", Util.replaceWords("receiver", replacements), is(equalTo("x0")));
    assertThat("param should be x1", Util.replaceWords("c", replacements), is(equalTo("x1")));
    assertThat("result should be x2", Util.replaceWords("result", replacements), is(equalTo("x2")));

    assertThat(
        "receiver and results should be replaced",
        Util.replaceWords("result.equals(receiver)", replacements),
        is(equalTo("x2.equals(x0)")));

    String conditionText = "result.equals(receiver)";
    Sequence sequence = createPrintWriterSequence(TypedOperation.forMethod(method));

    List<ExecutableBooleanExpression> postConditions = new ArrayList<>();
    Method conditionMethod =
        ExecutableBooleanExpression.createMethod(
            sig.getPoststateExpressionSignature(),
            sig.getPoststateExpressionDeclarations(),
            conditionText,
            sig.getCompiler());
    ExecutableBooleanExpression condition =
        new ExecutableBooleanExpression(
            conditionMethod,
            "returns this writer",
            Util.replaceWords(conditionText, sig.getReplacementMap()));
    postConditions.add(condition);
    List<Variable> inputList = new ArrayList<>(sequence.getInputs(sequence.size() - 1));
    inputList.add(sequence.getLastVariable());
    PostConditionCheck check = new PostConditionCheck(postConditions, inputList);

    assertThat("pre-statement should be empty", check.toCodeStringPreStatement(), is(equalTo("")));
    String expectedPost =
        "// Checks the post-condition: returns this writer\n"
            + "org.junit.Assert.assertTrue("
            + "\"Post-condition: returns this writer\", "
            + "printWriter3.equals(printWriter1));\n";
    assertThat("poststatement", check.toCodeStringPostStatement(), is(equalTo(expectedPost)));
  }

  private Method getPrintWriterAppendMethod() {
    Class<?> c = PrintWriter.class;
    Method method = null;
    try {
      method = c.getDeclaredMethod("append", char.class);
    } catch (NoSuchMethodException e) {
      throw new AssertionError("could not load PrintWriter.append(char)", e);
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

  private TypedOperation getPrintWriterConstructorOperation() {
    Class<?> c = PrintWriter.class;
    Constructor<?> constructor = null;
    try {
      constructor = c.getDeclaredConstructor(String.class);
    } catch (NoSuchMethodException e) {
      throw new AssertionError("could not load constructor", e);
    }
    return TypedOperation.forConstructor(constructor);
  }

  @Test
  public void testSignatureFromFile() {
    String specFileName = "test/randoop/condition/java-io-PrintWriter.json";
    Path specFile = Paths.get(specFileName);
    List<Path> specList = new ArrayList<>();
    specList.add(specFile);
    OperationSpecification specification = readSpecificationsForTest(specFile);
    Method method = getPrintWriterAppendMethod();
    SpecificationTranslator sig =
        SpecificationTranslator.createTranslator(method, specification, null);

    assertThat(
        "presignature is just receiver and parameters",
        sig.getPrestateExpressionDeclaration(),
        is(equalTo("(java.io.PrintWriter target, char c)")));
    assertThat(
        "postsignature is receiver, parameters and result",
        sig.getPoststateExpressionDeclarations(),
        is(equalTo("(java.io.PrintWriter target, char c, java.io.PrintWriter result)")));

    Map<String, String> replacements = sig.getReplacementMap();
    assertThat(
        "receiver should be x0", Util.replaceWords("target", replacements), is(equalTo("x0")));
    assertThat("param should be x1", Util.replaceWords("c", replacements), is(equalTo("x1")));
    assertThat("result should be x2", Util.replaceWords("result", replacements), is(equalTo("x2")));

    assertThat(
        "receiver and results should be replaced",
        Util.replaceWords("result.equals(target)", replacements),
        is(equalTo("x2.equals(x0)")));

    SpecificationCollection collection = SpecificationCollection.create(specList);
    ExecutableSpecification execSpec = collection.getExecutableSpecification(method);
    TypedClassOperation appendOp = TypedOperation.forMethod(method);
    appendOp.setExecutableSpecification(execSpec);
    Sequence sequence = createPrintWriterSequence(appendOp);
    ExecutableSequence eseq = new ExecutableSequence(sequence);
    eseq.execute(new DummyVisitor(), new DummyCheckGenerator());
    String expectedCode =
        "java.lang.String str0 = \"not-really-a-file\";\n"
            + "java.io.PrintWriter printWriter1 = new java.io.PrintWriter(str0);\n"
            + "char char2 = 'a';\n"
            + "java.io.PrintWriter printWriter3 = printWriter1.append(char2);\n"
            + "// Checks the post-condition: This writer\n"
            + "org.junit.Assert.assertTrue(\"Post-condition: This writer\", printWriter3.equals(printWriter1));\n\n";
    assertThat("sequence code", eseq.toCodeString(), is(equalTo(expectedCode)));
  }

  @SuppressWarnings("unchecked")
  private OperationSpecification readSpecificationsForTest(Path specFile) {
    List<OperationSpecification> specificationList = new ArrayList<>();
    Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    TypeToken<List<OperationSpecification>> typeToken =
        (new TypeToken<List<OperationSpecification>>() {});
    try (BufferedReader reader = Files.newBufferedReader(specFile, UTF_8)) {
      specificationList.addAll(
          (List<OperationSpecification>) gson.fromJson(reader, typeToken.getType()));
    } catch (FileNotFoundException e) {
      throw new AssertionError("could not find spec file", e);
    } catch (IOException e) {
      throw new AssertionError("exception while loading spec file", e);
    }
    assertThat("spec file has 8 specs", specificationList.size(), is(equalTo(8)));
    assertThat(
        "8th is right one",
        specificationList.get(7).getOperation().getName(),
        is(equalTo("append")));
    return specificationList.get(7);
  }
}
