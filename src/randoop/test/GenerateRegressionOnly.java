package randoop.test;

import randoop.sequence.ExecutableSequence;

/**
 * A TestCheckGenerator that generates only regression tests for sequences. 
 * Filters any sequences for which the the {@link ContractCheckingVisitor} finds
 * any failures.
 */
public class GenerateRegressionOnly implements TestCheckGenerator {

  private ContractCheckingVisitor contractVisitor;
  private RegressionCaptureVisitor regressionVisitor;
  
  /**
   * Creates a check generator that generates {@code RegressionChecks} for 
   * {@code ExecutableSequence} objects that do not have failures according to
   * the given {@code ContractCheckingVisitor}.
   * 
   * @param contractVisitor  the visitor to identify failures in the sequence
   * @param regressionVisitor  the visitor to identify regression checks for the sequence
   */
  public GenerateRegressionOnly(ContractCheckingVisitor contractVisitor, RegressionCaptureVisitor regressionVisitor) {
    this.contractVisitor = contractVisitor;
    this.regressionVisitor = regressionVisitor;
  }

  /**
   * {@inheritDoc}
   * @return a (possibly empty) set of regression checks for the current state of the sequence 
   */
  @Override
  public TestChecks visit(ExecutableSequence s) {
    TestChecks failChecks = contractVisitor.visit(s);
    if (failChecks.hasFailure()) {
      return new RegressionChecks();
    } else {
      return regressionVisitor.visit(s);
    }
  }

}
