import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegressionTest0 {

    public static boolean debug = false;

    private static java.lang.reflect.Method flatten;

    private static java.lang.reflect.Method getClass;

    private static java.lang.reflect.Method getOptionGroup;

    private static java.lang.reflect.Method addSuppressed;

    private static java.lang.reflect.Method processArgs;

    private static java.lang.reflect.Method getOption;

    private static java.lang.reflect.Method getSuppressed;

    private static java.lang.reflect.Method toString;

    private static java.lang.reflect.Method parse;

    static {
        try {
            java.lang.reflect.Method flatten = org.apache.commons.cli.BasicParser.class.getDeclaredMethod("flatten");
            flatten.setAccessible(true);
            java.lang.reflect.Method getClass = java.lang.Object.class.getDeclaredMethod("getClass");
            getClass.setAccessible(true);
            java.lang.reflect.Method getOptionGroup = org.apache.commons.cli.AlreadySelectedException.class.getDeclaredMethod("getOptionGroup");
            getOptionGroup.setAccessible(true);
            java.lang.reflect.Method addSuppressed = java.lang.Throwable.class.getDeclaredMethod("addSuppressed");
            addSuppressed.setAccessible(true);
            java.lang.reflect.Method processArgs = org.apache.commons.cli.Parser.class.getDeclaredMethod("processArgs");
            processArgs.setAccessible(true);
            java.lang.reflect.Method getOption = org.apache.commons.cli.AlreadySelectedException.class.getDeclaredMethod("getOption");
            getOption.setAccessible(true);
            java.lang.reflect.Method getSuppressed = java.lang.Throwable.class.getDeclaredMethod("getSuppressed");
            getSuppressed.setAccessible(true);
            java.lang.reflect.Method toString = java.lang.Throwable.class.getDeclaredMethod("toString");
            toString.setAccessible(true);
            java.lang.reflect.Method parse = org.apache.commons.cli.Parser.class.getDeclaredMethod("parse");
            parse.setAccessible(true);
        } catch (ReflectiveOperationException e) {
        }
    }

    @Test
    public void test01() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test01");
        org.apache.commons.cli.OptionGroup optionGroup0 = null;
        org.apache.commons.cli.Option option1 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.AlreadySelectedException alreadySelectedException2 = new org.apache.commons.cli.AlreadySelectedException(optionGroup0, option1);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Option.getKey()\" because \"option\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test02() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test02");
        java.lang.Object obj0 = new java.lang.Object();
        java.lang.Class<?> wildcardClass1 = null; // flaky (1) test02(RegressionTest0)): (java.lang.Class<?>) getClass.invoke(obj0);
// flaky (1) test02(RegressionTest0)):         org.junit.Assert.assertNotNull(wildcardClass1);
    }

    @Test
    public void test03() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test03");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray5 = new java.lang.String[] { "", "", "hi!" };
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine6 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray5);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray5);
        org.junit.Assert.assertArrayEquals(strArray5, new java.lang.String[] { "", "", "hi!" });
    }

    @Test
    public void test04() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test04");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray2 = new java.lang.String[] {};
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine3 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.CommandLineParser) basicParser0, options1, strArray2);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray2);
        org.junit.Assert.assertArrayEquals(strArray2, new java.lang.String[] {});
    }

    @Test
    public void test05() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test05");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.Option option2 = null; // flaky (2) test05(RegressionTest0)): (org.apache.commons.cli.Option) getOption.invoke(alreadySelectedException1);
        org.junit.Assert.assertNull(option2);
    }

    @Test
    public void test06() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test06");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray2 = new java.lang.String[] {};
        java.util.Properties properties3 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine4 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray2, properties3);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray2);
        org.junit.Assert.assertArrayEquals(strArray2, new java.lang.String[] {});
    }

    @Test
    public void test07() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test07");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray5 = new java.lang.String[] { "hi!", "", "hi!" };
        java.util.Properties properties6 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine8 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray5, properties6, false);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray5);
        org.junit.Assert.assertArrayEquals(strArray5, new java.lang.String[] { "hi!", "", "hi!" });
    }

    @Test
    public void test08() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test08");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray8 = new java.lang.String[] { "", "", "hi!", "", "hi!", "hi!" };
        java.util.Properties properties9 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine10 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray8, properties9);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray8);
        org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "hi!", "", "hi!", "hi!" });
    }

    @Test
    public void test09() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test09");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "hi!", "hi!", "" };
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine7 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray6);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "hi!", "hi!", "" });
    }

    @Test
    public void test10() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test10");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray5 = new java.lang.String[] { "hi!", "hi!", "hi!" };
        java.util.Properties properties6 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine7 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray5, properties6);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray5);
        org.junit.Assert.assertArrayEquals(strArray5, new java.lang.String[] { "hi!", "hi!", "hi!" });
    }

    @Test
    public void test11() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test11");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray7 = new java.lang.String[] { "hi!", "", "hi!", "", "hi!" };
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine8 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.CommandLineParser) basicParser0, options1, strArray7);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray7);
        org.junit.Assert.assertArrayEquals(strArray7, new java.lang.String[] { "hi!", "", "hi!", "", "hi!" });
    }

    @Test
    public void test12() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test12");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = null; // flaky (3) test12(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Options options9 = null;
        org.apache.commons.cli.BasicParser basicParser10 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options11 = null;
        java.lang.String[] strArray16 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray18 = null; // flaky (2) test12(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser10, options11, strArray16, false);
        java.util.Properties properties19 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine21 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options9, strArray16, properties19, true);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "", "", "" });
