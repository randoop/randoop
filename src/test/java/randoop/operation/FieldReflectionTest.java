package randoop.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import randoop.field.AccessibleField;
import randoop.field.ClassWithFields;
import randoop.field.SubclassWithFields;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OmitMethodsPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.RandoopTypeException;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * FieldReflectionTest consists of tests of reflection collection of field methods to verify that
 * field operations are collected as expected.
 */
public class FieldReflectionTest {

  /**
   * basicFields tests that all of the expected fields are collected for the ClassWithFields class.
   */
  @Test
  public void basicFields() {
    Class<?> c = ClassWithFields.class;

    @SuppressWarnings("unchecked")
    List<Field> fields = Arrays.asList(c.getFields());

    final Set<TypedOperation> operations = getConcreteOperations(c);

    // number of operations is twice number of fields plus constructor and getter minus one for each
    // constant
    // in this case, 11
    assertEquals(2 * fields.size() + 1, operations.size());

    // exclude private or protected fields
    List<Field> exclude = new ArrayList<>();
    for (Field f : c.getDeclaredFields()) {
      int mods = f.getModifiers();
      if (Modifier.isPrivate(mods) || Modifier.isProtected(mods)) {
        exclude.add(f);
      }
    }

    try {
      for (Field f : fields) {
        assertTrue(
            "field " + f.toGenericString() + " should occur",
            operations.containsAll(getOperations(f)));
      }
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }

    try {
      for (Field f : exclude) {
        assertFalse(
            "field " + f.toGenericString() + " should not occur",
            operations.containsAll(getOperations(f)));
      }
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
  }

  private Set<TypedOperation> getConcreteOperations(Class<?> c) {
    return getConcreteOperations(c, new DefaultReflectionPredicate(), IS_PUBLIC);
  }

  private Set<TypedOperation> getConcreteOperations(
      Class<?> c,
      ReflectionPredicate reflectionPredicate,
      VisibilityPredicate visibilityPredicate) {
    return new LinkedHashSet<>(
        OperationExtractor.operations(
            c, reflectionPredicate, OmitMethodsPredicate.NO_OMISSION, visibilityPredicate));
  }

  /**
   * inheritedFields looks for operations built for inherited fields. Avoid hidden fields, because
   * we cannot get to them without reflection.
   */
  @Test
  public void inheritedFields() {
    Class<?> c = SubclassWithFields.class;

    List<Field> expected = new ArrayList<>();
    List<Field> exclude = new ArrayList<>();
    List<String> declared = new ArrayList<>();
    for (Field f : c.getDeclaredFields()) {
      declared.add(f.getName());
    }

    for (Field f : c.getFields()) {
      if (declared.contains(f.getName())) {
        if (c.equals(f.getDeclaringClass())) {
          expected.add(f);
        } else { // hidden
          exclude.add(f);
        }
      } else {
        expected.add(f);
      }
    }
    Set<TypedOperation> actual = getConcreteOperations(c);

    try {
      for (Field f : expected) {
        assertTrue(
            "field " + f.toGenericString() + " should occur", actual.containsAll(getOperations(f)));
      }
    } catch (RandoopTypeException e) {
      fail("type error: " + e);
    }

    try {
      for (Field f : exclude) {
        assertFalse(
            "field " + f.toGenericString() + " should not occur",
            actual.containsAll(getOperations(f)));
      }
    } catch (RandoopTypeException e) {
      fail("type error: " + e);
    }
    assertEquals(2 * expected.size() - 1 + 3, actual.size());
  }

  /** filteredFields checks to ensure we don't get any fields that should be removed */
  @Test
  public void filteredFields() {
    Class<?> c = ClassWithFields.class;

    // let's exclude every field
    List<Field> exclude = new ArrayList<>();
    Set<String> excludeNames = new TreeSet<>();
    for (Field f : c.getFields()) {
      excludeNames.add(f.getDeclaringClass().getName() + "." + f.getName());
      exclude.add(f);
    }

    ReflectionPredicate filter = new DefaultReflectionPredicate(excludeNames);
    Set<TypedOperation> actual = getConcreteOperations(c, filter, IS_PUBLIC);

    assertEquals(3, actual.size());

    for (Field f : exclude) {
      try {
        assertFalse(
            "field " + f.toGenericString() + " should not occur",
            actual.containsAll(getOperations(f)));
      } catch (RandoopTypeException e) {
        fail("type error: " + e.getMessage());
      }
    }
  }

  /**
   * getOperations maps a field into possible operations. Looks at modifiers to decide which kind of
   * field wrapper to create and then builds list with getter and setter.
   *
   * @param f reflective Field object
   * @return a list of getter/setter statements for the field
   */
  private List<TypedOperation> getOperations(Field f) throws RandoopTypeException {
    ClassOrInterfaceType declaringType = ClassOrInterfaceType.forClass(f.getDeclaringClass());
    List<TypedOperation> statements = new ArrayList<>();
    Type fieldType = Type.forType(f.getGenericType());
    AccessibleField field = new AccessibleField(f, declaringType);
    List<Type> getInputTypeList = new ArrayList<>();
    List<Type> setInputTypeList = new ArrayList<>();
    if (!field.isStatic()) {
      getInputTypeList.add(declaringType);
      setInputTypeList.add(declaringType);
    }

    FieldGet fieldGet = new FieldGet(field);
    TypedClassOperation getOperation;
    if (field.isFinal() && field.isStatic()) {
      ClassOrInterfaceType fieldDeclaringType =
          ClassOrInterfaceType.forClass(f.getDeclaringClass());
      getOperation =
          new TypedClassOperation(
              fieldGet, fieldDeclaringType, new TypeTuple(getInputTypeList), fieldType);
    } else {
      getOperation =
          new TypedClassOperation(
              new FieldGet(field), declaringType, new TypeTuple(getInputTypeList), fieldType);
    }
    statements.add(getOperation);

    if (!field.isFinal()) {
      setInputTypeList.add(fieldType);
      TypedClassOperation operation =
          new TypedClassOperation(
              new FieldSet(field),
              declaringType,
              new TypeTuple(setInputTypeList),
              JavaTypes.VOID_TYPE);
      statements.add(operation);
    }
    return statements;
  }
}
