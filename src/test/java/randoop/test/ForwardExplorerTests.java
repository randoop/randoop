package randoop.test;

import static org.junit.Assert.assertTrue;
import static randoop.main.GenInputsAbstract.require_classname_in_test;
import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.generation.ComponentManager;
import randoop.generation.ForwardGenerator;
import randoop.generation.SeedSequences;
import randoop.generation.TestUtils;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.main.OptionsCache;
import randoop.main.RandoopBug;
import randoop.operation.ConstructorCall;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.ReflectionManager;
import randoop.reflection.VisibilityPredicate;
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
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.MultiMap;
import randoop.util.ReflectionExecutor;

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
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    List<Class<?>> classes = Collections.singletonList(Long.class);

    final List<TypedOperation> model = getConcreteOperations(classes);

    assertTrue("model not empty", model.size() != 0);
    GenInputsAbstract.dontexecute = true; // FIXME make this an instance field?
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    ForwardGenerator explorer =
        new ForwardGenerator(
            model,
            new LinkedHashSet<TypedOperation>(),
            new GenInputsAbstract.Limits(0, 1000, 1000, 1000),
            mgr,
            null,
            null);
    explorer.setTestCheckGenerator(createChecker(new ContractSet()));
    explorer.setTestPredicate(createOutputTest());
    TestUtils.setAllLogs(explorer);
    explorer.createAndClassifySequences();
    explorer.getOperationHistory().outputTable();
    GenInputsAbstract.dontexecute = false;
    assertTrue(explorer.numGeneratedSequences() != 0);
  }

  private static List<TypedOperation> getConcreteOperations(List<Class<?>> classes) {
    final List<TypedOperation> model = new ArrayList<>();
    VisibilityPredicate visibility = IS_PUBLIC;
    ReflectionManager mgr = new ReflectionManager(visibility);
    for (Class<?> c : classes) {
      ClassOrInterfaceType classType = ClassOrInterfaceType.forClass(c);
      final OperationExtractor extractor =
          new OperationExtractor(classType, new DefaultReflectionPredicate(), visibility);
      mgr.apply(extractor, c);
      model.addAll(extractor.getOperations());
    }
    return model;
  }

  @Test
  public void test2() throws Throwable {
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

    boolean bisort = false;
    boolean bimerge = false;
    boolean inorder = false;
    boolean swapleft = false;
    boolean swapright = false;
    boolean random = false;

    List<Class<?>> classes = new ArrayList<>();
    classes.add(randoop.test.BiSortVal.class);
    classes.add(BiSort.class);
    // GenFailures.progressdisplay = false;
    // Log.log = new FileWriter("templog.txt");
    int oldCallTimeout = ReflectionExecutor.call_timeout;
    ReflectionExecutor.call_timeout = 500;
    long oldProgressintervalsteps = GenInputsAbstract.progressintervalsteps;
    GenInputsAbstract.progressintervalsteps = 100;
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    final List<TypedOperation> model = getConcreteOperations(classes);
    assertTrue("model should not be empty", model.size() != 0);
    ForwardGenerator explorer =
        new ForwardGenerator(
            model,
            new LinkedHashSet<TypedOperation>(),
            new GenInputsAbstract.Limits(0, 200, 200, 200),
            mgr,
            null,
            null);
    explorer.setTestCheckGenerator(createChecker(new ContractSet()));
    explorer.setTestPredicate(createOutputTest());
    TestUtils.setAllLogs(explorer);
    explorer.createAndClassifySequences();
    explorer.getOperationHistory().outputTable();
    ReflectionExecutor.call_timeout = oldCallTimeout;
    GenInputsAbstract.progressintervalsteps = oldProgressintervalsteps;
    for (Sequence s : explorer.getAllSequences()) {
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
    randoop.util.Randomness.setSeed(0);
    ReflectionExecutor.resetStatistics();

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
    ComponentManager mgr = new ComponentManager(SeedSequences.defaultSeeds());
    final List<TypedOperation> model = getConcreteOperations(classes);
    assertTrue("model should not be empty", model.size() != 0);
    ForwardGenerator explorer =
        new ForwardGenerator(
            model,
            new LinkedHashSet<TypedOperation>(),
            new GenInputsAbstract.Limits(0, 200, 200, 200),
            mgr,
            null,
            null);
    GenInputsAbstract.forbid_null = false;
    explorer.setTestCheckGenerator(createChecker(new ContractSet()));
    explorer.setTestPredicate(createOutputTest());
    TestUtils.setAllLogs(explorer);
    explorer.createAndClassifySequences();
    explorer.getOperationHistory().outputTable();
    for (Sequence s : explorer.getAllSequences()) {
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
    return GenTests.createTestCheckGenerator(
        IS_PUBLIC, contracts, new MultiMap<Type, TypedOperation>());
  }

  private static Predicate<ExecutableSequence> createOutputTest() {
    Set<Sequence> sequences = new LinkedHashSet<>();
    ConstructorCall objectConstructor;
    try {
      objectConstructor = new ConstructorCall(Object.class.getConstructor());
    } catch (Exception e) {
      throw new RandoopBug(e); // Should never reach here!
    }
    TypedOperation op =
        new TypedClassOperation(
            objectConstructor, JavaTypes.OBJECT_TYPE, new TypeTuple(), JavaTypes.OBJECT_TYPE);
    sequences.add(new Sequence().extend(op, new ArrayList<Variable>()));
    return new GenTests()
        .createTestOutputPredicate(
            sequences, new LinkedHashSet<Class<?>>(), require_classname_in_test);
  }
}
