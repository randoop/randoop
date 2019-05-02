package randoop.main;

import java.util.ArrayList;
import java.util.List;
import randoop.Globals;
import randoop.condition.RandoopSpecificationError;
import randoop.generation.AbstractGenerator;
import randoop.sequence.Sequence;

/**
 * Main entry point for Randoop. Asks the command handlers who can handle the command given by the
 * user, and passes control to whoever does.
 */
public class Main {

  // Handlers for user-visible commands.
  public static List<CommandHandler> handlers;

  static {
    handlers = new ArrayList<>();
    handlers.add(new GenTests());
    handlers.add(new Help());
    handlers.add(new Minimize());
  }

  // The main method simply calls nonStaticMain.
  public static void main(String[] args) {

    Main main = new Main();
    main.nonStaticMain(args);
    System.exit(0);
  }

  // The real entry point of Main.
  public void nonStaticMain(String[] args) {

    if (args.length == 0) args = new String[] {"help"};

    String command = args[0];
    String[] args2 = new String[args.length - 1];
    for (int i = 1; i < args.length; i++) {
      args2[i - 1] = args[i];
    }

    // Figure out which handler handles this command.
    CommandHandler handler = null;
    List<CommandHandler> allHandlers = new ArrayList<>();
    allHandlers.addAll(handlers);
    for (CommandHandler h : allHandlers) {
      if (h.handles(command)) {
        handler = h;
        break;
      }
    }

    // If there was no handler for the command, print error message and exit.
    if (handler == null) {
      System.out.println("Unrecognized command: " + command + ".");
      System.out.println("For more help, invoke Randoop with \"help\" as its sole argument.");
      System.exit(1);
    }

    boolean success = false;
    try {

      success = handler.handle(args2);

      if (!success) {
        System.err.println("The Randoop command " + handler.fcommand + " failed.");
      }

    } catch (RandoopUsageError e) {

      System.out.println();
      if (e.getMessage() != null) {
        System.out.println(e.getMessage());
      }
      if (e instanceof RandoopCommandError) {
        System.out.println(
            "To get help on this command, invoke Randoop with arguments: help " + handler.fcommand);
      }
      System.exit(1);

    } catch (RandoopSpecificationError e) {

      System.out.println();
      if (e.getMessage() != null) {
        System.out.println(e.getMessage());
      }
      System.exit(1);

    } catch (RandoopBug e) {
      System.out.println();
      System.out.println("Randoop failed in an unexpected way.");
      System.out.println("Please report at https://github.com/randoop/randoop/issues ,");
      System.out.println(
          "providing the information requested at https://randoop.github.io/randoop/manual/index.html#bug-reporting .");

      // Calls to flush() do not untangle System.out and System.err;
      // probably an OS issue, not Java.  So we send printStackTrace()
      // to System.out not System.err.
      e.printStackTrace(System.out);
      System.exit(1);
    } catch (Throwable e) {

      System.out.println();
      System.out.println("Throwable thrown while handling command: " + e);
      e.printStackTrace(System.out);
      success = false;

    } finally {

      if (!success) {
        System.out.println();
        System.out.println("Randoop failed.");
        Sequence lastSequence = AbstractGenerator.currSeq;
        if (lastSequence == null) {
          System.out.println("No sequences generated.");
        } else {
          System.out.println("Last sequence under execution: ");
          String[] lines = lastSequence.toString().split(Globals.lineSep);
          for (String line : lines) {
            System.out.println(line);
          }
        }
        System.exit(1);
      }
    }
  }
}
