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

public final class JarReader {

  /** If true, output debugging diagnostics. */
  private static boolean debug = false;

  /** Do not instantiate. */
  private JarReader() {
    throw new Error("Do not instantiate");
  }

  public static void main(String[] args) throws IOException {
    List<String> names = getClasseNamesInJar(args[0]);
    Collections.sort(names);
    System.out.println(CollectionsExt.toStringInLines(names));
  }

  public static List<String> getClassNamesInPackage(String jarName, String packageName)
      throws IOException {
    ArrayList<String> classes = new ArrayList<>();

    packageName = packageName.replaceAll("\\.", "/");
    if (debug) {
      System.out.println("Jar " + jarName + " looking for " + packageName);
    }

    try (InputStream fis = Files.newInputStream(Paths.get(jarName));
        JarInputStream jarFile = new JarInputStream(fis)) {
      JarEntry jarEntry;

      while (true) {
        jarEntry = jarFile.getNextJarEntry();
        if (jarEntry == null) {
          break;
        }
        if (jarEntry.getName().startsWith(packageName) && jarEntry.getName().endsWith(".class")) {
          if (debug) {
            System.out.println(jarEntry.getName().replaceAll("/", "\\."));
          }
          classes.add(jarEntry.getName().replaceAll("/", "\\."));
        }
      }
    }
    return classes;
  }

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
