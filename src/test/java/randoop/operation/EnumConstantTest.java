package randoop.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import org.junit.Test;
import randoop.NormalExecution;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.NonParameterizedType;
import randoop.types.TypeTuple;

/** EnumConstantTest defines unit tests for {@link randoop.operation.EnumConstant}. */
public class EnumConstantTest {

  @Test
  public void parseConstraint() {
    String enumPair = "randoop.operation.SimpleEnumForTests:THREE";
    try {
      TypedOperation ec = EnumConstant.parse(enumPair);
      assertEquals(enumPair, ec.toParsableString());
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  @Test
  public void parseErrors() {

    String missingColon = "randoop.operation.SimpleEnumForTestsTHREE";
    String missingType = ":THREE";
    String missingValue = "randoop.operation.SimpleEnumForTests:";
    String spaceInType = "Simple EnumForTests:THREE";
    String spaceInValue = "randoop.operation.SimpleEnumForTests:THRE E";
    String badType = "SEFT:THREE";
    String badValue = "randoop.operation.SimpleEnumForTests:FOUR";
    String nonEnum = "randoop.operation.EnumConstantTest:FIVE";

    try {
      EnumConstant.parse(missingColon);
      fail("Expected OperationParseException to be thrown");
    } catch (OperationParseException e) {
      String msg =
          "An enum constant description must be of the form \""
              + "<type>:<value>"
              + " but description is \""
              + missingColon
              + "\".";
      assertEquals(msg, e.getMessage());
    }

    String errorPrefix1 = "Error when parsing type-value pair ";
    String errorPrefix2 = " for an enum description of the form <type>:<value>.";

    try {
      EnumConstant.parse(missingType);
      fail("Expected OperationParseException to be thrown");
    } catch (OperationParseException e) {
      String msg = errorPrefix1 + missingType + errorPrefix2 + " No type given.";
      assertEquals(msg, e.getMessage());
    }

    try {
      EnumConstant.parse(missingValue);
      fail("Expected OperationParseException to be thrown");
    } catch (OperationParseException e) {
      String msg = errorPrefix1 + missingValue + errorPrefix2 + " No value given.";
      assertEquals(msg, e.getMessage());
    }

    try {
      EnumConstant.parse(spaceInType);
      fail("Expected OperationParseException to be thrown");
    } catch (OperationParseException e) {
      String msg =
          errorPrefix1
              + spaceInType
              + errorPrefix2
              + " The type has unexpected whitespace characters.";
      assertEquals(msg, e.getMessage());
    }

    try {
      EnumConstant.parse(spaceInValue);
      fail("Expected OperationParseException to be thrown");
    } catch (OperationParseException e) {
      String msg =
          errorPrefix1
              + spaceInValue
              + errorPrefix2
              + " The value has unexpected whitespace characters.";
      assertEquals(msg, e.getMessage());
    }

    try {
      EnumConstant.parse(badType);
      fail("Expected OperationParseException to be thrown");
    } catch (OperationParseException e) {
      String msg =
          errorPrefix1 + badType + errorPrefix2 + " The type given \"SEFT\" was not recognized.";
      assertEquals(msg, e.getMessage());
    }
    try {
      EnumConstant.parse(badValue);
      fail("Expected OperationParseException to be thrown");
    } catch (OperationParseException e) {
      String msg =
          errorPrefix1
              + badValue
              + errorPrefix2
              + " The value given \"FOUR\" is not a constant of the enum randoop.operation.SimpleEnumForTests.";
      assertEquals(msg, e.getMessage());
    }
    try {
      EnumConstant.parse(nonEnum);
      fail("Expected OperationParseException to be thrown");
    } catch (OperationParseException e) {
      String msg =
          errorPrefix1
              + nonEnum
              + errorPrefix2
              + " The type given \"randoop.operation.EnumConstantTest\" is not an enum.";
      assertEquals(msg, e.getMessage());
    }
  }

  @Test
  public void testInheritedMethods() {
    // skipping reflection
    ClassOrInterfaceType enumType = new NonParameterizedType(SimpleEnumForTests.class);
    TypedOperation ec1 =
        new TypedClassOperation(
            new EnumConstant(SimpleEnumForTests.ONE), enumType, new TypeTuple(), enumType);
    TypedOperation ec1_2 =
        new TypedClassOperation(
            new EnumConstant(SimpleEnumForTests.ONE), enumType, new TypeTuple(), enumType);
    TypedOperation ec2 =
        new TypedClassOperation(
            new EnumConstant(SimpleEnumForTests.TWO), enumType, new TypeTuple(), enumType);

    // equals and hashcode
    assertEquals(ec1, ec1_2);
    assertEquals(ec1.hashCode(), ec1_2.hashCode());

    assertNotEquals(ec1, ec2);

    // types
    assertTrue(ec1.getInputTypes().isEmpty());
    assertEquals(
        new NonParameterizedType(SimpleEnumForTests.ONE.getDeclaringClass()), ec1.getOutputType());

    // Execution
    NormalExecution exec = new NormalExecution(SimpleEnumForTests.ONE, 0);
    NormalExecution actual = (NormalExecution) ec1.execute(new Object[0]);
    assertEquals(actual.getRuntimeValue(), exec.getRuntimeValue());
    assertEquals(actual.getExecutionTime(), exec.getExecutionTime());

    // code generation
    // need a sequence where variable lives
    String expected =
        "randoop.operation.SimpleEnumForTests simpleEnumForTests0 = randoop.operation.SimpleEnumForTests.TWO;";
    Statement st = new Statement(ec2);
    Sequence seq = new Sequence().extend(ec2, new ArrayList<Variable>());
    Variable var = new Variable(seq, 0);
    StringBuilder b = new StringBuilder();
    st.appendCode(var, new ArrayList<Variable>(), b);
    assertEquals(expected, b.toString());
  }
}
