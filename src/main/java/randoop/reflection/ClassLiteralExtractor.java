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

  /** The storage for constant mining information. */
  private ScopeToConstantStatistics constantMiningStatistics;

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
   * statistics. Only used when constant mining is enabled.
   *
   * @param constantMiningStatistics the storage for constant mining information
   */
  ClassLiteralExtractor(ScopeToConstantStatistics constantMiningStatistics) {
    this(new MultiMap<>());
    this.constantMiningStatistics = constantMiningStatistics;
  }

  /**
   * {@inheritDoc}
   *
   * <p>For each class, this adds a sequence that creates a value of the class type to the literal
   * map.
   *
   * <p>If constant mining is enabled, this also records the constant statistics (numUses,
   * classesWithConstant).
   */
  @Override
  public void visitBefore(Class<?> c) {
    // Record the visited sequences if constant mining is enabled to avoid adding duplicate
    // sequences in the same class.
    Set<Sequence> allConstants = new HashSet<>();
    ClassOrInterfaceType constantType = ClassOrInterfaceType.forClass(c);
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
        constantMiningStatistics.incrementNumUses(
            constantType, seq, constantSet.getConstantFrequency(termValue));
        allConstants.add(seq);
      } else {
        literalMap.add(constantType, seq);
      }
    }
    if (GenInputsAbstract.constant_tfidf) {
      for (Sequence seq : allConstants) {
        constantMiningStatistics.incrementNumClassesWith(constantType, seq, 1);
      }
      constantMiningStatistics.incrementNumClasses(constantType, 1);
    }
  }
}
