package randoop.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.BugInRandoopException;
import randoop.generation.ComponentManager;
import randoop.generation.ForwardGenerator;
import randoop.generation.SeedSequences;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.main.OptionsCache;
import randoop.operation.ConstructorCall;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionManager;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.test.bh.BH;
import randoop.test.bh.Body;
import randoop.test.bh.Cell;
import randoop.test.bh.MathVector;
import randoop.test.bh.Node;
import randoop.test.bh.Tree;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ConcreteTypes;
import randoop.types.GeneralType;
import randoop.types.TypeTuple;
import randoop.util.MultiMap;
import randoop.util.ReflectionExecutor;
import randoop.util.predicate.Predicate;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static randoop.main.GenInputsAbstract.include_if_classname_appears;

public class ForwardExplorerTests {

  private static OptionsCache optionsCache;

   @BeforeClass
   public static void setup() {
     optionsCache = new OptionsCache();
     optionsCache.saveState();
   }

   @AfterClass
   public static void restore() {
     optionsCache.restoreState();
   }

  @Test
  public void test1() {
    List<Class<?>> classes = new ArrayList<>();
    classes.add(Long.class);

    final List<TypedOperation> model = getConcreteOperations(classes);

    assertTrue("model not empty", model.size() != 0);
    GenInputsAbstract.dontexecute = true; // FIXME make this an instance field?
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    ForwardGenerator explorer =
        new ForwardGenerator(model, new LinkedHashSet<TypedOperation>(), Long.MAX_VALUE, 5000, 5000, mgr, null, null);
    explorer.addTestCheckGenerator(createChecker(new ContractSet()));
    explorer.addTestPredicate(createOutputTest());
    explorer.explore();
    GenInputsAbstract.dontexecute = false;
    assertTrue(explorer.numGeneratedSequences() != 0);
  }

  private static List<TypedOperation> getConcreteOperations(List<Class<?>> classes) {
    final List<TypedOperation> model = new ArrayList<>();

    ReflectionManager mgr = new ReflectionManager(new PublicVisibilityPredicate());
    for (Class<?> c: classes) {
      ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
      mgr.apply(new OperationExtractor(classType, model, new DefaultReflectionPredicate()), c);
    }
    return model;
  }

  @Test
  public void test2() throws Throwable {
    boolean bisort = false;
    boolean bimerge = false;
    boolean inorder = false;
    boolean swapleft = false;
    boolean swapright = false;
    boolean random = false;

    List<Class<?>> classes = new ArrayList<>();
    classes.add(randoop.test.BiSortVal.class);
    classes.add(BiSort.class);
    //GenFailures.noprogressdisplay = true;
    //Log.log = new FileWriter("templog.txt");
    int oldTimeout = ReflectionExecutor.timeout;
    ReflectionExecutor.timeout = 200;
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    final List<TypedOperation> model = getConcreteOperations(classes);
    assertTrue("model should not be empty", model.size() != 0);
    GenInputsAbstract.ignore_flaky_tests = true;
    ForwardGenerator exp = new ForwardGenerator(model, new LinkedHashSet<TypedOperation>(), Long.MAX_VALUE, 200, 200, mgr, null, null);
    exp.addTestCheckGenerator(createChecker(new ContractSet()));
    exp.addTestPredicate(createOutputTest());
    try {
      exp.explore();
    } catch (Throwable t) {
      fail("Exception during generation: " + t);
    }
    ReflectionExecutor.timeout = oldTimeout;
    for (Sequence s : exp.getAllSequences()) {
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

  @Test
  public void test4() throws Exception {

    boolean bh = false;
    boolean body = false;
    boolean cell = false;
    boolean mathvector = false;
    boolean node = false;
    boolean tree = false;

    List<Class<?>> classes = new ArrayList<>();
    classes.add(BH.class);
    classes.add(Body.class);
    classes.add(Cell.class);
    classes.add(MathVector.class);
    classes.add(Node.class);
    classes.add(Tree.class);

    System.out.println(classes);
    GenInputsAbstract.ignore_flaky_tests = true;
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    final List<TypedOperation> model = getConcreteOperations(classes);
    assertTrue("model should not be empty", model.size() != 0);
    ForwardGenerator exp = new ForwardGenerator(model, new LinkedHashSet<TypedOperation>(), Long.MAX_VALUE, 200, 200, mgr, null, null);
    GenInputsAbstract.forbid_null = false;
    exp.addTestCheckGenerator(createChecker(new ContractSet()));
    exp.addTestPredicate(createOutputTest());
    try {
      exp.explore();
    } catch (Throwable t) {
      fail("Exception during generation: " + t);
    }
    for (Sequence s : exp.getAllSequences()) {
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

  private static TestCheckGenerator createChecker(ContractSet contracts) {
    return (new GenTests()).createTestCheckGenerator(new PublicVisibilityPredicate(), contracts, new MultiMap<GeneralType, TypedOperation>(), new LinkedHashSet<TypedOperation>());
  }

  private static Predicate<ExecutableSequence> createOutputTest() {
    Set<Sequence> sequences = new LinkedHashSet<>();
    ConstructorCall objectConstructor;
    try {
      objectConstructor = new ConstructorCall(Object.class.getConstructor());
    } catch (Exception e) {
      throw new BugInRandoopException(e); // Should never reach here!
    }
    TypedOperation op = new TypedClassOperation(objectConstructor, ConcreteTypes.OBJECT_TYPE, new TypeTuple(), ConcreteTypes.OBJECT_TYPE);
    sequences.add((new Sequence().extend(op, new ArrayList<Variable>())));
    return (new GenTests())
        .createTestOutputPredicate(
            sequences, new LinkedHashSet<Class<?>>(), include_if_classname_appears);
  }
}
