package randoop.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import randoop.DFResultsOneSeq;
import randoop.DataFlowOutput;
import randoop.DummyVisitor;
import randoop.ExecutableSequence;
import randoop.MSequence;
import randoop.MStatement;
import randoop.MVariable;
import randoop.PrimitiveOrStringOrNullDecl;
import randoop.RMethod;
import randoop.Sequence;
import randoop.SequenceCollection;
import randoop.SequenceParseException;
import randoop.StatementKind;
import randoop.Variable;
import randoop.DFResultsOneSeq.VariableInfo;
import randoop.util.Files;
import randoop.util.RecordListReader;
import randoop.util.RecordProcessor;
import randoop.util.Reflection;
import randoop.util.SimpleList;
import randoop.util.Reflection.Match;
import plume.Option;
import plume.Options;
import plume.Pair;
import plume.Options.ArgException;
import cov.Branch;
import cov.Coverage;
import cov.CoverageAtom;

public class GenBranchDir {

  // Set in main method. List of coverage-instrumented classes.
  private static List<Class<?>> covClasses;

  // Set in main method. For logging events.
  private static PrintStream out;

  // The branches uncovered by Randoop.
  private static Set<Branch> uncoveredByRandoop;

  // The branches covered by bdgen across its entire execution.
  private static Set<Branch> coveredByBDGen = new LinkedHashSet<Branch>();

  // Results are placed here.
  // A result is a pair. The first element is a goal branch, which is the branch
  // opposite a frontier branch. The second element is a sequence that covers the
  // goal branch.
  private static List<Pair<Branch, Sequence>> successes = new ArrayList<Pair<Branch,Sequence>>();
  private static List<DFResultsOneSeq> failures = new ArrayList<DFResultsOneSeq>();
  private static int noDFInfo = 0;
  private static int nonRepro = 0;
  private static int numSeqs = 0;

  @Option("Input file can have different branches.")
  public static boolean many_branches = false;

  @Option("Name of a file containing a list of classes that are coverage-instrumented.")
  public static String input_covinst_classes = null;

  @Option("Name of a file containing a serialized list of sequences.")
  public static List<String> input_components_ser = new ArrayList<String>();

  @Option("Name of a file containing a textual list of sequences.")
  public static List<String> input_components_txt = new ArrayList<String>();

  @Option("Name of input file containing the output of DataFlow.")
  public static String input_df_results = null;

  @Option("Name of an input file containing branches covered by Randoop.")
  public static String input_covmap = null;

  @Option("If given, prints all the branches newly covered to the given file.")
  public static String output_new_branches;

  @Option("If given and --print-branches also given, prints branches sorted.")
  public static boolean output_new_branches_sorted;

  @Option("(REQUIRED) Name of the file where results will be written.")
  public static String output_new_sequences = null;

  @Option("Output summary to the given file.")
  public static String output_summary = null;

  @Option("Output failures to the given file.")
  public static String output_failures = null;

  @Option("Output the components that were used to the given file.")
  public static String output_components_used = null;

  @Option("Ouput the sequences that were successfully modified to the given file.")
  public static String output_success_seqs = null;

  @Option("Name of a file where detailed logging information will be written.")
  public static String logfile = null;

  private static PrintStream output = null;

  // Set in main method. Component sequences to help bdgen.
  private static SequenceCollection components = null;

  private static PrintStream componentsUsedStream = null;
  private static Set<Sequence> alreadyPrinted  = new LinkedHashSet<Sequence>();

