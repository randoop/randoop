package randoop.operation;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import randoop.field.AccessibleField;
import randoop.field.ClassWithFields;
import randoop.field.SubclassWithFields;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.ModelCollections;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionManager;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.TypedOperationManager;
import randoop.reflection.VisibilityPredicate;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ConcreteTypes;
import randoop.types.GeneralType;
import randoop.types.RandoopTypeException;
import randoop.types.SimpleClassOrInterfaceType;
import randoop.types.TypeTuple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * FieldReflectionTest consists of tests of reflection collection of field methods
 * to verify that field operations are collected as expected.
 *
 */
public class FieldReflectionTest {

  /**
   * basicFields tests that all of the expected fields are collected for the
   * ClassWithFields class.
   */
  @Test
  public void basicFields() {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType declaringType = new SimpleClassOrInterfaceType(c);

    @SuppressWarnings("unchecked")
    List<Field> fields = Arrays.asList(c.getFields());

    final Set<TypedOperation> operations = getConcreteOperations(c);

    //number of operations is twice number of fields plus constructor and getter minus one for each constant
    //in this case, 11
    assertEquals("number of operations twice number of fields", 2 * fields.size(), operations.size());

    //exclude private or protected fields
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
                "field " + f.toGenericString() + " should occur", operations.containsAll(getOperations(f, declaringType)));
      }
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }

    try {
      for (Field f : exclude) {
        assertFalse(
                "field " + f.toGenericString() + " should not occur",
                operations.containsAll(getOperations(f, declaringType)));
      }
    } catch (RandoopTypeException e) {
      fail("type error: " + e.getMessage());
    }
  }

  private Set<TypedOperation> getConcreteOperations(Class<?> c) {
    return getConcreteOperations(c, new DefaultReflectionPredicate(), new PublicVisibilityPredicate());
  }

  private Set<TypedOperation> getConcreteOperations(Class<?> c, ReflectionPredicate predicate, VisibilityPredicate visibilityPredicate) {
    final Set<TypedOperation> operations = new LinkedHashSet<>();
    TypedOperationManager operationManager = new TypedOperationManager(new ModelCollections() {
      @Override
      public void addConcreteOperation(ClassOrInterfaceType declaringType, TypedOperation operation) {
        operations.add(operation);
      }
    });
    OperationExtractor extractor = new OperationExtractor(operationManager, predicate);
    ReflectionManager manager = new ReflectionManager(visibilityPredicate);
    manager.add(extractor);
    manager.apply(c);
    return operations;
  }

  /**
   * inheritedFields looks for operations built for inherited fields.
   * Avoid hidden fields, because we cannot get to them without reflection.
   */
  @Test
  public void inheritedFields() {
    Class<?> c = SubclassWithFields.class;
    ClassOrInterfaceType declaringType = new SimpleClassOrInterfaceType(c);

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
        } else { //hidden
          exclude.add(f);
        }
      } else {
        expected.add(f);
      }
    }
    Set<TypedOperation> actual = getConcreteOperations(c);

    assertEquals("number of operations ", 2 * expected.size() - 1 + 2, actual.size());
    try {
      for (Field f : expected) {
        assertTrue(
                "field " + f.toGenericString() + " should occur", actual.containsAll(getOperations(f, declaringType)));
      }
    } catch (RandoopTypeException e) {
      fail("type error: " + e);
    }

    try {
      for (Field f : exclude) {
        assertFalse(
                "field " + f.toGenericString() + " should not occur",
                actual.containsAll(getOperations(f, declaringType)));
      }
    } catch (RandoopTypeException e) {
      fail("type error: " + e);
    }
  }

  /**
   * filteredFields checks to ensure we don't get any fields that should be removed
   *
   */
  @Test
  public void filteredFields() {
    Class<?> c = ClassWithFields.class;
    ClassOrInterfaceType declaringType = new SimpleClassOrInterfaceType(c);

    //let's exclude every field
    List<Field> exclude = new ArrayList<>();
    Set<String> excludeNames = new TreeSet<>();
    for (Field f : c.getFields()) {
      excludeNames.add(f.getDeclaringClass().getName() + "." + f.getName());
      exclude.add(f);
    }

    ReflectionPredicate filter = new DefaultReflectionPredicate(null, excludeNames);
    Set<TypedOperation> actual = getConcreteOperations(c, filter, new PublicVisibilityPredicate());

    assertEquals("number of operations ", 2, actual.size());

    for (Field f : exclude) {
      try {
        assertFalse(
            "field " + f.toGenericString() + " should not occur",
            actual.containsAll(getOperations(f, declaringType)));
      } catch (RandoopTypeException e) {
        fail("type error: " + e.getMessage());
      }
    }
  }

  /**
   * getOperations maps a field into possible operations.
   * Looks at modifiers to decide which kind of field wrapper
   * to create and then builds list with getter and setter.
   *
   * @param f - reflective Field object
   * @return List of getter/setter statements for the field
   */
  private List<TypedOperation> getOperations(Field f, ClassOrInterfaceType declaringType) throws RandoopTypeException {
    List<TypedOperation> statements = new ArrayList<>();
    GeneralType fieldType = GeneralType.forType(f.getGenericType());
    AccessibleField field = new AccessibleField(f, declaringType);
    List<GeneralType> getInputTypeList = new ArrayList<>();
    List<GeneralType> setInputTypeList = new ArrayList<>();
    if (! field.isStatic()) {
      getInputTypeList.add(declaringType);
      setInputTypeList.add(declaringType);
    }

    statements.add(new TypedClassOperation(new FieldGet(field), declaringType, new TypeTuple(getInputTypeList), fieldType));

    if (! field.isFinal()) {
      setInputTypeList.add(fieldType);
      statements.add(new TypedClassOperation(new FieldSet(field), declaringType, new TypeTuple(setInputTypeList), ConcreteTypes.VOID_TYPE));
    }
    return statements;
  }
}
