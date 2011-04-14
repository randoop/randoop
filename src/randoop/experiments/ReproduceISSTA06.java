package randoop.experiments;

import java.util.Properties;

import randoop.Globals;
import randoop.experiments.RandoopRun.RunType;





public class ReproduceISSTA06 {

  public static void checkIfReproduced(RunType runType, String packageName, Properties p) throws ReproduceISSTA06Failure {

    System.out.println("Checking results against ISSTA06 results, runType=" + runType
        + ", package=" + packageName);

    if (runType == RunType.ONLINE) {
      if (packageName.equals("org.apache.commons.chain")) {
        checkRange(p, "faultymethods", 15, Integer.MAX_VALUE);
        checkRange(p, "faultyclasses", 8, Integer.MAX_VALUE);
        checkRange(p, "exceptioncontractviolations", 14, Integer.MAX_VALUE);
        checkRange(p, "npecontractviolations", 14, Integer.MAX_VALUE);
        checkEquals(p, "objectcontractviolations", 1);
        checkEquals(p, "hashcodecontractviolations", 1);
        checkEquals(p, "tostringcontractviolations", 1);
        checkEquals(p, "eqoocontractviolations", 0);
        return;
      }
      if (packageName.equals("org.apache.commons.jelly")) {
        checkRange(p, "faultymethods", 28, Integer.MAX_VALUE);
        checkRange(p, "faultyclasses", 18, Integer.MAX_VALUE);
        checkRange(p, "exceptioncontractviolations", 28, Integer.MAX_VALUE);
        checkRange(p, "npecontractviolations", 28, Integer.MAX_VALUE);
        checkEquals(p, "objectcontractviolations", 0);
        checkEquals(p, "hashcodecontractviolations", 0);
        checkEquals(p, "tostringcontractviolations", 0);
        checkEquals(p, "eqoocontractviolations", 0);
        return;
      }
      if (packageName.equals("org.apache.commons.collections")) {
        checkRange(p, "faultymethods", 14, Integer.MAX_VALUE);
        checkRange(p, "faultyclasses", 8, Integer.MAX_VALUE);
        checkRange(p, "exceptioncontractviolations", 13, Integer.MAX_VALUE);
        checkRange(p, "npecontractviolations", 13, Integer.MAX_VALUE);
        checkRange(p, "objectcontractviolations", 1, Integer.MAX_VALUE);
        checkRange(p, "hashcodecontractviolations", 0, Integer.MAX_VALUE);
        checkRange(p, "tostringcontractviolations", 1, Integer.MAX_VALUE);
        checkRange(p, "eqoocontractviolations", 0, Integer.MAX_VALUE);
        return;
      }
      if (packageName.equals("org.apache.commons.logging")) {
        checkEquals(p, "faultymethods", 0);
        checkEquals(p, "faultyclasses", 0);
        checkEquals(p, "exceptioncontractviolations", 0);
        checkEquals(p, "npecontractviolations", 0);
        checkEquals(p, "objectcontractviolations", 0);
        checkEquals(p, "hashcodecontractviolations", 0);
        checkEquals(p, "tostringcontractviolations", 0);
        checkEquals(p, "eqoocontractviolations", 0);
        return;
      }
      if (packageName.equals("org.apache.commons.math")) {
        checkRange(p, "faultymethods", 3, Integer.MAX_VALUE);
        checkRange(p, "faultyclasses", 2, Integer.MAX_VALUE);
        checkRange  (p, "exceptioncontractviolations", 0, Integer.MAX_VALUE);
        checkRange(p, "npecontractviolations", 0, Integer.MAX_VALUE);
        checkRange(p, "objectcontractviolations", 3, Integer.MAX_VALUE);
        checkRange(p, "hashcodecontractviolations", 3, Integer.MAX_VALUE);
        checkEquals(p, "tostringcontractviolations", 0);
        checkEquals(p, "eqoocontractviolations", 0);
        return;
      }
      if (packageName.equals("org.apache.commons.collections.primitives")) {
        checkRange(p, "faultymethods", 11, Integer.MAX_VALUE);
        checkRange(p, "faultyclasses", 11, Integer.MAX_VALUE);
        checkEquals(p, "exceptioncontractviolations", 0);
        checkEquals(p, "npecontractviolations", 0);
        checkRange(p, "objectcontractviolations", 11, Integer.MAX_VALUE);
        checkEquals(p, "hashcodecontractviolations", 0);
        checkRange(p, "tostringcontractviolations", 2, Integer.MAX_VALUE);
        checkRange(p, "eqoocontractviolations", 9, Integer.MAX_VALUE);
        return;
      }
      if (packageName.equals("java.util")) {
        checkRange(p, "faultymethods", 4, Integer.MAX_VALUE);
        checkRange(p, "faultyclasses", 4, Integer.MAX_VALUE);
        checkRange(p, "exceptioncontractviolations", 0, Integer.MAX_VALUE);
        checkRange(p, "npecontractviolations", 0, Integer.MAX_VALUE);
        checkRange(p, "objectcontractviolations", 4, Integer.MAX_VALUE);
        checkRange(p, "hashcodecontractviolations", 1, Integer.MAX_VALUE);
        checkRange(p, "tostringcontractviolations", 3, Integer.MAX_VALUE);
        checkRange(p, "eqoocontractviolations", 1, Integer.MAX_VALUE);
        return;
      }
      if (packageName.equals("javax.xml")) {
        checkRange(p, "faultymethods", 9, Integer.MAX_VALUE);
        checkEquals(p, "faultyclasses", 3);
        checkEquals(p, "exceptioncontractviolations", 1);
        checkEquals(p, "npecontractviolations", 1);
        checkRange(p, "objectcontractviolations", 8, Integer.MAX_VALUE);
        checkRange(p, "hashcodecontractviolations", 0, 1);
        checkRange(p, "tostringcontractviolations", 8, Integer.MAX_VALUE);
        checkEquals(p, "eqoocontractviolations", 0);
        return;
      }
      throw new RuntimeException("Unrecognized experiment package=" + packageName);
    } else {
      assert runType == RunType.OFFLINE;
      if (packageName.equals("org.apache.commons.chain")) {
        // checkEquals(p, "testcases", 22);
        checkEquals(p, "faultymethods", 14);
        checkEquals(p, "faultyclasses", 11);
        checkEquals(p, "exceptioncontractviolations", 13);
        checkEquals(p, "npecontractviolations", 13);
        checkEquals(p, "objectcontractviolations", 1);
        checkEquals(p, "hashcodecontractviolations", 1);
        checkEquals(p, "tostringcontractviolations", 1);
        checkEquals(p, "eqoocontractviolations", 0);
        return;
      }
      if (packageName.equals("org.apache.commons.jelly")) {
        checkEquals(p, "faultymethods", 10);
        checkEquals(p, "faultyclasses", 7);
        checkEquals(p, "exceptioncontractviolations", 10);
        checkEquals(p, "npecontractviolations", 10);
        checkEquals(p, "objectcontractviolations", 0);
        checkEquals(p, "hashcodecontractviolations", 0);
        checkEquals(p, "tostringcontractviolations", 0);
        checkEquals(p, "eqoocontractviolations", 0);
        return;
      }
      if (packageName.equals("org.apache.commons.collections")) {
        checkRange(p, "faultymethods", 6, 20);
        checkRange(p, "faultyclasses", 4, 10);
        checkRange(p, "exceptioncontractviolations", 6, 20);
        checkRange(p, "npecontractviolations", 6, 20);
        checkRange(p, "objectcontractviolations", 0, 1);
        checkEquals(p, "hashcodecontractviolations", 0);
        checkEquals(p, "tostringcontractviolations", 0);
        checkRange(p, "eqoocontractviolations", 0, 1);
        return;
      }
      if (packageName.equals("org.apache.commons.logging")) {
        checkEquals(p, "faultymethods", 0);
        checkEquals(p, "faultyclasses", 0);
        checkEquals(p, "exceptioncontractviolations", 0);
        checkEquals(p, "npecontractviolations", 0);
        checkEquals(p, "objectcontractviolations", 0);
        checkEquals(p, "hashcodecontractviolations", 0);
        checkEquals(p, "tostringcontractviolations", 0);
        checkEquals(p, "eqoocontractviolations", 0);
        return;
      }
      if (packageName.equals("org.apache.commons.math")) {
        checkRange(p, "faultymethods", 3, 5);
        checkEquals(p, "faultyclasses", 3);
        checkRange(p, "exceptioncontractviolations", 1, 2);
        checkRange(p, "npecontractviolations", 1, 2);
        checkRange(p, "objectcontractviolations", 2, 3);
        checkRange(p, "hashcodecontractviolations", 2, 3);
        checkEquals(p, "tostringcontractviolations", 0);
        checkEquals(p, "eqoocontractviolations", 0);
        return;
      }
      if (packageName.equals("org.apache.commons.collections.primitives")) {
        checkEquals(p, "faultymethods", 2);
        checkEquals(p, "faultyclasses", 2);
        checkEquals(p, "exceptioncontractviolations", 0);
        checkEquals(p, "npecontractviolations", 0);
        checkEquals(p, "objectcontractviolations", 2);
        checkEquals(p, "hashcodecontractviolations", 0);
        checkEquals(p, "tostringcontractviolations", 0);
        checkEquals(p, "eqoocontractviolations", 2);
        return;
      }
      if (packageName.equals("java.util")) {
        checkEquals(p, "faultymethods", 2);
        checkEquals(p, "faultyclasses", 2);
        checkEquals(p, "exceptioncontractviolations", 1);
        checkEquals(p, "npecontractviolations", 1);
        checkEquals(p, "objectcontractviolations", 1);
        checkEquals(p, "hashcodecontractviolations", 0);
        checkEquals(p, "tostringcontractviolations", 1);
        checkEquals(p, "eqoocontractviolations", 0);
        return;
      }
      throw new RuntimeException("Unrecognized experiment package=" + packageName);
    }
  }

