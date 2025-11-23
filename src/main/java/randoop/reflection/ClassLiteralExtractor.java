package randoop.reflection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import randoop.generation.literaltfidf.ScopeToLiteralStatistics;
import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.util.ClassFileConstants;

/**
 * {@code ClassLiteralExtractor} is a {@link ClassVisitor} that extracts literals from the bytecode
 * of each class visited, recording constant statistics including usage frequency and the classes
 * that contain each constant.
 *
 * @see OperationModel
 */
class ClassLiteralExtractor extends DefaultClassVisitor {

  /** The storage for constant information. */
  private ScopeToLiteralStatistics scopeToLiteralStatistics;

  /**
   * Creates a visitor that records constant statistics.
   *
   * @param scopeToLiteralStatistics a map from types to sequences in them that yield a literal
   */
  ClassLiteralExtractor(ScopeToLiteralStatistics scopeToLiteralStatistics) {
    this.scopeToLiteralStatistics = scopeToLiteralStatistics;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Collects class bytecode literals and updates usage counts and per-class sequences.
   *
   * <p>This method does all the work; there is no {@code visitAfter()} method.
   */
  @Override
  public void visitBefore(Class<?> c) {
    Set<Sequence> allConstants = new HashSet<>();
    ClassOrInterfaceType containingType = ClassOrInterfaceType.forClass(c);
    ClassFileConstants.ConstantSet constantSet = ClassFileConstants.getConstants(c.getName());
    Set<NonreceiverTerm> nonreceiverTerms =
        ClassFileConstants.constantSetToNonreceiverTerms(constantSet);
    for (NonreceiverTerm term : nonreceiverTerms) {
      Sequence seq =
          new Sequence()
              .extend(
                  TypedOperation.createNonreceiverInitialization(term), new ArrayList<Variable>(0));
      Object termValue = term.getValue();
      if (termValue == null) {
        // Skip constants with null values; they are not useful as mined literals.
        continue;
      }
      scopeToLiteralStatistics.incrementNumUses(
          containingType, seq, constantSet.getConstantFrequency(termValue));
      allConstants.add(seq);
    }

    // Record class-level statistics once per class after processing all sequences
    scopeToLiteralStatistics.recordSequencesInClass(containingType, allConstants);
  }
}
