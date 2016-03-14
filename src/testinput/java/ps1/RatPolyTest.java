package ps1;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

@SuppressWarnings("unused")
public class RatPolyTest extends TestCase {

  private RatNum[] num =
      new RatNum[] {
        new RatNum(0),
        new RatNum(1),
        new RatNum(2),
        new RatNum(3),
        new RatNum(4),
        new RatNum(5),
        new RatNum(6),
        new RatNum(7),
        new RatNum(8),
        new RatNum(9),
        new RatNum(10)
      };

  private RatNum nanNum = (new RatNum(1)).div(new RatNum(0));

  private RatPoly rp(int coef, int expt) {
    return new RatPoly(coef, expt);
  }

  private RatPoly parse(String s) {
    return RatPoly.parse(s);
  }

  // 0
  private RatPoly zero;
  // x^2 + 2*x
  private RatPoly _XSqPlus2X;
  // 2*x^2 + x
  private RatPoly _2XSqPlusX;
  private boolean isSetup = false;

  private void checkInitRatPolyFields() {
    if (!isSetup) {
      zero = new RatPoly();
      _XSqPlus2X = rp(1, 2).add(rp(1, 1)).add(rp(1, 1));
      _2XSqPlusX = rp(1, 2).add(rp(1, 2)).add(rp(1, 1));
      isSetup = true;
    }
  }

  public RatPolyTest(String name) {
    super(name);
  }

  // only unparse is tested here
  private void eq(RatPoly p, String target) {
    String t = p.unparse();
    assertEquals(target, t);
  }

  private void eq(RatPoly p, String target, String message) {
    String t = p.unparse();
    assertEquals(message, target, t);
  }

  public void testNoArgCtor() {
    eq(new RatPoly(), "0");
  }

  public void testTwoArgCtor1() {
    eq(rp(0, 0), "0");
  }

  public void testTwoArgCtor2() {
    eq(rp(0, 1), "0");
  }

  public void testTwoArgCtor3() {
    eq(rp(1, 0), "1");
  }

  public void testTwoArgCtor4() {
    eq(rp(-1, 0), "-1");
  }

  public void testTwoArgCtor5() {
    eq(rp(1, 1), "x");
  }

  public void testTwoArgCtor6() {}

  public void testTwoArgCtor7() {
    eq(rp(1, 2), "x^2");
  }

  public void testTwoArgCtor8() {
    eq(rp(2, 2), "2*x^2");
  }

  public void testTwoArgCtor9() {
    eq(rp(2, 3), "2*x^3");
  }

  public void testTwoArgCtor10() {
    eq(rp(-2, 3), "-2*x^3");
  }

  public void testTwoArgCtor11() {
    eq(rp(-1, 1), "-x");
  }

  public void testTwoArgCtor12() {
    eq(rp(-1, 3), "-x^3");
  }

  public void testDegree0() {
    assertTrue("x^0 degree 0", rp(1, 0).degree() == 0);
  }

  public void testDegree1() {
    assertTrue("x^1 degree 1", rp(1, 1).degree() == 1);
  }

  public void testDegree2() {
    assertTrue("x^100 degree 100", rp(1, 100).degree() == 100);
  }

  public void testDegree3() {
    assertTrue("0*x^100 degree 0", rp(0, 100).degree() == 0);
  }

  public void testDegree4() {
    assertTrue("0*x^0 degree 0", rp(0, 0).degree() == 0);
  }

  public void testAdd0() {
    eq(rp(1, 0).add(rp(1, 0)), "2");
  }

  public void testAdd1() {
    eq(rp(1, 0).add(rp(5, 0)), "6");
  }

  public void testAdd2() {
    eq(rp(1, 1).add(rp(1, 1)), "2*x");
  }

  public void testAdd3() {
    eq(rp(1, 2).add(rp(1, 2)), "2*x^2");
  }

  public void testAdd4() {
    eq(rp(1, 2).add(rp(1, 1)), "x^2+x");
  }

  public void testAdd5() {
    checkInitRatPolyFields();
    eq(_XSqPlus2X, "x^2+2*x");
  }

  public void testAdd6() {
    checkInitRatPolyFields();
    eq(_2XSqPlusX, "2*x^2+x");
  }

  public void testAdd7() {
    eq(rp(1, 3).add(rp(1, 1)), "x^3+x");
  }

  public void testSub8() {
    eq(rp(1, 1).sub(rp(1, 0)), "x-1");
  }

  public void testSub9() {
    eq(rp(1, 1).add(rp(1, 0)), "x+1");
  }

  public void testMul0() {
    eq(rp(0, 0).mul(rp(0, 0)), "0");
  }

  public void testMul1() {
    eq(rp(1, 0).mul(rp(1, 0)), "1");
  }

  public void testMul2() {
    eq(rp(1, 0).mul(rp(2, 0)), "2");
  }

  public void testMul3() {
    eq(rp(2, 0).mul(rp(2, 0)), "4");
  }

  public void testMul4() {
    eq(rp(1, 0).mul(rp(1, 1)), "x");
  }

  public void testMul5() {
    eq(rp(1, 1).mul(rp(1, 1)), "x^2");
  }

  public void testMul6() {
    eq(rp(1, 1).sub(rp(1, 0)).mul(rp(1, 1).add(rp(1, 0))), "x^2-1");
  }

  public void testOpsWithNaN0(RatPoly p) {
    RatPoly nan = RatPoly.parse("NaN");
    eq(p.add(nan), "NaN");
  }

  public void testOpsWithNaN1(RatPoly p) {
    RatPoly nan = RatPoly.parse("NaN");
    eq(nan.add(p), "NaN");
  }

  public void testOpsWithNaN2(RatPoly p) {
    RatPoly nan = RatPoly.parse("NaN");
    eq(p.sub(nan), "NaN");
  }

  public void testOpsWithNaN3(RatPoly p) {
    RatPoly nan = RatPoly.parse("NaN");
    eq(nan.sub(p), "NaN");
  }

  public void testOpsWithNaN4(RatPoly p) {
    RatPoly nan = RatPoly.parse("NaN");
    eq(p.mul(nan), "NaN");
  }

  public void testOpsWithNaN5(RatPoly p) {
    RatPoly nan = RatPoly.parse("NaN");
    eq(nan.mul(p), "NaN");
  }

