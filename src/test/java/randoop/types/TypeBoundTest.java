package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import randoop.reflection.ClassVisitor;
import randoop.reflection.ReflectionManager;
import randoop.reflection.VisibilityPredicate;

/** Uses {@link WildcardBoundExamples} to test handling of type bounds involving variables. */
public class TypeBoundTest {

  @Test
  public void testEnumBound() {
    Type enumType = Type.forClass(Enum.class);
    assertTrue(enumType.isReferenceType());
    List<TypeVariable> typeParameters = ((ReferenceType) enumType).getTypeParameters();
    assertEquals(typeParameters.size(), 1);
    ReferenceType candidateType = ClassOrInterfaceType.forClass(Word.class);
    checkBound(typeParameters.get(0), candidateType);
  }

  @Test
  public void testWildcardBounds() {

    ReferenceType twType = ReferenceType.forClass(TW.class);
    ReferenceType swType = ReferenceType.forClass(SW.class);
    ReferenceType uwType = ReferenceType.forClass(UW.class);
    ReferenceType vwType = ReferenceType.forClass(VW.class);
    ReferenceType wwType = ReferenceType.forClass(WW.class);
    ReferenceType xwType = ReferenceType.forClass(XW.class);
    ReferenceType ywType = ReferenceType.forClass(YW.class);
    Map<String, TypeVariable> argTypes = getArgumentTypes(WildcardBoundExamples.class);

    TypeVariable variable;

    variable = argTypes.get("m1");
    assertTrue("TW satisfies " + variable.getName() + " bound", checkBound(variable, twType));
    assertFalse(
        "String does not satisfy " + variable.getName() + " bound",
        checkBound(variable, JavaTypes.STRING_TYPE));
    assertFalse(
        "SW does not satisfy " + variable.getName() + " bound", checkBound(variable, swType));
    assertFalse(
        "UW does not satisfy " + variable.getName() + " bound", checkBound(variable, uwType));
    assertFalse(
        "VW does not satisfy " + variable.getName() + " bound", checkBound(variable, vwType));
    assertFalse(
        "WW does not satisfy " + variable.getName() + " bound", checkBound(variable, wwType));
    assertFalse(
        "XW does not satisfy " + variable.getName() + " bound", checkBound(variable, xwType));
    assertFalse(
        "YW does not satisfy " + variable.getName() + " bound", checkBound(variable, ywType));

    variable = argTypes.get("m2");
    assertTrue("SW satisfies " + variable.getName() + " bound", checkBound(variable, swType));
    assertFalse(
        "TW does not satisfy " + variable.getName() + " bound", checkBound(variable, twType));
    assertFalse(
        "UW does not satisfy " + variable.getName() + " bound", checkBound(variable, uwType));
    assertFalse(
        "VW does not satisfy " + variable.getName() + " bound", checkBound(variable, vwType));
    assertFalse(
        "WW does not satisfy " + variable.getName() + " bound", checkBound(variable, wwType));
    assertFalse(
        "XW does not satisfy " + variable.getName() + " bound", checkBound(variable, xwType));
    assertFalse(
        "YW does not satisfy " + variable.getName() + " bound", checkBound(variable, ywType));
    assertFalse(
        "String does not satisfy " + variable.getName() + " bound",
        checkBound(variable, JavaTypes.STRING_TYPE));

    variable = argTypes.get("m3");
    assertFalse(
        "SW does not satisfy " + variable.getName() + " bound", checkBound(variable, swType));
    assertFalse(
        "TW does not satisfy " + variable.getName() + " bound", checkBound(variable, twType));
    assertTrue("UW satisfies " + variable.getName() + " bound", checkBound(variable, uwType));
    assertFalse(
        "VW does not satisfy " + variable.getName() + " bound", checkBound(variable, vwType));
    assertFalse(
        "WW does not satisfy " + variable.getName() + " bound", checkBound(variable, wwType));
    assertFalse(
        "XW does not satisfy " + variable.getName() + " bound", checkBound(variable, xwType));
    assertFalse(
        "YW does not satisfy " + variable.getName() + " bound", checkBound(variable, ywType));
    assertFalse(
        "String does not satisfy " + variable.getName() + " bound",
        checkBound(variable, JavaTypes.STRING_TYPE));

    variable = argTypes.get("m4");
    assertFalse(
        "SW does not satisfy " + variable.getName() + " bound", checkBound(variable, swType));
    assertFalse(
        "TW does not satisfy " + variable.getName() + " bound", checkBound(variable, twType));
    assertFalse(
        "UW does not satisfy " + variable.getName() + " bound", checkBound(variable, uwType));
    assertTrue("VW satisfies " + variable.getName() + " bound", checkBound(variable, vwType));
    assertFalse(
        "WW does not satisfy " + variable.getName() + " bound", checkBound(variable, wwType));
    assertFalse(
        "XW does not satisfy " + variable.getName() + " bound", checkBound(variable, xwType));
    assertFalse(
        "YW does not satisfy " + variable.getName() + " bound", checkBound(variable, ywType));
    assertFalse(
        "String does not satisfy " + variable.getName() + " bound",
        checkBound(variable, JavaTypes.STRING_TYPE));

    variable = argTypes.get("m5");
    assertFalse(
        "SW does not satisfy " + variable.getName() + " bound", checkBound(variable, swType));
    assertFalse(
        "TW does not satisfy " + variable.getName() + " bound", checkBound(variable, twType));
    assertFalse(
        "UW does not satisfy " + variable.getName() + " bound", checkBound(variable, uwType));
    assertFalse(
        "VW does not satisfy " + variable.getName() + " bound", checkBound(variable, vwType));
    assertFalse(
        "WW does not satisfy " + variable.getName() + " bound", checkBound(variable, wwType));
    assertFalse(
        "XW does not satisfy " + variable.getName() + " bound", checkBound(variable, xwType));
    assertFalse(
        "YW does not satisfy " + variable.getName() + " bound", checkBound(variable, ywType));
    assertTrue(
        "String satisfies " + variable.getName() + " bound",
        checkBound(variable, JavaTypes.STRING_TYPE));

    variable = argTypes.get("m6");
    assertTrue(
        "SW does not satisfy " + variable.getName() + " bound", checkBound(variable, swType));
    assertTrue(
        "TW does not satisfy " + variable.getName() + " bound", checkBound(variable, twType));
    assertTrue(
        "UW does not satisfy " + variable.getName() + " bound", checkBound(variable, uwType));
    assertTrue(
        "VW does not satisfy " + variable.getName() + " bound", checkBound(variable, vwType));
    assertTrue(
        "WW does not satisfy " + variable.getName() + " bound", checkBound(variable, wwType));
    assertTrue(
        "XW does not satisfy " + variable.getName() + " bound", checkBound(variable, xwType));
    assertTrue(
        "YW does not satisfy " + variable.getName() + " bound", checkBound(variable, ywType));
    assertTrue(
        "String satisfies " + variable.getName() + " bound",
        checkBound(variable, JavaTypes.STRING_TYPE));

    // m7 has two parameters, so not sure what we get
    /*
    variable = argTypes.get("m7");
    assertFalse("SW does not satisfy " + variable.getName() + " bound", checkBound(variable, swType));
    assertFalse("TW does not satisfy " + variable.getName() + " bound", checkBound(variable, twType));
    assertFalse("UW does not satisfy " + variable.getName() + " bound", checkBound(variable, uwType));
    assertFalse("VW does not satisfy " + variable.getName() + " bound", checkBound(variable, vwType));
    assertFalse("WW satisfies " + variable.getName() + " bound", checkBound(variable, wwType));
    assertFalse("XW does not satisfy " + variable.getName() + " bound", checkBound(variable, xwType));
    assertFalse("YW does not satisfy " + variable.getName() + " bound", checkBound(variable, ywType));
    assertFalse(
            "String does not satisfy " + variable.getName() + " bound", checkBound(variable, JavaTypes.STRING_TYPE));

    */

    variable = argTypes.get("m8");
    assertFalse(
        "SW does not satisfy " + variable.getName() + " bound", checkBound(variable, swType));
    assertFalse(
        "TW does not satisfy " + variable.getName() + " bound", checkBound(variable, twType));
    assertFalse(
        "UW does not satisfy " + variable.getName() + " bound", checkBound(variable, uwType));
    assertFalse(
        "VW does not satisfy " + variable.getName() + " bound", checkBound(variable, vwType));
    assertTrue("WW satisfies " + variable.getName() + " bound", checkBound(variable, wwType));
    assertFalse(
        "XW does not satisfy " + variable.getName() + " bound", checkBound(variable, xwType));
    assertFalse(
        "YW does not satisfy " + variable.getName() + " bound", checkBound(variable, ywType));
    assertFalse(
        "String does not satisfy " + variable.getName() + " bound",
        checkBound(variable, JavaTypes.STRING_TYPE));
  }

