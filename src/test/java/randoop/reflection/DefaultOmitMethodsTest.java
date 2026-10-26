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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.condition.RandoopSpecificationError;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;

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

  /** The predicate that omits the operations that the default omit-methods files match. */
  private static OmitMethodsPredicate defaultOmitPredicate;

  @BeforeClass
  public static void setup() throws IOException {
    List<Pattern> patterns = new ArrayList<>();
    for (String filename : DEFAULT_FILES) {
      for (String line : readLines(filename)) {
        patterns.add(Pattern.compile(line));
      }
    }
    defaultOmitPredicate = new OmitMethodsPredicate(patterns);
  }

  /**
   * Every pattern that quotes an entire signature must name a real method or constructor, in the
   * format of {@link RawSignature#toString()}. Otherwise, the pattern never matches anything.
   */
  @Test
  public void testExactSignatures() throws IOException {
    for (String filename : DEFAULT_FILES) {
      for (String line : readLines(filename)) {
        Matcher m = EXACT_SIGNATURE_PATTERN.matcher(line);
        if (!m.matches() || m.group(1).contains("\\E")) {
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

  @Test
  public void testJavaTimeNow() throws RandoopSpecificationError {
    assertOmitted(java.time.LocalDate.class, "java.time.LocalDate.now()");
    assertOmitted(java.time.LocalDate.class, "java.time.LocalDate.now(java.time.ZoneId)");
    assertNotOmitted(java.time.LocalDate.class, "java.time.LocalDate.now(java.time.Clock)");
    assertNotOmitted(java.time.LocalDate.class, "java.time.LocalDate.of(int,int,int)");

    assertOmitted(java.time.Instant.class, "java.time.Instant.now()");
    assertNotOmitted(java.time.Instant.class, "java.time.Instant.now(java.time.Clock)");

    assertOmitted(java.time.chrono.HijrahDate.class, "java.time.chrono.HijrahDate.now()");
    assertOmitted(
        java.time.chrono.HijrahDate.class, "java.time.chrono.HijrahDate.now(java.time.ZoneId)");
    assertNotOmitted(
        java.time.chrono.HijrahDate.class, "java.time.chrono.HijrahDate.now(java.time.Clock)");
  }

  @Test
  public void testJavaTimeClock() throws RandoopSpecificationError {
    assertOmitted(java.time.Clock.class, "java.time.Clock.systemUTC()");
    assertOmitted(java.time.Clock.class, "java.time.Clock.systemDefaultZone()");
    assertOmitted(java.time.Clock.class, "java.time.Clock.system(java.time.ZoneId)");
    assertOmitted(java.time.Clock.class, "java.time.Clock.tickSeconds(java.time.ZoneId)");
    assertNotOmitted(
        java.time.Clock.class, "java.time.Clock.fixed(java.time.Instant,java.time.ZoneId)");
  }

  @Test
  public void testJavaTimeChronologyDateNow() throws RandoopSpecificationError {
    // IsoChronology overrides Chronology.dateNow(), which the default patterns omit.
    assertOmitted(java.time.chrono.IsoChronology.class, "java.time.chrono.IsoChronology.dateNow()");
    assertOmitted(
        java.time.chrono.IsoChronology.class,
        "java.time.chrono.IsoChronology.dateNow(java.time.ZoneId)");
    assertNotOmitted(
        java.time.chrono.IsoChronology.class,
        "java.time.chrono.IsoChronology.dateNow(java.time.Clock)");
  }

  @Test
  public void testJavaUtilDate() throws RandoopSpecificationError {
    assertOmitted(java.util.Date.class, "java.util.Date()");
    assertNotOmitted(java.util.Date.class, "java.util.Date(long)");
  }

  @Test
  public void testJavaUtilCalendar() throws RandoopSpecificationError {
    assertOmitted(java.util.GregorianCalendar.class, "java.util.GregorianCalendar()");
    assertOmitted(
        java.util.GregorianCalendar.class, "java.util.GregorianCalendar(java.util.TimeZone)");
    assertNotOmitted(java.util.GregorianCalendar.class, "java.util.GregorianCalendar(int,int,int)");

    assertOmitted(java.util.Calendar.class, "java.util.Calendar.getInstance()");
    assertOmitted(
        java.util.Calendar.class,
        "java.util.Calendar.getInstance(java.util.TimeZone,java.util.Locale)");
    assertNotOmitted(java.util.Calendar.class, "java.util.Calendar.get(int)");
  }

  @Test
  public void testSimpleDateFormat() throws RandoopSpecificationError {
    assertOmitted(
        java.text.SimpleDateFormat.class, "java.text.SimpleDateFormat.get2DigitYearStart()");
    assertNotOmitted(
        java.text.SimpleDateFormat.class, "java.text.SimpleDateFormat(java.lang.String)");
    assertNotOmitted(java.text.SimpleDateFormat.class, "java.text.SimpleDateFormat.toPattern()");
  }

  /** A generic comparator. */
  public static class GenericComparator<T> implements Comparator<T> {
    @Override
    public int compare(T o1, T o2) {
      return 0;
    }
  }

  @Test
  public void testGenericComparator() throws RandoopSpecificationError {
    assertNotOmitted(
        GenericComparator.class,
        "randoop.reflection.DefaultOmitMethodsTest$GenericComparator.compare(java.lang.Object,java.lang.Object)");
  }

  @Test
  public void testJodaTimeReadClock() throws RandoopSpecificationError {
    assertOmitted(org.joda.time.DateTime.class, "org.joda.time.DateTime()");
    assertOmitted(
        org.joda.time.DateTime.class, "org.joda.time.DateTime(org.joda.time.DateTimeZone)");
    assertOmitted(org.joda.time.DateTime.class, "org.joda.time.DateTime.now()");
    assertOmitted(org.joda.time.DateTime.class, "org.joda.time.base.AbstractInstant.isAfterNow()");
    assertOmitted(
        org.joda.time.DateTime.class, "org.joda.time.DateTime.now(org.joda.time.DateTimeZone)");
    assertNotOmitted(org.joda.time.DateTime.class, "org.joda.time.DateTime(long)");
    assertNotOmitted(
        org.joda.time.DateTime.class, "org.joda.time.DateTime(long,org.joda.time.DateTimeZone)");
    assertNotOmitted(
        org.joda.time.DateTime.class, "org.joda.time.base.AbstractInstant.isAfter(long)");

    assertOmitted(org.joda.time.LocalDate.class, "org.joda.time.LocalDate()");
    assertOmitted(
        org.joda.time.LocalDate.class, "org.joda.time.LocalDate(org.joda.time.Chronology)");
    assertOmitted(
        org.joda.time.LocalDate.class, "org.joda.time.LocalDate(org.joda.time.DateTimeZone)");
    assertOmitted(org.joda.time.LocalDate.class, "org.joda.time.LocalDate(java.lang.Object)");
    assertOmitted(
        org.joda.time.LocalDate.class,
        "org.joda.time.LocalDate(java.lang.Object,org.joda.time.Chronology)");
    assertOmitted(org.joda.time.LocalDate.class, "org.joda.time.LocalDate.now()");
    assertOmitted(
        org.joda.time.LocalDate.class, "org.joda.time.LocalDate.now(org.joda.time.DateTimeZone)");
    assertOmitted(
        org.joda.time.LocalDate.class, "org.joda.time.LocalDate.toDateTimeAtCurrentTime()");
    assertNotOmitted(org.joda.time.LocalDate.class, "org.joda.time.LocalDate(int,int,int)");
    assertNotOmitted(org.joda.time.LocalDate.class, "org.joda.time.LocalDate(long)");
    assertNotOmitted(org.joda.time.LocalDate.class, "org.joda.time.LocalDate.plusDays(int)");
    assertNotOmitted(
        org.joda.time.LocalDate.class, "org.joda.time.LocalDate.toDateTimeAtStartOfDay()");

    assertOmitted(org.joda.time.Instant.class, "org.joda.time.Instant()");
    assertOmitted(org.joda.time.Instant.class, "org.joda.time.Instant.now()");
    assertOmitted(org.joda.time.Instant.class, "org.joda.time.Instant(java.lang.Object)");
    assertNotOmitted(org.joda.time.Instant.class, "org.joda.time.Instant(long)");

    assertOmitted(org.joda.time.LocalTime.class, "org.joda.time.LocalTime()");
    assertOmitted(
        org.joda.time.LocalTime.class,
        "org.joda.time.LocalTime.toDateTimeToday(org.joda.time.DateTimeZone)");
    assertNotOmitted(org.joda.time.LocalTime.class, "org.joda.time.LocalTime(int,int)");

    assertOmitted(org.joda.time.Interval.class, "org.joda.time.Interval(java.lang.Object)");
    assertOmitted(
        org.joda.time.Interval.class, "org.joda.time.base.AbstractInterval.containsNow()");
    assertNotOmitted(org.joda.time.Interval.class, "org.joda.time.Interval(long,long)");
    assertNotOmitted(
        org.joda.time.Interval.class, "org.joda.time.base.AbstractInterval.contains(long)");

    assertNotOmitted(org.joda.time.Partial.class, "org.joda.time.Partial()");
    assertNotOmitted(
        org.joda.time.Partial.class, "org.joda.time.Partial(org.joda.time.Chronology)");
  }

  @Test
  public void testJodaTimeSetGlobalState() throws RandoopSpecificationError {
    assertOmitted(
        org.joda.time.DateTimeUtils.class, "org.joda.time.DateTimeUtils.currentTimeMillis()");
    assertOmitted(
        org.joda.time.DateTimeUtils.class,
        "org.joda.time.DateTimeUtils.setCurrentMillisFixed(long)");
    assertOmitted(
        org.joda.time.DateTimeUtils.class, "org.joda.time.DateTimeUtils.setCurrentMillisSystem()");
    assertNotOmitted(
        org.joda.time.DateTimeUtils.class,
        "org.joda.time.DateTimeUtils.getInstantMillis(org.joda.time.ReadableInstant)");

    assertOmitted(
        org.joda.time.DateTimeZone.class,
        "org.joda.time.DateTimeZone.setDefault(org.joda.time.DateTimeZone)");
    assertNotOmitted(
        org.joda.time.DateTimeZone.class, "org.joda.time.DateTimeZone.forID(java.lang.String)");
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
   * Asserts that the default omit patterns omit the given operation of {@code c}, and that {@code
   * c} has that operation.
   *
   * @param c a class
   * @param signature the {@link RawSignature#toString()} of a public operation of {@code c}
   * @throws RandoopSpecificationError if Randoop cannot extract the operations of {@code c}
   */
  private static void assertOmitted(Class<?> c, String signature) throws RandoopSpecificationError {
    assertTrue(signature, signatures(c, OmitMethodsPredicate.NO_OMISSION).contains(signature));
    assertFalse(signature, signatures(c, defaultOmitPredicate).contains(signature));
  }

  /**
   * Asserts that the default omit patterns do not omit the given operation of {@code c}, and that
   * {@code c} has that operation.
   *
   * @param c a class
   * @param signature the {@link RawSignature#toString()} of a public operation of {@code c}
   * @throws RandoopSpecificationError if Randoop cannot extract the operations of {@code c}
   */
  private static void assertNotOmitted(Class<?> c, String signature)
      throws RandoopSpecificationError {
    assertTrue(signature, signatures(c, defaultOmitPredicate).contains(signature));
  }

  /**
   * Returns the signatures of the public constructors and methods of {@code c} that {@code
   * omitPredicate} does not omit.
   *
   * @param c a class
   * @param omitPredicate the operations to omit
   * @return the {@link RawSignature#toString()} of each operation of {@code c} that is not omitted
   * @throws RandoopSpecificationError if Randoop cannot extract the operations of {@code c}
   */
  private static Set<String> signatures(Class<?> c, OmitMethodsPredicate omitPredicate)
      throws RandoopSpecificationError {
    Set<String> result = new TreeSet<>();
    for (TypedOperation op :
        OperationExtractor.operations(
            c, new DefaultReflectionPredicate(), omitPredicate, AccessibilityPredicate.IS_PUBLIC)) {
      if (op instanceof TypedClassOperation && (op.isConstructorCall() || op.isMethodCall())) {
        result.add(((TypedClassOperation) op).getRawSignature().toString());
      }
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
