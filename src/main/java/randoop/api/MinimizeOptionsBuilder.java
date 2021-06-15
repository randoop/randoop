package randoop.api;

import org.apache.commons.io.FilenameUtils;
import randoop.main.Minimize;

public class MinimizeOptionsBuilder {

    private String suitepath = Minimize.suitepath;
    private String suiteClasspath = Minimize.suiteclasspath;
    private int minimizeTimeout = Minimize.minimizetimeout;
    private int testsuiteTimeout = Minimize.testsuitetimeout;
    private boolean verboseMinimizer = Minimize.verboseminimizer;

    public MinimizeOptionsBuilder suitepath(String suitepath) {
        if (suitepath == null) {
            throw new IllegalArgumentException("Suitepath cannot be null");
        }
        if (!FilenameUtils.getExtension(suitepath).equals("java")) {
            throw new IllegalArgumentException("The input file must be a Java file: " + suitepath);
        }
        this.suitepath = suitepath;
        return this;
    }

    public MinimizeOptionsBuilder suiteClasspath(String suiteClasspath) {
        this.suiteClasspath = suiteClasspath;
        return this;
    }

    public MinimizeOptionsBuilder minimizeTimeout(int minimizeTimeout) {
        if (minimizeTimeout <= 0) {
            throw new IllegalArgumentException(
                    "Minimizer timeout must be positive, was given as " + minimizeTimeout + ".");
        }
        this.minimizeTimeout = minimizeTimeout;
        return this;
    }

    public MinimizeOptionsBuilder testsuiteTimeout(int testsuiteTimeout) {
        if (testsuiteTimeout <= 0) {
            throw new IllegalArgumentException(
                    "Timout must be positive, was given as " + testsuiteTimeout + ".");
        }
        this.testsuiteTimeout = testsuiteTimeout;
        return this;
    }

    public MinimizeOptionsBuilder verboseMinimizer(boolean verboseMinimizer) {
        this.verboseMinimizer = verboseMinimizer;
        return this;
    }

    public MinimizeOptions build() {
        return new MinimizeOptions(suitepath, suiteClasspath, minimizeTimeout, testsuiteTimeout, verboseMinimizer);
    }
}
