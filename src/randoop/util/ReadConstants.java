package randoop.util;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.*;

public class ReadConstants {

  // Some test values when this class file is used as input
  static int ii = 20;
  static long ll = 200000;
  static float f = 3.0f;
  static double d = 35.3;

  public static void main (String args[]) throws java.io.IOException {

    ClassParser cp = new ClassParser(args[0]);
    JavaClass jc = cp.parse();

    // Get all of the constants from the pool
    ConstantPool constant_pool = jc.getConstantPool();
    for (Constant c : constant_pool.getConstantPool()) {
      // System.out.printf ("*Constant = %s%n", c);
      if (c instanceof ConstantString) {
        System.out.printf ("constant string = %s%n", 
                           constant_pool.constantToString (c));
      } else if (c instanceof ConstantDouble) {
        System.out.printf ("constant double = %s%n", 
                           constant_pool.constantToString (c));
      } else if (c instanceof ConstantFloat) {
        System.out.printf ("constant float = %s%n", 
                           constant_pool.constantToString (c));
      } else if (c instanceof ConstantInteger) {
        System.out.printf ("constant Integer = %s%n", 
                           constant_pool.constantToString (c));
      } else if (c instanceof ConstantLong) {
        System.out.printf ("constant long = %s%n", 
                           constant_pool.constantToString (c));
      }
    }

    ClassGen gen = new ClassGen (jc);
    ConstantPoolGen pool = gen.getConstantPool();

    // Process the code in each methods looking for literals
    for (Method m : jc.getMethods()) {
      MethodGen mg = new MethodGen (m, jc.getClassName(), pool);
      InstructionList il = mg.getInstructionList();
      for (Instruction inst : il.getInstructions()) {
        switch (inst.getOpcode()) {

        // Compare two objects, no literals
        case Constants.IF_ACMPEQ:
        case Constants.IF_ACMPNE:
          break;

        // These instructions compare the integer on the top of the stack
        // to zero.  There are no literals here (except 0)
        case Constants.IFEQ:
        case Constants.IFNE:
        case Constants.IFLT:
        case Constants.IFGE:
        case Constants.IFGT:
        case Constants.IFLE: {
          break;
        }

        // Instanceof pushes either 0 or 1 on the stack depending on whether
        // the object on top of stack is of the specified type.  
        // If were interested in class literals, this would be interesting
        case Constants.INSTANCEOF:
          break;

        // Duplicates the item on the top of stack.  No literal.
        case Constants.DUP: {
          break;
        }

        // Duplicates the item on the top of the stack and inserts it 2
        // values down in the stack.  No literals
        case Constants.DUP_X1: {
          break;
        }

        // Duplicates either the top 2 category 1 values or a single
        // category 2 value and inserts it 2 or 3 values down on the
        // stack.
        case Constants.DUP2_X1: {
          break;
        }

        // Duplicate either one category 2 value or two category 1 values.
        case Constants.DUP2: {
          break;
        }

        // Dup the category 1 value on the top of the stack and insert it either
        // two or three values down on the stack.
        case Constants.DUP_X2: {
          break;
        }

        case Constants.DUP2_X2: {
          break;
        }

        // Pop instructions discard the top of the stack.  
        case Constants.POP: {
          break;
        }

        // Pops either the top 2 category 1 values or a single category 2 value
        // from the top of the stack.  
        case Constants.POP2: {
          break;
        }

        // Swaps the two category 1 types on the top of the stack.  
        case Constants.SWAP: {
          break;
        }

        // Compares two integers on the stack
        case Constants.IF_ICMPEQ:
        case Constants.IF_ICMPGE:
        case Constants.IF_ICMPGT:
        case Constants.IF_ICMPLE:
        case Constants.IF_ICMPLT:
        case Constants.IF_ICMPNE: {
          break;
        }

        // Get the value of a field
        case Constants.GETFIELD: {
          break;
        }
          
        // stores the top of stack into a field
        case Constants.PUTFIELD: {
          break;
        }

        // Pushes the value of a static field on the stack
        case Constants.GETSTATIC: {
          break;
        }

        // Pops a value off of the stack into a static field 
        case Constants.PUTSTATIC: {
          break;
        }

        // pushes a local onto the stack
        case Constants.DLOAD:
        case Constants.DLOAD_0:
        case Constants.DLOAD_1:
        case Constants.DLOAD_2:
        case Constants.DLOAD_3:
        case Constants.FLOAD:
        case Constants.FLOAD_0:
        case Constants.FLOAD_1:
        case Constants.FLOAD_2:
        case Constants.FLOAD_3:
        case Constants.ILOAD:
        case Constants.ILOAD_0:
        case Constants.ILOAD_1:
        case Constants.ILOAD_2:
        case Constants.ILOAD_3:
        case Constants.LLOAD:
        case Constants.LLOAD_0:
        case Constants.LLOAD_1:
        case Constants.LLOAD_2:
        case Constants.LLOAD_3: {
          break;
        }

        // Pops a value off of the stack into a local
        case Constants.DSTORE:
        case Constants.DSTORE_0:
        case Constants.DSTORE_1:
        case Constants.DSTORE_2:
        case Constants.DSTORE_3:
        case Constants.FSTORE:
        case Constants.FSTORE_0:
        case Constants.FSTORE_1:
        case Constants.FSTORE_2:
        case Constants.FSTORE_3:
        case Constants.ISTORE:
        case Constants.ISTORE_0:
        case Constants.ISTORE_1:
        case Constants.ISTORE_2:
        case Constants.ISTORE_3:
        case Constants.LSTORE:
        case Constants.LSTORE_0:
        case Constants.LSTORE_1:
        case Constants.LSTORE_2:
        case Constants.LSTORE_3: {
          break;
        }

        // Push a value from the runtime constant pool.  We'll get these
        // values when processing the constant pool itself
        case Constants.LDC:
        case Constants.LDC_W:
        case Constants.LDC2_W: {
          break;
        }

        // Push the length of an array on the stack
        case Constants.ARRAYLENGTH: {
          break;
        }

        // Push small constants (-1..5) on the stack.  These literals are
        // too common to bother mentioning
        case Constants.DCONST_0:
        case Constants.DCONST_1:
        case Constants.FCONST_0:
        case Constants.FCONST_1:
        case Constants.FCONST_2:
        case Constants.ICONST_0:
        case Constants.ICONST_1:
        case Constants.ICONST_2:
        case Constants.ICONST_3:
        case Constants.ICONST_4:
        case Constants.ICONST_5:
        case Constants.ICONST_M1:
        case Constants.LCONST_0:
        case Constants.LCONST_1: {
          break;
        }

        case Constants.BIPUSH: 
        case Constants.SIPUSH: {
          ConstantPushInstruction cpi = (ConstantPushInstruction) inst;
          System.out.printf ("integer constant = %s%n", cpi.getValue());
          break;
        }

        // Primitive Binary operators.  
        case Constants.DADD:
        case Constants.DCMPG:
        case Constants.DCMPL:
        case Constants.DDIV:
        case Constants.DMUL:
        case Constants.DREM:
        case Constants.DSUB:
        case Constants.FADD:
        case Constants.FCMPG:
        case Constants.FCMPL:
        case Constants.FDIV:
        case Constants.FMUL:
        case Constants.FREM:
        case Constants.FSUB:
        case Constants.IADD:
        case Constants.IAND:
        case Constants.IDIV:
        case Constants.IMUL:
        case Constants.IOR:
        case Constants.IREM:
        case Constants.ISHL:
        case Constants.ISHR:
        case Constants.ISUB:
        case Constants.IUSHR:
        case Constants.IXOR:
        case Constants.LADD:
        case Constants.LAND:
        case Constants.LCMP:
        case Constants.LDIV:
        case Constants.LMUL:
        case Constants.LOR:
        case Constants.LREM:
        case Constants.LSHL:
        case Constants.LSHR:
        case Constants.LSUB:
        case Constants.LUSHR:
        case Constants.LXOR:
          break;

        case Constants.LOOKUPSWITCH:
        case Constants.TABLESWITCH:
          break;

        case Constants.ANEWARRAY:
        case Constants.NEWARRAY: {
          break;
        }

        case Constants.MULTIANEWARRAY: {
          break;
        }

        // push the value at an index in an array
        case Constants.AALOAD:
        case Constants.BALOAD:
        case Constants.CALOAD:
        case Constants.DALOAD:
        case Constants.FALOAD:
        case Constants.IALOAD:
        case Constants.LALOAD:
        case Constants.SALOAD: {
          break;
        }

        // Pop the top of stack into an array location
        case Constants.AASTORE:
        case Constants.BASTORE:
        case Constants.CASTORE:
        case Constants.DASTORE:
        case Constants.FASTORE:
        case Constants.IASTORE:
        case Constants.LASTORE:
        case Constants.SASTORE:
          break;

        case Constants.ARETURN:
        case Constants.DRETURN:
        case Constants.FRETURN:
        case Constants.IRETURN:
        case Constants.LRETURN:
        case Constants.RETURN: {
          break;
        }

        // subroutine calls.  
        case Constants.INVOKESTATIC:
        case Constants.INVOKEVIRTUAL:
        case Constants.INVOKESPECIAL:
        case Constants.INVOKEINTERFACE:
          break;

        // Throws an exception.  
        case Constants.ATHROW:
          break;

        // Opcodes that don't need any modifications.  Here for reference
        case Constants.ACONST_NULL:
        case Constants.ALOAD:
        case Constants.ALOAD_0:
        case Constants.ALOAD_1:
        case Constants.ALOAD_2:
        case Constants.ALOAD_3:
        case Constants.ASTORE:
        case Constants.ASTORE_0:
        case Constants.ASTORE_1:
        case Constants.ASTORE_2:
        case Constants.ASTORE_3:
        case Constants.CHECKCAST:
        case Constants.D2F:     // double to float
        case Constants.D2I:     // double to integer
        case Constants.D2L:     // double to long
        case Constants.DNEG:    // Negate double on top of stack
        case Constants.F2D:     // float to double
        case Constants.F2I:     // float to integer
        case Constants.F2L:     // float to long
        case Constants.FNEG:    // Negate float on top of stack
        case Constants.GOTO:
        case Constants.GOTO_W:
        case Constants.I2B:     // integer to byte
        case Constants.I2C:     // integer to char
        case Constants.I2D:     // integer to double
        case Constants.I2F:     // integer to float
        case Constants.I2L:     // integer to long
        case Constants.I2S:     // integer to short
        case Constants.IFNONNULL:
        case Constants.IFNULL:
        case Constants.IINC:    // increment local variable by a constant
        case Constants.INEG:    // negate integer on top of stack
        case Constants.JSR:     // pushes return address on the stack, 
        case Constants.JSR_W:
        case Constants.L2D:     // long to double
        case Constants.L2F:     // long to float
        case Constants.L2I:     // long to int
        case Constants.LNEG:    // negate long on top of stack
        case Constants.MONITORENTER:
        case Constants.MONITOREXIT:
        case Constants.NEW:
        case Constants.NOP:
        case Constants.RET:     // this is the internal JSR return
          break;

        // Make sure we didn't miss anything
        default:
          throw new Error("instruction " + inst + " unsupported");
        }
      }
    }
  }
}
