package randoop.test;

import randoop.tests.CodeFromManual;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllRandoopTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for randoop.test");

    suite.addTestSuite(SequenceTests.class);
    suite.addTestSuite(ClassComplexityCalculatorTests.class);
    suite.addTestSuite(ClassHierarchyTests.class);
    suite.addTestSuite(ReflectionTests.class);
    suite.addTestSuite(UtilTests.class);
    suite.addTestSuite(ListOfListsSelectorTest.class);
    suite.addTestSuite(SequenceTests.class);
    suite.addTestSuite(ForwardExplorerTests.class);
    suite.addTestSuite(ForwardExplorerTests2.class);
    suite.addTestSuite(DefaultReflectionFilterTests.class);
    suite.addTestSuite(ListOfListsIteratorTests.class);
    suite.addTestSuite(RandomnessTest.class);
    suite.addTestSuite(StatementCollectionWeightedTest.class);
    suite.addTestSuite(ArrayDeclarationTests.class);
    suite.addTestSuite(EqualsNotSymmetricTests.class);
    suite.addTestSuite(EqualsNotTransitiveTests.class);
    suite.addTestSuite(EqualsToNullTests.class);
    suite.addTestSuite(CompareToContractTests.class);
    suite.addTestSuite(SeedSequencesTests.class);
    suite.addTestSuite(CodeFromManual.class);

    return suite;
  }

}
