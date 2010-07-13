package randoop.util;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import plume.StrTok;

public class RunCmd {

  /**
   * Runs the specified command and waits for it to finish.  Throws an
   * Error if the command fails for any reason or returns a non-zero
   * status.  The parameters are the same as for Runtime.exec() except
   * that cmd is tokenized with support for quote characters.
   *
   * Any output is written to standard out.  No input is provided.
   */
  public static void run_cmd (String[] cmd_args, String[] env_arr, File dir) {

    int result = 0;
    try {
      Process p = java.lang.Runtime.getRuntime().exec (cmd_args, env_arr, dir);
      StreamRedirectThread err_thread
      = new StreamRedirectThread ("stderr", p.getErrorStream(), System.out);
      StreamRedirectThread out_thread
      = new StreamRedirectThread ("stdout", p.getInputStream(), System.out);
      err_thread.start();
      out_thread.start();
      result = p.waitFor();
      err_thread.join();
      out_thread.join();
    } catch (Exception e) {
      throw new Error (String.format ("error running cmd '%s'",
                                      Arrays.toString(cmd_args)), e);
    }
    if (result != 0)
      throw new Error (String.format ("cmd '%s' returned status %d",
                                      Arrays.toString(cmd_args), result));
  }

  /**
   * Runs the specified command and waits for it to finish.  Throws an
   * Error if the command fails for any reason.  Returns the status
   * of the command.
   *
   * Any output is written to standard out.  No input is provided.
   */
  public static void run_cmd (String[] cmd) {
    run_cmd (cmd, null, null);
  }

  /**
   * Runs the specified command and waits for it to finish.  Throws an
   * Error if the command fails for any reason or returns a non-zero
   * status.  The parameters are the same as for Runtime.exec() except
   * that cmd is tokenized with support for quote characters.
   *
   * Any output is written to standard out.  No input is provided.
   */
  public static void run_cmd (String cmd, String[] env_arr, File dir) {

    // Translate the input string into arguments with support for quotes
    List<String> args = new ArrayList<String>();
    StrTok stok = new StrTok(cmd);
    // stok.stok.wordChars ('-', '-');
    stok.quoteChar ('\'');
    stok.quoteChar ('"');
    for (String tok = stok.nextToken(); tok != null; tok = stok.nextToken()) {
      if (tok.startsWith ("'") || tok.startsWith ("\"")) {
        args.add (tok.substring (1, tok.length()-1));
      } else {
        args.add (tok);
      }
    }

    String[] arg_array = new String[args.size()];
    run_cmd (args.toArray(arg_array), env_arr, dir);
  }

  /**
   * Runs the specified command and waits for it to finish.  Throws an
   * Error if the command fails for any reason.  Returns the status
   * of the command.
   *
   * Any output is written to standard out.  No input is provided.
   */
  public static void run_cmd (String cmd) {
    run_cmd (cmd, null, null);
  }

}
