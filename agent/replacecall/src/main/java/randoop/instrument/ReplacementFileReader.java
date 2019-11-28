package randoop.instrument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ObjectType;
import org.checkerframework.checker.signature.qual.BinaryName;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.checkerframework.checker.signature.qual.DotSeparatedIdentifiers;
import org.plumelib.reflection.Signatures;
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
   * signatures. Group 1 is the package name, and group 2 is the class name.
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
   * @throws ReplacementFileException if there is an error in the replacement file
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
   * @throws ReplacementFileException if there is an error in the replacement file
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
          } catch (ReplacementException
              | ClassNotFoundException
              | IllegalClassFormatException
              | NoSuchMethodException e) {
            throw new ReplacementFileException(
                e.getMessage(), filename, reader.getLineNumber(), line);
          }
        } else {
          Matcher packageOrClassLineMatcher = PACKAGE_OR_CLASS_LINE.matcher(line);
          if (packageOrClassLineMatcher.matches()) {
            try {
              @SuppressWarnings("signature:assignment.type.incompatible") // regex match enforces
              @DotSeparatedIdentifiers String original = packageOrClassLineMatcher.group(1);
              @SuppressWarnings("signature:assignment.type.incompatible") // regex match enforces
              @DotSeparatedIdentifiers String replacement = packageOrClassLineMatcher.group(2);
              addReplacementsForClassOrPackage(replacementMap, original, replacement);
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
   * Adds a method replacement described by a pair of method signatures to the replacement map.
   * Verifies that both signatures are well-formed and correspond to a method that exists.
   *
   * <p>See {@link MethodSignature#of(String)} for the expected format of a method signature.
   *
   * @param replacementMap the map from a method to a replacement method
   * @param originalSignature the signature of the method to be replaced
   * @param replacementSignature the signature of the replacement method
   * @throws ReplacementException if either signature is badly-formed
   * @throws ClassNotFoundException if either class cannot be found
   * @throws IllegalClassFormatException if either class has an illegal format
   * @throws NoSuchMethodException if either method cannot be found
   */
  private static void addMethodReplacement(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      String originalSignature,
      String replacementSignature)
      throws ReplacementException, ClassNotFoundException, IllegalClassFormatException,
          NoSuchMethodException {

    MethodSignature original;
    try {
      original = MethodSignature.of(originalSignature);
    } catch (IllegalArgumentException e) {
      throw new ReplacementException("Bad original signature", e);
    }
    // Call toMethod() instead of exists() to get more precise error messages.
    original.toMethod();

    MethodSignature replacement;
    try {
      replacement = MethodSignature.of(replacementSignature);
    } catch (IllegalArgumentException e) {
      throw new ReplacementException("Bad replacement signature", e);
    }
    // Call toMethod() instead of exists() to get more precise error messages.
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
   * @throws IOException if there is an error while reading the file
   * @throws ClassNotFoundException if no class corresponding to the replacement is found
   */
  private static void addReplacementsForClassOrPackage(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      @DotSeparatedIdentifiers String original,
      @DotSeparatedIdentifiers String replacement)
      throws ReplacementException, IOException, ClassNotFoundException {

    String replacementClassPath = replacement.replace('.', java.io.File.separatorChar) + ".class";
    URL resource = ClassLoader.getSystemResource(replacementClassPath);

    // If the resource exists, then the replacement is a class.
    if (resource != null) {
      addReplacementsForClass(replacementMap, original, replacement);
    } else {
      // Otherwise, assume the replacement is a package.
      addReplacementsForPackage(replacementMap, original, replacement);
    }
  }

  /**
   * Adds method replacements determined by original and replacement class.
   *
   * <p>This is a wrapper around {@link #addReplacementsForClass(HashMap, String, String)}.
   *
   * @param replacementMap the replacement map to which new replacements are added
   * @param originalPackage the name of the package containing the original class
   * @param replacementPackage the name of the package containing the replacement class
   * @param classname the class's simple name
   * @throws ClassNotFoundException if either the original or replacement class cannot be loaded
   * @throws ReplacementException if a replacement method is not static, or has no matching
   *     original, or if the replacement class cannot be found
   */
  private static void addReplacementsForClass(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      @DotSeparatedIdentifiers String originalPackage,
      @DotSeparatedIdentifiers String replacementPackage,
      @BinaryName String classname)
      throws ClassNotFoundException, ReplacementException {
    addReplacementsForClass(
        replacementMap,
        Signatures.addPackage(originalPackage, classname),
        Signatures.addPackage(replacementPackage, classname));
  }

  /**
   * Adds method replacements determined by original and replacement class.
   *
   * <p>For each non-private static method in the replacement class, the original class is checked
   * for a corresponding method. This original method may either be non-static, where the receiver
   * is translated to the first argument of the replacement, or static, having the same arguments as
   * the replacement.
   *
   * <p>Note: This method will not overwrite an existing replacement method.
   *
   * @param replacementMap the replacement map to which new replacements are added
   * @param originalClassname the name of the original class
   * @param replacementClassname the name of the replacement class
   * @throws ClassNotFoundException if either the original or replacement class cannot be loaded
   * @throws ReplacementException if a replacement method is not static, or has no matching
   *     original, or if the replacement class cannot be found
   */
  private static void addReplacementsForClass(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      @ClassGetName String originalClassname,
      @ClassGetName String replacementClassname)
      throws ClassNotFoundException, ReplacementException {

    // Check that replacement class exists
    JavaClass replacementJC = getJavaClassFromClassname(replacementClassname);
    if (replacementJC == null) {
      throw new ReplacementException("Replacement class not found: " + replacementClassname);
    }

    for (Method m : replacementJC.getMethods()) {
      if (m.getName().equals("<init>")) {
        // Do not to replace the original class constructor with the replacement class constructor.
        continue;
      }
      if (m.isPrivate()) {
        // A replacement class may have private helper methods; quietly ignore them.
        continue;
      }
      if (!m.isStatic()) {
        String msg =
            String.format(
                "Non-static non-private replacement method found: %s.%s",
                replacementClassname, m.getName());
        throw new ReplacementException(msg);
      }

      MethodSignature replacement = MethodSignature.of(replacementClassname, m);

      MethodSignature original = replacement.substituteClassname(originalClassname);
      if (original.exists()) {
        addReplacement(replacementMap, original, replacement);
        continue;
      }

      // Check if the replacement might correspond to a non-static original
      if (replacement.getParameterTypes().length > 0
          && replacement.getParameterTypes()[0].equals(new ObjectType(originalClassname))) {
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
   * Adds method replacements determined by an original and replacement package. Uses
   * getSystemResources to find the replacement package.
   *
   * <p>Visits each class of the package on the classpath and applies {@link
   * #addReplacementsForClass(HashMap, String, Class)} to add the method replacements.
   *
   * @param replacementMap the method replacement map to which new replacements are added
   * @param originalPackage the original package name
   * @param replacementPackage the replacement package name
   * @throws ReplacementException if no package corresponding to the replacement is found
   * @throws ClassNotFoundException if no class corresponding to the replacement is found
   * @see #addReplacementsForClassOrPackage(HashMap, String, String)
   */
  private static void addReplacementsForPackage(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      @DotSeparatedIdentifiers String originalPackage,
      @DotSeparatedIdentifiers String replacementPackage)
      throws ReplacementException, ClassNotFoundException {

    if (ReplaceCallAgent.debug) {
      System.err.println("javaVersion: " + System.getProperty("java.version"));
      System.err.println("bootclasspath: " + System.getProperty("sun.boot.class.path"));
      System.err.println("javaHome: " + System.getProperty("java.class.path"));
      System.err.println("classpath: " + System.getProperty("java.home"));
    }

    // We will only process the first occurance found; the boot classpath
    // is searched prior to the system classpath.
    String replacementPackagePath = replacementPackage.replace('.', java.io.File.separatorChar);
    URL url = ClassLoader.getSystemResource(replacementPackagePath);
    if (url == null) {
      String msg =
          String.format("No package for replacement %s found on classpath", replacementPackage);
      throw new ReplacementException(msg);
    }

    String protocol = url.getProtocol();
    if (protocol.equals("jar")) {
      String jarFilePath = ReplaceCallAgent.getJarPathFromURL(url);
      Path file = Paths.get(jarFilePath);
      try {
        JarFile jarFile = new JarFile(file.toFile());
        addReplacementsFromAllClassesOfPackage(
            replacementMap, originalPackage, replacementPackage, jarFile);
        return;
      } catch (IOException e) {
        throw new ReplacementException("Error reading jar file: " + file, e);
      }
    } else if (protocol.equals("file")) {
      Path path = null;
      try {
        path = Paths.get(URLDecoder.decode(url.getPath(), "UTF-8"));
      } catch (Exception e) {
        throw new ReplacementException("Unable to extract Path from URL: " + url, e);
      }
      if (Files.exists(path) && Files.isDirectory(path)) {
        addReplacementsForPackage(replacementMap, originalPackage, replacementPackage, path);
        return;
      }
    } else {
      throw new ReplacementException("URL protocol not 'file' or 'jar'");
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
   */
  private static void addReplacementsForPackage(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      @DotSeparatedIdentifiers String originalPackage,
      @DotSeparatedIdentifiers String replacementPackage,
      Path replacementDirectory)
      throws ReplacementException, ClassNotFoundException {

    for (File file : replacementDirectory.toFile().listFiles()) {
      String filename = file.getName();
      if (file.isFile()) {
        if (filename.endsWith(".class")) {
          addReplacementsForClass(
              replacementMap,
              originalPackage,
              replacementPackage,
              Signatures.classfilenameToBinaryName(filename));
        }
      } else if (file.isDirectory()) {
        @SuppressWarnings(
            "signature:assignment.type.incompatible") // add identifier to dot-separated
        @DotSeparatedIdentifiers String originalPackageRecurse = originalPackage + "." + filename;
        @SuppressWarnings(
            "signature:assignment.type.incompatible") // add identifier to dot-separated
        @DotSeparatedIdentifiers String replacementPackageRecurse = replacementPackage + "." + filename;
        addReplacementsForPackage(
            replacementMap, originalPackageRecurse, replacementPackageRecurse, file.toPath());
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
   * @throws ReplacementException if no replacements are found in the replacement package
   * @throws ClassNotFoundException if no class corresponding to the replacement is found
   * @see #addReplacementsForPackage(HashMap, String, String)
   */
  private static void addReplacementsFromAllClassesOfPackage(
      HashMap<MethodSignature, MethodSignature> replacementMap,
      @DotSeparatedIdentifiers String originalPackage,
      @DotSeparatedIdentifiers String replacementPackage,
      JarFile jarFile)
      throws ReplacementException, ClassNotFoundException {

    String replacementPath = replacementPackage.replace('.', '/') + "/";
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      String filename = entry.getName();
      if (filename.endsWith(".class") && filename.startsWith(replacementPath)) {
        addReplacementsForClass(
            replacementMap,
            originalPackage,
            replacementPackage,
            Signatures.classfilenameToBinaryName(filename));
      }
    }
  }

  private static Map<String, JavaClass> javaClasses = new ConcurrentHashMap<String, JavaClass>();

  /**
   * Returns a JavaClass object for the given class name. Works by trying to find a class file and
   * loading it into a JavaClass object.
   *
   * @param classname name of class to find and load
   * @return JavaClass object or null if not found
   * @throws ReplacementException if any error loading and converting class file
   */
  protected static JavaClass getJavaClassFromClassname(String classname)
      throws ReplacementException {

    JavaClass c = javaClasses.get(classname);
    if (c != null) {
      return c;
    }
    String classFilename = classname.replace('.', java.io.File.separatorChar) + ".class";
    InputStream is = ClassLoader.getSystemResourceAsStream(classFilename);
    if (is == null) {
      return null; // class not found
    }

    // Parse the bytes of the classfile, die on any errors
    try {
      ClassParser parser = new ClassParser(is, classname);
      c = parser.parse();
      javaClasses.put(classname, c);
      return c;
    } catch (Exception e) {
      if (ReplaceCallAgent.debug) {
        e.printStackTrace();
      }
      throw new ReplacementException("Error reading class file: " + classFilename, e);
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
