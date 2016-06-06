package randoop.contract;

import randoop.Globals;

/**
 * The contract:
 * (x0.compareTo(x1) == 0) &rarr; ((x0.compareTo(x2) >= 0) == (x1.compareTo(x2) >= 0))
 */
public class CompareToSubs implements ObjectContract {
    private static final CompareToSubs instance = new CompareToSubs();

    private CompareToSubs() {};

    public static CompareToSubs getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean evaluate(Object... objects) {
        Object o1 = objects[0];
        Object o2 = objects[1];
        Object o3 = objects[2];

        // If o1 and o2 are comparable objects, check the implication
        if (o1 instanceof Comparable && o2 instanceof Comparable) {
            Comparable compObj1 = (Comparable)o1;
            Comparable compObj2 = (Comparable)o2;

            if (compObj1.compareTo(o2) == 0) {
                return ((compObj1.compareTo(o3) >= 0) && (compObj2.compareTo(o3) >= 0))
                        || ((compObj1.compareTo(o3) < 0) && (compObj2.compareTo(o3) < 0));
            }
            return true;
        }
        // If the compare to operation can't be done, the statement is trivially true
        return true;
    }

    @Override
    public int getArity() {
        return 3;
    }

    @Override
    public String toCommentString() {
        return "compare-to-substitutability on x0, x1, and x2";
    }

    @Override
    public String get_observer_str() {
        return "CompareToSubstitutability";
    }

    @Override
    public boolean evalExceptionMeansFailure() {
        return true;
    }

    @Override
    public String toCodeString() {
        StringBuilder b = new StringBuilder();
        b.append(Globals.lineSep);
        b.append("// Checks the contract: ");
        b.append(" " + toCommentString() + Globals.lineSep);
        b.append("org.junit.Assert.assertTrue(");
        b.append("\"Contract failed: " + toCommentString() + "\", ");
        b.append("!(x0.compareTo(x1)==0) || ((x0.compareTo(x2) >= 0) == (x1.compareTo(x2) >= 0))");
        b.append(");");
        return b.toString();
    }
}