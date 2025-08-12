package misc;

import org.checkerframework.dataflow.qual.Pure;

public class PureStaticUnaryMethodExample {
    private String lastValue;

    @Pure
    public static String describeLength(String str) {
        return str == null ? "null" : "Length: " + str.length();
    }

    public void printAndStore(String str) {
        this.lastValue = str;
        System.out.println("Stored value: " + str);
    }
}