package randoop.instrument;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.bcel.generic.Type;
import plume.BCELUtil;

/**
 * See the <a href="https://randoop.github.io/randoop/manual/index.html#map_calls">mapcall user
 * documentation</a> for details on the file format.
 */
class ReplacementFileReader {

  /** Regex string for Java identifiers */
  private static final String ID_STRING =
      "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

  /**
   * String for a regex to match fully-qualified package or class name as a sequence of identifiers
   * separated by periods.
   */
  private static final String PACKAGE_OR_CLASS_STRING = ID_STRING + "(\\." + ID_STRING + ")*";

  /**
   * String for naive regex to match a method signature as a string followed by a string enclosed in
   * parantheses. Does not actually check that the form of method-name or arguments is correct.
   */
  //private static final String SIGNATURE_STRING = "[^(]+\\([^)]*\\)";
  private static final String SIGNATURE_STRING = PACKAGE_OR_CLASS_STRING + "\\([^)]*\\)";
  /**
   * String for naive regex to match a method signature, with groups to extract method name and type
   * argument list.
   */
  //private static final String SIGNATURE_GROUP_STRING = "([^(]+)\\(([^)]*)\\)";
  private static final String SIGNATURE_GROUP_STRING =
      "(" + PACKAGE_OR_CLASS_STRING + ")" + "\\(([^)]*)\\)";

  /** Pattern to match naive signature regex with groups. */
  private static final Pattern SIGNATURE_PATTERN = Pattern.compile(SIGNATURE_GROUP_STRING);

  /**
   * Pattern to match of method replacements consisting of a pair of signatures. Uses naive
   * signature pattern separated by one or more spaces or tabs. Can be used to decide whether a
   * replacement file line could be method replacements. Has two groups that allow extracting the
   * "signature" strings.
   */
  private static final Pattern SIGNATURE_LINE =
      Pattern.compile("(" + SIGNATURE_STRING + ")[ \\t]+(" + SIGNATURE_STRING + ")");

  /**
   * Pattern to match class or package replacements consisting of a pair of class or package name
   * signatures. has two groups that allow extracting the substrings in the pair.
   */
  private static final Pattern PACKAGE_OR_CLASS_LINE =
      Pattern.compile(
          "(" + PACKAGE_OR_CLASS_STRING + ")[ \\t]+" + "(" + PACKAGE_OR_CLASS_STRING + ")");

  /**
   * Reads the replacement file specifying method calls that should be replaced by other method
   * calls. See the <a href="https://randoop.github.io/randoop/manual/index.html#map_calls">mapcall
   * user documentation</a> for details on the file format.
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
          readPackageOrClassLine(
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

  private static void readPackageOrClassLine(
      ConcurrentHashMap<MethodDef, MethodDef> replacementsMap, String original, String replacement)
      throws ReplacementFileException {

    ClassLoader loader = ReplacementFileReader.class.getClassLoader();
    Class<?> replacementClass = null;
    try {
      replacementClass = loader.loadClass(replacement);
    } catch (ClassNotFoundException e) {
      //System.out.println("not a class");
    }

    if (replacementClass != null) { // replacement is a class
      addClassReplacements(replacementsMap, original, replacementClass);
      return;
    }

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
          System.out.println("jar file");
        } else { //assume connection is a directory, at some point it will break
          File path = new File(URLDecoder.decode(url.getPath(), "UTF-8"));
          if (path.exists() && path.isDirectory()) {
            addPackageReplacements(replacementsMap, original, replacement, path);
            return;
          }
        }
      } catch (IOException e) {
        throw new ReplacementFileException(e.getMessage());
      }
    }
    String msg = String.format("No package or class for replacement %s", replacement);
    throw new ReplacementFileException(msg);
  }

  private static void addPackageReplacements(
      ConcurrentHashMap<MethodDef, MethodDef> replacementsMap,
      String original,
      String replacement,
      File packageDirectory) {
    for (String filename : packageDirectory.list()) {
      if (filename.endsWith(".class")) {
        final String classname = filename.substring(0, filename.lastIndexOf(".class"));
        final String originalClassname = original + "." + classname;
        if (classExists(originalClassname)) {
          try {
            Class<?> replacementClass = Class.forName(replacement + "." + classname);
            addClassReplacements(replacementsMap, originalClassname, replacementClass);
          } catch (ClassNotFoundException e) {
            // ignore
          }
        }
      } else {
        File directory = new File(packageDirectory, filename);
        if (directory.exists() && directory.isDirectory()) {
          System.out.println("found subdirectory");
        }
      }
    }
  }

  private static boolean classExists(String classname) {
    try {
      return Class.forName(classname) != null;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private static void addClassReplacements(
      ConcurrentHashMap<MethodDef, MethodDef> replacementsMap,
      String originalClassname,
      Class<?> replacementClass) {
    for (Method method : replacementClass.getDeclaredMethods()) {
      MethodDef replacementDef = MethodDef.of(method);
      if (!addNonStaticOriginal(replacementsMap, originalClassname, replacementDef)) {
        MethodDef staticDef =
            new MethodDef(
                originalClassname, replacementDef.getName(), replacementDef.getArgTypes());
        if (staticDef.exists()) {
          replacementsMap.put(staticDef, replacementDef);
        } else {
          System.err.println(
              "replacement method "
                  + replacementDef
                  + " has no matching original in "
                  + originalClassname);
        }
      }
    }
  }

  private static boolean addNonStaticOriginal(
      ConcurrentHashMap<MethodDef, MethodDef> replacementsMap,
      String originalClassname,
      MethodDef replacementDef) {
    if (replacementDef.getArgTypes().length > 0
        && replacementDef.getArgTypes()[0].equals(BCELUtil.classname_to_type(originalClassname))) {
      // check for original with both
      Type[] argTypes = replacementDef.getArgTypes();
      Type[] newArgTypes = new Type[argTypes.length - 1];
      System.arraycopy(argTypes, 1, newArgTypes, 0, argTypes.length - 1);
      MethodDef nonStaticDef =
          new MethodDef(originalClassname, replacementDef.getName(), newArgTypes);
      if (nonStaticDef.exists()) {
        replacementsMap.put(nonStaticDef, replacementDef);
        return true;
      }
    }
    return false;
  }
}
