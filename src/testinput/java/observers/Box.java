package observers;


public class Box {
    private int a, b;
    
    private Box() {
        a = 2;
        b = 3;
    }
    
    // Marked as observer.
    public int getA() {
        return a;
    }

    public static Box getNewBox() {
        return new Box();
    }
    
    // Marked as observer.
    public int getSum(Box other) {
        return other.a + other.b + a + b;
    }
}
