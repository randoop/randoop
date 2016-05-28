package randoop.contract;

import randoop.Globals;

/**
 * The contract:
 * <code>!(x0.equals(x1) &amp;&amp; x1.equals(x2))||x0.equals(x2)</code>.
 */
public class EqualsTransitive implements ObjectContract {
    private static final EqualsTransitive instance = new EqualsTransitive();

    private EqualsTransitive() {};

    public static EqualsTransitive getInstance() {
        return instance;
    }

    @Override
    public boolean evaluate(Object... objects) {
        Object o1 = objects[0];
        Object o2 = objects[1];
        Object o3 = objects[2];

        if (o1.equals(o2) && (o2.equals(o3))) {
            return o1.equals(o3);
        }
        return true;
    }

    @Override
    public int getArity() {
        return 3;
    }

    @Override
    public String toCommentString() {
        return "equals-transitive on x0, x1, and x2.";
    }

    @Override
    public String get_observer_str() {
        return "equals-transitive";
    }

    @Override
    public boolean evalExceptionMeansFailure() {
        return true;
    }

    @Override
    public String toCodeString() {
        StringBuilder b = new StringBuilder();
        b.append(Globals.lineSep);
        b.append("// This assertion (transitivity of equals) fails ");
        b.append(Globals.lineSep);
        b.append("org.junit.Assert.assertTrue(");
        b.append("\"Contract failed: " + toCommentString() + "\", ");
        b.append("!(x0.equals(x1) && x1.equals(x2)) || x0.equals(x2)");
        b.append(");");
        return b.toString();
    }
}
