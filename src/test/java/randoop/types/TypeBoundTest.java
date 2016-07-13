package randoop.types;

import org.junit.Test;

import java.lang.reflect.*;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Collections;
import randoop.reflection.ClassVisitor;
import randoop.reflection.PackageVisibilityPredicate;
import randoop.reflection.ReflectionManager;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Uses {@link WildcardBoundExamples} to test handling of recursive type bounds involving variables
 */
public class TypeBoundTest {

  @Test
  public void testEnumBound() {
    GeneralType enumType = GeneralType.forClass(Enum.class);
    List<TypeVariable> typeParameters = enumType.getTypeParameters();
    assert typeParameters.size() == 1 : "Enum only has one type parameter";
    TypeVariable paramType = typeParameters.get(0);
    ParameterBound bound = paramType.getUpperTypeBound();
    assertTrue(
        "bound is satisfied by an enum", bound.isSatisfiedBy(GeneralType.forClass(Word.class)));
    fail("not complete");
  }

  @Test
  public void testWildcardBounds() {
    Set<TypeVariable> argTypes = getArgumentTypes(WildcardBoundExamples.class);
    for (TypeVariable variable : argTypes) {
      System.out.println(variable);
    }
    fail("implementation not complete");
  }

  private Set<TypeVariable> getArgumentTypes(Class<?> classType) {
    Set<TypeVariable> arguments = new HashSet<>();
    ReflectionManager mgr =
        new ReflectionManager(new PackageVisibilityPredicate(classType.getPackage()));
    mgr.apply(new ArgumentVisitor(arguments), classType);
    return arguments;
  }

  private class ArgumentVisitor implements ClassVisitor {
    private Set<TypeVariable> argTypes;
    private Set<Method> declaredMethods;

    ArgumentVisitor(Set<TypeVariable> argTypes) {
      this.argTypes = argTypes;
      declaredMethods = new LinkedHashSet<>();
    }

    @Override
    public void visit(Constructor<?> c) {
      addParameters(c.getTypeParameters());
    }

    private void addParameters(java.lang.reflect.TypeVariable<?>[] typeParameters) {
      for (java.lang.reflect.TypeVariable variable : typeParameters) {
        argTypes.add(TypeVariable.forType(variable));
      }
    }

    @Override
    public void visit(Method m) {
      if (declaredMethods.contains(m)) {
        System.out.println("visiting: " + m);
        addParameters(m.getTypeParameters());
      }
    }

    @Override
    public void visit(Field f) {}

    @Override
    public void visit(Enum<?> e) {}

    @Override
    public void visitBefore(Class<?> c) {
      Collections.addAll(declaredMethods, c.getDeclaredMethods());
    }

    @Override
    public void visitAfter(Class<?> c) {}
  }
}
