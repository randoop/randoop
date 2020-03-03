package randoop.resource;

import static org.objectweb.asm.TypeReference.METHOD_RETURN;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.checkerframework.checker.signature.qual.BinaryName;
import org.checkerframework.checker.signature.qual.InternalForm;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;
import org.plumelib.reflection.Signatures;

/**
 * ClassAnnotationScanner is a ClassVisitor that collects methods that are annotated with specific
 * annotations.
 */
public class ClassAnnotationScanner extends ClassVisitor {

  /** The desired annotations: we will return methods that have one of these annotations. */
  private final Collection<String> desiredAnnotations;

  /** The binary name of the class being visited. */
  private @BinaryName String className = null;

  /** The accumulated set of captured fully qualified method signatures. */
  private final Set<String> matchingSignatures = new HashSet<>();

  /**
   * Creates a new AnnotationScanner to look for methods with the specified annotations.
   *
   * @param api API version for ASM
   * @param desiredAnnotations annotations to look for
   */
  public ClassAnnotationScanner(int api, Collection<String> desiredAnnotations) {
    super(api);
    this.desiredAnnotations = desiredAnnotations;
  }

  /**
   * Returns the Randoop fully qualified signature of a method. e.g. "java.util.String.hashCode()"
   *
   * @param method method name
   * @param argumentSignature signature of the method's arguments, e.g.
   *     "(char[],int,int,java.lang.String,int)"
   * @return Randoop fully qualified signature
   */
  private String getFullyQualifiedMethodSignature(String method, String argumentSignature) {
    return className + "." + method + argumentSignature;
  }

  /**
   * Adds a method to the set of captured methods if the given annotation is in {@code
   * desiredAnnotations}.
   *
   * @param annotationName annotation name as read by ASM, e.g.
   *     "org.checkerframework.checker.determinism.qual.NonDet"
   * @param method method name in Randoop fully-qualified signature format
   * @param argumentSignature signature of the method's arguments, e.g.
   *     "(char[],int,int,java.lang.String,int)"
   */
  private void addMethodIfMatching(String annotationName, String method, String argumentSignature) {
    if (desiredAnnotations.contains(Signatures.fieldDescriptorToBinaryName(annotationName))) {
      matchingSignatures.add(getFullyQualifiedMethodSignature(method, argumentSignature));
    }
  }

  @SuppressWarnings("signature")
  @Override
  public void visit(
      int version,
      int access,
      @InternalForm String name,
      String signature,
      String superName,
      String[] interfaces) {
    if (className == null) {
      // Then this is the outermost class we look at.
      className = Signatures.internalFormToBinaryName(name);
      super.visit(version, access, name, signature, superName, interfaces);
    } else {
      ClassAnnotationScanner nestedClassScanner =
          new ClassAnnotationScanner(api, desiredAnnotations);
      nestedClassScanner.visit(version, access, name, signature, superName, interfaces);
      matchingSignatures.addAll(nestedClassScanner.getMethodsWithDesiredAnnotations());
    }
  }

  /** MethodAnnotationScanner captures the methods that have our desired annotation. */
  class MethodAnnotationScanner extends MethodVisitor {
    /** Name of the visited method. */
    private final String methodName;

    /** Signature of the visited method's arguments. */
    private final String argumentSignature;

    /**
     * Creates a new MethodAnnotationScanner with ASM7.
     *
     * @param methodName name of the method currently visiting
     * @param argumentSignature signature of the method's arguments
     */
    MethodAnnotationScanner(String methodName, String argumentSignature) {
      super(Opcodes.ASM7);
      this.methodName = methodName;
      this.argumentSignature = argumentSignature;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      addMethodIfMatching(desc, methodName, argumentSignature);
      return super.visitAnnotation(desc, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        int typeRef, TypePath typePath, String descriptor, boolean visible) {

      // We only care about the return type annotations.
      if (new TypeReference(typeRef).getSort() == METHOD_RETURN) {
        addMethodIfMatching(descriptor, methodName, argumentSignature);
      }
      return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    return super.visitAnnotation(desc, visible);
  }

  @Override
  public MethodVisitor visitMethod(
      int access, String name, String desc, String signature, String[] exceptions) {
    // By default, the desc includes the return type.
    // We cut off the return type to pass it into the arglist JVM parser.
    String arglist = desc.substring(0, desc.indexOf(')') + 1);

    return new MethodAnnotationScanner(name, Signatures.arglistFromJvm(arglist));
  }

  /**
   * Retrieves the set of captured methods with the desired annotations.
   *
   * @return captured methods in Randoop fully qualified signature format
   */
  public Set<String> getMethodsWithDesiredAnnotations() {
    return matchingSignatures;
  }
}
