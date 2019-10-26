package randoop.reflection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.junit.Test;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.main.ClassNameErrorHandler;
import randoop.main.ThrowClassNameError;
import randoop.main.WarnOnBadClassName;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.supertypetest.InheritedEnum;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;

/** Tests of {@link randoop.reflection.OperationModel}. */
public class OperationModelTest {

  @Test
  public void linkedListTest() {
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Set<@ClassGetName String> classnames = new LinkedHashSet<>();
    classnames.add("java.util.LinkedList");
    Set<@ClassGetName String> coveredClassnames = new LinkedHashSet<>();
    ClassNameErrorHandler errorHandler = new ThrowClassNameError();
    List<String> literalsFileList = new ArrayList<>();
    OperationModel model = null;
    try {
      model =
          OperationModel.createModel(
              IS_PUBLIC,
              reflectionPredicate,
              classnames,
              coveredClassnames,
              errorHandler,
              literalsFileList);
    } catch (SignatureParseException e) {
      fail("failed to parse operation: " + e.getMessage());
    } catch (NoSuchMethodException e) {
      fail("did not find method: " + e.getMessage());
    }
    assertNotNull(model);

    assertEquals("only expect the LinkedList and Object classes", 2, model.getClassTypes().size());
    int genericOpCount = 0;
    int concreteOpCount = 0;
    int wildcardTypeCount = 0;
    for (TypedOperation operation : model.getOperations()) {
      if (operation.isGeneric()) {
        genericOpCount++;
      } else if (operation.hasWildcardTypes()) {
        wildcardTypeCount++;
      } else {
        concreteOpCount++;
      }
    }
    assertEquals("concrete operation count ", 1, concreteOpCount);
    assertThat(
        "generic operation count (JDK7: 51, JDK8: 58, JDK11: 59)",
        genericOpCount,
        isOneOf(50, 57, 58));
    assertEquals("wildcard operation count", 1, wildcardTypeCount);
    assertEquals(
        "all operations generic or concrete",
        concreteOpCount + genericOpCount + 1,
        model.getOperations().size());
  }

  @Test
  public void classWithInnerClassTest() {
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Set<@ClassGetName String> classnames = new LinkedHashSet<>();
    classnames.add("randoop.test.ClassWithInnerClass");
    classnames.add("randoop.test.ClassWithInnerClass$A");
    Set<@ClassGetName String> coveredClassnames = new LinkedHashSet<>();
    ClassNameErrorHandler errorHandler = new WarnOnBadClassName();
    List<String> literalsFileList = new ArrayList<>();
    OperationModel model = null;
    try {
      model =
          OperationModel.createModel(
              IS_PUBLIC,
              reflectionPredicate,
              classnames,
              coveredClassnames,
              errorHandler,
              literalsFileList);
    } catch (SignatureParseException e) {
      fail("failed to parse operation: " + e.getMessage());
    } catch (NoSuchMethodException e) {
      fail("did not find method: " + e.getMessage());
    }
    assertNotNull(model);
    assertEquals(
        "should have both outer and inner classes, plus Object", 3, model.getClassTypes().size());

    assertTrue("should have nonzero operations set", model.getOperations().size() > 1);
  }

  @Test
  public void instantiationTest() {
    OperationModel model = getOperationModel("randoop.reflection.GenericClass");
    assertNotNull(model);

    assertEquals("should be two classes ", 2, model.getClassTypes().size());

    for (ClassOrInterfaceType classType : model.getClassTypes()) {
      assertTrue(
          "types should be Object or generic", classType.isObject() || classType.isGeneric());
    }

    int genericOpCount = 0;
    int wildcardOpCount = 0;
    int concreteOpCount = 0;

    for (TypedOperation operation : model.getOperations()) {
      if (operation.isGeneric()) {
        genericOpCount++;
      } else if (operation.hasWildcardTypes()) {
        wildcardOpCount++;
      } else {
        concreteOpCount++;
      }
    }
    assertEquals("should be 20 generic operations", 20, genericOpCount);
    assertEquals("should be no wildcard operations other than getClass", 1, wildcardOpCount);
    assertEquals(
        "all operations should be concrete or generic ",
        model.getOperations().size() - genericOpCount - 1,
        concreteOpCount);
    int expectedCount = 22;
    assertEquals(
        "should have " + expectedCount + " operations",
        expectedCount,
        model.getOperations().size());
  }

