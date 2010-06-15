package randoop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import junit.framework.TestCase;
import randoop.Check;
import randoop.CheckRepContract;
import randoop.EqualsHashcode;
import randoop.EqualsReflexive;
import randoop.EqualsSymmetric;
import randoop.EqualsToNullRetFalse;
import randoop.ExceptionalExecution;
import randoop.ExecutableSequence;
import randoop.ExecutionVisitor;
import randoop.ForwardGenerator;
import randoop.JunitFileWriter;
import randoop.NoExceptionCheck;
import randoop.ObjectCheck;
import randoop.ObjectContract;
import randoop.RegressionCaptureVisitor;
import randoop.SeedSequences;
import randoop.SequenceCollection;
import randoop.StatementKind;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.util.DefaultReflectionFilter;
import randoop.util.Log;
import randoop.util.MultiMap;
import randoop.util.PrimitiveTypes;
import randoop.util.Randomness;
import randoop.util.Reflection;
import randoop.util.ReflectionExecutor;

// EXPERIMENTAL CODE.
public abstract class AbstractRandoopTest extends TestCase {

  public abstract Collection<Class<?>> getClassesUnderTest();

  public abstract String getProjectHome();

  public enum Mode {
    REGRESSION, ERRORS
  }

  public abstract Mode getMode();
  
  public int getInputLimit() {
    return 10000;
  }
  
  public int getTimeLimitSeconds() {
    return 10;
  }

