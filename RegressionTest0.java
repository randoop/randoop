import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegressionTest0 {

    public static boolean debug = false;

    private static java.lang.reflect.Method getClass;

    private static java.lang.reflect.Method parse_Options_StringArray_Properties_boolean;

    private static java.lang.reflect.Method parse_Options_StringArray_Properties;

    private static java.lang.reflect.Method parse_Options_StringArray;

    private static java.lang.reflect.Method addSuppressed_Throwable;

    private static java.lang.reflect.Method getOption;

    private static java.lang.reflect.Method getSuppressed;

    private static java.lang.reflect.Method flatten_Options_StringArray_boolean;

    static {
        try {
            getClass = java.lang.Object.class.getDeclaredMethod("getClass");
            getClass.setAccessible(true);
            parse_Options_StringArray_Properties_boolean = org.apache.commons.cli.Parser.class.getDeclaredMethod("parse", org.apache.commons.cli.Options.class, java.lang.String[].class, java.util.Properties.class, boolean.class);
            parse_Options_StringArray_Properties_boolean.setAccessible(true);
            parse_Options_StringArray_Properties = org.apache.commons.cli.Parser.class.getDeclaredMethod("parse", org.apache.commons.cli.Options.class, java.lang.String[].class, java.util.Properties.class);
            parse_Options_StringArray_Properties.setAccessible(true);
            parse_Options_StringArray = org.apache.commons.cli.Parser.class.getDeclaredMethod("parse", org.apache.commons.cli.Options.class, java.lang.String[].class);
            parse_Options_StringArray.setAccessible(true);
            addSuppressed_Throwable = java.lang.Throwable.class.getDeclaredMethod("addSuppressed", java.lang.Throwable.class);
            addSuppressed_Throwable.setAccessible(true);
            getOption = org.apache.commons.cli.AlreadySelectedException.class.getDeclaredMethod("getOption");
            getOption.setAccessible(true);
            getSuppressed = java.lang.Throwable.class.getDeclaredMethod("getSuppressed");
            getSuppressed.setAccessible(true);
            flatten_Options_StringArray_boolean = org.apache.commons.cli.BasicParser.class.getDeclaredMethod("flatten", org.apache.commons.cli.Options.class, java.lang.String[].class, boolean.class);
            flatten_Options_StringArray_boolean.setAccessible(true);
        } catch (Throwable t) {
            t.printStackTrace(System.out);
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
        java.lang.Class<?> wildcardClass1 = (java.lang.Class<?>) getClass.invoke(obj0);
        org.junit.Assert.assertNotNull(wildcardClass1);
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
            org.apache.commons.cli.CommandLine commandLine6 = null; // flaky "1) test03(RegressionTest0)": (org.apache.commons.cli.CommandLine) parse_Options_StringArray.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray5);
// flaky "1) test03(RegressionTest0)":             org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
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
            org.apache.commons.cli.CommandLine commandLine3 = null; // flaky "2) test04(RegressionTest0)": (org.apache.commons.cli.CommandLine) parse_Options_StringArray.invoke((org.apache.commons.cli.CommandLineParser) basicParser0, options1, strArray2);
// flaky "2) test04(RegressionTest0)":             org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
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
        org.apache.commons.cli.Option option2 = (org.apache.commons.cli.Option) getOption.invoke(alreadySelectedException1);
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
            org.apache.commons.cli.CommandLine commandLine4 = null; // flaky "3) test06(RegressionTest0)": (org.apache.commons.cli.CommandLine) parse_Options_StringArray_Properties.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray2, properties3);
// flaky "3) test06(RegressionTest0)":             org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
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
            org.apache.commons.cli.CommandLine commandLine8 = null; // flaky "4) test07(RegressionTest0)": (org.apache.commons.cli.CommandLine) parse_Options_StringArray_Properties_boolean.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray5, properties6, false);
// flaky "4) test07(RegressionTest0)":             org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
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
            org.apache.commons.cli.CommandLine commandLine10 = null; // flaky "5) test08(RegressionTest0)": (org.apache.commons.cli.CommandLine) parse_Options_StringArray_Properties.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray8, properties9);
// flaky "5) test08(RegressionTest0)":             org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
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
            org.apache.commons.cli.CommandLine commandLine7 = null; // flaky "6) test09(RegressionTest0)": (org.apache.commons.cli.CommandLine) parse_Options_StringArray.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray6);
// flaky "6) test09(RegressionTest0)":             org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
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
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException5 = new org.apache.commons.cli.AlreadySelectedException("");
        addSuppressed_Throwable.invoke((java.lang.Throwable) alreadySelectedException3, (java.lang.Throwable) alreadySelectedException5);
        java.lang.Throwable[] throwableArray7 = (java.lang.Throwable[]) getSuppressed.invoke((java.lang.Throwable) alreadySelectedException5);
        addSuppressed_Throwable.invoke((java.lang.Throwable) alreadySelectedException1, (java.lang.Throwable) alreadySelectedException5);
        org.junit.Assert.assertNotNull(throwableArray7);
        org.junit.Assert.assertArrayEquals(throwableArray7, new java.lang.Throwable[] {});
    }

    @Test
    public void test11() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test11");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray5 = new java.lang.String[] { "hi!", "hi!", "hi!" };
        java.util.Properties properties6 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine7 = null; // flaky "7) test11(RegressionTest0)": (org.apache.commons.cli.CommandLine) parse_Options_StringArray_Properties.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray5, properties6);
// flaky "7) test11(RegressionTest0)":             org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray5);
        org.junit.Assert.assertArrayEquals(strArray5, new java.lang.String[] { "hi!", "hi!", "hi!" });
    }

    @Test
    public void test12() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test12");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray7 = new java.lang.String[] { "hi!", "", "hi!", "", "hi!" };
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine8 = null; // flaky "8) test12(RegressionTest0)": (org.apache.commons.cli.CommandLine) parse_Options_StringArray.invoke((org.apache.commons.cli.CommandLineParser) basicParser0, options1, strArray7);
// flaky "8) test12(RegressionTest0)":             org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray7);
        org.junit.Assert.assertArrayEquals(strArray7, new java.lang.String[] { "hi!", "", "hi!", "", "hi!" });
    }

    @Test
    public void test13() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test13");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Options options9 = null;
        org.apache.commons.cli.BasicParser basicParser10 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options11 = null;
        java.lang.String[] strArray16 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray18 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser10, options11, strArray16, false);
        java.util.Properties properties19 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine21 = null; // flaky "9) test13(RegressionTest0)": (org.apache.commons.cli.CommandLine) parse_Options_StringArray_Properties_boolean.invoke((org.apache.commons.cli.Parser) basicParser0, options9, strArray16, properties19, true);
