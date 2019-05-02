package randoop.instrument;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.bcel.generic.Type;
import org.plumelib.bcelutil.BcelUtil;
import org.plumelib.util.EntryReader;

/**
 * Provides the methods {@link #readReplacements(Reader, String)} and {@link
 * #readReplacements(Path)} that read a replacecall agent replacement file and populate the method
 * replacement map used by the agent. See the <a
 * href="https://randoop.github.io/randoop/manual/index.html#replacecall">replacecall user
 * documentation</a> for the file format.
 */
public class ReplacementFileReader {

  /** Regex for Java identifiers. */
  public static final String ID_STRING = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

  /**
   * Regex to match a sequence of identifiers separated by periods. Corresponds to package names,
   * fully-qualified classnames, or method names with fully-qualified classname.
   */
  public static final String DOT_DELIMITED_IDS = ID_STRING + "(?:\\." + ID_STRING + ")*";

  /**
   * Naive regex to match a method signature consisting of a fully-qualified method name followed by
   * anything in parentheses. The parentheses are expected to contain argument types, but the
   * pattern permits anything.
   *
   * <p>Note: Replacements may only be methods, and so representing the {@code <init>} notation for
   * constructors is unnecessary.
   */
  private static final String SIGNATURE_STRING =
      DOT_DELIMITED_IDS + "(?:\\.<init>)?" + "\\([^)]*\\)";

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
   * href="https://randoop.github.io/randoop/manual/index.html#replacecall">replacecall user
   * documentation</a> for the file format.
   *
   * @param replacementFile the file with method substitutions
   * @return the method replacement map constructed from the file
   * @throws IOException if there is an error reading the file
   * @see #readReplacements(Reader, String)
   */
  static HashMap<MethodSignature, MethodSignature> readReplacements(Path replacementFile)
      throws IOException, ReplacementFileException {
    return readReplacements(
        Files.newBufferedReader(replacementFile, StandardCharsets.UTF_8),
        replacementFile.toString());
  }