  private static PrintStream successSeqsStream = null;

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {

    // Parse options and ensure that a scratch directory was specified.
    Options options = new Options(GenBranchDir.class);
    String[] args2 = null;
    try {
      args2 = options.parse(args);
    } catch (ArgException e) {
      throw new Error(e);
    }
    if (output_new_sequences == null) {
      System.out.println("Missing required option --out_new_sequences=<filename>");
      System.exit(1);
    }
    if (input_df_results == null) {
      System.out.println("Missing required option --inputfile=<filename>");
      System.exit(1);
    }
    if (output_summary != null && many_branches) {
      System.out.println("--output-summary option requires --many-branches=false.");
      System.exit(1);
    }

    try {
      output = new PrintStream(new FileOutputStream(output_new_sequences));
    } catch (Exception e) {
      throw new Error(e);
    }

    if (args2.length != 0) throw new IllegalArgumentException("Invalid arguments:" +
                                                              Arrays.asList(args2));

    try {
      if (output_components_used != null)
        componentsUsedStream = new PrintStream(new FileOutputStream(output_components_used));
      if (output_success_seqs != null)
        successSeqsStream = new PrintStream(new FileOutputStream(output_success_seqs));
    } catch (FileNotFoundException e) {
      throw new Error(e);
    }


    // Read in the coverage-instrumented classes.
    // We need them in order to reset their coverage before executing
    // a sequence, so we can determine what coverage atoms the sequence covers.
    File covClassesFile = new File(input_covinst_classes);
    covClasses = new ArrayList<Class<?>>();
    List<String> covClassNames = null;
    try {
      covClassNames = Files.readWhole(covClassesFile);
    } catch (IOException e) {
      throw new Error(e);
    }
    Set<CoverageAtom> allBranches = new LinkedHashSet<CoverageAtom>();
    for (String className : covClassNames) {
      Class<?> cls = Reflection.classForName(className);
      covClasses.add(cls);
      allBranches.addAll(Coverage.getBranches(cls));
    }
    System.out.println("ALL BRANCHES=" + allBranches.size());



    // Read coverage map.
    Map<CoverageAtom,Set<Sequence>> inputmap =
      new LinkedHashMap<CoverageAtom, Set<Sequence>>();
    if (input_covmap != null) {
      try {
        FileInputStream fileis = new FileInputStream(input_covmap);
        ObjectInputStream objectis = new ObjectInputStream(new GZIPInputStream(fileis));
        inputmap = (Map<CoverageAtom, Set<Sequence>>) objectis.readObject();
        objectis.close();
        fileis.close();
      } catch (Exception e) {
        throw new Error(e);
      }
    }

    Set<Branch> coveredByRandoop = new LinkedHashSet<Branch>();
    for (CoverageAtom ca : inputmap.keySet()) {
      coveredByRandoop.add((Branch)ca);
    }

    System.out.println("COVERED BY RANDOOP=" + coveredByRandoop.size());
    uncoveredByRandoop = new LinkedHashSet<Branch>();
    for (CoverageAtom ca : allBranches) {
      if (!coveredByRandoop.contains(ca))
        uncoveredByRandoop.add((Branch) ca);
    }
    System.out.println("NOT COVERED BY RANDOOP=" + uncoveredByRandoop.size());


    // If log file given, set out to it. Otherwise, set out to System.out.
    if (logfile != null) {
      File logFile = new File(logfile);
      try {
        out = new PrintStream(new FileOutputStream(logFile));
      } catch (FileNotFoundException e) {
        throw new Error(e);
      }
    } else {
      out = System.out;
    }

    getComponents(String.class, false);


    String inputFile = input_df_results;
    DataFlowOutput dfOut = DataFlowOutput.parse(inputFile);
    assert dfOut.results.size() > 0;

    if (!many_branches) {
      // Sanity check: all results for this file correspond to the same frontier branch.
      Branch frontier = (Branch)dfOut.results.get(0).frontierBranch;
      for (DFResultsOneSeq r : dfOut.results) assert frontier.equals(r.frontierBranch);
    }

    // System.out.printf ("covered = %s%n", coveredByRandoop);
    // System.out.printf ("uncovered = %s%n", uncoveredByRandoop);
    for (DFResultsOneSeq r : dfOut.results) {
      // System.out.printf ("r = %s%n", 
      //                    ((Branch)r.frontierBranch).getOppositeBranch());
      if (!coveredByRandoop.contains(((Branch)r.frontierBranch).getOppositeBranch())) {
        assert uncoveredByRandoop.contains
          (((Branch)r.frontierBranch).getOppositeBranch()) :
          uncoveredByRandoop + " " + coveredByRandoop + " " 
            + ((Branch)r.frontierBranch).getOppositeBranch();
        processOneSequence(r);
      } else {
        System.out.println("FRONTIER BRANCH COVERED IN A DIFFERENT RUN OF RANDOOP.");
      }
    }

    assert successes.size() + failures.size() + noDFInfo + nonRepro == numSeqs : numSeqs;

    // Output summary for the branch processed in this file.
    if (output_summary != null) {
      String summ = null;
      if (successes.size() > 0) {
        summ = "success";
      } else if (failures.size() > 0) {
        summ = "failure";
      } else if (noDFInfo > 0) {
        summ = "nodf";
      } else {
        summ = "nonrepro";
      }
      try {
        Files.writeToFile(summ, output_summary);
      } catch (IOException e) {
        throw new Error(e);
      }
    }

    String summ_detailed = "There were "
        + successes.size() + " successes, "
        + failures.size() + " failures, "
        + noDFInfo + " no DF info, and "
      + nonRepro + " nonrepro frontier branch.";

    System.out.println(summ_detailed);

    if (output_failures != null) {
      PrintStream failures_output;
      try {
        failures_output = new PrintStream(new FileOutputStream(output_failures));
        if (failures.size() > 0) {
          failures_output.println(failures.size());
          failures_output.println("Failed to reach the following frontier branches:");
          for (DFResultsOneSeq r : failures) {
            failures_output.println(r.frontierBranch);
            failures_output.println("SEQUENCE");
            failures_output.println(r.sequence.toParseableString());
          }
          failures_output.close();
        }
      } catch (FileNotFoundException e) {
        throw new Error(e);
      }
    }

    if (output_new_branches != null) {
      Branch.writeToFile(coveredByBDGen, output_new_branches, output_new_branches_sorted);
    }

    output.close();
    out.close();

    if (successSeqsStream != null)
      successSeqsStream.close();
    if (componentsUsedStream != null)
      componentsUsedStream.close();
  }

