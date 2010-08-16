package randoop.experiments;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import plume.Option;
import plume.Options;
import plume.Options.ArgException;


/**
 * MIT-specific! Will probably not work outside CSAIL.
 * 
 * A program that issues a list of make targets, possibly across
 * multiple machines (via ssh).
 */
public class MultiMachineRunner {

  protected TargetMaker targetMaker;

  protected ArrayList<String> targetsInProgress = new ArrayList<String>();

  protected ArrayList<String> targetsFinished = new ArrayList<String>();

  protected ArrayList<String> targetsEndedWithError = new ArrayList<String>();

  @Option("Output stdout of each command to <target>.output file")
  public static boolean stdout_to_file = false;

  protected static List<String> machines;

  protected static String RANDOOP_HOME;

  protected static String RANDOOP_MACHINES;

  @SuppressWarnings("unchecked")
  public static void main(String[] args2) throws ArgException {

    // Get RANDOOP_HOME variable.
    RANDOOP_HOME = System.getProperty("RANDOOP_HOME");
    if (RANDOOP_HOME == null) {
      RANDOOP_HOME = System.getenv("RANDOOP_HOME");
      if (RANDOOP_HOME == null) {
        System.out.println("The environment variable RANDOOP_HOME must be defined.");
        System.exit(1);
      }
    }

    // Create machine spawner. The spawner maintains the list
    // of targets (in progress and finished) that individual
    // machine managers access to determine what target to
    // perform next. It also handles the user interaction loop.
    MultiMachineRunner spawner = new MultiMachineRunner();

    // Create machine manages. Each manages is reponsible for
    // issuing make targets on a specific machine. If no machines
    // are specified in RANDOOP_MACHINES variable, the only machine
    // manager created is for the local machine.
    List<MachineManager> managers = new ArrayList<MachineManager>();
    RANDOOP_MACHINES = System.getProperty("RANDOOP_MACHINES");
    if (RANDOOP_MACHINES == null) {
      if (RANDOOP_MACHINES ==null) {
        RANDOOP_MACHINES = System.getenv("RANDOOP_MACHINES");
        if (RANDOOP_MACHINES == null) {
          managers.add(new MachineManager("local", spawner, stdout_to_file));
        }
      }
    } else {
      if (RANDOOP_MACHINES.length() == 0) {
        System.out.println("Error: Found empty RANDOOP_MACHINES variable.");
        System.exit(1);
      }
      String[] machines = RANDOOP_MACHINES.split(",");
      for (String machineName : machines) {
        if (machineName.length() == 0) {
          System.out.println("Error: Found empty machine name in RANDOOP_MACHINES.");
          System.exit(1);
        }
        System.out.println("Adding machine: " + machineName);
        managers.add(new MachineManager(machineName, spawner, stdout_to_file));
      }
    }

    // Read options.
    Options options = new Options(MultiMachineRunner.class);
    String[] args = options.parse(args2);

    // Create target maker. The target maker is used to populate
    // the list of targets in the spawner. It is specified via its class
    // name when invoking this program.
    if (args.length == 0)
      throw new IllegalArgumentException("Missing required argument.");

    String targetMakerName = args[0];
    String[] targetMakerArgs = new String[args.length-1];
    for (int i = 1 ; i < args.length ; i++) targetMakerArgs[i - 1] = args[i];

    Class<TargetMaker> targetMakerClass = null;
    try {
      targetMakerClass = (Class<TargetMaker>) Class.forName(targetMakerName);
    } catch (ClassNotFoundException e1) {
      System.out.println("Did not find target maker class: " + targetMakerName);
      System.exit(1);
    }
    try {
      spawner.targetMaker = targetMakerClass.getConstructor(String[].class).newInstance(new Object[]{targetMakerArgs});
    } catch (Exception e1) {
      System.out.println("Error while creating instance of target maker class: " + e1.getMessage());
      System.out.println("Stack trace: ");
      e1.printStackTrace(System.out);
      System.exit(1);
    }

    // Start the machine managers.
    for (MachineManager m : managers) {
      m.start();
    }

    // Start the user interaction loop.
    spawner.startUserInteractionLoop(managers);
  }