// flaky (1) test12(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray8);
// flaky (1) test12(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray16);
        org.junit.Assert.assertArrayEquals(strArray16, new java.lang.String[] { "", "", "", "" });
// flaky (1) test12(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray18);
// flaky (1) test12(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray18, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test13() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test13");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray5 = new java.lang.String[] { "", "hi!", "hi!" };
        java.util.Properties properties6 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine7 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray5, properties6);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray5);
        org.junit.Assert.assertArrayEquals(strArray5, new java.lang.String[] { "", "hi!", "hi!" });
    }

    @Test
    public void test14() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test14");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = null; // flaky (4) test14(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Options options9 = null;
        org.apache.commons.cli.BasicParser basicParser10 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options11 = null;
        java.lang.String[] strArray16 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray18 = null; // flaky (3) test14(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser10, options11, strArray16, false);
        java.util.Properties properties19 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine21 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options9, strArray18, properties19, false);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "", "", "" });
// flaky (2) test14(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray8);
// flaky (2) test14(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray16);
        org.junit.Assert.assertArrayEquals(strArray16, new java.lang.String[] { "", "", "", "" });
// flaky (2) test14(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray18);
// flaky (2) test14(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray18, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test15() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test15");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = null; // flaky (5) test15(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Option option9 = null;
        java.util.ListIterator listIterator10 = null;
        // The following exception was thrown during execution in test generation
        try {
            processArgs.invoke((org.apache.commons.cli.Parser) basicParser0, option9, listIterator10);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.util.ListIterator.hasNext()\" because \"iter\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "", "", "" });
// flaky (4) test15(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray8);
// flaky (3) test15(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test16() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test16");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = null; // flaky (6) test16(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Options options9 = null;
        org.apache.commons.cli.BasicParser basicParser10 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options11 = null;
        java.lang.String[] strArray16 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray18 = null; // flaky (5) test16(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser10, options11, strArray16, false);
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine20 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options9, strArray18, false);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "", "", "" });
// flaky (4) test16(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray8);
// flaky (3) test16(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray16);
        org.junit.Assert.assertArrayEquals(strArray16, new java.lang.String[] { "", "", "", "" });
// flaky (3) test16(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray18);
// flaky (3) test16(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray18, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test17() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test17");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = null; // flaky (7) test17(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Options options9 = null;
        org.apache.commons.cli.BasicParser basicParser10 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options11 = null;
        java.lang.String[] strArray16 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray18 = null; // flaky (6) test17(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser10, options11, strArray16, false);
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine20 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.CommandLineParser) basicParser0, options9, strArray16, false);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "", "", "" });
// flaky (5) test17(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray8);
// flaky (4) test17(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray16);
        org.junit.Assert.assertArrayEquals(strArray16, new java.lang.String[] { "", "", "", "" });
// flaky (4) test17(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray18);
// flaky (4) test17(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray18, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test18() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test18");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = null; // flaky (8) test18(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Options options9 = null;
        org.apache.commons.cli.BasicParser basicParser10 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options11 = null;
        java.lang.String[] strArray16 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray18 = null; // flaky (7) test18(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser10, options11, strArray16, false);
        java.util.Properties properties19 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine21 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options9, strArray18, properties19, true);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "", "", "" });
// flaky (6) test18(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray8);
// flaky (5) test18(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray16);
        org.junit.Assert.assertArrayEquals(strArray16, new java.lang.String[] { "", "", "", "" });
// flaky (5) test18(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray18);
// flaky (5) test18(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray18, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test19() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test19");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException5 = new org.apache.commons.cli.AlreadySelectedException("");
// flaky (9) test19(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException3, (java.lang.Throwable) alreadySelectedException5);
        java.lang.Throwable[] throwableArray7 = null; // flaky (8) test19(RegressionTest0)): (java.lang.Throwable[]) getSuppressed.invoke((java.lang.Throwable) alreadySelectedException5);
