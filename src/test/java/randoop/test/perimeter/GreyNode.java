package randoop.test.perimeter;
/**
 * A class to represent a grey quad tree node in the image.
 * A grey node represents a block of an image that contains
 * both 0s and 1s.
 */
public class GreyNode extends QuadTreeNode {
  /**
   * Construct a <tt>grey</tt> image node.
   * @param quadrant the quadrant that this node represents
   * @param parent the parent node in the quad tree
   */
  public GreyNode(Quadrant quadrant, QuadTreeNode parent) {
    super(quadrant, parent);
  }

  /**
   * Compute the perimeter for a grey node using Samet's algorithm.
   *
   * @param size the size of the image that this node represents
   * @return the perimeter of the image represented by this node
   */
  @Override
  public int perimeter(int size) {
    size = size / 2;
    int retval = sw.perimeter(size);
    retval += se.perimeter(size);
    retval += ne.perimeter(size);
    retval += nw.perimeter(size);
    return retval;
  }

  /**
   * Sum the perimeter of all white leaves in the two specified
   * quadrants of the sub quad tree rooted at this node.  Since
   * this is a grey node, we just recursively call this routine
   * on the appropriate children (that may be white nodes).
   *
   * @param quad1 the first specified quadrant
   * @param quad2 the second specified quadrant
   * @param size the size of the image represented by this node
   * @return the perimeter of the adjacent nodes
   */
  @Override
  public int sumAdjacent(Quadrant quad1, Quadrant quad2, int size) {
    QuadTreeNode child1 = quad1.child(this);
    QuadTreeNode child2 = quad2.child(this);
    size = size / 2;
    return child1.sumAdjacent(quad1, quad2, size) + child2.sumAdjacent(quad1, quad2, size);
  }
}
