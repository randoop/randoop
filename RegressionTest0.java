import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegressionTest0 {

    public static boolean debug = false;

    private static java.lang.reflect.Method flatten_Options_StringArray_boolean;

    static {
        try {
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
        java.lang.Class<?> wildcardClass1 = obj0.getClass();
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
            org.apache.commons.cli.CommandLine commandLine6 = basicParser0.parse(options1, strArray5);
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
            org.apache.commons.cli.CommandLine commandLine3 = basicParser0.parse(options1, strArray2);
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
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray2 = new java.lang.String[] {};
        java.util.Properties properties3 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine4 = basicParser0.parse(options1, strArray2, properties3);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray2);
        org.junit.Assert.assertArrayEquals(strArray2, new java.lang.String[] {});
    }

    @Test
    public void test06() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test06");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray5 = new java.lang.String[] { "hi!", "", "hi!" };
        java.util.Properties properties6 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine8 = basicParser0.parse(options1, strArray5, properties6, false);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray5);
        org.junit.Assert.assertArrayEquals(strArray5, new java.lang.String[] { "hi!", "", "hi!" });
    }

    @Test
    public void test07() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test07");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray8 = new java.lang.String[] { "", "", "hi!", "", "hi!", "hi!" };
        java.util.Properties properties9 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine10 = basicParser0.parse(options1, strArray8, properties9);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray8);
        org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "hi!", "", "hi!", "hi!" });
    }

    @Test
    public void test08() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test08");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "hi!", "hi!", "" };
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine7 = basicParser0.parse(options1, strArray6);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "hi!", "hi!", "" });
    }

    @Test
    public void test09() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test09");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray5 = new java.lang.String[] { "hi!", "hi!", "hi!" };
        java.util.Properties properties6 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine7 = basicParser0.parse(options1, strArray5, properties6);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray5);
        org.junit.Assert.assertArrayEquals(strArray5, new java.lang.String[] { "hi!", "hi!", "hi!" });
    }

    @Test
    public void test10() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test10");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray7 = new java.lang.String[] { "hi!", "", "hi!", "", "hi!" };
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine8 = basicParser0.parse(options1, strArray7);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray7);
        org.junit.Assert.assertArrayEquals(strArray7, new java.lang.String[] { "hi!", "", "hi!", "", "hi!" });
    }

    @Test
    public void test11() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test11");
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
            org.apache.commons.cli.CommandLine commandLine21 = basicParser0.parse(options9, strArray16, properties19, true);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
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
    public void test12() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test12");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray5 = new java.lang.String[] { "", "hi!", "hi!" };
        java.util.Properties properties6 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine7 = basicParser0.parse(options1, strArray5, properties6);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray5);
        org.junit.Assert.assertArrayEquals(strArray5, new java.lang.String[] { "", "hi!", "hi!" });
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
            org.apache.commons.cli.CommandLine commandLine21 = basicParser0.parse(options9, strArray18, properties19, false);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
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
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Option option9 = null;
        java.util.ListIterator listIterator10 = null;
        // The following exception was thrown during execution in test generation
        try {
            basicParser0.processArgs(option9, listIterator10);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.util.ListIterator.hasNext()\" because \"iter\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray8);
        org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test15() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test15");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Options options9 = null;
        org.apache.commons.cli.BasicParser basicParser10 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options11 = null;
        java.lang.String[] strArray16 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray18 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser10, options11, strArray16, false);
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine20 = basicParser0.parse(options9, strArray18, false);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
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
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine20 = basicParser0.parse(options9, strArray16, false);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
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
    public void test17() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test17");
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
            org.apache.commons.cli.CommandLine commandLine21 = basicParser0.parse(options9, strArray18, properties19, true);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
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
    public void test18() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test18");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException5 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException3.addSuppressed((java.lang.Throwable) alreadySelectedException5);
        java.lang.Throwable[] throwableArray7 = alreadySelectedException5.getSuppressed();
        alreadySelectedException1.addSuppressed((java.lang.Throwable) alreadySelectedException5);
        org.apache.commons.cli.Option option9 = alreadySelectedException5.getOption();
        org.junit.Assert.assertNotNull(throwableArray7);
        org.junit.Assert.assertArrayEquals(throwableArray7, new java.lang.Throwable[] {});
        org.junit.Assert.assertNull(option9);
    }

    @Test
    public void test19() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test19");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException1.addSuppressed((java.lang.Throwable) alreadySelectedException3);
        java.lang.Throwable[] throwableArray5 = alreadySelectedException3.getSuppressed();
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException7 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException3.addSuppressed((java.lang.Throwable) alreadySelectedException7);
        java.lang.String str9 = alreadySelectedException3.toString();
        org.junit.Assert.assertNotNull(throwableArray5);
        org.junit.Assert.assertArrayEquals(throwableArray5, new java.lang.Throwable[] {});
        org.junit.Assert.assertEquals("'" + str9 + "' != '" + "org.apache.commons.cli.AlreadySelectedException: " + "'", str9, "org.apache.commons.cli.AlreadySelectedException: ");
    }

    @Test
    public void test20() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test20");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser0, options1, strArray6, false);
        org.apache.commons.cli.Options options9 = null;
        org.apache.commons.cli.BasicParser basicParser10 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options11 = null;
        java.lang.String[] strArray16 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray18 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser10, options11, strArray16, false);
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine19 = basicParser0.parse(options9, strArray18);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
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
    public void test21() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test21");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Option option1 = null;
        java.util.ListIterator listIterator2 = null;
        // The following exception was thrown during execution in test generation
        try {
            basicParser0.processArgs(option1, listIterator2);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"java.util.ListIterator.hasNext()\" because \"iter\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
    }

    @Test
    public void test22() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test22");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        org.apache.commons.cli.BasicParser basicParser2 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options3 = null;
        java.lang.String[] strArray8 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray10 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser2, options3, strArray8, false);
        java.util.Properties properties11 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine13 = basicParser0.parse(options1, strArray8, properties11, true);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray8);
        org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray10);
        org.junit.Assert.assertArrayEquals(strArray10, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test23() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test23");
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
            org.apache.commons.cli.CommandLine commandLine20 = basicParser0.parse(options9, strArray16, properties19);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
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
    public void test24() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test24");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException1.addSuppressed((java.lang.Throwable) alreadySelectedException3);
        java.lang.Throwable[] throwableArray5 = alreadySelectedException3.getSuppressed();
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException7 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException3.addSuppressed((java.lang.Throwable) alreadySelectedException7);
        org.apache.commons.cli.Option option9 = alreadySelectedException7.getOption();
        org.junit.Assert.assertNotNull(throwableArray5);
        org.junit.Assert.assertArrayEquals(throwableArray5, new java.lang.Throwable[] {});
        org.junit.Assert.assertNull(option9);
    }

    @Test
    public void test25() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test25");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException1.addSuppressed((java.lang.Throwable) alreadySelectedException3);
        org.apache.commons.cli.OptionGroup optionGroup5 = alreadySelectedException3.getOptionGroup();
        // The following exception was thrown during execution in test generation
        try {
            java.lang.Class<?> wildcardClass6 = optionGroup5.getClass();
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNull(optionGroup5);
    }

    @Test
    public void test26() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test26");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException1.addSuppressed((java.lang.Throwable) alreadySelectedException3);
        org.apache.commons.cli.OptionGroup optionGroup5 = alreadySelectedException3.getOptionGroup();
        org.apache.commons.cli.Option option6 = alreadySelectedException3.getOption();
        org.junit.Assert.assertNull(optionGroup5);
        org.junit.Assert.assertNull(option6);
    }

    @Test
    public void test27() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test27");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException1.addSuppressed((java.lang.Throwable) alreadySelectedException3);
        java.lang.Throwable[] throwableArray5 = alreadySelectedException3.getSuppressed();
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException7 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException3.addSuppressed((java.lang.Throwable) alreadySelectedException7);
        java.lang.Throwable[] throwableArray9 = alreadySelectedException7.getSuppressed();
        org.junit.Assert.assertNotNull(throwableArray5);
        org.junit.Assert.assertArrayEquals(throwableArray5, new java.lang.Throwable[] {});
        org.junit.Assert.assertNotNull(throwableArray9);
        org.junit.Assert.assertArrayEquals(throwableArray9, new java.lang.Throwable[] {});
    }

    @Test
    public void test28() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test28");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        org.apache.commons.cli.BasicParser basicParser2 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options3 = null;
        java.lang.String[] strArray8 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray10 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser2, options3, strArray8, false);
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine11 = basicParser0.parse(options1, strArray10);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray8);
        org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray10);
        org.junit.Assert.assertArrayEquals(strArray10, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test29() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test29");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException5 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException3.addSuppressed((java.lang.Throwable) alreadySelectedException5);
        java.lang.Throwable[] throwableArray7 = alreadySelectedException5.getSuppressed();
        alreadySelectedException1.addSuppressed((java.lang.Throwable) alreadySelectedException5);
        java.lang.Throwable[] throwableArray9 = alreadySelectedException1.getSuppressed();
        org.junit.Assert.assertNotNull(throwableArray7);
        org.junit.Assert.assertArrayEquals(throwableArray7, new java.lang.Throwable[] {});
        org.junit.Assert.assertNotNull(throwableArray9);
    }

    @Test
    public void test30() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test30");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        org.apache.commons.cli.BasicParser basicParser2 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options3 = null;
        java.lang.String[] strArray8 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray10 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser2, options3, strArray8, false);
        org.apache.commons.cli.Options options11 = null;
        org.apache.commons.cli.BasicParser basicParser12 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options13 = null;
        java.lang.String[] strArray18 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray20 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser12, options13, strArray18, false);
        java.lang.String[] strArray22 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser2, options11, strArray18, true);
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine23 = basicParser0.parse(options1, strArray22);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray8);
        org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray10);
        org.junit.Assert.assertArrayEquals(strArray10, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray18);
        org.junit.Assert.assertArrayEquals(strArray18, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray20);
        org.junit.Assert.assertArrayEquals(strArray20, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray22);
        org.junit.Assert.assertArrayEquals(strArray22, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test31() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test31");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.Option option2 = alreadySelectedException1.getOption();
        org.apache.commons.cli.OptionGroup optionGroup3 = alreadySelectedException1.getOptionGroup();
        org.junit.Assert.assertNull(option2);
        org.junit.Assert.assertNull(optionGroup3);
    }

    @Test
    public void test32() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test32");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        java.lang.String[] strArray6 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray8 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser0, options1, strArray6, false);
        java.lang.Class<?> wildcardClass9 = strArray8.getClass();
        org.junit.Assert.assertNotNull(strArray6);
        org.junit.Assert.assertArrayEquals(strArray6, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray8);
        org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(wildcardClass9);
    }

    @Test
    public void test33() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test33");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("hi!");
    }

    @Test
    public void test34() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test34");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.Option option2 = alreadySelectedException1.getOption();
        org.apache.commons.cli.Option option3 = alreadySelectedException1.getOption();
        // The following exception was thrown during execution in test generation
        try {
            java.lang.Class<?> wildcardClass4 = option3.getClass();
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNull(option2);
        org.junit.Assert.assertNull(option3);
    }

    @Test
    public void test35() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test35");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException1.addSuppressed((java.lang.Throwable) alreadySelectedException3);
        java.lang.Throwable[] throwableArray5 = alreadySelectedException3.getSuppressed();
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException7 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException9 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException7.addSuppressed((java.lang.Throwable) alreadySelectedException9);
        java.lang.Throwable[] throwableArray11 = alreadySelectedException9.getSuppressed();
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException13 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException9.addSuppressed((java.lang.Throwable) alreadySelectedException13);
        alreadySelectedException3.addSuppressed((java.lang.Throwable) alreadySelectedException13);
        java.lang.Class<?> wildcardClass16 = alreadySelectedException3.getClass();
        org.junit.Assert.assertNotNull(throwableArray5);
        org.junit.Assert.assertArrayEquals(throwableArray5, new java.lang.Throwable[] {});
        org.junit.Assert.assertNotNull(throwableArray11);
        org.junit.Assert.assertArrayEquals(throwableArray11, new java.lang.Throwable[] {});
        org.junit.Assert.assertNotNull(wildcardClass16);
    }

    @Test
    public void test36() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test36");
        org.apache.commons.cli.BasicParser basicParser0 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options1 = null;
        org.apache.commons.cli.BasicParser basicParser2 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options3 = null;
        java.lang.String[] strArray8 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray10 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser2, options3, strArray8, false);
        org.apache.commons.cli.Options options11 = null;
        org.apache.commons.cli.BasicParser basicParser12 = new org.apache.commons.cli.BasicParser();
        org.apache.commons.cli.Options options13 = null;
        java.lang.String[] strArray18 = new java.lang.String[] { "", "", "", "" };
        java.lang.String[] strArray20 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser12, options13, strArray18, false);
        java.lang.String[] strArray22 = (java.lang.String[]) flatten_Options_StringArray_boolean.invoke(basicParser2, options11, strArray18, true);
        java.util.Properties properties23 = null;
        // The following exception was thrown during execution in test generation
        try {
            org.apache.commons.cli.CommandLine commandLine25 = basicParser0.parse(options1, strArray18, properties23, true);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"org.apache.commons.cli.Options.helpOptions()\" because \"options\" is null");
        } catch (java.lang.NullPointerException e) {
            // Expected exception.
        }
        org.junit.Assert.assertNotNull(strArray8);
        org.junit.Assert.assertArrayEquals(strArray8, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray10);
        org.junit.Assert.assertArrayEquals(strArray10, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray18);
        org.junit.Assert.assertArrayEquals(strArray18, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray20);
        org.junit.Assert.assertArrayEquals(strArray20, new java.lang.String[] { "", "", "", "" });
        org.junit.Assert.assertNotNull(strArray22);
        org.junit.Assert.assertArrayEquals(strArray22, new java.lang.String[] { "", "", "", "" });
    }

    @Test
    public void test37() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test37");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException1 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException3 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException1.addSuppressed((java.lang.Throwable) alreadySelectedException3);
        java.lang.Throwable[] throwableArray5 = alreadySelectedException3.getSuppressed();
        java.lang.Throwable[] throwableArray6 = alreadySelectedException3.getSuppressed();
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException8 = new org.apache.commons.cli.AlreadySelectedException("");
        org.apache.commons.cli.AlreadySelectedException alreadySelectedException10 = new org.apache.commons.cli.AlreadySelectedException("");
        alreadySelectedException8.addSuppressed((java.lang.Throwable) alreadySelectedException10);
        org.apache.commons.cli.OptionGroup optionGroup12 = alreadySelectedException10.getOptionGroup();
        alreadySelectedException3.addSuppressed((java.lang.Throwable) alreadySelectedException10);
        org.apache.commons.cli.OptionGroup optionGroup14 = alreadySelectedException3.getOptionGroup();
        org.apache.commons.cli.Option option15 = alreadySelectedException3.getOption();
        org.junit.Assert.assertNotNull(throwableArray5);
        org.junit.Assert.assertArrayEquals(throwableArray5, new java.lang.Throwable[] {});
        org.junit.Assert.assertNotNull(throwableArray6);
        org.junit.Assert.assertArrayEquals(throwableArray6, new java.lang.Throwable[] {});
        org.junit.Assert.assertNull(optionGroup12);
        org.junit.Assert.assertNull(optionGroup14);
        org.junit.Assert.assertNull(option15);
    }
}

