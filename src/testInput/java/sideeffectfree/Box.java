package sideeffectfree;


public class Box {
    private int a, b;
    
    private Box() {
        a = 2;
        b = 3;
    }
    
    // Marked as side-effect-free.
    public int getA() {
        return a;
    }

    public static Box getNewBox() {
        return new Box();
    }
    
    // Marked as side-effect-free.
    public int getSum(Box other) {
        return other.a + other.b + a + b;
    }
}
