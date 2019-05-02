package randoop.output;

/** Partial test -- disabled in build.gradle */
public class JUnitCreatorTest {
  /*
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
          JUnitCreator.getTestCreator("pkg", beforeAll, afterAll, beforeEach, afterEach);

      List<ExecutableSequence> sequences = getExecutableSequences();
      System.out.println(creator.createTestClass("TestClass", "testMethod", sequences));
    }

    @Test
    public void testBadFixtureInput() {
      List<String> afterAll = new ArrayList<>();
      afterAll.add(")");
      List<String> afterEach = new ArrayList<>();
      afterEach.add("System.out.println(\"after each\");");
      List<String> beforeAll = new ArrayList<>();
      beforeAll.add("System.out.println(\"before all\");");
      List<String> beforeEach = new ArrayList<>();
      beforeEach.add("System.out.println(\"before each\");");
      JUnitCreator creator =
          JUnitCreator.getTestCreator("pkg", beforeAll, afterAll, beforeEach, afterEach);

      List<ExecutableSequence> sequences = getExecutableSequences();
      System.out.println(creator.createTestClass("TestClass", "testMethod", sequences));
    }
  private List<ExecutableSequence> getExecutableSequences() {
    List<ExecutableSequence> sequences = new ArrayList<>();
    ExecutionVisitor visitor = getExecutionVisitor();
    for (int i = 0; i < 5; i++) {
      ExecutableSequence sequence = new ExecutableSequence(Sequence.createSequenceForPrimitive(i));
      TestCheckGenerator checkGen = getTestCheckGenerator(i, sequence.sequence.getVariable(0));

      sequence.execute(visitor, checkGen);
      sequences.add(sequence);
    }
    return sequences;
  }

  private ExecutionVisitor getExecutionVisitor() {
    return new ExecutionVisitor() {
      @Override
      public void visitBeforeStatement(ExecutableSequence sequence, int i) {}

      @Override
      public void visitAfterStatement(ExecutableSequence sequence, int i) {}

      @Override
      public void initialize(ExecutableSequence executableSequence) {}

      @Override
      public void visitAfterSequence(ExecutableSequence executableSequence) {}
    };
  }

  private TestCheckGenerator getTestCheckGenerator(final int i, final Variable variable) {
    return new TestCheckGenerator() {
      @Override
      public RegressionChecks generateTestChecks(ExecutableSequence eseq) {
        return new RegressionChecks(
            new ObjectCheck(new PrimValue(i, PrimValue.PrintMode.EQUALSEQUALS), variable));
      }
    };
  }
  */

}
