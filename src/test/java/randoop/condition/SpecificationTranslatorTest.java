package randoop.condition;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
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
        SpecificationTranslator.createTranslator(method, opSpec, new SequenceCompiler());

    assertEquals(
        "presignature is just receiver and parameters",
        "(java.io.PrintWriter receiver, char c)",
        sig.getPrestateExpressionDeclaration());
    assertEquals(
        "postsignature is receiver, parameters and result",
        "(java.io.PrintWriter receiver, char c, java.io.PrintWriter result)",
        sig.getPoststateExpressionDeclarations());

    Map<String, String> replacements = sig.getReplacementMap();
    assertEquals("receiver should be x0", "x0", Util.replaceWords("receiver", replacements));
    assertEquals("param should be x1", "x1", Util.replaceWords("c", replacements));
    assertEquals("result should be x2", "x2", Util.replaceWords("result", replacements));

    assertEquals(
        "receiver and results should be replaced",
        "x2.equals(x0)",
        Util.replaceWords("result.equals(receiver)", replacements));

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

    assertEquals("pre-statement should be empty", "", check.toCodeStringPreStatement());
    String expectedPost =
        "// Checks the post-condition: returns this writer\n"
            + "org.junit.Assert.assertTrue("
            + "\"Post-condition: returns this writer\", "
            + "printWriter3.equals(printWriter1));\n";
    assertEquals("poststatement", expectedPost, check.toCodeStringPostStatement());
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

    assertEquals(
        "presignature is just receiver and parameters",
        "(java.io.PrintWriter target, char c)",
        sig.getPrestateExpressionDeclaration());
    assertEquals(
        "postsignature is receiver, parameters and result",
        "(java.io.PrintWriter target, char c, java.io.PrintWriter result)",
        sig.getPoststateExpressionDeclarations());

    Map<String, String> replacements = sig.getReplacementMap();
    assertEquals("receiver should be x0", "x0", Util.replaceWords("target", replacements));
    assertEquals("param should be x1", "x1", Util.replaceWords("c", replacements));
    assertEquals("result should be x2", "x2", Util.replaceWords("result", replacements));

    assertEquals(
        "receiver and results should be replaced",
        "x2.equals(x0)",
        Util.replaceWords("result.equals(target)", replacements));

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
    assertEquals("sequence code", expectedCode, eseq.toCodeString());
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
    assertEquals("spec file has 8 specs", 8, specificationList.size());
    assertEquals("8th is right one", "append", specificationList.get(7).getOperation().getName());
    return specificationList.get(7);
  }
}
