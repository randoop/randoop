package randoop.test;

import junit.framework.TestCase;

public class Minimizer2Test extends TestCase {

//private Class<?> ubStackClass;

//public Minimizer2Test(String name) {
//super(name);
//}

//@Override
//protected void setUp() throws Exception {        
//try {
//super.setUp();
//ubStackClass<?> = Class.forName("randoop.test.UBStack");
//} catch (ClassNotFoundException e) {
//// exceptions while creating classes
//throw e;
//}
//}
///*
//public void testReachability() {
////*******************************
////NOTE: Change type of performReachabilityTest(StatementSequence) to 
////public static before running this testclasses stub
////*******************************


////Sequence #1: Remove stmt 5 => stmt 3 unreachable
////1. Stack s = new Stack();
////- create primitive value: int 1
////2. s.push(1);
////3. int i = s.top();
////4. Stack t = new Stack();
////5. t.push(i);

//Statement constrStackS = callEmptyConstructor(ubStackClass);

//Statement integerOne = new Statement(new PrimitiveOrStringOrNullDeclInfo(int.class, 1), new ArrayList<Variable> ());

//Class<?>[] pushArgs = { int.class };
//List<Variable> inputVars = new ArrayList<Variable> ();
//inputVars.add(constrStackS.result); //receiver
//inputVars.add(integerOne.result);   //parameter
//Statement pushS = callMethod(ubStackClass, "push", pushArgs, inputVars);

//Class<?>[] emptyArgs = { };
//inputVars = new ArrayList<Variable> ();
//inputVars.add(constrStackS.result);
//Statement topS = callMethod(ubStackClass, "top", emptyArgs, inputVars);

//Statement constrStackT = callEmptyConstructor(ubStackClass);

//inputVars = new ArrayList<Variable> ();
//inputVars.add(constrStackT.result); //receiver
//inputVars.add(topS.result);         //parameter
//Statement pushT = callMethod(ubStackClass, "push", pushArgs, inputVars);

//List <Statement> statements = new ArrayList<Statement> ();
//statements.add(constrStackS);
//statements.add(integerOne);
//statements.add(pushS);
//statements.add(topS);
//statements.add(constrStackT);
//statements.add(pushT);

//StatementSequence seq = new StatementSequence(statements);
//System.out.println("ORIGINAL STATEMENT SEQUENCE:");
//System.out.println(seq.toCodeString());
////--------------- SEQUENCE CREATED ----------------------

////take out last statement
//statements.remove(statements.size()-1); 
//StatementSequence newSeq = new StatementSequence(statements);
//System.out.println("REMOVE LAST STATEMENT FROM SEQUENCE:");       
//System.out.println(newSeq.toCodeString());


//newSeq = Minimizer2.performReachabilityTest(newSeq);
//newSeq = Minimizer2.removeUnreachableStatements(newSeq);
//System.out.println("REMOVE UNREACHABLE STATEMENTS:");       
//System.out.println(newSeq.toCodeString());


//}
//*/

//public void testNoMinPossible() {
////Sequence: CANNOT be minimized
////1. Stack s = new Stack();
////2. s.pop();
////- create primitive value: int 1
////3. s.push(1);
////==> Error: IndexOutOfBoundsException


//SlowStatement constrStackS = callEmptyConstructor(ubStackClass);
//Class<?>[] emptyArgs = { };
//Class<?>[] pushArgs = { int.class };
//List<Variable> inputVars = new ArrayList<Variable> ();

//inputVars.add(constrStackS.result); //receiver
//SlowStatement popS = callMethod(ubStackClass, "pop", emptyArgs, inputVars);

//SlowStatement integerOne = new SlowStatement(new PrimitiveOrStringOrNullDeclInfo(int.class, 1), new ArrayList<Variable> ());

//inputVars = new ArrayList<Variable> ();
//inputVars.add(constrStackS.result); //receiver
//inputVars.add(integerOne.result);         //parameter
//SlowStatement pushS = callMethod(ubStackClass, "push", pushArgs, inputVars);

//List <SlowStatement> statements = new ArrayList<SlowStatement> ();
//statements.add(constrStackS);
//statements.add(popS);
//statements.add(integerOne);
//statements.add(pushS);

//SlowSequence seq = new SlowSequence(statements);

////System.out.println(seq.toCodeString());

//TestCase badSeq = new TestCaseThatThrowsBadException(seq, new IndexOutOfBoundsException());


//TestCase minSeq = Minimizer2.minimize(badSeq); 

////assertTrue(badSeq.equals(minSeq));
//System.out.println(badSeq.toCodeString());
//System.out.println(minSeq.toCodeString());
//}

//public void testMinPossible() {
////Sequence: CAN be minimized  ==> can take out step #3
////1. Stack s = new Stack();
////2. s.pop();
////3. s.pop();
////- create primitive value: int 1
////4. s.push(1);
////5. s.push(1);
////==> Error: IndexOutOfBoundsException


//SlowStatement constrStackS = callEmptyConstructor(ubStackClass);
//Class<?>[] emptyArgs = { };
//Class<?>[] pushArgs = { int.class };
//List<Variable> inputVars = new ArrayList<Variable> ();

//inputVars.add(constrStackS.result); //receiver
//SlowStatement pop1S = callMethod(ubStackClass, "pop", emptyArgs, inputVars);

//inputVars = new ArrayList<Variable> ();
//inputVars.add(constrStackS.result); //receiver
//SlowStatement pop2S = callMethod(ubStackClass, "pop", emptyArgs, inputVars);


//SlowStatement integerOne = new SlowStatement(new PrimitiveOrStringOrNullDeclInfo(int.class, 1), new ArrayList<Variable> ());

//inputVars = new ArrayList<Variable> ();
//inputVars.add(constrStackS.result); //receiver
//inputVars.add(integerOne.result);         //parameter
//SlowStatement pushS = callMethod(ubStackClass, "push", pushArgs, inputVars);

//inputVars = new ArrayList<Variable> ();
//inputVars.add(constrStackS.result); //receiver
//inputVars.add(integerOne.result);         //parameter
//SlowStatement pushS2 = callMethod(ubStackClass, "push", pushArgs, inputVars);

//List <SlowStatement> statements = new ArrayList<SlowStatement> ();
//statements.add(constrStackS);
//statements.add(pop1S);
//statements.add(pop2S);
//statements.add(integerOne);
//statements.add(pushS);
//statements.add(pushS2);

//SlowSequence seq = new SlowSequence(statements);

//TestCase badSeq = new TestCaseThatThrowsBadException(seq, new IndexOutOfBoundsException());

//TestCase minSeq = Minimizer2.minimize(badSeq); 

//System.out.println(badSeq.toCodeString());
//System.out.println(minSeq.toCodeString());
//}

//public void testUnaryFaultyBehavior() {
////For testing purposes, the toString() method in UBStack throws an
////exception if stack contains less than two elements.

////Sequence: CAN be minimized ==> can take out step #2
////1. Stack s = new Stack();
////2. s.push(1);
////==> Error: s.toString() throws exception

//SlowStatement constrStackS = callEmptyConstructor(ubStackClass);
//Class<?>[] emptyArgs = { };
//Class<?>[] pushArgs = { int.class };
//List<Variable> inputVars = new ArrayList<Variable> ();

//SlowStatement integerOne = new SlowStatement(new PrimitiveOrStringOrNullDeclInfo(int.class, 1), new ArrayList<Variable> ());

//inputVars.add(constrStackS.result); //receiver
//inputVars.add(integerOne.result);         //parameter
//SlowStatement pushS = callMethod(ubStackClass, "push", pushArgs, inputVars);

//List <SlowStatement> statements = new ArrayList<SlowStatement> ();
//statements.add(constrStackS);
//statements.add(integerOne);
//statements.add(pushS);

//SlowSequence seq = new SlowSequence(statements);

//Decoration badBehavior = new ToStringThrowsException(RuntimeException.class, null);

//List <Variable> errorVars = new ArrayList<Variable> ();
//errorVars.add(constrStackS.result);

//TestCase badSeq = new TestCaseThatCreatesBadObjects(seq, errorVars, badBehavior);

//TestCase minSeq = Minimizer2.minimize(badSeq);

//System.out.println(badSeq.toSequence().toCodeString());
//System.out.println(minSeq.toSequence().toCodeString());
//}

//public void testBinaryFaultyBehavior() {
////For testing purposes, the equals() method in UBStack throws an
////exception if stack contains less than two elements.

////Sequence: CAN be minimized ==> can take out step #2
////1. Stack s = new Stack();
////2. s.push(1);
////3. Stack t = new Stack();
////4. t.push(1);
////==> Error: s.equals(t) throws exception

//SlowStatement constrStackS = callEmptyConstructor(ubStackClass);
//Class<?>[] emptyArgs = { };
//Class<?>[] pushArgs = { int.class };
//List<Variable> inputVars = new ArrayList<Variable> ();

//SlowStatement integerOne = new SlowStatement(new PrimitiveOrStringOrNullDeclInfo(int.class, 1), new ArrayList<Variable> ());

//inputVars.add(constrStackS.result); //receiver
//inputVars.add(integerOne.result);         //parameter
//SlowStatement pushS = callMethod(ubStackClass, "push", pushArgs, inputVars);

//SlowStatement constrStackT = callEmptyConstructor(ubStackClass);
//inputVars = new ArrayList<Variable> (); 
//inputVars.add(constrStackT.result); //receiver
//inputVars.add(integerOne.result);         //parameter
//SlowStatement pushT = callMethod(ubStackClass, "push", pushArgs, inputVars);

//List <SlowStatement> statements = new ArrayList<SlowStatement> ();
//statements.add(constrStackS);
//statements.add(integerOne);
//statements.add(pushS);
//statements.add(constrStackT);
//statements.add(pushT);

//SlowSequence seq = new SlowSequence(statements);

//Decoration badBehavior = new EqualsThrowsException(RuntimeException.class, null);

//List <Variable> errorVars = new ArrayList<Variable> ();
//errorVars.add(constrStackS.result);
//errorVars.add(constrStackT.result);

//TestCase badSeq = new TestCaseThatCreatesBadObjects(seq, errorVars, badBehavior);

//TestCase minSeq = Minimizer2.minimize(badSeq);

//System.out.println(badSeq.toSequence().toCodeString());
//System.out.println(minSeq.toSequence().toCodeString());
//}



///*
//* Creates statement that calls empty constructors
//*/
//private SlowStatement callEmptyConstructor(Class<?> c) {
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
//private SlowStatement callMethod(Class<?> c, String name, Class<?>[] args, List<Variable> inputs) {
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

//public void testReplacerMinimizer() {
//SlowStatement constrStackS = callEmptyConstructor(ubStackClass);
//Class<?>[] emptyArgs = { };
//Class<?>[] pushArgs = { int.class };
//List<Variable> inputVars = new ArrayList<Variable> ();

//inputVars.add(constrStackS.result); //receiver
//SlowStatement pop1S = callMethod(ubStackClass, "pop", emptyArgs, inputVars);

//inputVars = new ArrayList<Variable> ();
//inputVars.add(constrStackS.result); //receiver
//SlowStatement pop2S = callMethod(ubStackClass, "pop", emptyArgs, inputVars);


//SlowStatement integerOne = new SlowStatement(new PrimitiveOrStringOrNullDeclInfo(int.class, 1), new ArrayList<Variable> ());

//inputVars = new ArrayList<Variable> ();
//inputVars.add(constrStackS.result); //receiver
//inputVars.add(integerOne.result);         //parameter
//SlowStatement pushS = callMethod(ubStackClass, "push", pushArgs, inputVars);

//inputVars = new ArrayList<Variable> ();
//inputVars.add(constrStackS.result); //receiver
//inputVars.add(integerOne.result);         //parameter
//SlowStatement pushS2 = callMethod(ubStackClass, "push", pushArgs, inputVars);

//List <SlowStatement> statements = new ArrayList<SlowStatement> ();
//statements.add(constrStackS);
//statements.add(pop1S);
//statements.add(pop2S);
//statements.add(integerOne);
//statements.add(pushS);
//statements.add(pushS2);

//SlowSequence longSeq = new SlowSequence(statements);

//statements = new ArrayList<SlowStatement> ();
//statements.add(constrStackS);

//SlowSequence constructorSeq = new SlowSequence(statements);

//statements = new ArrayList<SlowStatement> ();
//statements.add(constrStackS);
//statements.add(pop1S);

//SlowSequence popSeq = new SlowSequence(statements);




//}






//public static Test suite() { 
//TestSuite suite = new TestSuite("Minimizer Tests");
////suite.addTest(new Minimizer2Test("testReachability"));
////suite.addTest(new Minimizer2Test("testNoMinPossible"));
////suite.addTest(new Minimizer2Test("testMinPossible"));
////suite.addTest(new Minimizer2Test("testUnaryFaultyBehavior"));
////suite.addTest(new Minimizer2Test("testBinaryFaultyBehavior"));
//suite.addTest(new Minimizer2Test("testReplacerMinimizer"));
//return suite; 
//}

}
