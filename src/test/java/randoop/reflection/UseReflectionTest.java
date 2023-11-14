package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.reflection.accessibilitytest.PublicClass;
import randoop.types.ClassOrInterfaceType;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

public class UseReflectionTest {
    /*
     * Tests than when setting the use_reflection option to true Randoop will collect
     * inaccessible methods.
     */
    @Test
    public void testMethodCollection() {
        // TODO: Update this test when adding functionality for other
        //  reflective calls (i.e. constructors, enums, etc.)
        GenInputsAbstract.use_reflection = true;
        Class<?> c = PublicClass.class;

        List<Constructor<?>> expectedConstructors = new ArrayList<>();
        for (Constructor<?> co : c.getDeclaredConstructors()) {
            int mods = co.getModifiers() & Modifier.constructorModifiers();
            if (isPubliclyAccessible(mods)) {
                expectedConstructors.add(co);
            }
        }
        assertFalse(expectedConstructors.isEmpty());

        List<Enum<?>> expectedEnums = new ArrayList<>();
        for (Class<?> ic : c.getDeclaredClasses()) {
            int mods = ic.getModifiers() & Modifier.classModifiers();
            if (ic.isEnum() && isPubliclyAccessible(mods)) {
                for (Object o : ic.getEnumConstants()) {
                    Enum<?> e = (Enum<?>) o;
                    expectedEnums.add(e);
                }
            }
        }

        assertFalse(expectedEnums.isEmpty());

        List<Field> expectedFields = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            int mods = f.getModifiers() & Modifier.fieldModifiers();
            if (isPubliclyAccessible(mods)) {
                expectedFields.add(f);
            }
        }

        assertFalse(expectedFields.isEmpty());

        List<Method> expectedMethods = new ArrayList<>();
        for (Method m : c.getDeclaredMethods()) {
            // method should be included even if its non-public
            if (!m.isBridge() && !m.isSynthetic()) {
                expectedMethods.add(m);
            }
        }

        assertFalse(expectedMethods.isEmpty());

        ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
        AccessibilityPredicate accessibility = IS_PUBLIC;
        List<TypedOperation> actual = getConcreteOperations(c, reflectionPredicate, accessibility);
        int expectedCount =
                expectedMethods.size()
                        + 2 * expectedFields.size()
                        + expectedEnums.size()
                        + expectedConstructors.size()
                        + 1;
        assertEquals(expectedCount, actual.size());
        GenInputsAbstract.use_reflection = false; // set reflection bool back to default
    }

    private List<TypedOperation> getConcreteOperations(
            Class<?> c,
            ReflectionPredicate reflectionPredicate,
            AccessibilityPredicate accessibilityPredicate) {
        Set<ClassOrInterfaceType> classTypes =
                DeclarationExtractor.classTypes(c, reflectionPredicate, accessibilityPredicate);
        final List<TypedOperation> operations =
                OperationExtractor.operations(classTypes, reflectionPredicate, accessibilityPredicate);
        return operations;
    }
    private boolean isPubliclyAccessible(int mods) {
        return Modifier.isPublic(mods);
    }
}
