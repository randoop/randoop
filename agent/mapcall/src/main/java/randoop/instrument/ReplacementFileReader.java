package randoop.instrument;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.bcel.generic.Type;
import plume.BCELUtil;
import plume.EntryReader;

/**
 * Provides the methods {@link #readReplacements(Reader, String)} and {@link
 * #readReplacements(File)} that read a MapCall agent replacement file and populate the method
 * replacement map used by the agent. See the <a
 * href="https://randoop.github.io/randoop/manual/index.html#map_calls">mapcall user
 * documentation</a> for the file format.
 */
class ReplacementFileReader {

  /** Regex for Java identifiers */
  private static final String ID_STRING =
      "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

  /**
   * Regex to match a sequence of identifiers separated by periods. Corresponds to package names,
   * fully-qualified classnames, or method names with fully-qualified classname.
   */
  private static final String DOT_DELIMITED_IDS = ID_STRING + "(?:\\." + ID_STRING + ")*";

  /**
   * Naive regex to match a method signature consisting of a fully-qualified method name followed by
   * anything in parentheses. The parentheses are expected to contain argument types, but the
   * pattern permits anything.
   */
  private static final String SIGNATURE_STRING = DOT_DELIMITED_IDS + "\\([^)]*\\)";

  /**
   * Unanchored pattern to match of method replacements consisting of a pair of signatures. Uses the
   * naive signature pattern separated by one or more spaces or tabs. Can be used to decide whether
   * a replacement file line indicates a method replacement. Groups 1 and 2 correspond to each of
   * the signature strings. (Use with {@code matches}.)
   */
  private static final Pattern SIGNATURE_LINE =
      Pattern.compile("(" + SIGNATURE_STRING + ")[ \\t]+(" + SIGNATURE_STRING + ")");

  /**
   * Pattern to match class or package replacements consisting of a pair of class or package name
   * signatures. Groups 1 and 2 correspond to each of the package or class names.
   */
  private static final Pattern PACKAGE_OR_CLASS_LINE =
      Pattern.compile("(" + DOT_DELIMITED_IDS + ")[ \\t]+" + "(" + DOT_DELIMITED_IDS + ")");

  /**
   * Reads the given replacement file specifying method calls that should be replaced by other
   * method calls. See the <a
   * href="https://randoop.github.io/randoop/manual/index.html#map_calls">mapcall user
   * documentation</a> for the file format.
   *
   * @see #readReplacements(Reader, String)
   * @param map_file the file with map of method substitutions
   * @throws IOException if there is an error reading the file
   * @return the method replacement map constructed from the file
   */
  static ConcurrentHashMap<MethodSignature, MethodSignature> readReplacements(File map_file)
      throws IOException, ReplacementFileException {
    return readReplacements(new FileReader(map_file), map_file.getName());
  }

