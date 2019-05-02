package randoop.main;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.plumelib.options.Options;
import org.plumelib.options.Options.ArgException;
import randoop.Globals;
import randoop.util.Util;

public class Help extends CommandHandler {

  public static PrintStream out = System.out;

  public Help() {
    super(
        "help",
        "Displays a help message for a given command.",
        "help",
        "",
        "Displays a help message for a given command.",
        null,
        "None (for the general help message), or the name of a command (for command-specific help).",
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

      try {
        introMessage(out);
      } catch (Exception e) {
        System.out.println("Error while reading file containing Randoop's version number.");
        System.out.println(e.getMessage());
        System.exit(1);
      }
      out.println();
      out.println("Type `help' followed by a command name to see documentation.");
      out.println();
      out.println();
      out.println("Commands:");
      out.println();
      for (CommandHandler h : Main.handlers) {
        out.println(
            Util.hangingParagraph(
                h.fcommand + " -- " + h.fpitch, Globals.COLWIDTH, Globals.INDENTWIDTH));
      }
      out.println();

    } else {

      if (args.length != 1) {
        throw new RandoopUsageError(
            String.format(
                "The `help' command must be followed by exactly one argument:%n"
                    + "the command that you want help on.%n"
                    + "For a list of commands, invoke Randoop with argument: help"));
      }

      String command = args[0];

      // User wants help on a specific command.
      List<CommandHandler> allHandlers = new ArrayList<>();
      allHandlers.addAll(Main.handlers);

      for (CommandHandler h : allHandlers) {

        if (h.fcommand.equals(command)) {
          h.usageMessage(out);
          return true;
        }
      }
      throw new RandoopUsageError("The command you asked help for was not recognized: " + command);
    }

    return true;
  }

  protected static void introMessage(PrintStream out) {
    out.println("Randoop for Java version " + Globals.getRandoopVersion() + ".");
    out.println();
    out.println("Randoop is a command-line tool that creates unit tests for Java.");
    out.println("It accepts one of the commands listed below. For the user manual,");
    out.println("please visit https://randoop.github.io/randoop/manual/index.html");
  }
}
