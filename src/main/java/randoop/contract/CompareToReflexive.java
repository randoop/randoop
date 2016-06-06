package randoop.contract;

import randoop.Globals;

/**
 * The contract: Comparing an object with itself should result in 0
 * <code>x0.compareTo(x0) == 0</code>.
 */
public class CompareToReflexive implements ObjectContract {
    private static final CompareToReflexive instance = new CompareToReflexive();

    private CompareToReflexive() {};

    public static CompareToReflexive getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean evaluate(Object... objects) {
        assert objects != null && objects.length == 1;
        // Get first and only object
        Object o1 = objects[0];
        assert o1 != null;

        if (o1 instanceof Comparable) {
            Comparable compObj1 = (Comparable) o1;
            return (compObj1.compareTo(o1) == 0);
        }
        return true;
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public String toCommentString() {
        return "compare-to-reflexive on x0";
    }

    @Override
    public String get_observer_str() {
        return "CompareToReflexive";
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
        b.append("x0.compareTo(x0) == 0");
        b.append(");");
        return b.toString();
    }
}

