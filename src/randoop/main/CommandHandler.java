package randoop.main;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import randoop.Globals;
import randoop.util.Util;
import plume.Options;


/**
 * A command is the first argument given to eclat.  A command handler
 * handles one command. It also takes care of printing the command's
 * documentation.
 */
public abstract class CommandHandler {

  public String fcommand;
  public String fpitch;
  public String fcommandGrammar;
  public String fwhere;
  public String fsummary;
  public List<String> fnotes;
  public String finput;
  public String foutput;
  public String fexample;
  public Options foptions;

  /**
   * No arguments should be null.
   */
  public CommandHandler(String command,
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

    this.fcommand = command;
    this.fpitch = pitch == null ? "undocumented" : pitch;
    this.fcommandGrammar = commandGrammar == null ? "undocumented" : commandGrammar;
    this.fwhere = where == null ? "undocumented" : where;
    this.fsummary = summary == null ? "undocumented" : summary;
    this.fnotes = notes == null ? new ArrayList<String>() : notes;
    this.finput = input == null ? "undocumented" : input;
    this.foutput = output == null ? "undocumented" : output;
    this.fexample = example == null ? "undocumented" : example;
    this.foptions = options;

  }

  public final boolean handles(String command) {
    if (command == null) {
      return false;
    }
    if (command.toUpperCase().equals(fcommand.toUpperCase())) {
      return true;
    } else {
      return false;
    }
  }

  public abstract boolean handle(String[] args) throws RandoopTextuiException;

  public final void printHTMLMenuItem(PrintStream out) {
    out.println("<li><tt><a href=\"#" + fcommand + "\">" + fcommand + "</a></tt>");
    out.println("<br>" + fpitch);
  }

  /**
   * Prints out formatted text, putting the command name as an "<h4>" element.
   */
  public final void printHTML(PrintStream out) {

    out.println("<h2>");
    out.println("<a name=\"" + fcommand + "\">");
    out.println(fcommand);
    out.println("</a></h2>");

    //out.println("<blockquote>");
    if (!fcommandGrammar.trim().equals("")) {
      out.println("<b>Usage: </b>");
      out.println("<tt>");
      for (String line : fcommandGrammar.split(System.getProperty("line.separator"))) {
        out.println(line);
        out.println("<br>");
      }
      out.println("</tt>");
      out.println("<p>");
    }
    if (!fwhere.trim().equals("")) {
      out.println("<b>Where:</b> ");
      for (String line : fwhere.split(System.getProperty("line.separator"))) {
        out.println(line);
        out.println("<br>");
      }
      out.println("<p>");
    }
    if (!fsummary.trim().equals("")) {
      out.println("<b>Summary.</b>");
      for (String line : fsummary.split(System.getProperty("line.separator"))) {
        out.println(line);
        out.println("<br>");
      }
      out.println("<p>");
    }
    if (!finput.trim().equals("")) {
      out.println("<b>Input: </b>");
      for (String line : finput.split(System.getProperty("line.separator"))) {
        out.println(line);
        out.println("<br>");
      }
      out.println("<p>");
    }
    if (!foutput.trim().equals("")) {
      out.println("<b>Output: </b>");
      for (String line : foutput.split(System.getProperty("line.separator"))) {
        out.println(line);
        out.println("<br>");
      }
      out.println("<p>");
    }
    if (!fexample.trim().equals("")) {
      out.println("<b>Example use:</b>");
      out.println("<br>");


      out.println("<div class=\"code\"><pre>");
      for (String line : fexample.split(System.getProperty("line.separator"))) {
        out.println(line);
      }
      out.println("</pre></div>");
      out.println("<p>");
    }
    if (fnotes != null && fnotes.size() > 0) {
      out.println("<b>Notes.</b><ul>");
      for (String note : fnotes) {
        out.println("<li>");
        out.println(note);
      }
      out.println("</ul><p>");
    }
// Comment out temporarily.  Uses non-documented "toStringHTML" routine in Options.
//     if (foptions != null) {
//       out.println("<b>Options</b>");
//       out.println("<p>");
//       foptions.toStringHTML(out);
//     }
    //out.println("</blockquote>");
  }

  public final void usageMessage(PrintStream out) {

    out.println();

    if (!fcommand.trim().equals("")) {
      out.print(Util.hangingParagraph("COMMAND: " +  fcommand, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (!fcommandGrammar.trim().equals("")) {
      out.print(Util.hangingParagraph("Usage: " +  fcommandGrammar, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (!fwhere.trim().equals("")) {
      out.print(Util.hangingParagraph("Where: " +  fwhere, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (!fsummary.trim().equals("")) {
      out.print(Util.hangingParagraph("Summary: " +  fsummary, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (!finput.trim().equals("")) {
      out.print(Util.hangingParagraph("Input: " +  finput, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (!foutput.trim().equals("")) {
      out.print(Util.hangingParagraph("Output: " +  foutput, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (!fexample.trim().equals("")) {
      out.print(Util.hangingParagraph("Example: " +  fexample, Globals.COLWIDTH, Globals.INDENTWIDTH));
    }
    out.println();
    if (fnotes != null && fnotes.size() > 0) {
      out.println("Notes:");
      out.println();
      for (int i = 0 ; i < fnotes.size() ; i++) {
        String note = fnotes.get(i);
        out.println(Util.hangingParagraph(Integer.toString(i+1) + ". " + note, Globals.COLWIDTH, Globals.INDENTWIDTH));
      }
    }
    out.println();
    if (foptions != null) {
      out.println("OPTIONS:");
      out.println();
      for (String use : foptions.usage()) {
        if (!use.trim().equals("")) {
          out.print(Util.hangingParagraph(use, Globals.COLWIDTH, Globals.INDENTWIDTH));
          out.println();
        }
      }
    }
  }


}
