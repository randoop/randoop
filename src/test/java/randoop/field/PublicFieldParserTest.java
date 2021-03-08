package randoop.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import randoop.operation.OperationParseException;
import randoop.types.Type;

public class PublicFieldParserTest {

  @Test
  public void parseConstraintInstance() {
    String fieldPair = "randoop.field.ClassWithFields.oneField";
    String classname = "randoop.field.ClassWithFields";
    String fieldname = "oneField";
    Type classType;
    try {
      classType = Type.forName(classname);
    } catch (ClassNotFoundException e) {
      fail("couldn't load class: " + e.getMessage());
      throw new Error("unreachable");
    }
    try {
      AccessibleField pf = FieldParser.parse(fieldPair, classname, fieldname);
      assertEquals(fieldPair, pf.toParsableString(classType));
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }

  @Test
  public void parseConstraintStatic() {
    String fieldPair = "randoop.field.ClassWithFields.fourField";
    String classname = "randoop.field.ClassWithFields";
    String fieldname = "fourField";
    Type classType;
    try {
      classType = Type.forName(classname);
    } catch (ClassNotFoundException e) {
      fail("couldn't load class: " + e);
      throw new Error("unreachable");
    }
    try {
      AccessibleField pf = FieldParser.parse(fieldPair, classname, fieldname);
      assertEquals(fieldPair, pf.toParsableString(classType));

      assertTrue(pf.isStatic());
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }

  @Test
  public void parseConstraintStaticFinal() {
    String fieldPair = "randoop.field.ClassWithFields.FIVEFIELD";
    String classname = "randoop.field.ClassWithFields";
    String fieldname = "FIVEFIELD";
    Type classType;
    try {
      classType = Type.forName(classname);
    } catch (ClassNotFoundException e) {
      fail("couldn't load class: " + e);
      throw new Error("unreachable");
    }
    try {
      AccessibleField pf = FieldParser.parse(fieldPair, classname, fieldname);
      assertEquals(fieldPair, pf.toParsableString(classType));

      assertTrue(pf.isStatic() && pf.isFinal());
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  @Test
  public void parseErrors() {
    String spaceInValue = "randoop.field.ClassWithFields.one ield";
    String nonField = "randoop.field.ClassWithFields.oneMethod";

    String errorPrefix1 = "Error when parsing field ";

    String classname = "randoop.field.ClassWithFields";
    String fieldname = "one ield";
    try {
      AccessibleField pf = FieldParser.parse(spaceInValue, classname, fieldname);
      fail("Expected OperationParseException to be thrown");
    } catch (OperationParseException e) {
      String msg =
          errorPrefix1
              + spaceInValue
              + "."
              + " The field name "
              + fieldname
              + " has unexpected whitespace characters.";
      assertEquals(msg, e.getMessage());
    }

    classname = "randoop.field.ClassWithFields";
    fieldname = "oneMethod";
    try {
      AccessibleField pf = FieldParser.parse(nonField, classname, fieldname);
      fail("Expected OperationParseException to be thrown");
    } catch (OperationParseException e) {
      String msg =
          errorPrefix1
              + nonField
              + ". The field name \"oneMethod\""
              + " is not a field of the class \"randoop.field.ClassWithFields\".";
      assertEquals(msg, e.getMessage());
    }
  }
}
