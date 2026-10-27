package randoop.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

import java.util.Collections;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.generation.literaltfidf.ScopeToLiteralStatistics;
import randoop.main.GenInputsAbstract;
import randoop.main.OptionsCache;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;

/** Tests of {@link ComponentManager}. */
public class ComponentManagerTest {

  private static OptionsCache optionsCache;

  @BeforeClass
  public static void setup() {
    optionsCache = new OptionsCache();
    optionsCache.saveState();
  }

  @After
  public void restore() {
    optionsCache.restoreState();
  }

  /** A seed prevents adding its value to the component set again. */
  @Test
  public void seedSuppressesGeneratedValue() {
    Sequence seed = Sequence.createSequenceForPrimitive(4242);
    ComponentManager componentMgr = new ComponentManager(Collections.singleton(seed), IS_PUBLIC);
    componentMgr.addPrimitiveValueIfNew(4242);
    assertEquals(1, componentMgr.getAllGeneratedSequences().size());
  }

  /** A literal does not prevent adding its value to the component set, at any literals level. */
  @Test
  public void literalDoesNotSuppressGeneratedValue() {
    assertTrue(addsValueThatIsALiteral(GenInputsAbstract.ClassLiteralsMode.CLASS));
    assertTrue(addsValueThatIsALiteral(GenInputsAbstract.ClassLiteralsMode.PACKAGE));
    assertTrue(addsValueThatIsALiteral(GenInputsAbstract.ClassLiteralsMode.ALL));
  }

  /**
   * Returns true if {@link ComponentManager#addPrimitiveValueIfNew} adds a value that is a literal
   * of {@link ComponentManagerTest} to the component set.
   *
   * @param literalsLevel the value for {@link GenInputsAbstract#literals_level}
   * @return true if {@link ComponentManager#addPrimitiveValueIfNew} adds the value
   */
  private static boolean addsValueThatIsALiteral(
      GenInputsAbstract.ClassLiteralsMode literalsLevel) {
    GenInputsAbstract.literals_level = literalsLevel;
    Sequence literal = Sequence.createSequenceForPrimitive(4242);
    ScopeToLiteralStatistics literals = new ScopeToLiteralStatistics();
    literals.incrementNumUses(
        ClassOrInterfaceType.forClass(ComponentManagerTest.class), literal, 1);
    ComponentManager componentMgr = new ComponentManager(IS_PUBLIC);
    componentMgr.setScopeToLiteralStatistics(literals);
    componentMgr.addPrimitiveValueIfNew(4242);
    return componentMgr.getAllGeneratedSequences().contains(literal);
  }
}