  /**
   * Tests the case where an enum inherits a method from a class that has an overloaded method with
   * an incompatible type.
   */
  @Test
  public void testEnumOverloads() {
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Set<@ClassGetName String> classnames = new LinkedHashSet<>();
    classnames.add("randoop.reflection.supertypetest.InheritedEnum");
    Set<@ClassGetName String> coveredClassnames = new LinkedHashSet<>();
    ClassNameErrorHandler errorHandler = new ThrowClassNameError();
    List<String> literalsFileList = new ArrayList<>();
    OperationModel model = null;
    try {
      model =
          OperationModel.createModel(
              IS_PUBLIC,
              reflectionPredicate,
              classnames,
              coveredClassnames,
              errorHandler,
              literalsFileList);
    } catch (NoSuchMethodException e) {
      fail("did not find method: " + e.getMessage());
    } catch (SignatureParseException e) {
      fail("failed to parse operation: " + e.getMessage());
    }
    assertNotNull(model);

    List<TypedOperation> alphaOps = new ArrayList<>();
    for (TypedOperation operation : model.getOperations()) {
      String simpleOpName = operation.getName().substring(operation.getName().lastIndexOf('.') + 1);
      if (simpleOpName.equals("alpha")) {
        alphaOps.add(operation);
      }
    }
    assertEquals("should be two operations with name alpha", 2, alphaOps.size());

    for (TypedOperation operation : alphaOps) {
      Object[] inputs = new Object[2];
      ExecutionOutcome outcome;
      Object value;
      if (operation.getOutputType().equals(JavaTypes.STRING_TYPE)) {
        inputs[0] = InheritedEnum.ONE;
        inputs[1] = 1;
        outcome = operation.execute(inputs);
        assertTrue("execution should be normal", outcome instanceof NormalExecution);
        value = ((NormalExecution) outcome).getRuntimeValue();
        assertEquals("outcome should be string \"one\"", "one", (String) value);

        inputs[0] = InheritedEnum.TWO;
        inputs[1] = 1;
        outcome = operation.execute(inputs);
        assertTrue("execution should be normal", outcome instanceof NormalExecution);
        value = ((NormalExecution) outcome).getRuntimeValue();
        assertEquals("outcome should be string \"two\"", "two", (String) value);

      } else if (operation.getOutputType().equals(JavaTypes.INT_TYPE)) {
        inputs[0] = InheritedEnum.ONE;
        inputs[1] = "one";
        outcome = operation.execute(inputs);
        assertTrue("execution should be normal", outcome instanceof NormalExecution);
        value = ((NormalExecution) outcome).getRuntimeValue();
        assertEquals("outcome should be string \"one\"", 1, (int) value);

        inputs[0] = InheritedEnum.TWO;
        inputs[1] = "two";
        outcome = operation.execute(inputs);
        assertTrue("execution should be normal", outcome instanceof NormalExecution);
        value = ((NormalExecution) outcome).getRuntimeValue();
        assertEquals("outcome should be string \"one\"", 2, (int) value);
      } else {
        fail("output type should be either String or int");
      }
    }
  }

  /** Test whether member classes are harvested. */
  @Test
  public void memberTypeTest() {
    String classname = "randoop.reflection.ClassWithMemberTypes";
    OperationModel model = getOperationModel(classname);
    assertNotNull(model);

    List<ClassOrInterfaceType> expected = new ArrayList<>();
    expected.add(ClassOrInterfaceType.forClass(ClassWithMemberTypes.class));
    expected.add(ClassOrInterfaceType.forClass(ClassWithMemberTypes.InnerEnum.class));
    expected.add(ClassOrInterfaceType.forClass(ClassWithMemberTypes.StaticClass.class));
    expected.add(ClassOrInterfaceType.forClass(ClassWithMemberTypes.InnerClass.class));
    expected.add(ClassOrInterfaceType.forClass(ClassWithMemberTypes.MemberInterface.class));
    for (ClassOrInterfaceType t : expected) {
      assertTrue("expected type " + t + " should be harvested", model.getClassTypes().contains(t));
    }

    List<ClassOrInterfaceType> notExpected = new ArrayList<>();
    notExpected.add(
        ClassOrInterfaceType.forClass(ClassWithMemberTypes.PackagePrivateInnerClass.class));
    notExpected.add(
        ClassOrInterfaceType.forClass(ClassWithMemberTypes.PackagePrivateInnerEnum.class));
    notExpected.add(
        ClassOrInterfaceType.forClass(ClassWithMemberTypes.PackagePrivateMemberInterface.class));
    notExpected.add(
        ClassOrInterfaceType.forClass(ClassWithMemberTypes.PackagePrivateStaticClass.class));
    for (ClassOrInterfaceType t : notExpected) {
      assertFalse(
          "package private type " + t + " should not be harvested",
          model.getClassTypes().contains(t));
    }
  }

