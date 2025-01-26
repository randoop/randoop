package randoop.reflection;

import static randoop.main.GenInputsAbstract.ClassLiteralsMode.CLASS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import randoop.generation.ConstantMiningWrapper;
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
  /** Map a literal sequences corresponding to each class under test. */
  private MultiMap<ClassOrInterfaceType, Sequence> literalMap;

  /** The wrapper for storing constant mining information. */
  private ConstantMiningWrapper constantMiningWrapper;

  /**
   * Creates a visitor that adds discovered literals to the given map.
   *
   * @param literalMap the map from types to sequences
   */
  ClassLiteralExtractor(MultiMap<ClassOrInterfaceType, Sequence> literalMap) {
    this.literalMap = literalMap;
  }

  /**
   * Creates a visitor that adds discovered literals to the given map and records constant mining
   * information. Only used when constant mining is enabled.
   *
   * @param constantMiningWrapper the wrapper for storing constant mining information
   */
  ClassLiteralExtractor(ConstantMiningWrapper constantMiningWrapper) {
    this.constantMiningWrapper = constantMiningWrapper;
  }

  /**
   * {@inheritDoc}
   *
   * <p>For each class, this adds a sequence that creates a value of the class type to the literal
   * map.
   *
   * <p>If constant mining is enabled, this also records the sequence information(frequency,
   * classesWithConstant).
   */
  @Override
  public void visitBefore(Class<?> c) {
    // Record the visited sequences if constant mining is enabled to avoid adding duplicate
    // sequences in the same class.
    HashSet<Sequence> occurredSequences = new HashSet<>();
    ClassOrInterfaceType constantType = ClassOrInterfaceType.forClass(c);
    ClassFileConstants.ConstantSet constantSet = ClassFileConstants.getConstants(c.getName());
    Set<NonreceiverTerm> nonreceiverTerms =
        ClassFileConstants.constantSetToNonreceiverTerms(constantSet);
    for (NonreceiverTerm term : nonreceiverTerms) {
      Sequence seq =
          new Sequence()
              .extend(
                  TypedOperation.createNonreceiverInitialization(term), new ArrayList<Variable>(0));
      if (GenInputsAbstract.constant_mining) {
        constantMiningWrapper.addFrequency(
            constantType, seq, constantSet.getConstantFrequency(term.getValue()));
        occurredSequences.add(seq);
      } else {
        literalMap.add(constantType, seq);
      }
    }
    if (GenInputsAbstract.constant_mining && GenInputsAbstract.literals_level != CLASS) {
      for (Sequence seq : occurredSequences) {
        constantMiningWrapper.addClassesWithConstant(constantType, seq, 1);
      }
      constantMiningWrapper.addTotalClasses(constantType, 1);
    }
  }
}
