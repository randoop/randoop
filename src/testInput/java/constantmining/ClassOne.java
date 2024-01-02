package constantmining;

import constantmining.test.ClassTwo;

public class ClassOne {
    // int0 = 2 : 3
    // int0 = 3 : 1
    // long0 = 2l : 1
    // long0 = 1l : 3
    // string0 = "b" : 1
    // string0 = "c" : 2 ??

    public int a = 2;
    public String b = "b";
    public long c = 10000l;
    public long d = 10000l;
    public Class<ClassTwo> e = ClassTwo.class;

    public ClassOne() {
        a = 2;
//        InnerClass ic = new InnerClass();
//        ic.setA(2);
        c = 2l;
        d = 2l;
        e = ClassTwo.class;
    }

    public void setA(int a) {
        this.a = a;
    }

    public void doSomething1() {
        a = 2;
        b = "c";
        System.out.println("c");
        String d = "c";
        System.out.println(d);
    }

    public void doSomething2() {
        a = 3;
        b = "c";
    }

}
