package randoop.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class ObservationVectorTests extends TestCase{
  private Map<String, Object> varMap;
  @Override
  public void setUp() {
    varMap = new HashMap<String, Object>();
  }
  public void testDummy(){}
//public void test1() throws Exception {
//PurityInfo purityInfo =  new PurityInfo(PurityFileReader.createFromLines(""));

//List<Class<?>> clzs = new ArrayList<Class<?>>();
//clzs.add(String.class);

//ObservationVector vect= new ObservationVector(Arrays.asList((Object)"string"),
//Arrays.asList("var1"),
//clzs,
//purityInfo);

//varMap.put("var1", 3);
//MatchResult result = vect.matches(varMap);
//assertTrue(! result.isSuccessful());
//}

//public void test2() throws Exception {
//PurityInfo purityInfo=  new PurityInfo(PurityFileReader.createFromLines(
//"java.util.Vector.capacity() RC" + Globals.lineSep +
//"java.lang.Object.toString() RC" + Globals.lineSep
//));        

//List<Class<?>> clzs = new ArrayList<Class<?>>();
//clzs.add(Vector.class);

//ObservationVector vect= new ObservationVector(Arrays.asList((Object) new Vector<String>()),
//Arrays.asList("var3"),
//clzs,
//purityInfo);
//varMap.put("var3", new Vector<String>());
//MatchResult result = vect.matches(varMap);
//assertTrue(result.getErrorMessage(), result.isSuccessful());        
//}

//public void test3() throws Exception {
//PurityInfo purityInfo=  new PurityInfo(PurityFileReader.createFromLines(
//"java.util.Vector.capacity() RC" + Globals.lineSep +
//"java.lang.Object.toString() RC" + Globals.lineSep
//));        

//List<Class<?>> clzs = new ArrayList<Class<?>>();
//clzs.add(Vector.class);

//ObservationVector vect= new ObservationVector(Arrays.asList((Object) new Vector<String>(1)),
//Arrays.asList("var1"), clzs, purityInfo);

//varMap.put("var1", new Vector<String>(2));
//MatchResult result = vect.matches(varMap);

//assertTrue(result.getErrorMessage(), !result.isSuccessful());
//assertEquals("On object 'var1' match failure: capacity() OLD VALUE WAS 1 BUT NEW VALUE IS 2", result.getErrorMessage());
//}    

//public void test4() throws Exception {
//PurityInfo purityInfo=  new PurityInfo(PurityFileReader.createFromLines(
//"java.lang.Object.toString() RC" + Globals.lineSep
//));        

//List<Class<?>> clzs = new ArrayList<Class<?>>();
//clzs.add(ArrayList.class);

//ObservationVector vect= new ObservationVector(Arrays.asList((Object) new ArrayList<String>()),
//Arrays.asList("var1"), clzs, purityInfo);

//varMap.put("var1", new LinkedList<String>());
//MatchResult result = vect.matches(varMap);
////this must fail because the classes of the objects are different
//assertTrue(result.getErrorMessage(), ! result.isSuccessful());        
//}

//public void test5() throws Exception {
//PurityInfo purityInfo= PurityInfo.createFromLines(
//"java.util.Collection.size() receiver" + Globals.lineSep +
//"java.util.Vector.capacity() receiver" + Globals.lineSep +
//"java.lang.Object.toString() receiver" + Globals.lineSep
//);        

//Object[] executionObjects= {new Vector<String>()};

//Invocation
//ObservationVector vect= new ObservationVector(executionObjects, purityInfo);
//Object[] obs= {new Vector<String>()};
//MatchResult result = vect.matches(obs);
//assertTrue(result.getErrorMessage(), result.isSuccessful());        
//}

}
