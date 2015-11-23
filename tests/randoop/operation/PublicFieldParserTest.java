package randoop.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class PublicFieldParserTest {

  @Test
  public void parseConstraintInstance() {
    String fieldPair = "int:randoop.operation.ClassWithFields.oneField";
    try {
      PublicField pf = (new PublicFieldParser()).parse(fieldPair);
      assertEquals("parse(\"t:v\").toParseableString() should equal \"t:v\"", fieldPair, pf.toParseableString());
      assertEquals("toString acts like toParseableString", pf.toParseableString(), pf.toString());
      assertTrue("object is an instance field", pf instanceof InstanceField);
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }
  
  @Test
  public void parseConstraintStatic() {
    String fieldPair = "int:randoop.operation.ClassWithFields.fourField";
    try {
      PublicField pf = (new PublicFieldParser()).parse(fieldPair);
      assertEquals("parse(\"t:v\").toParseableString() should equal \"t:v\"", fieldPair, pf.toParseableString());
      assertEquals("toString acts like toParseableString", pf.toParseableString(), pf.toString());
      assertTrue("object is a static field", pf instanceof StaticField);
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }
  
  @Test
  public void parseConstraintStaticFinal() {
    String fieldPair = "int:randoop.operation.ClassWithFields.FIVEFIELD";
    try {
      PublicField pf = (new PublicFieldParser()).parse(fieldPair);
      assertEquals("parse(\"t:v\").toParseableString() should equal \"t:v\"", fieldPair, pf.toParseableString());
      assertEquals("toString acts like toParseableString", pf.toParseableString(), pf.toString());
      assertTrue("object is a static final field", pf instanceof StaticFinalField);
    } catch (OperationParseException e) {
      fail("Parse error: " + e.getMessage());
    }
  }
  
  @SuppressWarnings("unused")
  @Test
  public void parseErrors() {
    String missingColon = "intrandoop.operation.ClassWithFields.oneField";
    String missingType = ":randoop.operation.ClassWithFields.oneField";
    String missingValue = "int:";
    String spaceInType = "i t:randoop.operation.ClassWithFields.oneField";
    String spaceInValue = "int:randoop.operation.ClassWithFields.one ield";
    String badType = "NATR:randoop.operation.ClassWithFields.oneField";
    String badValueNoField = "int:randoop.operation.ClassWithFields.twoField";
    String badValueNoClass = "int:oneField";
    String badValueBadClass = "int:NATC.oneField";
    String nonField = "int:randoop.operation.ClassWithFields.oneMethod";
    String wrongType = "double:randoop.operation.ClassWithFields.oneField";
    
    PublicFieldParser parser = new PublicFieldParser();
    
    try {
      PublicField pf = parser.parse(missingColon);
      fail("Expected StatementKindParseException to be thrown");
    } catch (OperationParseException e) {
      String msg = "A field description must be of the form \"" +
          "<type>:<field>\" but description is \"" + missingColon + "\".";
      assertEquals("Expecting missing colon message",msg,e.getMessage());
    }
    
    String errorPrefix1 = "Error when parsing type-value pair ";
    String errorPrefix2 = " for a field description of the form <type>:<field-name>.";
    
    try {
      PublicField pf = parser.parse(missingType);
      fail("Expected StatementKindParseException to be thrown");
    } catch (OperationParseException e) {
      String msg = errorPrefix1 + missingType + errorPrefix2 + " No type given.";
      assertEquals("Expecting missing type message",msg,e.getMessage());
    }
    
    try {
      PublicField pf = parser.parse(missingValue);
      fail("Expected StatementKindParseException to be thrown");
    } catch (OperationParseException e) {
      String msg = errorPrefix1 + missingValue + errorPrefix2 + " No field name given.";
      assertEquals("Expecting missing variable message",msg,e.getMessage());
    }
    
    try {
      PublicField pf = parser.parse(spaceInType);
      fail("Expected StatementKindParseException to be thrown");
    } catch (OperationParseException e) {
      String msg = errorPrefix1 + spaceInType + errorPrefix2 + " The type has unexpected whitespace characters.";
      assertEquals("Expecting space in type message",msg,e.getMessage());
    }
    
    try {
      PublicField pf = parser.parse(spaceInValue);
      fail("Expected StatementKindParseException to be thrown");
    } catch (OperationParseException e) {
      String msg = errorPrefix1 + spaceInValue + errorPrefix2 + " The field name has unexpected whitespace characters.";
      assertEquals("Expecting space in field message",msg,e.getMessage());
    }
    
    try {
      PublicField pf = parser.parse(badType);
      fail("Expected StatementKindParseException to be thrown");
    } catch (OperationParseException e) {
      String msg = errorPrefix1 + badType + errorPrefix2 + " The type given \"NATR\" was not recognized.";
      assertEquals("Expecting bad type message",msg,e.getMessage());
    }
    
    try {
      PublicField pf = parser.parse(badValueNoField);
      fail("Expected StatementKindParseException to be thrown");
    } catch (OperationParseException e) {
      String msg = errorPrefix1 + badValueNoField + errorPrefix2 + " The field name given \"twoField\" is not a field of the class \"randoop.operation.ClassWithFields\".";
      assertEquals("Expecting bad field message",msg,e.getMessage());
    }
    
    try {
      PublicField pf = parser.parse(badValueNoClass);
      fail("Expected StatementKindParseException to be thrown");
    } catch (OperationParseException e) {
      String msg = errorPrefix1 + badValueNoClass + errorPrefix2 + " No class name given in field name \"oneField\".";
      assertEquals("Expecting bad field message",msg,e.getMessage());
    }
    
    try {
      PublicField pf = parser.parse(badValueBadClass);
      fail("Expected StatementKindParseException to be thrown");
    } catch (OperationParseException e) {
      String msg = errorPrefix1 + badValueBadClass + errorPrefix2 + " The class name \"NATC\" of the field name \"NATC.oneField\" was not recognized as a class.";
      assertEquals("Expecting bad field message",msg,e.getMessage());
    }
    
    try {
      PublicField pf = parser.parse(nonField);
      fail("Expected StatementKindParseException to be thrown");
    } catch (OperationParseException e) {
      String msg = errorPrefix1 + nonField + errorPrefix2 + " The field name given \"oneMethod\"" +
             " is not a field of the class \"randoop.operation.ClassWithFields\".";
      assertEquals("Expecting not a field message",msg,e.getMessage());
    }
    
    try {
      PublicField pf = parser.parse(wrongType);
      fail("Expected StatementKindParseException to be thrown");
    } catch (OperationParseException e) {
      String msg = errorPrefix1 + wrongType + errorPrefix2 + " The type of the field \"randoop.operation.ClassWithFields.oneField\" is int, but given as double.";
      assertEquals("Expecting wrong type message",msg,e.getMessage());
    }
    
  }

}
