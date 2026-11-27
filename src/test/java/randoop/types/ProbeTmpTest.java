package randoop.types;

import java.util.Collections;
import org.junit.Test;

public class ProbeTmpTest {
  @Test
  public void probe() throws Exception {
    Class<?> c = Class.forName("randoop.types.ParameterBoundTest$OwnerVariableMemberBound");
    java.lang.reflect.Type declaredBound = c.getTypeParameters()[1].getBounds()[0];
    LazyParameterBound bound =
        (LazyParameterBound) ParameterBound.forType(Collections.emptySet(), declaredBound);
    ReferenceType stringOwner =
        ReferenceType.forType(
            ParameterBoundTest.class.getDeclaredField("stringOwnerInner").getGenericType());
    TypeVariable x = bound.getTypeParameters().get(0);
    Substitution s1 = new Substitution().extendWithIdentity(bound.getTypeParameters());
    System.out.println("s1=" + s1 + " unknown(X)=" + s1.isUnknown(x));
    Substitution s2 = s1.withIdentityFor(bound.getTypeParameters());
    System.out.println("s2=" + s2 + " unknown(X)=" + s2.isUnknown(x));
    ParameterBound b = bound.substituteWithUnknowns(new Substitution());
    ReferenceType bt = ((ReferenceBound) b).getBoundType();
    System.out.println(
        "b=" + b.getClass().getSimpleName() + " typeparams=" + bt.getTypeParameters());
    System.out.println(
        "eager.isUpperBound(s1)=" + new EagerReferenceBound(bt).isUpperBound(stringOwner, s1));
    System.out.println(
        "eager.isUpperBound(s2)=" + new EagerReferenceBound(bt).isUpperBound(stringOwner, s2));
    System.out.println("Subtyping(s1)=" + Subtyping.isSubtypeOrEqual(stringOwner, bt, s1));
    System.out.println("b.isUpperBound(s2)=" + b.isUpperBound(stringOwner, s2));
    System.out.println("result=" + bound.isUpperBound(stringOwner, new Substitution()));
  }
}