  /**
   * @return The name of the next (undone) target in the list. Returns
   * null if there are no more undone targets.
   */
  public synchronized String nextTarget() {

    if (targetMaker.hasMoreTargets()) {
      String ret = targetMaker.getNextTarget();
      targetsInProgress.add(ret);
      return ret;
    }

    // No experiments found for machine.
    return null;
  }

  public synchronized void markTargetDone(String e) {
    assert targetsInProgress.contains(e);
    targetsInProgress.remove(e);
    targetsFinished.add(e);
    checkIfDone();
  }

  public synchronized void markTargetEndedWithError(String e) {
    assert targetsInProgress.contains(e);
    targetsInProgress.remove(e);
    targetsEndedWithError.add(e);
    checkIfDone();
  }

  private synchronized void checkIfDone() {
    // This is the exit point of the program.
    if (targetsInProgress.size() == 0
        && !targetMaker.hasMoreTargets()) {
      System.out.println("No more experiments left.");
      if (!targetsEndedWithError.isEmpty()) {
        System.out.println("Some targets failed. Will exit with code 1.");
        System.exit(1);
      } else {
        System.out.println("All targets succeeded. Will exit with code 0.");
        System.exit(0);
      }
    }

  }

  public void startUserInteractionLoop(List<MachineManager> managers) {

    try {

      BufferedReader console =
        new BufferedReader ( new InputStreamReader (System.in));

      System.out.println("Enter a command (e.g. \"help\"):");

      String line = console.readLine();

      for (;;) {

        if (line.toUpperCase().equals("HELP")) {
          System.out.println("p -- print progress");
          System.out.println("machines -- list machines in use");
          System.out.println("kill <machine> -- kill machine's experiment (add experiment back to experiments), and swap it out");
          System.out.println("killall -- kill all experiments and machines.");
          System.out.println("remove <machine> -- swap out machine after it finishes its current experiment");
          System.out.println("add <machine> -- swap in machine");
          System.out.println("show <machine> -- print the stdout and stderr output of machine");
        }

        if (line.toUpperCase().equals("P")) {

          System.out.println("To do: " + targetMaker.targetsLeft());
          System.out.println("In progress: " + targetsInProgress.size());
          System.out.println("Done: " + targetsFinished.size());
          System.out.println("Terminated with error (will not retry): " + targetsEndedWithError.size());

        } else if (line.toUpperCase().startsWith("KILLALL")) {
            for (int i = 0 ; i < managers.size() ; i++) {
              MachineManager m = managers.get(i);
              m.fswapOut = true;
              killJavaProcesses(m.machine);
              managers.remove(i);
            }
        } else if (line.toUpperCase().startsWith("KILL")) {
          String[] split = line.split("\\s");
          if (split.length != 2) {
            System.out.println("Wrong command syntax.");
          } else {
            String machine = split[1].trim();
            boolean found = false;
            for (int i = 0 ; i < managers.size() ; i++) {
              MachineManager m = managers.get(i);
              if (m.machine.equals(machine)) {
                found = true;
                if (machine.equals("local")) {
                  System.out.println("This command will attempt to kill all Java processes"
                      + " in your local machine! Proceed? ");
                  String answer = console.readLine();
                  if (answer.toUpperCase().trim().equals("YES"))
                    break;
                }
                m.fswapOut = true;
                killJavaProcesses(m.machine);
                managers.remove(i);
                break;
              }
            }
            if (! found) {
              System.out.println("Machine " + machine + " not found.");
            }
          }
        } else if (line.toUpperCase().startsWith("MACHINES")) {

          System.out.print("Machines in use: ");
          for (MachineManager m : managers) {
            System.out.println(m.machine + "\t target: " + m.currentTarget);
          }
          System.out.println();

        } else if (line.toUpperCase().startsWith("REMOVE")) {
          String[] split = line.split("\\s");
          if (split.length != 2) {
            System.out.println("Wrong command syntax.");
          } else {
            String machine = split[1].trim();
            boolean found = false;
            for (int i = 0 ; i < managers.size() ; i++) {
              MachineManager m = managers.get(i);
              if (m.machine.equals(machine)) {
                found = true;
                System.out.println("Removing " + machine + " from list of machines.");
                m.fswapOut = true;
                managers.remove(i);
                break;
              }
            }
            if (! found) {
              System.out.println("Machine " + machine + " not found.");
            }
          }
        } else if (line.toUpperCase().startsWith("ADD")) {
          String[] split = line.split("\\s");
          if (split.length != 2) {
            System.out.println("Wrong command syntax.");
          } else {
            String machine = split[1].trim();
            // Add a new manager
            MachineManager m = new MachineManager(machine, this, stdout_to_file);
            m.start();
            managers.add(m);
          }

        } else if (line.toUpperCase().startsWith("SHOW")) {

          String[] split = line.split("\\s");
          if (split.length != 2) {
            System.out.println("Wrong command syntax.");
          } else {
            String machine = split[1].trim();
            boolean found = false;
            for (int i = 0 ; i < managers.size() ; i++) {
              MachineManager m = managers.get(i);
              if (m.machine.equals(machine)) {
                found = true;
                List<String> command = new ArrayList<String>();
                try {
                  ByteArrayOutputStream out = new ByteArrayOutputStream();
                  PrintStream str = new PrintStream(out);
                  command.add("cat");
                  command.add(m.tmp.getAbsolutePath());
                  Command.exec(command.toArray(new String[0]), str, str,  "", false);
                  System.out.println(out.toString());
                } catch (Exception e) {
                  System.out.print("EXCEPTION " + e.getClass() +
                    " while running command: ");
                  System.out.println(command.toString());
                  System.out.println("Exception message: " + e.getMessage());
                }
                break;
              }
            }
            if (! found) {
              System.out.println("Machine " + machine + " not found.");
            }
          }

        } else {
          System.out.println("Unrecognized command.");
        }

        System.out.println("Enter a command (e.g. \"help\"):");
        line = console.readLine();
      }

    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("IOException from reading console, exception=" + e);
    }

  }

