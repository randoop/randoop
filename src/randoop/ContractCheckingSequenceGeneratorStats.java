package randoop;

import java.util.List;


public class ContractCheckingSequenceGeneratorStats extends SequenceGeneratorStats {
  public ContractCheckingSequenceGeneratorStats(List<StatementKind> statements,
      List<Class<?>> coverageClasses) {
    super(statements, coverageClasses);
  }

  private static final long serialVersionUID = -2475024176853398636L;


//public ContractCheckingSequenceGeneratorStats(List<StatementKind> statements) {
//super(statements);
//addStats();
//}

//private void addStats() {
//addKey(STAT_SEQUENCE_OBJECT_CONTRACT_VIOLATED_LAST_STATEMENT);
//addKey(STAT_SEQUENCE_FORBIDDEN_EXCEPTION_LAST_STATEMENT);
//}
//public enum ExecutionSummary { NORMAL, NOT_NORMAL_NO_BUG, BUG }

//public ExecutionSummary updateSequenceLevelContractStatistics(ExecutableSequence es) {
//if (es.hasNonExecutedStatements()) {
//return ExecutionSummary.NOT_NORMAL_NO_BUG;
//}

//StatementKind lastStatement = es.sequence.getLastStatement();
//int lastIndex = es.sequence.size() - 1;

//if (es.hasDecoration(ExceptionCheck.class, lastIndex)) { // XXX
//addToCount(lastStatement, STAT_SEQUENCE_FORBIDDEN_EXCEPTION_LAST_STATEMENT, 1);
//return ExecutionSummary.BUG;
//}

//if (es.hasDecoration(ExpressionEqualsVariable.class, lastIndex)) {
//addToCount(lastStatement, STAT_SEQUENCE_OBJECT_CONTRACT_VIOLATED_LAST_STATEMENT, 1);
//return ExecutionSummary.BUG;
//}

//if (es.isNormalExecution()) {
//return ExecutionSummary.NORMAL;
//}
//return ExecutionSummary.NOT_NORMAL_NO_BUG;
//}

}