  public void testOpsWithNaN6(RatPoly p) {
    RatPoly nan = RatPoly.parse("NaN");
    eq(p.div(nan), "NaN");
  }

  public void testOpsWithNaN7(RatPoly p) {
    RatPoly nan = RatPoly.parse("NaN");
    eq(nan.div(p), "NaN");
  }

  public void testOpsWithNaN001() {
    testOpsWithNaN0(rp(0, 0));
  }

  public void testOpsWithNaN101() {
    testOpsWithNaN1(rp(0, 0));
  }

  public void testOpsWithNaN201() {
    testOpsWithNaN2(rp(0, 0));
  }

  public void testOpsWithNaN301() {
    testOpsWithNaN3(rp(0, 0));
  }

  public void testOpsWithNaN401() {
    testOpsWithNaN4(rp(0, 0));
  }

  public void testOpsWithNaN501() {
    testOpsWithNaN5(rp(0, 0));
  }

  public void testOpsWithNaN601() {
    testOpsWithNaN6(rp(0, 0));
  }

  public void testOpsWithNaN701() {
    testOpsWithNaN7(rp(0, 0));
  }

  public void testOpsWithNaN002() {
    testOpsWithNaN0(rp(0, 1));
  }

  public void testOpsWithNaN102() {
    testOpsWithNaN1(rp(0, 1));
  }

  public void testOpsWithNaN202() {
    testOpsWithNaN2(rp(0, 1));
  }

  public void testOpsWithNaN302() {
    testOpsWithNaN3(rp(0, 1));
  }

  public void testOpsWithNaN402() {
    testOpsWithNaN4(rp(0, 1));
  }

  public void testOpsWithNaN502() {
    testOpsWithNaN5(rp(0, 1));
  }

  public void testOpsWithNaN602() {
    testOpsWithNaN6(rp(0, 1));
  }

  public void testOpsWithNaN702() {
    testOpsWithNaN7(rp(0, 1));
  }

  public void testOpsWithNaN003() {
    testOpsWithNaN0(rp(1, 0));
  }

  public void testOpsWithNaN103() {
    testOpsWithNaN1(rp(1, 0));
  }

  public void testOpsWithNaN203() {
    testOpsWithNaN2(rp(1, 0));
  }

  public void testOpsWithNaN303() {
    testOpsWithNaN3(rp(1, 0));
  }

  public void testOpsWithNaN403() {
    testOpsWithNaN4(rp(1, 0));
  }

  public void testOpsWithNaN503() {
    testOpsWithNaN5(rp(1, 0));
  }

  public void testOpsWithNaN603() {
    testOpsWithNaN6(rp(1, 0));
  }

  public void testOpsWithNaN703() {
    testOpsWithNaN7(rp(1, 0));
  }

  public void testOpsWithNaN004() {
    testOpsWithNaN0(rp(1, 1));
  }

  public void testOpsWithNaN104() {
    testOpsWithNaN1(rp(1, 1));
  }

  public void testOpsWithNaN204() {
    testOpsWithNaN2(rp(1, 1));
  }

  public void testOpsWithNaN304() {
    testOpsWithNaN3(rp(1, 1));
  }

  public void testOpsWithNaN404() {
    testOpsWithNaN4(rp(1, 1));
  }

  public void testOpsWithNaN504() {
    testOpsWithNaN5(rp(1, 1));
  }

  public void testOpsWithNaN604() {
    testOpsWithNaN6(rp(1, 1));
  }

  public void testOpsWithNaN704() {
    testOpsWithNaN7(rp(1, 1));
  }

  public void testOpsWithNaN005() {
    testOpsWithNaN0(rp(2, 0));
  }

  public void testOpsWithNaN105() {
    testOpsWithNaN1(rp(2, 0));
  }

  public void testOpsWithNaN205() {
    testOpsWithNaN2(rp(2, 0));
  }

  public void testOpsWithNaN305() {
    testOpsWithNaN3(rp(2, 0));
  }

  public void testOpsWithNaN405() {
    testOpsWithNaN4(rp(2, 0));
  }

  public void testOpsWithNaN505() {
    testOpsWithNaN5(rp(2, 0));
  }

  public void testOpsWithNaN605() {
    testOpsWithNaN6(rp(2, 0));
  }

  public void testOpsWithNaN705() {
    testOpsWithNaN7(rp(2, 0));
  }

  public void testOpsWithNaN006() {
    testOpsWithNaN0(rp(2, 1));
  }

  public void testOpsWithNaN106() {
    testOpsWithNaN1(rp(2, 1));
  }

  public void testOpsWithNaN206() {
    testOpsWithNaN2(rp(2, 1));
  }

  public void testOpsWithNaN306() {
    testOpsWithNaN3(rp(2, 1));
  }

  public void testOpsWithNaN406() {
    testOpsWithNaN4(rp(2, 1));
  }

  public void testOpsWithNaN506() {
    testOpsWithNaN5(rp(2, 1));
  }

  public void testOpsWithNaN606() {
    testOpsWithNaN6(rp(2, 1));
  }

  public void testOpsWithNaN706() {
    testOpsWithNaN7(rp(2, 1));
  }

  public void testOpsWithNaN007() {
    testOpsWithNaN0(rp(0, 2));
  }

  public void testOpsWithNaN107() {
    testOpsWithNaN1(rp(0, 2));
  }

  public void testOpsWithNaN207() {
    testOpsWithNaN2(rp(0, 2));
  }

  public void testOpsWithNaN307() {
    testOpsWithNaN3(rp(0, 2));
  }

  public void testOpsWithNaN407() {
    testOpsWithNaN4(rp(0, 2));
  }

  public void testOpsWithNaN507() {
    testOpsWithNaN5(rp(0, 2));
  }

  public void testOpsWithNaN607() {
    testOpsWithNaN6(rp(0, 2));
  }

  public void testOpsWithNaN707() {
    testOpsWithNaN7(rp(0, 2));
  }

  public void testOpsWithNaN008() {
    testOpsWithNaN0(rp(1, 2));
  }

  public void testOpsWithNaN108() {
    testOpsWithNaN1(rp(1, 2));
  }

  public void testOpsWithNaN208() {
    testOpsWithNaN2(rp(1, 2));
  }

  public void testOpsWithNaN308() {
    testOpsWithNaN3(rp(1, 2));
  }

  public void testOpsWithNaN408() {
    testOpsWithNaN4(rp(1, 2));
  }

