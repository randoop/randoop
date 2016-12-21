package randoop.test.bh;

import java.util.Enumeration;

/**
 * A class that represents the root of the data structure used
 * to represent the N-bodies in the Barnes-Hut algorithm.
 */
public class Tree {
  MathVector rmin;
  double rsize;
  /**
   * A reference to the root node.
   */
  Node root;
  /**
   * The complete list of bodies that have been created.
   */
  private Body bodyTab;
  /**
   * The complete list of bodies that have been created - in reverse.
   */
  private Body bodyTabRev;

  /**
   * Construct the root of the data structure that represents the N-bodies.
   */
  public Tree() {
    rmin = new MathVector();
    rsize = -2.0 * -2.0;
    root = null;
    bodyTab = null;
    bodyTabRev = null;

    rmin.value(0, -2.0);
    rmin.value(1, -2.0);
    rmin.value(2, -2.0);
  }

  /**
   * Return an enumeration of the bodies.
   * @return an enumeration of the bodies
   */
  public final Enumeration<Body> bodies() {
    return bodyTab.elements();
  }

  /**
   * Return an enumeration of the bodies, in reverse.
   * @return an enumeration of the bodies, in reverse
   */
  public final Enumeration<Body> bodiesRev() {
    return bodyTabRev.elementsRev();
  }

  /**
   * Create the testdata used in the benchmark.
   * @param nbody the number of bodies to create
   */
  public final void createTestData(int nbody) {
    MathVector cmr = new MathVector();
    MathVector cmv = new MathVector();

    Body head = new Body();
    Body prev = head;

    double rsc = 3.0 * Math.PI / 16.0;
    double vsc = Math.sqrt(1.0 / rsc);
    double seed = 123.0;

    for (int i = 0; i < nbody; i++) {
      Body p = new Body();

      prev.setNext(p);
      prev = p;
      p.mass = 1.0 / nbody;

      seed = BH.myRand(seed);
      double t1 = BH.xRand(0.0, 0.999, seed);
      t1 = Math.pow(t1, (-2.0 / 3.0)) - 1.0;
      double r = 1.0 / Math.sqrt(t1);

      double coeff = 4.0;
      for (int k = 0; k < MathVector.NDIM; k++) {
        seed = BH.myRand(seed);
        r = BH.xRand(0.0, 0.999, seed);
        p.pos.value(k, coeff * r);
      }

      cmr.addition(p.pos);

      double x, y;
      do {
        seed = BH.myRand(seed);
        x = BH.xRand(0.0, 1.0, seed);
        seed = BH.myRand(seed);
        y = BH.xRand(0.0, 0.1, seed);
      } while (y > x * x * Math.pow(1.0 - x * x, 3.5));

      double v = Math.sqrt(2.0) * x / Math.pow(1 + r * r, 0.25);

      double rad = vsc * v;
      double rsq;
      do {
        for (int k = 0; k < MathVector.NDIM; k++) {
          seed = BH.myRand(seed);
          p.vel.value(k, BH.xRand(-1.0, 1.0, seed));
        }
        rsq = p.vel.dotProduct();
      } while (rsq > 1.0);
      double rsc1 = rad / Math.sqrt(rsq);
      p.vel.multScalar(rsc1);
      cmv.addition(p.vel);
    }

    // mark end of list
    prev.setNext(null);
    // toss the dummy node at the beginning and set a reference to the first element
    bodyTab = head.getNext();

    cmr.divScalar(nbody);
    cmv.divScalar(nbody);

    prev = null;

    for (Enumeration<Body> e = bodyTab.elements(); e.hasMoreElements(); ) {
      Body b = e.nextElement();
      b.pos.subtraction(cmr);
      b.vel.subtraction(cmv);
      b.setProcNext(prev);
      prev = b;
    }

    // set the reference to the last element
    bodyTabRev = prev;
  }

  /**
   * Advance the N-body system one time-step.
   * @param nstep the current time step
   */
  public void stepSystem(int nstep) {
    // free the tree
    root = null;

    makeTree(nstep);

    // compute the gravity for all the particles
    for (Enumeration<Body> e = bodyTabRev.elementsRev(); e.hasMoreElements(); ) {
      Body b = e.nextElement();
      b.hackGravity(rsize, root);
    }

    vp(bodyTabRev, nstep);
  }

  /**
   * Initialize the tree structure for hack force calculation.
   * @param nstep the current time step
   */
  private void makeTree(int nstep) {
    for (Enumeration<Body> e = bodiesRev(); e.hasMoreElements(); ) {
      Body q = e.nextElement();
      if (q.mass != 0.0) {
        q.expandBox(this, nstep);
        MathVector xqic = intcoord(q.pos);
        if (root == null) {
          root = q;
        } else {
          root = root.loadTree(q, xqic, Node.IMAX >> 1, this);
        }
      }
    }
    root.hackcofm();
  }

  /**
   * Compute integerized coordinates.
   * @return the coordinates or null if rp is out of bounds
   */
  public final MathVector intcoord(MathVector vp) {
    MathVector xp = new MathVector();

    double xsc = (vp.value(0) - rmin.value(0)) / rsize;
    if (0.0 <= xsc && xsc < 1.0) {
      xp.value(0, Math.floor(Node.IMAX * xsc));
    } else {
      return null;
    }

    xsc = (vp.value(1) - rmin.value(1)) / rsize;
    if (0.0 <= xsc && xsc < 1.0) {
      xp.value(1, Math.floor(Node.IMAX * xsc));
    } else {
      return null;
    }

    xsc = (vp.value(2) - rmin.value(2)) / rsize;
    if (0.0 <= xsc && xsc < 1.0) {
      xp.value(2, Math.floor(Node.IMAX * xsc));
    } else {
      return null;
    }
    return xp;
  }

  static final private void vp(Body p, int nstep) {
    MathVector dacc = new MathVector();
    MathVector dvel = new MathVector();
    double dthf = 0.5 * BH.DTIME;

    for (Enumeration<Body> e = p.elementsRev(); e.hasMoreElements(); ) {
      Body b = e.nextElement();
      MathVector acc1 = (MathVector) b.newAcc.clone();
      if (nstep > 0) {
        dacc.subtraction(acc1, b.acc);
        dvel.multScalar(dacc, dthf);
        dvel.addition(b.vel);
        b.vel = (MathVector) dvel.clone();
      }
      b.acc = (MathVector) acc1.clone();
      dvel.multScalar(b.acc, dthf);

      MathVector vel1 = (MathVector) b.vel.clone();
      vel1.addition(dvel);
      MathVector dpos = (MathVector) vel1.clone();
      dpos.multScalar(BH.DTIME);
      dpos.addition(b.pos);
      b.pos = (MathVector) dpos.clone();
      vel1.addition(dvel);
      b.vel = (MathVector) vel1.clone();
    }
  }
}
