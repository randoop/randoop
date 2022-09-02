package randoop.main.gentests;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.main.gentests.classes.AnotherDependency;
import randoop.main.gentests.classes.AnotherTestedClass;
import randoop.main.gentests.classes.ConstructorDependency;
import randoop.main.gentests.classes.MethodDependency;
import randoop.main.gentests.classes.TestedClass;
import randoop.reflection.AccessibilityPredicate;

public class AutoAddDependenciesTest {

  private final List<@ClassGetName String> defaultTestclass = new ArrayList<>();
  private final boolean defaultTestAddDependencies = GenInputsAbstract.test_add_dependencies;
  private final List<Pattern> defaultOmitClasses = new ArrayList<>();
  private final AccessibilityPredicate accessibility = AccessibilityPredicate.IS_PUBLIC;
  private final Path defaultMethodlist = GenInputsAbstract.methodlist;
  private final List<Pattern> defaultOmitMethods = new ArrayList<>();
  private final List<Path> defaultOmitMethodFile = GenInputsAbstract.omit_methods_file;

  @Before
  public void init() {
    GenInputsAbstract.test_add_dependencies = true;
  }

  @After
  public void deinit() {
    GenInputsAbstract.testclass = defaultTestclass;
    GenInputsAbstract.test_add_dependencies = defaultTestAddDependencies;
    GenInputsAbstract.omit_classes = defaultOmitClasses;
    GenInputsAbstract.methodlist = defaultMethodlist;
    GenInputsAbstract.omit_methods = defaultOmitMethods;
    GenInputsAbstract.omit_methods_file = defaultOmitMethodFile;
  }

  @Test
  public void getClassnamesFromArgsShouldIncludeDependenciesToConstructorOfProvidedClass() {
    GenInputsAbstract.testclass.add(TestedClass.class.getName());

    Set<@ClassGetName String> classnames = GenInputsAbstract.getClassnamesFromArgs(accessibility);
    Set<@ClassGetName String> result =
        GenTests.getDependentClassnamesFromClassnames(classnames, AccessibilityPredicate.IS_PUBLIC);

    Assert.assertTrue(result.contains(ConstructorDependency.class.getName()));
  }

  @Test
  public void getClassnamesFromArgsShouldIncludeDependenciesOfMethodOfProvidedClass() {
    GenInputsAbstract.testclass.add(TestedClass.class.getName());

    Set<@ClassGetName String> classnames = GenInputsAbstract.getClassnamesFromArgs(accessibility);
    Set<@ClassGetName String> result =
        GenTests.getDependentClassnamesFromClassnames(classnames, AccessibilityPredicate.IS_PUBLIC);

    Assert.assertTrue(result.contains(MethodDependency.class.getName()));
  }

  @Test
  public void getClassnamesFromArgsShouldNotIncludeOmittedDependency() {
    GenInputsAbstract.testclass.add(TestedClass.class.getName());
    String methodDependencyPattern = ".*ethod.*";
    GenInputsAbstract.omit_classes.add(Pattern.compile(methodDependencyPattern));

    Set<@ClassGetName String> classnames = GenInputsAbstract.getClassnamesFromArgs(accessibility);
    Set<@ClassGetName String> result =
        GenTests.getDependentClassnamesFromClassnames(classnames, AccessibilityPredicate.IS_PUBLIC);

    Assert.assertFalse(result.contains(MethodDependency.class.getName()));
  }

  @Test
  public void getClassnamesFromArgsShouldNotIncludeDependencyOfOmittedClass() {
    GenInputsAbstract.testclass.add(TestedClass.class.getName());
    GenInputsAbstract.testclass.add(AnotherTestedClass.class.getName());
    Pattern anotherTestedClassPattern = Pattern.compile(".*erTested.*");
    GenInputsAbstract.omit_classes.add(anotherTestedClassPattern);

    Set<@ClassGetName String> classnames = GenInputsAbstract.getClassnamesFromArgs(accessibility);
    Set<@ClassGetName String> result =
        GenTests.getDependentClassnamesFromClassnames(classnames, AccessibilityPredicate.IS_PUBLIC);

    Assert.assertFalse(result.contains(AnotherTestedClass.class.getName()));
    Assert.assertFalse(result.contains(AnotherDependency.class.getName()));
  }

  @Test
  public void getClassnamesFromArgsShouldIncludeDependencyOfProvidedMethod() {
    Set<@ClassGetName String> result =
        GenTests.getDependentClassnamesFromMethodList(
            new File("test/geninput/onemethod.txt").toPath().toAbsolutePath(),
            AccessibilityPredicate.IS_PUBLIC,
            GenInputsAbstract.omit_methods);

    Assert.assertTrue(result.contains(MethodDependency.class.getName()));
  }

  @Test
  public void getClassnamesFromArgsShouldIncludeDependencyOfProvidedConstructor() {
    Set<@ClassGetName String> result =
        GenTests.getDependentClassnamesFromMethodList(
            new File("test/geninput/oneconstructor.txt").toPath().toAbsolutePath(),
            AccessibilityPredicate.IS_PUBLIC,
            GenInputsAbstract.omit_methods);

    Assert.assertTrue(result.contains(ConstructorDependency.class.getName()));
  }

  @Test
  public void getClassnamesFromArgsShouldNotIncludeDependencyToOmittedMethod() {
    Pattern methodPattern = Pattern.compile(".*method.*");
    GenInputsAbstract.omit_methods.add(methodPattern);

    Set<@ClassGetName String> result =
        GenTests.getDependentClassnamesFromMethodList(
            new File("test/geninput/onemethod.txt").toPath().toAbsolutePath(),
            AccessibilityPredicate.IS_PUBLIC,
            GenInputsAbstract.omit_methods);

    Assert.assertFalse(result.contains(MethodDependency.class.getName()));
  }

  @Test
  public void getClassnamesFromArgsShouldNotIncludeDependencyOfOmittedConstructor() {
    Pattern constructorPattern = Pattern.compile(".*TestedClass\\(.*");
    GenInputsAbstract.omit_methods.add(constructorPattern);

    Set<@ClassGetName String> result =
        GenTests.getDependentClassnamesFromMethodList(
            new File("test/geninput/oneconstructor.txt").toPath().toAbsolutePath(),
            AccessibilityPredicate.IS_PUBLIC,
            GenInputsAbstract.omit_methods);

    Assert.assertFalse(result.contains(ConstructorDependency.class.getName()));
  }
}
