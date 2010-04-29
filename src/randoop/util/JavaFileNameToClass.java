package randoop.util;

/**
 * Takes a String representing a list of files "a/b/C.java ... d/e/f/G.java" and
 * returns "a.b.C ... d.e.f.G"
 * 
 * (This is probably a perl one-liner, but I am better with Java).
 */
public class JavaFileNameToClass {

  public static void main(String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (i > 0) {
        System.out.print(" ");
      }
      String arg = args[i];
      assert arg.endsWith(".java");
      arg = arg.substring(0, arg.length() - 5);
      String[] splits = arg.split("/");
      for (int j = 0; j < splits.length; j++) {
        if (j > 0) {
          System.out.print(".");
        }
        System.out.print(splits[j]);
      }
    }
  }
}
