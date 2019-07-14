package randoop.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static randoop.types.TypeBoundExamples.SW;
import static randoop.types.TypeBoundExamples.TW;
import static randoop.types.TypeBoundExamples.UW;
import static randoop.types.TypeBoundExamples.VW;
import static randoop.types.TypeBoundExamples.WW;
import static randoop.types.TypeBoundExamples.WildcardBoundExamples;
import static randoop.types.TypeBoundExamples.Word;
import static randoop.types.TypeBoundExamples.XW;
import static randoop.types.TypeBoundExamples.YW;

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
    checkBound(typeParameters.get(0), candidateType, true);
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
    checkBound(variable, twType, true);
    checkBound(variable, JavaTypes.STRING_TYPE, false);
    checkBound(variable, swType, false);
    checkBound(variable, uwType, false);
    checkBound(variable, vwType, false);
    checkBound(variable, wwType, false);
    checkBound(variable, xwType, false);
    checkBound(variable, ywType, false);

    variable = argTypes.get("m2");
    checkBound(variable, swType, true);
    checkBound(variable, twType, false);
    checkBound(variable, uwType, false);
    checkBound(variable, vwType, false);
    checkBound(variable, wwType, false);
    checkBound(variable, xwType, false);
    checkBound(variable, ywType, false);
    checkBound(variable, JavaTypes.STRING_TYPE, false);

    variable = argTypes.get("m3");
    checkBound(variable, swType, false);
    checkBound(variable, twType, false);
    checkBound(variable, uwType, true);
    checkBound(variable, vwType, false);
    checkBound(variable, wwType, false);
    checkBound(variable, xwType, false);
    checkBound(variable, ywType, false);
    checkBound(variable, JavaTypes.STRING_TYPE, false);

    variable = argTypes.get("m4");
    checkBound(variable, swType, false);
    checkBound(variable, twType, false);
    checkBound(variable, uwType, false);
    checkBound(variable, vwType, true);
    checkBound(variable, wwType, false);
    checkBound(variable, xwType, false);
    checkBound(variable, ywType, false);
    checkBound(variable, JavaTypes.STRING_TYPE, false);

    variable = argTypes.get("m5");
    checkBound(variable, swType, false);
    checkBound(variable, twType, false);
    checkBound(variable, uwType, false);
    checkBound(variable, vwType, false);
    checkBound(variable, wwType, false);
    checkBound(variable, xwType, false);
    checkBound(variable, ywType, false);
    checkBound(variable, JavaTypes.STRING_TYPE, true);

    variable = argTypes.get("m6");
    checkBound(variable, swType, true);
    checkBound(variable, twType, true);
    checkBound(variable, uwType, true);
    checkBound(variable, vwType, true);
    checkBound(variable, wwType, true);
    checkBound(variable, xwType, true);
    checkBound(variable, ywType, true);
    checkBound(variable, JavaTypes.STRING_TYPE, true);

    // m7 has two parameters, so not sure what we get
    /*
    variable = argTypes.get("m7");
    checkBound(variable, swType, false);
    checkBound(variable, twType, false);
    checkBound(variable, uwType, false);
    checkBound(variable, vwType, false);
    checkBound(variable, wwType, false);
    checkBound(variable, xwType, false);
    checkBound(variable, ywType, false);
    checkBound(variable, JavaTypes.STRING_TYPE, false);

    */

    variable = argTypes.get("m8");
    checkBound(variable, swType, false);
    checkBound(variable, twType, false);
    checkBound(variable, uwType, false);
    checkBound(variable, vwType, false);
    checkBound(variable, wwType, true);
    checkBound(variable, xwType, false);
    checkBound(variable, ywType, false);
    checkBound(variable, JavaTypes.STRING_TYPE, false);
  }

  private void checkBound(
      TypeVariable typeParameter, ReferenceType candidateType, boolean expected) {
    ParameterBound lowerBound = typeParameter.getLowerTypeBound();
    ParameterBound upperBound = typeParameter.getUpperTypeBound();
    Substitution substitution = new Substitution(typeParameter, candidateType);
    boolean lbResult = lowerBound.isLowerBound(candidateType, substitution);
    boolean ubResult = upperBound.isUpperBound(candidateType, substitution);
    boolean result = lbResult && ubResult;
    if (expected != result) {
      throw new Error(
          String.format(
              "%s %s bound on %s (%s)%n  (lower satisfaction=%s, upper satisfaction=%s)",
              candidateType,
              (result ? "unexpectedly satisfies" : "does not satisfy"),
              typeParameter.getName(),
              typeParameter,
              lbResult,
              ubResult));
    }
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
      for (java.lang.reflect.TypeVariable<?> variable : typeParameters) {
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
