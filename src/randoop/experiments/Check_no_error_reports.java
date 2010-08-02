package randoop.experiments;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;




/**
 * Randoop should generate Junit tests that result in testclasses failures.
 * This means there should be no tests that terminate normally,
 * and no tests that result in an error (which is different
 * from a failure).
 * 
 * The single input to this script is a file (which presumably
 * contains the screen output of a junit run).
 * 
 *  If the last line, which looks something like
 *  
 *    Tests run: X,  Failures: Y,  Errors: Z
 *  
 *  is such that X != Y, this script exits
 *  with code 1, and outputs the line.
 *  
 *  Otherwise, it exits normally and outputs
 *  a success message.
 */
public class Check_no_error_reports {

  // Input: a text file with the output that Junit printed when
  // running a testclasses suite.
  public static void main(String[] args) throws FileNotFoundException, IOException {
    assert args.length == 1;
    Reader r = new FileReader(args[0]);
    check_no_error_reports(r);
    r.close();
  }

  public static void check_no_error_reports(Reader in) throws IOException {

    BufferedReader reader = new BufferedReader(in);

    String line = reader.readLine();

    while (line != null) {

      if (line.startsWith("Tests run:")) {

        // We found the last line.
        // Tests run: X,  Failures: Y,  Errors: Z

        String[] tokens = line.trim().split("[\\s]+");
        assert tokens[0].equals("Tests");
        assert tokens[1].equals("run:");
        // Remove comma, parse int.
        int testsRun = Integer.parseInt(tokens[2].substring(0, tokens[2].length()-1));
        assert tokens[3].equals("Failures:");
        // Remove comma, parse int.
        int failures = Integer.parseInt(tokens[4].substring(0, tokens[4].length()-1));
        assert tokens[5].equals("Errors:");
        // Parse int.
        int errors = Integer.parseInt(tokens[6]);

        if (testsRun != failures) {
          throw new RuntimeException("### Check_no_error_reports: There were non-failures: " + line);
        } else {
          assert errors == 0;
          System.out.println("### Check_no_error_reports: All tests were failures (this is good, means that randoop worked correctly).");
          return;
        }
      }

      line = reader.readLine();
    }

    throw new IllegalArgumentException("### Check_no_error_reports:     The file you provided had no \"Tests run\" line.");
  }

}
