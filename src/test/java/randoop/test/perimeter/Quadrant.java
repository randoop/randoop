package randoop.test.perimeter;
/**
 * An abstract class that represents a quadrant in the image.
 * The quadrants specify the NW, NE, SW, and SE parts of the
 * image.
 */
public interface Quadrant {
  final static Quadrant cNorthWest = new NorthWest();
  final static Quadrant cNorthEast = new NorthEast();
  final static Quadrant cSouthWest = new SouthWest();
  final static Quadrant cSouthEast = new SouthEast();

  /**
   * Return true iff this quadrant is adjacent to the boundary
   * of an image in the given direction.
   * @param direction the image boundary
   * @return true if the quadrant is adjacent, false otherwise
   */
  public boolean adjacent(int direction);
  /**
   * Return the quadrant of a block of equal size that is
   * adjacent to the given side of this quadrant.
   * @param direction the image boundary
   * @return the reflected quadrant
   */
  public Quadrant reflect(int direction);
  /**
   * Return the child that represents this quadrant of the given
   * node.
   * @param node the node that we want the child from
   * @return the child node representing this quadrant
   */
  public QuadTreeNode child(QuadTreeNode node);
}
