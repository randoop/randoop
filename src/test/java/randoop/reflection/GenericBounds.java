package randoop.reflection;

/**
 * For randoop.reflection.InstantiationTest.
 */
enum Word {
  WORD,
  MOT;
}

class AW<E> {}

class BW<E> {}

class CW<E, F> {}

class DW<E> {}

interface TI {}

class TW extends AW<TI> implements TI {}

class SW extends AW<BW<? super SW>> {}

class UW extends CW<AW<UW>, BW<? super UW>> {}

class VW extends AW<DW<VW>> {}

class WW extends DW<AW<? extends WW>> {}

class XW extends AW<YW> {}

class YW extends BW<XW> {}

public class GenericBounds {
  public <I> void m00(I i) {}

  public <I extends Comparable<I>> void m01(I i) {}

  public <I extends Comparable<? super I>> void m02(I i) {}

  public <I, C extends I> void m03(C c) {}

  public <T1 extends AW<? super T1>> void m04(T1 t) {}

  public <S1 extends AW<BW<? super S1>>> void m05(S1 s) {}

  public <U1 extends CW<AW<U1>, BW<? super U1>>> void m06(U1 u) {}

  public <V1 extends AW<? super DW<V1>>> void m07(V1 v) {}

  public <W1 extends Comparable<? super W1>> void m08(W1 w) {}

  public <U2> void m09(Comparable<? super U2> u) {}

  public <S2 extends AW<? super T2>, T2 extends BW<? super S2>> void m10(S2 s, T2 t) {}

  public <W2 extends DW<? super AW<? extends W2>>> void m11(W2 w) {}
}
