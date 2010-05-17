package randoop.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllRandoopTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for randoop.test");

    // This test runs before others because it's a
    // performance test, and is more likely to pass
    // on a fresh JVM.
    //suite.addTestSuite(ISSTA06ContainersTest.class);

    suite.addTestSuite(SequenceTests.class);
    suite.addTestSuite(ClassComplexityCalculatorTests.class);
    suite.addTestSuite(ClassHierarchyTests.class);
    suite.addTestSuite(Test_AllPass.class);
    suite.addTestSuite(ReflectionTests.class);
    suite.addTestSuite(UtilTests.class);
    suite.addTestSuite(ListOfListsSelectorTest.class);
    suite.addTestSuite(SequenceTests.class);
    suite.addTestSuite(ForwardExplorerTests.class);
    suite.addTestSuite(ForwardExplorerTests2.class);
    suite.addTestSuite(DefaultReflectionFilterTests.class);
    suite.addTestSuite(HeapLinearizerTests.class);
    suite.addTestSuite(ListOfListsIteratorTests.class);
    suite.addTestSuite(RandomnessTest.class);
    suite.addTestSuite(StatementCollectionWeightedTest.class);
    suite.addTestSuite(ArrayDeclarationTests.class);
    suite.addTestSuite(EqualsNotSymmetricTests.class);
    suite.addTestSuite(EqualsNotTransitiveTests.class);
    suite.addTestSuite(EqualsToNullTests.class);
    suite.addTestSuite(CompareToContractTests.class);
    suite.addTestSuite(LineRemoverTests2.class);
    suite.addTestSuite(DependencyTests.class);
    suite.addTestSuite(SeedSequencesTests.class);

    return suite;
  }

}
