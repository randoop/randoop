package randoop.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Set;

import junit.extensions.TestSetup;
import junit.framework.TestCase;

import org.junit.Test;

import randoop.NonreceiverTerm;
import randoop.SeedSequences;
import randoop.Sequence;
import randoop.TestValue;

public class SeedSequencesTests extends TestCase {

  @Test
  public void testGetSeedsFromAnnotatedFields() {
 
    try {
      SeedSequences.getSeedsFromAnnotatedFields(MissingPublicMod.class);
      fail();
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("public")); // message should at least mention static modifier.
    }

    try {
      SeedSequences.getSeedsFromAnnotatedFields(MissingStaticMod.class);
      fail();
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("static")); // message should at least mention static modifier.
    }


    try {
      SeedSequences.getSeedsFromAnnotatedFields(ClassNotPublic.class);
      fail();
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("visible")); // message should at least mention potential visibility problem.
    }
    

    try {
      SeedSequences.getSeedsFromAnnotatedFields(BadType0.class);
      fail();
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("type")); // message should at least mention type problem.
    }

    try {
      SeedSequences.getSeedsFromAnnotatedFields(BadType1.class);
      fail();
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("type")); // message should at least mention type problem.
    }

    try {
      SeedSequences.getSeedsFromAnnotatedFields(BadType2.class);
      fail();
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("type")); // message should at least mention type problem.
    }

    Set<Sequence> s4 = SeedSequences.getSeedsFromAnnotatedFields(SeedSequencesTests.TestValueExamples.class);
    Set<Sequence> expected = SeedSequences.objectsToSeeds(Arrays.asList(new Object[] { 0, 1, 2, 3, (String)"hi", false, (byte)3,
        'c', 3L, (float)1.3, (double)1.4 }));
    expected.add(Sequence.create(NonreceiverTerm.nullOrZeroDecl(String.class)));
    assertEquals(expected, s4);
    
    
  }
  

  public static class TestValueExamples {
    @TestValue
    public static int x1 = 0;
    @TestValue
    public static boolean b = false;
    @TestValue
    public static byte by = 3;
    @TestValue
    public static char c = 'c';
    @TestValue
    public static long l = 3L;
    @TestValue
    public static float f = (float) 1.3;
    @TestValue
    public static double d = 1.4;
    @TestValue
    public static String s1 = null;
    @TestValue
    public static String s2 = "hi";
    @TestValue
    public static int[] a1 = new int[] { 1, 2, 3 };
    @TestValue
    public static int[] a2 = new int[] {};
  }
}

class MissingPublicMod {
  @TestValue static int x1 = 0;
}

class MissingStaticMod {
  @TestValue public int x1 = 0;
}

class ClassNotPublic {
  // not static
  @TestValue static public int x1 = 0;
}

class BadType0 {
  // not static
  @TestValue static public Integer x1 = 0;
}

class BadType1 {
  @TestValue
  public static Object o1 = (int) 1;
}

class BadType2 {
  @TestValue
  public static Object o1 = (int) 1;
}
