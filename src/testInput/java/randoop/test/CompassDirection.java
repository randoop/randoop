package randoop.test;

/**
 * CompassDirection enum based on examples from Java Language Specification intended to be used with
 * tests in Randoop.
 */
public enum CompassDirection {
  NORTH {
    @Override
    public CompassDirection rotateRight() {
      return EAST;
    }
  },
  EAST {
    @Override
    public CompassDirection rotateRight() {
      return SOUTH;
    }
  },
  SOUTH {
    @Override
    public CompassDirection rotateRight() {
      return WEST;
    }
  },
  WEST {
    @Override
    public CompassDirection rotateRight() {
      return NORTH;
    }
  };

  public abstract CompassDirection rotateRight();
}
