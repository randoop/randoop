package randoop.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.util.Randomness;
import randoop.util.SimpleList;

/**
 * Implements the Orienteering component, as described by the paper "GRT: Program-Analysis-Guided
 * Random Testing" by Ma et. al (appears in ASE 2015):
 * https://people.kth.se/~artho/papers/lei-ase2015.pdf .
 *
 * <p>Biases input selection towards sequences that have lower execution cost. Execution cost is
 * measured by the number of method calls in a sequence and the time it takes to execute.
 *
 * <p>Our implementation of Orienteering differs from that described in the GRT paper in that we do
 * not keep track of the execution time of an input sequence for each new run. Instead, we assume
 * that an input sequence's execution time is equal to the execution time of its first run. We
 * believe this assumption is reasonable since a sequence does not take any inputs (it is
 * self-contained), so its execution time probably does not differ greatly between separate runs.
 *
 * <p>The GRT paper also does not describe how to handle input sequences that have an execution time
 * of zero, such as one that only includes the assignment of a primitive type {@code byte byte0 =
 * (byte)1;}. We assign these input sequences an execution time of 1 nanosecond to prevent division
 * by zero when later computing weights.
 */
public class OrienteeringSelection implements InputSequenceSelector {
  /** Map from a sequence to its weight. */
  private final Map<Sequence, Double> weightMap = new HashMap<>();

  /** Map from a sequence to the number of times it was selected. */
  private final Map<Sequence, Integer> sequenceSelectionCount = new HashMap<>();

  /**
   * Cache for the square root of the number of method calls in a sequence of statements. Once
   * computed for a given sequence, the value is never updated.
   */
  private final Map<Sequence, Double> sequenceMethodSizeSqrt = new HashMap<>();

  /**
   * Map from a sequence to its execution time in nanoseconds. Once computed for a given sequence,
   * the execution time of an input sequence is never updated.
   */
  private final Map<Sequence, Long> sequenceExecutionTime = new HashMap<>();

  /**
   * Bias input selection towards lower-cost sequences. We first compute and update the weights of
   * all the candidates within the candidate list before making our selection.
   *
   * @param candidates sequences to choose from
   * @return the chosen sequence
   */
  @Override
  public Sequence selectInputSequence(SimpleList<Sequence> candidates) {
    double totalWeight = computeWeightForCandidates(candidates);

    Sequence selectedSequence = Randomness.randomMemberWeighted(candidates, weightMap, totalWeight);
    incrementCountInMap(sequenceSelectionCount, selectedSequence);
    return selectedSequence;
  }

  /**
   * Compute the weights of the candidates in the given list that have not been assigned a weight.
   *
   * @param candidates list of candidate sequences
   * @return the total weight of all the elements in the candidate list
   */
  private double computeWeightForCandidates(SimpleList<Sequence> candidates) {
    double totalWeight = 0.0;

    // Iterate through the candidate list, computing the weight for a sequence only if it has
    // not yet been computed before.
    for (int i = 0; i < candidates.size(); i++) {
      Sequence candidate = candidates.get(i);

      Double weight = weightMap.get(candidate);
      if (weight == null) {
        weight = computeWeightForCandidate(candidate);
      }

      totalWeight += weight;
    }

    return totalWeight;
  }

  /**
   * Compute the weight of an input sequence. The formula for updating a sequence's weight is:
   *
   * <p>1.0 / (k * seq.exec_time * sqrt(seq.meth_size))
   *
   * <p>Where k is the number of selections of seq and exec_time is the execution time of seq and
   * meth_size is the number of method call statements in seq. This formula is a slight
   * simplification of the one described in the GRT paper which maintains a separate exec_time for
   * each execution of seq. However, we assume that every execution times for a sequence is the same
   * as the first execution.
   *
   * @param sequence the sequence to compute a weight for
   * @return the computed weight for the given sequence
   */
  private double computeWeightForCandidate(Sequence sequence) {
    double methodSizeSqrt = getMethodSizeSquareRootForSequence(sequence);
    Integer selectionCount = sequenceSelectionCount.get(sequence);
    if (selectionCount == null) {
      selectionCount = 1;
    }

    Long executionTime = sequenceExecutionTime.get(sequence);
    if (executionTime == null) {
      executionTime = 1L;
    }

    double weight = 1.0 / (selectionCount * executionTime * methodSizeSqrt);
    weightMap.put(sequence, weight);

    return weight;
  }

