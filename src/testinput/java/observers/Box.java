package observers;


public class Box {
    private int a, b;
    
    public Box() {
        a = 2;
        b = 3;
    }
    
    // Marked as observer.
    public int getA() {
        return a;
    }
    
    // Marked as observer.
    public int getB() {
        return b;
    }
}