  private boolean checkBound(TypeVariable typeParameter, ReferenceType candidateType) {
    ParameterBound lowerBound = typeParameter.getLowerTypeBound();
    ParameterBound upperBound = typeParameter.getUpperTypeBound();
    List<TypeVariable> typeParameters = Collections.singletonList(typeParameter);
    Substitution<ReferenceType> substitution = Substitution.forArgs(typeParameters, candidateType);
    return lowerBound.isLowerBound(candidateType, substitution)
        && upperBound.isUpperBound(candidateType, substitution);
  }

  private Map<String, TypeVariable> getArgumentTypes(Class<?> classType) {
    Map<String, TypeVariable> arguments = new LinkedHashMap<>();
    ReflectionManager mgr =
        new ReflectionManager(
            new VisibilityPredicate.PackageVisibilityPredicate(classType.getPackage().getName()));
    mgr.apply(new ArgumentVisitor(arguments), classType);
    return arguments;
  }

  private static class ArgumentVisitor implements ClassVisitor {
    private Map<String, TypeVariable> argTypes;
    private Set<Method> declaredMethods;

    ArgumentVisitor(Map<String, TypeVariable> argTypes) {
      this.argTypes = argTypes;
      declaredMethods = new LinkedHashSet<>();
    }

    @Override
    public void visit(Class<?> c, ReflectionManager reflectionManager) {
      // do nothing for member class
    }

    @Override
    public void visit(Constructor<?> c) {
      addParameters(c.getName(), c.getTypeParameters());
    }

    private void addParameters(String name, java.lang.reflect.TypeVariable<?>[] typeParameters) {
      for (java.lang.reflect.TypeVariable variable : typeParameters) {
        argTypes.put(name, TypeVariable.forType(variable));
      }
    }

    @Override
    public void visit(Method m) {
      if (declaredMethods.contains(m)) {
        addParameters(m.getName(), m.getTypeParameters());
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