  // Reads a file containing a collection of sequences in textual representation.
  private static Set<Sequence> readTextSequences(String file) {

    final Set<Sequence> sequences = new LinkedHashSet<Sequence>();

    // Parse the file using a RecordListReader.
    RecordProcessor processor = new RecordProcessor() {
      public void processRecord(List<String> record) {
        try {
          sequences.add(Sequence.parse(record));
        } catch (SequenceParseException e) {
          throw new Error(e);
        }
      }
    };
    RecordListReader reader = new RecordListReader("SEQUENCE", processor);
    reader.parse(file);

    return sequences;
  }


  /**
   * @param dfOut
   */
  private static void processOneSequence(DFResultsOneSeq r) {

    // Workaround for the fact that dataflow gives no information on null
    // values. If DF gives says that at least one non-primitive declaration was compared
    // to null, We add to the set of interesting variables every variable
    // declared as "var = null", and we add "null" as one of the values it was
    // compared to.
    boolean null_is_interesting = false;
    for (VariableInfo vi : r.values) {
      if (vi.branch_compares.contains("null")) {
        null_is_interesting = true;
        break;
      }
    }
    if (null_is_interesting) {
      // Try single-variable null flip strategy on variables that were null.
      for (int i = 0 ; i < r.sequence.size() ; i++) {
        StatementKind st = r.sequence.getStatementKind(i);
        // If i-th statement is "x = null;" ...
        if (st instanceof PrimitiveOrStringOrNullDecl
            && ((PrimitiveOrStringOrNullDecl)st).getValue() == null) {
          Variable v = r.sequence.getVariable(i);

          // See if there is already a variable info for this variable.
          VariableInfo vi = null;
          for (VariableInfo rvi : r.values) {
            if (rvi.value.equals(v)) {
              vi = rvi;
              break;
            }
          }

          // If not already a variable info for this variable, create one.
          if (vi == null) {
            vi = new VariableInfo(v);
            r.values.add(vi);
          }
          vi.add_branch_compare("null");
        }
      }
    }

    numSeqs++;
    out.println("============================================================");

    // Print out as DataFlowInput.
    out.println("Record in DataFlowInput format:");
    out.println("START RECORD");
    out.println("BRANCH ");
    out.println(r.frontierBranch.toString());
    out.println("SEQUENCE");
    out.println(r.sequence.toParseableString());
    out.println("END RECORD");
    out.println();
    out.println();

    // Print out as DataFlowOutput.
    out.println("Record in DataFlowOutput format:");
    out.println("START DFRESULT");
    out.println(r.toParseableString());
    out.println("END DFRESULT");
    out.println();
    out.println();

    // Print out the source code surrounding the frontier branch.
    String lineSource = null;
    try {
      lineSource = Coverage.getMethodSource(Class.forName(r.frontierBranch.getClassName()),
          r.frontierBranch.getLineNumber(), ">>");
    } catch (ClassNotFoundException e) {
      throw new Error(e);
    }

    out.println(lineSource + "\n");

    // Verify that sequence covers frontier branch.
    ExecutableSequence eseq = new ExecutableSequence(r.sequence);
    Coverage.clearCoverage(covClasses);
    eseq.execute(new DummyVisitor());
    if (!Coverage.getCoveredAtoms(covClasses).contains(r.frontierBranch)) {
      nonRepro++;
      out.println("WARNING: Sequence doesn't cover frontier branch (WILL NOT add to successes or failures).");
      return;
    }

    // If no variables, we have no dataflow information. Go to
    // next sequence, but don't count as failure.
    if (r.values.size() == 0) {
      noDFInfo++;
      out.println("NO DATAFLOW INFORMATION. WILL SKIP TO NEXT SEQUENCE (WILL NOT add to successes or failures).");
      return;
    }

    Pair<Branch, Sequence> covseq = null;

    // Try single-variable strategies.
    covseq = oneVarStrategies(r);
    if (covseq != null) {
      successes.add(covseq);
      printSucc(r);
      return;
    }

    // If more than one variable, try multi-variable strategies.
    if (r.values.size() > 1) {
      covseq = twoVarStrategies(r);
      if (covseq != null) {
        successes.add(covseq);
        printSucc(r);
        return;
      }
      covseq = twoVarsAliasLastStatementVars(r);
      if (covseq != null) {
        successes.add(covseq);
        printSucc(r);
        return;
      }
    }

    failures.add(r);

    out.println("FAILURE");
  }

  private static void printSucc(DFResultsOneSeq r) {
    if (successSeqsStream != null) {
      successSeqsStream.println("START DFRESULT");
      successSeqsStream.println(r.toParseableString());
      successSeqsStream.println("END DFRESULT");
    }
  }

