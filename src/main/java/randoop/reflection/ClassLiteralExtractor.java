package randoop.reflection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import randoop.generation.constanttfidf.ScopeToConstantStatistics;
import randoop.main.GenInputsAbstract;
import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.util.ClassFileConstants;
import randoop.util.MultiMap;

/**
 * {@code ClassLiteralExtractor} is a {@link ClassVisitor} that extracts literals from the bytecode
 * of each class visited, adding a sequence for each to a map associating a sequence with a type.
 *
 * @see OperationModel
 */
class ClassLiteralExtractor extends DefaultClassVisitor {

  /** Map from a class under test to the literal sequences that appear in it. */
  private MultiMap<ClassOrInterfaceType, Sequence> literalMap;

  /** The storage for constant information. */
  private ScopeToConstantStatistics scopeToConstantStatistics;

  /**
   * Creates a visitor that adds discovered literals to the given map.
   *
   * @param literalMap a map from types to sequences in them that yield a constant
   */
  ClassLiteralExtractor(MultiMap<ClassOrInterfaceType, Sequence> literalMap) {
    this.literalMap = literalMap;
  }

  /**
   * Creates a visitor that adds discovered literals to the given map and records constant
   * statistics. Only used when constant-tfidf is enabled.
   *
   * @param scopeToConstantStatistics the storage for constant information
   */
  ClassLiteralExtractor(ScopeToConstantStatistics scopeToConstantStatistics) {
    this(new MultiMap<>());
    this.scopeToConstantStatistics = scopeToConstantStatistics;
  }

  /**
   * {@inheritDoc}
   *
   * <p>For each class, add to the literal map a sequence for each constant that the class uses.
   *
   * <p>If constant-tfidf is enabled, this also records the constant statistics (numUses,
   * classesWithConstant).
   */
  @Override
  public void visitBefore(Class<?> c) {
    // Record the visited sequences if constant-tfidf is enabled to avoid adding duplicate
    // sequences in the same class.
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
      if (GenInputsAbstract.constant_tfidf) {
        @SuppressWarnings("nullness:assignment") // TODO: how do we know the term value is non-null?
        @NonNull Object termValue = term.getValue();
        scopeToConstantStatistics.incrementNumUses(
            containingType, seq, constantSet.getConstantFrequency(termValue));
        allConstants.add(seq);
      } else {
        literalMap.add(containingType, seq);
      }
    }

    // Record class-level statistics once per class after processing all sequences
    if (GenInputsAbstract.constant_tfidf && !allConstants.isEmpty()) {
      scopeToConstantStatistics.incrementClassesWithSequences(containingType, allConstants);
    }
  }
}
