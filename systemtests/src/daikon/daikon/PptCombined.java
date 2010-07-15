package daikon;

import java.util.*;

/**
 * A program point which consists of a number of program points.  Invariants
 * are looked for over all combinations of variables from all of the program
 * points that make up the combined ppt.
 */
public class PptCombined extends PptTopLevel {

  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20071129L;

  List<PptTopLevel> ppts;

  public PptCombined (List<PptTopLevel> ppts) {
    super ("combined_" + ppts.get(0).name(), new VarInfo[0]);
    this.ppts = ppts;
  }

  /** Returns a name basic on its constituent ppts **/
  public String name() {
    String name = ppts.get(0).name();
    name += ".." + ppts.get(ppts.size()-1).ppt_name.name();
    return name;
  }

  /**
   * Creates combined program points that cover multiple basic
   * blocks.  Each basic block ppt is combined with any basic blocks
   * that dominate it (always occur before it).
   *
   * The input is a list of the basic block ppts that make up the
   * function.  Each bb ppt contains a list of the names of all of the
   * basic blocks that directly succeed it.  That list is used to
   * calculate the dominators.
   *
   * Each program point in the function is modified as follows: <ul>
   *   <li> Its combined_ppts_init flag is set to true
   *   <li> Its combined_ppt field is set to point to the combined
   *    program point that should be processed when this bb ppt is
   *    executed.   This field may be null if this bb ppt is completely
   *    subsumed by other combined ppts
   *    <li> Its combined_subsumed boolean field is set to true if this
   *    ppt is subsumed by a combine dprogram point, false otherwise.
   *
   * The current implementation is just an example that creates a combined
   * program point for each program point with exactly one successor
   */
  public static void combine_func_ppts (PptMap all_ppts,
                                        List<PptTopLevel> func_ppts) {

    // Loop through each basic block ppt in the function
    for (PptTopLevel ppt : func_ppts) {

      // Mark this ppt as initialized
      ppt.combined_ppts_init = true;

      // Skip any ppt that doesn't have exactly one successor
      if ((ppt.ppt_successors == null) || (ppt.ppt_successors.size() != 1))
        continue;

      // Get the successor ppt
      String succ = ppt.ppt_successors.get(0);
      PptTopLevel succ_ppt = all_ppts.get (succ);

      // If the successor already has a combined ppt, skip it
      if (succ_ppt.combined_ppt != null)
        continue;

      // Build the combined program point that includes this ppt and its
      // successor
      List<PptTopLevel> combined_ppts = new ArrayList<PptTopLevel>();
      combined_ppts.add (ppt);
      combined_ppts.add (succ_ppt);
      succ_ppt.combined_ppt = new PptCombined (combined_ppts);

      // Presume that the new combined ppt subsumes both of these ppts
      ppt.combined_subsumed = true;
      succ_ppt.combined_subsumed = true;
    }
  }

}
