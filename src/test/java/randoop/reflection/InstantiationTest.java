package randoop.reflection;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.main.ClassNameErrorHandler;
import randoop.main.ThrowClassNameError;
import randoop.operation.OperationParseException;
import randoop.operation.TypedOperation;
import randoop.types.ClassOrInterfaceType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests instantiation of type parameters by OperationModel
 */
public class InstantiationTest {

  @Test
  public void testGenericBounds() {
    Set<String> classnames = new LinkedHashSet<>();
    String packageName = "randoop.reflection";
    classnames.add(packageName + "." + "GenericBounds");
    classnames.add(packageName + "." + "TW");
    classnames.add(packageName + "." + "SW");
    classnames.add(packageName + "." + "UW");
    classnames.add(packageName + "." + "VW");
    classnames.add(packageName + "." + "WW");
    classnames.add(packageName + "." + "XW");
    classnames.add(packageName + "." + "YW");
    classnames.add(packageName + "." + "RML");

    Set<String> methodNames = new LinkedHashSet<>();
    methodNames.add("m00");
    methodNames.add("m01");
    methodNames.add("m02");
    methodNames.add("m03");
    methodNames.add("m04");
    methodNames.add("m05");
    methodNames.add("m06");
    methodNames.add("m07");
    methodNames.add("m08");
    methodNames.add("m09");
    methodNames.add("m10");
    methodNames.add("m11");
    methodNames.add("m12");
    methodNames.add("getZ");
    methodNames.add("setZ");

    OperationModel model = createModel(classnames, packageName);

    int expectedClassCount = classnames.size() + 1;
    assertThat(
        "expect "
            + expectedClassCount
            + " classes: GenericBounds, SW, TW, UW, VW, WW, XW, YW, RML and Object",
        model.getConcreteClasses().size(),
        is(equalTo(expectedClassCount)));

    int methodCount = 0;
    for (TypedOperation operation : model.getConcreteOperations()) {
      if (operation.isMethodCall()) {
        assertTrue(
            "method name " + operation.getName() + " should be in expected list",
            methodNames.contains(operation.getName()));
        methodCount++;
      }
    }
    assertThat(
        "expect " + methodNames.size() + " methods", methodCount, is(equalTo(methodNames.size())));
  }

  /**
   * This test fails if {@code D_BST} is removed since model always chooses {@code String} for
   * parameter to {@code BST} and without {@code D_BST} there is no class that implements
   * {@code C_BST<String>}.
   *
   * it should be possible for it to pass with {@code B_BST}.
   */
  @Test
  public void testOperationInstantiation() {
    Set<String> classnames = new LinkedHashSet<>();
    String packageName = "randoop.reflection";
    classnames.add(packageName + "." + "BST");
    classnames.add(packageName + "." + "B_BST");
    classnames.add(packageName + "." + "D_BST");

    OperationModel model = createModel(classnames, packageName);

    int expectedClassCount = classnames.size() + 1;
    assertThat(
        "expect " + expectedClassCount + " classes",
        model.getConcreteClasses().size(),
        is(equalTo(expectedClassCount)));

    int methodCount = 0;
    for (TypedOperation operation : model.getConcreteOperations()) {
      System.out.println("op: " + operation);
      if (operation.isMethodCall()) {
        methodCount++;
        if (!operation.getName().equals("m")) {
          fail("should only have method m, got " + operation.getName());
        }
      }
    }
    assertThat("expect one method", methodCount, is(equalTo(1)));
  }

  @Test
  public void testRecursiveInstantiation() {
    Set<String> classnames = new LinkedHashSet<>();
    String packageName = "randoop.reflection";
    classnames.add(packageName + "." + "BMB");
    classnames.add(packageName + "." + "AI");
    classnames.add(packageName + "." + "AT");

    OperationModel model = createModel(classnames, packageName);

    int expectedClassCount = classnames.size() + 1;
    assertThat(
        "expect " + expectedClassCount + " classes",
        model.getConcreteClasses().size(),
        is(equalTo(expectedClassCount)));

    for (ClassOrInterfaceType type : model.getConcreteClasses()) {
      assertThat(
          "class name one of BMB, AI, AT, or Object",
          type.getSimpleName(),
          isOneOf("BMB", "AI", "AT", "Object"));
    }
  }

  private OperationModel createModel(Set<String> names, String packageName) {
    VisibilityPredicate visibility =
        new PackageVisibilityPredicate(Package.getPackage(packageName));
    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate();
    Set<String> exercisedClassnames = new LinkedHashSet<>();
    Set<String> methodSignatures = new LinkedHashSet<>();
    ClassNameErrorHandler errorHandler = new ThrowClassNameError();
    List<String> literalsFileList = new ArrayList<>();
    OperationModel model = null;
    try {
      model =
          OperationModel.createModel(
              visibility,
              reflectionPredicate,
              names,
              exercisedClassnames,
              methodSignatures,
              errorHandler,
              literalsFileList);
    } catch (OperationParseException e) {
      fail("failed to parse operation: " + e.getMessage());
    } catch (NoSuchMethodException e) {
      fail("did not find method: " + e.getMessage());
    }
    assert model != null : "model was not initialized";
    return model;
  }
}
