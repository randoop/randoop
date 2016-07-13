package randoop.types;

/**
 * This file includes examples of recursive type bounds (with wildcards)
 */
enum Word {
  WORD,
  MOT;
}

class AW<E> {}

class BW<E> {}

class CW<E, F> {}

class DW<E> {}

class TW extends AW<TW> {}

class SW extends AW<BW<? super SW>> {}

class UW extends CW<AW<UW>, BW<? super UW>> {}

class VW extends AW<DW<VW>> {}

class WildcardBoundExamples {
  public <T extends AW<? super T>> void m1(T t) {}

  public <S extends AW<BW<? super S>>> void m2(S s) {}

  public <U extends CW<AW<U>, BW<? super U>>> void m3(U u) {}

  public <V extends AW<? super DW<V>>> void m4(V v) {}

  public <W extends Comparable<? super W>> void m5(W w) {}

  public <U> void m6(Comparable<? super U> u) {}
}

class ExampleW {
  public void m() {
    WildcardBoundExamples wbe = new WildcardBoundExamples();
    wbe.m1(new TW());
    wbe.m2(new SW());
    wbe.m3(new UW());
    wbe.m4(new VW());
    wbe.m5("word");
    wbe.m6("mot");
  }
}
