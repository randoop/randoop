package randoop.test.bh;
/**
 * A class used to represent internal nodes in the tree
 */
public final class Cell extends Node {
  // subcells per cell
  public final static int NSUB = 8; // 1 << NDIM

  /**
   * The children of this cell node.  Each entry may contain either
   * another cell or a body.
   */
  Node[] subp;
  Cell next;

  public Cell() {
    subp = new Node[NSUB];
    next = null;
  }

  /**
   * Descend Tree and insert particle.  We're at a cell so
   * we need to move down the tree.
   * @param p the body to insert into the tree
   * @param xpic
   * @param l
   * @param tree the root of the tree
   * @return the subtree with the new body inserted
   */
  @Override
  public final Node loadTree(Body p, MathVector xpic, int l, Tree tree) {
    // move down one level
    int si = oldSubindex(xpic, l);
    Node rt = subp[si];
    if (rt != null) subp[si] = rt.loadTree(p, xpic, l >> 1, tree);
    else subp[si] = p;
    return this;
  }

  /**
   * Descend tree finding center of mass coordinates
   * @return the mass of this node
   */
  @Override
  public final double hackcofm() {
    double mq = 0.0;
    MathVector tmpPos = new MathVector();
    MathVector tmpv = new MathVector();
    for (int i = 0; i < NSUB; i++) {
      Node r = subp[i];
      if (r != null) {
        double mr = r.hackcofm();
        mq = mr + mq;
        tmpv.multScalar(r.pos, mr);
        tmpPos.addition(tmpv);
      }
    }
    mass = mq;
    pos = tmpPos;
    pos.divScalar(mass);

    return mq;
  }

  /**
   * Recursively walk the tree to do hackwalk calculation
   */
  @Override
  public final HG walkSubTree(double dsq, HG hg) {
    if (subdivp(dsq, hg)) {
      for (int k = 0; k < Cell.NSUB; k++) {
        Node r = subp[k];
        if (r != null) hg = r.walkSubTree(dsq / 4.0, hg);
      }
    } else {
      hg = gravSub(hg);
    }
    return hg;
  }

  /**
   * Decide if the cell is too close to accept as a single term.
   * @return true if the cell is too close
   */
  public final boolean subdivp(double dsq, HG hg) {
    MathVector dr = new MathVector();
    dr.subtraction(pos, hg.pos0);
    double drsq = dr.dotProduct();

    // in the original olden version drsp is multiplied by 1.0
    return (drsq < dsq);
  }

  /**
   * Return a string represenation of a cell.
   * @return a string represenation of a cell
   */
  @Override
  public String toString() {
    return "Cell " + super.toString();
  }
}
