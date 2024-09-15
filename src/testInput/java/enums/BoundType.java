package enums;

/*
 * Modified from Guava 16.0.1 BoundType.java
 */

/**
 * Indicates whether an endpoint of some range is contained in the range itself ("closed") or not
 * ("open"). If a range is unbounded on a side, it is neither open nor closed on that side; the
 * bound simply does not exist.
 */
public enum BoundType {
    /**
     * The endpoint value <i>is not</i> considered part of the set ("exclusive").
     */
    OPEN {
        @Override
        BoundType flip() {
            return CLOSED;
        }
    },
    /**
     * The endpoint value <i>is</i> considered part of the set ("inclusive").
     */
    CLOSED {
        @Override
        BoundType flip() {
            return OPEN;
        }
    };

    /**
     * Returns the bound type corresponding to a boolean value for inclusivity.
     */
    static BoundType forBoolean(boolean inclusive) {
        return inclusive ? CLOSED : OPEN;
    }

    /**
     * Returns an array of all the enum values.
     */
    public static BoundType[] getValues() {
        return values();
    }

    abstract BoundType flip();
}
