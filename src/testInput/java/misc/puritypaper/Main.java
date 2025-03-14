package misc.puritypaper;

public class Main {
  public static void main(String[] args) {
    Point p = new Point(2, 3);
    Point q = new Point(5, 6);
    Line l = new Line(p, q);
    l.swapCoordinates();
  }
}