  private static Pair<Branch, Sequence> twoVarsAliasLastStatementVars(
      DFResultsOneSeq r) {

    StatementKind st = r.sequence.getLastStatement();
    List<Class<?>> types = r.sequence.getLastStatement().getInputTypes();
    List<Variable> vars = r.sequence.getInputs(r.sequence.size() - 1);
    assert vars.size() == types.size();
    Branch goalBranch = ((Branch)r.frontierBranch).getOppositeBranch();

    boolean isInstanceMethod =
      (st instanceof RMethod)
      && (!((RMethod)st).isStatic());


    for (int i = 0 ; i < types.size() ; i++) {
      for (int j = 0 ; j < types.size() ; j++) {
        if (i == j)
          continue;
        // See if we can replace i <- j.
        if (!Reflection.canBeUsedAs(types.get(j), types.get(i)))
          continue;
        StatementKind jSt = r.sequence.getStatementKind(vars.get(j).getDeclIndex());
        if (i == 0 && isInstanceMethod && jSt instanceof PrimitiveOrStringOrNullDecl)
          continue;

        // Ok to replace.
        MSequence s = r.sequence.toModifiableSequence();
        MStatement lastSt = s.statements.get(s.size() - 1);
        lastSt.inputs.set(i, lastSt.inputs.get(j));

        out.println("WILL TRY TO REPLACE " + i + "th INPUT TO LAST STATEMENT WITH " + j + "th INPUT.");
        out.println("MODIFIED SEQUENCE:");
        Sequence news = s.toImmutableSequence();
        out.println(news.toParseableString());
        if (coversUncovered(news, goalBranch, "prim swap vars 3")) {
          out.println("SUCCESS! (twovars)");
          return new Pair<Branch, Sequence>(goalBranch, news);
        }
      }
    }
    return null;
  }

  private static Pair<Branch,Sequence> twoVarStrategies(DFResultsOneSeq r) {
    out.append("WILL TRY TWO-VARIABLE STRATEGIES.");
    Branch goalBranch = ((Branch)r.frontierBranch).getOppositeBranch();
    Comparator<VariableInfo> comp = new Comparator<VariableInfo>() {
      public int compare(VariableInfo o1, VariableInfo o2) {
        return new Integer(o1.value.getDeclIndex()).compareTo(new Integer(o2.value.getDeclIndex()));
      }
    };
    Set<VariableInfo> sorted = new TreeSet<VariableInfo>(comp);
    sorted.addAll(r.values);
    // System.out.println("vars: " + sorted);
    VariableInfo[] vars = sorted.toArray(new VariableInfo[0]);
    for (int i = 0 ; i < vars.length ; i++) {
      for (int j = 0 ; j < vars.length ; j++) {
        if (i == j)
          continue;
        MSequence s = r.sequence.toModifiableSequence();
        MVariable ith = s.getVariable(vars[i].value.getDeclIndex());
        MVariable jth = s.getVariable(vars[j].value.getDeclIndex());
        if (i > j) {
          if (!moveJthBeforeIth(s, jth, ith))
            continue;
        }
        if (!canReplace(s, ith, jth))
            continue;
        MSequence s2 = s.makeCopy();
        MVariable s2i = s2.getVariable(ith.getDeclIndex());
        MVariable s2j = s2.getVariable(jth.getDeclIndex());
        replaceUses(s2, s2i, s2j);
        out.println("WILL TRY TO REPLACE " + i + "th VAR WITH " + j + "th VAR.");
        out.println("MODIFIED SEQUENCE:");
        Sequence news = s2.toImmutableSequence();
        out.println(news.toParseableString());
        if (coversUncovered(news, goalBranch, "prim swap vars 2")) {
          out.println("SUCCESS! (twovars)");
          return new Pair<Branch, Sequence>(goalBranch, news);
        }

        s2 = s.makeCopy();
        s2i = s2.getVariable(ith.getDeclIndex());
        s2j = s2.getVariable(jth.getDeclIndex());

        replaceUsesWithCopy(s2, s2i, s2j);
        out.println("WILL TRY TO REPLACE " + i + "th VAR WITH " + j + "th VAR.");
        out.println("MODIFIED SEQUENCE:");
        news = s2.toImmutableSequence();
        out.println(news.toParseableString());
        if (coversUncovered(news, goalBranch, "prim swap vars 1")) {
          out.println("SUCCESS! (twovars)");
          return new Pair<Branch, Sequence>(goalBranch, news);
        }
      }
    }
    return null;
  }

  private static void replaceUsesWithCopy(MSequence mseq, MVariable mv1,
      MVariable mv2) {

    MSequence copy = mseq.makeCopy();
    MVariable mv1_copy = copy.getVariable(mv1.getDeclIndex());
    Set<MVariable> predecessors = getPredecessors(mv1_copy);
    List<MVariable> sorted = sortByDeclIndex(predecessors);
    List<MStatement> newsts = new ArrayList<MStatement>();
    for (MVariable v : sorted) {
      newsts.add(copy.statements.get(v.getDeclIndex()));
    }
    copy.statements = newsts;
    copy.checkRep();

    Map<MVariable, MVariable> map = mseq.insert(0, copy);
    MVariable mv1_new = map.get(mv1_copy);

    for (int i = mv2.getDeclIndex() + 1 ; i < mseq.size() ; i++) {
      MStatement st = mseq.getStatement(i);
      for (int j = 0 ; j < st.inputs.size() ; j++) {
        if (st.inputs.get(j).equals(mv2)) {
          st.inputs.set(j, mv1_new);
        }
      }
    }
  }

