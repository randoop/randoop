package modelexample;

public class A {

    public A(int i) {
        throw new RuntimeException("bomb!");
    }

    public void m1() {

    }
    
    public int obs1() {
        return 1;
    }

}
