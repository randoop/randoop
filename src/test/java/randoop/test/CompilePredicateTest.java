package randoop.test;

import static org.apache.commons.codec.CharEncoding.UTF_8;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import randoop.output.JUnitCreator;

/** Test for compilation predicate. */
public class CompilePredicateTest {

  @Test
  public void uncompilablePredicateTest() throws ParseException, UnsupportedEncodingException {
    String failedCode =
        "import org.junit.FixMethodOrder;\n"
            + "import org.junit.Test;\n"
            + "import org.junit.runners.MethodSorters;\n"
            + "\n"
            + "@FixMethodOrder(MethodSorters.NAME_ASCENDING)\n"
            + "public class CompRegression0 {\n"
            + "\n"
            + "    public static boolean debug = false;\n"
            + "\n"
            + "    @Test\n"
            + "    public void test04() throws Throwable {\n"
            + "        if (debug)\n"
            + "            System.out.format(\"%n%s%n\", \"CompRegression0.test04\");\n"
            + "        java.lang.String[] str_array1 = new java.lang.String[] { \"hi!\" };\n"
            + "        java.util.ArrayList<java.lang.String> arraylist_str2 = new java.util.ArrayList<java.lang.String>();\n"
            + "        boolean b3 = java.util.Collections.addAll((java.util.Collection<java.lang.String>) arraylist_str2, str_array1);\n"
            + "        java.util.List<java.lang.Integer> list_i4 = compileerr.WildcardCollection.getAnIntegerList();\n"
            + "        java.util.List<java.lang.Integer> list_i5 = compileerr.WildcardCollection.getAnIntegerList();\n"
            + "        java.util.List<? extends java.lang.CharSequence> list_wildcard6 = compileerr.WildcardCollection.munge(list_i4, list_i5);\n"
            + "        java.util.List<? extends java.lang.Cloneable> list_wildcard7 = compileerr.WildcardCollection.munge((java.util.List<java.lang.String>) arraylist_str2, list_i5);\n"
            + "        java.util.List<java.lang.String> list_str8 = compileerr.WildcardCollection.getAStringList();\n"
            + "        java.util.List<java.lang.Integer> list_i9 = compileerr.WildcardCollection.getAnIntegerList();\n"
            + "        java.util.List<java.lang.Integer> list_i10 = compileerr.WildcardCollection.getAnIntegerList();\n"
            + "        java.util.List<? extends java.lang.CharSequence> list_wildcard11 = compileerr.WildcardCollection.munge(list_i9, list_i10);\n"
            + "        java.util.List<java.lang.Integer> list_i12 = compileerr.WildcardCollection.getAnIntegerList();\n"
            + "        java.util.List<java.lang.Integer> list_i13 = compileerr.WildcardCollection.getAnIntegerList();\n"
            + "        java.util.List<? extends java.lang.CharSequence> list_wildcard14 = compileerr.WildcardCollection.munge(list_i12, list_i13);\n"
            + "        java.util.List<? extends java.util.ArrayList<java.lang.Integer>> list_wildcard15 = compileerr.WildcardCollection.munge(list_i9, list_i13);\n"
            + "        java.util.List<? extends java.lang.CharSequence> list_wildcard16 = compileerr.WildcardCollection.munge(list_str8, list_i9);\n"
            + "        java.util.List<java.lang.String> list_str17 = compileerr.WildcardCollection.getAStringList();\n"
            + "        java.util.List<java.lang.String> list_str18 = compileerr.WildcardCollection.getAStringList();\n"
            + "        java.util.List<? extends java.lang.Comparable<java.lang.String>> list_wildcard19 = compileerr.WildcardCollection.munge(list_str17, list_str18);\n"
            + "        java.util.List<java.lang.String> list_str20 = compileerr.WildcardCollection.getAStringList();\n"
            + "        java.util.List<java.lang.String> list_str21 = compileerr.WildcardCollection.getAStringList();\n"
            + "        java.util.List<? extends java.lang.Comparable<java.lang.String>> list_wildcard22 = compileerr.WildcardCollection.munge(list_str20, list_str21);\n"
            + "        java.util.List<? extends java.lang.Iterable<java.lang.Integer>> list_wildcard23 = compileerr.WildcardCollection.munge(list_str17, list_str21);\n"
            + "        java.util.List<java.lang.String> list_str24 = compileerr.WildcardCollection.getAStringList();\n"
            + "        java.util.List<java.lang.String> list_str25 = compileerr.WildcardCollection.getAStringList();\n"
            + "        java.util.List<? extends java.lang.Comparable<java.lang.String>> list_wildcard26 = compileerr.WildcardCollection.munge(list_str24, list_str25);\n"
            + "        java.util.List<java.lang.String> list_str27 = compileerr.WildcardCollection.getAStringList();\n"
            + "        java.util.List<java.lang.String> list_str28 = compileerr.WildcardCollection.getAStringList();\n"
            + "        java.util.List<? extends java.lang.Comparable<java.lang.String>> list_wildcard29 = compileerr.WildcardCollection.munge(list_str27, list_str28);\n"
            + "        java.util.List<? extends java.lang.Iterable<java.lang.Integer>> list_wildcard30 = compileerr.WildcardCollection.munge(list_str24, list_str28);\n"
            + "        java.util.List<? extends java.util.List<? extends java.lang.CharSequence>> list_wildcard31 = compileerr.WildcardCollection.munge(list_str17, list_str24);\n"
            + "        java.util.List<? extends java.lang.CharSequence> list_wildcard32 = compileerr.WildcardCollection.munge(list_str8, list_str24);\n"
            + "        java.util.List<? extends java.util.Collection<? extends java.util.Collection<? extends java.lang.Comparable<java.lang.String>>>> list_wildcard33 = compileerr.WildcardCollection.munge(list_i5, list_str24);\n"
            + "        org.junit.Assert.assertNotNull(str_array1);\n"
            + "        org.junit.Assert.assertTrue(b3 == true);\n"
            + "        org.junit.Assert.assertNotNull(list_i4);\n"
            + "        org.junit.Assert.assertNotNull(list_i5);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard6);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard7);\n"
            + "        org.junit.Assert.assertNotNull(list_str8);\n"
            + "        org.junit.Assert.assertNotNull(list_i9);\n"
            + "        org.junit.Assert.assertNotNull(list_i10);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard11);\n"
            + "        org.junit.Assert.assertNotNull(list_i12);\n"
            + "        org.junit.Assert.assertNotNull(list_i13);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard14);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard15);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard16);\n"
            + "        org.junit.Assert.assertNotNull(list_str17);\n"
            + "        org.junit.Assert.assertNotNull(list_str18);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard19);\n"
            + "        org.junit.Assert.assertNotNull(list_str20);\n"
            + "        org.junit.Assert.assertNotNull(list_str21);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard22);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard23);\n"
            + "        org.junit.Assert.assertNotNull(list_str24);\n"
            + "        org.junit.Assert.assertNotNull(list_str25);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard26);\n"
            + "        org.junit.Assert.assertNotNull(list_str27);\n"
            + "        org.junit.Assert.assertNotNull(list_str28);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard29);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard30);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard31);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard32);\n"
            + "        org.junit.Assert.assertNotNull(list_wildcard33);\n"
            + "    }\n"
            + "}";
    CompilationUnit source;
    source = JavaParser.parse(new ByteArrayInputStream(failedCode.getBytes(UTF_8)));
    assertNotNull(source);
    JUnitCreator jUnitCreator = JUnitCreator.getTestCreator(null, null, null, null, null);
    CompilableTestPredicate pred = new CompilableTestPredicate(jUnitCreator, null);

    assertFalse("predicate should fail on code", pred.testSource("CompRegression0", source, ""));
  }