// flaky (7) test19(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException1, (java.lang.Throwable) alreadySelectedException5);
        org.apache.commons.cli.Option option9 = null; // flaky (6) test19(RegressionTest0)): (org.apache.commons.cli.Option) getOption.invoke(alreadySelectedException5);
// flaky (6) test19(RegressionTest0)):         org.junit.Assert.assertNotNull(throwableArray7);
// flaky (6) test19(RegressionTest0)):         org.junit.Assert.assertArrayEquals(throwableArray7, new java.lang.Throwable[] {});
        org.junit.Assert.assertNull(option9);
    }

    @Test
    public void test20() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test20");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
// flaky (10) test20(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException1, (java.lang.Throwable) alreadySelectedException3);
        java.lang.Throwable[] throwableArray5 = null; // flaky (9) test20(RegressionTest0)): (java.lang.Throwable[]) getSuppressed.invoke((java.lang.Throwable) alreadySelectedException3);
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException7 = new org.apache.commons.cli.AlreadySelectedException("");
// flaky (8) test20(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException3, (java.lang.Throwable) alreadySelectedException7);
        java.lang.String str9 = null; // flaky (7) test20(RegressionTest0)): (java.lang.String) toString.invoke((java.lang.Throwable) alreadySelectedException3);
// flaky (7) test20(RegressionTest0)):         org.junit.Assert.assertNotNull(throwableArray5);
// flaky (7) test20(RegressionTest0)):         org.junit.Assert.assertArrayEquals(throwableArray5, new java.lang.Throwable[] {});
// flaky (1) test20(RegressionTest0)):         org.junit.Assert.assertEquals("'" + str9 + "' != '" + "org.apache.commons.cli.AlreadySelectedException: " + "'", str9, "org.apache.commons.cli.AlreadySelectedException: ");
    }

    @Test
    public void test21() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test21");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = null; // flaky (11) test21(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Options options9 = null;
        org.apache.commons.cli.BasicParser basicParser10 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options11 = null;
        java.lang.String[] strArray16 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray18 = null; // flaky (10) test21(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser10, options11, strArray16, false);
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine19 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options9, strArray18);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "", "", "" });
// flaky (9) test21(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray8);
// flaky (8) test21(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray16);
        org.junit.Assert.assertArrayEquals(strArray16, new java.lang.String[] { "", "", "", "" });
// flaky (8) test21(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray18);
// flaky (8) test21(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray18, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test22() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test22");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Option option1 = null;
        java.util.ListIterator listIterator2 = null;
        // The following exception was thrown during execution in test generation
        try {
            processArgs.invoke((org.apache.commons.cli.Parser) basicParser0, option1, listIterator2);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.util.ListIterator.hasNext()\" because \"iter\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test23() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test23");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        org.apache.commons.cli.BasicParser basicParser2 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options3 = null;
        java.lang.String[] strArray8 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray10 = null; // flaky (12) test23(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser2, options3, strArray8, false);
        java.util.Properties properties11 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine13 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray8, properties11, true);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray8);
        org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
// flaky (11) test23(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray10);
// flaky (10) test23(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray10, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test24() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test24");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = null; // flaky (13) test24(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Options options9 = null;
        org.apache.commons.cli.BasicParser basicParser10 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options11 = null;
        java.lang.String[] strArray16 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray18 = null; // flaky (12) test24(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser10, options11, strArray16, false);
        java.util.Properties properties19 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine20 = (org.apache.commons.cli.CommandLine) parse.invoke((org.apache.commons.cli.Parser) basicParser0, options9, strArray16, properties19);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "", "", "" });
// flaky (11) test24(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray8);
// flaky (9) test24(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray16);
        org.junit.Assert.assertArrayEquals(strArray16, new java.lang.String[] { "", "", "", "" });
// flaky (9) test24(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray18);
// flaky (9) test24(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray18, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test25() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test25");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
// flaky (14) test25(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException1, (java.lang.Throwable) alreadySelectedException3);
        java.lang.Throwable[] throwableArray5 = null; // flaky (13) test25(RegressionTest0)): (java.lang.Throwable[]) getSuppressed.invoke((java.lang.Throwable) alreadySelectedException3);
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException7 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException9 = new org.apache.commons.cli.AlreadySelectedException("");
// flaky (12) test25(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException7, (java.lang.Throwable) alreadySelectedException9);
        java.lang.Throwable[] throwableArray11 = null; // flaky (10) test25(RegressionTest0)): (java.lang.Throwable[]) getSuppressed.invoke((java.lang.Throwable) alreadySelectedException9);
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException13 = new org.apache.commons.cli.AlreadySelectedException("");
// flaky (10) test25(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException9, (java.lang.Throwable) alreadySelectedException13);
// flaky (10) test25(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException3, (java.lang.Throwable) alreadySelectedException13);
// flaky (2) test25(RegressionTest0)):         org.junit.Assert.assertNotNull(throwableArray5);
// flaky (1) test25(RegressionTest0)):         org.junit.Assert.assertArrayEquals(throwableArray5, new java.lang.Throwable[] {});
// flaky (1) test25(RegressionTest0)):         org.junit.Assert.assertNotNull(throwableArray11);
// flaky (1) test25(RegressionTest0)):         org.junit.Assert.assertArrayEquals(throwableArray11, new java.lang.Throwable[] {});
    }

    @Test
    public void test26() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test26");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