  /**
   * Reads the replacement file specifying method calls that should be replaced by other method
   * calls. See the <a
   * href="https://randoop.github.io/randoop/manual/index.html#replacecall">replacecall user
   * documentation</a> for the file format.
   *
   * @param in the {@code Reader} for the replacement file
   * @param filename the name of the file read by {@code in}, used for error reporting
   * @return the method replacement map constructed from the file
   * @throws IOException if there is an error while reading the file
   */
  static HashMap<MethodSignature, MethodSignature> readReplacements(Reader in, String filename)
      throws ReplacementFileException, IOException {
    HashMap<MethodSignature, MethodSignature> replacementMap = new HashMap<>();
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
            throw new ReplacementFileException(
                e.getMessage(), filename, reader.getLineNumber(), line);
          } catch (ClassNotFoundException e) {
            throw new ReplacementFileException(
                "Class not found: " + e.getMessage(), filename, reader.getLineNumber(), line);
          } catch (NoSuchMethodException e) {
            throw new ReplacementFileException(
                "Method not found: " + e.getMessage(), filename, reader.getLineNumber(), line);
          }
        } else {
          Matcher packageOrClassLineMatcher = PACKAGE_OR_CLASS_LINE.matcher(line);
          if (packageOrClassLineMatcher.matches()) {
            try {
              addReplacementsForClassOrPackage(
                  replacementMap,
                  packageOrClassLineMatcher.group(1),
                  packageOrClassLineMatcher.group(2));
            } catch (ReplacementException | IOException | ClassNotFoundException e) {
              throw new ReplacementFileException(
                  e.getMessage(), filename, reader.getLineNumber(), line);
            }
          } else {
            String msg = "Error in replacement file: bad format";
            throw new ReplacementFileException(msg, filename, reader.getLineNumber(), line);
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
   * @param originalSignature the signature of the method to be replaced
   * @param replacementSignature the signature of the replacement method
   * @throws ReplacementException if either replacement signature is badly-formed, the replacement
   *     class is not found, or the method does not exist
   */
  private static void addMethodReplacement(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      String originalSignature,
      String replacementSignature)
      throws ReplacementException, NoSuchMethodException, ClassNotFoundException {

    MethodSignature original;
    try {
      original = MethodSignature.of(originalSignature);
    } catch (IllegalArgumentException e) {
      throw new ReplacementException("Bad original signature", e);
    }

    MethodSignature replacement;
    try {
      replacement = MethodSignature.of(replacementSignature);
    } catch (IllegalArgumentException e) {
      throw new ReplacementException("Bad replacement signature", e);
    }

    // This call is made instead of calling replacement.exists(). The exists() method captures the
    // NoSuchMethodException, but the exception should be thrown to allow for error handling.
    replacement.toMethod();

    addReplacement(replacementMap, original, replacement);
  }

  /**
   * Adds a method replacement for the method with the original signature with the replacement
   * method.
   *
   * <p>It is an error if a replacement already exists for the original signature.
   *
   * @param replacementMap the map from an original signature to a replacement signature
   * @param original the original signature
   * @param replacement the replacement method signature
   * @return true if the replacement was added, false otherwise
   * @throws ReplacementException if a replacement already exists for {@code original}
   */
  private static void addReplacement(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      MethodSignature original,
      MethodSignature replacement)
      throws ReplacementException {
    // If there is already a replacement, do not overwrite it.
    if (replacementMap.get(original) != null) {
      String msg =
          String.format(
              "Method %s already has replacement %s, trying to add another %s",
              original, replacementMap.get(original), replacement);
      throw new ReplacementException(msg);
    }
    replacementMap.put(original, replacement);
  }

  /**
   * Adds the method replacements for the package or class replacement determined by the {@code
   * original} and {@code replacement} strings.
   *
   * <p>Behaves differently depending on whether the replacement corresponds to a class or a
   * package:
   *
   * <ul>
   *   <li>If the replacement is a class, then checks whether every method in the class has a
   *       corresponding method in the original class, and if so adds it to the {@code
   *       replacementMap}.
   *   <li>If the replacement is a package, then each class in the package is visited as described.
   * </ul>
   *
   * @param replacementMap the method replacement map to which new replacements are added
   * @param original the original package or class name
   * @param replacement the replacement package or class name
   * @throws ReplacementException if the replacement does not correspond to a package or class on
   *     the classpath
   */
  private static void addReplacementsForClassOrPackage(
      HashMap<MethodSignature, MethodSignature> replacementMap, String original, String replacement)
      throws ReplacementException, IOException, ClassNotFoundException {

    // Check whether the replacement string corresponds to a class (that can be loaded)
    Class<?> replacementClass;
    try {
      replacementClass = Class.forName(replacement);
    } catch (ClassNotFoundException e) {
      replacementClass = null;
      // if not found, then replacement is not a class than can be loaded or not a class
    }

    // If the class was loaded, then the replacement is a class.
    if (replacementClass != null) {
      addReplacementsForClass(replacementMap, original, replacementClass);
    } else {
      // Otherwise, assume the replacement is a package.
      // Finding the package depends on whether the agent is run on bootloaded classes,
      // which is the case if the ClassLoader is null.
      ClassLoader loader = ReplacementFileReader.class.getClassLoader();
      if (loader == null) {
        // The agent is run on bootloaded classes, so we have to check the path directly.
        addReplacementsForPackage(replacementMap, original, replacement);
      } else {
        // Otherwise, use the ClassLoader to avoid missing any classes on the normal classpath.
        addReplacementsForPackage(replacementMap, original, replacement, loader);
      }
    }
  }

  /**
   * Adds method replacements determined by an original and replacement class.
   *
   * <p>For each non-private static method in the replacement class, the original class is checked
   * for a corresponding method. This original method may either be non-static, where the receiver
   * is translated to the first argument of the replacement, or static, having the same arguments as
   * the replacement.
   *
   * <p>Note: This method will not overwrite a replacement for an original method.
   *
   * @param replacementMap the replacement map to which new replacements are added
   * @param originalClassname the name of the original class
   * @param replacementClass the {@code Class<>} for the replacement class
   * @throws ReplacementException if a replacement method is not static, or has no matching original
   */
  private static void addReplacementsForClass(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      String originalClassname,
      Class<?> replacementClass)
      throws ReplacementException {
    final Type originalType = BcelUtil.classnameToType(originalClassname);
    for (Method method : replacementClass.getDeclaredMethods()) {
      int modifiers = method.getModifiers() & Modifier.methodModifiers();
      if (Modifier.isPrivate(modifiers)) {
        // A mock class may have private helper methods; quietly ignore them.
        continue;
      }
      if (!Modifier.isStatic(modifiers)) {
        String msg =
            String.format(
                "Non-static non-private replacement method found: %s.%s",
                replacementClass.getCanonicalName(), method.getName());
        throw new ReplacementException(msg);
      }

      MethodSignature replacement = MethodSignature.of(method);

      MethodSignature original = replacement.substituteClassname(originalClassname);
      if (original.exists()) {
        addReplacement(replacementMap, original, replacement);
        continue;
      }

      // Check if the replacement might correspond to a non-static original
      if (replacement.getParameterTypes().length > 0
          && replacement.getParameterTypes()[0].equals(originalType)) {
        original = original.removeFirstParameter();
        if (original.exists()) {
          addReplacement(replacementMap, original, replacement);
          continue;
        }
      }

      String msg =
          String.format(
              "Replacement method %s has no matching original in %s%n",
              replacement, originalClassname);
      throw new ReplacementException(msg);
    }
  }

  /**
   * Adds method replacements determined by an original and replacement package. Uses the
   * bootclasspath to find the replacement package.
   *
   * <p>Visits each class of the package on the classpath and applies {@link
   * #addReplacementsForClass(HashMap, String, Class)} to add the method replacements.
   *
   * <p>Contrasts with {@link #addReplacementsForPackage(HashMap, String, String, ClassLoader)} that
   * searches for the package using a class loader.
   *
   * @param replacementMap the method replacement map to which new replacements are added
   * @param originalPackage the original package name
   * @param replacementPackage the replacement package name
   * @throws ReplacementException if no package corresponding to the replacement is found
   * @see #addReplacementsForClassOrPackage(HashMap, String, String)
   */
  private static void addReplacementsForPackage(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      String originalPackage,
      String replacementPackage)
      throws ReplacementException, ClassNotFoundException {
    String bootclasspath = System.getProperty("sun.boot.class.path");
    String javaHome = System.getProperty("java.home");

    // Explore the whole classpath to ensure all replacements are found.
    for (String pathString : bootclasspath.split(java.io.File.pathSeparator)) {
      // Replacements won't be found in java.home.
      if (pathString.startsWith(javaHome)) {
        continue;
      }
      Path file = Paths.get(pathString);
      if (!Files.exists(file)) {
        continue;
      }
      if (Files.isDirectory(file)) {
        Path path = file;
        Path replacementPath =
            path.resolve(replacementPackage.replace('.', java.io.File.separatorChar));
        if (Files.exists(replacementPath) && Files.isDirectory(replacementPath)) {
          addReplacementsForPackage(
              replacementMap, originalPackage, replacementPackage, replacementPath);
        }
      } else { // or a jar file
        try {
          JarFile jarFile = new JarFile(file.toFile());
          addReplacementsFromAllClassesOfPackage(
              replacementMap, originalPackage, replacementPackage, jarFile);
        } catch (IOException e) {
          throw new ReplacementException("Error reading jar file from boot classpath: " + file, e);
        }
      }
    }
  }

  /**
   * Adds method replacements determined by an original and replacement package. Uses the given
   * {@code ClassLoader} to find the replacement package.
   *
   * <p>Visits each class of the package on the classpath and applies {@link
   * #addReplacementsForClass(HashMap, String, Class)} to add the method replacements.
   *
   * <p>Contrasts with {@link #addReplacementsForPackage(HashMap, String, String)} that searches for
   * the package on the boot classpath.
   *
   * @param replacementMap the method replacement map to which new replacements are added
   * @param originalPackage the original package name
   * @param replacementPackage the replacement package name
   * @param loader the {@code ClassLoader}
   * @throws IOException if no package corresponding to replacement is found
   * @throws ReplacementException if no replacements are found in the replacement package
   * @see #addReplacementsForClassOrPackage(HashMap, String, String)
   */
  private static void addReplacementsForPackage(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      String originalPackage,
      String replacementPackage,
      ClassLoader loader)
      throws ReplacementException, IOException, ClassNotFoundException {
    boolean found = false;
    Enumeration<URL> resources = loader.getResources(replacementPackage.replace('.', '/'));

    // Explore the whole classpath to ensure all replacements are found.
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      try {
        URLConnection connection = url.openConnection();
        if (connection instanceof JarURLConnection) {
          JarFile jarFile = ((JarURLConnection) connection).getJarFile();
          addReplacementsFromAllClassesOfPackage(
              replacementMap, originalPackage, replacementPackage, jarFile);
        } else {
          // The subclass for directories is an internal Java class and its use results in compiler
          // warnings.
          // It seems to work to assume that connection is a directory, and let an exception occur
          // if it is not.
          Path path = Paths.get(URLDecoder.decode(url.getPath(), "UTF-8"));
          if (Files.exists(path) && Files.isDirectory(path)) {
            addReplacementsForPackage(replacementMap, originalPackage, replacementPackage, path);
            found = true;
          }
        }
      } catch (IOException e) {
        String msg =
            String.format(
                "Error identifying replacement %s with a package: %s",
                replacementPackage, e.getMessage());
        throw new ReplacementException(msg, e);
      }
    }
    if (!found) {
      String msg =
          String.format("No package for replacement %s found on classpath", replacementPackage);
      throw new ReplacementException(msg);
    }
  }

  /**
   * Adds method replacements for the classes in the replacement package to the replacement map.
   *
   * <p>Assumes that if a replacement package has a class or a subpackage, then the original package
   * does also.
   *
   * @param replacementMap the method replacement map to which replacements are added
   * @param originalPackage the name of the original package
   * @param replacementPackage the name of the replacement package
   * @param replacementDirectory the directory for the replacement package, must be non-null
   * @throws ReplacementException if a replacement method is not valid
   * @throws ClassNotFoundException if a replacement or package class is not found
   * @see #addReplacementsForPackage(HashMap, String, String)
   * @see #addReplacementsForPackage(HashMap, String, String, ClassLoader)
   */
  private static void addReplacementsForPackage(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      String originalPackage,
      String replacementPackage,
      Path replacementDirectory)
      throws ReplacementException, ClassNotFoundException {

    for (String filename : replacementDirectory.toFile().list()) {
      if (filename.endsWith(".class")) {
        final String classname = filename.substring(0, filename.length() - 6);
        final String originalClassname = originalPackage + "." + classname;
        final String replacementClassname = replacementPackage + "." + classname;
        addReplacementsForClass(replacementMap, originalClassname, replacementClassname);
      } else {
        Path subdirectory = new java.io.File(replacementDirectory.toFile(), filename).toPath();
        if (Files.exists(subdirectory) && Files.isDirectory(subdirectory)) {
          addReplacementsForPackage(
              replacementMap,
              originalPackage + filename,
              replacementPackage + filename,
              subdirectory);
        }
      }
    }
  }

  /**
   * For each class of the replacement package in the given jar file, calls {@link
   * #addReplacementsForClass(HashMap, String, String)} to add method replacements from the original
   * package to the replacement package.
   *
   * @param replacementMap the method replacement map to which new replacements are added
   * @param originalPackage the original package name
   * @param replacementPackage the replacement package name
   * @param jarFile the jar file to search
   * @see #addReplacementsForPackage(HashMap, String, String)
   * @see #addReplacementsForPackage(HashMap, String, String, ClassLoader)
   */
  private static void addReplacementsFromAllClassesOfPackage(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      String originalPackage,
      String replacementPackage,
      JarFile jarFile)
      throws ReplacementException, ClassNotFoundException {
    String replacementPath = replacementPackage.replace('.', '/') + "/";
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      String filename = entry.getName();
      if (filename.endsWith(".class") && filename.startsWith(replacementPath)) {
        final String classname =
            filename.substring(replacementPackage.length() + 1, filename.lastIndexOf(".class"));
        final String originalClassname = originalPackage + "." + classname;
        final String replacementClassname = replacementPackage + "." + classname;
        addReplacementsForClass(replacementMap, originalClassname, replacementClassname);
      }
    }
  }

  /**
   * Adds replacements for methods in the original class, if it exists, to methods found in the
   * replacement class.
   *
   * @param replacementMap the map to which replacements are added
   * @param originalClassname the name of the original class
   * @param replacementClassname the name of the replacement class
   * @throws ClassNotFoundException if either the original or replacement class cannot be loaded
   */
  private static void addReplacementsForClass(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      String originalClassname,
      String replacementClassname)
      throws ClassNotFoundException, ReplacementException {

    // Check that original class exists
    if (Class.forName(originalClassname) != null) {
      Class<?> replacementClass = Class.forName(replacementClassname);
      addReplacementsForClass(replacementMap, originalClassname, replacementClass);
    }
  }

  /**
   * Exception to represent an error discovered while reading a replacement file. Used to represent
   * errors within the reader methods where file name and line number are not available. Repackaged
   * as a {@link ReplacementFileException} in {@link ReplacementFileReader#readReplacements(Reader,
   * String)}.
   */
  private static class ReplacementException extends Throwable {
    /**
     * Create an exception with the message.
     *
     * @param message the error message
     */
    ReplacementException(String message) {
      super(message);
    }

    /**
     * Create an exception with the message and causing exception.
     *
     * @param message the message
     * @param cause the error message
     */
    ReplacementException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