  @Test
  public void compilablePredicateTest() throws ParseException, UnsupportedEncodingException {
    String compilableCode =
        "package foo.bar;\n"
            + "\n"
            + "import org.junit.FixMethodOrder;\n"
            + "import org.junit.Test;\n"
            + "import org.junit.runners.MethodSorters;\n"
            + "\n"
            + "@FixMethodOrder(MethodSorters.NAME_ASCENDING)\n"
            + "public class TestClass0 {\n"
            + "\n"
            + "    public static boolean debug = false;\n"
            + "\n"
            + "    @Test\n"
            + "    public void test001() throws Throwable {\n"
            + "        if (debug)\n"
            + "            System.out.format(\"%n%s%n\", \"TestClass0.test001\");\n"
            + "        java7.util7.Collection collection0 = null;\n"
            + "        try {\n"
            + "            java7.util7.TreeSet treeSet1 = new java7.util7.TreeSet(collection0);\n"
            + "            org.junit.Assert.fail(\"Expected exception of type java.lang.NullPointerException\");\n"
            + "        } catch (java.lang.NullPointerException e) {\n"
            + "        }\n"
            + "    }\n"
            + "}";
    CompilationUnit source;
    source = JavaParser.parse(new ByteArrayInputStream(compilableCode.getBytes(UTF_8)));
    assertNotNull(source);
    JUnitCreator jUnitCreator = JUnitCreator.getTestCreator("foo.bar", null, null, null, null);
    CompilableTestPredicate pred = new CompilableTestPredicate(jUnitCreator, null);

    assertTrue("predicate should pass on code", pred.testSource("TestClass0", source, "foo.bar"));
  }
}
