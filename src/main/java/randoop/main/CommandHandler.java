package randoop.main;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.plumelib.options.Options;
import randoop.Globals;
import randoop.condition.RandoopSpecificationError;
import randoop.util.Util;

/**
 * A command is the first argument given to Randoop. A command handler handles one command. It also
 * takes care of printing the command's documentation.
 */
public abstract class CommandHandler {

  /** The name of the command, which is the first argument given to Randoop. */
  public String fcommand;

  /** A one-line description of the command. */
  public String fpitch;

  /** The usage of the command, shown in its documentation. */
  public String fcommandGrammar;

  /** Constraints on the command-line arguments, shown in the command's documentation. */
  public String fwhere;

  /** A summary of what the command does. */
  public String fsummary;

  /** Notes about the command, shown in its documentation. */
  public List<String> fnotes;

  /** A description of the command's input. */
  public String finput;

  /** A description of the command's output. */
  public String foutput;

  /** An example invocation of the command. */
  public String fexample;

  /** The command-line options that the command accepts. */
  public Options foptions;

  /**
   * Creates a CommandHandler. Every argument other than {@code command} and {@code options} may be
   * null, which means that the information is undocumented.
   *
   * @param command the name of the command
   * @param pitch the description of the command
   * @param commandGrammar the usage of the command
   * @param where the where
   * @param summary the summary of the command
   * @param notes the notes for the command
   * @param input the input
   * @param output the output
   * @param example the usage example
   * @param options the command line arguments
   */
  @SuppressWarnings("PMD.ExcessiveParameterList")
  protected CommandHandler(
      String command,
      String pitch,
      String commandGrammar,
      String where,
      String summary,
      List<String> notes,
      String input,
      String output,
      String example,
      Options options) {

    if ((command == null)) {
      throw new IllegalArgumentException("command cannot be null.");
    }
    if ((options == null)) {
      throw new IllegalArgumentException("options cannot be null.");
    }

    this.fcommand = command;
    this.fpitch = pitch == null ? "undocumented" : pitch;
    this.fcommandGrammar = commandGrammar == null ? "undocumented" : commandGrammar;
    this.fwhere = where == null ? "undocumented" : where;
    this.fsummary = summary == null ? "undocumented" : summary;
    this.fnotes = notes == null ? new ArrayList<>(0) : notes;
    this.finput = input == null ? "undocumented" : input;
    this.foutput = output == null ? "undocumented" : output;
    this.fexample = example == null ? "undocumented" : example;
    this.foptions = options;
  }

  /**
   * Returns true if this CommandHandler handles the given command.
   *
   * @param command a command
   * @return true if this CommandHandler handles the given command
   */
  public final boolean handles(String command) {
    return command != null
        && command
            .toUpperCase(Locale.getDefault())
            .equals(fcommand.toUpperCase(Locale.getDefault()));
  }

  /**
   * Executes the command with the given arguments.
   *
   * @param args the command-line arguments, not including the command itself
   * @return true if the command succeeded
   * @throws RandoopClassNameError if a class named by an argument cannot be loaded
   * @throws RandoopSpecificationError if a specification is malformed or cannot be evaluated
   */
  public abstract boolean handle(String[] args)
      throws RandoopClassNameError, RandoopSpecificationError;

  /**
   * Prints out formatted text in (google code) Wiki format.
   *
   * @param out the output stream for printing html formatted usage
   */
  public final void printHTML(PrintStream out) {

    out.println("=== " + fcommand + " ===");

    if (!fcommandGrammar.trim().equals("")) {
      out.println("*Usage:*");
      out.println();
      out.println("{{{");
      out.println("java randoop.main.Main " + fcommandGrammar);
      out.println("}}}");
      out.println();
    }
    if (!fwhere.trim().equals("")) {
      out.println("_Where_:");
      out.println();
      out.println(fwhere);
      out.println();
    }
    if (!fsummary.trim().equals("")) {
      out.println("*Summary:*");
      out.println();
      out.println(fsummary);
      out.println();
    }
    if (!finput.trim().equals("")) {
      out.println("*Input:*");
      out.println();
      out.println(finput);
      out.println();
    }
    if (!foutput.trim().equals("")) {
      out.println("*Output:*");
      out.println();
      out.println(foutput);
      out.println();
    }
    if (!fexample.trim().equals("")) {
      out.println("*Example use:*");
      out.println("{{{");
      out.println(fexample);
      out.println("}}}");
      out.println();
    }
    if (fnotes != null && !fnotes.isEmpty()) {
      out.println("*Notes:*");
      out.println();
      for (String note : fnotes) {
        out.println("  * " + note);
      }
    }
  }

  /**
   * Prints a plain-text usage message for the command, including its documentation and options.
   *
   * @param out the output stream for printing the usage message
   */
  public final void usageMessage(PrintStream out) {

    out.println();

    if (!fcommand.trim().equals("")) {
      out.print(
          Util.hangingParagraph("COMMAND: " + fcommand, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (!fcommandGrammar.trim().equals("")) {
      out.print(
          Util.hangingParagraph(
              "Usage: " + fcommandGrammar, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (!fwhere.trim().equals("")) {
      out.print(Util.hangingParagraph("Where: " + fwhere, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (!fsummary.trim().equals("")) {
      out.print(
          Util.hangingParagraph("Summary: " + fsummary, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (!finput.trim().equals("")) {
      out.print(Util.hangingParagraph("Input: " + finput, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (!foutput.trim().equals("")) {
      out.print(Util.hangingParagraph("Output: " + foutput, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (!fexample.trim().equals("")) {
      out.print(
          Util.hangingParagraph("Example: " + fexample, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (fnotes != null && !fnotes.isEmpty()) {
      out.println("Notes:");
      out.println();
      for (int i = 0; i < fnotes.size(); i++) {
        String note = fnotes.get(i);
        out.println(
            Util.hangingParagraph(
                Integer.toString(i + 1) + ". " + note, Globals.COLWIDTH, Globals.INDENTWIDTH));
      }
    }
    out.println();
    out.println("OPTIONS:");
    out.println();
    out.println(foptions.usage(false));
  }
}
