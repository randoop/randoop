package randoop.resource.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.plumelib.reflection.Signatures;
import scenelib.annotations.Annotation;
import scenelib.annotations.el.AClass;
import scenelib.annotations.el.AMethod;
import scenelib.annotations.el.AScene;
import scenelib.annotations.io.classfile.ClassFileReader;

public class NonDetMain {
  /**
   * @param args [0] - root of the input .class directory [1] - output file for pure observer
   *     methods [2] - output file for side effect free methods
   */
  public static void main(String[] args) {
    Path workingDirectory = Paths.get(args[0]);
    Path nonDetFile = Paths.get(args[1]);
    List<String> nonDetMethods;
    try {
      nonDetMethods = walkClassFiles(workingDirectory);

      // Write out the captured methods.
      try {
        BufferedWriter nonDetMethodWriter = new BufferedWriter(new FileWriter(nonDetFile.toFile()));
        for (String method : nonDetMethods) {
          nonDetMethodWriter.write(method);
          nonDetMethodWriter.newLine();
        }
        nonDetMethodWriter.flush();
        nonDetMethodWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static List<String> walkClassFiles(Path root) throws IOException {

    // Recursively walk the file directory
    List<String> nonDetMethodNames = new ArrayList<>();
    Stream<Path> paths = Files.walk(root).filter(Files::isRegularFile);
    paths.forEach(
        filePath -> {
          if (filePath.toString().endsWith(".class")) {
            try {
              nonDetMethodNames.addAll(readFile(filePath));
            } catch (IOException ex) {
              throw (new RuntimeException(ex));
            }
          }
        });
    paths.close();
    return nonDetMethodNames;
  }

  private static List<String> readFile(Path filePath) throws IOException {
    List<String> nonDetMethods = new ArrayList<>();
    // Invoke the parser for the specified jaif file
    AScene scene = new AScene();
    ClassFileReader.read(scene, filePath.toString());

    // Look through each class and its corresponding methods.
    for (Map.Entry<String, AClass> entry : scene.classes.entrySet()) {
      AClass cls = entry.getValue();
      for (Map.Entry<String, AMethod> m : cls.methods.entrySet()) {

        // The commented out code might be too heavy for our purposes (?)
        //	besides the fact I haven't got it working yet...
        /*AnnotationDef aDef = new AnnotationDef("@org.checkerframework.dataflow.qual.Pure");
        aDef.setFieldTypes(new HashMap<String, AnnotationFieldType>());
        Annotation annotation = AnnotationFactory.saf.beginAnnotation(aDef).finish();*/
        boolean nonDet = false;

        // Check annotations for the method
        for (Annotation a : m.getValue().returnType.tlAnnotationsHere) {
          if (a.def.name.equals("org.checkerframework.checker.determinism.qual.NonDet")) {
            nonDet = true;
            break;
          }
        }

        if (nonDet) {
          String JVMLmethodSignature = m.getValue().methodName;
          // Parse out the arguments to feed into plume.
          int arglistStartIndex = JVMLmethodSignature.indexOf("(");
          int arglistEndIndex = JVMLmethodSignature.indexOf(")") + 1;
          String methodName = JVMLmethodSignature.substring(0, arglistStartIndex);

          String fullyQualifiedMethodName = cls.className;
          String fullyQualifiedArgs =
              Signatures.arglistFromJvm(
                  JVMLmethodSignature.substring(arglistStartIndex, arglistEndIndex));
          String fullyQualifiedSignature =
              fullyQualifiedMethodName + "." + methodName + fullyQualifiedArgs;
          nonDetMethods.add(fullyQualifiedSignature);
        }
      }
    }
    return nonDetMethods;
  }
}
