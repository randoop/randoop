package randoop.types;

class TypeBoundExamples {

/** This file includes examples of recursive type bounds (with wildcards) */
static enum Word {
  WORD,
  MOT;
}

static class AW<E> {}

static class BW<E> {}

static class CW<E, F> {}

static class DW<E> {}

static interface TI {}

static class TW extends AW<TI> implements TI {}

static class SW extends AW<BW<? super SW>> {}

static class UW extends CW<AW<UW>, BW<? super UW>> {}

static class VW extends AW<DW<VW>> {}

static class WW extends DW<AW<? extends WW>> {}

static class XW extends AW<YW> {}

static class YW extends BW<XW> {}

static class WildcardBoundExamples {
  public <T1 extends AW<? super T1>> void m1(T1 t) {}

  public <S1 extends AW<BW<? super S1>>> void m2(S1 s) {}

  public <U1 extends CW<AW<U1>, BW<? super U1>>> void m3(U1 u) {}

  public <V1 extends AW<? super DW<V1>>> void m4(V1 v) {}

  public <W1 extends Comparable<? super W1>> void m5(W1 w) {}

  public <U2> void m6(Comparable<? super U2> u) {}

  public <S2 extends AW<? super T2>, T2 extends BW<? super S2>> void m7(S2 s, T2 t) {}

  public <W2 extends DW<? super AW<? extends W2>>> void m8(W2 w) {}
}

static class ExampleW {
  public void m() {
    WildcardBoundExamples wbe = new WildcardBoundExamples();
    wbe.m1(new TW());
    wbe.m2(new SW());
    wbe.m3(new UW());
    wbe.m4(new VW());
    wbe.m5("word");
    wbe.m6("mot");
    wbe.m7(new XW(), new YW());
    wbe.m8(new WW());
  }
}

}
