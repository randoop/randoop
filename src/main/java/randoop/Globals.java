package randoop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import randoop.main.RandoopBug;
import randoop.main.RandoopUsageError;

/** Various general global variables used throughout Randoop. */
public class Globals {

  /** The version number for Randoop. */
  public static final String RANDOOP_VERSION = "4.3.0";

  /** The system-specific line separator string. */
  public static final String lineSep = System.lineSeparator();

  /** A PrintStream whose contents are ignored. */
  public static PrintStream blackHole;

  /** Discards anything written to it. */
  private static class NullOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {}
  }

  // private static PrintStream realSystemErr;

  static {
    blackHole = new PrintStream(new NullOutputStream());
    // realSystemErr = System.err;
    // System.setErr(blackHole);
  }

  /**
   * Return the version number for Randoop.
   *
   * @return the version number for Randoop
   */
  public static String getRandoopVersion() {
    Properties prop = new Properties();
    boolean isRelease = Globals.class.getResourceAsStream("/this-is-a-randoop-release") != null;
    if (isRelease) {
      return RANDOOP_VERSION;
    }
    InputStream inputStream = Globals.class.getResourceAsStream("/git.properties");
    try {
      prop.load(inputStream);
    } catch (IOException e) {
      throw new RandoopBug(e);
    }

    String localChanges = prop.getProperty("git.dirty").equals("true") ? ", local changes" : "";
    return "\""
        + String.join(
            ", ",
            RANDOOP_VERSION + localChanges,
            "branch " + prop.getProperty("git.branch"),
            "commit " + prop.getProperty("git.commit.id.abbrev"),
            prop.getProperty("git.commit.time").substring(0, 10))
        + "\"";
  }

  /**
   * Return the Java classpath.
   *
   * @return the Java classpath
   */
  public static String getClassPath() {
    return System.getProperty("java.class.path");
  }

  /**
   * Returns list of all classpath entries (jars and directories), including entries
   * defined in jars' manifests
   * @return classpath entries
   */
  public static List<String> getClassPathEntries() {
    String[] classpathEntries = getClassPath().split(File.pathSeparator);
    List<String> allEntries = new ArrayList<>(Arrays.asList(classpathEntries));
    List<String> manifestEntries = new ArrayList<>(allEntries);
    while (!manifestEntries.isEmpty()) {
      // iterate over copy to avoid concurrent modification
      List<String> manifestEntriesCopy = new ArrayList<>(manifestEntries);
      for (String path : manifestEntriesCopy) {
        manifestEntries.addAll(extractManifestClasspath(path));
      }
      manifestEntries.removeAll(allEntries);
      allEntries.addAll(manifestEntries);
    }
    return allEntries.stream().distinct().collect(Collectors.toList());
  }

  /**
   * Returns list of classpath entries defined in manifest if file with given path
   * is jar file, manifest exists and manifest contains Class-Path attribute, empty
   * list otherwise.
   *
   * @param path path of jar file to extract manifest from
   * @return classpath entries
   */
  private static List<String> extractManifestClasspath(String path) {
    List<String> classpathEntries = new ArrayList<>();
    File location = new File(path);
    if (location.isFile() && location.getName().endsWith(".jar")) {
      try (JarInputStream jarStream = new JarInputStream(new FileInputStream(location))) {
        Manifest manifest = jarStream.getManifest();
        if (manifest == null) {
          return classpathEntries;
        }
        Attributes attributes = manifest.getMainAttributes();
        String manifestClasspath = attributes.getValue("Class-Path");
        if (manifestClasspath == null) {
          return classpathEntries;
        }
        manifestClasspath = manifestClasspath.replace("file:/", "");
        classpathEntries.addAll(Arrays.asList(manifestClasspath.split(" ")));
      } catch (FileNotFoundException e) {
        throw new RandoopUsageError("Can't find the file specified in the classpath " + location.getAbsolutePath());
      } catch (IOException e) {
        throw new RandoopUsageError("IO exception while reading file " + location.getAbsolutePath());
      }
    }
    return classpathEntries;
  }

  /** Column width for printing messages. */
  public static final int COLWIDTH = 70;

  /** Number of spaces for leading indentation for printing messages. */
  public static final int INDENTWIDTH = 8;
}
