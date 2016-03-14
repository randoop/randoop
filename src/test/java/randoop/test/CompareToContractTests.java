package randoop.test;

import junit.framework.TestCase;

public class CompareToContractTests extends TestCase {

  public void test() {
    //throw new RuntimeException();
  }

  //public void testReverseToString() throws Exception {
  //CompareToNotReverseSign contract= new CompareToNotReverseSign();
  //contract.toJunitCode("var1", "var2"); //check that no exception
  //}

  //public void testReverse1() throws Exception {
  //CompareToNotReverseSign contract= new CompareToNotReverseSign();
  //Decoration result = contract.evaluate("foo", "bar");
  //assertNull(result);
  //}

  //public void testReverse2() throws Exception {
  //@SuppressWarnings("unchecked")
  //class BrokenCompareto implements Comparable{
  //public int compareTo(Object o) {
  //return -2;
  //}
  //}
  //CompareToNotReverseSign contract= new CompareToNotReverseSign();
  //Decoration result = contract.evaluate(new BrokenCompareto(), new BrokenCompareto());
  //assertNotNull(result);
  //}

  //public void testReverse3() throws Exception {
  //@SuppressWarnings("unchecked")
  //class ComparetoException implements Comparable{
  //public int compareTo(Object o) {
  //throw new IllegalStateException();
  //}
  //}

  //@SuppressWarnings("unchecked")
  //class ComparetoNoException implements Comparable{
  //public int compareTo(Object o) {
  //return 0;
  //}
  //}

  //CompareToNotReverseSign contract= new CompareToNotReverseSign();
  //Decoration result = contract.evaluate(new ComparetoException(), new ComparetoNoException());
  //assertNotNull(result);
  //}

  //public void testTransitiveToString() throws Exception {
  //CompareToNotTransitive contract= new CompareToNotTransitive();
  //contract.toJunitCode("v1", "v2", "v3");
  //}

  //public void testTransitive1() throws Exception {
  //CompareToNotTransitive contract= new CompareToNotTransitive();
  //Decoration result = contract.evaluate("foo", "bar", "baz");
  //assertNull(result);
  //}

  //@SuppressWarnings("unchecked")
  //public void testTransitive2() throws Exception {
  //class LargerThanEveryone implements Comparable{
  //public int compareTo(Object o) {
  //return 1;
  //}
  //}

  //class LargerThanAnyNonString implements Comparable{
  //public int compareTo(Object o) {
  //if (! (o instanceof String))
  //return 1;
  //return -1;
  //}
  //}

  //CompareToNotTransitive contract= new CompareToNotTransitive();
  //Decoration result = contract.evaluate(new LargerThanAnyNonString(), new LargerThanEveryone(), "y");
  //assertNotNull(result);
  //}

  //public void testTernaryToString() throws Exception {
  //CompareToTernaryContractViolated contract= new CompareToTernaryContractViolated();
  //contract.toJunitCode("v1", "v2", "v3");
  //}

  //public void testTernary1() throws Exception {
  //CompareToTernaryContractViolated contract= new CompareToTernaryContractViolated();
  //Decoration result = contract.evaluate("foo", "bar", "baz");
  //assertNull(result);
  //}

  //public void testTernary2() throws Exception {
  //CompareToTernaryContractViolated contract= new CompareToTernaryContractViolated();
  //Decoration result = contract.evaluate("foo", "foo", "baz");
  //assertNull(result);
  //}

  //@SuppressWarnings("unchecked")
  //public void testTernary3() throws Exception {
  //class EqualToEveryone implements Comparable{
  //public int compareTo(Object o) {
  //return 0;
  //}
  //}

  //class SmallerThanAnyone implements Comparable{
  //public int compareTo(Object o) {
  //return -1;
  //}
  //}

  //Decoration contract= new CompareToTernaryContractViolated();
  //Decoration result = contract.checkFaultyBehavior(new EqualToEveryone(), new SmallerThanAnyone(), "baz");
  //assertNotNull(result);
  //}

}
