package randoop.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import randoop.experiments.WriteModelCheckerDriver;
import randoop.experiments.WriteModelCheckerDriver.Target;
import randoop.util.Reflection;
import plume.Option;
import plume.Options;
import plume.Options.ArgException;

public class UniversalDriverHandler extends CommandHandler {

  private static final Object[] usage_synopsis = null;

  @Option("Specify a class under test")
  public static List<String> test_class = new ArrayList<String>();

  @Option("The given file contains the list of target classes.")
  public static String classlist = null;

  public UniversalDriverHandler() {
    super("universal-driver", "", "", "", "", null, "", "", "", null);
  }

  @Override
  public boolean handle(String[] args) throws RandoopTextuiException {

    Options parsedArgs = new Options(usage_synopsis,
        WriteModelCheckerDriver.class);
    String non_options[] = null;

    try {
      non_options = parsedArgs.parse (args);
    } catch (ArgException ae) {
      throw new RuntimeException(ae);
    }

    String targetStr = args[0];
    String className = args[1];
    Target target = null;
    if (targetStr.equals("jcute")) {
      target = Target.JCUTE;
    } else if (targetStr.equals("jpf")) {
      target = Target.JPF;
    } else if (targetStr.equals("rand")) {
      target = Target.RANDOM;
    } else {
      throw new IllegalArgumentException(targetStr);
    }
    try {
      randoop.experiments.WriteModelCheckerDriver.writeDriver(target,
          className, findClassesFromArgs(parsedArgs));
    } catch (IOException e) {
      // TODO this exception will propagate to top-level. bad.
      throw new RuntimeException(e);
    }

    return true;
  }

  private static List<Class<?>> findClassesFromArgs(Options printUsageTo)
  throws IOException {
    List<Class<?>> classes = new ArrayList<Class<?>>();
    if (classlist == null && test_class.size() == 0) {
      Object[] args = {};
      System.out.printf ("You must specify some classes to test! Use the `classlist' or `testclass' options.", args);
      for (String use : printUsageTo.usage()) {
        System.out.printf ("  %s%n", use);
      }
      System.exit(1);
    }
    if (classlist != null) {
      File classListingFile = new File(classlist);
      classes.addAll(Reflection.loadClassesFromFile(classListingFile));
    }
    classes.addAll(Reflection.loadClassesFromList(test_class, GenInputsAbstract.silently_ignore_bad_class_names));
    assert classes.size() > 0;
    return classes;
  }

}