  private static List<MVariable> sortByDeclIndex(Set<MVariable> predecessors) {
    Comparator<MVariable> c = new Comparator<MVariable>() {
      public int compare(MVariable o1, MVariable o2) {
        return new Integer(o1.getDeclIndex()).compareTo(new Integer(o2.getDeclIndex()));
      }
    };
    List<MVariable> list = new ArrayList<MVariable>(predecessors);
    Collections.sort(list, c);
    return list;
  }

  private static void replaceUses(MSequence mseq, MVariable mv1, MVariable mv2) {
    for (int i = mv2.getDeclIndex() + 1 ; i < mseq.size() ; i++) {
      MStatement st = mseq.getStatement(i);
      for (int j = 0 ; j < st.inputs.size() ; j++) {
        if (st.inputs.get(j).equals(mv2)) {
          st.inputs.set(j, mv1);
        }
      }
    }
  }

  private static boolean canReplace(MSequence seq, MVariable v1, MVariable v2) {
    // Check if v1 is type-compatible with all uses of v2.
    for (int statementIndex : seq.getUses(v2)) {
      for (int ithInput = 0 ; ithInput < seq.getInputs(statementIndex).size() ; ithInput++) {
        if (seq.getInputs(statementIndex).get(ithInput).equals(v2)) {
          Class<?> inputType = seq.getStatementKind(statementIndex).getInputTypes().get(ithInput);
          if (!Reflection.canBeUsedAs(v1.getType(), inputType)) {
            return false;
          }
        }
      }
    }
    return true;
  }



  private static Pair<Branch, Sequence> oneVarStrategies(DFResultsOneSeq r) {
    out.append("WILL TRY ONE-VARIABLE STRATEGIES.");
    for (VariableInfo varinfo : r.values) {
      Pair<Branch, Sequence> pair = oneVarNumericStrats(r, varinfo);
      if (pair != null) {
        return pair;
      }

      if (varinfo.branch_compares.contains("null")) {
        pair = oneVarNullStrat(r.sequence, (Branch) r.frontierBranch, varinfo.value);
        if (pair != null) {
          return pair;
        } else {
        }
      }
    }
    return null;
  }

  private static Pair<Branch,Sequence> oneVarNullStrat(Sequence sequence,
      Branch frontierBranch, Variable var) {


    Branch goalBranch = (frontierBranch).getOppositeBranch();

    StatementKind st = sequence.getStatementKind(var.getDeclIndex());

    if (st instanceof PrimitiveOrStringOrNullDecl) {

      PrimitiveOrStringOrNullDecl decl = (PrimitiveOrStringOrNullDecl)st;
      if (st.getOutputType().isPrimitive() || st.getOutputType().equals(String.class)) {
        out.println("Primitive value or string...");
        return null;
      }
      assert decl.getValue() == null : sequence + "," + var;

      // Find su-bsequences that creates the type.
      // TODO do components have things like "x = null"? We don't want those.
      SimpleList<Sequence> comps = getComponents(decl.getType(), false);
      if (comps.size() == 0) {
        out.println("NO COMPONENTS.");
        return null;
      }

      // Choose a sub-sequence
      Sequence comp = getSmallest(comps);
      out.println("WILL USE:");
      out.println(comp.toParseableString());
      out.println();
      if (componentsUsedStream != null && !alreadyPrinted.contains(comp)) {
        componentsUsedStream.println("START SEQUENCE");
        componentsUsedStream.println(comp.toParseableString().trim());
        componentsUsedStream.println("END SEQUENCE");
        componentsUsedStream.println();
        alreadyPrinted.add(comp);
      }

      // Choose a variable in the sub-sequence. We'll call it the "new
      // variable".
      List<Variable> varsOfType = comp.getVariablesOfType(decl.getType(),
          Match.COMPATIBLE_TYPE);
      Variable compvar = getLast(varsOfType);


      // Translate everything into the modifiable sequence world.
      // The old (original) sequence.
      MSequence seq = sequence.toModifiableSequence();
      // The old (its uses to be replaced) variable.
      MVariable oldvar = seq.getVariable(var.getDeclIndex());
      // The sub-sequence to insert.
      MSequence subseq = comp.toModifiableSequence();
      // The variable to replace oldvar's uses.
      MVariable newvar = subseq.getVariable(compvar.getDeclIndex());

      // Insert sub-sequence into old sequence, right before the old var declaration.
      Map<MVariable,MVariable> varmap = seq.insert(oldvar.getDeclIndex(), subseq);
      // update newvar to its MVariable in the modified sequence.
      newvar = varmap.get(newvar);
      assert newvar != null;

      // Replace all uses of old var with new var.
      // Since we picked new var to be type-compatible with old var, so this
      // must succeed.
      out.println("REPLACING ALL USES OF " + oldvar + " WITH " + newvar);
      replaceUses(seq, newvar, oldvar);
      seq.checkRep();

      Sequence newseq = seq.toImmutableSequence();

      out.println("NEW SEQUENCE: ");
      out.println(newseq.toParseableString());

      // Check if it covers the goal branch.
      if (coversUncovered(newseq, goalBranch, "make null non-null")) {
        out.println("SUCCESS! (onevarnull)");
        return new Pair<Branch, Sequence>(goalBranch, newseq);
      }

    } else {

      // The variable is not null. Replace it with null.

      StatementKind newSt = new PrimitiveOrStringOrNullDecl(st.getOutputType(), null);
      Sequence comp = new Sequence();
      comp = comp.extend(newSt, new ArrayList<Variable>());
      Variable compvar = comp.getLastVariable();


      // Translate everything into the modifiable sequence world.
      // The old (original) sequence.
      MSequence seq = sequence.toModifiableSequence();
      // The old (its uses to be replaced) variable.
      MVariable oldvar = seq.getVariable(var.getDeclIndex());
      // The sub-sequence to insert.
      MSequence subseq = comp.toModifiableSequence();
      // The variable to replace oldvar's uses.
      MVariable newvar = subseq.getVariable(compvar.getDeclIndex());

      // Insert sub-sequence into old sequence, right before the old var declaration.
      Map<MVariable,MVariable> varmap = seq.insert(oldvar.getDeclIndex(), subseq);
      // update newvar to its MVariable in the modified sequence.
      newvar = varmap.get(newvar);
      assert newvar != null;

      // Replace all uses of old var with new var.
      // Since we picked new var to be type-compatible with old var, so this
      // must succeed.
      out.println("REPLACING ALL USES OF " + oldvar + " WITH " + newvar);
      replaceUses(seq, newvar, oldvar);
      seq.checkRep();

      Sequence newseq = seq.toImmutableSequence();

      out.println("NEW SEQUENCE: ");
      out.println(newseq.toParseableString());

      // Check if it covers the goal branch.
      if (coversUncovered(newseq, goalBranch, "make non-null null")) {
        out.println("SUCCESS! (onevarnull)");
        return new Pair<Branch, Sequence>(goalBranch, newseq);
      }
    }


    return null;
  }