  public void testOpsWithNaN508() {
    testOpsWithNaN5(rp(1, 2));
  }

  public void testOpsWithNaN608() {
    testOpsWithNaN6(rp(1, 2));
  }

  public void testOpsWithNaN708() {
    testOpsWithNaN7(rp(1, 2));
  }

  public void testImmutabilityOfOperations0() {
    // not the most thorough testclasses possible, but hopefully will
    // catch the easy cases early on...
    RatPoly one = rp(1, 0);
    RatPoly two = rp(2, 0);

    one.degree();
    two.degree();
    eq(one, "1", "Degree mutates receiver!");
    eq(two, "2", "Degree mutates receiver!");
  }

  public void testImmutabilityOfOperations1() {
    // not the most thorough testclasses possible, but hopefully will
    // catch the easy cases early on...
    RatPoly one = rp(1, 0);
    RatPoly two = rp(2, 0);

    one.coeff(0);
    two.coeff(0);
    eq(one, "1", "Coeff mutates receiver!");
    eq(two, "2", "Coeff mutates receiver!");
  }

  public void testImmutabilityOfOperations2() {
    // not the most thorough testclasses possible, but hopefully will
    // catch the easy cases early on...
    RatPoly one = rp(1, 0);
    RatPoly two = rp(2, 0);

    one.isNaN();
    two.isNaN();
    eq(one, "1", "isNaN mutates receiver!");
    eq(two, "2", "isNaN mutates receiver!");
  }

  public void testImmutabilityOfOperations3() {
    // not the most thorough testclasses possible, but hopefully will
    // catch the easy cases early on...
    RatPoly one = rp(1, 0);
    RatPoly two = rp(2, 0);

    one.eval(0.0);
    two.eval(0.0);
    eq(one, "1", "eval mutates receiver!");
    eq(two, "2", "eval mutates receiver!");
  }

  public void testImmutabilityOfOperations4() {
    // not the most thorough testclasses possible, but hopefully will
    // catch the easy cases early on...
    RatPoly one = rp(1, 0);
    RatPoly two = rp(2, 0);

    one.negate();
    two.negate();
    eq(one, "1", "Negate mutates receiver!");
    eq(two, "2", "Negate mutates receiver!");
  }

  public void testImmutabilityOfOperations5() {
    // not the most thorough testclasses possible, but hopefully will
    // catch the easy cases early on...
    RatPoly one = rp(1, 0);
    RatPoly two = rp(2, 0);

    one.add(two);
    eq(one, "1", "Add mutates receiver!");
    eq(two, "2", "Add mutates argument!");
  }

  public void testImmutabilityOfOperations6() {
    // not the most thorough testclasses possible, but hopefully will
    // catch the easy cases early on...
    RatPoly one = rp(1, 0);
    RatPoly two = rp(2, 0);

    one.sub(two);
    eq(one, "1", "Sub mutates receiver!");
    eq(two, "2", "Sub mutates argument!");
  }

  public void testImmutabilityOfOperations7() {
    // not the most thorough testclasses possible, but hopefully will
    // catch the easy cases early on...
    RatPoly one = rp(1, 0);
    RatPoly two = rp(2, 0);

    one.mul(two);
    eq(one, "1", "Mul mutates receiver!");
    eq(two, "2", "Mul mutates argument!");
  }

  public void testImmutabilityOfOperations8() {
    // not the most thorough testclasses possible, but hopefully will
    // catch the easy cases early on...
    RatPoly one = rp(1, 0);
    RatPoly two = rp(2, 0);

    one.div(two);
    eq(one, "1", "Div mutates receiver!");
    eq(two, "2", "Div mutates argument!");
  }