  /**
   * Each {@link Sequence} within {@code inputSequences} is a subsequence of {@code eSeq}. At this
   * point, the given {@link ExecutableSequence} has been executed and contains the execution time
   * of the sequence as a whole. The ExecutableSequence, eSeq, contains an {@link
   * randoop.sequence.Execution} which contains a list of {@link ExecutionOutcome}s. The execution
   * outcome object represents the result of executing a single statement in a sequence. To compute
   * the execution time of an input sequence, we iterate through its statements and retrieve their
   * respective execution times from the eSeq object.
   *
   * <p>If the statement executed normally but has a measured time of 0 nanoseconds, we assign it an
   * execution time of 1 nanosecond. This is to prevent division by zero when computing weights
   * which uses the statement's execution time as part of a product in the denominator. We've
   * observed that statements that have a measured execution time of zero typically include
   * assignment statements of primitive types with a constant value. For example: {@code byte byte0
   * = (byte)1;}.
   *
   * <p>We do not update the execution time of an input sequence once it has been assigned. This is
   * because we do not believe that a single input sequence's execution time will change drastically
   * between different runs. This is a simplification upon GRT's description of Orienteering which
   * does differentiate execution times of a given sequence between multiple runs.
   *
   * @param inputSequences the sequences that were chosen as the input to the method under test for
   *     creating {@code eSeq} which is a new and unique sequence
   * @param eSeq the recently executed sequence which is new and unique
   */
  @Override
  public void createdExecutableSequenceFromInputs(
      List<Sequence> inputSequences, ExecutableSequence eSeq) {
    Map<Statement, Long> statementExecTimeMap = new HashMap<>();

    // We iterate through the executable sequence and populate our map, mapping from statement to
    // execution time. There will be an execution outcome for each statement since we have
    // the invariant in ExecutableSequence that sequence.size() == executionResults.size().
    for (int i = 0; i < eSeq.size(); i++) {
      Statement statement = eSeq.sequence.getStatement(i);
      ExecutionOutcome executionOutcome = eSeq.getResult(i);

      if (executionOutcome instanceof NormalExecution) {
        Long statementExecTime = executionOutcome.getExecutionTime();
        // If the statement executed normally but has a measured time of 0 nanoseconds, we assign it
        // an execution time of 1 nanosecond. This is to prevent division by zero when computing
        // weights which uses the statement's execution time as part of a product in the
        // denominator.
        if (statementExecTime == 0) {
          statementExecTime = 1L;
        }
        statementExecTimeMap.put(statement, statementExecTime);
      } else {
        // If the statement did not execute normally, we assign it a measured time of -1
        // nanoseconds.  We will check for this below and subsequently skip computing the execution
        // time of any sequence that contains a statement that did not execute normally.
        statementExecTimeMap.put(statement, -1L);
      }
    }

    // We compute the execution time of each input sequence for the given eSeq.
    for (Sequence inputSequence : inputSequences) {
      Long executionTime = sequenceExecutionTime.get(inputSequence);

      // If we have not yet computed an execution time for this input sequence, we do so now.
      // Otherwise, we continue to the next statement since we only ever compute an input sequence's
      // execution time once.
      if (executionTime == null) {
        boolean sequenceExecutedNormally = true;
        Long sequenceExecTime = 0L;

        // An input sequence's execution time is equal to the total sum of the execution times of
        // the statements that constitute the sequence itself.
        for (int i = 0; i < inputSequence.size(); i++) {
          Statement statement = inputSequence.getStatement(i);
          Long statementExecTime = statementExecTimeMap.get(statement);

          // Since each input sequence is a subsequence of the overall executable sequence, we
          // expect every statement to exist within the executable sequence. We can then use our map
          // from statement to execution time to add onto our running execution time sum.
          assert statementExecTime != null;

          // If this statement has a negative execution time, we know that it did not execute
          // normally. We therefore do not compute the execution time of the input sequence as a
          // whole.
          if (statementExecTime < 0) {
            sequenceExecutedNormally = false;
            break;
          }
          sequenceExecTime += statementExecTimeMap.get(statement);
        }

        // We only assign an input sequence the computed sequence execution time if the sequence as
        // a whole executed normally - meaning all of its statements also executed normally. If this
        // is not the case, we will skip this input sequence for now and perhaps assign it an
        // execution time in the future.
        if (sequenceExecutedNormally) {
          // A sequence will have a positive execution time if all of the statements within it
          // executed normally.
          assert sequenceExecTime > 0;
          sequenceExecutionTime.put(inputSequence, sequenceExecTime);
        }
      }

      // Update the weight of the input sequence.
      computeWeightForCandidate(inputSequence);
    }
  }

  /**
   * Increments the value mapped to by the key in the given map. If the map does not contain the
   * value, the value is set to 1 in the map.
   *
   * @param map input map
   * @param key given key
   * @param <K> any reference type
   */
  private <K> void incrementCountInMap(Map<K, Integer> map, K key) {
    Integer count = map.get(key);
    if (count == null) {
      count = 0;
    }
    map.put(key, count + 1);
  }

  /**
   * Retrieve the method size square root of the given sequence. This is the square root of the
   * number of method call statements within the given sequence.
   *
   * <p>The method size square root value will be used in a product in the denominator of a
   * division. To prevent division by zero, we assign the method size square root a value of 1 if it
   * 0. This could happen for sequences with no method calls.
   *
   * @param sequence the sequence to get the method size square root of
   * @return square root of the number of method calls in the given sequence
   */
  private double getMethodSizeSquareRootForSequence(Sequence sequence) {
    // If we haven't computed the square root of the method size of this sequence,
    // compute it and permanently store it.
    Double methodSizeSqrt = sequenceMethodSizeSqrt.get(sequence);
    if (methodSizeSqrt == null) {
      methodSizeSqrt = Math.sqrt(sequence.numMethodCalls());

      // Check for special case where a sequence can have zero method calls and assign it a
      // method size square root value of 1.
      if (methodSizeSqrt == 0) {
        methodSizeSqrt = 1.0;
      }

      sequenceMethodSizeSqrt.put(sequence, methodSizeSqrt);
    }

    return methodSizeSqrt;
  }
}
