package randoop.generation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static randoop.reflection.AccessibilityPredicate.IS_PUBLIC;

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

  /** A class under test whose constructor creates a non-primitive value. */
  public static class Box {
    /** The boxed value. */
    public final int value;

    /**
     * Creates a Box.
     *
     * @param value the boxed value
     */
    public Box(int value) {
      this.value = value;
    }
  }

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

  /** A CLASS literal does not prevent adding its value to the component set. */
  @Test
  public void classLiteralDoesNotSuppressGeneratedValue() {
    assertTrue(addsValueThatIsALiteral(GenInputsAbstract.ClassLiteralsMode.CLASS));
  }

  /** An ALL literal prevents adding its value to the component set. */
  @Test
  public void allLiteralSuppressesGeneratedValue() {
    assertFalse(addsValueThatIsALiteral(GenInputsAbstract.ClassLiteralsMode.ALL));
  }

  /**
   * Returns true if {@link ComponentManager#addPrimitiveValueIfNew} adds a value that is a literal
   * of {@link Box} to the component set.
   *
   * @param literalsLevel the value for {@link GenInputsAbstract#literals_level}
   * @return true if {@link ComponentManager#addPrimitiveValueIfNew} adds the value
   */
  private static boolean addsValueThatIsALiteral(
      GenInputsAbstract.ClassLiteralsMode literalsLevel) {
    GenInputsAbstract.literals_level = literalsLevel;
    Sequence literal = Sequence.createSequenceForPrimitive(4242);
    ScopeToLiteralStatistics literals = new ScopeToLiteralStatistics();
    literals.incrementNumUses(ClassOrInterfaceType.forClass(Box.class), literal, 1);
    ComponentManager componentMgr = new ComponentManager(IS_PUBLIC);
    componentMgr.setScopeToLiteralStatistics(literals);
    componentMgr.addPrimitiveValueIfNew(4242);
    return componentMgr.getAllGeneratedSequences().contains(literal);
  }
}
