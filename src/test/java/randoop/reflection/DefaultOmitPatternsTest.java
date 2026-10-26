package randoop.reflection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.junit.BeforeClass;
import org.junit.Test;
import org.plumelib.util.EntryReader;
import org.plumelib.util.EntryReader.CommentFormat;
import org.plumelib.util.EntryReader.EntryFormat;
import randoop.condition.RandoopSpecificationError;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;

/** Tests the omit patterns that Randoop uses by default. */
public class DefaultOmitPatternsTest {

  /** The patterns in Randoop's default omit-methods files. */
  private static List<Pattern> defaultPatterns;

  /** The predicate that omits operations that match {@link #defaultPatterns}. */
  private static OmitMethodsPredicate defaultOmitPredicate;

  @BeforeClass
  public static void setup() throws IOException {
    defaultPatterns = new ArrayList<>();
    for (String resource : new String[] {"/omitmethods-defaults.txt", "/JDK-nondet-methods.txt"}) {
      try (InputStream in = DefaultOmitPatternsTest.class.getResourceAsStream(resource)) {
        assertNotNull(resource, in);
        try (EntryReader er =
            new EntryReader(
                in, "UTF-8", resource, EntryFormat.DEFAULT, CommentFormat.SHELL, null)) {
          for (String line : er) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
              defaultPatterns.add(Pattern.compile(trimmed));
            }
          }
        }
      }
    }
    defaultOmitPredicate = new OmitMethodsPredicate(defaultPatterns);
  }

  @Test
  public void testJavaTimeNow() throws RandoopSpecificationError {
    Set<String> signatures = signatures(java.time.LocalDate.class);
    assertFalse(signatures.contains("java.time.LocalDate.now()"));
    assertFalse(signatures.contains("java.time.LocalDate.now(java.time.ZoneId)"));
    assertTrue(signatures.contains("java.time.LocalDate.now(java.time.Clock)"));
    assertTrue(signatures.contains("java.time.LocalDate.of(int,int,int)"));

    signatures = signatures(java.time.Instant.class);
    assertFalse(signatures.contains("java.time.Instant.now()"));
    assertTrue(signatures.contains("java.time.Instant.now(java.time.Clock)"));
  }

  @Test
  public void testJavaTimeClock() throws RandoopSpecificationError {
    Set<String> signatures = signatures(java.time.Clock.class);
    assertFalse(signatures.contains("java.time.Clock.systemUTC()"));
    assertFalse(signatures.contains("java.time.Clock.systemDefaultZone()"));
    assertFalse(signatures.contains("java.time.Clock.system(java.time.ZoneId)"));
    assertFalse(signatures.contains("java.time.Clock.tickSeconds(java.time.ZoneId)"));
    assertTrue(signatures.contains("java.time.Clock.fixed(java.time.Instant,java.time.ZoneId)"));
  }

  @Test
  public void testJavaTimeChronologyDateNow() throws RandoopSpecificationError {
    // IsoChronology overrides Chronology.dateNow(), which the default patterns omit.
    Set<String> signatures = signatures(java.time.chrono.IsoChronology.class);
    assertFalse(signatures.contains("java.time.chrono.IsoChronology.dateNow()"));
    assertFalse(signatures.contains("java.time.chrono.IsoChronology.dateNow(java.time.ZoneId)"));
    assertTrue(signatures.contains("java.time.chrono.IsoChronology.dateNow(java.time.Clock)"));
  }

  @Test
  public void testJavaUtilDate() throws RandoopSpecificationError {
    Set<String> signatures = signatures(java.util.Date.class);
    assertFalse(signatures.contains("java.util.Date()"));
    assertTrue(signatures.contains("java.util.Date(long)"));
  }

  /** Joda-Time is not on the test classpath, so these tests match signature strings directly. */
  @Test
  public void testJodaTime() {
    assertOmitted("org.joda.time.DateTime()");
    assertOmitted("org.joda.time.DateTime(org.joda.time.DateTimeZone)");
    assertOmitted("org.joda.time.LocalDate(org.joda.time.Chronology)");
    assertOmitted("org.joda.time.YearMonthDay()");
    assertOmitted("org.joda.time.Instant()");
    assertOmitted("org.joda.time.DateTime.now()");
    assertOmitted("org.joda.time.LocalDate.now(org.joda.time.DateTimeZone)");
    assertOmitted("org.joda.time.MonthDay.now(org.joda.time.Chronology)");
    assertOmitted("org.joda.time.LocalDate.toDateTimeAtCurrentTime()");
    assertOmitted("org.joda.time.TimeOfDay.toDateTimeToday(org.joda.time.DateTimeZone)");
    assertOmitted("org.joda.time.base.AbstractInstant.isAfterNow()");
    assertOmitted("org.joda.time.base.AbstractInterval.containsNow()");
    assertOmitted("org.joda.time.DateTimeUtils.currentTimeMillis()");

    assertNotOmitted("org.joda.time.DateTime(long)");
    assertNotOmitted("org.joda.time.LocalDate(int,int,int)");
    assertNotOmitted("org.joda.time.Instant(long)");
    assertNotOmitted("org.joda.time.Partial()");
    assertNotOmitted("org.joda.time.LocalDate.toDateTimeAtStartOfDay()");
    assertNotOmitted("org.joda.time.base.AbstractInstant.isAfter(long)");
  }

  /**
   * Asserts that some default omit pattern matches the given signature string.
   *
   * @param signature a signature string, in the format of {@link RawSignature#toString()}
   */
  private static void assertOmitted(String signature) {
    assertTrue(signature, isOmitted(signature));
  }

  /**
   * Asserts that no default omit pattern matches the given signature string.
   *
   * @param signature a signature string, in the format of {@link RawSignature#toString()}
   */
  private static void assertNotOmitted(String signature) {
    assertFalse(signature, isOmitted(signature));
  }

  /**
   * Returns true if some default omit pattern matches the given signature string.
   *
   * @param signature a signature string, in the format of {@link RawSignature#toString()}
   * @return true if some default omit pattern matches {@code signature}
   */
  private static boolean isOmitted(String signature) {
    for (Pattern pattern : defaultPatterns) {
      if (pattern.matcher(signature).find()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the signatures of the public constructors and methods of {@code c} that the default
   * omit patterns do not omit.
   *
   * @param c a class
   * @return the {@link RawSignature#toString()} of each operation of {@code c} that is not omitted
   * @throws RandoopSpecificationError if Randoop cannot extract the operations of {@code c}
   */
  private static Set<String> signatures(Class<?> c) throws RandoopSpecificationError {
    Set<String> result = new TreeSet<>();
    for (TypedOperation op :
        OperationExtractor.operations(
            c,
            new DefaultReflectionPredicate(),
            defaultOmitPredicate,
            AccessibilityPredicate.IS_PUBLIC)) {
      if (op instanceof TypedClassOperation && (op.isConstructorCall() || op.isMethodCall())) {
        result.add(((TypedClassOperation) op).getRawSignature().toString());
      }
    }
    return result;
  }
}
