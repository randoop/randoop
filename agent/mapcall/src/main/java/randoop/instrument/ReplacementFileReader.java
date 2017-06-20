package randoop.instrument;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
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
import plume.BCELUtil;

/**
 * Provides the methods {@link #readFile(Reader)} and {@link #readFile(File)} that read a MapCall
 * agent replacement file and populate the method replacement map used by the agent. See the <a
 * href="https://randoop.github.io/randoop/manual/index.html#map_calls">mapcall user
 * documentation</a> for details on the file format.
 */
class ReplacementFileReader {

  /** Regex string for Java identifiers */
  private static final String ID_STRING =
      "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

  /**
   * String for a regex to match fully-qualified package or class name as a sequence of identifiers
   * separated by periods.
   *
   * <p>Note: when included in more complex expressions that use groups, the parentheses in this
   * expression also form a group.
   */
  private static final String PACKAGE_OR_CLASS_STRING = ID_STRING + "(\\." + ID_STRING + ")*";

  /**
   * String for naive regex to match a method signature as a sequence of period-delimited
   * identifiers followed by a string enclosed in parentheses. Does not actually check that the form
   * of the arguments is correct.
   */
  private static final String SIGNATURE_STRING = PACKAGE_OR_CLASS_STRING + "\\([^)]*\\)";

  /**
   * String for naive regex to match a method signature with groups to extract the fully-qualified
   * method name (group 1) and type argument list (group 3). (Group 2 is the group from {@link
   * #PACKAGE_OR_CLASS_STRING}.)
   */
  private static final String SIGNATURE_GROUP_STRING =
      "(" + PACKAGE_OR_CLASS_STRING + ")" + "\\(([^)]*)\\)";

  /** Pattern to match naive signature regex with groups. */
  private static final Pattern SIGNATURE_PATTERN = Pattern.compile(SIGNATURE_GROUP_STRING);

  /**
   * Pattern to match of method replacements consisting of a pair of signatures. Uses the naive
   * signature pattern separated by one or more spaces or tabs. Can be used to decide whether a
   * replacement file line indicates a method replacement. Groups 1 and 3 correspond to each of the
   * signature strings.
   */
  private static final Pattern SIGNATURE_LINE =
      Pattern.compile("(" + SIGNATURE_STRING + ")[ \\t]+(" + SIGNATURE_STRING + ")");

  /**
   * Pattern to match class or package replacements consisting of a pair of class or package name
   * signatures. Groups 1 and 3 correspond to each of the package or class names.
   */
  private static final Pattern PACKAGE_OR_CLASS_LINE =
      Pattern.compile(
          "(" + PACKAGE_OR_CLASS_STRING + ")[ \\t]+" + "(" + PACKAGE_OR_CLASS_STRING + ")");

  /**
   * Reads the given replacement file specifying method calls that should be replaced by other
   * method calls. See the <a
   * href="https://randoop.github.io/randoop/manual/index.html#map_calls">mapcall user
   * documentation</a> for details on the file format.
   *
   * @param map_file the file with map of method substitutions
   * @throws IOException if there is an error reading the file
   */
  static ConcurrentHashMap<MethodDef, MethodDef> readFile(File map_file)
      throws IOException, ReplacementFileException {
    return readFile(new FileReader(map_file));
  }

