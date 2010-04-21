package randoop.experiments;

import java.lang.reflect.Member;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.ExecutableSequence;
import randoop.ForwardGenerator;
import randoop.Globals;
import randoop.LineRemover;
import randoop.StatementKind;




public class ResultsWriter {

  public static String faultsSummary(long explorationTime,
      ForwardGenerator explorer,
      List<StatementKind> model) {

    Date today = new Date();
    // Maps fault type --> violating methods
    Map<String, Set<Member>> methodsThatExhibitFault
    = new LinkedHashMap<String, Set<Member>>();

    // Violating methods that violate randoop.contract.SequenceThrowsException
    Set<Member> methodsSequenceThrowsException = new LinkedHashSet<Member>();
    // Violating methods that violate other than randoop.contract.SequenceThrowsException
    Set<Member> methodsObjectContractViolation = new LinkedHashSet<Member>();

    Set<Member> allFaultyMethods = new LinkedHashSet<Member>();
    Set<Class<?>> allFaultyClasses = new LinkedHashSet<Class<?>>();
    processfaults(explorer.stats.outSeqs,
        methodsThatExhibitFault,
        allFaultyMethods,
        allFaultyClasses,
        methodsObjectContractViolation,
        methodsSequenceThrowsException);
    int Sequencecrashnpe = 0;
    int tostringcrash = 0;
    int hashcodecrash = 0;
    int eqeqfalse = 0;
    for (Map.Entry<String, Set<Member>> e : methodsThatExhibitFault.entrySet()) {
      String contract = e.getKey();
      if (contract.equals("randoop.contract.InstanceMethodThrowsException")) {
        Sequencecrashnpe = e.getValue().size();
      } else if (contract.equals("randoop.contract.unary.ToStringThrowsException")) {
        tostringcrash = e.getValue().size();
      } else if (contract.equals("randoop.contract.unary.HashcodeThrowsException")) {
        hashcodecrash = e.getValue().size();
      } else if (contract.equals("randoop.contract.unary.EqualsNotReflexive")) {
        eqeqfalse = e.getValue().size();
      } else {
        throw new RuntimeException(contract);
      }
    }
    // int distinctobjectscreated = explorer.getObjectCache().size();

    // HACK HACK HACK I'm hardcoding the contract violations that we'll use for the paper.
    // Now we'll output a single line of a latex table, with the following columns:
    //
    // exploration testtime
    // classes exercised
    // methods exercised
    // inputs generated
    // faulty classes
    // faulty methods
    // methods that are faulty because they throw an NPE
    // methods that are faulty because they create an object that crashes when hashCode() called
    // methods that are faulty because they create an object that crashes when toString() called
    // methods that are faulty because they create an object s.t. o.equals(o) returns false
    // Non-minimized fault-revealing input average size
    // Minimized fault-revealing input average size
    // Total number of well-formed distinct objects created (w.r.t. equals method)
    StringBuilder summary = new StringBuilder();
    summary.append("# EXPERIMENT DATA ");
    summary.append("# (date randoop.experiments ran: " + today.toString() + ")" + Globals.lineSep + "");
    summary.append("# ---------------------------------------------------------" + Globals.lineSep + "");
    summary.append(Globals.lineSep);

    summary.append("# exploration testtime" + Globals.lineSep + "");
    summary.append("explorationtime=" + explorationTime + Globals.lineSep);

    summary.append("# testclasses cases" + Globals.lineSep + "");
    summary.append("testcases=" + explorer.stats.outSeqs.size() + Globals.lineSep);

    summary.append("# faulty methods" + Globals.lineSep + "");
    summary.append("faultymethods=" + allFaultyMethods.size() + Globals.lineSep);

    summary.append("# faulty classes" + Globals.lineSep + "");
    summary.append("faultyclasses=" + allFaultyClasses.size() + Globals.lineSep);

    summary.append("# methods that are faulty because they throw an exception" + Globals.lineSep + "");
    summary.append("exceptioncontractviolations=" + methodsSequenceThrowsException.size() + Globals.lineSep);


    summary.append("# methods that are faulty because they throw an NPE" + Globals.lineSep + "");
    summary.append("npecontractviolations=" + Sequencecrashnpe + Globals.lineSep);

    summary.append("# methods that are faulty because they create an object that violates an object contract" + Globals.lineSep + "");
    summary.append("objectcontractviolations=" + methodsObjectContractViolation.size() + Globals.lineSep);

    summary.append("# methods that are faulty because they create an object that crashes when hashCode() called" + Globals.lineSep + "");
    summary.append("hashcodecontractviolations=" + hashcodecrash + Globals.lineSep);

    summary.append("# methods that are faulty because they create an object that crashes when toString() called" + Globals.lineSep + "");
    summary.append("tostringcontractviolations=" + tostringcrash + Globals.lineSep);

    summary.append("# methods that are faulty because they create an object s.t. o.equals(o) returns false" + Globals.lineSep + "");
    summary.append("eqoocontractviolations=" + eqeqfalse + Globals.lineSep);

    summary.append("# Non-minimized fault-revealing input average size" + Globals.lineSep + "");
    summary.append("nonminimizedinputsize=" + LineRemover.averageOriginalSize() + Globals.lineSep);

    summary.append("# Minimized fault-revealing input average size" + Globals.lineSep + "");
    summary.append("minimizedinputsize=" + LineRemover.averageMinimizedSize() + Globals.lineSep);

    return summary.toString();
  }

  private static void processfaults(List<ExecutableSequence> oneSequenceFaults, Map<String, Set<Member>> methodsThatExhibitFault,
      Set<Member> allFaultyMethods, Set<Class<?>> allFaultyClasses, Set<Member> methodsObjectContractViolation,
      Set<Member> methodsSequenceThrowsException) {

    for (ExecutableSequence s : oneSequenceFaults) {

      throw new RuntimeException("TODO");
//    Member method = null;
//    Statement t = s.sequence.getLastStatement();
//    if (t instanceof RMethod) {
//    method = ((RMethod)t).getMethod();
//    } else {
//    assert t instanceof RConstructor);
//    method = ((RConstructor)t).getConstructor();
//    }
//    allFaultyMethods.add(method);
//    allFaultyClasses.add(method.getDeclaringClass());

//    List<Observation> decorations = s.getDecorations(s.sequence.size() - 1);

//    ContractFailureDecoration firstDecoration = decorations.get(0);
//    String behaviorClassName = firstDecoration.getClass().getName();

//    if (firstDecoration instanceof ForbiddenException) {
//    behaviorClassName = firstDecoration.getClass().getName();
//    methodsSequenceThrowsException.add(method);
//    } else {
//    assert firstDecoration instanceof ObjectContractViolation);
//    behaviorClassName = ((ObjectContractViolation)firstDecoration).getViolatedContract().getClass().getName();
//    methodsObjectContractViolation.add(method);
//    }

//    Set<Member> methodsForThisFault = methodsThatExhibitFault.get(behaviorClassName);
//    if (methodsForThisFault == null) {
//    methodsForThisFault = new LinkedHashSet<Member>();
//    methodsThatExhibitFault.put(behaviorClassName, methodsForThisFault);
//    }
//    methodsForThisFault.add(method);



    }
  }

}
