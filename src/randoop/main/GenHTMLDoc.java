package randoop.main;

import java.io.PrintStream;

import plume.Options;

public class GenHTMLDoc extends CommandHandler {

  private static final String command = "html-help";

  private static final String pitch = "Outputs HTML for all the commands (used to create the Randoop manual).";

  private static final String commandGrammar = "html-help";

  private static final String where = "";

  private static final String summary = "Outputs HTML for all the commands (used to create the Randoop manual). "
    + "The output goes to files randoop_commands.php and randoop_commands_list.php";

  private static final String input = "None.";

  private static final String output = "A bunch of HTML is printed to stdout.";

  private static final String example = "";

  private static Options options = null; 

  public GenHTMLDoc() {
    super(command, pitch, commandGrammar, where, summary, null, input, output, example, options);
  }

  @Override
  public boolean handle(String[] args) throws RandoopTextuiException {

    if (args.length > 0) {
      throw new RandoopTextuiException("html-help takes no arguments.");
    }

    PrintStream eclat_commands = null;
    PrintStream eclat_commands_list = null;

    try {
      eclat_commands = new PrintStream("randoop_commands.php");
      eclat_commands_list = new PrintStream("randoop_commands_list.php");
    } catch (Exception e) {
      throw new Error(e);
    }

    // User wants help on a specific command.
    for (CommandHandler h : Main.handlers) {

      h.printHTML(eclat_commands);
      h.printHTMLMenuItem(eclat_commands_list);
    }

    eclat_commands.close();
    eclat_commands_list.close();

    return true;
  }

}
