package randoop.experiments;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


/**
 * A thread that issues make targets on a specified machine. The manager
 * enters a loop. At each iteration, it asks its spawner for the next
 * target to execute. If there are no more targets, the thread dies.
 *
 * If there is a target and this manager's machine name is "local",
 * the manager issues the command
 *
 *   make -C $RANDOOP_HOME/systemtests <target>
 *
 * If the machine name is other than "local", the target issues the
 * command
 *
 *   ssh <machine> make -C $RANDOOP_HOME/systemtests <target>
 */
public class MachineManager extends Thread {

  String machine;

  private MultiMachineRunner spawner;

  protected Process fproc;

  protected boolean fswapOut = false;

  protected File tmp = null;

  protected String currentTarget = "<none>";

  private boolean stdout_to_file;

  public MachineManager(String machine, MultiMachineRunner spawner, boolean stdout_to_file) {
    this.machine = machine;
    this.spawner = spawner;
    this.stdout_to_file = stdout_to_file;
  }

  @Override
  public void run() {
    for (;;) {
      if (fswapOut) {
        break;
      }
      String e = spawner.nextTarget();
      if (e == null) {
        break;
      } else {

        currentTarget = e;

        List<String> command = new ArrayList<String>();
        if (!machine.equals("local")) {
          command.add("ssh");
          command.add(machine);
        }
        command.add("make");
        command.add("-C");
        command.add(MultiMachineRunner.RANDOOP_HOME
            + File.separator
            + "systemtests");
        command.add(e);

        fproc = null;
        int exitVariable = 0;
        // try {
        System.out.println("Machine " + machine + " starting experiment " + e);
        System.out.println("Executing command:" + e);

        tmp = null;
        PrintStream str = null;
        FileOutputStream out = null;
        try {
          tmp = File.createTempFile("randoop", null, new File(System.getProperty("user.dir")));
          out = new FileOutputStream(tmp);
          str = new PrintStream(out);
        } catch (Exception ex) {
          throw new Error(ex);
        }
        exitVariable = Command.exec(command.toArray(new String[0]), str, str,  "", false);

        try {
          str.close();
          out.close();
          if (exitVariable == 0) {
            // tmp.delete(); // Succeeded executing target; no need to keep around output.
          }
        } catch (Exception ex) {
          throw new Error(ex);
        }

        // Copy output (in tmp file) to <target>.output
        if (stdout_to_file) {
          try {
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            PrintStream str2 = new PrintStream(out2);
            command = new ArrayList<String>();
            command.add("cp");
            command.add(tmp.getAbsolutePath());
            String output = e + ".output";
            command.add(output);
            if (Command.exec(command.toArray(new String[0]), str2, str2,  "", false) != 0) {
              System.out.println("FAILURE COPYING " + tmp.getAbsolutePath() + " TO " + output);
            }
            out2.close();
            str2.close();
          } catch (Exception ex) {
            System.out.print("EXCEPTION " + ex.getClass() +
              " while running command: ");
            System.out.println(command.toString());
            System.out.println("Exception message: " + ex.getMessage());
          }
        }

        if (exitVariable != 0) {
          System.out
          .println("FAILURE: return value of process was != 0. Machine "
              + machine
              + " will get a new experiment. Command was:\n"
              + command
              + "\n\nOutput of the command is in file "
              + tmp.getAbsolutePath()
              + "\n");
          spawner.markTargetEndedWithError(e);
        } else {
          System.out.println("Machine " + machine + " finished experiment " + e + " (exit " + exitVariable + ")");
          spawner.markTargetDone(e);
        }

        currentTarget = "<none>";

      }
    }
    System.out.println("Machine " + machine + " has no more work.");
  }
}
