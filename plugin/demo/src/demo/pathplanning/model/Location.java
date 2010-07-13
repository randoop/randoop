package demo.pathplanning.model;


public class Location {
  private int fX;
  private int fY;
  
  public Location(int x, int y) {
    fX = x;
    fY = y;
  }
  
  public int getX() {
    return fX;
  }
  
  public int getY() {
    return fY;
  }
  
  Location append(Direction dir) {
    return new Location(fX + dir.getDx(), fY + dir.getDy());
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Location))
      return false;
    Location otherLoc = (Location) obj;

    return getX() == otherLoc.getX() && getY() == otherLoc.getY();
  }
  
  @Override
  public int hashCode() {
    int xHash = (int) (getX() * Math.PI * 10000000);
    int yHash = (int) (getY() * Math.E * 10000000);

    return xHash % 10000 + yHash % 10000;
  }
  
  @Override
  public String toString() {
    return "(" + getX() + "," + getY() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}