  /**
   * Reads the replacement file specifying method calls that should be replaced by other method
   * calls. See the <a href="https://randoop.github.io/randoop/manual/index.html#map_calls">mapcall
   * user documentation</a> for details on the file format.
   *
   * @param in the {@code Reader} for the replacement file
   * @throws IOException if there is an error reading from the file
   */
  static ConcurrentHashMap<MethodDef, MethodDef> readFile(Reader in)
      throws IOException, ReplacementFileException {
    ConcurrentHashMap<MethodDef, MethodDef> replacementMap = new ConcurrentHashMap<>();
    LineNumberReader lr = new LineNumberReader(in);
    for (String line = lr.readLine(); line != null; line = lr.readLine()) {
      line = line.replaceFirst("//.*$", "").trim();
      if (line.length() == 0) {
        continue;
      }
      Matcher signatureLineMatcher = SIGNATURE_LINE.matcher(line);
      if (signatureLineMatcher.matches()) {
        try {
          // note that group 2 and 4 correspond to the last ".identifier" portion of method name
          addMethodReplacement(
              replacementMap, signatureLineMatcher.group(1), signatureLineMatcher.group(3));
        } catch (ReplacementFileException e) {
          String msg =
              String.format("Error in replacement file: %s on line %n%s%n", e.getMessage(), line);
          throw new ReplacementFileException(msg);
        }
      } else {
        Matcher packageOrClassLineMatcher = PACKAGE_OR_CLASS_LINE.matcher(line);
        if (packageOrClassLineMatcher.matches()) {
          addPackageOrClassReplacements(
              replacementMap,
              packageOrClassLineMatcher.group(1),
              packageOrClassLineMatcher.group(3));
        } else {
          String msg = String.format("Error in replacement file: bad format on line %n%s%n", line);
          throw new ReplacementFileException(msg);
        }
      }
    }
    return replacementMap;
  }

  /**
   * Adds a method replacement described by a pair of method signatures to the replacement map. Uses
   * reflection to check that the replacement method signature is well-formed and corresponds to a
   * method that exists. (Doing the same with the original signature may result in class loader
   * issues.)
   *
   * <p>Note: this method will overwrite a replacement for an original method, which ensures that
   * the most specific replacement definition is used.
   *
   * @param replacementMap the map from a method to a replacement method
   * @param originalSignature the signature of the method to be mapped
   * @param replacementSignature the signature of the method to be mapped
   * @throws ReplacementFileException if the replacement signature is badly-formed, or the class or
   *     method does not exist
   */
  private static void addMethodReplacement(
      ConcurrentHashMap<MethodDef, MethodDef> replacementMap,
      String originalSignature,
      String replacementSignature)
      throws ReplacementFileException {
    MethodDef orig = getMethodDef(originalSignature);
    if (orig != null) {
      MethodDef replacement = getMethodDef(replacementSignature);
      try {
        // check that replacement exists in a way that allows reporting errors
        if (replacement != null && replacement.toMethod() != null) {
          replacementMap.put(orig, replacement);
        }
      } catch (ClassNotFoundException e) {
        String msg = "Class " + replacement.getClassname() + " not found for replacement method";
        throw new ReplacementFileException(msg);
      } catch (NoSuchMethodException e) {
        throw new ReplacementFileException("Replacement method not found " + replacement);
      } catch (RuntimeException e) {
        throw new ReplacementFileException("In replacement method: " + e.getMessage());
      }
    }
  }

  /**
   * Reads a signature string and builds the corresponding {@link MethodDef}.
   *
   * @param signature the fully-qualified method signature string
   * @return the {@link MethodDef} for the method represented by the signature string
   * @throws ReplacementFileException if the given string does not have the format of a signature
   */
  private static MethodDef getMethodDef(String signature) throws ReplacementFileException {
    MethodDef method = null;
    Matcher sigMatcher = SIGNATURE_PATTERN.matcher(signature);
    if (sigMatcher.matches()) {
      if (Pattern.matches(PACKAGE_OR_CLASS_STRING, sigMatcher.group(1).trim())) {
        String[] arguments = new String[0];
        String argString = sigMatcher.group(3);
        if (!argString.isEmpty()) {
          arguments = argString.split(",");
        }
        method = MethodDef.of(sigMatcher.group(1).trim(), arguments);
      } else {
        String msg = String.format("Signature \"%s\" has badly formed method name", signature);
        throw new ReplacementFileException(msg);
      }
    }
    return method;
  }