  @Test
  public void memberOfGenericTypeTest() {
    String classname = "randoop.reflection.GenericTreeWithInnerNode";
    OperationModel model = getOperationModel(classname);
    assertNotNull(model);

    List<TypedOperation> operations = model.getOperations();
    for (TypedOperation operation : operations) {
      if (!operation.isConstructorCall()
          && !operation.getOutputType().isVoid()
          && !operation.getName().equals("java.lang.Object.getClass")) {
        assertTrue(
            "is member class: " + operation.getOutputType(),
            ((ClassOrInterfaceType) operation.getOutputType()).isMemberClass());
        assertFalse("is not parameterized", operation.getOutputType().isParameterized());
        assertTrue("is generic", operation.getOutputType().isGeneric());
      }
    }
    // fail("incomplete");
  }

  @Test
  public void orderModelTest() {
    Set<@ClassGetName String> classnames1 = new LinkedHashSet<>();
    classnames1.add("randoop.reflection.ClassWithMemberTypes");
    classnames1.add("randoop.reflection.GenericTreeWithInnerNode");
    classnames1.add("randoop.reflection.supertypetest.InheritedEnum");
    classnames1.add("randoop.reflection.visibilitytest.PublicClass");
    OperationModel model1 = getOperationModel(classnames1);
    List<TypedOperation> operations1 = model1.getOperations();

    Set<@ClassGetName String> classnames2 = new LinkedHashSet<>();
    classnames2.add("randoop.reflection.visibilitytest.PublicClass");
    classnames2.add("randoop.reflection.GenericTreeWithInnerNode");
    classnames2.add("randoop.reflection.supertypetest.InheritedEnum");
    classnames2.add("randoop.reflection.ClassWithMemberTypes");
    OperationModel model2 = getOperationModel(classnames2);
    List<TypedOperation> operations2 = model2.getOperations();

    assertEquals("operations lists should be same length", operations2.size(), operations1.size());
    assertEquals("should be same elements", operations1, operations2);
  }

  @Test
  public void staticFinalFieldTest() {
    Set<@ClassGetName String> classnames = new LinkedHashSet<>();
    classnames.add("randoop.reflection.FieldInheritingClass");
    OperationModel model = getOperationModel(classnames);
    List<TypedOperation> operations = model.getOperations();

    Set<TypedClassOperation> constantOps = new HashSet<>();
    for (TypedOperation operation : operations) {
      if (!operation.isConstructorCall() && operation instanceof TypedClassOperation) {
        TypedClassOperation op = (TypedClassOperation) operation;
        String simpleOpName = op.getName().substring(op.getName().lastIndexOf('.') + 1);
        if (simpleOpName.equals("<get>(CONSTANT)")) {
          constantOps.add(op);
        }
      }
    }
    assertEquals("should be two constant operations", 2, constantOps.size());
    for (TypedClassOperation operation : constantOps) {
      assertThat(
          "declaring type should be interface",
          operation.getDeclaringType().getSimpleName(),
          anyOf(is(equalTo("ConstantFieldParent")), is(equalTo("ConstantFieldChild"))));
      assertTrue("operation is a constant", operation.isConstantField());
    }
  }

  private OperationModel getOperationModel(@ClassGetName String classname) {
    Set<@ClassGetName String> classnames = new LinkedHashSet<>();
    classnames.add(classname);
    return getOperationModel(classnames);
  }

  private OperationModel getOperationModel(Set<@ClassGetName String> classnames) {
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Set<@ClassGetName String> coveredClassnames = new LinkedHashSet<>();
    ClassNameErrorHandler errorHandler = new WarnOnBadClassName();
    List<String> literalsFileList = new ArrayList<>();
    OperationModel model = null;
    try {
      model =
          OperationModel.createModel(
              IS_PUBLIC,
              reflectionPredicate,
              classnames,
              coveredClassnames,
              errorHandler,
              literalsFileList);
    } catch (SignatureParseException e) {
      fail("failed to parse operation: " + e.getMessage());
    } catch (NoSuchMethodException e) {
      fail("did not find method: " + e.getMessage());
    }
    return model;
  }
}
