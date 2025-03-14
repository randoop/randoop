package randoop.grt_mock.jcommander_1_35.com.beust.jcommander.internal;

public class DefaultConsole {

    /**
     * Mock of com.beust.jcommander.internal.DefaultConsole.readPassword(boolean).
     * This method does nothing and returns an empty char array.
     *
     * @param instance    The instance of DefaultConsole (ignored).
     * @param echoInput   The echoInput parameter (ignored).
     * @return            An empty char array.
     */
    public static char[] readPassword(com.beust.jcommander.internal.Console instance, boolean echoInput) {
        return new char[0];
    }
}
