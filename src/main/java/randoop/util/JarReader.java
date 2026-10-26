package randoop.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/** Utility methods for listing the classes contained in a jar file. */
public final class JarReader {

  /** If true, output debugging diagnostics. */
  private static boolean debug = false;

  /** Do not instantiate. */
  private JarReader() {
    throw new UnsupportedOperationException("Do not instantiate");
  }

  /**
   * Prints, one per line in sorted order, the names of the class files in the given jar file.
   *
   * @param args the command-line arguments; the first is the name of the jar file
   * @throws IOException if there is a problem reading the jar file
   */
  public static void main(String[] args) throws IOException {
    List<String> names = getClasseNamesInJar(args[0]);
    Collections.sort(names);
    System.out.println(CollectionsExt.toStringInLines(names));
  }

  /**
   * Returns the names of the class files in the given jar file that are in the given package or in
   * one of its subpackages. Each result is a jar entry name with "/" replaced by "." (for example,
   * {@code "java.lang.Object.class"}).
   *
   * @param jarName the name of the jar file
   * @param packageName the package name, in dotted form; the empty string means the unnamed package
   * @return the names of the class files in the given package or in one of its subpackages
   * @throws IOException if there is a problem reading the jar file
   */
  public static List<String> getClassNamesInPackage(String jarName, String packageName)
      throws IOException {
    ArrayList<String> classes = new ArrayList<>();

    // The trailing "/" prevents matching a sibling package whose name has the same prefix.
    String prefix = packageName.isEmpty() ? "" : packageName.replace('.', '/') + "/";
    if (debug) {
      System.out.println("Jar " + jarName + " looking for " + prefix);
    }

    try (InputStream fis = Files.newInputStream(Paths.get(jarName));
        JarInputStream jarFile = new JarInputStream(fis)) {
      JarEntry jarEntry;

      while (true) {
        jarEntry = jarFile.getNextJarEntry();
        if (jarEntry == null) {
          break;
        }
        if (jarEntry.getName().startsWith(prefix) && jarEntry.getName().endsWith(".class")) {
          if (debug) {
            System.out.println(jarEntry.getName().replaceAll("/", "\\."));
          }
          classes.add(jarEntry.getName().replaceAll("/", "\\."));
        }
      }
    }
    return classes;
  }

  /**
   * Returns the names of all the class files in the given jar file. Each result is a jar entry name
   * with "/" replaced by "." (for example, {@code "java.lang.Object.class"}).
   *
   * @param jarName the name of the jar file
   * @return the names of the class files in the jar file
   * @throws IOException if there is a problem reading the jar file
   */
  public static List<String> getClasseNamesInJar(String jarName) throws IOException {
    ArrayList<String> classes = new ArrayList<>();

    if (debug) {
      System.out.println("Jar " + jarName);
    }

    try (InputStream fis = Files.newInputStream(Paths.get(jarName));
        JarInputStream jarFile = new JarInputStream(fis)) {
      JarEntry jarEntry;

      while (true) {
        jarEntry = jarFile.getNextJarEntry();
        if (jarEntry == null) {
          break;
        }
        if (jarEntry.getName().endsWith(".class")) {
          if (debug) {
            System.out.println(jarEntry.getName().replaceAll("/", "\\."));
          }
          classes.add(jarEntry.getName().replaceAll("/", "\\."));
        }
      }
    }
    return classes;
  }
}