  /**
   * Discovers and adds the method replacements for the package or class replacement determined by
   * the {@code original} and {@code replacement} strings. This method determines whether the
   * replacement corresponds to a either a class or a package. If the replacement is a class, then
   * it will check whether every method in the class has a corresponding method in the original, and
   * if so adds it to the {@code replacementMap}. If the replacement is a package, then each class
   * in the package is visited in the same way.
   *
   * @param replacementMap the method replacement map to which new replacements are added
   * @param original the original package or class name
   * @param replacement the replacement package or class name
   * @throws ReplacementFileException if the replacement does not correspond to a package or class
   *     on the classpath
   */
  private static void addPackageOrClassReplacements(
      ConcurrentHashMap<MethodDef, MethodDef> replacementMap, String original, String replacement)
      throws ReplacementFileException {

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
      // Otherwise, determine if the replacement is a package
      // How depends on whether the agent is run on bootloaded classes, which is the case if the
      // ClassLoader is null
      ClassLoader loader = ReplacementFileReader.class.getClassLoader();
      if (loader == null) {
        // If the agent is run on bootloaded classes, we have to check the path directly.
        findPackageReplacements(replacementMap, original, replacement);
      } else {
        // Otherwise, use the ClassLoader to avoid missing any classes on the normal classpath.
        findPackageReplacements(replacementMap, original, replacement, loader);
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
   * @param replacementMap the replacement map
   * @param originalClassname the name of the original class
   * @param replacementClass the {@code Class<>} for the replacement class
   */
  private static void addClassReplacements(
      ConcurrentHashMap<MethodDef, MethodDef> replacementMap,
      String originalClassname,
      Class<?> replacementClass)
      throws ReplacementFileException {
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

      MethodDef replacementDef = MethodDef.of(method);

      MethodDef originalDef = replacementDef.substituteClassname(originalClassname);
      if (originalDef.exists()) {
        // If there is already a replacement, do not overwrite it
        if (replacementMap.get(originalDef) == null) {
          replacementMap.put(originalDef, replacementDef);
        }
        continue;
      }

      // Check if the replacement might correspond to a non-static original
      if (replacementDef.getParameterTypes().length > 0
          && replacementDef.getParameterTypes()[0]
              .equals(BCELUtil.classname_to_type(originalClassname))) {
        originalDef = originalDef.removeFirstParameter();
        if (originalDef.exists()) {
          // If there is already a replacement, do not overwrite it
          if (replacementMap.get(originalDef) == null) {
            replacementMap.put(originalDef, replacementDef);
          }
          continue;
        }
      }

      String msg =
          String.format(
              "Replacement method %s has no matching original in %s%n",
              replacementDef, originalClassname);
      throw new ReplacementFileException(msg);
    }
  }

  /**
   * Discovers and adds method replacements for the package replacement determined by the {@code
   * original} and {@code replacement} strings and the classes on the boot classpath.
   *
   * @see #addPackageOrClassReplacements(ConcurrentHashMap, String, String)
   * @param replacementMap the method replacement map to which new replacements are added
   * @param original the original package name
   * @param replacement the replacement package name
   * @throws ReplacementFileException if no package corresponding to the replacement is found
   */
  private static void findPackageReplacements(
      ConcurrentHashMap<MethodDef, MethodDef> replacementMap, String original, String replacement)
      throws ReplacementFileException {
    String bootpathString = System.getProperty("sun.boot.class.path");
    String javaHome = System.getProperty("java.home");

    String[] pathToks = bootpathString.split(File.pathSeparator);
    boolean found = false;
    for (String pathString : pathToks) {
      if (!pathString.startsWith(javaHome)) {
        File file = new File(pathString);
        if (file.exists()) { // either a directory
          if (file.isDirectory()) {
            Path path = file.toPath();
            Path replacementPath = path.resolve(replacement.replace('.', File.separatorChar));
            if (Files.exists(replacementPath) && Files.isDirectory(replacementPath)) {
              addPackageReplacements(
                  replacementMap, original, replacement, replacementPath.toFile());
              found = true; // directory for package was found
            }
          } else { // or a jar file
            try {
              JarFile jarFile = new JarFile(file);
              if (addPackageReplacements(replacementMap, original, replacement, jarFile)) {
                found = true;
              }
            } catch (IOException e) {
              // ignore
            }
          }
        }
      }
    }
    if (!found) {
      String msg =
          String.format(
              "No package or class for replacement %s found on the boot classpath", replacement);
      throw new ReplacementFileException(msg);
    }
  }

  /**
   * Discovers and adds method replacements for the package replacement determined by the {@code
   * original} and {@code replacement} strings and the given {@code ClassLoader}.
   *
   * @see #addPackageOrClassReplacements(ConcurrentHashMap, String, String)
   * @param replacementMap the method replacement map to which new replacements are added
   * @param original the original package name
   * @param replacement the replacement package name
   * @param loader the {@code ClassLoader}
   * @throws ReplacementFileException if no package corresponding to replacement is found
   */
  private static void findPackageReplacements(
      ConcurrentHashMap<MethodDef, MethodDef> replacementMap,
      String original,
      String replacement,
      ClassLoader loader)
      throws ReplacementFileException {
    Enumeration<URL> resources;
    try {
      resources = loader.getResources(replacement.replace('.', '/'));
    } catch (IOException e) {
      throw new ReplacementFileException(e.getMessage());
    }
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      try {
        URLConnection connection = url.openConnection();
        if (connection instanceof JarURLConnection) {
          JarFile jarFile = ((JarURLConnection) connection).getJarFile();
          addPackageReplacements(replacementMap, original, replacement, jarFile);
        } else {
          // The subclass for directories is is internal.  It seems to work to assume the
          // connection is a directory, and let an exception occur if it is not
          File path = new File(URLDecoder.decode(url.getPath(), "UTF-8"));
          if (path.exists() && path.isDirectory()) {
            addPackageReplacements(replacementMap, original, replacement, path);
            return;
          }
        }
      } catch (IOException e) {
        String msg =
            String.format(
                "Error identifying replacement %s with a package: %s", replacement, e.getMessage());
        throw new ReplacementFileException(msg);
      }
    }
    String msg =
        String.format("No package or class for replacement %s found on classpath", replacement);
    throw new ReplacementFileException(msg);
  }

