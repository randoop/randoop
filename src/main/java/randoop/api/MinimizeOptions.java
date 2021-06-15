package randoop.api;

import randoop.main.Minimize;

public class MinimizeOptions {

    // first ever options to be created, all values same as default in Minimize
    public static final MinimizeOptions DEFAULT = new MinimizeOptions();

    private final String suitepath;
    private final String suiteClasspath;
    private final int minimizeTimeout;
    private final int testsuiteTimeout;
    private final boolean verboseMinimizer;

    private MinimizeOptions() {
        this(Minimize.suitepath,
                Minimize.suiteclasspath,
                Minimize.minimizetimeout,
                Minimize.testsuitetimeout,
                Minimize.verboseminimizer);
    }

    public MinimizeOptions(String suitepath,
                           String suiteClasspath,
                           int minimizeTimeout,
                           int testsuiteTimeout,
                           boolean verboseMinimizer) {
        this.suitepath = suitepath;
        this.suiteClasspath = suiteClasspath;
        this.minimizeTimeout = minimizeTimeout;
        this.testsuiteTimeout = testsuiteTimeout;
        this.verboseMinimizer = verboseMinimizer;
    }

    public void configure() {
        Minimize.suitepath = suitepath;
        Minimize.suiteclasspath = suiteClasspath;
        Minimize.minimizetimeout = minimizeTimeout;
        Minimize.testsuitetimeout = testsuiteTimeout;
        Minimize.verboseminimizer = verboseMinimizer;
    }

    public String getSuitepath() {
        return suitepath;
    }

    public String getSuiteClasspath() {
        return suiteClasspath;
    }

    public int getMinimizeTimeout() {
        return minimizeTimeout;
    }

    public int getTestsuiteTimeout() {
        return testsuiteTimeout;
    }

    public boolean isVerboseMinimizer() {
        return verboseMinimizer;
    }
}
