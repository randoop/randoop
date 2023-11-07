import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegressionTest0 {

    public static boolean debug = false;

    private static java.lang.reflect.Method privateMethod1_Object;

    private static java.lang.reflect.Method privateMethod2_Integer;

    static {
        try {
            privateMethod1_Object = MyClass1.class.getDeclaredMethod("privateMethod1", java.lang.Object.class);
            privateMethod1_Object.setAccessible(true);
            privateMethod2_Integer = MyClass2.class.getDeclaredMethod("privateMethod2", java.lang.Integer.class);
            privateMethod2_Integer.setAccessible(true);
        } catch (Throwable t) {
            t.printStackTrace(System.out);
        }
    }

    @Test
    public void test01() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test01");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) (-4));
    }

    @Test
    public void test02() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test02");
        java.lang.Object obj0 = null;
        MyClass1 myClass1_1 = new MyClass1(obj0);
        MyClass1 myClass1_2 = new MyClass1(obj0);
        java.lang.Object obj3 = new java.lang.Object();
        MyClass1 myClass1_4 = new MyClass1(obj3);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) (short) 100);
        java.lang.String str7 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) (short) 100);
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "100" + "'", str6, "100");
        org.junit.Assert.assertEquals("'" + str7 + "' != '" + "100" + "'", str7, "100");
    }

    @Test
    public void test03() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test03");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 10L);
    }

    @Test
    public void test04() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test04");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 10);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) (byte) 10);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) false);
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "10" + "'", str3, "10");
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "false" + "'", str5, "false");
    }

    @Test
    public void test05() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test05");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 20);
    }

    @Test
    public void test06() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test06");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 100);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) 100);
        java.lang.Class<?> wildcardClass3 = myClass1_2.getClass();
        org.junit.Assert.assertNotNull(wildcardClass3);
    }

    @Test
    public void test07() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test07");
        java.lang.Object obj0 = new java.lang.Object();
        MyClass1 myClass1_1 = new MyClass1(obj0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) (short) 100);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) "hi!");
        java.lang.Object obj6 = new java.lang.Object();
        MyClass1 myClass1_7 = new MyClass1(obj6);
        java.lang.String str8 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, obj6);
        java.lang.Class<?> wildcardClass9 = obj6.getClass();
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "100" + "'", str3, "100");
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "hi!" + "'", str5, "hi!");
        org.junit.Assert.assertNotNull(wildcardClass9);
    }

    @Test
    public void test08() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test08");
        java.lang.Object obj0 = new java.lang.Object();
        java.lang.Class<?> wildcardClass1 = obj0.getClass();
        org.junit.Assert.assertNotNull(wildcardClass1);
    }

    @Test
    public void test09() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test09");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) (-8));
    }

    @Test
    public void test10() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test10");
        java.lang.Object obj0 = new java.lang.Object();
        MyClass1 myClass1_1 = new MyClass1(obj0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) (short) 100);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) "hi!");
        java.lang.Class<?> wildcardClass6 = myClass1_1.getClass();
        MyClass1 myClass1_7 = new MyClass1((java.lang.Object) myClass1_1);
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "100" + "'", str3, "100");
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "hi!" + "'", str5, "hi!");
        org.junit.Assert.assertNotNull(wildcardClass6);
    }

    @Test
    public void test11() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test11");
        java.lang.Object obj0 = new java.lang.Object();
        MyClass1 myClass1_1 = new MyClass1(obj0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) (short) 100);
        java.lang.Class<?> wildcardClass4 = myClass1_1.getClass();
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "100" + "'", str3, "100");
        org.junit.Assert.assertNotNull(wildcardClass4);
    }

    @Test
    public void test12() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test12");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) (-16));
    }

    @Test
    public void test13() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test13");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 10);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) (byte) 10);
        java.lang.Class<?> wildcardClass4 = myClass1_1.getClass();
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "10" + "'", str3, "10");
        org.junit.Assert.assertNotNull(wildcardClass4);
    }

    @Test
    public void test14() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test14");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 10);
        java.lang.Class<?> wildcardClass2 = myClass1_1.getClass();
        org.junit.Assert.assertNotNull(wildcardClass2);
    }

    @Test
    public void test15() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test15");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) (short) 100);
        MyClass1 myClass1_3 = new MyClass1((java.lang.Object) 0);
        java.lang.Class<?> wildcardClass4 = myClass1_3.getClass();
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) myClass1_3);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_5);
        java.lang.Class<?> wildcardClass7 = myClass1_5.getClass();
        org.junit.Assert.assertNotNull(wildcardClass4);
        org.junit.Assert.assertNotNull(wildcardClass7);
    }

    @Test
    public void test16() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test16");
        java.lang.Object obj0 = new java.lang.Object();
        MyClass1 myClass1_1 = new MyClass1(obj0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) (short) 100);
        MyClass1 myClass1_4 = new MyClass1((java.lang.Object) (short) 100);
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "100" + "'", str3, "100");
    }

    @Test
    public void test17() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test17");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) true);
        java.lang.Object obj2 = new java.lang.Object();
        MyClass1 myClass1_3 = new MyClass1(obj2);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_3, (java.lang.Object) (short) 100);
        java.lang.String str7 = (java.lang.String) privateMethod1_Object.invoke(myClass1_3, (java.lang.Object) "hi!");
        java.lang.Object obj8 = new java.lang.Object();
        MyClass1 myClass1_9 = new MyClass1(obj8);
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_3, obj8);
        java.lang.String str11 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, obj8);
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "100" + "'", str5, "100");
        org.junit.Assert.assertEquals("'" + str7 + "' != '" + "hi!" + "'", str7, "hi!");
    }

    @Test
    public void test18() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test18");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 1);
        java.lang.Class<?> wildcardClass2 = myClass2_1.getClass();
        org.junit.Assert.assertNotNull(wildcardClass2);
    }

    @Test
    public void test19() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test19");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 800);
    }

    @Test
    public void test20() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test20");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) (-2));
    }

    @Test
    public void test21() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test21");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0);
        java.lang.Class<?> wildcardClass4 = myClass1_1.getClass();
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0" + "'", str3, "0");
        org.junit.Assert.assertNotNull(wildcardClass4);
    }

    @Test
    public void test22() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test22");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 10);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) myClass2_1);
    }

    @Test
    public void test23() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test23");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) (-32));
    }

    @Test
    public void test24() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test24");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 1);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) "");
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0L);
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "" + "'", str3, "");
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "0" + "'", str5, "0");
    }

    @Test
    public void test25() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test25");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 400);
        java.lang.Class<?> wildcardClass2 = myClass2_1.getClass();
        org.junit.Assert.assertNotNull(wildcardClass2);
    }

    @Test
    public void test26() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test26");
        java.lang.Object obj0 = new java.lang.Object();
        MyClass1 myClass1_1 = new MyClass1(obj0);
        MyClass1 myClass1_2 = new MyClass1(obj0);
        java.lang.Class<?> wildcardClass3 = myClass1_2.getClass();
        org.junit.Assert.assertNotNull(wildcardClass3);
    }

    @Test
    public void test27() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test27");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0);
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_5);
        java.lang.String str8 = (java.lang.String) privateMethod1_Object.invoke(myClass1_5, (java.lang.Object) 1L);
        java.lang.Class<?> wildcardClass9 = myClass1_5.getClass();
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0" + "'", str3, "0");
        org.junit.Assert.assertEquals("'" + str8 + "' != '" + "1" + "'", str8, "1");
        org.junit.Assert.assertNotNull(wildcardClass9);
    }

    @Test
    public void test28() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test28");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) (short) 100);
        MyClass1 myClass1_3 = new MyClass1((java.lang.Object) 0);
        java.lang.Class<?> wildcardClass4 = myClass1_3.getClass();
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) myClass1_3);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_5);
        MyClass1 myClass1_7 = new MyClass1((java.lang.Object) myClass1_1);
        org.junit.Assert.assertNotNull(wildcardClass4);
    }

    @Test
    public void test29() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test29");
        java.lang.Object obj0 = null;
        MyClass1 myClass1_1 = new MyClass1(obj0);
        java.lang.Object obj2 = new java.lang.Object();
        MyClass1 myClass1_3 = new MyClass1(obj2);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_3, (java.lang.Object) (short) 100);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) str5);
        MyClass1 myClass1_7 = new MyClass1((java.lang.Object) str5);
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "100" + "'", str5, "100");
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "100" + "'", str6, "100");
    }

    @Test
    public void test30() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test30");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) '4');
    }

    @Test
    public void test31() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test31");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) '#');
        MyClass1 myClass1_3 = new MyClass1((java.lang.Object) 0);
        java.lang.Class<?> wildcardClass4 = myClass1_3.getClass();
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_3);
        org.junit.Assert.assertNotNull(wildcardClass4);
    }

    @Test
    public void test32() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test32");
        java.lang.Object obj0 = null;
        MyClass1 myClass1_1 = new MyClass1(obj0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) (byte) 0);
        java.lang.Class<?> wildcardClass4 = myClass1_1.getClass();
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0" + "'", str3, "0");
        org.junit.Assert.assertNotNull(wildcardClass4);
    }

    @Test
    public void test33() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test33");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 100L);
        MyClass1 myClass1_6 = new MyClass1((java.lang.Object) 100L);
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0" + "'", str3, "0");
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "100" + "'", str5, "100");
    }

    @Test
    public void test34() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test34");
        java.lang.Object obj0 = null;
        MyClass1 myClass1_1 = new MyClass1(obj0);
        MyClass1 myClass1_2 = new MyClass1(obj0);
        MyClass1 myClass1_4 = new MyClass1((java.lang.Object) "hi!");
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) "hi!");
        java.lang.Object obj6 = new java.lang.Object();
        MyClass1 myClass1_7 = new MyClass1(obj6);
        MyClass1 myClass1_8 = new MyClass1(obj6);
        MyClass1 myClass1_9 = new MyClass1((java.lang.Object) myClass1_8);
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) myClass1_9);
        MyClass2 myClass2_12 = new MyClass2((java.lang.Integer) (-1));
        java.lang.Class<?> wildcardClass13 = myClass2_12.getClass();
        java.lang.String str14 = (java.lang.String) privateMethod1_Object.invoke(myClass1_9, (java.lang.Object) wildcardClass13);
        MyClass1 myClass1_15 = new MyClass1((java.lang.Object) myClass1_9);
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "hi!" + "'", str5, "hi!");
        org.junit.Assert.assertNotNull(wildcardClass13);
        org.junit.Assert.assertEquals("'" + str14 + "' != '" + "class MyClass2" + "'", str14, "class MyClass2");
    }

    @Test
    public void test35() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test35");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 100L);
        java.lang.Class<?> wildcardClass6 = myClass1_1.getClass();
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0" + "'", str3, "0");
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "100" + "'", str5, "100");
        org.junit.Assert.assertNotNull(wildcardClass6);
    }

    @Test
    public void test36() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test36");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 0);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) 0);
        MyClass1 myClass1_4 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) 0);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) 0);
        java.lang.String str9 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) myClass1_8);
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) str9);
        MyClass2 myClass2_12 = new MyClass2((java.lang.Integer) 100);
        MyClass1 myClass1_13 = new MyClass1((java.lang.Object) 100);
        java.lang.String str14 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) 100);
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "0" + "'", str6, "0");
        org.junit.Assert.assertEquals("'" + str14 + "' != '" + "100" + "'", str14, "100");
    }

    @Test
    public void test37() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test37");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 80);
        java.lang.Class<?> wildcardClass2 = myClass2_1.getClass();
        org.junit.Assert.assertNotNull(wildcardClass2);
    }

    @Test
    public void test38() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test38");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 100);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) 100);
        MyClass1 myClass1_4 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) 0);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) 0);
        java.lang.String str9 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) myClass1_8);
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) str9);
        java.lang.Class<?> wildcardClass11 = myClass1_2.getClass();
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "0" + "'", str6, "0");
        org.junit.Assert.assertNotNull(wildcardClass11);
    }

    @Test
    public void test39() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test39");
        java.lang.Object obj0 = null;
        MyClass1 myClass1_1 = new MyClass1(obj0);
        MyClass1 myClass1_2 = new MyClass1(obj0);
        MyClass1 myClass1_4 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) 0);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) 0);
        java.lang.String str9 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) myClass1_8);
        MyClass1 myClass1_11 = new MyClass1((java.lang.Object) (-1));
        java.lang.String str13 = (java.lang.String) privateMethod1_Object.invoke(myClass1_11, (java.lang.Object) (byte) 10);
        java.lang.String str14 = (java.lang.String) privateMethod1_Object.invoke(myClass1_8, (java.lang.Object) (byte) 10);
        MyClass1 myClass1_15 = new MyClass1((java.lang.Object) myClass1_8);
        java.lang.String str16 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) myClass1_15);
        java.lang.Class<?> wildcardClass17 = myClass1_2.getClass();
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "0" + "'", str6, "0");
        org.junit.Assert.assertEquals("'" + str13 + "' != '" + "10" + "'", str13, "10");
        org.junit.Assert.assertEquals("'" + str14 + "' != '" + "10" + "'", str14, "10");
        org.junit.Assert.assertNotNull(wildcardClass17);
    }

    @Test
    public void test40() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test40");
        java.lang.Object obj0 = null;
        MyClass1 myClass1_1 = new MyClass1(obj0);
        java.lang.Object obj2 = new java.lang.Object();
        MyClass1 myClass1_3 = new MyClass1(obj2);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_3, (java.lang.Object) (short) 100);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) str5);
        MyClass1 myClass1_7 = new MyClass1((java.lang.Object) str6);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) myClass1_7);
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "100" + "'", str5, "100");
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "100" + "'", str6, "100");
    }

    @Test
    public void test41() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test41");
        java.lang.Object obj0 = new java.lang.Object();
        MyClass1 myClass1_1 = new MyClass1(obj0);
        MyClass1 myClass1_2 = new MyClass1(obj0);
        MyClass1 myClass1_3 = new MyClass1((java.lang.Object) myClass1_2);
        java.lang.Class<?> wildcardClass4 = myClass1_2.getClass();
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) wildcardClass4);
        java.lang.Class<?> wildcardClass6 = myClass1_5.getClass();
        org.junit.Assert.assertNotNull(wildcardClass4);
        org.junit.Assert.assertNotNull(wildcardClass6);
    }

    @Test
    public void test42() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test42");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) true);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 10L);
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "10" + "'", str3, "10");
    }

    @Test
    public void test43() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test43");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) (-1));
        java.lang.Class<?> wildcardClass2 = myClass1_1.getClass();
        org.junit.Assert.assertNotNull(wildcardClass2);
    }

    @Test
    public void test44() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test44");
        java.lang.Object obj0 = new java.lang.Object();
        MyClass1 myClass1_1 = new MyClass1(obj0);
        MyClass2 myClass2_3 = new MyClass2((java.lang.Integer) 200);
        java.lang.String str4 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass2_3);
        MyClass1 myClass1_6 = new MyClass1((java.lang.Object) 0);
        java.lang.String str8 = (java.lang.String) privateMethod1_Object.invoke(myClass1_6, (java.lang.Object) 0);
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_6, (java.lang.Object) 100L);
        java.lang.String str11 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) str10);
        org.junit.Assert.assertEquals("'" + str8 + "' != '" + "0" + "'", str8, "0");
        org.junit.Assert.assertEquals("'" + str10 + "' != '" + "100" + "'", str10, "100");
        org.junit.Assert.assertEquals("'" + str11 + "' != '" + "100" + "'", str11, "100");
    }

    @Test
    public void test45() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test45");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0);
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_5);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) (-1));
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_8, (java.lang.Object) (byte) 10);
        java.lang.String str11 = (java.lang.String) privateMethod1_Object.invoke(myClass1_5, (java.lang.Object) (byte) 10);
        MyClass1 myClass1_12 = new MyClass1((java.lang.Object) myClass1_5);
        MyClass2 myClass2_14 = new MyClass2((java.lang.Integer) 100);
        MyClass1 myClass1_15 = new MyClass1((java.lang.Object) 100);
        MyClass1 myClass1_17 = new MyClass1((java.lang.Object) 0);
        java.lang.String str19 = (java.lang.String) privateMethod1_Object.invoke(myClass1_17, (java.lang.Object) 0);
        MyClass1 myClass1_21 = new MyClass1((java.lang.Object) 0);
        java.lang.String str22 = (java.lang.String) privateMethod1_Object.invoke(myClass1_17, (java.lang.Object) myClass1_21);
        java.lang.String str23 = (java.lang.String) privateMethod1_Object.invoke(myClass1_15, (java.lang.Object) str22);
        java.lang.String str24 = (java.lang.String) privateMethod1_Object.invoke(myClass1_5, (java.lang.Object) str23);
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0" + "'", str3, "0");
        org.junit.Assert.assertEquals("'" + str10 + "' != '" + "10" + "'", str10, "10");
        org.junit.Assert.assertEquals("'" + str11 + "' != '" + "10" + "'", str11, "10");
        org.junit.Assert.assertEquals("'" + str19 + "' != '" + "0" + "'", str19, "0");
    }

    @Test
    public void test46() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test46");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 100.0d);
        java.lang.Class<?> wildcardClass2 = myClass1_1.getClass();
        org.junit.Assert.assertNotNull(wildcardClass2);
    }

    @Test
    public void test47() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test47");
        java.lang.Object obj0 = new java.lang.Object();
        MyClass1 myClass1_1 = new MyClass1(obj0);
        MyClass1 myClass1_2 = new MyClass1(obj0);
        MyClass1 myClass1_3 = new MyClass1((java.lang.Object) myClass1_2);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) "hi!");
        java.lang.String str7 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) (short) 0);
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "hi!" + "'", str5, "hi!");
        org.junit.Assert.assertEquals("'" + str7 + "' != '" + "0" + "'", str7, "0");
    }

    @Test
    public void test48() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test48");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 4);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) myClass2_1);
    }

    @Test
    public void test49() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test49");
        java.lang.Object obj0 = null;
        MyClass1 myClass1_1 = new MyClass1(obj0);
        MyClass1 myClass1_2 = new MyClass1(obj0);
        MyClass1 myClass1_4 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) 0);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) 0);
        java.lang.String str9 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) myClass1_8);
        MyClass1 myClass1_11 = new MyClass1((java.lang.Object) (-1));
        java.lang.String str13 = (java.lang.String) privateMethod1_Object.invoke(myClass1_11, (java.lang.Object) (byte) 10);
        java.lang.String str14 = (java.lang.String) privateMethod1_Object.invoke(myClass1_8, (java.lang.Object) (byte) 10);
        MyClass1 myClass1_15 = new MyClass1((java.lang.Object) myClass1_8);
        java.lang.String str16 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) myClass1_15);
        java.lang.String str18 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) (short) 100);
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "0" + "'", str6, "0");
        org.junit.Assert.assertEquals("'" + str13 + "' != '" + "10" + "'", str13, "10");
        org.junit.Assert.assertEquals("'" + str14 + "' != '" + "10" + "'", str14, "10");
        org.junit.Assert.assertEquals("'" + str18 + "' != '" + "100" + "'", str18, "100");
    }

    @Test
    public void test50() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test50");
        java.lang.Object obj0 = null;
        MyClass1 myClass1_1 = new MyClass1(obj0);
        java.lang.Object obj2 = new java.lang.Object();
        MyClass1 myClass1_3 = new MyClass1(obj2);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_3, (java.lang.Object) (short) 100);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) str5);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) true);
        java.lang.String str9 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) true);
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "100" + "'", str5, "100");
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "100" + "'", str6, "100");
        org.junit.Assert.assertEquals("'" + str9 + "' != '" + "true" + "'", str9, "true");
    }

    @Test
    public void test51() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test51");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 10);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) (byte) 10);
        java.lang.Object obj4 = null;
        MyClass1 myClass1_5 = new MyClass1(obj4);
        MyClass1 myClass1_6 = new MyClass1(obj4);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) "hi!");
        java.lang.String str9 = (java.lang.String) privateMethod1_Object.invoke(myClass1_6, (java.lang.Object) "hi!");
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) str9);
        MyClass1 myClass1_11 = new MyClass1((java.lang.Object) str9);
        java.lang.Class<?> wildcardClass12 = myClass1_11.getClass();
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "10" + "'", str3, "10");
        org.junit.Assert.assertEquals("'" + str9 + "' != '" + "hi!" + "'", str9, "hi!");
        org.junit.Assert.assertEquals("'" + str10 + "' != '" + "hi!" + "'", str10, "hi!");
        org.junit.Assert.assertNotNull(wildcardClass12);
    }

    @Test
    public void test52() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test52");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 4);
        java.lang.Class<?> wildcardClass2 = myClass2_1.getClass();
        org.junit.Assert.assertNotNull(wildcardClass2);
    }

    @Test
    public void test53() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test53");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) (-1));
        java.lang.Object obj2 = new java.lang.Object();
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, obj2);
    }

    @Test
    public void test54() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test54");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 100);
        java.lang.Class<?> wildcardClass2 = myClass2_1.getClass();
        org.junit.Assert.assertNotNull(wildcardClass2);
    }

    @Test
    public void test55() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test55");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 8);
    }

    @Test
    public void test56() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test56");
        java.lang.Object obj0 = null;
        MyClass1 myClass1_1 = new MyClass1(obj0);
        java.lang.Object obj2 = new java.lang.Object();
        MyClass1 myClass1_3 = new MyClass1(obj2);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_3, (java.lang.Object) (short) 100);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) str5);
        java.lang.String str8 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 8);
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "100" + "'", str5, "100");
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "100" + "'", str6, "100");
        org.junit.Assert.assertEquals("'" + str8 + "' != '" + "8" + "'", str8, "8");
    }

    @Test
    public void test57() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test57");
        java.lang.Object obj0 = null;
        MyClass1 myClass1_1 = new MyClass1(obj0);
        java.lang.Object obj2 = new java.lang.Object();
        MyClass1 myClass1_3 = new MyClass1(obj2);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_3, (java.lang.Object) (short) 100);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) str5);
        java.lang.Class<?> wildcardClass7 = myClass1_1.getClass();
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) wildcardClass7);
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "100" + "'", str5, "100");
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "100" + "'", str6, "100");
        org.junit.Assert.assertNotNull(wildcardClass7);
    }

    @Test
    public void test58() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test58");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 2);
    }

    @Test
    public void test59() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test59");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0);
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_5);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) (-1));
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_8, (java.lang.Object) (byte) 10);
        java.lang.String str11 = (java.lang.String) privateMethod1_Object.invoke(myClass1_5, (java.lang.Object) (byte) 10);
        MyClass1 myClass1_12 = new MyClass1((java.lang.Object) myClass1_5);
        java.lang.Class<?> wildcardClass13 = myClass1_12.getClass();
        MyClass1 myClass1_14 = new MyClass1((java.lang.Object) wildcardClass13);
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0" + "'", str3, "0");
        org.junit.Assert.assertEquals("'" + str10 + "' != '" + "10" + "'", str10, "10");
        org.junit.Assert.assertEquals("'" + str11 + "' != '" + "10" + "'", str11, "10");
        org.junit.Assert.assertNotNull(wildcardClass13);
    }

    @Test
    public void test60() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test60");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 0);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) 0);
        MyClass1 myClass1_4 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) 0);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) 0);
        java.lang.String str9 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) myClass1_8);
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) str9);
        MyClass1 myClass1_11 = new MyClass1((java.lang.Object) str10);
        MyClass1 myClass1_12 = new MyClass1((java.lang.Object) myClass1_11);
        java.lang.Class<?> wildcardClass13 = myClass1_12.getClass();
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "0" + "'", str6, "0");
        org.junit.Assert.assertNotNull(wildcardClass13);
    }

    @Test
    public void test61() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test61");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 100);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) 100);
        MyClass1 myClass1_4 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) 0);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) 0);
        java.lang.String str9 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) myClass1_8);
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) str9);
        MyClass1 myClass1_11 = new MyClass1((java.lang.Object) str9);
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "0" + "'", str6, "0");
    }

    @Test
    public void test62() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test62");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 0);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) 0);
        MyClass1 myClass1_3 = new MyClass1((java.lang.Object) myClass1_2);
    }

    @Test
    public void test63() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test63");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 1);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0.0f);
        MyClass2 myClass2_5 = new MyClass2((java.lang.Integer) (-1));
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) (-1));
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0.0" + "'", str3, "0.0");
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "-1" + "'", str6, "-1");
    }

    @Test
    public void test64() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test64");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) (-512));
    }

    @Test
    public void test65() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test65");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 0);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) 0);
        java.lang.String str4 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) (byte) 1);
        java.lang.Object obj5 = null;
        // The following exception was thrown during execution in test generation
        try {
            java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, obj5);
            org.junit.Assert.fail("Expected exception of type java.lang.reflect.InvocationTargetException");
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof java.lang.NullPointerException) {
                // Expected exception.
            } else {
                org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException");
            }
        }
        org.junit.Assert.assertEquals("'" + str4 + "' != '" + "1" + "'", str4, "1");
    }

    @Test
    public void test66() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test66");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 160);
    }

    @Test
    public void test67() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test67");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) (short) 100);
        MyClass1 myClass1_3 = new MyClass1((java.lang.Object) 0);
        java.lang.Class<?> wildcardClass4 = myClass1_3.getClass();
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) myClass1_3);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_5);
        java.lang.String str8 = (java.lang.String) privateMethod1_Object.invoke(myClass1_5, (java.lang.Object) "100");
        org.junit.Assert.assertNotNull(wildcardClass4);
        org.junit.Assert.assertEquals("'" + str8 + "' != '" + "100" + "'", str8, "100");
    }

    @Test
    public void test68() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test68");
        java.lang.Object obj0 = null;
        MyClass1 myClass1_1 = new MyClass1(obj0);
        MyClass1 myClass1_2 = new MyClass1(obj0);
        MyClass1 myClass1_4 = new MyClass1((java.lang.Object) "hi!");
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) "hi!");
        MyClass1 myClass1_6 = new MyClass1((java.lang.Object) "hi!");
        java.lang.Class<?> wildcardClass7 = myClass1_6.getClass();
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "hi!" + "'", str5, "hi!");
        org.junit.Assert.assertNotNull(wildcardClass7);
    }

    @Test
    public void test69() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test69");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) "true");
    }

    @Test
    public void test70() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test70");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 10);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) 10);
        MyClass1 myClass1_4 = new MyClass1((java.lang.Object) 0);
        java.lang.Class<?> wildcardClass5 = myClass1_4.getClass();
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) myClass1_4);
        org.junit.Assert.assertNotNull(wildcardClass5);
    }

    @Test
    public void test71() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test71");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) (byte) -1);
        MyClass1 myClass1_3 = new MyClass1((java.lang.Object) 10);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_3, (java.lang.Object) (byte) 10);
        java.lang.Object obj6 = null;
        MyClass1 myClass1_7 = new MyClass1(obj6);
        MyClass1 myClass1_8 = new MyClass1(obj6);
        MyClass1 myClass1_10 = new MyClass1((java.lang.Object) "hi!");
        java.lang.String str11 = (java.lang.String) privateMethod1_Object.invoke(myClass1_8, (java.lang.Object) "hi!");
        java.lang.String str12 = (java.lang.String) privateMethod1_Object.invoke(myClass1_3, (java.lang.Object) str11);
        java.lang.Class<?> wildcardClass13 = myClass1_3.getClass();
        java.lang.String str14 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_3);
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "10" + "'", str5, "10");
        org.junit.Assert.assertEquals("'" + str11 + "' != '" + "hi!" + "'", str11, "hi!");
        org.junit.Assert.assertEquals("'" + str12 + "' != '" + "hi!" + "'", str12, "hi!");
        org.junit.Assert.assertNotNull(wildcardClass13);
    }

    @Test
    public void test72() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test72");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0);
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_5);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) 0);
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_8, (java.lang.Object) 0);
        MyClass1 myClass1_12 = new MyClass1((java.lang.Object) 0);
        java.lang.String str13 = (java.lang.String) privateMethod1_Object.invoke(myClass1_8, (java.lang.Object) myClass1_12);
        java.lang.String str14 = (java.lang.String) privateMethod1_Object.invoke(myClass1_5, (java.lang.Object) myClass1_8);
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0" + "'", str3, "0");
        org.junit.Assert.assertEquals("'" + str10 + "' != '" + "0" + "'", str10, "0");
    }

    @Test
    public void test73() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test73");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) (-1));
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) (-1));
        MyClass1 myClass1_3 = new MyClass1((java.lang.Object) myClass1_2);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) 800);
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "800" + "'", str5, "800");
    }

    @Test
    public void test74() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test74");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 0);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) 0);
        MyClass1 myClass1_4 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) 0);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) 0);
        java.lang.String str9 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) myClass1_8);
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) str9);
        MyClass1 myClass1_11 = new MyClass1((java.lang.Object) myClass1_2);
        MyClass2 myClass2_13 = new MyClass2((java.lang.Integer) 0);
        MyClass1 myClass1_14 = new MyClass1((java.lang.Object) 0);
        MyClass1 myClass1_16 = new MyClass1((java.lang.Object) 0);
        java.lang.String str18 = (java.lang.String) privateMethod1_Object.invoke(myClass1_16, (java.lang.Object) 0);
        MyClass1 myClass1_20 = new MyClass1((java.lang.Object) 0);
        java.lang.String str21 = (java.lang.String) privateMethod1_Object.invoke(myClass1_16, (java.lang.Object) myClass1_20);
        java.lang.String str22 = (java.lang.String) privateMethod1_Object.invoke(myClass1_14, (java.lang.Object) str21);
        MyClass1 myClass1_23 = new MyClass1((java.lang.Object) str22);
        java.lang.String str24 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) str22);
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "0" + "'", str6, "0");
        org.junit.Assert.assertEquals("'" + str18 + "' != '" + "0" + "'", str18, "0");
    }

    @Test
    public void test75() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test75");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) (byte) 0);
    }

    @Test
    public void test76() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test76");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 100);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) 100);
        MyClass1 myClass1_4 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) 0);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) 0);
        java.lang.String str9 = (java.lang.String) privateMethod1_Object.invoke(myClass1_4, (java.lang.Object) myClass1_8);
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) str9);
        MyClass1 myClass1_12 = new MyClass1((java.lang.Object) (short) 100);
        java.lang.Class<?> wildcardClass13 = myClass1_12.getClass();
        java.lang.String str14 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) myClass1_12);
        org.junit.Assert.assertEquals("'" + str6 + "' != '" + "0" + "'", str6, "0");
        org.junit.Assert.assertNotNull(wildcardClass13);
    }

    @Test
    public void test77() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test77");
        java.lang.Object obj0 = new java.lang.Object();
        MyClass1 myClass1_1 = new MyClass1(obj0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) (short) 100);
        java.lang.String str5 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) "hi!");
        java.lang.Object obj6 = new java.lang.Object();
        MyClass1 myClass1_7 = new MyClass1(obj6);
        java.lang.String str8 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, obj6);
        MyClass1 myClass1_9 = new MyClass1((java.lang.Object) str8);
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "100" + "'", str3, "100");
        org.junit.Assert.assertEquals("'" + str5 + "' != '" + "hi!" + "'", str5, "hi!");
    }

    @Test
    public void test78() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test78");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) "hi!");
        java.lang.Class<?> wildcardClass2 = myClass1_1.getClass();
        org.junit.Assert.assertNotNull(wildcardClass2);
    }

    @Test
    public void test79() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test79");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0);
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_5);
        java.lang.Class<?> wildcardClass7 = myClass1_5.getClass();
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0" + "'", str3, "0");
        org.junit.Assert.assertNotNull(wildcardClass7);
    }

    @Test
    public void test80() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test80");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) (-1));
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) (-1));
        java.lang.String str4 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) 20);
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) 20);
        org.junit.Assert.assertEquals("'" + str4 + "' != '" + "20" + "'", str4, "20");
    }

    @Test
    public void test81() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test81");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) "4");
    }

    @Test
    public void test82() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test82");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 16);
    }

    @Test
    public void test83() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test83");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) (-1));
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) (-1));
        MyClass1 myClass1_3 = new MyClass1((java.lang.Object) myClass1_2);
        MyClass2 myClass2_5 = new MyClass2((java.lang.Integer) 100);
        MyClass1 myClass1_6 = new MyClass1((java.lang.Object) 100);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) 0);
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_8, (java.lang.Object) 0);
        MyClass1 myClass1_12 = new MyClass1((java.lang.Object) 0);
        java.lang.String str13 = (java.lang.String) privateMethod1_Object.invoke(myClass1_8, (java.lang.Object) myClass1_12);
        java.lang.String str14 = (java.lang.String) privateMethod1_Object.invoke(myClass1_6, (java.lang.Object) str13);
        java.lang.String str15 = (java.lang.String) privateMethod1_Object.invoke(myClass1_3, (java.lang.Object) str13);
        org.junit.Assert.assertEquals("'" + str10 + "' != '" + "0" + "'", str10, "0");
    }

    @Test
    public void test84() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test84");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0);
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_5);
        java.lang.Class<?> wildcardClass7 = myClass1_1.getClass();
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) wildcardClass7);
        MyClass1 myClass1_9 = new MyClass1((java.lang.Object) myClass1_8);
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0" + "'", str3, "0");
        org.junit.Assert.assertNotNull(wildcardClass7);
    }

    @Test
    public void test85() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test85");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0);
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_5);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) true);
        java.lang.Object obj9 = null;
        MyClass1 myClass1_10 = new MyClass1(obj9);
        java.lang.Object obj11 = new java.lang.Object();
        MyClass1 myClass1_12 = new MyClass1(obj11);
        java.lang.String str14 = (java.lang.String) privateMethod1_Object.invoke(myClass1_12, (java.lang.Object) (short) 100);
        java.lang.String str15 = (java.lang.String) privateMethod1_Object.invoke(myClass1_10, (java.lang.Object) str14);
        MyClass1 myClass1_16 = new MyClass1((java.lang.Object) str15);
        java.lang.Class<?> wildcardClass17 = myClass1_16.getClass();
        java.lang.String str18 = (java.lang.String) privateMethod1_Object.invoke(myClass1_8, (java.lang.Object) wildcardClass17);
        java.lang.String str19 = (java.lang.String) privateMethod1_Object.invoke(myClass1_5, (java.lang.Object) wildcardClass17);
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0" + "'", str3, "0");
        org.junit.Assert.assertEquals("'" + str14 + "' != '" + "100" + "'", str14, "100");
        org.junit.Assert.assertEquals("'" + str15 + "' != '" + "100" + "'", str15, "100");
        org.junit.Assert.assertNotNull(wildcardClass17);
        org.junit.Assert.assertEquals("'" + str18 + "' != '" + "class MyClass1" + "'", str18, "class MyClass1");
        org.junit.Assert.assertEquals("'" + str19 + "' != '" + "class MyClass1" + "'", str19, "class MyClass1");
    }

    @Test
    public void test86() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test86");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 100.0d);
        MyClass2 myClass2_3 = new MyClass2((java.lang.Integer) 1);
        java.lang.String str4 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass2_3);
    }

    @Test
    public void test87() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test87");
        java.lang.Object obj0 = new java.lang.Object();
        MyClass1 myClass1_1 = new MyClass1(obj0);
        MyClass1 myClass1_2 = new MyClass1(obj0);
        MyClass1 myClass1_3 = new MyClass1((java.lang.Object) myClass1_2);
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) 0);
        java.lang.String str7 = (java.lang.String) privateMethod1_Object.invoke(myClass1_5, (java.lang.Object) 0);
        java.lang.String str9 = (java.lang.String) privateMethod1_Object.invoke(myClass1_5, (java.lang.Object) 100L);
        MyClass1 myClass1_10 = new MyClass1((java.lang.Object) myClass1_5);
        java.lang.String str11 = (java.lang.String) privateMethod1_Object.invoke(myClass1_3, (java.lang.Object) myClass1_10);
        java.lang.Class<?> wildcardClass12 = myClass1_10.getClass();
        org.junit.Assert.assertEquals("'" + str7 + "' != '" + "0" + "'", str7, "0");
        org.junit.Assert.assertEquals("'" + str9 + "' != '" + "100" + "'", str9, "100");
        org.junit.Assert.assertNotNull(wildcardClass12);
    }

    @Test
    public void test88() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test88");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 0);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) 0);
    }

    @Test
    public void test89() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test89");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) (-128));
        java.lang.Class<?> wildcardClass2 = myClass2_1.getClass();
        org.junit.Assert.assertNotNull(wildcardClass2);
    }

    @Test
    public void test90() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test90");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) false);
        MyClass2 myClass2_3 = new MyClass2((java.lang.Integer) 10);
        java.lang.String str4 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 10);
        org.junit.Assert.assertEquals("'" + str4 + "' != '" + "10" + "'", str4, "10");
    }

    @Test
    public void test91() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test91");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 4);
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) 4);
    }

    @Test
    public void test92() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test92");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) "hi!");
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) "hi!");
        java.lang.String str4 = (java.lang.String) privateMethod1_Object.invoke(myClass1_2, (java.lang.Object) (-1.0d));
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) str4);
        org.junit.Assert.assertEquals("'" + str4 + "' != '" + "-1.0" + "'", str4, "-1.0");
    }

    @Test
    public void test93() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test93");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) (short) 0);
        java.lang.Class<?> wildcardClass2 = myClass1_1.getClass();
        org.junit.Assert.assertNotNull(wildcardClass2);
    }

    @Test
    public void test94() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test94");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0);
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_5);
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) 0);
        java.lang.String str10 = (java.lang.String) privateMethod1_Object.invoke(myClass1_8, (java.lang.Object) 0);
        MyClass1 myClass1_12 = new MyClass1((java.lang.Object) 0);
        java.lang.String str13 = (java.lang.String) privateMethod1_Object.invoke(myClass1_8, (java.lang.Object) myClass1_12);
        java.lang.Class<?> wildcardClass14 = myClass1_8.getClass();
        java.lang.String str15 = (java.lang.String) privateMethod1_Object.invoke(myClass1_5, (java.lang.Object) myClass1_8);
        java.lang.Class<?> wildcardClass16 = myClass1_8.getClass();
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0" + "'", str3, "0");
        org.junit.Assert.assertEquals("'" + str10 + "' != '" + "0" + "'", str10, "0");
        org.junit.Assert.assertNotNull(wildcardClass14);
        org.junit.Assert.assertNotNull(wildcardClass16);
    }

    @Test
    public void test95() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test95");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 40);
        java.lang.Class<?> wildcardClass2 = myClass2_1.getClass();
        org.junit.Assert.assertNotNull(wildcardClass2);
    }

    @Test
    public void test96() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test96");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) (-1));
        MyClass1 myClass1_2 = new MyClass1((java.lang.Object) (-1));
        MyClass1 myClass1_3 = new MyClass1((java.lang.Object) myClass1_2);
        java.lang.Class<?> wildcardClass4 = myClass1_2.getClass();
        org.junit.Assert.assertNotNull(wildcardClass4);
    }

    @Test
    public void test97() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test97");
        MyClass2 myClass2_1 = new MyClass2((java.lang.Integer) 32);
    }

    @Test
    public void test98() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test98");
        java.lang.Object obj0 = new java.lang.Object();
        MyClass1 myClass1_1 = new MyClass1(obj0);
        MyClass1 myClass1_2 = new MyClass1(obj0);
        MyClass1 myClass1_3 = new MyClass1((java.lang.Object) myClass1_2);
        java.lang.Class<?> wildcardClass4 = myClass1_2.getClass();
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) myClass1_2);
        org.junit.Assert.assertNotNull(wildcardClass4);
    }

    @Test
    public void test99() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest0.test99");
        MyClass1 myClass1_1 = new MyClass1((java.lang.Object) 0);
        java.lang.String str3 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) 0);
        MyClass1 myClass1_5 = new MyClass1((java.lang.Object) 0);
        java.lang.String str6 = (java.lang.String) privateMethod1_Object.invoke(myClass1_1, (java.lang.Object) myClass1_5);
        java.lang.Class<?> wildcardClass7 = myClass1_1.getClass();
        MyClass1 myClass1_8 = new MyClass1((java.lang.Object) wildcardClass7);
        MyClass1 myClass1_9 = new MyClass1((java.lang.Object) wildcardClass7);
        MyClass1 myClass1_10 = new MyClass1((java.lang.Object) myClass1_9);
        org.junit.Assert.assertEquals("'" + str3 + "' != '" + "0" + "'", str3, "0");
        org.junit.Assert.assertNotNull(wildcardClass7);
    }
}

