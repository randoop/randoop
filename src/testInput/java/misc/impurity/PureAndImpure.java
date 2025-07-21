package misc.impurity;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;

/**
 * A class with many pure methods and one impure method.
 * Randoop’s object fuzzer will guarantee coverage of doImpure().
 */
public class PureAndImpure {

    private int counter = 0;

    // ———————— Pure, side‐effect‐free methods ————————
    @Pure
    public int getConstant0()    { return 0; }
    @Pure
    public int getConstant1()    { return 1; }
    @Pure
    public int getConstant2()    { return 2; }
    @Pure
    public int square(int x)     { return x * x; }
    @Pure
    public int plusOne(int x)    { return x + 1; }
    @Pure
    public String hello()        { return "hello"; }
    @Pure
    public String echo(String s) { return s;         }
    @Pure
    public boolean alwaysTrue()  { return true;      }
    @Pure
    public boolean isZero(int x) { return x == 0;    }
    @Pure
    public double half(double d) { return d / 2.0;   }

    // ———————— The single impure method ————————
    /**
     * Impure: mutates internal state so side‐effects can be observed.
     */
    @Impure
    public void doImpure(int delta) {
        this.counter += delta;
    }

    /**
     * Pure accessor of the mutated state.
     */
    public int getCounter() {
        return this.counter;
    }
}
