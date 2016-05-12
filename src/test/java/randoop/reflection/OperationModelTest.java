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
import randoop.types.GeneralType;
import randoop.types.RandoopTypeException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by bjkeller on 4/5/16.
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
    assertTrue("should have nonzero operations set", model.getConcreteOperations().size() > 0);

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

    assertTrue("should have nonzero operations set", model.getConcreteOperations().size() > 0);

  }
}
