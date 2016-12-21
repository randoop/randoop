package randoop.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.bcel6.Const;
import org.apache.commons.bcel6.classfile.ClassParser;
import org.apache.commons.bcel6.classfile.Constant;
import org.apache.commons.bcel6.classfile.ConstantClass;
import org.apache.commons.bcel6.classfile.ConstantDouble;
import org.apache.commons.bcel6.classfile.ConstantFieldref;
import org.apache.commons.bcel6.classfile.ConstantFloat;
import org.apache.commons.bcel6.classfile.ConstantInteger;
import org.apache.commons.bcel6.classfile.ConstantInterfaceMethodref;
import org.apache.commons.bcel6.classfile.ConstantLong;
import org.apache.commons.bcel6.classfile.ConstantMethodref;
import org.apache.commons.bcel6.classfile.ConstantNameAndType;
import org.apache.commons.bcel6.classfile.ConstantPool;
import org.apache.commons.bcel6.classfile.ConstantString;
import org.apache.commons.bcel6.classfile.ConstantUtf8;
import org.apache.commons.bcel6.classfile.JavaClass;
import org.apache.commons.bcel6.classfile.Method;
import org.apache.commons.bcel6.generic.ClassGen;
import org.apache.commons.bcel6.generic.ConstantPoolGen;
import org.apache.commons.bcel6.generic.ConstantPushInstruction;
import org.apache.commons.bcel6.generic.Instruction;
import org.apache.commons.bcel6.generic.InstructionList;
import org.apache.commons.bcel6.generic.MethodGen;
import org.apache.commons.bcel6.util.ClassPath;

import randoop.operation.NonreceiverTerm;
import randoop.types.JavaTypes;
import randoop.reflection.TypeNames;

// Implementation notes:  All string, float, and double constants are in the
// the constant table.  Integer constants less that 64K are in the code.
// There are also special opcodes to push values from -1 to 5.  This code
// does not include them, but it would be easy to add them.  This code also
// does not include class literals as constants.
// It would be possible to determine the method with the constant if you
// wanted finer-grained information about where the constants were used.

/**
 * Reads literals from a class file, including from the constant pool and from
 * bytecodes that take immediate arguments.
 */
public class ClassFileConstants {

  // Some test values when this class file is used as input.
  // Byte, int, short, and char values are all stored in the .class file as int.
  static byte bb = 23;
  static double d = 35.3;
  static float f = 3.0f;
  static int ii = 20;
  static long ll = 200000;
  static short s = 32000;
  static char c = 'a';

  public static class ConstantSet {
    public String classname;
    public Set<Integer> ints = new TreeSet<>();
    public Set<Long> longs = new TreeSet<>();
    public Set<Float> floats = new TreeSet<>();
    public Set<Double> doubles = new TreeSet<>();
    public Set<String> strings = new TreeSet<>();
    public Set<Class<?>> classes = new TreeSet<>();

    @Override
    public String toString() {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      System.out.printf("START CLASSLITERALS%n");
      System.out.printf("%nCLASSNAME%n%s%n%nLITERALS%n", classname);
      for (int x : ints) {
        System.out.printf("int:%d%n", x);
      }
      for (long x : longs) {
        System.out.printf("long:%d%n", x);
      }
      for (float x : floats) {
        System.out.printf("float:%g%n", x);
      }
      for (double x : doubles) {
        System.out.printf("double:%g%n", x);
      }
      for (String x : strings) {
        System.out.printf("String:\"%s\"%n", x);
      }
      for (Class<?> x : classes) {
        System.out.printf("Class:%s%n", x);
      }
      System.out.printf("%nEND CLASSLITERALS%n");

      return baos.toString();
    }
  }

  /**
   * A simple driver program that prints output literals file format.
   *
   * @see randoop.reflection.LiteralFileReader
   * @param args  the command line arguments
   * @throws IOException if an error occurs in writing the constants
   */
  public static void main(String[] args) throws IOException {
    for (String classname : args) {
      System.out.println(getConstants(classname));
    }
  }

  /**
   * Returns all the constants found in the given class.
   *
   * @see #getConstants(String,ConstantSet)
   * @param classname  the name of the type
   * @return the set of constants of the given type
   */
  public static ConstantSet getConstants(String classname) {
    ConstantSet result = new ConstantSet();
    getConstants(classname, result);
    return result;
  }

