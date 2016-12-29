package randoop.test.perimeter;
/**
 * A class representing a node in the quad tree.
 */
public abstract class QuadTreeNode {
  /**
   * Variable used to determine the x-axis size of the image
   */
  static int gcmp = 4194304;
  /**
   * Variable used to determine the y-axis size of the image
   */
  static int lcmp = 1048576;

  /**
   * The quadrant that this node represents (i.e., northwest, northeast,
   * southwest, or southeast).
   */
  protected Quadrant quadrant;
  /**
   * Node that represents the northwest quadrant of the image
   */
  protected QuadTreeNode nw;
  /**
   * Node that represents the northeast quadrant of the image
   */
  protected QuadTreeNode ne;
  /**
   * Node that represents the southwest quadrant of the image
   */
  protected QuadTreeNode sw;
  /**
   * Node that represents the southeast quadrant of the image
   */
  protected QuadTreeNode se;
  /**
   * Node that represents the parent quadrant of the image
   */
  protected QuadTreeNode parent;

  // enumeration for direction
  public final static int NORTH = 0;
  public final static int EAST = 1;
  public final static int SOUTH = 2;
  public final static int WEST = 3;

  /**
   * Create a leaf node in the Quad Tree.
   *
   * @param quad childType if there's a parent, the type of child this node represents
   * @param parent the parent quad tree node
   */
  public QuadTreeNode(Quadrant quad, QuadTreeNode parent) {
    this(quad, null, null, null, null, parent);
  }

  /**
   * Create a node in the quad tree.
   *
   * @param quad childType if there's a parent, the type of child
   * @param nw the node represent the northwest quadrant
   * @param ne the node represent the northeast quadrant
   * @param sw the node represent the southwest quadrant
   * @param se the node represent the southeast quadrant
   */
  private QuadTreeNode(
      Quadrant quad,
      QuadTreeNode nw,
      QuadTreeNode ne,
      QuadTreeNode sw,
      QuadTreeNode se,
      QuadTreeNode parent) {
    this.quadrant = quad;
    this.nw = nw;
    this.ne = ne;
    this.sw = sw;
    this.se = se;
    this.parent = parent;
  }

  /**
   * Set the children of the quad tree node.
   *
   * @param nw the node represent the northwest quadrant
   * @param ne the node represent the northeast quadrant
   * @param sw the node represent the southwest quadrant
   * @param se the node represent the southeast quadrant
   */
  protected void setChildren(QuadTreeNode nw, QuadTreeNode ne, QuadTreeNode sw, QuadTreeNode se) {
    this.nw = nw;
    this.ne = ne;
    this.sw = sw;
    this.se = se;
  }

  /**
   * Return the node representing the north west quadrant.
   * @return the node representing the north west quadrant
   */
  public final QuadTreeNode getNorthWest() {
    return nw;
  }
  /**
   * Return the node representing the north east quadrant.
   * @return the node representing the north east quadrant
   */
  public final QuadTreeNode getNorthEast() {
    return ne;
  }
  /**
   * Return the node representing the south west quadrant.
   * @return the node representing the south west quadrant
   */
  public final QuadTreeNode getSouthWest() {
    return sw;
  }
  /**
   * Return the node representing the south east quadrant.
   * @return the node representing the south east quadrant
   */
  public final QuadTreeNode getSouthEast() {
    return se;
  }

  /**
   * Create an image which is represented using a QuadTreeNode.
   * @param size size of image
   * @param center_x x coordinate of center
   * @param center_y y coordinate of center
   * @param parent parent quad tree node
   * @param quadrant the quadrant that the sub tree is in
   * @param level the level of the tree
   */
  public static QuadTreeNode createTree(
      int size, int center_x, int center_y, QuadTreeNode parent, Quadrant quadrant, int level) {
    QuadTreeNode node;

    int intersect = checkIntersect(center_x, center_y, size);
    size = size / 2;
    if (intersect == 0 && size < 512) {
      node = new WhiteNode(quadrant, parent);
    } else if (intersect == 2) {
      node = new BlackNode(quadrant, parent);
    } else {
      if (level == 0) {
        node = new BlackNode(quadrant, parent);
      } else {
        node = new GreyNode(quadrant, parent);
        QuadTreeNode sw =
            createTree(
                size, center_x - size, center_y - size, node, Quadrant.cSouthWest, level - 1);
        QuadTreeNode se =
            createTree(
                size, center_x + size, center_y - size, node, Quadrant.cSouthEast, level - 1);
        QuadTreeNode ne =
            createTree(
                size, center_x + size, center_y + size, node, Quadrant.cNorthEast, level - 1);
        QuadTreeNode nw =
            createTree(
                size, center_x - size, center_y + size, node, Quadrant.cNorthWest, level - 1);
        node.setChildren(nw, ne, sw, se);
      }
    }
    return node;
  }

  /**
   * Compute the total perimeter of a binary image that is represented
   * as a quadtree using Samet's algorithm.
   *
   * @param size the size of the image that this node represents (size X size)
   * @return the size of the perimeter of the image
   */
  abstract public int perimeter(int size);

  /**
   * Sum the perimeter of all white leaves in the two specified
   * quadrants of the sub quad tree rooted at this node.
   *
   * @param quad1 the first specified quadrant
   * @param quad2 the second specified quadrant
   * @param size the size of the image represented by this node
   * @return the perimeter of the adjacent nodes
   */
  abstract public int sumAdjacent(Quadrant quad1, Quadrant quad2, int size);

  /**
   * Return the neighbor of this node in the given direction which is
   * greater than or equal in size to this node.  If the node doesn't
   * exist, then a grey node of equal size is returned.  Otherwise,
   * the node is adjacent to the border of the image and null is
   * returned.
   *
   * @param dir the direction of the neighbor
   * @return the appropriate neighbor based upon the direction, or null
   */
  public QuadTreeNode gtEqualAdjNeighbor(int dir) {
    QuadTreeNode q;
    if (parent != null && quadrant.adjacent(dir)) {
      q = parent.gtEqualAdjNeighbor(dir);
    } else {
      q = parent;
    }

    if (q != null && q instanceof GreyNode) {
      return quadrant.reflect(dir).child(q);
    } else {
      return q;
    }
  }

  /**
   * Count the number of leaves in the quad tree.
   * @return the number of leaves in the quad tree
   */
  public int countTree() {
    if (nw == null && ne == null && sw == null && se == null) {
      return 1;
    } else {
      return sw.countTree() + se.countTree() + ne.countTree() + nw.countTree();
    }
  }

  private static int checkOutside(int x, int y) {
    int euclid = x * x + y * y;
    if (euclid > gcmp) return 1;
    if (euclid < lcmp) return -1;
    return 0;
  }

  private static int checkIntersect(int center_x, int center_y, int size) {
    if (checkOutside(center_x + size, center_y + size) == 0
        && checkOutside(center_x + size, center_y - size) == 0
        && checkOutside(center_x - size, center_y - size) == 0
        && checkOutside(center_x - size, center_y + size) == 0) return 2;

    int sum =
        checkOutside(center_x + size, center_y + size)
            + checkOutside(center_x + size, center_y - size)
            + checkOutside(center_x - size, center_y - size)
            + checkOutside(center_x - size, center_y + size);

    if ((sum == 4) || (sum == -4)) {
      return 0;
    }

    return 1;
  }

  @Override
  public String toString() {
    return getClass().getName() + " " + quadrant.getClass().getName();
  }
}
