package misc.puritypaper;

public class Line {
  private Point a, b;
  static Line newestLine;

  public Line(Point a, Point b) {
    this.a = a;
    this.b = b;
    newestLine = this;
  }

  void replaceIfCloser(Point other) {
    if (other.distanceTo(a) > b.distanceTo(a)) other.copyTo(this.b);
  }

  void replaceWithOrigin() {
    replaceIfCloser(new Point(0, 0));
  }

  void swapCoordinates() {
    getThis().a.swapMyCoordinates();
    getThis().b.swapMyCoordinates();
  }

  private Line getThis() {
    return this;
  }

  void checkAndSwapCoordinates() {
    if (a.distanceToOrigin() > b.distanceToOrigin()) swapCoordinates();
  }
}