  /**
   * Adds all the constants found in the given class into the given ConstantSet,
   * and returns it.
   *
   * @see #getConstants(String)
   * @param classname  the name of the type
   * @param result  the set of constants to which constants are added
   * @return the set of constants with new constants of given type added
   */
  public static ConstantSet getConstants(String classname, ConstantSet result) {

    ClassParser cp;
    JavaClass jc;
    try {
      String classfileBase = classname.replace('.', '/');
      InputStream is = ClassPath.SYSTEM_CLASS_PATH.getInputStream(classfileBase, ".class");
      cp = new ClassParser(is, classname);
      jc = cp.parse();
    } catch (java.io.IOException e) {
      throw new Error("IOException while reading '" + classname + "': " + e.getMessage());
    }
    result.classname = jc.getClassName();

    // Get all of the constants from the pool
    ConstantPool constant_pool = jc.getConstantPool();
    for (Constant c : constant_pool.getConstantPool()) {
      // System.out.printf ("*Constant = %s%n", c);
      if (c == null
          || c instanceof ConstantClass
          || c instanceof ConstantFieldref
          || c instanceof ConstantInterfaceMethodref
          || c instanceof ConstantMethodref
          || c instanceof ConstantNameAndType
          || c instanceof ConstantUtf8) {
        continue;
      }
      if (c instanceof ConstantString) {
        result.strings.add((String) ((ConstantString) c).getConstantValue(constant_pool));
      } else if (c instanceof ConstantDouble) {
        result.doubles.add((Double) ((ConstantDouble) c).getConstantValue(constant_pool));
      } else if (c instanceof ConstantFloat) {
        result.floats.add((Float) ((ConstantFloat) c).getConstantValue(constant_pool));
      } else if (c instanceof ConstantInteger) {
        result.ints.add((Integer) ((ConstantInteger) c).getConstantValue(constant_pool));
      } else if (c instanceof ConstantLong) {
        result.longs.add((Long) ((ConstantLong) c).getConstantValue(constant_pool));
      } else {
        throw new RuntimeException("Unrecognized constant of type " + c.getClass() + ": " + c);
      }
    }

    ClassGen gen = new ClassGen(jc);
    ConstantPoolGen pool = gen.getConstantPool();

    // Process the code in each method looking for literals
    for (Method m : jc.getMethods()) {
      MethodGen mg = new MethodGen(m, jc.getClassName(), pool);
      InstructionList il = mg.getInstructionList();
      if (il != null) {
        for (Instruction inst : il.getInstructions()) {
          switch (inst.getOpcode()) {

              // Compare two objects, no literals
            case Const.IF_ACMPEQ:
            case Const.IF_ACMPNE:
              break;

              // These instructions compare the integer on the top of the stack
              // to zero. There are no literals here (except 0)
            case Const.IFEQ:
            case Const.IFNE:
            case Const.IFLT:
            case Const.IFGE:
            case Const.IFGT:
            case Const.IFLE:
              {
                break;
              }

              // InstanceOf pushes either 0 or 1 on the stack depending on
              // whether
              // the object on top of stack is of the specified type.
              // If were interested in class literals, this would be interesting
            case Const.INSTANCEOF:
              break;

              // Duplicates the item on the top of stack. No literal.
            case Const.DUP:
              {
                break;
              }

              // Duplicates the item on the top of the stack and inserts it 2
              // values down in the stack. No literals
            case Const.DUP_X1:
              {
                break;
              }

              // Duplicates either the top 2 category 1 values or a single
              // category 2 value and inserts it 2 or 3 values down on the
              // stack.
            case Const.DUP2_X1:
              {
                break;
              }

              // Duplicate either one category 2 value or two category 1 values.
            case Const.DUP2:
              {
                break;
              }

              // Dup the category 1 value on the top of the stack and insert it
              // either
              // two or three values down on the stack.
            case Const.DUP_X2:
              {
                break;
              }

            case Const.DUP2_X2:
              {
                break;
              }

              // Pop instructions discard the top of the stack.
            case Const.POP:
              {
                break;
              }

              // Pops either the top 2 category 1 values or a single category 2
              // value
              // from the top of the stack.
            case Const.POP2:
              {
                break;
              }

              // Swaps the two category 1 types on the top of the stack.
            case Const.SWAP:
              {
                break;
              }

              // Compares two integers on the stack
            case Const.IF_ICMPEQ:
            case Const.IF_ICMPGE:
            case Const.IF_ICMPGT:
            case Const.IF_ICMPLE:
            case Const.IF_ICMPLT:
            case Const.IF_ICMPNE:
              {
                break;
              }

              // Get the value of a field
            case Const.GETFIELD:
              {
                break;
              }

              // stores the top of stack into a field
            case Const.PUTFIELD:
              {
                break;
              }

              // Pushes the value of a static field on the stack
            case Const.GETSTATIC:
              {
                break;
              }

              // Pops a value off of the stack into a static field
            case Const.PUTSTATIC:
              {
                break;
              }

              // pushes a local onto the stack
            case Const.DLOAD:
            case Const.DLOAD_0:
            case Const.DLOAD_1:
            case Const.DLOAD_2:
            case Const.DLOAD_3:
            case Const.FLOAD:
            case Const.FLOAD_0:
            case Const.FLOAD_1:
            case Const.FLOAD_2:
            case Const.FLOAD_3:
            case Const.ILOAD:
            case Const.ILOAD_0:
            case Const.ILOAD_1:
            case Const.ILOAD_2:
            case Const.ILOAD_3:
            case Const.LLOAD:
            case Const.LLOAD_0:
            case Const.LLOAD_1:
            case Const.LLOAD_2:
            case Const.LLOAD_3:
              {
                break;
              }

              // Pops a value off of the stack into a local
            case Const.DSTORE:
            case Const.DSTORE_0:
            case Const.DSTORE_1:
            case Const.DSTORE_2:
            case Const.DSTORE_3:
            case Const.FSTORE:
            case Const.FSTORE_0:
            case Const.FSTORE_1:
            case Const.FSTORE_2:
            case Const.FSTORE_3:
            case Const.ISTORE:
            case Const.ISTORE_0:
            case Const.ISTORE_1:
            case Const.ISTORE_2:
            case Const.ISTORE_3:
            case Const.LSTORE:
            case Const.LSTORE_0:
            case Const.LSTORE_1:
            case Const.LSTORE_2:
            case Const.LSTORE_3:
              {
                break;
              }

              // Push a value from the runtime constant pool. We'll get these
              // values when processing the constant pool itself
            case Const.LDC:
            case Const.LDC_W:
            case Const.LDC2_W:
              {
                break;
              }

              // Push the length of an array on the stack
            case Const.ARRAYLENGTH:
              {
                break;
              }

              // Push small constants (-1..5) on the stack. These literals are
              // too common to bother mentioning
            case Const.DCONST_0:
            case Const.DCONST_1:
            case Const.FCONST_0:
            case Const.FCONST_1:
            case Const.FCONST_2:
            case Const.ICONST_0:
            case Const.ICONST_1:
            case Const.ICONST_2:
            case Const.ICONST_3:
            case Const.ICONST_4:
            case Const.ICONST_5:
            case Const.ICONST_M1:
            case Const.LCONST_0:
            case Const.LCONST_1:
              {
                break;
              }

            case Const.BIPUSH:
            case Const.SIPUSH:
              {
                ConstantPushInstruction cpi = (ConstantPushInstruction) inst;
                result.ints.add((Integer) cpi.getValue());
                break;
              }

              // Primitive Binary operators.
            case Const.DADD:
            case Const.DCMPG:
            case Const.DCMPL:
            case Const.DDIV:
            case Const.DMUL:
            case Const.DREM:
            case Const.DSUB:
            case Const.FADD:
            case Const.FCMPG:
            case Const.FCMPL:
            case Const.FDIV:
            case Const.FMUL:
            case Const.FREM:
            case Const.FSUB:
            case Const.IADD:
            case Const.IAND:
            case Const.IDIV:
            case Const.IMUL:
            case Const.IOR:
            case Const.IREM:
            case Const.ISHL:
            case Const.ISHR:
            case Const.ISUB:
            case Const.IUSHR:
            case Const.IXOR:
            case Const.LADD:
            case Const.LAND:
            case Const.LCMP:
            case Const.LDIV:
            case Const.LMUL:
            case Const.LOR:
            case Const.LREM:
            case Const.LSHL:
            case Const.LSHR:
            case Const.LSUB:
            case Const.LUSHR:
            case Const.LXOR:
              break;

            case Const.LOOKUPSWITCH:
            case Const.TABLESWITCH:
              break;

            case Const.ANEWARRAY:
            case Const.NEWARRAY:
              {
                break;
              }

            case Const.MULTIANEWARRAY:
              {
                break;
              }

              // push the value at an index in an array
            case Const.AALOAD:
            case Const.BALOAD:
            case Const.CALOAD:
            case Const.DALOAD:
            case Const.FALOAD:
            case Const.IALOAD:
            case Const.LALOAD:
            case Const.SALOAD:
              {
                break;
              }

              // Pop the top of stack into an array location
            case Const.AASTORE:
            case Const.BASTORE:
            case Const.CASTORE:
            case Const.DASTORE:
            case Const.FASTORE:
            case Const.IASTORE:
            case Const.LASTORE:
            case Const.SASTORE:
              break;

            case Const.ARETURN:
            case Const.DRETURN:
            case Const.FRETURN:
            case Const.IRETURN:
            case Const.LRETURN:
            case Const.RETURN:
              {
                break;
              }

              // subroutine calls.
            case Const.INVOKESTATIC:
            case Const.INVOKEVIRTUAL:
            case Const.INVOKESPECIAL:
            case Const.INVOKEINTERFACE:
              break;

              // Throws an exception.
            case Const.ATHROW:
              break;

              // Opcodes that don't need any modifications. Here for reference
            case Const.ACONST_NULL:
            case Const.ALOAD:
            case Const.ALOAD_0:
            case Const.ALOAD_1:
            case Const.ALOAD_2:
            case Const.ALOAD_3:
            case Const.ASTORE:
            case Const.ASTORE_0:
            case Const.ASTORE_1:
            case Const.ASTORE_2:
            case Const.ASTORE_3:
            case Const.CHECKCAST:
            case Const.D2F: // double to float
            case Const.D2I: // double to integer
            case Const.D2L: // double to long
            case Const.DNEG: // Negate double on top of stack
            case Const.F2D: // float to double
            case Const.F2I: // float to integer
            case Const.F2L: // float to long
            case Const.FNEG: // Negate float on top of stack
            case Const.GOTO:
            case Const.GOTO_W:
            case Const.I2B: // integer to byte
            case Const.I2C: // integer to char
            case Const.I2D: // integer to double
            case Const.I2F: // integer to float
            case Const.I2L: // integer to long
            case Const.I2S: // integer to short
            case Const.IFNONNULL:
            case Const.IFNULL:
            case Const.IINC: // increment local variable by a constant
            case Const.INEG: // negate integer on top of stack
            case Const.JSR: // pushes return address on the stack,
            case Const.JSR_W:
            case Const.L2D: // long to double
            case Const.L2F: // long to float
            case Const.L2I: // long to int
            case Const.LNEG: // negate long on top of stack
            case Const.MONITORENTER:
            case Const.MONITOREXIT:
            case Const.NEW:
            case Const.NOP:
            case Const.RET: // this is the internal JSR return
              break;

              // Make sure we didn't miss anything
            default:
              throw new Error("instruction " + inst + " unsupported");
          }
        }
      }
    }
    return result;
  }