  private static SimpleList<Sequence> getComponents(Class<?> type, boolean match) {

    if (components == null) {

      // Initialize components.
      components = new SequenceCollection();
      if (!input_components_ser.isEmpty()) {
        for (String onefile : input_components_ser) {
          try {
            FileInputStream fileos = new FileInputStream(onefile);
            ObjectInputStream objectos = new ObjectInputStream(new GZIPInputStream(fileos));
            @SuppressWarnings("unchecked")
            Set<Sequence> seqset = (Set<Sequence>)objectos.readObject();
            System.out.println("Adding " + seqset.size() + " component sequences from file "
                               + onefile);
            components.addAll(seqset);
          } catch (Exception e) {
            throw new Error(e);
          }
        }
      }
      if (!input_components_txt.isEmpty()) {
        for (String onefile : input_components_txt) {
          Set<Sequence> seqset = readTextSequences(onefile);
          System.out.println("Adding " + seqset.size() + " component sequences from file "
                             + onefile);
          components.addAll(seqset);
        }
      }

    }

    return components.getSequencesForType(type, match);

  }

  private static Variable getLast(List<Variable> varsOfType) {
    assert varsOfType.size() > 0;
    Variable last = varsOfType.get(0);
    for (int i = 1 ; i < varsOfType.size() ; i++) {
      Variable v2 = varsOfType.get(i);
      if (v2.getDeclIndex() > last.getDeclIndex())
        last = v2;
    }
    // If the following assert fails, it doesn't mean something
    // is incorrect. I put it here to reflect the fact that
    // the method SequenceCollection.getSequencesForType
    // currently always returns sequences where the LAST
    // statement creates a value of the given type. If I ever
    // change the behavior of the method, this assert may fail
    // which will force me to revisit this code to make sure
    // it still makes sense.
    assert last.getDeclIndex() == last.sequence.size() - 1;
    return last;

  }

  private static Sequence getSmallest(SimpleList<Sequence> comps) {
    assert comps.size() > 0;
    Sequence smallest = comps.get(0);
    for (int i = 1 ; i < comps.size() ; i++) {
      if (comps.get(i).size() < smallest.size())
        smallest = comps.get(i);
    }
    return smallest;
  }

