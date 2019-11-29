package randoop.resource.generator;

import static java.nio.charset.StandardCharsets.UTF_8;

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
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.plumelib.reflection.Signatures;
import scenelib.annotations.Annotation;
import scenelib.annotations.el.AClass;
import scenelib.annotations.el.AMethod;
import scenelib.annotations.el.AScene;
import scenelib.annotations.io.classfile.ClassFileReader;

/**
 * This tool generates the resource files JDK-sef-methods.txt and omitmethods-defaults.txt, which
 * contain a list of side effect free methods and a list of methods to omit during Randoop
 * execution, respectively.
 */
public class MethodListGen {
  // The extension for a Java .class file. These files contain annotations that
  // this tool will parse.
  private static final String CLASS_EXT = ".class";

  // The Nondeterministic annotation.
  private static final String NONDET_ANNOTATION =
      "org.checkerframework.checker.determinism.qual.NonDet";

  // The annotations for side effect free methods.
  private static final String SEF_ANNOTATION = "org.checkerframework.dataflow.qual.SideEffectFree";
  private static final String PURE_ANNOTATION = "org.checkerframework.dataflow.qual.Pure";

  // The list of qualifying annotations for each criteria.
  private static final Collection<String> NONDET_QUALIFYING_ANNOTATIONS =
      Collections.singletonList(NONDET_ANNOTATION);
  private static final Collection<String> SEF_QUALIFYING_ANNOTATIONS =
      Arrays.asList(SEF_ANNOTATION, PURE_ANNOTATION);

  // omitmethods-defaults.txt contains some long-running methods that are not
  // generated directly by this tool. This is will appended to the beginning of
  // generated omitmethods-defaults.txt file.
  private static final String OMIT_METHODS_DEFAULTS_EXISTING_CONTENT =
      "# Long-running.  With sufficiently small arguments, can be fast.\n"
          + "# org.apache.commons.math3.analysis.differentiation.DSCompiler.getCompiler\\(int,int\\)\n"
          + "^org.apache.commons.math3.analysis.differentiation.\n"
          + "^org.apache.commons.math3.analysis.integration.\n"
          + "\n"
          + "# Nondeterministic.\n";

  // Used as distinction between which annotation we want to capture.
  enum AnnotationCategory {
    NON_DET,
    SIDE_EFFECT_FREE
  }