  public void test() throws Throwable {

    Randomness.reset(1);

    File randoopTestsDir = new File(new File(getProjectHome(), "tests"), "randoop");
    if (!randoopTestsDir.exists() && !randoopTestsDir.mkdirs()) {
      System.out.println("ERROR: Unable to create directory " + randoopTestsDir.getAbsolutePath());
      return;
    }
    // Create an initial dummy randoop/tests/FailinTest.java if doesn't exist.
    File failingTestFile = new File(randoopTestsDir, "FailingTest.java");
    if (!failingTestFile.exists()) {
      writeSubSuite(new ExecutableSequence(new Sequence()), randoopTestsDir, "randoop", "FailingTest");
    }

    List<Class<?>> classes = new ArrayList<Class<?>>();
    classes.addAll(getClassesUnderTest());
    GenInputsAbstract.public_only = false;

    List<StatementKind> model = Reflection.getStatements(classes, new DefaultReflectionFilter(null));
    SequenceCollection components = new SequenceCollection();
    components.addAll(SeedSequences.objectsToSeeds(SeedSequences.primitiveSeeds));
    components.addAll(SeedSequences.getSeedsFromAnnotatedFields(classes.toArray(new Class<?>[0])));
    List<ExecutionVisitor> visitors = new ArrayList<ExecutionVisitor>();
    List<ObjectContract> contracts = new ArrayList<ObjectContract>();
    List<ObjectContract> checkRepContracts = GenTests.getContractsFromAnnotations(classes.toArray(new Class<?>[0]));
    contracts.addAll(checkRepContracts);
    contracts.add(new EqualsReflexive());
    contracts.add(new EqualsToNullRetFalse());
    contracts.add(new EqualsHashcode());
    contracts.add(new EqualsSymmetric());
    ContractCheckingVisitor contractVisitor = new ContractCheckingVisitor(contracts, true);
    visitors.add(contractVisitor);
    visitors.add(new RegressionCaptureVisitor());

    // GenInputsAbstract.clear = 330;
    GenInputsAbstract.use_object_cache = true;
    GenInputsAbstract.maxsize = 50;
    GenInputsAbstract.forbid_null = false;
    GenInputsAbstract.null_ratio = 0.2;
    ReflectionExecutor.usethreads = false;
    ForwardGenerator explorer = new ForwardGenerator(model, null, getTimeLimitSeconds() * 1000, getInputLimit(), components, null);
    GenInputsAbstract.noprogressdisplay = true;
    GenInputsAbstract.output_tests = (getMode() == Mode.ERRORS ? "fail" : "pass");
    GenInputsAbstract.junit_output_dir = "tests";
    
    explorer.executionVisitor.visitors.addAll(visitors);

    explorer.explore();

    System.out.println(explorer.allSequences.size() + " inputs generated.");

    List<ExecutableSequence> tests = explorer.stats.outSeqs;
    if (getMode() == Mode.ERRORS) {
      System.out.println(tests.size() + " failing input" + (tests.size() > 1 || tests.size() == 0 ? "s" : "") + ".");
    }

    if (tests.isEmpty()) {
      return;
    }

    if (getMode() == Mode.REGRESSION) {
      JunitFileWriter jfw = new JunitFileWriter("tests", "randoop", GenTests.junit_classname, GenTests.testsperfile);
      List<File> files = jfw.createJunitFiles(tests);
      System.out.println();
      for (File f : files) {
        System.out.println("Created file: " + f.getAbsolutePath());
      }
    } else {
      // Print single error.
      ExecutableSequence test = tests.get(0);

      writeSubSuite(test, randoopTestsDir, "randoop", "FailingTest");

      List<String> options = new ArrayList<String>(); 
      options.addAll(Arrays.asList("-classpath", System.getProperty("java.class.path")));

      int failureIdx = test.getFailureIndex();
      List<Check> failures = null;
      if (failureIdx >= 0) {
        failures = test.getFailures(failureIdx);
      }
      
      assert failures.get(0) instanceof CheckWithTrace;
      CheckWithTrace firstFailure = (CheckWithTrace)failures.get(0);
      
      List<StackTraceElement> trace = new ArrayList<StackTraceElement>();
      StackTraceElement[] oldtrace = null;
      if (firstFailure.trace == null) {
        oldtrace = new StackTraceElement[0];
      } else {
        oldtrace = firstFailure.trace.getStackTrace();
      }
      if (firstFailure.trace != null && oldtrace.length > 0) {
        for (int i = 0 ; i < oldtrace.length ; i++) {
          StackTraceElement elt = oldtrace[i];
          if (elt.getClassName().startsWith("sun.reflect")) {
            break; // THIS WILL BE TOO GENERAL FOR CASES WHERE CODE UNDER TEST USES REFLECTION.
          }
          if (elt.getClassName().startsWith("java.lang.reflect")) {
            break; // THIS WILL BE TOO GENERAL FOR CASES WHERE CODE UNDER TEST USES REFLECTION.
          }
          if (elt.getClassName().startsWith("randoop")) {
            break; // WE PROBABLY NEVER GET HERE - ALWAYS GET REFLECTION FIRST...
          }
          trace.add(elt);
        } 
      }
      
      int traceIdx = 7 /* see writeSubSuite */ + test.toCodeString().split(Globals.lineSep).length;
      trace.add(new StackTraceElement("randoop.FailingTest", "test", "FailingTest.java", traceIdx));
      
      if (firstFailure.check instanceof NoExceptionCheck) {
        ExceptionalExecution ee = (ExceptionalExecution) test.getResult(failureIdx);
        Throwable ex = ee.getException();
        ex.setStackTrace(trace.toArray(new StackTraceElement[0]));
        throw ex;
      } else {
        assert firstFailure.check instanceof ObjectCheck; // TRUE?
        ObjectCheck c = (ObjectCheck) firstFailure.check;
        if (firstFailure.trace == null) {
          AssertionError ex = new AssertionError(); // BOGUS - MUST ACCT FOR
                                                    // BOOLEAN-VALUED, GRAL
                                                    // EXCEPTION.
          ex.setStackTrace(trace.toArray(new StackTraceElement[0]));
          throw ex;
        } else {
          Throwable ex = firstFailure.trace;
          ex.setStackTrace(trace.toArray(new StackTraceElement[0]));
          throw ex;
        }
      }   
    }
  }
 
  private File writeSubSuite(ExecutableSequence s, File dir, String packageName, String className) {
    File file = new File(dir, className + ".java");
    PrintStream out = null;
    try {
      out = new PrintStream(file);
      out.println("package " + packageName + ";");
      out.println("import junit.framework.*;");
      out.println();
      out.println("public class " + className + " extends TestCase {");
      out.println();
      out.println("  public void test() throws Throwable {");
      out.println();
      out.println(JunitFileWriter.indent(s.toCodeString()));
      out.println("  }");
      out.println();
      out.println("}");
    } catch (Exception e) {
      throw new Error(e);
    } finally {
      if (out != null) {
        out.close();
      }
    }
    return file;
  }
  
