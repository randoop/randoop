package randoop.test;

import junit.framework.TestCase;

public class ObservationTests extends TestCase {

  public void testDummy(){}
//public void testNullObservationOK() throws Exception {
//Observation obs = new NullObservation("foo");
//MatchResult result = obs.matches(null);
//assertTrue(result.isSuccessful());
//}

//public void testNullObservationFail() throws Exception {
//Observation obs = new NullObservation("bar");
//MatchResult result = obs.matches(this);
//assertTrue(!result.isSuccessful());
//}

//public void testPrimitive1() throws Exception {
//try {
//Object object = new Object();
//new PrimitiveOrStringObservation(object, "var1");
//fail("not a primitive passed: " + object);
//} catch (IllegalArgumentException e) {
//assertTrue(true);
//}
//}

//public void testPrimitive2() throws Exception {
//try {
//Observation obs = null;
//try {
//Object object = "real string";
//obs = new PrimitiveOrStringObservation(object, "var1");
//} catch (IllegalArgumentException e) {
//assertTrue(e.getMessage(), false);
//}
//Object newobj = new Object();
//obs.matches(newobj);
//fail("not a primitive passed: " + newobj);
//} catch (IllegalArgumentException e) {
//assertTrue(true);
//}
//}

//public void testPrimitive3() throws Exception {
//try {
//Observation obs = null;
//try {
//Object object = "real string";
//obs = new PrimitiveOrStringObservation(object, "var1");
//} catch (IllegalArgumentException e) {
//assertTrue(e.getMessage(), false);
//}
//Object newobj = 7;
//assertTrue(!obs.matches(newobj).isSuccessful());
//} catch (IllegalArgumentException e) {
//assertTrue(e.getMessage(), false);
//}
//}

//public void testPrimitive4() throws Exception {
//try {
//Observation obs = null;
//try {
//Object object = "real string";
//obs = new PrimitiveOrStringObservation(object, "var1");
//} catch (IllegalArgumentException e) {
//assertTrue(e.getMessage(), false);
//}
//Object newobj = "real string";
//MatchResult matches = obs.matches(newobj);
//assertTrue(matches.getErrorMessage(), matches.isSuccessful());
//} catch (IllegalArgumentException e) {
//assertTrue(e.getMessage(), false);
//}
//}

//public void testCodeGen1() throws Exception {
//Object object = "foo";
//PrimitiveOrStringObservation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals(\"foo\", (java.lang.String)var1);", string);
//}

//public void testCodeGen2() throws Exception {
//Object object = 'c';
//PrimitiveOrStringObservation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals('c', (char)(java.lang.Character)var1);", string);
//}

//public void testCodeGen3() throws Exception {
//Object object = 4;
//PrimitiveOrStringObservation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals((int)4, (int)(java.lang.Integer)var1);", string);
//}

//public void testCodeGen6() throws Exception {
//try {
//Object object = null;
//PrimitiveOrStringObservation obs = new PrimitiveOrStringObservation(object, "var1");
//obs.toAssertionString();
//fail("cannot allow null");
//} catch (IllegalArgumentException e) {
//assertTrue(true);
//}
//}

//public void testCodeGen7() throws Exception {
//Object object = Character.valueOf('c');
//PrimitiveOrStringObservation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals('c', (char)(java.lang.Character)var1);", string);
//}

//public void testCodeGen8() throws Exception {
//Observation obs = new NullObservation("baz");
//String string = obs.toAssertionString();
//assertEquals("", string);// TODO some of those assertions refer to
//// things that were not declared - disable
//// all for now
//}

//public void testCodeGen9() throws Exception {
//Object obj = Collections.emptyList();
//Observation obs = new ObserverMethodObservation(Object.class.getDeclaredMethod("toString", new Class[0]), obj, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals(\"[]\", var1.toString());", string);
//}

//public void testCodeGen10() throws Exception {
//Object obj = Collections.emptyList();
//Observation obs = new ObserverMethodObservation(List.class.getMethod("size", new Class[0]), obj, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals((int)0, var1.size());", string);
//}

//public void testCodeGen11() throws Exception {
//Object object = Double.NaN;
//Observation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals(Double.NaN, (double)(java.lang.Double)var1);", string);
//}

//public void testCodeGen12() throws Exception {
//Object object = Float.NaN;
//Observation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals(Float.NaN, (float)(java.lang.Float)var1);", string);
//}

//public void testCodeGen13() throws Exception {
//Object receiver = Collections.emptyList();
//Observation obs = new ObserverMethodObservationSet(Collections.<Method> emptySet(), receiver, "var1");
//String string = obs.toAssertionString();
//assertEquals("", string);
//}

//public void testCodeGen14() throws Exception {
//Object receiver = new ArrayList<String>(Arrays.asList("foo", "bar"));
//Method size = ArrayList.class.getMethod("size", new Class[0]);
//Method toString = ArrayList.class.getMethod("isEmpty", new Class[0]);
//LinkedHashSet<Method> observers = new LinkedHashSet<Method>(Arrays.asList(size, toString));
//Observation obs = new ObserverMethodObservationSet(observers, receiver, "var1");
//String assertioString = obs.toAssertionString();
//String expected = "assertEquals((int)2, var1.size());" + Util.newLine + "assertEquals(false, var1.isEmpty());";
//assertEquals(expected, assertioString);
//}

//public void testCodeGen17() throws Exception {
//Object object = Float.NaN;
//PrimitiveOrStringObservation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals(Float.NaN, (float)(java.lang.Float)var1);", string);
//}

//public void testNonPublicMethodObservation() throws Exception {
//try {
//Object receiver = new ArrayList<String>(Arrays.asList("foo", "bar"));
//Method privateMethod = ArrayList.class.getDeclaredMethod("fastRemove", new Class<?>[] { int.class });
//LinkedHashSet<Method> observers = new LinkedHashSet<Method>(Arrays.asList(privateMethod));
//new ObserverMethodObservationSet(observers, receiver, "var1");// performs
//// observations
//fail("expected exception");

//} catch (IllegalArgumentException e) {
//assertTrue(true);
//}
//}

//// from ObservationTests
//public void testCodeGenUnicode() throws Exception {
//String object = new DecimalFormatSymbols().getInfinity();
//Observation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals(\"\\u221E\", (java.lang.String)var1);", string);
//}

//// from ObservationTests
//public void testCodeGen15() throws Exception {
//Object object = Float.POSITIVE_INFINITY;
//PrimitiveOrStringObservation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals(Float.POSITIVE_INFINITY, (float)(java.lang.Float)var1);", string);
//}

//// from ObservationTests
//public void testCodeGen16() throws Exception {
//Object object = Float.NEGATIVE_INFINITY;
//PrimitiveOrStringObservation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals(Float.NEGATIVE_INFINITY, (float)(java.lang.Float)var1);", string);
//}

//public void testCodeGen21() throws Exception {
//Object object = Double.NaN;
//PrimitiveOrStringObservation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals(Double.NaN, (double)(java.lang.Double)var1);", string);
//}

//public void testCodeGen20() throws Exception {
//Object object = 10000000000L;
//PrimitiveOrStringObservation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals((long)10000000000L, (long)(java.lang.Long)var1);", string);
//}

//// from ObservationTests
//public void testCodeGen18() throws Exception {
//Object object = Double.POSITIVE_INFINITY;
//PrimitiveOrStringObservation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals(Double.POSITIVE_INFINITY, (double)(java.lang.Double)var1);", string);
//}

//// from ObservationTests
//public void testCodeGen19() throws Exception {
//Object object = Double.NEGATIVE_INFINITY;
//PrimitiveOrStringObservation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals(Double.NEGATIVE_INFINITY, (double)(java.lang.Double)var1);", string);
//}

//public void testCodeGen22() throws Exception {
//Object object = Long.valueOf(10L);
//Observation obs = new PrimitiveOrStringObservation(object, "var1");
//String string = obs.toAssertionString();
//assertEquals("assertEquals((long)10L, (long)(java.lang.Long)var1);", string);
//}
}