  public void testEval0() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" 0 at 0 ", 0.0, zero.eval(0.0), 0.0001);
  }

  public void testEval1() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" 0 at 1 ", 0.0, zero.eval(1.0), 0.0001);
  }

  public void testEval2() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" 0 at 2 ", 0.0, zero.eval(2.0), 0.0001);
  }

  public void testEval3() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" 1 at 0 ", 1.0, one.eval(0.0), 0.0001);
  }

  public void testEval4() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" 1 at 1 ", 1.0, one.eval(1.0), 0.0001);
  }

  public void testEval5() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" 1 at 1 ", 1.0, one.eval(2.0), 0.0001);
  }

  public void testEval6() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" x at 0 ", 0.0, _X.eval(0.0), 0.0001);
  }

  public void testEval7() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" x at 1 ", 1.0, _X.eval(1.0), 0.0001);
  }

  public void testEval8() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" x at 2 ", 2.0, _X.eval(2.0), 0.0001);
  }

  public void testEval9() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" 2*x at 0 ", 0.0, _2X.eval(0.0), 0.0001);
  }

  public void testEval95() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" 2*x at 1 ", 2.0, _2X.eval(1.0), 0.0001);
  }

  public void testEval10() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" 2*x at 2 ", 4.0, _2X.eval(2.0), 0.0001);
  }

  public void testEval11() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" x^2 at 0 ", 0.0, _XSq.eval(0.0), 0.0001);
  }

  public void testEval12() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" x^2 at 1 ", 1.0, _XSq.eval(1.0), 0.0001);
  }

  public void testEval13() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" x^2 at 2 ", 4.0, _XSq.eval(2.0), 0.0001);
  }

  public void testEval14() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" x^2-2*x at 0 ", 0.0, _XSq_minus_2X.eval(0.0), 0.0001);
  }

  public void testEval15() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" x^2-2*x at 1 ", -1.0, _XSq_minus_2X.eval(1.0), 0.0001);
  }

  public void testEval16() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" x^2-2*x at 2 ", 0.0, _XSq_minus_2X.eval(2.0), 0.0001);
  }

  public void testEval17() {
    RatPoly zero = new RatPoly();
    RatPoly one = new RatPoly(1, 0);
    RatPoly _X = new RatPoly(1, 1);
    RatPoly _2X = new RatPoly(2, 1);
    RatPoly _XSq = new RatPoly(1, 2);
    RatPoly _XSq_minus_2X = _XSq.sub(_2X);

    assertEquals(" x^2-2*x at 3 ", 3.0, _XSq_minus_2X.eval(3.0), 0.0001);
  }

  // parses s into p, and then checks that it is as anticipATED
  // forall i, parse(s).coeff(expts[i]) = anticipCoeffForExpts(i)
  private void eqP(String s, int anticipDegree, int[] expts, RatNum[] anticipCoeffForExpts) {
    RatPoly p = parse(s);
    eqP(p, s, anticipDegree, expts, anticipCoeffForExpts);
  }
  // checks that 'p' is as anticipATED (unparse, degree, and coeff are tested)
  // forall i, parse(s).coeff(expts[i]) = anticipCoeffForExpts(i)
  private void eqP(
      RatPoly p, String s, int anticipDegree, int[] expts, RatNum[] anticipCoeffForExpts) {
    eq(p, s);
    assertTrue(p.degree() == anticipDegree);
    for (int i = 0; i < expts.length; i++) {
      assertTrue(
          "wrong coeff; "
              + "anticipated: "
              + anticipCoeffForExpts[i]
              + "; received: "
              + p.coeff(expts[i])
              + " received: "
              + p
              + " anticipated:"
              + s,
          p.coeff(expts[i]).equals(anticipCoeffForExpts[i]));
    }
  }

  public void testParseSimple0() {
    eqP("0", 0, new int[] {0}, new RatNum[] {num[0]});
  }

  public void testParseSimple1() {
    eqP("x", 1, new int[] {1, 0}, new RatNum[] {num[1], num[0]});
  }

  public void testParseSimple2() {
    eqP("x^2", 2, new int[] {2, 1, 0}, new RatNum[] {num[1], num[0], num[0]});
  }

  public void testParseMultTerms0() {
    eqP("x^3+x^2", 3, new int[] {3, 2}, new RatNum[] {num[1], num[1]});
  }

  public void testParseMultTerms1() {
    eqP("x^3-x^2", 3, new int[] {3, 2}, new RatNum[] {num[1], num[1].negate()});
  }

  public void testParseMultTerms2() {
    eqP("x^100+x^2", 100, new int[] {100, 2}, new RatNum[] {num[1], num[1]});
  }

  public void testParseLeadingNeg0() {
    eqP("-x^2", 2, new int[] {2, 1, 0}, new RatNum[] {num[1].negate(), num[0], num[0]});
  }

  public void testParseLeadingNeg1() {
    eqP("-x^2+1", 2, new int[] {2, 1, 0}, new RatNum[] {num[1].negate(), num[0], num[1]});
  }

  public void testParseLeadingNeg2() {
    eqP("-x^2+x", 2, new int[] {2, 1, 0}, new RatNum[] {num[1].negate(), num[1], num[0]});
  }

  public void testParseLeadingConstants0() {
    eqP("10*x", 1, new int[] {2, 1, 0}, new RatNum[] {num[0], num[10], num[0]});
  }

  public void testParseLeadingConstants1() {
    eqP("10*x^100+x^2", 100, new int[] {100, 2, 0}, new RatNum[] {num[10], num[1], num[0]});
  }

  public void testParseLeadingConstants2() {
    eqP(
        "10*x^100+100*x^2",
        100,
        new int[] {100, 2, 0},
        new RatNum[] {num[10], new RatNum(100), num[0]});
  }

  public void testParseLeadingConstants3() {
    eqP(
        "-10*x^100+100*x^2",
        100,
        new int[] {100, 2, 0},
        new RatNum[] {num[10].negate(), new RatNum(100), num[0]});
  }

  public void testParseRationals0() {
    eqP("1/2", 0, new int[] {0}, new RatNum[] {num[1].div(num[2])});
  }

  public void testParseRationals1() {
    eqP("1/2*x", 1, new int[] {1}, new RatNum[] {num[1].div(num[2])});
  }

  public void testParseRationals2() {
    eqP("x+1/3", 1, new int[] {1, 0}, new RatNum[] {num[1], num[1].div(num[3])});
  }

  public void testParseRationals3() {
    eqP("1/2*x+1/3", 1, new int[] {1, 0}, new RatNum[] {num[1].div(num[2]), num[1].div(num[3])});
  }

  public void testParseRationals4() {
    eqP("1/2*x+3/2", 1, new int[] {1, 0}, new RatNum[] {num[1].div(num[2]), num[3].div(num[2])});
  }

  public void testParseRationals5() {
    eqP(
        "1/2*x^10+3/2",
        10,
        new int[] {10, 0},
        new RatNum[] {num[1].div(num[2]), num[3].div(num[2])});
  }

  public void testParseRationals6() {
    eqP(
        "1/2*x^10+3/2*x^2+1",
        10,
        new int[] {10, 2, 0},
        new RatNum[] {num[1].div(num[2]), num[3].div(num[2]), num[1]});
  }

  public void testParseNaN() {
    eq(parse("NaN"), "NaN");
  }

  public void testCoeff0() {
    checkInitRatPolyFields();
    // coeff already gets some grunt testing in eqP; checking an interesting
    // input here...

    assertTrue(_XSqPlus2X.coeff(-1).equals(num[0]));
  }

  public void testCoeff1() {
    checkInitRatPolyFields();
    // coeff already gets some grunt testing in eqP; checking an interesting
    // input here...

    assertTrue(_XSqPlus2X.coeff(-10).equals(num[0]));
  }

  public void testCoeff2() {
    checkInitRatPolyFields();
    // coeff already gets some grunt testing in eqP; checking an interesting
    // input here...

    assertTrue(_2XSqPlusX.coeff(-1).equals(num[0]));
  }

  public void testCoeff3() {
    checkInitRatPolyFields();
    // coeff already gets some grunt testing in eqP; checking an interesting
    // input here...

    assertTrue(_2XSqPlusX.coeff(-10).equals(num[0]));
  }

  public void testCoeff4() {
    checkInitRatPolyFields();
    // coeff already gets some grunt testing in eqP; checking an interesting
    // input here...

    assertTrue(zero.coeff(-10).equals(num[0]));
  }

  public void testCoeff5() {
    checkInitRatPolyFields();
    // coeff already gets some grunt testing in eqP; checking an interesting
    // input here...

    assertTrue(zero.coeff(-1).equals(num[0]));
  }

  public void testDiv0() {
    // 0/x = 0
    eq(rp(0, 1).div(rp(1, 1)), "0");
  }

  public void testDiv1() {

    // x/x = 1
    eq(rp(1, 1).div(rp(1, 1)), "1");
  }

  public void testDiv2() {
    // -x/x = -1
    eq(rp(-1, 1).div(rp(1, 1)), "-1");
  }

  public void testDiv3() {

    // -x/-x = -1
    eq(rp(1, 1).div(rp(-1, 1)), "-1");
  }

  public void testDiv4() {
    // -x/-x = 1
    eq(rp(-1, 1).div(rp(-1, 1)), "1");
  }

  public void testDiv5() {
    // -x^2/x = -x
    eq(rp(-1, 2).div(rp(1, 1)), "-x");
  }

  public void testDiv55() {
    // x^100/x^1000 = 0
    eq(rp(1, 100).div(rp(1, 1000)), "0");
  }

  public void testDiv6() {
    // x^100/x = x^99
    eq(rp(1, 100).div(rp(1, 1)), "x^99");
  }

  public void testDiv7() {
    // x^99/x^98 = x
    eq(rp(1, 99).div(rp(1, 98)), "x");
  }

  public void testDiv8() {
    // x^10 / x = x^9 (r: 0)
    eq(rp(1, 10).div(rp(1, 1)), "x^9");
  }

  public void testDiv9() {
    // x^10 / x^3+x^2 = x^7-x^6+x^5-x^4+x^3-x^2+x-1  (r: -x^2)
    eq(rp(1, 10).div(rp(1, 3).add(rp(1, 2))), "x^7-x^6+x^5-x^4+x^3-x^2+x-1");
  }

  public void testDiv10() {
    // x^10 / x^3+x^2+x = x^7-x^6+x^4-x^3+x-1 (r: -x)
    eq(rp(1, 10).div(rp(1, 3).add(rp(1, 2).add(rp(1, 1)))), "x^7-x^6+x^4-x^3+x-1");
  }

  public void testDiv11() {
    // x^10+x^5 / x = x^9+x^4 (r: 0)
    eq(rp(1, 10).add(rp(1, 5)).div(rp(1, 1)), "x^9+x^4");
  }

  public void testDiv13() {
    // x^10+x^5 / x^3 = x^7+x^2 (r: 0)
    eq(rp(1, 10).add(rp(1, 5)).div(rp(1, 3)), "x^7+x^2");
  }

  public void testDiv12() {

    // x^10+x^5 / x^3+x+3 = x^7-x^5-3*x^4+x^3+7*x^2+8*x-10 (r: 29*x^2+14*x-30)
    eq(
        rp(1, 10).add(rp(1, 5)).div(rp(1, 3).add(rp(1, 1)).add(rp(3, 0))),
        "x^7-x^5-3*x^4+x^3+7*x^2+8*x-10");
  }

  public void testDivComplexI0() {
    // (x+1)*(x+1) = x^2+2*x+1
    eq(rp(1, 2).add(rp(2, 1)).add(rp(1, 0)).div(rp(1, 1).add(rp(1, 0))), "x+1");
  }

  public void testDivComplexI1() {
    // (x-1)*(x+1) = x^2-1
    eq(rp(1, 2).add(rp(-1, 0)).div(rp(1, 1).add(rp(1, 0))), "x-1");
  }

  public void testDivComplexII1() {
    // x^8+2*x^6+8*x^5+2*x^4+17*x^3+11*x^2+8*x+3 =
    // (x^3+2*x+1) * (x^5+7*x^2+2*x+3)
    RatPoly large =
        rp(1, 8)
            .add(rp(2, 6))
            .add(rp(8, 5))
            .add(rp(2, 4))
            .add(rp(17, 3))
            .add(rp(11, 2))
            .add(rp(8, 1))
            .add(rp(3, 0));
    // x^3+2*x+1
    RatPoly sub1 = rp(1, 3).add(rp(2, 1)).add(rp(1, 0));
    // x^5+7*x^2+2*x+3
    RatPoly sub2 = rp(1, 5).add(rp(7, 2)).add(rp(2, 1)).add(rp(3, 0));
    // just a last minute typo check...
    eq(sub1.mul(sub2), large.unparse());
  }

  public void testDivComplexII2() {
    // x^8+2*x^6+8*x^5+2*x^4+17*x^3+11*x^2+8*x+3 =
    // (x^3+2*x+1) * (x^5+7*x^2+2*x+3)
    RatPoly large =
        rp(1, 8)
            .add(rp(2, 6))
            .add(rp(8, 5))
            .add(rp(2, 4))
            .add(rp(17, 3))
            .add(rp(11, 2))
            .add(rp(8, 1))
            .add(rp(3, 0));
    // x^3+2*x+1
    RatPoly sub1 = rp(1, 3).add(rp(2, 1)).add(rp(1, 0));
    // x^5+7*x^2+2*x+3
    RatPoly sub2 = rp(1, 5).add(rp(7, 2)).add(rp(2, 1)).add(rp(3, 0));
    // just a last minute typo check...
    eq(sub2.mul(sub1), large.unparse());
  }

  public void testDivComplexII3() {
    // x^8+2*x^6+8*x^5+2*x^4+17*x^3+11*x^2+8*x+3 =
    // (x^3+2*x+1) * (x^5+7*x^2+2*x+3)
    RatPoly large =
        rp(1, 8)
            .add(rp(2, 6))
            .add(rp(8, 5))
            .add(rp(2, 4))
            .add(rp(17, 3))
            .add(rp(11, 2))
            .add(rp(8, 1))
            .add(rp(3, 0));
    // x^3+2*x+1
    RatPoly sub1 = rp(1, 3).add(rp(2, 1)).add(rp(1, 0));
    // x^5+7*x^2+2*x+3
    RatPoly sub2 = rp(1, 5).add(rp(7, 2)).add(rp(2, 1)).add(rp(3, 0));
    // just a last minute typo check...
    eq(large.div(sub2), "x^3+2*x+1");
  }

  public void testDivComplexII4() {
    // x^8+2*x^6+8*x^5+2*x^4+17*x^3+11*x^2+8*x+3 =
    // (x^3+2*x+1) * (x^5+7*x^2+2*x+3)
    RatPoly large =
        rp(1, 8)
            .add(rp(2, 6))
            .add(rp(8, 5))
            .add(rp(2, 4))
            .add(rp(17, 3))
            .add(rp(11, 2))
            .add(rp(8, 1))
            .add(rp(3, 0));
    // x^3+2*x+1
    RatPoly sub1 = rp(1, 3).add(rp(2, 1)).add(rp(1, 0));
    // x^5+7*x^2+2*x+3
    RatPoly sub2 = rp(1, 5).add(rp(7, 2)).add(rp(2, 1)).add(rp(3, 0));
    // just a last minute typo check...
    eq(large.div(sub1), "x^5+7*x^2+2*x+3");
  }

  public void testDivExamplesFromSpec() {
    // seperated this testclasses case out because it has a dependency on
    // both "parse" and "div" functioning properly

    // example 1 from spec
    eq(parse("x^3-2*x+3").div(parse("3*x^2")), "1/3*x");
  }

  public void testDivExamplesFromSpec1() {
    // seperated this testclasses case out because it has a dependency on
    // both "parse" and "div" functioning properly

    // example 2 from spec
    eq(parse("x^2+2*x+15").div(parse("2*x^3")), "0");
  }

  public void testDivExampleFromPset() {
    eq(
        parse("x^8+x^6+10*x^4+10*x^3+8*x^2+2*x+8").div(parse("3*x^6+5*x^4+9*x^2+4*x+8")),
        "1/3*x^2-2/9");
  }

  private void assertIsNaNanswer(RatPoly nanAnswer) {
    eq(nanAnswer, "NaN");
  }

  public void testDivByZero() {
    checkInitRatPolyFields();

    RatPoly nanAnswer;
    nanAnswer = rp(1, 0).div(zero);
    assertIsNaNanswer(nanAnswer);
  }

  public void testDivByZero1() {
    checkInitRatPolyFields();

    RatPoly nanAnswer;
    nanAnswer = rp(1, 1).div(zero);
    assertIsNaNanswer(nanAnswer);
  }

  public void testDivByPolyWithNaN() {
    checkInitRatPolyFields();
    RatPoly nan_x2 = rp(1, 2).mul(rp(1, 1).div(zero));
    RatPoly one_x1 = new RatPoly(1, 1);

    assertIsNaNanswer(nan_x2.div(one_x1));
  }

  public void testDivByPolyWithNaN1() {
    checkInitRatPolyFields();
    RatPoly nan_x2 = rp(1, 2).mul(rp(1, 1).div(zero));
    RatPoly one_x1 = new RatPoly(1, 1);

    assertIsNaNanswer(one_x1.div(nan_x2));
  }

  public void testDivByPolyWithNaN2() {
    checkInitRatPolyFields();
    RatPoly nan_x2 = rp(1, 2).mul(rp(1, 1).div(zero));
    RatPoly one_x1 = new RatPoly(1, 1);

    assertIsNaNanswer(nan_x2.div(zero));
  }

  public void testDivByPolyWithNaN3() {
    checkInitRatPolyFields();
    RatPoly nan_x2 = rp(1, 2).mul(rp(1, 1).div(zero));
    RatPoly one_x1 = new RatPoly(1, 1);

    assertIsNaNanswer(zero.div(nan_x2));
  }

  public void testDivByPolyWithNaN4() {
    checkInitRatPolyFields();
    RatPoly nan_x2 = rp(1, 2).mul(rp(1, 1).div(zero));
    RatPoly one_x1 = new RatPoly(1, 1);

    assertIsNaNanswer(nan_x2.div(nan_x2));
  }

  public void testIsNaN0() {
    assertTrue(RatPoly.parse("NaN").isNaN());
  }

  public void testIsNaN1() {
    assertTrue(!RatPoly.parse("1").isNaN());
  }

  public void testIsNaN2() {
    assertTrue(!RatPoly.parse("1/2").isNaN());
  }

  public void testIsNaN3() {
    assertTrue(!RatPoly.parse("x+1").isNaN());
  }

  public void testIsNaN4() {
    assertTrue(!RatPoly.parse("x^2+x+1").isNaN());
  }

  // Tell JUnit what order to run the tests in
  //   public static Test suite()
  //   {
  //     TestSuite suite = new TestSuite();
  //     suite.addTest(new RatPolyTest("testNoArgCtor"));
  //     suite.addTest(new RatPolyTest("testTwoArgCtor"));
  //     suite.addTest(new RatPolyTest("testDegree"));
  //     suite.addTest(new RatPolyTest("testAdd"));
  //     suite.addTest(new RatPolyTest("testSub"));
  //     suite.addTest(new RatPolyTest("testMul"));
  //     suite.addTest(new RatPolyTest("testOpsWithNaN"));
  //     suite.addTest(new RatPolyTest("testParseSimple"));
  //     suite.addTest(new RatPolyTest("testParseMultTerms"));
  //     suite.addTest(new RatPolyTest("testParseLeadingNeg"));
  //     suite.addTest(new RatPolyTest("testParseLeadingConstants"));
  //     suite.addTest(new RatPolyTest("testParseRationals"));
  //     suite.addTest(new RatPolyTest("testParseNaN"));
  //     suite.addTest(new RatPolyTest("testCoeff"));
  //     suite.addTest(new RatPolyTest("testDiv"));
  //     suite.addTest(new RatPolyTest("testDivComplexI"));
  //     suite.addTest(new RatPolyTest("testDivComplexII"));
  //     suite.addTest(new RatPolyTest("testDivExamplesFromSpec"));
  //     suite.addTest(new RatPolyTest("testDivExampleFromPset"));
  //     suite.addTest(new RatPolyTest("testDivByZero"));
  //     suite.addTest(new RatPolyTest("testDivByPolyWithNaN"));
  //     suite.addTest(new RatPolyTest("testIsNaN"));
  //     suite.addTest(new RatPolyTest("testImmutabilityOfOperations"));
  //     suite.addTest(new RatPolyTest("testEval"));
  //     return suite;
  //   }

  // Tell JUnit what order to run the tests in
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new RatPolyTest("testNoArgCtor"));
    suite.addTest(new RatPolyTest("testTwoArgCtor1"));
    suite.addTest(new RatPolyTest("testTwoArgCtor2"));
    suite.addTest(new RatPolyTest("testTwoArgCtor3"));
    suite.addTest(new RatPolyTest("testTwoArgCtor4"));
    suite.addTest(new RatPolyTest("testTwoArgCtor5"));
    suite.addTest(new RatPolyTest("testTwoArgCtor6"));
    suite.addTest(new RatPolyTest("testTwoArgCtor7"));
    suite.addTest(new RatPolyTest("testTwoArgCtor8"));
    suite.addTest(new RatPolyTest("testTwoArgCtor9"));
    suite.addTest(new RatPolyTest("testTwoArgCtor10"));
    suite.addTest(new RatPolyTest("testTwoArgCtor11"));
    suite.addTest(new RatPolyTest("testTwoArgCtor12"));
    suite.addTest(new RatPolyTest("testDegree0"));
    suite.addTest(new RatPolyTest("testDegree1"));
    suite.addTest(new RatPolyTest("testDegree2"));
    suite.addTest(new RatPolyTest("testDegree3"));
    suite.addTest(new RatPolyTest("testDegree4"));
    suite.addTest(new RatPolyTest("testAdd0"));
    suite.addTest(new RatPolyTest("testAdd1"));
    suite.addTest(new RatPolyTest("testAdd2"));
    suite.addTest(new RatPolyTest("testAdd3"));
    suite.addTest(new RatPolyTest("testAdd4"));
    suite.addTest(new RatPolyTest("testAdd5"));
    suite.addTest(new RatPolyTest("testAdd6"));
    suite.addTest(new RatPolyTest("testAdd7"));
    suite.addTest(new RatPolyTest("testSub8"));
    suite.addTest(new RatPolyTest("testSub9"));
    suite.addTest(new RatPolyTest("testMul0"));
    suite.addTest(new RatPolyTest("testMul1"));
    suite.addTest(new RatPolyTest("testMul2"));
    suite.addTest(new RatPolyTest("testMul3"));
    suite.addTest(new RatPolyTest("testMul4"));
    suite.addTest(new RatPolyTest("testMul5"));
    suite.addTest(new RatPolyTest("testMul6"));
    suite.addTest(new RatPolyTest("testOpsWithNaN001"));
    suite.addTest(new RatPolyTest("testOpsWithNaN101"));
    suite.addTest(new RatPolyTest("testOpsWithNaN201"));
    suite.addTest(new RatPolyTest("testOpsWithNaN301"));
    suite.addTest(new RatPolyTest("testOpsWithNaN401"));
    suite.addTest(new RatPolyTest("testOpsWithNaN501"));
    suite.addTest(new RatPolyTest("testOpsWithNaN601"));
    suite.addTest(new RatPolyTest("testOpsWithNaN701"));
    suite.addTest(new RatPolyTest("testOpsWithNaN002"));
    suite.addTest(new RatPolyTest("testOpsWithNaN102"));
    suite.addTest(new RatPolyTest("testOpsWithNaN202"));
    suite.addTest(new RatPolyTest("testOpsWithNaN302"));
    suite.addTest(new RatPolyTest("testOpsWithNaN402"));
    suite.addTest(new RatPolyTest("testOpsWithNaN502"));
    suite.addTest(new RatPolyTest("testOpsWithNaN602"));
    suite.addTest(new RatPolyTest("testOpsWithNaN702"));
    suite.addTest(new RatPolyTest("testOpsWithNaN003"));
    suite.addTest(new RatPolyTest("testOpsWithNaN103"));
    suite.addTest(new RatPolyTest("testOpsWithNaN203"));
    suite.addTest(new RatPolyTest("testOpsWithNaN303"));
    suite.addTest(new RatPolyTest("testOpsWithNaN403"));
    suite.addTest(new RatPolyTest("testOpsWithNaN503"));
    suite.addTest(new RatPolyTest("testOpsWithNaN603"));
    suite.addTest(new RatPolyTest("testOpsWithNaN703"));
    suite.addTest(new RatPolyTest("testOpsWithNaN004"));
    suite.addTest(new RatPolyTest("testOpsWithNaN104"));
    suite.addTest(new RatPolyTest("testOpsWithNaN204"));
    suite.addTest(new RatPolyTest("testOpsWithNaN304"));
    suite.addTest(new RatPolyTest("testOpsWithNaN404"));
    suite.addTest(new RatPolyTest("testOpsWithNaN504"));
    suite.addTest(new RatPolyTest("testOpsWithNaN604"));
    suite.addTest(new RatPolyTest("testOpsWithNaN704"));
    suite.addTest(new RatPolyTest("testOpsWithNaN005"));
    suite.addTest(new RatPolyTest("testOpsWithNaN105"));
    suite.addTest(new RatPolyTest("testOpsWithNaN205"));
    suite.addTest(new RatPolyTest("testOpsWithNaN305"));
    suite.addTest(new RatPolyTest("testOpsWithNaN405"));
    suite.addTest(new RatPolyTest("testOpsWithNaN505"));
    suite.addTest(new RatPolyTest("testOpsWithNaN605"));
    suite.addTest(new RatPolyTest("testOpsWithNaN705"));
    suite.addTest(new RatPolyTest("testOpsWithNaN006"));
    suite.addTest(new RatPolyTest("testOpsWithNaN106"));
    suite.addTest(new RatPolyTest("testOpsWithNaN206"));
    suite.addTest(new RatPolyTest("testOpsWithNaN306"));
    suite.addTest(new RatPolyTest("testOpsWithNaN406"));
    suite.addTest(new RatPolyTest("testOpsWithNaN506"));
    suite.addTest(new RatPolyTest("testOpsWithNaN606"));
    suite.addTest(new RatPolyTest("testOpsWithNaN706"));
    suite.addTest(new RatPolyTest("testOpsWithNaN007"));
    suite.addTest(new RatPolyTest("testOpsWithNaN107"));
    suite.addTest(new RatPolyTest("testOpsWithNaN207"));
    suite.addTest(new RatPolyTest("testOpsWithNaN307"));
    suite.addTest(new RatPolyTest("testOpsWithNaN407"));
    suite.addTest(new RatPolyTest("testOpsWithNaN507"));
    suite.addTest(new RatPolyTest("testOpsWithNaN607"));
    suite.addTest(new RatPolyTest("testOpsWithNaN707"));
    suite.addTest(new RatPolyTest("testOpsWithNaN008"));
    suite.addTest(new RatPolyTest("testOpsWithNaN108"));
    suite.addTest(new RatPolyTest("testOpsWithNaN208"));
    suite.addTest(new RatPolyTest("testOpsWithNaN308"));
    suite.addTest(new RatPolyTest("testOpsWithNaN408"));
    suite.addTest(new RatPolyTest("testOpsWithNaN508"));
    suite.addTest(new RatPolyTest("testOpsWithNaN608"));
    suite.addTest(new RatPolyTest("testOpsWithNaN708"));
    suite.addTest(new RatPolyTest("testImmutabilityOfOperations0"));
    suite.addTest(new RatPolyTest("testImmutabilityOfOperations1"));
    suite.addTest(new RatPolyTest("testImmutabilityOfOperations2"));
    suite.addTest(new RatPolyTest("testImmutabilityOfOperations3"));
    suite.addTest(new RatPolyTest("testImmutabilityOfOperations4"));
    suite.addTest(new RatPolyTest("testImmutabilityOfOperations5"));
    suite.addTest(new RatPolyTest("testImmutabilityOfOperations6"));
    suite.addTest(new RatPolyTest("testImmutabilityOfOperations7"));
    suite.addTest(new RatPolyTest("testImmutabilityOfOperations8"));
    suite.addTest(new RatPolyTest("testEval0"));
    suite.addTest(new RatPolyTest("testEval1"));
    suite.addTest(new RatPolyTest("testEval2"));
    suite.addTest(new RatPolyTest("testEval3"));
    suite.addTest(new RatPolyTest("testEval4"));
    suite.addTest(new RatPolyTest("testEval5"));
    suite.addTest(new RatPolyTest("testEval6"));
    suite.addTest(new RatPolyTest("testEval7"));
    suite.addTest(new RatPolyTest("testEval8"));
    suite.addTest(new RatPolyTest("testEval9"));
    suite.addTest(new RatPolyTest("testEval95"));
    suite.addTest(new RatPolyTest("testEval10"));
    suite.addTest(new RatPolyTest("testEval11"));
    suite.addTest(new RatPolyTest("testEval12"));
    suite.addTest(new RatPolyTest("testEval13"));
    suite.addTest(new RatPolyTest("testEval14"));
    suite.addTest(new RatPolyTest("testEval15"));
    suite.addTest(new RatPolyTest("testEval16"));
    suite.addTest(new RatPolyTest("testEval17"));
    suite.addTest(new RatPolyTest("testParseSimple0"));
    suite.addTest(new RatPolyTest("testParseSimple1"));
    suite.addTest(new RatPolyTest("testParseSimple2"));
    suite.addTest(new RatPolyTest("testParseMultTerms0"));
    suite.addTest(new RatPolyTest("testParseMultTerms1"));
    suite.addTest(new RatPolyTest("testParseMultTerms2"));
    suite.addTest(new RatPolyTest("testParseLeadingNeg0"));
    suite.addTest(new RatPolyTest("testParseLeadingNeg1"));
    suite.addTest(new RatPolyTest("testParseLeadingNeg2"));
    suite.addTest(new RatPolyTest("testParseLeadingConstants0"));
    suite.addTest(new RatPolyTest("testParseLeadingConstants1"));
    suite.addTest(new RatPolyTest("testParseLeadingConstants2"));
    suite.addTest(new RatPolyTest("testParseLeadingConstants3"));
    suite.addTest(new RatPolyTest("testParseRationals0"));
    suite.addTest(new RatPolyTest("testParseRationals1"));
    suite.addTest(new RatPolyTest("testParseRationals2"));
    suite.addTest(new RatPolyTest("testParseRationals3"));
    suite.addTest(new RatPolyTest("testParseRationals4"));
    suite.addTest(new RatPolyTest("testParseRationals5"));
    suite.addTest(new RatPolyTest("testParseRationals6"));
    suite.addTest(new RatPolyTest("testParseNaN"));
    suite.addTest(new RatPolyTest("testCoeff0"));
    suite.addTest(new RatPolyTest("testCoeff1"));
    suite.addTest(new RatPolyTest("testCoeff2"));
    suite.addTest(new RatPolyTest("testCoeff3"));
    suite.addTest(new RatPolyTest("testCoeff4"));
    suite.addTest(new RatPolyTest("testCoeff5"));
    suite.addTest(new RatPolyTest("testDiv0"));
    suite.addTest(new RatPolyTest("testDiv1"));
    suite.addTest(new RatPolyTest("testDiv2"));
    suite.addTest(new RatPolyTest("testDiv3"));
    suite.addTest(new RatPolyTest("testDiv4"));
    suite.addTest(new RatPolyTest("testDiv5"));
    suite.addTest(new RatPolyTest("testDiv55"));
    suite.addTest(new RatPolyTest("testDiv6"));
    suite.addTest(new RatPolyTest("testDiv7"));
    suite.addTest(new RatPolyTest("testDiv8"));
    suite.addTest(new RatPolyTest("testDiv9"));
    suite.addTest(new RatPolyTest("testDiv10"));
    suite.addTest(new RatPolyTest("testDiv11"));
    suite.addTest(new RatPolyTest("testDiv13"));
    suite.addTest(new RatPolyTest("testDiv12"));
    suite.addTest(new RatPolyTest("testDivComplexI0"));
    suite.addTest(new RatPolyTest("testDivComplexI1"));
    suite.addTest(new RatPolyTest("testDivComplexII1"));
    suite.addTest(new RatPolyTest("testDivComplexII2"));
    suite.addTest(new RatPolyTest("testDivComplexII3"));
    suite.addTest(new RatPolyTest("testDivComplexII4"));
    suite.addTest(new RatPolyTest("testDivExamplesFromSpec"));
    suite.addTest(new RatPolyTest("testDivExamplesFromSpec1"));
    suite.addTest(new RatPolyTest("testDivExampleFromPset"));
    suite.addTest(new RatPolyTest("testDivByZero"));
    suite.addTest(new RatPolyTest("testDivByZero1"));
    suite.addTest(new RatPolyTest("testDivByPolyWithNaN"));
    suite.addTest(new RatPolyTest("testDivByPolyWithNaN1"));
    suite.addTest(new RatPolyTest("testDivByPolyWithNaN2"));
    suite.addTest(new RatPolyTest("testDivByPolyWithNaN3"));
    suite.addTest(new RatPolyTest("testDivByPolyWithNaN4"));
    suite.addTest(new RatPolyTest("testIsNaN0"));
    suite.addTest(new RatPolyTest("testIsNaN1"));
    suite.addTest(new RatPolyTest("testIsNaN2"));
    suite.addTest(new RatPolyTest("testIsNaN3"));
    suite.addTest(new RatPolyTest("testIsNaN4"));
    return suite;
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
}
