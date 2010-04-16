package randoop.experiments;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.ForwardGenerator;
import randoop.RConstructor;
import randoop.RMethod;
import randoop.SeedSequences;
import randoop.SequenceCollection;
import randoop.StatementKind;
import randoop.main.GenInputsAbstract;
import randoop.util.Reflection;
import randoop.util.ReflectionExecutor;

public class JCuteDataStructuresExp {

  public static void main(String[] args) throws IOException {

    List<Member> methodsToTest =
      Reflection.loadMethodsAndCtorsFromFile(new File(args[0]));

    Set<StatementKind> statements = new LinkedHashSet<StatementKind>();
    for (Member m : methodsToTest) {
      statements.add(statementFor(m));
    }

    List<Member> coverageMethods =
      Reflection.loadMethodsAndCtorsFromFile(new File(args[1]));

    List<StatementKind> model = new ArrayList<StatementKind>(statements);

    ForwardGenerator explorer = new ForwardGenerator(
        model, null, 120 /*seconds*/ * 1000, Integer.MAX_VALUE,
        new SequenceCollection(SeedSequences.objectsToSeeds(SeedSequences.primitiveSeeds)));

    GenInputsAbstract.forbid_null = false;
    ForwardGenerator.print_stats = true;
    ReflectionExecutor.usethreads = false;

    // Generate inputs.
    explorer.explore();

    // Print branch coverage.
    System.out.println("BRANCH COVERAGE:");
    throw new RuntimeException("todo");
    //DecimalFormat format = new DecimalFormat("#.###");

    //System.out.println("HITS:" + hits + ",TOTAL:" + total + "RATIO:" + hits/(double)total);

//  int maxCovAllMethods = 0;
//  int covAllMethods = 0;

//  for (Member m : coverageMethods) {
//  int max = 0;
//  int covered = 0;
//  Set<CoverageAtom> branchesTot = covInfo.getBranches(m);
//  max = branchesTot.size();
//  for (CoverageAtom a : branchesTot) {
//  if (explorer.branchesCovered.contains(a)) {
//  covered++;
//  }
//  }
//  maxCovAllMethods += max;
//  covAllMethods += covered;
//  System.out.println(m.toString());
//  System.out.println("max=" + max);
//  System.out.println("cov=" + covered);
//  }

//  System.out.println("MAX:" + maxCovAllMethods + ",COV:" + covAllMethods + ",RATIO:" + covAllMethods/(double)maxCovAllMethods);
  }

  private static StatementKind statementFor(Member m) {
    if (m instanceof Method) {
      return RMethod.getRMethod((Method)m);
    } else {
      assert m instanceof Constructor;
      return RConstructor.getRConstructor((Constructor<?>)m);
    }
  }
}