  private static class CheckWithTrace implements Check {
	  
    final Check check;
    final Throwable trace; // What about TheadDeath, other things we shouldn't hold on to?

    // trace can be null (if there was no exception thrown during the check).
    CheckWithTrace(Check c, Throwable trace) {
    	if (c == null) throw new IllegalArgumentException("c is null");
		  this.check = c;
		  this.trace = trace;
	  }

    @Override
    public boolean evaluate(Execution execution) {
    	return check.evaluate(execution);
    }

    @Override
    public String get_value() {
    	return check.get_value();

    }

    @Override
    public String toCodeStringPostStatement() {
    	return check.toCodeStringPostStatement();
    }

    @Override
    public String toCodeStringPreStatement() {
    	return check.toCodeStringPreStatement();
    }
  }

  private static class ContractCheckingVisitor implements ExecutionVisitor {

    private List<ObjectContract> contracts;
    private boolean checkAtEndOfExec;

    public ContractCheckingVisitor(List<ObjectContract> contracts, boolean checkAfterLast) {
      this.contracts = new ArrayList<ObjectContract>();
      this.checkAtEndOfExec = checkAfterLast;
      for (ObjectContract c : contracts) {
        if (c.getArity() > 2)
          throw new IllegalArgumentException("Visitor accepts only unary or binary contracts.");
        this.contracts.add(c);
      }
    }

    @Override
    public void initialize(ExecutableSequence s) {
      s.checks.clear();
      s.checksResults.clear();
      for (int i = 0; i < s.sequence.size(); i++) {
        s.checks.add(new ArrayList<Check>(1));
        s.checksResults.add(new ArrayList<Boolean>(1));
      }
    }

    public void visitBefore(ExecutableSequence sequence, int i) {
      // no body.
    }

    /**
     * If idx is the last index, checks contracts.
     */
    public boolean visitAfter(ExecutableSequence s, int idx) {

      for (int i = 0; i <= idx; i++) {
        assert !(s.getResult(i) instanceof NotExecuted) : s;
        if (i < idx)
          assert !(s.getResult(i) instanceof ExceptionalExecution) : s;
      }

      if (checkAtEndOfExec && idx < s.sequence.size() - 1) {
        // Check contracts only after the last statement is executed.
        return true;
      }

      if (s.getResult(idx) instanceof ExceptionalExecution) {
        ExceptionalExecution exec = (ExceptionalExecution) s.getResult(idx);
        if (exec.getException().getClass().equals(NullPointerException.class) || exec.getException().getClass().equals(AssertionError.class)) {
          NoExceptionCheck obs = new NoExceptionCheck(idx);
          s.addCheck(idx, new CheckWithTrace(obs, exec.getException()), false);
        }
        return true;
      }
      
      assert s.getResult(idx) instanceof NormalExecution : s.getResult(idx);

      MultiMap<Class<?>, Integer> idxmap = objectIndicesToCheck(s, idx);
      for (Class<?> cls : idxmap.keySet()) {
        for (ObjectContract c : contracts) {
          if (c.getArity() == 1) {
            if (checkUnary(s, c, idxmap.getValues(cls), idx)) {
            	return true;
            }
          } else {
            assert c.getArity() == 2;
            if (checkBinary(s, c, idxmap.getValues(cls), idx)) {
            	return true;
            }
          }
        }
      }
      return true;
    }

