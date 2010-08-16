package demo.pathplanning.model;

import java.util.Random;


public class CartesianGraph {
  int fXSize;
  int fYSize;
  private Node[][] fGrid;

  public CartesianGraph(int xSize, int ySize) {
    fXSize = xSize;
    fYSize = ySize;

    fGrid = new Node[fXSize][fYSize];

    Random rand = new Random(System.nanoTime());
    for (int i = 0; i < fXSize; i++) {
      for (int j = 0; j < fYSize; j++) {
        fGrid[i][j] = new Node(this, new Location(i, j), i + rand.nextInt(j + 1));
      }
    }
  }

  public Node getNode(Location loc, Direction dir) {
    try {
      return getNode(loc.append(dir));
    } catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }

  public Node getNode(Location loc) {
    try {
      return fGrid[loc.getX()][loc.getY()];
    } catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }

  public int getXSize() {
    return fXSize;
  }

  public int getYSize() {
    return fYSize;
  }
  
  @Override
  public String toString() {
    String graph = ""; //$NON-NLS-1$
    for (int i = 0; i < getXSize(); i++) {
      String line = ""; //$NON-NLS-1$
      for (int j = 0; j < getYSize(); j++) {
        Location loc = new Location(i, j);
        Node n = getNode(loc);
        
        line += n.getCost() + ","; //$NON-NLS-1$
      }
      graph += line + "\n"; //$NON-NLS-1$
    }
    return graph;
  }
}
