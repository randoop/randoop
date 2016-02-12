package randoop.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import randoop.field.StaticFinalField;

public class StaticFinalFieldTest {

  @Test
  public void inheritedMethods() {
    Class<?> c = ClassWithFields.class;
    try {
      StaticFinalField sf1 = new StaticFinalField(c.getField("fourField"));
      StaticFinalField sf1_2 = new StaticFinalField(c.getField("fourField"));
      StaticFinalField sf2 = new StaticFinalField(c.getField("sixField"));
      
      //identity
      assertEquals("Object built from same field should be equal",sf1,sf1_2);
      assertFalse("Objects of different fields should not be equal",sf1.equals(sf2));
      assertEquals("Objects build from same field should have same hashcode",sf1.hashCode(),sf1_2.hashCode());
      
      //types
      List<Class<?>> types = sf1.getSetTypes();
      assertEquals("Instance field needs two input types",0,types.size());
      List<Class<?>> expectedTypes = new ArrayList<>();
      assertEquals("Input type is field type",expectedTypes,types);
      assertTrue("Access types should be empty",sf1.getAccessTypes().isEmpty());
      
    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    }
  }

}