    // Returns true if a failure was found.
    private boolean checkBinary(ExecutableSequence s, ObjectContract c, Set<Integer> values, int idx) {
      for (Integer i : values) {
        for (Integer j : values) {

          ExecutionOutcome result1 = s.getResult(i);
          assert result1 instanceof NormalExecution : s;

          ExecutionOutcome result2 = s.getResult(j);
          assert result2 instanceof NormalExecution : s;

          if (Log.isLoggingOn())
            Log.logLine("Checking contract " + c.getClass() + " on " + i + ", " + j);

          ExecutionOutcome exprOutcome = ObjectContractUtils.execute(c, ((NormalExecution) result1).getRuntimeValue(), ((NormalExecution) result2)
              .getRuntimeValue());

          Check obs = null;

          if (exprOutcome instanceof NormalExecution) {
            NormalExecution e = (NormalExecution) exprOutcome;
            if (e.getRuntimeValue().equals(true)) {
              continue; // Behavior ok.
            } else {
              if (Log.isLoggingOn())
                Log.logLine("Contract returned false. Will add ExpressionEqFalse check");
              // Create an check that records the actual value
              // returned by the expression, marking it as invalid
              // behavior.
              obs = new ObjectCheck(c, s.sequence.getVariable(i), s.sequence.getVariable(j));
              s.addCheck(idx, new CheckWithTrace(obs, null), false);
              return true;
            }
          } else {
        	  
            // Execution of contract resulted in exception. Do not create
            // a contract-violation decoration.
            assert exprOutcome instanceof ExceptionalExecution;
            if (checkExceptionFailure(s, idx, c, (ExceptionalExecution)exprOutcome, s.sequence.getVariable(i), s.sequence.getVariable(j))) {
              return true;
            }
          }
        }
      }
      return false;
    }

    // Returns true if a failure was found.
    private boolean checkUnary(ExecutableSequence s, ObjectContract c, Set<Integer> values, int idx) {
      for (Integer i : values) {
        ExecutionOutcome result = s.getResult(i);
        assert result instanceof NormalExecution : s;
        ExecutionOutcome exprOutcome = ObjectContractUtils.execute(c, ((NormalExecution) result).getRuntimeValue());
        if (exprOutcome instanceof NormalExecution) {
          NormalExecution e = (NormalExecution) exprOutcome;
          if (e.getRuntimeValue().equals(true)) {
            continue; // Behavior ok.
          } else {
        	  assert e.getRuntimeValue().equals(false);
              Check obs = new ObjectCheck(c, s.sequence.getVariable(i));
              s.addCheck(idx, new CheckWithTrace(obs, null), false);
              return true;
          }
        } else {
          assert exprOutcome instanceof ExceptionalExecution;
          if (checkExceptionFailure(s, idx, c, (ExceptionalExecution)exprOutcome, s.sequence.getVariable(i))) {
            return true;
          }
        }
      }
      return false;
    }

    // Checks if the exception wrapped in e represents a failure, and if so,
    // adds a failing check to the sequence.
    // Returns true if a failure was found.
    private static boolean checkExceptionFailure(ExecutableSequence s, int idx, ObjectContract c, ExceptionalExecution e, Variable... vars) {
      // Execution of contract resulted in exception. Do not create
      // a contract-violation decoration.
      if (e.getException().equals(BugInRandoopException.class)) {
        throw new BugInRandoopException(e.getException());
      }
      if (!c.evalExceptionMeansFailure()) {
        // Exception thrown, but not considered a failure.
        // Will not record behavior.
        return false;
      }
      // If we get here, the contract resulted
      // in an exception that is considered a failure. Add
      // a contract violation check.
      // Create an check that records the actual value
      // returned by the expression, marking it as invalid
      // behavior.
      Check obs = new ObjectCheck(c, vars);
      s.addCheck(idx, new CheckWithTrace(obs, e.getException()), false);
      return true;
    }
    
    // Returns the indices for the objects to check contracts over.
    //
    // If an element is primitive, a String, or null, its index is not returned.
    //
    // The indices are returned as a map, from types to the indices of
    // the given type. Binary contracts are only checked for objects
    // of equal types, so themap is handy.
    private static MultiMap<Class<?>, Integer> objectIndicesToCheck(ExecutableSequence s, int maxIdx) {

      MultiMap<Class<?>, Integer> map = new MultiMap<Class<?>, Integer>();

      for (int i = 0; i <= maxIdx; i++) {
        ExecutionOutcome result = s.getResult(i);

        assert result instanceof NormalExecution;

        Class<?> outputType = s.sequence.getStatementKind(i).getOutputType();

        if (outputType.equals(void.class))
          continue;
        if (outputType.equals(String.class))
          continue;
        if (PrimitiveTypes.isPrimitive(outputType))
          continue;

        Object runtimeValue = ((NormalExecution) result).getRuntimeValue();
        if (runtimeValue == null)
          continue;

        map.add(outputType, i);
      }
      return map;
    }

  }
}