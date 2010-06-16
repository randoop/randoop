package demo.pathplanning.model;

public enum Direction {
  UP(0, -1),
  DOWN(0, 1),
  LEFT(-1, 0),
  RIGHT(1, 0);

  private int fDx;
  private int fDy;

  Direction(int dx, int dy) {
    fDx = dx;
    fDy = dy;
  }

  public int getDx() {
    return fDx;
  }

  public int getDy() {
    return fDy;
  }
}
