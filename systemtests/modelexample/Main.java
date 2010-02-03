package modelexample;

public class Main {

    public static void main(String[] args) {

    A a1 = new A(1);
    B b1 = new B();
    a1.m1();
    a1.m1();
    b1.m2(a1);

    A a2 = new A(3);
    B b2 = new B();
    a1.m1();
    a1.m1();
    b2.m2(a1);

    }
}
