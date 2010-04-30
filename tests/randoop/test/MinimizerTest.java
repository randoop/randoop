package randoop.test;

import junit.framework.TestCase;



public class MinimizerTest extends TestCase {

//private Sequence primitivesequuence;

//private PrintStream printStream;

//private ByteArrayOutputStream bos;

//private Class<?> ubStackClass;
//private Class<?> aClass;

//public static void main(String[] args) {
//}

//@Override
//protected void setUp() throws Exception {
//try {
//this.bos = new ByteArrayOutputStream();
//this.printStream = new PrintStream(bos);

//// Primitive sequuence containing the int 1
//primitivesequuence = new sequuence((new PrimitiveOrStringOrNullDeclInfo(
//Integer.TYPE, 1)));

//ubStackClass<?> = Class.forName("randoop.test.UBStack");
//aClass<?> = Class.forName("randoop.test.A");


//} catch (ClassNotFoundException e) {
//// exceptions while creating classes
//throw e;
//}
//}

//public void testNoMinPossible() {
////create a ubStack object
//sequence stackConstructorsequence = createSimpleConstructorsequence(ubStackClass);

//// call pop on that empty ubStack object
//sequenceIndexPair[] popInputsequences = { new sequenceIndexPair(stackConstructorsequence, 0) };
//Class<?>[] popArgs = {};
//sequence popsequence = createSimpleMethodsequence(ubStackClass, "pop",
//popInputsequences, popArgs);

//// call pop and push on that same ubStack object
//sequenceIndexPair[] popPushInputsequences = {
//new sequenceIndexPair(popsequence, 0),
//new sequenceIndexPair(primitivesequence, 0) };
//Class<?>[] popPushArgs = { int.class };
//sequence popPushsequence = createSimpleMethodsequence(ubStackClass, "push",
//popPushInputsequences, popPushArgs);

//// create empty stack --> pop() --> push() --> ERROR

//sequenceWithUnaryFaultyBehavior f_orig = new sequenceWithUnaryFaultyBehavior
//(new sequenceIndexPair(popPushsequence, 0), 
//new sequenceThrowsException(IndexOutOfBoundsException.class, null));
////sequenceWithUnaryFaultyBehavior f_min = f_orig.minimize();

////assertEquals(f_orig.getP1().sequence,f_min.getP1().sequence);       
//}

//public void testMinPossible() {
////create a ubStack object
//sequence stackConstructorsequence = createSimpleConstructorsequence(ubStackClass);

//// call pop on that empty ubStack object
//sequenceIndexPair[] popInputsequences = { new sequenceIndexPair(stackConstructorsequence, 0) };
//Class<?>[] popArgs = {};
//sequence popsequence = createSimpleMethodsequence(ubStackClass, "pop",
//popInputsequences, popArgs);

////call pop again on that empty ubStack object
//sequenceIndexPair[] popPopInputsequences = { new sequenceIndexPair(popsequence, 0) };
//Class<?>[] popPopArgs = {};
//sequence popPopsequence = createSimpleMethodsequence(ubStackClass, "pop",
//popPopInputsequences, popPopArgs);


//// call pop and push on that same ubStack object
//sequenceIndexPair[] popPopPushInputsequences = {
//new sequenceIndexPair(popPopsequence, 0),
//new sequenceIndexPair(primitivesequence, 0) };
//Class<?>[] popPopPushArgs = { int.class };
//sequence popPopPushsequence = createSimpleMethodsequence(ubStackClass, "push",
//popPopPushInputsequences, popPopPushArgs);
////create empty stack --> pop() --> pop() --> push() --> ERROR

//sequenceWithUnaryFaultyBehavior f_orig = new sequenceWithUnaryFaultyBehavior
//(new sequenceIndexPair(popPopPushsequence, 0), 
//new sequenceThrowsException(IndexOutOfBoundsException.class, null));


////sequenceWithUnaryFaultyBehavior f_min = f_orig.minimize();



//////System.out.println(f_orig.getP1().sequence.toString());
//////System.out.println();
//////System.out.println(f_min.getP1().sequence.toString());

////assertFalse(f_orig.getP1().sequence.equals(f_min.getP1().sequence));          
//}





//public void testBinaryFaultBehavior() {

////InstanceMethodThrowsException f = new EqualsThrowsException(RuntimeException.class,null);
////assertFalse(f.checkFaultyBehavior(new A(), new A()) == null);



////sequence sequence1 = createSimpleConstructorsequence(aClass);
////sequence sequence2 = createSimpleConstructorsequence(aClass);

////sequenceWithBinaryFaultyBehavior f_orig = new sequenceWithBinaryFaultyBehavior
////(new sequenceIndexPair(sequence1, 0),
////new sequenceIndexPair(sequence2, 0),        
////new EqualsThrowsException(RuntimeException.class,null));

////sequenceWithBinaryFaultyBehavior f_min = f_orig.minimize();

////assertEquals(f_min.getP1().sequence, f_min.getP2().sequence);


////create stack #1
//sequence stackConstructorsequence1 = createSimpleConstructorsequence(ubStackClass);

////push 1 onto stack #1
//sequenceIndexPair[] pushInputsequences1 = {
//new sequenceIndexPair(stackConstructorsequence1, 0),
//new sequenceIndexPair(primitivesequence, 0) };
//Class<?>[] pushArgs = { int.class };
//sequence pushsequence1 = createSimpleMethodsequence(ubStackClass, "push",
//pushInputsequences1, pushArgs);


////create stack #2
//Isequence stackConstructorsequence2 = createSimpleConstructorsequence(ubStackClass);

////push 1 onto stack #2
//sequenceIndexPair[] pushInputsequences2 = {
//new sequenceIndexPair(stackConstructorsequence1, 0),
//new sequenceIndexPair(primitivesequence, 0) };
//sequence pushsequence2 = createSimpleMethodsequence(ubStackClass, "push",
//pushInputsequences2, pushArgs);

////push 1 again onto stack #2
//sequenceIndexPair[] pushPushInputsequences2 = {
//new sequenceIndexPair(pushsequence2, 0),
//new sequenceIndexPair(primitivesequence, 0) };
//sequence pushPushsequence2 = createSimpleMethodsequence(ubStackClass, "push",
//pushPushInputsequences2, pushArgs);



//sequenceWithBinaryFaultyBehavior f_orig = new sequenceWithBinaryFaultyBehavior
//(new sequenceIndexPair(pushsequence1, 0),
//new sequenceIndexPair(pushPushsequence2, 0),        
//new EqualsThrowsException(RuntimeException.class,null));



//sequenceWithBinaryFaultyBehavior f_min = f_orig.minimize();

////System.out.println("ORIGINAL");
////System.out.println("sequence 1:");
////System.out.println(f_orig.getP1().sequence.toString());
////System.out.println("sequence 2:");
////System.out.println(f_orig.getP2().sequence.toString());
////System.out.println();
////System.out.println("MINIMIZED");
////System.out.println("sequence 1:");
////System.out.println(f_min.getP1().sequence.toString());
////System.out.println("sequence 2:");
////System.out.println(f_min.getP2().sequence.toString());

//assertFalse(f_min.getP1().sequence.equals(f_orig.getP1().sequence));
//assertFalse(f_min.getP2().sequence.equals(f_orig.getP2().sequence));

//}


///*

//public void testRootSplicing() {
////create stack #1
//sequence stackConstructorsequence1 = createSimpleConstructorsequence(ubStackClass);

////push 1 onto stack #1
//sequenceIndexPair[] pushInputsequences1 = {
//new sequenceIndexPair(stackConstructorsequence1, 0),
//new sequenceIndexPair(primitivesequence, 0) };
//Class<?>[] pushArgs = { int.class };
//sequence pushsequence1 = createSimpleMethodsequence(ubStackClass, "push",
//pushInputsequences1, pushArgs);


////create stack #2
//sequence stackConstructorsequence2 = createSimpleConstructorsequence(ubStackClass);

////push 1 onto stack #2
//sequenceIndexPair[] pushInputsequences2 = {
//new sequenceIndexPair(stackConstructorsequence1, 0),
//new sequenceIndexPair(primitivesequence, 0) };
//sequence pushsequence2 = createSimpleMethodsequence(ubStackClass, "push",
//pushInputsequences2, pushArgs);

////push 1 again onto stack #2
//sequenceIndexPair[] pushPushInputsequences2 = {
//new sequenceIndexPair(pushsequence2, 0),
//new sequenceIndexPair(primitivesequence, 0) };
//sequence pushPushsequence2 = createSimpleMethodsequence(ubStackClass, "push",
//pushPushInputsequences2, pushArgs);

////System.out.println(Minimizer.replaceRootTransformIndex(stackConstructorsequence1,pushsequence1,0));

//sequenceWithUnaryFaultyBehavior f_orig = new sequenceWithUnaryFaultyBehavior
//(new sequenceIndexPair(pushsequence1, 0), 
//new HashcodeThrowsException(RuntimeException.class, null));

//System.out.println("ORIGINAL");
//System.out.println("sequence 1:");
//System.out.println(f_orig.getP1().sequence.toString());

//sequenceWithUnaryFaultyBehavior f_min = f_orig.minimize();


//System.out.println();
//System.out.println("MINIMIZED");
//System.out.println("sequence 1:");
//System.out.println(f_min.getP1().sequence.toString());

//}


//*/


///*
//* Creates sequence with one statement: a ConstructorCallInfo of type c
//* NOTE: Creates empty constructors
//*/
//private sequence createSimpleConstructorsequence(Class<?> c) {
//sequence p = null;
//sequenceIndexPair[] inputs = {};
//try {
//Constructor<?> defaultConstructor = c.getConstructor(new Class[0]);
//StatementInfo cTransformer = ConstructorCallInfo
//.getDefaultStatementInfo(defaultConstructor);
//p = new sequence(cTransformer, inputs);
//} catch (NoSuchMethodException e) {
//// to catch exceptions from Class.getConstructor("..")
//}
//return p;
//}

///*
//* Creates sequence with one statement: a MethodCallInfo of type c
//*/
//private sequence createSimpleMethodsequence(Class<?> c, String name,
//sequenceIndexPair[] inputs, Class<?>[] args) {
//sequence p = null;
//try {
//StatementInfo mTransformer = MethodCallInfo.getDefaultStatementInfo(c
//.getMethod(name, args));
//p = new sequence(mTransformer, inputs);
//} catch (NoSuchMethodException e) {
//throw new RuntimeException(e);
//// to catch exceptions from Class.getConstructor("..")
//}
//return p;
//}

}
