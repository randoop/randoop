package randoop.contract;

import java.util.ArrayList;
import java.util.List;

import randoop.Globals;
import randoop.types.ConcreteTypes;
import randoop.types.GeneralType;
import randoop.types.TypeTuple;

/**
 * The contract: Checks that compareTo is consistent with equals
 * <code>(x0.compareTo(x1) == 0) == x0.equals(x1)</code>.
 */

public class CompareToEquals implements ObjectContract {
    private static final CompareToEquals instance = new CompareToEquals();

    private CompareToEquals() {};

    public static CompareToEquals getInstance() {
        return instance;
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    @Override
    public boolean evaluate(Object... objects) {
        Object o1 = objects[0];
        Object o2 = objects[1];

        if (o1 instanceof Comparable) {
            Comparable compObj1 = (Comparable) o1;
            return (compObj1.compareTo(o2) == 0) == (o1.equals(o2));
        }
        return true;
    }

    @Override
    public int getArity() {
        return 2;
    }

    @Override
    public TypeTuple getInputTypes() {
        List<GeneralType> inputTypes = new ArrayList<>();
        inputTypes.add(ConcreteTypes.COMPARABLE_TYPE);
        inputTypes.add(ConcreteTypes.COMPARABLE_TYPE);
        return new TypeTuple(inputTypes);
    }

    @Override
    public String toCommentString() {
        return "compareTo-equals on x0 and x1";
    }

    @Override
    public String get_observer_str() {
        return "CompareToEquals";
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
        b.append("(x0.compareTo(x1) == 0) == x0.equals(x1)");
        b.append(");");
        return b.toString();
    }
}