// flaky (15) test26(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException1, (java.lang.Throwable) alreadySelectedException3);
        java.lang.Throwable[] throwableArray5 = null; // flaky (14) test26(RegressionTest0)): (java.lang.Throwable[]) getSuppressed.invoke((java.lang.Throwable) alreadySelectedException3);
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException7 = new org.apache.commons.cli.AlreadySelectedException("");
// flaky (13) test26(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException3, (java.lang.Throwable) alreadySelectedException7);
        org.apache.commons.cli.Option option9 = null; // flaky (11) test26(RegressionTest0)): (org.apache.commons.cli.Option) getOption.invoke(alreadySelectedException7);
// flaky (11) test26(RegressionTest0)):         org.junit.Assert.assertNotNull(throwableArray5);
// flaky (11) test26(RegressionTest0)):         org.junit.Assert.assertArrayEquals(throwableArray5, new java.lang.Throwable[] {});
        org.junit.Assert.assertNull(option9);
    }

    @Test
    public void test27() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test27");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
// flaky (16) test27(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException1, (java.lang.Throwable) alreadySelectedException3);
        org.apache.commons.cli.OptionGroup optionGroup5 = null; // flaky (15) test27(RegressionTest0)): (org.apache.commons.cli.OptionGroup) getOptionGroup.invoke(alreadySelectedException3);
        // The following exception was thrown during execution in test generation
        try {
            java.lang.Class<?> wildcardClass6 = (java.lang.Class<?>) getClass.invoke((java.lang.Object) optionGroup5);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNull(optionGroup5);
    }

    @Test
    public void test28() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test28");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = null; // flaky (17) test28(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Options options9 = null;
        org.apache.commons.cli.BasicParser basicParser10 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options11 = null;
        java.lang.String[] strArray16 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray18 = null; // flaky (16) test28(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser10, options11, strArray16, false);
        java.lang.String[] strArray20 = null; // flaky (14) test28(RegressionTest0)): (java.lang.String[]) flatten.invoke(basicParser0, options9, strArray16, true);
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "", "", "" });
// flaky (12) test28(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray8);
// flaky (12) test28(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray16);
        org.junit.Assert.assertArrayEquals(strArray16, new java.lang.String[] { "", "", "", "" });
// flaky (12) test28(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray18);
// flaky (3) test28(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray18, new java.lang.String[] { "", "", "", "" });
// flaky (2) test28(RegressionTest0)):         org.junit.Assert.assertNotNull(strArray20);
// flaky (2) test28(RegressionTest0)):         org.junit.Assert.assertArrayEquals(strArray20, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test29() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test29");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
// flaky (18) test29(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException1, (java.lang.Throwable) alreadySelectedException3);
        java.lang.Throwable[] throwableArray5 = null; // flaky (17) test29(RegressionTest0)): (java.lang.Throwable[]) getSuppressed.invoke((java.lang.Throwable) alreadySelectedException3);
        java.lang.Throwable[] throwableArray6 = null; // flaky (15) test29(RegressionTest0)): (java.lang.Throwable[]) getSuppressed.invoke((java.lang.Throwable) alreadySelectedException3);
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException8 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException10 = new org.apache.commons.cli.AlreadySelectedException("");
// flaky (13) test29(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException8, (java.lang.Throwable) alreadySelectedException10);
        org.apache.commons.cli.OptionGroup optionGroup12 = null; // flaky (13) test29(RegressionTest0)): (org.apache.commons.cli.OptionGroup) getOptionGroup.invoke(alreadySelectedException10);
// flaky (13) test29(RegressionTest0)):         addSuppressed.invoke((java.lang.Throwable) alreadySelectedException3, (java.lang.Throwable) alreadySelectedException10);
// flaky (4) test29(RegressionTest0)):         org.junit.Assert.assertNotNull(throwableArray5);
// flaky (3) test29(RegressionTest0)):         org.junit.Assert.assertArrayEquals(throwableArray5, new java.lang.Throwable[] {});
// flaky (3) test29(RegressionTest0)):         org.junit.Assert.assertNotNull(throwableArray6);
// flaky (2) test29(RegressionTest0)):         org.junit.Assert.assertArrayEquals(throwableArray6, new java.lang.Throwable[] {});
        org.junit.Assert.assertNull(optionGroup12);
    }
}