  private static Pair<Branch,Sequence> oneVarNumericStrats(DFResultsOneSeq r, VariableInfo varinfo) {

    Variable var = varinfo.value;
    assert var.sequence == r.sequence;

    // Determine goal branch.
    assert r.frontierBranch instanceof Branch;
    Branch goalBranch = ((Branch)r.frontierBranch).getOppositeBranch();
    out.println("GOAL BRANCH (WILL TRY TO COVER): " + goalBranch);

    if (!var.getType().equals(int.class))
      return null;


    // First, try setting its value to a value compared to it during execution.
    for (String val : varinfo.branch_compares) {

      out.println("WILL TRY REPLACING " + var + " WITH VALUE " + val);
      int intval = -1;
      try {
         intval = Integer.parseInt(val);
      } catch (NumberFormatException e) {
        System.out.println("WARNING: NumberFormatException when parsing value in VariableInfo: " + varinfo);
        continue;
      }
      Sequence news = replaceVarValue(r.sequence, var, intval, goalBranch);
      if (coversUncovered(news, goalBranch, "prim replace with " + val)) {
        out.println("SUCCESS! (onevarnum)");
        return new Pair<Branch, Sequence>(goalBranch, news);
      }
    }

    // First, try setting its value to a value compared to it during execution, +1.
    for (String val : varinfo.branch_compares) {
      out.println("WILL TRY REPLACING " + var + " WITH VALUE " + val + " PLUS ONE.");
      int intval = -1;
      try {
         intval = Integer.parseInt(val);
      } catch (NumberFormatException e) {
        System.out.println("WARNING: NumberFormatException when parsing value in VariableInfo: " + varinfo);
        continue;
      }
      Sequence news = replaceVarValue(r.sequence, var, intval + 1, goalBranch);
      if (coversUncovered(news, goalBranch, "primt replace " + val + " +1")) {
        out.println("SUCCESS! (onevarnum)");
        return new Pair<Branch, Sequence>(goalBranch, news);
      }
    }

    // First, try setting its value to a value compared to it during execution, -1.
    for (String val : varinfo.branch_compares) {
      out.println("WILL TRY REPLACING " + var + " WITH VALUE " + val + " MINUS ONE.");
      int intval = -1;
      try {
         intval = Integer.parseInt(val);
      } catch (NumberFormatException e) {
        System.out.println("WARNING: NumberFormatException when parsing value in VariableInfo: " + varinfo);
        continue;
      }
      Sequence news = replaceVarValue(r.sequence, var, intval - 1 , goalBranch);
      if (coversUncovered(news, goalBranch, "prim replace with " + var + " -1")) {
        out.println("SUCCESS! (onevarnum)");
        return new Pair<Branch, Sequence>(goalBranch, news);
      }
    }

    // Try negating.
    {
      out.println("WILL TRY NEGATING " + var);
      StatementKind st = r.sequence.getStatementKind(var.getDeclIndex());
      assert st instanceof PrimitiveOrStringOrNullDecl;
      PrimitiveOrStringOrNullDecl prim = (PrimitiveOrStringOrNullDecl)st;
      assert prim.getType().equals(int.class);
      int value = (Integer) prim.getValue();
      Sequence news = replaceVarValue(r.sequence, var, -value, goalBranch);
      if (coversUncovered(news, goalBranch, "prim negate")) {
        out.println("SUCCESS! (onevarnum)");
        return new Pair<Branch, Sequence>(goalBranch, news);
      }
    }

    // Try setting to 0.
    {
      out.println("WILL TRY SETTING TO ZERO " + var);
      Sequence news = replaceVarValue(r.sequence, var, 0, goalBranch);
      if (coversUncovered(news, goalBranch, "prim set 0")) {
        out.println("SUCCESS! (onevarnum)");
        return new Pair<Branch, Sequence>(goalBranch, news);
      }
    }

    for (int i = 1 ; i <= 10000 ; i = i * 10) {
      // Try adding i, then setting to i.
      {
        {
          out.println("WILL TRY ADDING " + i + " TO " + var);
          StatementKind st = r.sequence.getStatementKind(var.getDeclIndex());
          assert st instanceof PrimitiveOrStringOrNullDecl;
          PrimitiveOrStringOrNullDecl prim = (PrimitiveOrStringOrNullDecl)st;
          assert prim.getType().equals(int.class);
          int value = (Integer) prim.getValue();
          Sequence news = replaceVarValue(r.sequence, var, value + i, goalBranch);
          if (coversUncovered(news, goalBranch, "prim plus " + var )) {
            out.println("SUCCESS! (onevarnum)");
            return new Pair<Branch, Sequence>(goalBranch, news);
          }
        }
        {
          out.println("WILL TRY SETTING " + i + " TO " + var);
          StatementKind st = r.sequence.getStatementKind(var.getDeclIndex());
          assert st instanceof PrimitiveOrStringOrNullDecl;
          PrimitiveOrStringOrNullDecl prim = (PrimitiveOrStringOrNullDecl)st;
          assert prim.getType().equals(int.class);
          Sequence news = replaceVarValue(r.sequence, var, i, goalBranch);
          if (coversUncovered(news, goalBranch, "prim set to " + var)) {
            out.println("SUCCESS! (onevarnum)");
            return new Pair<Branch, Sequence>(goalBranch, news);
          }
        }
      }
    }



    // Try adding 1.
    {
      out.println("WILL TRY SUBTRACTING 1 " + var);
      StatementKind st = r.sequence.getStatementKind(var.getDeclIndex());
      assert st instanceof PrimitiveOrStringOrNullDecl;
      PrimitiveOrStringOrNullDecl prim = (PrimitiveOrStringOrNullDecl)st;
      assert prim.getType().equals(int.class);
      int value = (Integer) prim.getValue();
      Sequence news = replaceVarValue(r.sequence, var, value - 1, goalBranch);
      if (coversUncovered(news, goalBranch, "prim -1")) {
        out.println("SUCCESS! (onevarnum)");
        return new Pair<Branch, Sequence>(goalBranch, news);
      }
    }

    return null;
  }

