package net;

public class ConnectionConditions {

    // void net.Connection.open()
    public static boolean m1_t0(net.Connection target) {
        // @throws java.lang.IllegalStateException if the connection is already open ==> target.isOpen()
        return target.isOpen();
    }

    // void net.Connection.send(java.lang.String message)
    public static boolean m2_t0(net.Connection target, java.lang.String message) {
        // @throws java.lang.NullPointerException if message is null ==> args[0]==null
        return message == null;
    }

    // void net.Connection.send(java.lang.String message)
    public static boolean m2_t1(net.Connection target, java.lang.String message) {
        // @throws java.lang.IllegalStateException if the connection is not open ==> (target.isOpen()) == false
        return (target.isOpen()) == false;
    }

    // void net.Connection.send(int code)
    public static boolean m3_t0(net.Connection target, int code) {
        // @throws java.lang.IllegalStateException if the connection is not open ==> (target.isOpen()) == false
        return (target.isOpen()) == false;
    }

    // void net.Connection.send(int code)
    public static boolean m3_p0(net.Connection target, int code) {
        // @param code the code, must be positive ==> args[0]>0
        return code > 0;
    }
}