  /**
   * Discovers and adds method replacements for the package replacement determined by the {@code
   * original} and {@code replacement} strings found in the given {@code ClassLoader}.
   *
   * @param replacementMap the method replacement map to which new replacements are added
   * @param original the original package name
   * @param replacement the replacement package name
   * @param jarFile the jar file to search
   * @return true if a class in the replacement package is found in the jar, false otherwise
   */
  private static boolean addPackageReplacements(
      ConcurrentHashMap<MethodDef, MethodDef> replacementMap,
      String original,
      String replacement,
      JarFile jarFile)
      throws ReplacementFileException {

    boolean found = false;
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      String filename = entry.getName();
      if (filename.endsWith(".class") && filename.startsWith(replacement.replace('.', '/'))) {
        final String classname =
            filename.substring(replacement.length() + 1, filename.lastIndexOf(".class"));
        final String originalClassname = original + "." + classname;
        if (classExists(originalClassname)) {
          try {
            Class<?> replacementClass = Class.forName(replacement + "." + classname);
            addClassReplacements(replacementMap, originalClassname, replacementClass);
            found = true;
          } catch (ClassNotFoundException e) {
            // ignore
          }
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
   * @param replacementMap the method replacement map
   * @param original the name of the original package
   * @param replacement the name of the replacement package
   * @param replacementDirectory the directory for the replacement package, must be non-null
   */
  private static void addPackageReplacements(
      ConcurrentHashMap<MethodDef, MethodDef> replacementMap,
      String original,
      String replacement,
      File replacementDirectory)
      throws ReplacementFileException {

    for (String filename : replacementDirectory.list()) {
      if (filename.endsWith(".class")) {
        final String classname = filename.substring(0, filename.lastIndexOf(".class"));
        final String originalClassname = original + "." + classname;
        if (classExists(originalClassname)) {
          try {
            Class<?> replacementClass = Class.forName(replacement + "." + classname);
            addClassReplacements(replacementMap, originalClassname, replacementClass);
          } catch (ClassNotFoundException e) {
            // ignore
          }
        }
      } else {
        File subdirectory = new File(replacementDirectory, filename);
        if (subdirectory.exists() && subdirectory.isDirectory()) {
          addPackageReplacements(
              replacementMap, original + filename, replacement + filename, subdirectory);
        }
      }
    }
  }

  /**
   * Indicates whether a class with the given name can be loaded in the context classpath.
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
}
