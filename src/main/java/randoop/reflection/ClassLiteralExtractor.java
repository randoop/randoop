package randoop.reflection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
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
 * that contain each constant. All extracted literals are stored in {@link ScopeToLiteralStatistics}
 * to support both TF-IDF and non-TF-IDF literal selection strategies.
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
   * <p>For each class, add to the literal map a sequence for each literal that the class uses.
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
      @SuppressWarnings("nullness:assignment") // TODO: how do we know the term value is non-null?
      @NonNull Object termValue = term.getValue();
      scopeToLiteralStatistics.incrementNumUses(
          containingType, seq, constantSet.getConstantFrequency(termValue));
      allConstants.add(seq);
    }

    // Record class-level statistics once per class after processing all sequences
    scopeToLiteralStatistics.recordSequencesInClass(containingType, allConstants);
  }
}