// flaky "9) test13(RegressionTest0)":             org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray8);
        org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray16);
        org.junit.Assert.assertArrayEquals(strArray16, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray18);
        org.junit.Assert.assertArrayEquals(strArray18, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test14() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test14");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray5 = new java.lang.String[] { "", "hi!", "hi!" };
        java.util.Properties properties6 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine7 = null; // flaky "10) test14(RegressionTest0)": (org.apache.commons.cli.CommandLine) parse_Options_StringArray_Properties.invoke((org.apache.commons.cli.Parser) basicParser0, options1, strArray5, properties6);
// flaky "10) test14(RegressionTest0)":             org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray5);
        org.junit.Assert.assertArrayEquals(strArray5, new java.lang.String[] { "", "hi!", "hi!" });
    }

    @Test
    public void test15() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test15");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
        addSuppressed_Throwable.invoke((java.lang.Throwable) alreadySelectedException1, (java.lang.Throwable) alreadySelectedException3);
        java.lang.Throwable[] throwableArray5 = (java.lang.Throwable[]) getSuppressed.invoke((java.lang.Throwable) alreadySelectedException3);
        java.lang.Throwable[] throwableArray6 = (java.lang.Throwable[]) getSuppressed.invoke((java.lang.Throwable) alreadySelectedException3);
        org.junit.Assert.assertNotNull(throwableArray5);
        org.junit.Assert.assertArrayEquals(throwableArray5, new java.lang.Throwable[] {});
        org.junit.Assert.assertNotNull(throwableArray6);
        org.junit.Assert.assertArrayEquals(throwableArray6, new java.lang.Throwable[] {});
    }

    @Test
    public void test16() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test16");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Options options9 = null;
        org.apache.commons.cli.BasicParser basicParser10 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options11 = null;
        java.lang.String[] strArray16 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray18 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser10, options11, strArray16, false);
        java.util.Properties properties19 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine21 = null; // flaky "11) test16(RegressionTest0)": (org.apache.commons.cli.CommandLine) parse_Options_StringArray_Properties_boolean.invoke((org.apache.commons.cli.Parser) basicParser0, options9, strArray18, properties19, false);
// flaky "11) test16(RegressionTest0)":             org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray8);
        org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray16);
        org.junit.Assert.assertArrayEquals(strArray16, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray18);
        org.junit.Assert.assertArrayEquals(strArray18, new java.lang.String[] { "", "", "", "" });
    }
}
