package randoop.experiments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.DummyVisitor;
import randoop.ExecutableSequence;
import randoop.ExecutionOutcome;
import randoop.Sequence;
import randoop.util.Files;
import randoop.util.Reflection;
import cov.Branch;
import cov.Coverage;
import cov.CoverageAtom;

public class Execute {
  
  public static void main(String[] args) {
    
    File covClassesFile = new File(args[0]);
    List<Class<?>> covClasses = new ArrayList<Class<?>>();
    List<String> covClassNames = null;
    try {
      covClassNames = Files.readWhole(covClassesFile);
    } catch (IOException e) {
      throw new Error(e);
    }
    for (String className : covClassNames) {
      Class<?> cls = Reflection.classForName(className);
      System.out.println(cls.toString() + " " + Coverage.getBranches(cls).size());
      covClasses.add(cls);
    }
    
    DataFlowInput dfin = DataFlowInput.parse(args[1]);
    
    for (Map.Entry<Branch, Set<Sequence>> e : dfin.frontierMap.entrySet()) {
      assert e.getValue().size() == 1;
      
      Sequence s = e.getValue().toArray(new Sequence[0])[0];
      ExecutableSequence eseq = new ExecutableSequence(s);
      
      Set<CoverageAtom> coveredBranches = new LinkedHashSet<CoverageAtom>();
      Coverage.clearCoverage(covClasses);
      
      eseq.execute(new DummyVisitor());
      
      for (ExecutionOutcome o : eseq.getAllResults()) {
        System.out.println(">" + o);
      }
      
      for (Class<?> cls : covClasses) {
        System.out.println("@" + cls + " " + Coverage.getCoveredAtoms(cls));
      }
      
      coveredBranches = Coverage.getCoveredAtoms(covClasses);
      
      
      if (coveredBranches.contains(e.getKey())) {
        System.out.println("COVERED BRANCH "+ e.getKey());
      } else {
        System.out.println("DID NOT COVER BRANCH "+ e.getKey());
        System.out.println("Covered branches:");
        for (CoverageAtom a : coveredBranches) {
          System.out.println(a);
        }
      }
    }
  }
  }
