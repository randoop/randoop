package randoop.resource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static randoop.reflection.OperationModel.signatureToOperation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
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
import randoop.main.RandoopUsageError;
import randoop.reflection.EverythingAllowedPredicate;
import randoop.reflection.VisibilityPredicate;

/**
 * This tool generates the resource files JDK-sef-methods.txt and omitmethods-defaults.txt, which
 * contain a list of side effect free methods and a list of methods to omit during Randoop
 * execution, respectively.
 */
public class MethodListGen {
  /**
   * The extension for a Java .class file. These files contain annotations that this tool will
   * parse.
   */
  private static final String CLASS_EXT = ".class";

  /** The type annotations indicating a non-deterministic return value. */
  private static final Collection<String> NONDET_ANNOTATIONS =
      Collections.singletonList("org.checkerframework.checker.determinism.qual.NonDet");

  /** The method annotations indicating a side-effect-free method. */
  private static final Collection<String> SEF_ANNOTATIONS =
      Arrays.asList(
          "org.checkerframework.dataflow.qual.SideEffectFree",
          "org.checkerframework.dataflow.qual.Pure");

  /** Text to place at the beginning of file {@code omitmethods-defaults.txt}. */
  private static final String OMIT_METHODS_FILE_HEADER =
      String.join(
          System.lineSeparator(),
          "# Long-running.  With sufficiently small arguments, can be fast.",
          "# org.apache.commons.math3.analysis.differentiation.DSCompiler.getCompiler\\(int,int\\)",
          "^org.apache.commons.math3.analysis.differentiation.",
          "^org.apache.commons.math3.analysis.integration.",
          "# Nondeterministic.");

  /**
   * Main entry point to generate Nondeterministic and Side Effect Free Method lists.
   *
   * @param args command line arguments
   *     <ul>
   *       <li>args[0] - JDK .jar location as a path
   *       <li>args[1] - output directory
   *     </ul>
   */
  public static void main(String[] args) {
    Path annotatedJar = Paths.get(args[0]);

    Path nonDetFile = Paths.get(args[1], "omitmethods-defaults.txt");
    Path sideEffectFreeFile = Paths.get(args[1], "JDK-sef-methods.txt");
    Path unparsableSideEffectFreeFile = Paths.get(args[1], "JDK-sef-methods-unparsable.txt");

    try {
      List<String> nonDetMethods = getAnnotatedMethodsFromJar(annotatedJar, NONDET_ANNOTATIONS);
      List<String> sideEffectFreeMethods =
          getAnnotatedMethodsFromJar(annotatedJar, SEF_ANNOTATIONS);

      // Randoop expects a list of omitmethods as regex.
      if (nonDetMethods.size() > 0) {
        System.out.println("Nondeterministic methods count: " + nonDetMethods.size());
        try (BufferedWriter nonDetMethodWriter = Files.newBufferedWriter(nonDetFile, UTF_8)) {
          nonDetMethodWriter.write(OMIT_METHODS_FILE_HEADER);
          for (String method : nonDetMethods) {
            nonDetMethodWriter.write("^" + Pattern.quote(method) + "$");
            nonDetMethodWriter.newLine();
          }
        }
      } else {
        System.out.println("No nondeterministic methods found. Not writing to file.");
      }

      // There are some fully qualified signatures that Randoop cannot parse.
      // We will separate these into two files, the default (parsable) file and
      // the non-parsable file.
      if (sideEffectFreeMethods.size() > 0) {
        System.out.println("Total side effect free methods count: " + sideEffectFreeMethods.size());
        try (BufferedWriter sideEffectMethodWriter =
                Files.newBufferedWriter(sideEffectFreeFile, UTF_8);
            BufferedWriter unparsableSideEffectMethodWriter =
                Files.newBufferedWriter(unparsableSideEffectFreeFile, UTF_8)) {
          for (String fullyQualifiedMethodSignature : sideEffectFreeMethods) {
            try {
              signatureToOperation(
                  fullyQualifiedMethodSignature,
                  VisibilityPredicate.IS_ANY,
                  new EverythingAllowedPredicate());
              sideEffectMethodWriter.write(fullyQualifiedMethodSignature);
              sideEffectMethodWriter.newLine();
            } catch (RandoopUsageError e) {
              System.err.println("Not parsable: " + e.getMessage());
              unparsableSideEffectMethodWriter.write(fullyQualifiedMethodSignature);
              unparsableSideEffectMethodWriter.newLine();
            }
          }
        }
      } else {
        System.out.println("No side effect free methods found. Not writing to file.");
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Returns the annotated methods from the the given jar.
   *
   * @param jarFile JDK .jar file path
   * @param annotations which annotations to capture
   * @return list of annotated methods in fully-qualified signature format, in alphabetical order
   * @throws IOException if Randoop cannot parse a method
   */
  private static List<String> getAnnotatedMethodsFromJar(
      Path jarFile, Collection<String> annotations) throws IOException {

    List<String> annotatedMethods = new ArrayList<>();
    JarFile jar = new JarFile(jarFile.toFile());
    Stream<JarEntry> jarEntries = jar.stream();
    jarEntries.forEach(
        jarEntry -> {
          try {
            if (jarEntry.getName().endsWith(CLASS_EXT)) {
              InputStream is = jar.getInputStream(jarEntry);
              annotatedMethods.addAll(getAnnotatedMethodsFromClassFile(is, annotations));
            }
          } catch (IOException e) {
            throw new RuntimeException("Failure to parse: " + e.getMessage());
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
    // Invoke the parser for the specified class file
    ClassReader cr = new ClassReader(classInputStream);

    AnnotationScanner as = new AnnotationScanner(Opcodes.ASM5, desiredAnnotations);

    // Invoke the annotation scanner on the class.
    cr.accept(as, 0);

    return as.getMethodsWithAnnotations();
  }
}
