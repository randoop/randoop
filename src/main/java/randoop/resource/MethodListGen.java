package randoop.resource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static randoop.reflection.OperationModel.signatureToOperation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import randoop.reflection.EverythingAllowedPredicate;
import randoop.reflection.FailedPredicateException;
import randoop.reflection.SignatureParseException;
import randoop.reflection.VisibilityPredicate;

/**
 * This tool generates the resource files JDK-sef-methods.txt and JDK-nondet-methods.txt, which
 * contain a list of side effect free methods and a list of nondeterministic methods, respectively.
 */
public class MethodListGen {
  /** Methods to ignore (present in JDK 8 but not JDK 11) */
  private static final Collection<String> METHODS_TO_IGNORE =
      Arrays.asList(
          "java.util.RandomAccessSubList.subList(int, int)",
          // https://hg.openjdk.java.net/jdk8/jdk8/jdk/file/687fd7c7986d/src/share/classes/java/lang/String.java#l1740
          "java.lang.String.indexOf(char[], int, int, char[], int, int, int)",
          // package-private in Java 8, not in Java 11:
          "java.lang.String.<init>(char[], boolean)");

  /** The type annotations indicating a non-deterministic return value. */
  private static final Collection<String> NONDET_ANNOTATIONS =
      Collections.singletonList("org.checkerframework.checker.determinism.qual.NonDet");

  /** The method annotations indicating a side-effect-free method. */
  private static final Collection<String> SEF_ANNOTATIONS =
      Arrays.asList(
          "org.checkerframework.dataflow.qual.SideEffectFree",
          "org.checkerframework.dataflow.qual.Pure");

  /**
   * Main entry point to generate Nondeterministic and Side Effect Free Method lists.
   *
   * @param args command line arguments
   *     <ul>
   *       <li>args[0] - JDK .jar location as a path. Currently only tested with JDK 8.
   *       <li>args[1] - output directory
   *     </ul>
   */
  public static void main(String[] args) {
    Path annotatedJar = Paths.get(args[0]);
    String outputDirectory = args[1];

    Path nonDetFile = Paths.get(outputDirectory, "JDK-nondet-methods.txt");
    Path sideEffectFreeFile = Paths.get(outputDirectory, "JDK-sef-methods.txt");
    Path unparsableSideEffectFreeFile =
        Paths.get(outputDirectory, "JDK-sef-methods-unparsable.txt");

    try {
      List<String> nonDetMethods = getAnnotatedMethodsFromJar(annotatedJar, NONDET_ANNOTATIONS);

      System.out.println("Nondeterministic methods count: " + nonDetMethods.size());
      try (BufferedWriter nonDetMethodWriter = Files.newBufferedWriter(nonDetFile, UTF_8)) {
        for (String method : nonDetMethods) {
          if (METHODS_TO_IGNORE.contains(method)) {
            continue;
          }
          nonDetMethodWriter.write("^" + Pattern.quote(method) + "$");
          nonDetMethodWriter.newLine();
        }
      }

      List<String> sideEffectFreeMethods =
          getAnnotatedMethodsFromJar(annotatedJar, SEF_ANNOTATIONS);

      // There are some fully qualified signatures that Randoop cannot parse.
      // We will separate these into two files, the default (parsable) file and
      // the non-parsable file.
      System.out.println("Side effect free methods count: " + sideEffectFreeMethods.size());
      try (BufferedWriter sideEffectMethodWriter =
              Files.newBufferedWriter(sideEffectFreeFile, UTF_8);
          BufferedWriter unparsableSideEffectMethodWriter =
              Files.newBufferedWriter(unparsableSideEffectFreeFile, UTF_8)) {
        for (String fullyQualifiedMethodSignature : sideEffectFreeMethods) {
          if (METHODS_TO_IGNORE.contains(fullyQualifiedMethodSignature)) {
            continue;
          }
          try {
            signatureToOperation(
                fullyQualifiedMethodSignature,
                VisibilityPredicate.IS_ANY,
                new EverythingAllowedPredicate());
            sideEffectMethodWriter.write(fullyQualifiedMethodSignature);
            sideEffectMethodWriter.newLine();
          } catch (SignatureParseException | FailedPredicateException e) {
            System.err.println("Not parsable: " + e.getMessage());
            unparsableSideEffectMethodWriter.write(fullyQualifiedMethodSignature);
            unparsableSideEffectMethodWriter.newLine();
            e.printStackTrace(new PrintWriter(unparsableSideEffectMethodWriter));
            unparsableSideEffectMethodWriter.newLine();
          }
        }
      }
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  /**
   * Returns the annotated methods from the the given jar.
   *
   * @param jarFile .jar file path
   * @param annotations which annotations to capture
   * @return list of annotated methods in fully-qualified signature format, in alphabetical order
   * @throws IOException if we cannot read the jar file
   */
  private static List<String> getAnnotatedMethodsFromJar(
      Path jarFile, Collection<String> annotations) throws IOException {

    List<String> annotatedMethods = new ArrayList<>();
    JarFile jar = new JarFile(jarFile.toFile());
    Stream<JarEntry> jarEntries = jar.stream();
    jarEntries.forEach(
        jarEntry -> {
          try {
            if (jarEntry.getName().endsWith(".class")) {
              InputStream is = jar.getInputStream(jarEntry);
              annotatedMethods.addAll(getAnnotatedMethodsFromClassFile(is, annotations));
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });

    // Sort in alphabetical order
    Collections.sort(annotatedMethods);
    return annotatedMethods;
  }

  /**
   * Gets the annotated methods with the specified annotations from a class file.
   *
   * @param classInputStream input stream used for reading the class file
   * @param desiredAnnotations which annotations to capture
   * @return list of methods with the desired annotations in fully qualified signature format
   * @throws IOException if ASM fails to parse the class file
   */
  private static Set<String> getAnnotatedMethodsFromClassFile(
      InputStream classInputStream, Collection<String> desiredAnnotations) throws IOException {
    ClassReader cr = new ClassReader(classInputStream);
    ClassAnnotationScanner as = new ClassAnnotationScanner(Opcodes.ASM5, desiredAnnotations);
    // Invoke the annotation scanner on the class.
    cr.accept(as, 0);
    return as.getMethodsWithDesiredAnnotations();
  }
}
