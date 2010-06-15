package randoop.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import randoop.ComponentManager;
import randoop.ForwardGenerator;
import randoop.SeedSequences;
import randoop.Sequence;
import randoop.SequenceCollection;
import randoop.StatementKind;
import randoop.main.GenInputsAbstract;
import randoop.test.bh.BH;
import randoop.test.bh.Body;
import randoop.test.bh.Cell;
import randoop.test.bh.MathVector;
import randoop.test.bh.Node;
import randoop.test.bh.Tree;
import randoop.test.perimeter.BlackNode;
import randoop.test.perimeter.GreyNode;
import randoop.test.perimeter.NorthEast;
import randoop.test.perimeter.NorthWest;
import randoop.test.perimeter.Perimeter;
import randoop.test.perimeter.QuadTreeNode;
import randoop.test.perimeter.Quadrant;
import randoop.test.perimeter.SouthEast;
import randoop.test.perimeter.SouthWest;
import randoop.test.perimeter.WhiteNode;
import randoop.util.Reflection;
import randoop.util.ReflectionExecutor;

public class ForwardExplorerTests extends TestCase {

  private static final int TIME_LIMIT_SECS = 1;

  public static void test1() {

    List<StatementKind> m =
      Reflection.getStatements(Arrays.<Class<?>>asList(Long.class), null);

    GenInputsAbstract.dontexecute = true; // FIXME make this an instance field?
    ComponentManager mgr = new ComponentManager(new SequenceCollection(SeedSequences.defaultSeeds()));
    ForwardGenerator explorer = new ForwardGenerator(m,
      null, Long.MAX_VALUE, 5000, mgr, null);
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
    ComponentManager mgr = new ComponentManager(new SequenceCollection(SeedSequences.defaultSeeds()));
    ForwardGenerator exp =
      new ForwardGenerator(Reflection.getStatements(classes, null), null, Long.MAX_VALUE, 200, mgr, null);
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

// Removed because it fails nondeterministically.
//   public void test3() {


//     boolean blacknode = false;
//     boolean greynode = false;
//     boolean northeast = false;
//     boolean northwest = false;
//     boolean perimeter = false;
//     boolean quadrant = false;
//     boolean quadtreenode = false;
//     boolean southeast = false;
//     boolean southwest = false;
//     boolean whitenode = false;


//     List<Class<?>> classes = new ArrayList<Class<?>>();
//     classes.add(BlackNode.class);
//     classes.add(GreyNode.class);
//     classes.add(NorthEast.class);
//     classes.add(NorthWest.class);
//     classes.add(Perimeter.class);
//     classes.add(Quadrant.class);
//     classes.add(QuadTreeNode.class);
//     classes.add(SouthEast.class);
//     classes.add(SouthWest.class);
//     classes.add(WhiteNode.class);

//     long oldTimeout = ReflectionExecutor.timeout;
//     ReflectionExecutor.timeout = 200;
//     ForwardGenerator exp =
//       new ForwardGenerator(Reflection.getStatements(classes, null), null, Long.MAX_VALUE, 200, null);
//     GenInputsAbstract.forbid_null = false;
//     exp.explore();
//     ReflectionExecutor.timeout = oldTimeout;
//     for (Sequence s : exp.allSequences()) {
//       String str = s.toCodeString();
//       if (str.contains("BlackNode")) blacknode = true;
//       if (str.contains("GreyNode")) greynode = true;
//       if (str.contains("NorthEast")) northeast = true;
//       if (str.contains("NorthWest")) northwest = true;
//       if (str.contains("Perimeter")) perimeter = true;
//       if (str.contains("Quadrant")) quadrant = true;
//       if (str.contains("QuadTreeNode")) quadtreenode = true;
//       if (str.contains("SouthEast")) southeast = true;
//       if (str.contains("SouthWest")) southwest = true;
//       if (str.contains("WhiteNode")) whitenode = true;
//     }

//     assertTrue(blacknode);
//     assertTrue(greynode);
//     assertTrue(northeast);
//     assertTrue(northwest);
//     assertTrue(perimeter);
//     assertTrue(quadrant);
//     assertTrue(quadtreenode);
//     assertTrue(southeast);
//     assertTrue(southwest);
//     assertTrue(whitenode);
//   }


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

    ComponentManager mgr = new ComponentManager(new SequenceCollection(SeedSequences.defaultSeeds()));
    ForwardGenerator exp =
      new ForwardGenerator(Reflection.getStatements(classes, null), null, Long.MAX_VALUE, 200, mgr, null);
    GenInputsAbstract.forbid_null = false;
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
}
