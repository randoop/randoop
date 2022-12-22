package randoop.main;

import java.util.Arrays;
import org.plumelib.options.Options;
import org.plumelib.options.Options.ArgException;
import randoop.Globals;
import randoop.util.Util;

/** The "help" command. */
public class Help extends CommandHandler {

  /** Create the "help" command. */
  public Help() {
    super(
        "help",
        "Displays a help message for a given command.",
        "help",
        "",
        "Displays a help message for a given command.",
        null,
        "None (for the general help message),"
            + " or the name of a command (for command-specific help).",
        "A help message is printed to stdout.",
        "",
        new Options(Help.class));
  }

  @Override
  public boolean handle(String[] argsWithOptions) {

    String[] args = null;
    try {
      args = foptions.parse(argsWithOptions);
      if (args.length > 1) {
        throw new ArgException("Unrecognized arguments: " + Arrays.toString(args));
      }
    } catch (ArgException ae) {
      System.out.println("Error while parsing command-line arguments: " + ae.getMessage());
      System.exit(1);
    }

    if (args.length == 0) {

      System.out.println("Randoop for Java version " + Globals.getRandoopVersion() + ".");
      System.out.println("Type `help' followed by a command name to see documentation.");
      System.out.println("Commands:");
      for (CommandHandler h : Main.handlers) {
        System.out.println(
            Util.hangingParagraph(
                h.fcommand + " -- " + h.fpitch, Globals.COLWIDTH, Globals.INDENTWIDTH));
      }
      System.out.println();

    } else {

      if (args.length != 1) {
        throw new RandoopUsageError(
            String.format(
                "The `help' command must be followed by exactly one argument:%n"
                    + "the command that you want help on.%n"
                    + "For a list of commands, invoke Randoop with argument: help"));
      }

      // User wants help on a specific command.
      String command = args[0];

      for (CommandHandler h : Main.handlers) {

        if (h.fcommand.equals(command)) {
          h.usageMessage(System.out);
          return true;
        }
      }
      throw new RandoopUsageError("The command you asked help for was not recognized: " + command);
    }

    return true;
  }
}
