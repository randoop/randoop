package randoop.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Partial;
import org.joda.time.base.AbstractInstant;
import org.joda.time.base.AbstractInterval;
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

  /** The Joda-Time patterns omit the operations that read the clock, and no others. */
  @Test
  public void testJodaTime() throws Exception {
    List<Pattern> patterns = readPatterns("/omitmethods-defaults.txt");

    assertOmitted(patterns, RawSignature.of(LocalDate.class.getConstructor()));
    assertOmitted(patterns, RawSignature.of(LocalDate.class.getConstructor(DateTimeZone.class)));
    assertOmitted(patterns, RawSignature.of(LocalDate.class.getConstructor(Chronology.class)));
    assertOmitted(patterns, RawSignature.of(LocalDate.class.getConstructor(Object.class)));
    assertOmitted(
        patterns,
        RawSignature.of(LocalDate.class.getConstructor(Object.class, DateTimeZone.class)));
    assertOmitted(
        patterns, RawSignature.of(LocalDate.class.getConstructor(Object.class, Chronology.class)));
    assertOmitted(patterns, RawSignature.of(LocalDate.class.getMethod("now")));
    assertOmitted(patterns, RawSignature.of(DateTime.class.getConstructor()));
    assertOmitted(patterns, RawSignature.of(DateTime.class.getMethod("now", DateTimeZone.class)));
    assertOmitted(patterns, RawSignature.of(Instant.class.getConstructor()));
    assertOmitted(patterns, RawSignature.of(Instant.class.getConstructor(Object.class)));
    assertOmitted(patterns, RawSignature.of(Instant.class.getMethod("now")));
    assertOmitted(patterns, RawSignature.of(Interval.class.getConstructor(Object.class)));
    assertOmitted(patterns, RawSignature.of(AbstractInstant.class.getMethod("isAfterNow")));
    assertOmitted(patterns, RawSignature.of(AbstractInterval.class.getMethod("containsNow")));

    assertNotOmitted(
        patterns, RawSignature.of(LocalDate.class.getConstructor(int.class, int.class, int.class)));
    assertNotOmitted(patterns, RawSignature.of(LocalDate.class.getConstructor(long.class)));
    assertNotOmitted(
        patterns, RawSignature.of(DateTime.class.getConstructor(long.class, DateTimeZone.class)));
    assertNotOmitted(patterns, RawSignature.of(Partial.class.getConstructor()));
    assertNotOmitted(patterns, RawSignature.of(Partial.class.getConstructor(Chronology.class)));
    assertNotOmitted(
        patterns, RawSignature.of(Interval.class.getConstructor(long.class, long.class)));
    assertNotOmitted(patterns, RawSignature.of(LocalDate.class.getMethod("plusDays", int.class)));
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
   * Asserts that some pattern matches the signature.
   *
   * @param patterns omit patterns
   * @param signature a signature
   */
  private static void assertOmitted(List<Pattern> patterns, RawSignature signature) {
    assertTrue(signature.toString(), isOmitted(patterns, signature));
  }

  /**
   * Asserts that no pattern matches the signature.
   *
   * @param patterns omit patterns
   * @param signature a signature
   */
  private static void assertNotOmitted(List<Pattern> patterns, RawSignature signature) {
    assertFalse(signature.toString(), isOmitted(patterns, signature));
  }

  /**
   * Returns true if some pattern matches the signature, as {@link OmitMethodsPredicate} does.
   *
   * @param patterns omit patterns
   * @param signature a signature
   * @return true if some pattern matches the signature
   */
  private static boolean isOmitted(List<Pattern> patterns, RawSignature signature) {
    String signatureString = signature.toString();
    for (Pattern pattern : patterns) {
      if (pattern.matcher(signatureString).find()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the patterns in the given resource.
   *
   * @param resourceName the name of a resource that contains omit patterns
   * @return the patterns in the resource
   */
  private static List<Pattern> readPatterns(String resourceName) throws IOException {
    List<Pattern> result = new ArrayList<>();
    for (String line : readLines(resourceName)) {
      result.add(Pattern.compile(line));
    }
    return result;
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
