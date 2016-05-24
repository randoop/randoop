package randoop.reflection;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.main.ClassNameErrorHandler;
import randoop.main.ThrowClassNameError;
import randoop.main.WarnOnBadClassName;
import randoop.operation.OperationParseException;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests of {@link randoop.reflection.OperationModel}.
 *
 */
public class OperationModelTest {

  @Test
  public void linkedListTest() {
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Set<String> classnames = new LinkedHashSet<>();
    classnames.add("java.util.LinkedList");
    Set<String> exercisedClassnames = new LinkedHashSet<>();
    Set<String> methodSignatures = new LinkedHashSet<>();
    ClassNameErrorHandler errorHandler = new ThrowClassNameError();
    List<String> literalsFileList = new ArrayList<>();
    OperationModel model = null;
    try {
      model = OperationModel.createModel(visibility, reflectionPredicate, classnames, exercisedClassnames, methodSignatures, errorHandler, literalsFileList);
    } catch (OperationParseException e) {
      fail("failed to parse operation: " + e.getMessage());
    } catch (NoSuchMethodException e) {
      fail("did not find method: " + e.getMessage());
    }
    assert model != null : "model was not initialized";

    assertThat("only expect the LinkedList and Object classes", model.getConcreteClasses().size(), is(equalTo(2)));
    int genericOpCount = 0;
    int concreteOpCount = 0;
    int wildcardTypeCount = 0;
    for (TypedOperation operation : model.getConcreteOperations()) {
      if (operation.isGeneric()) {
        genericOpCount++;
      } else if (operation.hasWildcardTypes()) {
        wildcardTypeCount++;
      } else {
        concreteOpCount++;
      }
    }
    assertEquals("concrete operations", 58, concreteOpCount);
    assertEquals("generic operations", 0, genericOpCount);
    assertEquals("wildcard operations", 0, wildcardTypeCount);
    assertEquals("all operations instantiated", model.getConcreteOperations().size(), concreteOpCount);
  }

  @Test
  public void classWithInnerClassTest() {
    VisibilityPredicate visibilityPredicate = new PublicVisibilityPredicate();
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Set<String> classnames = new LinkedHashSet<>();
    classnames.add("randoop.test.ClassWithInnerClass");
    classnames.add("randoop.test.ClassWithInnerClass$A");
    Set<String> exercisedClassname = new LinkedHashSet<>();
    Set<String> methodSignatures = new LinkedHashSet<>();
    ClassNameErrorHandler errorHandler = new WarnOnBadClassName();
    List<String> literalsFileList = new ArrayList<>();
    OperationModel model = null;
    try {
      model = OperationModel.createModel(visibilityPredicate, reflectionPredicate, classnames, exercisedClassname, methodSignatures, errorHandler, literalsFileList);
    } catch (OperationParseException e) {
      fail("failed to parse operation: " + e.getMessage());
    } catch (NoSuchMethodException e) {
      fail("did not find method: " + e.getMessage());
    }
    assert model != null: "model was not initialized";
    assertThat("should have both outer and inner classes, plus Object", model.getConcreteClasses().size(), is(equalTo(3)));

    assertTrue("should have nonzero operations set", model.getConcreteOperations().size() > 1);
  }

  @Test
  public void instantiationTest() {
    VisibilityPredicate visibilityPredicate = new PublicVisibilityPredicate();
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Set<String> classnames = new LinkedHashSet<>();
    classnames.add("randoop.reflection.GenericClass");
    Set<String> exercisedClassname = new LinkedHashSet<>();
    Set<String> methodSignatures = new LinkedHashSet<>();
    ClassNameErrorHandler errorHandler = new WarnOnBadClassName();
    List<String> literalsFileList = new ArrayList<>();
    OperationModel model = null;
    try {
      model = OperationModel.createModel(visibilityPredicate, reflectionPredicate, classnames, exercisedClassname, methodSignatures, errorHandler, literalsFileList);
    } catch (OperationParseException e) {
      fail("failed to parse operation: " + e.getMessage());
    } catch (NoSuchMethodException e) {
      fail("did not find method: " + e.getMessage());
    }
    assert model != null: "model was not initialized";

    assertEquals("should be two classes ", 2, model.getConcreteClasses().size());

    for (ClassOrInterfaceType classType : model.getConcreteClasses()) {
      assertTrue("classes are all non generic", ! classType.isGeneric());
    }

    int genericOpCount = 0;
    int wildcardOpCount = 0;
    int concreteOpCount = 0;

    for (TypedOperation operation : model.getConcreteOperations()) {

      if (operation.isGeneric()) {
        genericOpCount++;
        System.out.println(operation);
      } else if (operation.hasWildcardTypes()) {
        wildcardOpCount++;
      } else {
        concreteOpCount++;
      }
    }
    assertEquals("should be no generic operations", 0, genericOpCount);
    assertEquals("should be no wildcard operations", 0, wildcardOpCount);
    assertEquals("all operations should be instantiated ", model.getConcreteOperations().size(), concreteOpCount);
    assertEquals("should have 21 operations", 21, model.getConcreteOperations().size());
  }
}