  /**
   * Reads the replacement file specifying method calls that should be replaced by other method
   * calls. See the <a href="https://randoop.github.io/randoop/manual/index.html#map_calls">mapcall
   * user documentation</a> for the file format.
   *
   * @param in the {@code Reader} for the replacement file
   * @param filename the name of the file read by {@code in}, used for error reporting
   * @throws IOException if there is an error while reading the file
   * @return the method replacement map constructed from the file
   */
  static ConcurrentHashMap<MethodSignature, MethodSignature> readReplacements(
      Reader in, String filename) throws IOException, ReplacementFileException {
    ConcurrentHashMap<MethodSignature, MethodSignature> replacementMap = new ConcurrentHashMap<>();
    try (EntryReader reader = new EntryReader(in, filename, "//.*$", null)) {
      for (String line : reader) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
          continue;
        }
        Matcher signatureLineMatcher = SIGNATURE_LINE.matcher(line);
        if (signatureLineMatcher.matches()) {
          try {
            addMethodReplacement(
                replacementMap, signatureLineMatcher.group(1), signatureLineMatcher.group(2));
          } catch (ReplacementException e) {
            throw new ReplacementFileException(e.getMessage(), reader.getLineNumber(), line);
          }
        } else {
          Matcher packageOrClassLineMatcher = PACKAGE_OR_CLASS_LINE.matcher(line);
          if (packageOrClassLineMatcher.matches()) {
            try {
              discoverClassOrPackageReplacements(
                  replacementMap,
                  packageOrClassLineMatcher.group(1),
                  packageOrClassLineMatcher.group(2));
            } catch (ReplacementException e) {
              throw new ReplacementFileException(e.getMessage(), reader.getLineNumber(), line);
            }
          } else {
            String msg = "Error in replacement file: bad format";
            throw new ReplacementFileException(msg, reader.getLineNumber(), line);
          }
        }
      }
    }
    return replacementMap;
  }

  /**
   * Adds a method replacement described by a pair of method signatures to the replacement map. Uses
   * reflection to check that the replacement method signature is well-formed and corresponds to a
   * method that exists.
   *
   * <p>See {@link MethodSignature#of(String)} for the expected format of a method signature.
   *
   * @param replacementMap the map from a method to a replacement method to which replacement is
   *     added
   * @param originalSignature the signature of the method to be mapped
   * @param replacementSignature the signature of the method to be mapped
   * @throws ReplacementException if the replacement signature is badly-formed, the replacement
   *     class is not found, or the method does not exist
   */
  private static void addMethodReplacement(
      ConcurrentHashMap<MethodSignature, MethodSignature> replacementMap,
      String originalSignature,
      String replacementSignature)
      throws ReplacementException {

    MethodSignature originalDef;
    try {
      originalDef = MethodSignature.of(originalSignature);
    } catch (IllegalArgumentException e) {
      String msg = String.format("Bad original signature: %s", e.getMessage());
      throw new ReplacementException(msg);
    }

    MethodSignature replacementDef;
    try {
      replacementDef = MethodSignature.of(replacementSignature);
    } catch (IllegalArgumentException e) {
      String msg = String.format("Bad replacement signature: %s", e.getMessage());
      throw new ReplacementException(msg);
    }

    Method replacementMethod;
    try {
      // check that replacement exists in a way that allows reporting errors
      replacementMethod = replacementDef.toMethod();
    } catch (ArgumentClassNotFoundException e) {
      String msg =
          String.format("Class not found for replacement argument type %s", e.getMessage());
      throw new ReplacementException(msg);
    } catch (ClassNotFoundException e) {
      String msg = "Class " + replacementDef.getClassname() + " not found for replacement method";
      throw new ReplacementException(msg);
    } catch (NoSuchMethodException e) {
      throw new ReplacementException("Replacement method not found " + replacementDef);
    }

    if (replacementMethod != null) {
      if (replacementMap.get(originalDef) != null) {
        String msg =
            String.format(
                "Conflicting replacement found for method %s by %s", originalDef, replacementDef);
        throw new ReplacementException(msg);
      }
      replacementMap.put(originalDef, replacementDef);
    }
  }

  /**
   * Discovers and adds the method replacements for the package or class replacement determined by
   * the {@code original} and {@code replacement} strings. This method determines whether the
   * replacement corresponds to a either a class or a package. If the replacement is a class, then
   * it will check whether every method in the class has a corresponding method in the original, and
   * if so adds it to the {@code replacementMap}. If the replacement is a package, then each class
   * in the package is visited in the same way.
   *
   * <p>Determines whether the replacement is a class or a package, and calls {@link
   * #addClassReplacements(ConcurrentHashMap, String, Class)} if the replacement is a class.
   * Otherwise determines whether the agent is operating on boot-loaded classes or not. If so, it
   * calls {@link #discoverPackageReplacements(ConcurrentHashMap, String, String)} to find
   * replacements on the boot classpath. Otherwise, it calls {@link
   * #discoverPackageReplacements(ConcurrentHashMap, String, String, ClassLoader)} to find
   * replacements using the class loader for the agent.
   *
   * @param replacementMap the method replacement map to which new replacements are added
   * @param original the original package or class name
   * @param replacement the replacement package or class name
   * @throws ReplacementException if the replacement does not correspond to a package or class on
   *     the classpath
   */
  private static void discoverClassOrPackageReplacements(
      ConcurrentHashMap<MethodSignature, MethodSignature> replacementMap,
      String original,
      String replacement)
      throws ReplacementException {

    // Check whether the replacement string corresponds to a class (that can be loaded)
    Class<?> replacementClass = null;
    try {
      replacementClass = Class.forName(replacement);
    } catch (ClassNotFoundException e) {
      //if not found, then replacement is not a class than can be loaded or not a class
    }

    // If the class was loaded then the replacement is a class
    if (replacementClass != null) {
      addClassReplacements(replacementMap, original, replacementClass);
    } else {
      // Otherwise, assume the replacement is a package.
      // Finding the package depends on whether the agent is run on bootloaded classes,
      // which is the case if the ClassLoader is null.
      ClassLoader loader = ReplacementFileReader.class.getClassLoader();
      if (loader == null) {
        // If the agent is run on bootloaded classes, we have to check the path directly.
        discoverPackageReplacements(replacementMap, original, replacement);
      } else {
        // Otherwise, use the ClassLoader to avoid missing any classes on the normal classpath.
        discoverPackageReplacements(replacementMap, original, replacement, loader);
      }
    }
  }

  /**
   * Adds method replacements for the class replacement determined by the original classname and
   * replacement class. For each non-private static method in the replacement class, the original
   * class is checked for a corresponding method. This original method may either be non-static,
   * where the receiver is translated to the first argument of the replacement, or static, having
   * the same arguments as the replacement.
   *
   * <p>Note: This method will not overwrite a replacement for an original method.
   *
   * @param replacementMap the replacement map to which new replacements are added
   * @param originalClassname the name of the original class
   * @param replacementClass the {@code Class<>} for the replacement class
   */
  private static void addClassReplacements(
      ConcurrentHashMap<MethodSignature, MethodSignature> replacementMap,
      String originalClassname,
      Class<?> replacementClass)
      throws ReplacementException {
    final Type originalType = BCELUtil.classname_to_type(originalClassname);
    for (Method method : replacementClass.getDeclaredMethods()) {
      int modifiers = method.getModifiers() & Modifier.methodModifiers();
      if (Modifier.isPrivate(modifiers)) {
        // a mock class may have private helper methods, quietly ignore them
        continue;
      }
      if (!Modifier.isStatic(modifiers)) {
        System.err.format(
            "Ignoring non-static method in replacement class %s%n",
            replacementClass.getCanonicalName());
        continue;
      }

      MethodSignature replacementDef = MethodSignature.of(method);

      MethodSignature originalDef = replacementDef.substituteClassname(originalClassname);
      if (originalDef.exists()) {
        // If there is already a replacement, do not overwrite it.
        if (replacementMap.get(originalDef) != null) {
          String msg =
              String.format(
                  "Conflicting replacement found for method %s by %s", originalDef, replacementDef);
          throw new ReplacementException(msg);
        }
        replacementMap.put(originalDef, replacementDef);
        continue;
      }

      // Check if the replacement might correspond to a non-static original
      if (replacementDef.getParameterTypes().length > 0
          && replacementDef.getParameterTypes()[0].equals(originalType)) {
        originalDef = originalDef.removeFirstParameter();
        if (originalDef.exists()) {
          // If there is already a replacement, do not overwrite it.
          if (replacementMap.get(originalDef) != null) {
            String msg =
                String.format(
                    "Conflicting replacement found for method %s by %s",
                    originalDef, replacementDef);
            throw new ReplacementException(msg);
          }
          replacementMap.put(originalDef, replacementDef);
          continue;
        }
      }

      String msg =
          String.format(
              "Replacement method %s has no matching original in %s%n",
              replacementDef, originalClassname);
      throw new ReplacementException(msg);
    }
  }

  /**
   * Discovers and adds method replacements on the boot classpath for the package replacement
   * determined by the {@code original} and {@code replacement} strings and the classes. Contrasts
   * with {@link #discoverPackageReplacements(ConcurrentHashMap, String, String, ClassLoader)} that
   * searches for the package using a class loader.
   *
   * @see #discoverClassOrPackageReplacements(ConcurrentHashMap, String, String, ClassLoader)
   * @param replacementMap the method replacement map to which new replacements are added
   * @param originalPackage the original package name
   * @param replacementPackage the replacement package name
   * @throws ReplacementException if no package corresponding to the replacement is found
   */
  private static void discoverPackageReplacements(
      ConcurrentHashMap<MethodSignature, MethodSignature> replacementMap,
      String originalPackage,
      String replacementPackage)
      throws ReplacementException {
    String bootclasspath = System.getProperty("sun.boot.class.path");
    String javaHome = System.getProperty("java.home");

    boolean found = false;
    for (String pathString : bootclasspath.split(File.pathSeparator)) {
      if (!pathString.startsWith(javaHome)) {
        File file = new File(pathString);
        if (!file.exists()) {
          continue;
        }
        if (file.isDirectory()) {
          Path path = file.toPath();
          Path replacementPath = path.resolve(replacementPackage.replace('.', File.separatorChar));
          if (Files.exists(replacementPath) && Files.isDirectory(replacementPath)) {
            addPackageReplacements(
                replacementMap, originalPackage, replacementPackage, replacementPath.toFile());
            found = true; // directory for package was found
          }
        } else { // or a jar file
          try {
            JarFile jarFile = new JarFile(file);
            if (addPackageReplacements(
                replacementMap, originalPackage, replacementPackage, jarFile)) {
              found = true;
            }
          } catch (IOException e) {
            throw new ReplacementException(
                "Error reading jar file from boot classpath: " + file.getName());
          }
        }
      }
    }
    if (!found) {
      String msg =
          String.format(
              "No package or class for replacement %s found on the boot classpath",
              replacementPackage);
      throw new ReplacementException(msg);
    }
  }

  /**
   * Discovers and adds method replacements from the given {@code ClassLoader} for the package
   * replacement determined by the {@code originalPackage} and {@code replacementPackage} strings.
   * Contrasts with {@link #discoverPackageReplacements(ConcurrentHashMap, String, String)} that
   * searches for the package on the boot classpath.
   *
   * @see #discoverClassOrPackageReplacements(ConcurrentHashMap, String, String)
   * @param replacementMap the method replacement map to which new replacements are added
   * @param originalPackage the original package name
   * @param replacementPackage the replacement package name
   * @param loader the {@code ClassLoader}
   * @throws ReplacementException if no package corresponding to replacement is found
   */
  private static void discoverPackageReplacements(
      ConcurrentHashMap<MethodSignature, MethodSignature> replacementMap,
      String originalPackage,
      String replacementPackage,
      ClassLoader loader)
      throws ReplacementException {
    boolean found = false;
    Enumeration<URL> resources;
    try {
      resources = loader.getResources(replacementPackage.replace('.', '/'));
    } catch (IOException e) {
      throw new ReplacementException(e.getMessage());
    }
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      try {
        URLConnection connection = url.openConnection();
        if (connection instanceof JarURLConnection) {
          JarFile jarFile = ((JarURLConnection) connection).getJarFile();
          if (addPackageReplacements(
              replacementMap, originalPackage, replacementPackage, jarFile)) {
            found = true;
          }
        } else {
          // The subclass for directories is is internal.  It seems to work to assume the
          // connection is a directory, and let an exception occur if it is not
          File path = new File(URLDecoder.decode(url.getPath(), "UTF-8"));
          if (path.exists() && path.isDirectory()) {
            addPackageReplacements(replacementMap, originalPackage, replacementPackage, path);
            found = true;
          }
        }
      } catch (IOException e) {
        String msg =
            String.format(
                "Error identifying replacement %s with a package: %s",
                replacementPackage, e.getMessage());
        throw new ReplacementException(msg);
      }
    }
    if (!found) {
      String msg =
          String.format(
              "No package or class for replacement %s found on classpath", replacementPackage);
      throw new ReplacementException(msg);
    }
  }

  /**
   * Adds method replacements for the package replacement determined by the {@code originalPackage}
   * and {@code replacementPackage} strings found in the given jar file.
   *
   * @see #discoverPackageReplacements(ConcurrentHashMap, String, String)
   * @see #discoverPackageReplacements(ConcurrentHashMap, String, String, ClassLoader)
   * @param replacementMap the method replacement map to which new replacements are added
   * @param originalPackage the original package name
   * @param replacementPackage the replacement package name
   * @param jarFile the jar file to search
   * @return true if a class in the replacement package is found in the jar and replacements added
   *     to {@code replacementMap}, false otherwise
   */
  private static boolean addPackageReplacements(
      ConcurrentHashMap<MethodSignature, MethodSignature> replacementMap,
      String originalPackage,
      String replacementPackage,
      JarFile jarFile)
      throws ReplacementException {
    String replacementPath = replacementPackage.replace('.', '/') + "/";
    boolean found = false;
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      String filename = entry.getName();
      if (filename.endsWith(".class") && filename.startsWith(replacementPath)) {
        final String classname =
            filename.substring(replacementPackage.length() + 1, filename.lastIndexOf(".class"));
        final String originalClassname = originalPackage + "." + classname;

        if (classExists(originalClassname)) {
          try {
            String replacementClassname = replacementPackage + "." + classname;
            Class<?> replacementClass = Class.forName(replacementClassname);
            addClassReplacements(replacementMap, originalClassname, replacementClass);
            found = true;
          } catch (ClassNotFoundException e) {
            throw new ReplacementException("Error loading replacement file: " + e.getMessage());
          }
        } else {
          String msg = String.format("Expected class %s does not exist", originalClassname);
          throw new ReplacementException(msg);
        }
      }
    }
    return found;
  }

  /**
   * Adds method replacements for the classes in the replacement package to the replacement map.
   *
   * <p>Assumes that subpackage structure of original and replacement packages is identical.
   *
   * @see #discoverPackageReplacements(ConcurrentHashMap, String, String)
   * @see #discoverPackageReplacements(ConcurrentHashMap, String, String, ClassLoader)
   * @param replacementMap the method replacement map to which replacements are added
   * @param originalPackage the name of the original package
   * @param replacementPackage the name of the replacement package
   * @param replacementDirectory the directory for the replacement package, must be non-null
   */
  private static void addPackageReplacements(
      ConcurrentHashMap<MethodSignature, MethodSignature> replacementMap,
      String originalPackage,
      String replacementPackage,
      File replacementDirectory)
      throws ReplacementException {

    for (String filename : replacementDirectory.list()) {
      if (filename.endsWith(".class")) {
        final String classname = filename.substring(0, filename.length() - 6);
        final String originalClassname = originalPackage + "." + classname;
        if (classExists(originalClassname)) {
          try {
            String replacementClassname = replacementPackage + "." + classname;
            Class<?> replacementClass = Class.forName(replacementClassname);
            addClassReplacements(replacementMap, originalClassname, replacementClass);
          } catch (ClassNotFoundException e) {
            throw new ReplacementException("Error loading replacement file: " + e.getMessage());
          }
        } else {
          String msg = String.format("Expected class %s does not exist", originalClassname);
          throw new ReplacementException(msg);
        }
      } else {
        File subdirectory = new File(replacementDirectory, filename);
        if (subdirectory.exists() && subdirectory.isDirectory()) {
          addPackageReplacements(
              replacementMap,
              originalPackage + filename,
              replacementPackage + filename,
              subdirectory);
        }
      }
    }
  }

  /**
   * Indicates whether a class with the given name can be loaded by the context classloader of the
   * agent.
   *
   * @param classname the name of the class to find
   * @return true if the class can be loaded, false otherwise
   */
  private static boolean classExists(String classname) {
    try {
      return Class.forName(classname) != null;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  /** Exception to represent the occurrence of an error in the replacement file */
  private static class ReplacementException extends Throwable {
    ReplacementException(String message) {
      super(message);
    }
  }
}
