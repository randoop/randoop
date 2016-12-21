package randoop.test.perimeter;
/**
 * A class to represent a white quadtree node in the image.
 * A white node represents a block of an image that contains
 * only 0's.
 */
public class WhiteNode extends QuadTreeNode {

  /**
   * Construct a <tt>white</tt> image node.
   * @param quadrant the quadrant that this node represents
   * @param parent the parent node in the quad tree
   */
  public WhiteNode(Quadrant quadrant, QuadTreeNode parent) {
    super(quadrant, parent);
  }

  /**
   * Compute the total perimeter of a white node in a binary image
   * that is represented as a quadtree using Samet's algorithm.
   *
   * @param size the size of the image that this node represents (size X size)
   * @return the size of the perimeter of the image
   */
  @Override
  public int perimeter(int size) {
    return 0;
  }

  /**
   * Sum the perimeter of all white leaves in the two specified
   * quadrants of the sub quad tree rooted at this node.
   *
   * @param quad1 the first specified quadrant
   * @param quad2 the second specified quadrant
   * @param size the size of the image represented by this node
   * @return the image size that the white node represents
   */
  @Override
  public int sumAdjacent(Quadrant quad1, Quadrant quad2, int size) {
    return size;
  }
}
