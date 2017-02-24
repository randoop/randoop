package net;

public class ConnectionConditions {

    // void net.Connection.open()
    public static boolean m1_t0(net.Connection receiver1_t0) {
        // @throws java.lang.IllegalStateException if the connection is already open ==> target.isOpen()
        return receiver1_t0.isOpen();
    }

    // void net.Connection.send(java.lang.String message)
    public static boolean m2_t0(net.Connection receiver2_t0, java.lang.String message) {
        // @throws java.lang.NullPointerException if message is null ==> args[0]==null
        return message == null;
    }

    // void net.Connection.send(java.lang.String message)
    public static boolean m2_t1(net.Connection receiver2_t1, java.lang.String message) {
        // @throws java.lang.IllegalStateException if the connection is not open ==> (target.isOpen()) == false
        return (receiver2_t1.isOpen()) == false;
    }

    // void net.Connection.send(int code)
    public static boolean m3_t0(net.Connection receiver3_t0, int code) {
        // @throws java.lang.IllegalStateException if the connection is not open ==> (target.isOpen()) == false
        return (receiver3_t0.isOpen()) == false;
    }

    // void net.Connection.send(int code)
    public static boolean m3_p0(net.Connection receiver3_p0, int code) {
        // @param code the code, must be positive ==> args[0]>0
        return code > 0;
    }
}
