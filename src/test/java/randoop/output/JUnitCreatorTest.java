package randoop.output;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import randoop.ExecutionVisitor;
import randoop.contract.PrimValue;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.test.ObjectCheck;
import randoop.test.RegressionChecks;
import randoop.test.TestCheckGenerator;
import randoop.test.TestChecks;

/** Partial test -- disabled in build.gradle */
public class JUnitCreatorTest {

  @Test
  public void testClassCreation() {
    List<String> afterAll = new ArrayList<>();
    afterAll.add("System.out.println(\"after all\");");
    List<String> afterEach = new ArrayList<>();
    afterEach.add("System.out.println(\"after each\");");
    List<String> beforeAll = new ArrayList<>();
    beforeAll.add("System.out.println(\"before all\");");
    List<String> beforeEach = new ArrayList<>();
    beforeEach.add("System.out.println(\"before each\");");
    JUnitCreator creator =
        JUnitCreator.getTestCreator(
            "pkg", "testMethod", beforeAll, afterAll, beforeEach, afterEach);

    List<ExecutableSequence> sequences = new ArrayList<>();

    ExecutionVisitor visitor =
        new ExecutionVisitor() {
          @Override
          public void visitBeforeStatement(ExecutableSequence sequence, int i) {}

          @Override
          public void visitAfterStatement(ExecutableSequence sequence, int i) {}

          @Override
          public void initialize(ExecutableSequence executableSequence) {}

          @Override
          public void visitAfterSequence(ExecutableSequence executableSequence) {}
        };

    for (int i = 0; i < 5; i++) {
      ExecutableSequence sequence = new ExecutableSequence(Sequence.createSequenceForPrimitive(i));
      TestCheckGenerator checkGen = getTestCheckGenerator(i, sequence.sequence.getVariable(0));

      sequence.execute(visitor, checkGen);
      sequences.add(sequence);
    }
    System.out.println(creator.createTestClass("TestClass", sequences));
  }

  private TestCheckGenerator getTestCheckGenerator(final int i, final Variable variable) {
    return new TestCheckGenerator() {
      @Override
      public TestChecks visit(ExecutableSequence s) {
        TestChecks checks = new RegressionChecks();
        checks.add(new ObjectCheck(new PrimValue(i, PrimValue.PrintMode.EQUALSEQUALS), variable));
        return checks;
      }
    };
  }
}