  /**
   * Convert a collection of ConstantSets to the format expected by
   * GenTest.addClassLiterals.
   * @param constantSets  the sets of constantSets
   * @return a map of types to constant operations
   */
  public static MultiMap<Class<?>, NonreceiverTerm> toMap(Collection<ConstantSet> constantSets) {
    final MultiMap<Class<?>, NonreceiverTerm> map = new MultiMap<>();
    for (ConstantSet cs : constantSets) {
      Class<?> clazz;
      try {
        clazz = TypeNames.getTypeForName(cs.classname);
      } catch (ClassNotFoundException e) {
        throw new Error("Class " + cs.classname + " not found on the classpath.");
      }
      for (Integer x : cs.ints) {
        map.add(clazz, new NonreceiverTerm(JavaTypes.INT_TYPE, x));
      }
      for (Long x : cs.longs) {
        map.add(clazz, new NonreceiverTerm(JavaTypes.LONG_TYPE, x));
      }
      for (Float x : cs.floats) {
        map.add(clazz, new NonreceiverTerm(JavaTypes.FLOAT_TYPE, x));
      }
      for (Double x : cs.doubles) {
        map.add(clazz, new NonreceiverTerm(JavaTypes.DOUBLE_TYPE, x));
      }
      for (String x : cs.strings) {
        map.add(clazz, new NonreceiverTerm(JavaTypes.STRING_TYPE, x));
      }
      for (Class<?> x : cs.classes) {
        map.add(clazz, new NonreceiverTerm(JavaTypes.CLASS_TYPE, x));
      }
    }
    return map;
  }
}
