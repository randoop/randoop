package randoop.test.perimeter;
/**
 * A class to represent a black quad tree node in the image.
 * A black node represents a block of an image that contains only 1's.
 */
public class BlackNode extends QuadTreeNode {

  /**
   * Construct a <tt>black</tt> quad tree node.
   * @param quadrant the quadrant that this node represents
   * @param parent quad tree node
   */
  public BlackNode(Quadrant quadrant, QuadTreeNode parent) {
    super(quadrant, parent);
  }

  /**
   * Compute the perimeter for a black node.
   * @param size
   */
  @Override
  public int perimeter(int size) {
    int retval = 0;
    // North
    QuadTreeNode neighbor = gtEqualAdjNeighbor(NORTH);
    if (neighbor == null || neighbor instanceof WhiteNode) retval += size;
    else if (neighbor instanceof GreyNode) {
      retval += neighbor.sumAdjacent(Quadrant.cSouthEast, Quadrant.cSouthWest, size);
    }

    // East
    neighbor = gtEqualAdjNeighbor(EAST);
    if (neighbor == null || neighbor instanceof WhiteNode) retval += size;
    else if (neighbor instanceof GreyNode) {
      retval += neighbor.sumAdjacent(Quadrant.cSouthWest, Quadrant.cNorthWest, size);
    }

    // South
    neighbor = gtEqualAdjNeighbor(SOUTH);
    if (neighbor == null || neighbor instanceof WhiteNode) retval += size;
    else if (neighbor instanceof GreyNode) {
      retval += neighbor.sumAdjacent(Quadrant.cNorthWest, Quadrant.cNorthEast, size);
    }

    // West
    neighbor = gtEqualAdjNeighbor(WEST);
    if (neighbor == null || neighbor instanceof WhiteNode) retval += size;
    else if (neighbor instanceof GreyNode) {
      retval += neighbor.sumAdjacent(Quadrant.cNorthEast, Quadrant.cSouthEast, size);
    }

    return retval;
  }

  /**
   * Sum the perimeter of all white leaves in the two specified
   * quadrants of the sub quad tree rooted at this node.
   *
   * @param quad1 the first specified quadrant
   * @param quad2 the second specified quadrant
   * @param size the size of the image represented by this node
   * @return the perimeter of the adjacent nodes
   */
  @Override
  public int sumAdjacent(Quadrant quad1, Quadrant quad2, int size) {
    return 0;
  }
}
