package randoop.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import randoop.operation.ClassWithFields;

public class InstanceFieldTest {

  @Test
  public void inheritedMethods() {
    Class<?> c = ClassWithFields.class;
    try {
      InstanceField pf1 = new InstanceField(c.getField("oneField"));
      InstanceField pf1_2 = new InstanceField(c.getField("oneField"));
      InstanceField pf2 = new InstanceField(c.getField("threeField"));
      
      //identity
      assertEquals("Object built from same field should be equal",pf1,pf1_2);
      assertFalse("Objects of different fields should not be equal",pf1.equals(pf2));
      assertEquals("Objects build from same field should have same hashcode",pf1.hashCode(),pf1_2.hashCode());
      
      //types
      List<Class<?>> types = pf1.getSetTypes();
      assertEquals("Instance field needs two input types",2,types.size());
      
      List<Class<?>> expectedTypes = new ArrayList<>();
      expectedTypes.add(c);
      expectedTypes.add(int.class);
      assertEquals("Input type is field type",expectedTypes,types);
      
      List<Class<?>> accessTypes = new ArrayList<>();
      accessTypes.add(c);
      assertEquals("Access types should be declaring class",accessTypes,pf1.getAccessTypes());
      
    } catch (NoSuchFieldException e) {
      fail("test failed because field in test class not found");
    } catch (SecurityException e) {
      fail("test failed because of unexpected security exception");
    }
    
  }

}