  private static void checkEquals(Properties p, String property, int expectedVariable) throws ReproduceISSTA06Failure {
    String propertyVariable = p.getProperty(property);
    if (propertyVariable == null) {
      StringBuilder b = new StringBuilder();
      b.append("The following property was not found in the run: " + property);
      throw new ReproduceISSTA06Failure(b.toString());
    }
    int runVariable = Integer.parseInt(propertyVariable);
    if (runVariable != expectedVariable) {
      StringBuilder b = new StringBuilder();
      b.append("Property " + property + " had value " + runVariable + Globals.lineSep);
      b.append("Expected a value ==" + expectedVariable + Globals.lineSep);
      throw new ReproduceISSTA06Failure(b.toString());
    }
  }

  private static void checkRange(Properties p, String property, int min, int max) throws ReproduceISSTA06Failure {
    String propertyVariable = p.getProperty(property);
    if (propertyVariable == null) {
      StringBuilder b = new StringBuilder();
      b.append("The following property was not found in the run: " + property);
      throw new ReproduceISSTA06Failure(b.toString());
    }
    int runVariable = Integer.parseInt(propertyVariable);
    if (runVariable < min || runVariable > max) {
      StringBuilder b = new StringBuilder();
      b.append("Property " + property + " had value " + runVariable + Globals.lineSep);
      b.append("Expected a value >=" + min + " and <= " + max + Globals.lineSep);
      throw new ReproduceISSTA06Failure(b.toString());
    }
  }
}
