package randoop.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import randoop.ComponentManager;
import randoop.EqualsHashcode;
import randoop.EqualsReflexive;
import randoop.EqualsSymmetric;
import randoop.EqualsToNullRetFalse;
import randoop.ObjectContract;
import randoop.SeedSequences;
import randoop.main.GenInputsAbstract;
import randoop.main.GenInputsAbstract.BehaviorType;
import randoop.operation.Operation;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.ForwardGenerator;
import randoop.sequence.Sequence;
import randoop.test.bh.BH;
import randoop.test.bh.Body;
import randoop.test.bh.Cell;
import randoop.test.bh.MathVector;
import randoop.test.bh.Node;
import randoop.test.bh.Tree;
import randoop.test.predicate.AlwaysTrueExceptionPredicate;
import randoop.test.predicate.DefaultFailureExceptionPredicate;
import randoop.test.predicate.ExceptionPredicate;
import randoop.test.predicate.NPEContractPredicate;
import randoop.util.ReflectionExecutor;
import randoop.util.predicate.Predicate;

import junit.framework.TestCase;

public class ForwardExplorerTests extends TestCase {

  public static void test1() {

    List<Operation> model =
      OperationExtractor.getOperations(Arrays.<Class<?>>asList(Long.class), null);
    assertTrue("model not empty", model.size() != 0);
    GenInputsAbstract.dontexecute = true; // FIXME make this an instance field?
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    ForwardGenerator explorer = new ForwardGenerator(model,
      Long.MAX_VALUE, 5000, 5000, mgr, null, null);
    explorer.addTestCheckGenerator(createChecker());
    explorer.addTestPredicate(createOutputTest());
    explorer.explore();
    GenInputsAbstract.dontexecute = false;
    assertTrue(explorer.allSequences.size() != 0);
  }

  public void test2() throws Throwable {
    boolean bisort = false;
    boolean bimerge = false;
    boolean inorder = false;
    boolean swapleft = false;
    boolean swapright = false;
    boolean random = false;

    List<Class<?>> classes = new ArrayList<Class<?>>();
    classes.add(randoop.test.BiSortVal.class);
    classes.add(BiSort.class);
    //GenFailures.noprogressdisplay = true;
    //Log.log = new FileWriter("templog.txt");
    int oldTimeout = ReflectionExecutor.timeout;
    ReflectionExecutor.timeout = 200;
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    List<Operation> model = OperationExtractor.getOperations(classes, null);
    assertTrue("model should not be empty", model.size() != 0);
    ForwardGenerator exp =
      new ForwardGenerator(model, Long.MAX_VALUE, 200, 200, mgr, null, null);
    exp.addTestCheckGenerator(createChecker());
    exp.addTestPredicate(createOutputTest());
    exp.explore();
    ReflectionExecutor.timeout = oldTimeout;
    for (Sequence s : exp.allSequences()) {
      String str = s.toCodeString();
      if (str.contains("bisort")) bisort = true;
      if (str.contains("bimerge")) bimerge = true;
      if (str.contains("inOrder")) inorder = true;
      if (str.contains("swapValLeft")) swapleft = true;
      if (str.contains("swapValRight")) swapright = true;
      if (str.contains("random")) random = true;
    }

    assertTrue(bisort);
    assertTrue(bimerge);
    assertTrue(inorder);
    assertTrue(swapleft);
    assertTrue(swapright);
    assertTrue(random);
  }

  public void test4() throws Exception {

    boolean bh = false;
    boolean body = false;
    boolean cell = false;
    boolean mathvector = false;
    boolean node = false;
    boolean tree = false;

    List<Class<?>> classes = new ArrayList<Class<?>>();
    classes.add(BH.class);
    classes.add(Body.class);
    classes.add(Cell.class);
    classes.add(MathVector.class);
    classes.add(Node.class);
    classes.add(Tree.class);

    System.out.println(classes);

    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    List<Operation> model = OperationExtractor.getOperations(classes, null);
    assertTrue("model should not be empty", model.size() != 0);
    ForwardGenerator exp =
      new ForwardGenerator(model, Long.MAX_VALUE, 200, 200, mgr, null, null);
    GenInputsAbstract.null_ratio = 0.05; //used to be forbid_null = false
    exp.addTestCheckGenerator(createChecker());
    exp.addTestPredicate(createOutputTest());
    exp.explore();
    for (Sequence s : exp.allSequences()) {
      String str = s.toCodeString();
      if (str.contains("BH")) bh = true;
      if (str.contains("Body")) body = true;
      if (str.contains("Cell")) cell = true;
      if (str.contains("MathVector")) mathvector = true;
      if (str.contains("Node")) node = true;
      if (str.contains("Tree")) tree = true;
    }
    assertTrue(bh);
    assertTrue(body);
    assertTrue(cell);
    assertTrue(mathvector);
    assertTrue(node);
    assertTrue(tree);
  }
  
  private static TestCheckGenerator createChecker() {
    List<ObjectContract> contracts = new ArrayList<ObjectContract>();

    // Now add all of Randoop's default contracts.
    // Note: if you add to this list, also update the Javadoc for check_object_contracts.
    contracts.add(new EqualsReflexive());
    contracts.add(new EqualsSymmetric());
    contracts.add(new EqualsHashcode());
    contracts.add(new EqualsToNullRetFalse());
    
    ExceptionPredicate exceptionChecker = new DefaultFailureExceptionPredicate();
    if (GenInputsAbstract.npe_on_null_input == BehaviorType.ERROR) {
      exceptionChecker = exceptionChecker.or(new NPEContractPredicate());
    }
    ContractCheckingVisitor contractChecker = new ContractCheckingVisitor(contracts,exceptionChecker);
    VisibilityPredicate visibility = new PublicVisibilityPredicate();
    ExceptionPredicate isExpected = new AlwaysTrueExceptionPredicate();
    ExpectedExceptionCheckGen expectation; 
    expectation = new ExpectedExceptionCheckGen(visibility, isExpected);
    RegressionCaptureVisitor regressionCapture = new RegressionCaptureVisitor(expectation, true);
    return new ExtendGenerator(contractChecker,regressionCapture);
  }
  
  private static Predicate<ExecutableSequence> createOutputTest() {
    return new ErrorTestPredicate().or(new RegressionTestPredicate());
  }
}
