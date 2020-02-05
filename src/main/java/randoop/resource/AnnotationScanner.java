package randoop.resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.plumelib.reflection.Signatures;

public class AnnotationScanner extends ClassVisitor {
  private static final int ASM_OPCODE = Opcodes.ASM5;
  private final Collection<String> desiredAnnotations;
  private String fullyQualifiedClassName = null;
  private final Set<String> matchingFullyQualifiedMethodSignatures;

  private String getCurrentFullyQualifiedMethodSignature(String method, String argumentSignature) {
    return fullyQualifiedClassName + "." + method + argumentSignature;
  }

  private void addAnnotationIfMatching(String desc, String method, String argumentSignature) {
    if (method.contains("<init>") || method.contains("<clinit>")) {
      return; // Ignore constructors and static constructors
    }

    if (desiredAnnotations.contains(Signatures.fieldDescriptorToBinaryName(desc.trim()))) {
      matchingFullyQualifiedMethodSignatures.add(
          getCurrentFullyQualifiedMethodSignature(method, argumentSignature));
    }
  }

  @Override
  public void visit(
      int version,
      int access,
      java.lang.String name,
      java.lang.String signature,
      java.lang.String superName,
      java.lang.String[] interfaces) {
    fullyQualifiedClassName = name.replace('/', '.');

    super.visit(version, access, name, signature, superName, interfaces);
  }

  /**
   * MethodAnnotationScanner overrides the default annotation visitor behavior to capture the
   * methods that have our desired annotation.
   */
  class MethodAnnotationScanner extends MethodVisitor {
    private final String methodName, argumentSignature;

    MethodAnnotationScanner(String methodName, String argumentSignature) {
      super(ASM_OPCODE);
      this.methodName = methodName;
      this.argumentSignature = argumentSignature;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      addAnnotationIfMatching(desc, methodName, argumentSignature);
      return super.visitAnnotation(desc, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        int typeRef, TypePath typePath, java.lang.String descriptor, boolean visible) {
      addAnnotationIfMatching(descriptor, methodName, argumentSignature);
      return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }
  }

  /**
   * Initializes a new AnnotationScanner to look for methods with the specified annotations.
   *
   * @param api API version for ASM
   * @param desiredAnnotations annotations to look for
   */
  public AnnotationScanner(int api, Collection<String> desiredAnnotations) {
    super(api);
    this.desiredAnnotations = desiredAnnotations;
    this.matchingFullyQualifiedMethodSignatures = new HashSet<>();
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

  public Set<String> getMethodsWithAnnotations() {
    return matchingFullyQualifiedMethodSignatures;
  }
}
