package randoop.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import randoop.ContractCheckingVisitor;
import randoop.EqualsReflexive;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.ObjectContract;
import randoop.ReplayVisitor;
import randoop.operation.ConstructorCall;
import randoop.operation.MethodCall;
import randoop.operation.Operation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceParseException;

import junit.framework.TestCase;

public class CodeFromManual extends TestCase {

  public static void test() throws SecurityException, NoSuchMethodException, SequenceParseException {

    Operation newLL = ConstructorCall.getConstructorCall(LinkedList.class.getConstructor());
    Operation newOb = ConstructorCall.getConstructorCall(Object.class.getConstructor());
    Operation addFist = MethodCall.getMethodCall(LinkedList.class.getMethod("addFirst", Object.class));
    Operation size = MethodCall.getMethodCall(LinkedList.class.getMethod("size"));
    Operation newTS = ConstructorCall.getConstructorCall(TreeSet.class.getConstructor(Collection.class));
    Operation syncS = MethodCall.getMethodCall(Collections.class.getMethod("synchronizedSet", Set.class));

    Sequence s = new Sequence();
    s = s.extend(newLL);
    s = s.extend(newOb);
    s = s.extend(addFist, s.getVariable(0), s.getVariable(1));
    s = s.extend(size,    s.getVariable(0));
    s = s.extend(newTS,   s.getVariable(0));
    s = s.extend(syncS,   s.getVariable(4));

    List<ObjectContract> contracts = new ArrayList<ObjectContract>();
    contracts.add(new EqualsReflexive());
    ContractCheckingVisitor cvisitor = new ContractCheckingVisitor(contracts, true);
    
    {
      ExecutableSequence es = new ExecutableSequence(s);
      es.execute(null);
      ExecutionOutcome resultAt3 = es.getResult(3);
      if (resultAt3 instanceof NormalExecution) {
        System.out.println(((NormalExecution)resultAt3).getRuntimeValue());
      }
    }
    
    ExecutableSequence es = new ExecutableSequence(s);
    es.execute(cvisitor);

    System.out.println(es.hasFailure());
    
    ReplayVisitor replayVisitor = new ReplayVisitor();
    es.execute(replayVisitor);
    
    System.out.println();
    
    System.out.println(es.toCodeString());
    
    System.out.println();

    System.out.println(s.toCodeString());
    
    System.out.println();

    String parseable = s.toParseableString();
    
    System.out.println();
    
    System.out.println(parseable);

    Sequence.parse(parseable);
  }
}
