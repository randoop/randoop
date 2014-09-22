package randoop.experiments;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import randoop.Globals;


/**
 * Specifies a number of properties about a subject program, e.g.:
 *
 *   + A classpath containing the subject program classes,
 *   + A classpath containing coverage-instrumented versions of the classes,
 *   + The location of a file containing a list of public, top-level classes.
 *
 * For most of these properties to refer to valid resources, the script
 * randoop.experiments.PrepareSubjectPrograms must be called first.
 */
public class ExperimentBase {

  public String experimentName;

  // Common properties.
  // ==================
  // Directory where the tested class files reside.
  public String classDirAbs;
  // Directory containing jar files required by testclasses classes.
  public String auxJarsDir;
  // Pattern specifying classes that shouldn't be explored
  // (will match fully-qualified classnames).
  public String classOmitPattern;
  // Pattern specifiying methods that shouldn't be explored.
  public String methodOmitPattern;

  // Properties found that are not common properties.
  // ================================================
  // (Example: "TIME" is the generation limit used by randoop)
  public Properties extraProperties;

  // Things computed based on the common properties.
  // ===============================================
  // Classpath that includes target package and auxiliary jars.
  public String classPath;
  // The name of a file containing the target class names.
  public String targetClassListFile;
  // The name of a file containing the list of all coverage-instumented class names.
  public String covInstClassListFile;
  // The name of a file containing the source (.java) files.
  // public String sourceListFile; // XXX this includes everything now, not just one package, right? same for targetClassListFile.
  // The name of the coverage-instrumented sources (This directory is created by running randoop.experiments.PrepareSubjectProgram).
  public String covInstSourcesDir;

  /**
   * Creates a new ExperimentBase whose properties are
   * obtained from inStream.
   */
  public ExperimentBase(String fileName) throws IOException {
    String experimentNameFull = new File(fileName).getName();
    if (!experimentNameFull.endsWith(".experiment"))
      throw new RuntimeException("Experiment file should end with .experiment suffix: " + fileName);
    experimentName = experimentNameFull.substring(0, experimentNameFull.length() - ".experiment".length());
    FileInputStream inStream = new FileInputStream(fileName);
    Properties allProperties = new Properties();
    allProperties.load(inStream);
    this.extraProperties = new Properties();
    for (Enumeration<?> propertyEnum = allProperties.propertyNames() ; propertyEnum.hasMoreElements() ; ) {
      String property = (String)propertyEnum.nextElement();
      if (property.equals("CLASS_DIR")) {
        this.classDirAbs = allProperties.getProperty(property);
      } else if (property.equals("AUX_JARS_DIR")) {
        this.auxJarsDir = allProperties.getProperty(property);
      } else if (property.equals("CLASS_OMIT")) {
        this.classOmitPattern = allProperties.getProperty(property);
      } else if (property.equals("METHOD_OMIT")) {
        this.methodOmitPattern = allProperties.getProperty(property);
      } else {
        extraProperties.setProperty(property, allProperties.getProperty(property));
      }
    }

    // Why??
    if (!this.classDirAbs.startsWith("src"))
      throw new RuntimeException("Property CLASS_DIR must start with \"src\"");

    this.classDirAbs = new File(this.classDirAbs).getAbsolutePath();

    // Compute classpath
    StringBuilder classPathBuilder = new StringBuilder();
    classPathBuilder.append(this.classDirAbs);
    if (!auxJarsDir.equals("")) {
      File dir = new File(auxJarsDir);
      if (!dir.isDirectory())
        throw new IllegalArgumentException("Not a directory: " + auxJarsDir);
      for (String s : dir.list()) {
        if (s.endsWith(".jar")) {
          File jarFile = new File(dir, s);
          classPathBuilder.append(":");
          classPathBuilder.append(jarFile.getAbsolutePath());
        }
      }
    }
    classPathBuilder.append(":" + System.getProperty("java.class.path"));
    this.classPath = classPathBuilder.toString();

    this.targetClassListFile = new File(experimentName + ".classlist.txt").getAbsolutePath();
    this.covInstClassListFile = new File(experimentName + ".covinstclasslist.txt").getAbsolutePath();
    this.covInstSourcesDir = new File(this.experimentName + "-covinst").getAbsolutePath();
  }

  // Prints a message to System.out saying that the given command
  // is going to be run.
  protected static void printCommand(List<String> command, boolean truncateLongLines, boolean oneline) {
    System.out.println("Running command:");
    for (String s : command) {
      if (truncateLongLines && s.length() > 80)
        s = s.substring(0, 79) + ".....";
      System.out.print(" " + s);
      if (!oneline)
        System.out.println();
    }
    System.out.println();
  }

  // Creates and returns a list of ExperimentBases, where each element is
  // created from the properties found in a file from fileNames.
  protected static List<ExperimentBase> getExperimentBasesFromFiles(String[] fileNames) throws IOException {
    List<ExperimentBase> retval = new ArrayList<ExperimentBase>();
    for (String fileName : fileNames) {
      retval.add(new ExperimentBase(fileName));
    }
    return retval;
  }

  // Find all files that end in ".java" reachable from dir.
  protected static List<String> findJavaFilesRecursively(File dir) {
    List<String> retval = new ArrayList<String>();
    if (!dir.isDirectory())
      throw new IllegalArgumentException();
    if (!dir.exists())
      throw new IllegalArgumentException();
    for (String file : dir.list()) {
      if (file.endsWith(".java")) {
        retval.add(dir.getPath() + "/" + file);
      } else {
        File dirMaybe = new File(dir, file);
        if (dirMaybe.isDirectory()) {
          retval.addAll(findJavaFilesRecursively(dirMaybe));
        }
      }
    }
    return retval;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("experimentName:" + this.experimentName + Globals.lineSep);
    b.append("classDir:" + this.classDirAbs + Globals.lineSep);
    b.append("auxJarsDir:" + this.auxJarsDir + Globals.lineSep);
    b.append("classOmitPattern:" + this.classOmitPattern + Globals.lineSep);
    b.append("methodOmitPattern:" + this.methodOmitPattern + Globals.lineSep);
    b.append("classpath:" + this.classPath + Globals.lineSep);
    b.append("classListFile:" + this.targetClassListFile + Globals.lineSep);
    return b.toString();
  }
}
