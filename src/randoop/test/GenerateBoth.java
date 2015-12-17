package randoop.test;

import randoop.sequence.ExecutableSequence;

/**
 * A {@code TestCheckGenerator} that returns {@code TestChecks} for both 
 * error-revealing tests and regression tests.
 * Returns a {@code RegressionChecks} object if the {@code ContractCheckingVisitor}
 * returns no failures, otherwise returns that {@code ErrorRevealingChecks} object.  
 */
public class GenerateBoth implements TestCheckGenerator {
  
  private ContractCheckingVisitor contractVisitor;
  private RegressionCaptureVisitor regressionVisitor;

  /**
   * Creates a check generator that generates {@code TestChecks} for 
   * {@code ExecutableSequence} objects using the given {@code ContractCheckingVisitor}
   * and {@code RegressionCaptureVisitor} objects. Returns {@code RegressionChecks}
   * unless the contract checker returns {@code ErrorRevealingChecks}.
   * 
   * @param contractVisitor  the visitor to identify failures in the sequence
   * @param regressionVisitor  the visitor to identify regression checks for the sequence
   */
  public GenerateBoth(ContractCheckingVisitor contractVisitor, RegressionCaptureVisitor regressionVisitor) {
    this.contractVisitor = contractVisitor;
    this.regressionVisitor = regressionVisitor;
  }

  /**
   * {@inheritDoc}
   * Returns checks for both error-revealing and regression tests.
   * 
   * @return a {@code RegressionChecks} object if the sequence has no failures,
   * and a {@code ErrorRevealingChecks} object otherwise 
   */
  @Override
  public TestChecks visit(ExecutableSequence s) {
    TestChecks failChecks = contractVisitor.visit(s);
    if (failChecks.hasFailure()) {
      return failChecks;
    } else {
      return regressionVisitor.visit(s);
    }
  }

}
