package randoop.contract;

import randoop.Globals;

/**
 * The contract: Checks the transitivity of the compare to method
 * <code>((x0.compareTo(x1) > 0) &amp;&amp; (x1.compareTo(x2) > 0)) &rarr; (x0.compareTo(x2) > 0)</code>.
 */
public class CompareToTransitive implements ObjectContract {
    private static final CompareToTransitive instance = new CompareToTransitive();

    private CompareToTransitive() {};

    public static CompareToTransitive getInstance() {
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
            Comparable compObj1 = (Comparable) o1;
            Comparable compObj2 = (Comparable) o2;
            Comparable compObj3 = (Comparable) o3;

            if (compObj1.compareTo(compObj2) > 0 && compObj2.compareTo(compObj3) > 0) {
                return (compObj1.compareTo(compObj3) > 0);
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
        return "compareTo-transitive on x0, x1, and x2";
    }

    @Override
    public String get_observer_str() {
        return "CompareToTransitive";
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
        b.append("!(x0.compareTo(x1)>0 && x1.compareTo(x2)>0) || x0.compareTo(x2)>0");
        b.append(");");
        return b.toString();
    }
}