  // sequence: sequence to modify (goes through branch oppostive goalBranch)
  //
  // Modifies sequence so that instead of "var = old_val", it has "var = val".
  // Executes sequence and checks if goalBranch is covered.
  // If so, returns true, else false.
  private static Sequence replaceVarValue(Sequence sequence, Variable var,
      int val, Branch goalBranch) {
    assert var.sequence == sequence;

    MSequence seq = sequence.toModifiableSequence();

    MVariable mvar = seq.getVariable(var.getDeclIndex());
    StatementKind st = new PrimitiveOrStringOrNullDecl(mvar.getType(), val);
    seq.statements.set(var.getDeclIndex(), new MStatement(st, new ArrayList<MVariable>(), mvar));
    // out.println("@@@" + seq.toCodeString());

    return seq.toImmutableSequence();
  }

  private static boolean coversUncovered(Sequence s, Branch br, String comment) {

    assert uncoveredByRandoop.contains(br);

    ExecutableSequence eseq = new ExecutableSequence(s);
    Set<Branch> coveredBranches = new LinkedHashSet<Branch>();
    Coverage.clearCoverage(covClasses);
    eseq.execute(new DummyVisitor());
    for (CoverageAtom ca : Coverage.getCoveredAtoms(covClasses)) {
      assert ca instanceof Branch;
      coveredBranches.add((Branch)ca);
    }

    coveredByBDGen.addAll(coveredBranches);

    boolean frontierWasCovered = false;

    if (coveredBranches.contains(br)) {
      frontierWasCovered = true;
    }

    // Branch db = Branch.parse("classname=java2.util2.AbstractCollection,methodname=remove,line=254,id=13,direction=true");

    boolean usefulSequence = false;
    for (Branch b : coveredBranches) {
      if (uncoveredByRandoop.contains(b)) {
        usefulSequence = true;
        // if (br.equals(db)) System.out.println(">>" + b);
        break;
      } else {
        // if (br.equals(db)) System.out.println("<<" + b);
      }
    }

    assert (frontierWasCovered ? usefulSequence : true) : br;

    if (usefulSequence) {
      output.println("# " + comment);
      output.println("START SEQUENCE");
      output.println(s.toParseableString());
      output.println("END SEQUENCE");
      out.println();
    }

    return frontierWasCovered;
  }

  private static boolean moveJthBeforeIth(MSequence s, MVariable oldv, MVariable newv) {
    assert oldv.getDeclIndex() < newv.getDeclIndex();

    // Create a new sequence where oldv comes after newv, not before.
    Set<MVariable> newvPreds = getPredecessors(newv);
    if (newvPreds.contains(oldv))
      return false;
    List<MVariable> valuesAfter = valuesAfter(oldv);
    valuesAfter.retainAll(newvPreds);
    // newvPreds has the values that newv depends on that come after v.
    // Those (and newv itself) are the values that we have to move to before oldv.
    List<MVariable> valuesAfter2 = valuesAfter(oldv);
    valuesAfter2.removeAll(newvPreds);

    List<MStatement> newStatements = new ArrayList<MStatement>();
    for (int i = 0 ; i < oldv.getDeclIndex() ; i++) {
      newStatements.add(s.statements.get(i));
    }
    // At this point, add the values from newv.
    for (MVariable v : valuesAfter) {
      newStatements.add(v.getCreatingStatementWithInputs());
    }
    newStatements.add(oldv.getCreatingStatementWithInputs());
    for (MVariable v : valuesAfter2) {
      newStatements.add(v.getCreatingStatementWithInputs());
    }

    s.statements = newStatements;
    return true;
  }

  private static List<MVariable> valuesAfter(MVariable oldv) {
    List<MVariable> valuesAfter = new ArrayList<MVariable>();
    for (int i = oldv.getDeclIndex() + 1 ; i < oldv.owner.size() ; i++) {
      valuesAfter.add(oldv.owner.getVariable(i));
    }
    return valuesAfter;
  }

  /**
   * Returns a set that includes v and any variables that are
   * inputs to its declaring statement, recursively.
   */
  private static Set<MVariable> getPredecessors(MVariable v) {
    if (v == null)
      throw new IllegalArgumentException();
    List<MVariable> inputs = v.getCreatingStatementWithInputs().inputs;
    Set<MVariable> inputsSet = new LinkedHashSet<MVariable>(inputs);
    inputsSet.add(v);
    if (inputs.isEmpty())
      return inputsSet;
    for (MVariable input : inputs) {
      inputsSet.addAll(getPredecessors(input));
    }
    // Sanity check: all values belong to same sequence.
    MSequence owner = v.owner;
    for (MVariable input : inputsSet)
      assert input.owner == owner;
    return inputsSet;
  }


}
