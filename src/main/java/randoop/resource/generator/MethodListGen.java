package randoop.resource.generator;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.plumelib.reflection.Signatures;
import scenelib.annotations.Annotation;
import scenelib.annotations.el.AClass;
import scenelib.annotations.el.AMethod;
import scenelib.annotations.el.AScene;
import scenelib.annotations.io.classfile.ClassFileReader;

public class MethodListGen {
  private static final String CLASS_EXT = ".class";

  private static final String NONDET_ANNOTATION =
      "org.checkerframework.checker.determinism.qual.NonDet";
  private static final String SEF_ANNOTATION = "org.checkerframework.dataflow.qual.SideEffectFree";
  private static final String PURE_ANNOTATION = "org.checkerframework.dataflow.qual.Pure";

  private static final List<String> NONDET_QUALIFYING_ANNOTATIONS = new ArrayList<String>();
  private static final List<String> SEF_QUALIFYING_ANNOTATIONS = new ArrayList<String>();

  private static final String OMIT_METHODS_DEFAULTS_EXISTING_CONTENT =
      "# Long-running.  With sufficiently small arguments, can be fast.\n"
          + "# org.apache.commons.math3.analysis.differentiation.DSCompiler.getCompiler\\(int,int\\)\n"
          + "^org.apache.commons.math3.analysis.differentiation.\n"
          + "^org.apache.commons.math3.analysis.integration.\n"
          + "\n"
          + "# Nondeterministic.\n";

  static {
    NONDET_QUALIFYING_ANNOTATIONS.add(NONDET_ANNOTATION);
    SEF_QUALIFYING_ANNOTATIONS.add(SEF_ANNOTATION);
    SEF_QUALIFYING_ANNOTATIONS.add(PURE_ANNOTATION); // Pure = SideEffectFree + Deterministic
  }

  enum AnnotationCategory {
    NON_DET,
    SIDE_EFFECT_FREE
  }

  /**
   * Main entry point to generate Nondeterministic and Side Effect Free Method lists.
   *
   * @param args command line arguments as follows: args[0] - root of the input .class directory
   *     args[2] - output directory for sefMethods.txt and nonDetMethodsRegex.txt.
   */
  public static void main(String[] args) {
    Path classWorkingDirectory = Paths.get(args[0]);

    Path nonDetFile = Paths.get(args[1] + "/omitmethods-defaults.txt");
    Path sideEffectFreeFile = Paths.get(args[1] + "/JDK-sef-methods.txt");

    try {
      List<String> nonDetMethods =
          walkFilesForAnnotation(
              classWorkingDirectory, AnnotationCategory.NON_DET, NONDET_QUALIFYING_ANNOTATIONS);
      List<String> sideEffectFreeMethods =
          walkFilesForAnnotation(
              classWorkingDirectory,
              AnnotationCategory.SIDE_EFFECT_FREE,
              SEF_QUALIFYING_ANNOTATIONS);

      // Sort in alphabetical order
      Collections.sort(nonDetMethods);
      Collections.sort(sideEffectFreeMethods);

      // Write out the captured methods.
      System.out.println("Nondeterministic methods count: " + nonDetMethods.size());
      if (nonDetMethods.size() > 0) {
        try (BufferedWriter nonDetMethodWriter =
            Files.newBufferedWriter(nonDetFile.toFile().toPath(), UTF_8)) {
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
            Files.newBufferedWriter(sideEffectFreeFile.toFile().toPath(), UTF_8)) {
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

  private static List<String> walkFilesForAnnotation(
      Path root, AnnotationCategory annotationCategory, List<String> qualifyingAnnotations)
      throws IOException {
    List<String> annotatedMethods = new ArrayList<>();
    // Recursively walk the file directory
    try (Stream<Path> paths = Files.walk(root).filter(Files::isRegularFile)) {
      paths.forEach(
          filePath -> {
            if (filePath.toString().endsWith(CLASS_EXT)) {
              try {
                annotatedMethods.addAll(
                    readFile(filePath, annotationCategory, qualifyingAnnotations));
              } catch (IOException ex) {
                throw (new RuntimeException(ex));
              }
            }
          });
    }
    return annotatedMethods;
  }

  private static List<String> readFile(
      Path filePath, AnnotationCategory annotationCategory, List<String> qualifyingAnnotations)
      throws IOException {
    List<String> annotatedMethods = new ArrayList<>();

    // Invoke the parser for the specified class file
    AScene scene = new AScene();
    ClassFileReader.read(scene, filePath.toString());

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
          // Relocation changes the constant annotation comparisons...
          if (qualifyingAnnotations.contains("randoop." + a.def.name.trim())) {
            String fullyQualifiedName = getFullyQualifiedName(aclass, m.getValue().methodName);
            if (!fullyQualifiedName.contains("<init>") && !fullyQualifiedName.contains("$")) {
              annotatedMethods.add(fullyQualifiedName);
            }
            break;
          }
        }
      }
    }
    return annotatedMethods;
  }

  private static String getFullyQualifiedName(AClass aclass, String JVMLmethodSignature) {
    // Parse out the arguments to feed into plume.
    int arglistStartIndex = JVMLmethodSignature.indexOf("(");
    int arglistEndIndex = JVMLmethodSignature.indexOf(")") + 1;
    String methodName = JVMLmethodSignature.substring(0, arglistStartIndex);

    String fullyQualifiedMethodName = aclass.className;
    String fullyQualifiedArgs =
        Signatures.arglistFromJvm(
            JVMLmethodSignature.substring(arglistStartIndex, arglistEndIndex));
    String fullyQualifiedSignature =
        fullyQualifiedMethodName + "." + methodName + fullyQualifiedArgs;
    return fullyQualifiedSignature;
  }
}
