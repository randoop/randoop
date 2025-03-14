package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;

/**
 * The JDKTypes class has constants for JDK Collections classes (classes implementing the Collection
 * and Map interfaces, and maps each interface into an implementing class. This test mainly checks
 * this mapping to ensure that a concrete subtype is selected for each type.
 */
public class JDKTypesTest {

  @Test
  public void collectionsMapTest() throws IllegalAccessException {

    // Load class types from fields in JDKTypes
    Set<GenericClassType> collectionTypes = new LinkedHashSet<>();
    for (Field f : JDKTypes.class.getDeclaredFields()) {
      if (!f.getName().equals("$jacocoData")
          && Modifier.isFinal(Modifier.fieldModifiers() & f.getModifiers())) {
        collectionTypes.add((GenericClassType) f.get(null));
      }
    }

    for (GenericClassType classType : collectionTypes) {
      if (classType.equals(JDKTypes.COMPARATOR_TYPE)) {
        continue;
      }
      GenericClassType implementingType = JDKTypes.getImplementingTypeForCollection(classType);
      if (classType.equals(JDKTypes.ENUM_SET_TYPE)) { // EnumSet is a special case
        assertEquals(classType, implementingType);
      } else if (classType.isInterface() || classType.isAbstract()) {
        assertFalse(implementingType.isInterface());
        assertFalse(implementingType.isAbstract());
        assertTrue(implementingType.isSubtypeOf(classType));
      } else {
        assertEquals(classType, implementingType);
      }
    }
  }
}
