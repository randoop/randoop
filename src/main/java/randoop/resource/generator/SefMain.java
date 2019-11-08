package randoop.resource.generator;

import annotator.specification.IndexFileSpecification;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
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

public class SefMain {
  /**
   * @param args [0] - root of the input jaif directory [1] - output file for pure observer methods
   *     [2] - output file for side effect free methods
   */
  public static void main(String[] args) {
    Path workingDirectory = Paths.get(args[0]);
    Path pureFile = Paths.get(args[1]);
    Path sideEffectFreeFile = Paths.get(args[2]);
    List<String> pureMethods = new ArrayList<String>();
    List<String> sideEffectFreeMethods = new ArrayList<String>();
    try {
      walkClassFiles(workingDirectory, pureMethods, sideEffectFreeMethods);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    // Write out the captured methods.
    try {
      BufferedWriter sideEffectMethodWriter =
          new BufferedWriter(new FileWriter(sideEffectFreeFile.toFile()));
      for (String method : sideEffectFreeMethods) {
        sideEffectMethodWriter.write(method);
        sideEffectMethodWriter.newLine();
      }
      sideEffectMethodWriter.flush();
      sideEffectMethodWriter.close();

      BufferedWriter pureMethodWriter = new BufferedWriter(new FileWriter(pureFile.toFile()));
      for (String method : pureMethods) {
        pureMethodWriter.write(method);
        pureMethodWriter.newLine();
      }
      pureMethodWriter.flush();
      pureMethodWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void walkClassFiles(
      Path root, List<String> outPureMethods, List<String> outSideEffectFreeMethods)
      throws IOException {

    // Recursively walk the file directory
    Stream<Path> paths = Files.walk(root).filter(Files::isRegularFile);
    paths.forEach(
        filePath -> {
          if (filePath.toString().endsWith(".jaif")) {
            try {
              readFile(filePath, outPureMethods, outSideEffectFreeMethods);
            } catch (FileNotFoundException ex) {
              throw (new RuntimeException(ex));
            } catch (IOException ex) {
              throw (new RuntimeException(ex));
            }
          }
        });
    paths.close();
  }

  private static void readFile(
      Path filePath, List<String> pureMethods, List<String> sideEffectFreeMethods)
      throws FileNotFoundException, IOException {
    // Invoke the parser for the specified jaif file
    IndexFileSpecification ifs = new IndexFileSpecification(filePath.toString());
    AScene scene = ifs.getScene();
    ifs.parse();

    // Look through each class and its corresponding methods.
    for (Map.Entry<String, AClass> entry : scene.classes.entrySet()) {
      AClass cls = entry.getValue();
      for (Map.Entry<String, AMethod> m : cls.methods.entrySet()) {

        // The commented out code might be too heavy for our purposes (?)
        //	besides the fact I haven't got it working yet...
        /*AnnotationDef aDef = new AnnotationDef("@org.checkerframework.dataflow.qual.Pure");
        aDef.setFieldTypes(new HashMap<String, AnnotationFieldType>());
        Annotation annotation = AnnotationFactory.saf.beginAnnotation(aDef).finish();*/
        boolean sideEffectFree = false;
        boolean pure = false;

        // Check annotations for the method
        for (Annotation a : m.getValue().tlAnnotationsHere) {
          if (a.def.name.equals("org.checkerframework.dataflow.qual.Pure")) {
            pure = true;
            break;
          } else if (a.def.name.equals("org.checkerframework.dataflow.qual.SideEffectFree")) {
            sideEffectFree = true;
            break;
          }
        }
        if (!sideEffectFree && !pure) {
          continue;
        }

        String JVMLmethodSignature = m.getValue().methodName;
        // Parse out the arguments to feed into plume.
        int arglistStartIndex = JVMLmethodSignature.indexOf("(");
        int arglistEndIndex = JVMLmethodSignature.indexOf(")") + 1;
        String methodName = JVMLmethodSignature.substring(0, arglistStartIndex);

        // TODO: Figure out how constructors are parsed.
        if (methodName.equals("<init>")) {
          continue; // Ignore constructors (?)
        }

        String fullyQualifiedMethodName = cls.className;
        String fullyQualifiedArgs =
            Signatures.arglistFromJvm(
                JVMLmethodSignature.substring(arglistStartIndex, arglistEndIndex));
        String fullyQualifiedSignature =
            fullyQualifiedMethodName + "." + methodName + fullyQualifiedArgs;

        if (pure) {
          pureMethods.add(fullyQualifiedSignature);
        }
        sideEffectFreeMethods.add(fullyQualifiedSignature);
      }
    }
  }
}