  /**
   * Main entry point to generate Nondeterministic and Side Effect Free Method lists.
   *
   * @param args command line arguments
   *     <ul>
   *       <li>args[0] - root of the input .class directory
   *       <li>args[1] - output directory
   *     </ul>
   */
  public static void main(String[] args) {
    Path classWorkingDirectory = Paths.get(args[0]);

    Path nonDetFile = Paths.get(args[1] + "/omitmethods-defaults.txt");
    Path sideEffectFreeFile = Paths.get(args[1] + "/JDK-sef-methods.txt");

    try {
      List<String> nonDetMethods =
          getAnnotatedMethodsFromJDKJar(
              classWorkingDirectory, AnnotationCategory.NON_DET, NONDET_QUALIFYING_ANNOTATIONS);
      List<String> sideEffectFreeMethods =
          getAnnotatedMethodsFromJDKJar(
              classWorkingDirectory,
              AnnotationCategory.SIDE_EFFECT_FREE,
              SEF_QUALIFYING_ANNOTATIONS);

      // Write out the captured methods.
      System.out.println("Nondeterministic methods count: " + nonDetMethods.size());
      if (nonDetMethods.size() > 0) {
        try (BufferedWriter nonDetMethodWriter = Files.newBufferedWriter(nonDetFile, UTF_8)) {
          nonDetMethodWriter.write(OMIT_METHODS_DEFAULTS_EXISTING_CONTENT);
          for (String method : nonDetMethods) {
            nonDetMethodWriter.write(
                "^" + method.replace(".", "\\.").replace("(", "\\(").replace(")", "\\)") + "$");
            nonDetMethodWriter.newLine();
          }
          nonDetMethodWriter.flush();
        }
      } else {
        System.out.println("No nonDet methods found. Not writing to file.");
      }

      System.out.println("SideEffect Free methods count: " + sideEffectFreeMethods.size());
      if (sideEffectFreeMethods.size() > 0) {
        try (BufferedWriter sideEffectMethodWriter =
            Files.newBufferedWriter(sideEffectFreeFile, UTF_8)) {
          for (String method : sideEffectFreeMethods) {
            sideEffectMethodWriter.write(method);
            sideEffectMethodWriter.newLine();
          }
          sideEffectMethodWriter.flush();
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
   * @param jarFile jdk jar
   * @param annotationCategory type of annotation we are parsing for
   * @param qualifyingAnnotations list of annotations
   * @return annotated methods in fully-qualified signature format
   */
  private static List<String> getAnnotatedMethodsFromJDKJar(
      Path jarFile,
      AnnotationCategory annotationCategory,
      Collection<String> qualifyingAnnotations) {
    List<String> annotatedMethods = new ArrayList<>();
    try {
      JarFile jar = new JarFile(jarFile.toFile());

      // Read through each entry in a jar via stream
      Stream<JarEntry> jarEntries = jar.stream();
      jarEntries.forEach(
          jarEntry -> {
            try {
              if (jarEntry.getName().endsWith(CLASS_EXT)) {
                InputStream is = jar.getInputStream(jarEntry);
                annotatedMethods.addAll(
                    readClassFile(is, annotationCategory, qualifyingAnnotations));
              }
            } catch (IOException e) {
              throw new RuntimeException("Failure to parse: " + e.getMessage());
            }
          });
    } catch (IOException e) {
      throw new RuntimeException("Failure to parse: " + e.getMessage());
    }

    // Sort in alphabetical order
    Collections.sort(annotatedMethods);
    return annotatedMethods;
  }

  /**
   * Reads a class file using the given input stream and parses it for the desired annotations.
   *
   * @param classInputStream input stream used for reading
   * @param annotationCategory type of annotation we are parsing for
   * @param qualifyingAnnotations list of annotations
   * @return list of methods with the desired annotations in fully qualified signature format
   * @throws IOException if SceneLib fails to parse the class file
   */
  private static List<String> readClassFile(
      InputStream classInputStream,
      AnnotationCategory annotationCategory,
      Collection<String> qualifyingAnnotations)
      throws IOException {
    List<String> annotatedMethods = new ArrayList<>();

    // Invoke the parser for the specified class file
    AScene scene = new AScene();
    ClassFileReader.read(scene, classInputStream);

    // Look through each class and its corresponding methods.
    for (Map.Entry<String, AClass> entry : scene.classes.entrySet()) {
      AClass aclass = entry.getValue();
      for (Map.Entry<String, AMethod> m : aclass.methods.entrySet()) {
        // Check annotations for the method
        Collection<Annotation> annotationLocation =
            annotationCategory.equals(AnnotationCategory.NON_DET)
                ? m.getValue().returnType.tlAnnotationsHere
                : m.getValue().tlAnnotationsHere;
        for (Annotation a : annotationLocation) {
          // Relocation changes the constant annotation comparisons.
          // Thus, we need to append the 'randoop.' prefix to the annotation
          // parsed from the class files.
          if (qualifyingAnnotations.contains("randoop." + a.def.name.trim())) {
            String fullyQualifiedName =
                getFullyQualifiedSignatures(aclass, m.getValue().methodName);
            annotatedMethods.add(fullyQualifiedName);
            break;
          }
        }
      }
    }
    return annotatedMethods;
  }

  /**
   * Returns the fully qualified signature from the given class and signature in JVML format.
   *
   * @param aclass AClass that the method belongs to
   * @param JVMLmethodSignature method signature in JVML
   * @return fully qualified method signature
   */
  private static String getFullyQualifiedSignatures(AClass aclass, String JVMLmethodSignature) {
    int arglistStartIndex = JVMLmethodSignature.indexOf("(");
    int arglistEndIndex = JVMLmethodSignature.indexOf(")") + 1;
    String methodName = JVMLmethodSignature.substring(0, arglistStartIndex);

    String fullyQualifiedClassName = aclass.className;
    String fullyQualifiedArgs =
        Signatures.arglistFromJvm(
            JVMLmethodSignature.substring(arglistStartIndex, arglistEndIndex));
    String fullyQualifiedSignature =
        fullyQualifiedClassName + "." + methodName + fullyQualifiedArgs;
    return fullyQualifiedSignature;
  }
}
