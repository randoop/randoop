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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests instantiation of type parameters by OperationModel
 */
public class InstantiationTest {

  @Test
  public void testInstantiation() {
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
