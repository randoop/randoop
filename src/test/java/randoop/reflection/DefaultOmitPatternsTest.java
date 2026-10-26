package randoop.reflection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.BeforeClass;
import org.junit.Test;
import randoop.condition.RandoopSpecificationError;
import randoop.main.GenTests;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;

/** Tests the omit patterns that Randoop uses by default. */
public class DefaultOmitPatternsTest {

  /** The patterns in Randoop's default omit-methods files. */
  private static List<Pattern> defaultPatterns;

  /** The predicate that omits operations that match {@link #defaultPatterns}. */
  private static OmitMethodsPredicate defaultOmitPredicate;

  /** Matches a pattern that matches exactly one signature string; group 1 is that string. */
  private static final Pattern EXACT_PATTERN = Pattern.compile("^\\^\\\\Q(.*)\\\\E\\$$");

  @BeforeClass
  public static void setup() {
    defaultPatterns = GenTests.defaultOmitMethodsPatterns();
    defaultOmitPredicate = new OmitMethodsPredicate(defaultPatterns);
  }

  /**
   * Checks that each default pattern that matches exactly one signature string is in the format of
   * {@link RawSignature#toString()} and names an existing method or constructor. A pattern that
   * names a class that is not on the classpath is not checked for existence.
   */
  @Test
  public void testExactPatternsNameExistingOperations() {
    for (Pattern pattern : defaultPatterns) {
      Matcher m = EXACT_PATTERN.matcher(pattern.pattern());
      if (!m.matches() || m.group(1).contains("\\E")) {
        continue;
      }
      String signature = m.group(1);
      // SignatureParser accepts these, but a RawSignature string never contains them.
      assertFalse(pattern.pattern(), signature.contains(" ") || signature.contains("<init>"));
      try {
        SignatureParser.parse(
            signature, AccessibilityPredicate.IS_ANY, new DefaultReflectionPredicate());
      } catch (FailedPredicateException e) {
        // The operation exists, though Randoop would not use it anyway.
      } catch (SignatureParseException e) {
        Throwable cause = e.getCause();
        if (!(cause instanceof ClassNotFoundException || cause instanceof NoClassDefFoundError)) {
          fail("Default omit pattern " + pattern + " matches no operation: " + e.getMessage());
        }
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
        "randoop.reflection.DefaultOmitPatternsTest$GenericComparator.compare(java.lang.Object,java.lang.Object)");
  }

  @Test
  public void testJodaTimeReadClock() throws RandoopSpecificationError {
    assertOmitted(org.joda.time.DateTime.class, "org.joda.time.DateTime()");
    assertOmitted(
        org.joda.time.DateTime.class, "org.joda.time.DateTime(org.joda.time.DateTimeZone)");
    assertOmitted(org.joda.time.DateTime.class, "org.joda.time.DateTime.now()");
    assertOmitted(org.joda.time.DateTime.class, "org.joda.time.base.AbstractInstant.isAfterNow()");
    assertNotOmitted(org.joda.time.DateTime.class, "org.joda.time.DateTime(long)");
    assertNotOmitted(
        org.joda.time.DateTime.class, "org.joda.time.base.AbstractInstant.isAfter(long)");

    assertOmitted(org.joda.time.LocalDate.class, "org.joda.time.LocalDate()");
    assertOmitted(
        org.joda.time.LocalDate.class, "org.joda.time.LocalDate(org.joda.time.Chronology)");
    assertOmitted(
        org.joda.time.LocalDate.class, "org.joda.time.LocalDate.now(org.joda.time.DateTimeZone)");
    assertOmitted(
        org.joda.time.LocalDate.class, "org.joda.time.LocalDate.toDateTimeAtCurrentTime()");
    assertNotOmitted(org.joda.time.LocalDate.class, "org.joda.time.LocalDate(int,int,int)");
    assertNotOmitted(
        org.joda.time.LocalDate.class, "org.joda.time.LocalDate.toDateTimeAtStartOfDay()");

    assertOmitted(org.joda.time.Instant.class, "org.joda.time.Instant()");
    assertOmitted(org.joda.time.Instant.class, "org.joda.time.Instant.now()");
    assertNotOmitted(org.joda.time.Instant.class, "org.joda.time.Instant(long)");

    assertOmitted(org.joda.time.LocalTime.class, "org.joda.time.LocalTime()");
    assertOmitted(
        org.joda.time.LocalTime.class,
        "org.joda.time.LocalTime.toDateTimeToday(org.joda.time.DateTimeZone)");
    assertNotOmitted(org.joda.time.LocalTime.class, "org.joda.time.LocalTime(int,int)");

    assertOmitted(
        org.joda.time.Interval.class, "org.joda.time.base.AbstractInterval.containsNow()");
    assertNotOmitted(
        org.joda.time.Interval.class, "org.joda.time.base.AbstractInterval.contains(long)");

    assertNotOmitted(org.joda.time.Partial.class, "org.joda.time.Partial()");
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
}
