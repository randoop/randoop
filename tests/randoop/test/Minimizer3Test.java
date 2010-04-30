package randoop.test;

import junit.framework.TestCase;

public class Minimizer3Test extends TestCase {

//private static final int TIME_LIMIT_SECS = 1;

//public Minimizer3Test(String name) {
//super(name);
//}

//public static void test1() {

////----------------generate explorer------------------------------------

//InputStream classStream =
//ForwardExplorerPerformanceTest.class.getResourceAsStream("resources/java.util.classlist.txt");

//StatementInfos model = StatementInfos.publicMembersDefaultModel(Reflection.loadClassesFromStream(classStream), "");

//UniformRandomSelector<Sequence> selector = new UniformRandomSelector<Sequence>();
//ContractCheckingVisitor faultManager = new ContractCheckingVisitor(ContractCheckingVisitor.defaultCheckers());
//ForwardExplorer.dontexecute = true; // FIXME make this an instance field?        

//ContractCheckingExplorer<? extends Sequence> explorer = null;
//explorer = new ContractCheckingExplorer<Sequence>(new SlowSequenceFactory(),
//selector, selector, model, faultManager, TIME_LIMIT_SECS*1000, Integer.MAX_VALUE);
//explorer.explore();
//ForwardExplorer.dontexecute = false; 

//assertTrue(! explorer.allSequences().isEmpty());

////----------------generate test case -----------------------------------
//TestCase errorSeq = null;
//try {
////0: LinkedList l1 = new LinkedList();
//Class<?> linkedListClass<?> = Class.forName("java.util.LinkedList");
//SlowStatement constrLinkedList = callEmptyConstructor(linkedListClass);

////1: int zero = 0;
//SlowStatement integerZero = new SlowStatement(new PrimitiveOrStringOrNullDeclInfo(int.class, 0), new ArrayList<Variable> ());

//List <Variable> inputVars = new ArrayList<Variable> ();

////2: HashMap h1 = new HashMap(0);
//Class<?> hashMapClass<?> = Class.forName("java.util.HashMap");
//Constructor<?> hashMapConstructor = hashMapClass.getConstructor(int.class);
//StatementInfo hashMapTransformer = ConstructorCallInfo.getDefaultStatementInfo(hashMapConstructor);
//inputVars.add(integerZero.result);
//SlowStatement constrHashMap = new SlowStatement(hashMapTransformer, inputVars);

////3: Collection c1 = h1.values();
//inputVars = new ArrayList<Variable> ();
//inputVars.add(constrHashMap.result);
//SlowStatement callHashMapVariables = callMethod(hashMapClass, "values", new Class[0], inputVars);

////4: Object[] a1 = c1.toArray();
//inputVars = new ArrayList<Variable> ();
//inputVars.add(callHashMapVariables.result);
//Class<?> collectionClass<?> = Class.forName("java.util.Collection");
//SlowStatement callToArray = callMethod(collectionClass, "toArray", new Class[0], inputVars);

////5: l1.addFirst(a1);
//inputVars = new ArrayList<Variable> ();
//inputVars.add(constrLinkedList.result);
//inputVars.add(callToArray.result);
//Class<?>[] AddFirstInputArgs = { Object.class };
//SlowStatement callAddFirst = callMethod(linkedListClass, "addFirst", AddFirstInputArgs , inputVars);

////6: TreeSet t1 = new TreeSet(l1);
//Class<?> treeSetClass<?> = Class.forName("java.util.TreeSet");
//Constructor<?> treeSetConstructor = treeSetClass.getConstructor(Collection.class);
//StatementInfo treeSetTransformer = ConstructorCallInfo.getDefaultStatementInfo(treeSetConstructor);
//inputVars = new ArrayList<Variable> ();
//inputVars.add(constrLinkedList.result);
//SlowStatement constrTreeSet = new SlowStatement(treeSetTransformer, inputVars);

////7: Collections object (to get around calling a static method)
//SlowStatement collectionsObject = new SlowStatement(new PrimitiveOrStringOrNullDeclInfo(Collections.class, null), new ArrayList<Variable> ());

////8: Set s1 = Collections.unmodifiableSet(t1);
//Class<?> [] unmodSetInputArgs = { Set.class };
//inputVars = new ArrayList<Variable> ();
//inputVars.add(collectionsObject.result);
//inputVars.add(constrTreeSet.result);            
//Class<?> collectionsClass<?> = Class.forName("java.util.Collections");
//SlowStatement callUnmodSet = callMethod(collectionsClass, "unmodifiableSet", unmodSetInputArgs , inputVars);

//List <SlowStatement> statements = new ArrayList<SlowStatement> ();
//statements.add(constrLinkedList);
//statements.add(integerZero);
//statements.add(constrHashMap);
//statements.add(callHashMapVariables);
//statements.add(callToArray);
//statements.add(callAddFirst);
//statements.add(constrTreeSet);
//statements.add(collectionsObject);
//statements.add(callUnmodSet);

//SlowSequence seq = new SlowSequence(statements);

//List<Variable> errorRevealingVariables = new ArrayList<Variable> ();
//errorRevealingVariables.add(callUnmodSet.result);
//Decoration badBehavior = new EqualsOnSameObjectReturnsFalse();
//errorSeq = new TestCaseThatCreatesBadObjects(seq, errorRevealingVariables, badBehavior);
//System.out.println();
//System.out.println("BEFORE MINIMIZATION");
//System.out.println(errorSeq.toCodeString());

//} catch (Exception e) {
//System.out.println(e.getLocalizedMessage());
//e.printStackTrace(System.out);
//}

////----------------minimize sequence-------------------------------

////ErrorRevealingSequence minSeq = Minimizer2.minimize(errorSeq);
//TestCase minSeq = Minimizer3.minimize(errorSeq, explorer);

//System.out.println();
//System.out.println("AFTER MINIMIZATION");
//System.out.println(minSeq.toSequence().toCodeString());

//}

///*
//* Creates statement that calls empty constructors
//*/
//private static SlowStatement callEmptyConstructor(Class<?> c) {
//SlowStatement s = null;
//try {
//Constructor<?> defaultConstructor = c.getConstructor(new Class[0]);
//StatementInfo cTransformer = ConstructorCallInfo
//.getDefaultStatementInfo(defaultConstructor);
//s = new SlowStatement(cTransformer, new ArrayList<Variable> ());
//} catch (NoSuchMethodException e) {
//// to catch exceptions from Class.getConstructor("..")
//}
//return s;
//}


///*
//* Creates method call with specified input parameters
//*/
//private static SlowStatement callMethod(Class<?> c, String name, Class<?>[] args, List<Variable> inputs) {
//SlowStatement s = null;
//try {
//StatementInfo mTransformer = MethodCallInfo.getDefaultStatementInfo(c
//.getMethod(name, args));
//s = new SlowStatement(mTransformer, inputs);
//} catch (NoSuchMethodException e) {
//throw new RuntimeException(e);
//// to catch exceptions from Class.getConstructor("..")
//}
//return s;
//}


//public static Test suite() { 
//TestSuite suite = new TestSuite("Minimizer Tests");
//suite.addTest(new Minimizer3Test("test1"));
//return suite; 
//}

}
