package pkg;

public class pkg_SuperClass {

    // void pkg.SuperClass.methodWithOverride(int count)
    public static boolean m0_p0(pkg.SuperClass target, int count) {
        // @param count the object count, must be positive ==> args[0]>0
        return count > 0;
    }

    // void pkg.SuperClass.methodWithImplicitOverride(int count)
    public static boolean m1_p0(pkg.SuperClass target, int count) {
        // @param count the object count, must be positive ==> args[0]>0
        return count > 0;
    }

    // void pkg.SuperClass.methodWithoutOverride(int count)
    public static boolean m2_p0(pkg.SuperClass target, int count) {
        // @param count the something count, must be positive ==> args[0]>0
        return count < 0;
    }
}