  private static void killJavaProcesses(String machine) {

    String user = System.getProperty("user.name");

    if (user == null) {
      System.out.println("Could not find System property user.name. Cannot kill " +
      "any processes.");
    }

    System.out.println("Will kill all java/javac processes for user "
        + user + " in machine " + machine);

    List<String> command = new ArrayList<String>();
    command.add("ssh");
    command.add(machine);
    command.add("ps");
    command.add("-U");
    command.add(user);

    ByteArrayOutputStream out = null;
    int exitFlag = 0;
    out = new ByteArrayOutputStream();
    PrintStream str = new PrintStream(out);
    exitFlag = Command.exec(command.toArray(new String[0]), str, str, "", false);
    if (exitFlag == 0) {
      List<String> processesToKill = new ArrayList<String>();
      String[] psOutputLines = out.toString().split(randoop.Globals.lineSep);
      for (String s : psOutputLines) {
        if (s.endsWith("java") || s.endsWith("javac")) {
          String[] tokens = s.trim().split("\\s");
          String processNumber = tokens[0].trim();
          try {
            Long.parseLong(processNumber);
          } catch (NumberFormatException e) {
            System.out.println("Process number \"" +
                processNumber + "\" not a number in line: " + s);
            System.out.println("Aborting kill.");
            return;
          }
          processesToKill.add(processNumber);
        }
      }

      System.out.println("Processes to kill: " + processesToKill);

      command = new ArrayList<String>();
      command.add("ssh");
      command.add(machine);
      command.add("kill");
      command.add("-9");
      for (String processNum : processesToKill) {
        command.add(processNum);
      }

      Command.exec(command.toArray(new String[0]), System.out, System.err, "KILL>", false);

    } else {
      System.out.println("ps command failed. Aborting kill. Output of command:");
      System.out.println(out.toString());
      return;
    }
  }

}
