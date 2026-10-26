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
import org.checkerframework.checker.mustcall.qual.Owning;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.regex.qual.Regex;
import org.checkerframework.checker.signedness.qual.PolySigned;
import randoop.main.RandoopBug;
import randoop.main.RandoopUsageError;

/** Various general global variables used throughout Randoop. */
public final class Globals {

  /** Do not instantiate. */
  private Globals() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /** The version number for Randoop. */
  public static final String RANDOOP_VERSION = "4.3.4";

  /** The system-specific line separator string. */
  public static final @Regex(0) String lineSep = System.lineSeparator();

  /** A PrintStream whose contents are ignored. */
  @SuppressWarnings("PMD.ModifierOrder") // `@Owning` isn't a type annotation, but should be.
  public static @Owning PrintStream blackHole = new PrintStream(new NullOutputStream());

  /** Discards anything written to it. */
  private static final class NullOutputStream extends OutputStream {
    /** Creates a new NullOutputStream. */
    public NullOutputStream() {}

    @Override
    public void write(@PolySigned int b) throws IOException {
      // The null stream consumes and ignores all data.
    }
  }

  /**
   * Returns the version number for Randoop.
   *
   * @return the version number for Randoop
   */
  public static String getRandoopVersion() {
    Properties prop = new Properties();
    try (InputStream isReleaseStream =
        Globals.class.getResourceAsStream("/this-is-a-randoop-release")) {
      if (isReleaseStream != null) {
        return RANDOOP_VERSION;
      }
    } catch (IOException e) {
      throw new RandoopBug(e);
    }
    try (@SuppressWarnings("nullness:assignment") // file git.properties exists
        @NonNull InputStream inputStream = Globals.class.getResourceAsStream("/git.properties")) {
      prop.load(inputStream);
    } catch (IOException e) {
      throw new RandoopBug(e);
    }

    @SuppressWarnings("nullness:dereference.of.nullable") // property git.dirty exists
    String localChanges = prop.getProperty("git.dirty").equals("true") ? ", local changes" : "";
    @SuppressWarnings("nullness:dereference.of.nullable") // property git.commit.time exists
    String commitTime = prop.getProperty("git.commit.time").substring(0, 10);
    return "\""
        + String.join(
            ", ",
            RANDOOP_VERSION + localChanges,
            "branch " + prop.getProperty("git.branch"),
            "commit " + prop.getProperty("git.commit.id.abbrev"),
            commitTime)
        + "\"";
  }

  /**
   * Returns the Java classpath.
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
      try (FileInputStream fileStream = new FileInputStream(location);
          JarInputStream jarStream = new JarInputStream(fileStream)) {
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
