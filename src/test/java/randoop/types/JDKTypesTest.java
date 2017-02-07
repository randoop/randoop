package randoop.types;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * The JDKTypes class has constants for JDK Collections classes (classes implementing the Collection
 * and Map interfaces, and maps each interface into an implementing class.
 * This test mainly checks this mapping to ensure that a concrete subtype is selected for each type.
 */
public class JDKTypesTest {

  @Test
  public void collectionsMapTest() {

    // Load class types from fields in JDKTypes
    Set<GenericClassType> collectionTypes = new LinkedHashSet<>();
    for (Field f : JDKTypes.class.getDeclaredFields()) {
      try {
        if (!f.getName().equals("$jacocoData")
            && Modifier.isFinal(Modifier.fieldModifiers() & f.getModifiers())) {
          collectionTypes.add((GenericClassType) f.get(null));
        }
      } catch (IllegalAccessException e) {
        fail("could not access field: " + f.getName());
      }
    }

    for (GenericClassType classType : collectionTypes) {
      if (classType.equals(JDKTypes.COMPARATOR_TYPE)) {
        continue;
      }
      GenericClassType implementingType = JDKTypes.getImplementingType(classType);
      if (classType.equals(JDKTypes.ENUM_SET_TYPE)) { // EnumSet is a special case
        assertTrue("EnumSet should be implemented by itself", classType.equals(implementingType));
      } else if (classType.isInterface() || classType.isAbstract()) {
        assertTrue(
            "interface "
                + classType
                + " may not map to interface or abstract class "
                + implementingType,
            !implementingType.isInterface() && !implementingType.isAbstract());
        assertThat(
            "interface " + classType + " should have subtype " + implementingType,
            implementingType.isSubtypeOf(classType));
      } else {
        assertTrue(
            "classtype " + classType + " should implement itself",
            classType.equals(implementingType));
      }
    }
  }
}
