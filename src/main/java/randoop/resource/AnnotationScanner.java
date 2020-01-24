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
  private String currentMethod = null;
  private String argumentSignature = null;
  private final Set<String> matchingFullyQualifiedMethodSignatures;

  // Is there a way to link this?
  private boolean nonDetFlag = false;

  private String getCurrentFullyQualifiedMethodSignature() {
    return fullyQualifiedClassName + "." + currentMethod + argumentSignature;
  }

  private void addSefAnnotationIfMatching(String desc) {
    if (currentMethod.contains("<init>") || currentMethod.contains("<clinit>")) {
      return; // Ignore constructors and static constructors
    }

    if (desiredAnnotations.contains(Signatures.fieldDescriptorToBinaryName(desc.trim()))) {
      matchingFullyQualifiedMethodSignatures.add(getCurrentFullyQualifiedMethodSignature());
    }
  }

  private void flagNonDetAnnotationIfMatching(String desc) {
    if (desiredAnnotations.contains(Signatures.fieldDescriptorToBinaryName(desc.trim()))) {
      matchingFullyQualifiedMethodSignatures.add(getCurrentFullyQualifiedMethodSignature());
      nonDetFlag = true;
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
    fullyQualifiedClassName = name.replace('/', '.').replace('$', '.');
    System.out.println("class");

    super.visit(version, access, name, signature, superName, interfaces);
  }

  /**
   * MethodAnnotationScanner overrides the default annotation visitor behavior to capture the
   * methods that have our desired annotation.
   */
  class MethodAnnotationScanner extends MethodVisitor {
    MethodAnnotationScanner() {
      super(ASM_OPCODE);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      addSefAnnotationIfMatching(desc);
      return super.visitAnnotation(desc, visible);
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
    System.out.println(name);
    currentMethod = name;

    // By default, the desc includes the return type.
    // We cut off the return type to pass it into the arglist JVM parser.
    String arglist = desc.substring(0, desc.indexOf(')') + 1);
    argumentSignature = Signatures.arglistFromJvm(arglist);

    if (nonDetFlag) {
      nonDetFlag = false;
      if (!currentMethod.contains("<init>") && !currentMethod.contains("<clinit>")) {
        matchingFullyQualifiedMethodSignatures.add(getCurrentFullyQualifiedMethodSignature());
      }
    }

    return new MethodAnnotationScanner();
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(
      int typeRef, TypePath typePath, java.lang.String descriptor, boolean visible) {
    flagNonDetAnnotationIfMatching(descriptor);
    return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
  }

  public Set<String> getMethodsWithAnnotations() {
    return matchingFullyQualifiedMethodSignatures;
  }
}
