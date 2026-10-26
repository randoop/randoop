package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/** Tests the default omit-methods files, which Randoop reads unless --omit-methods-no-defaults. */
public class DefaultOmitMethodsTest {

  /** The default omit-methods files, as resource names. */
  private static final String[] DEFAULT_FILES = {
    "/omitmethods-defaults.txt", "/JDK-nondet-methods.txt"
  };

  /** Matches a pattern that quotes an entire signature; group 1 is the signature. */
  private static final Pattern EXACT_SIGNATURE_PATTERN = Pattern.compile("^\\^\\\\Q(.*)\\\\E\\$$");

  /** Matches the syntax of {@link RawSignature#toString()}. */
  private static final Pattern RAW_SIGNATURE_SYNTAX =
      Pattern.compile("[\\w$]+(\\.[\\w$]+)+\\(([\\w$.]+(\\[\\])*(,[\\w$.]+(\\[\\])*)*)?\\)");

  /**
   * Packages that are not on the test classpath. A signature in one of them is checked only for
   * syntax.
   */
  private static final String[] ABSENT_PACKAGES = {"org.apache.commons.math4."};

  /**
   * Every pattern that quotes an entire signature must name a real method or constructor, in the
   * format of {@link RawSignature#toString()}. Otherwise, the pattern never matches anything.
   */
  @Test
  public void testExactSignatures() throws IOException {
    for (String filename : DEFAULT_FILES) {
      for (String line : readLines(filename)) {
        Matcher m = EXACT_SIGNATURE_PATTERN.matcher(line);
        if (!m.matches()) {
          continue;
        }
        String signature = m.group(1);
        assertTrue(
            filename + ": bad syntax: " + signature,
            RAW_SIGNATURE_SYNTAX.matcher(signature).matches());
        if (isInAbsentPackage(signature)) {
          continue;
        }
        AccessibleObject member;
        try {
          member =
              SignatureParser.parse(
                  signature, AccessibilityPredicate.IS_ANY, new EverythingAllowedPredicate());
        } catch (SignatureParseException | FailedPredicateException e) {
          fail(filename + ": " + e.getMessage());
          throw new Error("unreachable");
        }
        RawSignature rawSignature =
            (member instanceof Constructor)
                ? RawSignature.of((Constructor<?>) member)
                : RawSignature.of((Method) member);
        assertEquals(filename, rawSignature.toString(), signature);
      }
    }
  }

  /**
   * Returns true if the signature is in a package that is not on the test classpath.
   *
   * @param signature a signature
   * @return true if the signature is in a package that is not on the test classpath
   */
  private static boolean isInAbsentPackage(String signature) {
    for (String pkg : ABSENT_PACKAGES) {
      if (signature.startsWith(pkg)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the lines of the given resource, omitting blank lines and comments.
   *
   * @param resourceName the name of a resource that contains omit patterns
   * @return the non-blank, non-comment lines of the resource
   */
  private static List<String> readLines(String resourceName) throws IOException {
    List<String> result = new ArrayList<>();
    try (InputStream is = DefaultOmitMethodsTest.class.getResourceAsStream(resourceName);
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.isEmpty() && !line.startsWith("#")) {
          result.add(line);
        }
      }
    }
    return result;
  }
}
