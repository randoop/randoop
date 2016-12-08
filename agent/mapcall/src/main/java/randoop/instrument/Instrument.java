package randoop.instrument;

import static java.lang.System.out;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.bcel6.Const;
import org.apache.commons.bcel6.classfile.Attribute;
import org.apache.commons.bcel6.classfile.ClassParser;
import org.apache.commons.bcel6.classfile.Constant;
import org.apache.commons.bcel6.classfile.ConstantUtf8;
import org.apache.commons.bcel6.classfile.JavaClass;
import org.apache.commons.bcel6.classfile.Method;
import org.apache.commons.bcel6.generic.ClassGen;
import org.apache.commons.bcel6.generic.CodeExceptionGen;
import org.apache.commons.bcel6.generic.ConstantPoolGen;
import org.apache.commons.bcel6.generic.INVOKESTATIC;
import org.apache.commons.bcel6.generic.INVOKEVIRTUAL;
import org.apache.commons.bcel6.generic.Instruction;
import org.apache.commons.bcel6.generic.InstructionFactory;
import org.apache.commons.bcel6.generic.InstructionHandle;
import org.apache.commons.bcel6.generic.InstructionList;
import org.apache.commons.bcel6.generic.InstructionTargeter;
import org.apache.commons.bcel6.generic.LineNumberGen;
import org.apache.commons.bcel6.generic.LocalVariableGen;
import org.apache.commons.bcel6.generic.MethodGen;
import org.apache.commons.bcel6.generic.Type;
import plume.ArraysMDE;
import plume.BCELUtil;
import plume.SimpleLog;
import plume.StrTok;
import plume.UtilMDE;

/**
 * The Instrument class is responsible for modifying another class's bytecode. It changes calls to
 * specified functions into static calls to user functions. This allows a user of Randoop to capture
 * arbitrary calls and modify them as desired. For example, all calls to
 * JOptionPane.showConfirmDialog (which would otherwise hang a regressions test) can be changed to
 * throw an exception, or simply print a message and continue.
 */
public class Instrument implements ClassFileTransformer {

  boolean debug = false;
  boolean log_on = false;
  boolean debug_class = false;

  /** current Constant Pool */
  static ConstantPoolGen pgen = null;

  protected InstructionFactory ifact;

  /** Debug information about which classes are transformed and why */
  public static SimpleLog debug_transform = new SimpleLog(false);

  public static SimpleLog debug_instrument = new SimpleLog(false);
  public static SimpleLog debug_instrument_inst = new SimpleLog(false);

  /** Debug information on method maping */
  public static SimpleLog debug_map = new SimpleLog("method_mapping.txt", true);

  /** Class that defines a method (by its fully-qualified name and argument types) */
  static class MethodDef {
    /** Fully-qualified method name, such as "javax.swing.JOptionPane.showMessageDialog" */
    String name;
    /** The argument types */
    Type[] arg_types;

    MethodDef(String name, Type[] arg_types) {
      this.name = name;
      this.arg_types = arg_types;
    }

    boolean equals(String name, Type[] arg_types) {
      if (!name.equals(this.name)) return false;
      if (this.arg_types.length != arg_types.length) return false;
      for (int ii = 0; ii < arg_types.length; ii++)
        if (!arg_types[ii].equals(this.arg_types[ii])) return (false);
      return (true);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof MethodDef)) return false;
      MethodDef md = (MethodDef) obj;
      return equals(md.name, md.arg_types);
    }

    @Override
    public int hashCode() {
      int code = name.hashCode();
      for (Type arg : arg_types) code += arg.hashCode();
      return code;
    }

    @Override
    public String toString() {
      return String.format("%s (%s)", name, UtilMDE.join(arg_types, ", "));
    }
  }

  /**
   * Wrapper class that gives the fully-qualified class name of the class that contains a
   * replacement call, and counts the number of replacements performed.
   */
  private static class ReplacementClass {
    /** Fully-qualified class name. */
    String method_class;
    /** The number of times that the replacement has been performed so far. */
    int cnt;

    ReplacementClass(String method_class) {
      this.method_class = method_class;
      this.cnt = 0;
    }
  }

  /**
   * Represents one block in a map_calls file:
   *
   * <ul>
   *   <li>the classes in which to do the replacement
   *   <li>a method map, indicating for each old method the class containing its replacement
   * </ul>
   */
  private static class MethodMapInfo {
    /** Classes in which to do the replacement */
    Pattern class_regex;
    /** The method map: for each method, what class contains the replacement method? */
    Map<MethodDef, ReplacementClass> map;

    MethodMapInfo(Pattern class_regex, Map<MethodDef, ReplacementClass> map) {
      this.class_regex = class_regex;
      this.map = map;
    }
  }

  /**
   * Represents all method replacements the user has requested. Another way of viewing this is that
   * it represents a complete map_calls file. Such a file contains a set of MethodMapInfos, each of
   * which specifies some replacements.
   */
  public static List<MethodMapInfo> map_calls_file = new ArrayList<>();

  public Instrument() {}

  /**
   * {@inheritDoc}
   *
   * <p>Transforms class by replacing calls to methods with corresponding calls defined in this
   * class.
   */
  @Override
  public byte[] transform(
      ClassLoader loader,
      String className,
      Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain,
      byte[] classfileBuffer)
      throws IllegalClassFormatException {

    // debug = className.equals ("chicory/Test");

    String fullClassName = className.replace("/", ".");

    debug_transform.log("In Transform: class = %s%n", className);

    // Don't instrument boot classes. We only want to instrument
    // user classes classpath.
    // Most boot classes have the null loader,
    // but some generated classes (such as those in sun.reflect) will
    // have a non-null loader. Some of these have a null parent loader,
    // but some do not. The check for the sun.reflect package is a hack
    // to catch all of these. A more consistent mechanism to determine
    // boot classes would be preferrable.
    if (loader == null) {
      debug_transform.log("ignoring system class %s, class loader == null", fullClassName);
      return (null);
    } else if (loader.getParent() == null) {
      debug_transform.log("ignoring system class %s, parent loader == null\n", fullClassName);
      return (null);
    } else if (fullClassName.startsWith("sun.reflect")) {
      debug_transform.log("ignoring system class %s, in sun.reflect package", fullClassName);
      return (null);
    } else if (fullClassName.startsWith("com.sun")) {
      System.out.printf("Class from com.sun package %s with nonnull loaders\n", fullClassName);
    }

    // Don't intrument our code
    if (className.startsWith("randoop.")) {
      debug_transform.log("Not considering randoop class %s%n", fullClassName);
      return (null);
    }

    // Look for match with specified regular expressions for class
    Map<MethodDef, ReplacementClass> current_method_map = null;
    debug_class = false;
    for (MethodMapInfo mmi : map_calls_file) {
      if (mmi.class_regex.matcher(className).matches()) {
        if (false && className.startsWith("RandoopTest")) debug_class = true;
        if (debug_class)
          System.out.printf("Classname %s matches re %s%n", className, mmi.class_regex);
        current_method_map = mmi.map;
        break;
      }
    }
    if (current_method_map == null) {
      // no replacements to be done in this class
      return null;
    }

    debug_transform.log(
        "transforming class %s, loader %s - %s%n", className, loader, loader.getParent());

    // Parse the bytes of the classfile, die on any errors
    JavaClass c;
    try {
      ClassParser parser = new ClassParser(new ByteArrayInputStream(classfileBuffer), className);
      c = parser.parse();
    } catch (Exception e) {
      throw new RuntimeException("Unexpected error", e);
    }

    try {
      // Get the class information
      ClassGen cg = new ClassGen(c);
      ifact = new InstructionFactory(cg);

      map_calls(cg, className, loader, current_method_map);

      JavaClass njc = cg.getJavaClass();
      if (debug) njc.dump("/tmp/ret/" + njc.getClassName() + ".class");

      if (true) {
        return (cg.getJavaClass().getBytes());
      } else {
        debug_transform.log("not including class %s (filtered out)", className);
        return null;
      }

    } catch (Throwable e) {
      out.format("Unexpected error %s in transform", e);
      e.printStackTrace();
      return (null);
    }
  }

  /**
   * Processes each method in cg replacing any specified calls with static user calls.
   *
   * @param fullClassName must be packageName.className
   */
  private boolean map_calls(
      ClassGen cg,
      String fullClassName,
      ClassLoader loader,
      Map<MethodDef, ReplacementClass> current_method_map) {

    boolean transformed = false;

    try {
      pgen = cg.getConstantPool();

      // Loop through each method in the class
      Method[] methods = cg.getMethods();
      for (int i = 0; i < methods.length; i++) {
        MethodGen mg = new MethodGen(methods[i], cg.getClassName(), pgen);

        // Get the instruction list and skip methods with no instructions
        InstructionList il = mg.getInstructionList();
        if (il == null) continue;

        if (debug) out.format("Original code: %s%n", mg.getMethod().getCode());

        instrument_method(methods[i], mg, current_method_map);

        // Remove the Local variable type table attribute (if any).
        // Evidently, some changes we make require this to be updated, but
        // without BCEL support, that would be hard to do. Just delete it
        // for now (since it is optional, and we are unlikely to be used by
        // a debugger)
        for (Attribute a : mg.getCodeAttributes()) {
          if (is_local_variable_type_table(a)) {
            mg.removeCodeAttribute(a);
          }
        }

        // Update the instruction list
        mg.setInstructionList(il);
        mg.update();

        // Update the max stack and Max Locals
        mg.setMaxLocals();
        mg.setMaxStack();
        mg.update();

        // Update the method in the class
        cg.replaceMethod(methods[i], mg.getMethod());
        if (debug) out.format("Modified code: %s%n", mg.getMethod().getCode());

        // verify the new method
        // StackVer stackver = new StackVer();
        // VerificationResult vr = stackver.do_stack_ver (mg);
        // log ("vr for method %s = %s%n", mg.getName(), vr);
        // if (vr.getStatus() != VerificationResult.VERIFIED_OK) {
        // System.out.printf ("Warning BCEL Verify failed for method %s: %s",
        // mg.getName(), vr);
        // System.out.printf ("Code: %n%s%n", mg.getMethod().getCode());
        // System.exit(1);
        // }
      }

      cg.update();
    } catch (Exception e) {
      out.format("Unexpected exception encountered: " + e);
      e.printStackTrace();
    }

    return transformed;
  }

  /**
   * Instrument the specified method to replace mapped calls.
   *
   * @param m the method to transform
   * @param mg the method generator
   */
  public void instrument_method(
      Method m, MethodGen mg, Map<MethodDef, ReplacementClass> current_method_map) {

    // Loop through each instruction, making substitutions
    InstructionList il = mg.getInstructionList();
    for (InstructionHandle ih = il.getStart(); ih != null; ) {
      if (debug_instrument_inst.enabled()) {
        debug_instrument_inst.log("instrumenting instruction %s%n", ih);
        // ih.getInstruction().toString(pool.getConstantPool()));
      }

      // Remember the next instruction to process
      InstructionHandle next_ih = ih.getNext();

      // Get the translation for this instruction (if any)
      InstructionList new_il = xform_inst(mg, ih.getInstruction(), current_method_map);
      if (debug_instrument_inst.enabled()) debug_instrument_inst.log("  new inst: %s%n", new_il);

      // If this instruction was modified, replace it with the new
      // instruction list. If this instruction was the target of any
      // jumps or line numbers, replace them with the first
      // instruction in the new list.
      replace_instructions(il, ih, new_il);

      ih = next_ih;
    }
  }

  /**
   * Transforms invoke instructions that match the specified list for this class to call the
   * specified static call instead.
   */
  private InstructionList xform_inst(
      MethodGen mg, Instruction inst, Map<MethodDef, ReplacementClass> current_method_map) {

    switch (inst.getOpcode()) {
      case Const.INVOKESTATIC:
        {
          InstructionList il = new InstructionList();
          INVOKESTATIC is = (INVOKESTATIC) inst;
          String cname = is.getClassName(pgen);
          String mname = is.getMethodName(pgen);
          Type[] args = is.getArgumentTypes(pgen);
          MethodDef orig = new MethodDef(cname + "." + mname, args);
          ReplacementClass call = current_method_map.get(orig);
          if (call != null) {
            call.cnt++;
            String classname = call.method_class;
            String methodname = mname;
            debug_map.log(
                "%s.%s: Replacing method %s.%s (%s) with %s.%s%n",
                mg.getClassName(),
                mg.getName(),
                cname,
                mname,
                UtilMDE.join(args, ", "),
                classname,
                methodname);
            il.append(
                ifact.createInvoke(
                    classname, methodname, is.getReturnType(pgen), args, Const.INVOKESTATIC));
          }
          return (il);
        }

      case Const.INVOKEVIRTUAL:
        {
          InstructionList il = new InstructionList();
          INVOKEVIRTUAL iv = (INVOKEVIRTUAL) inst;
          String cname = iv.getClassName(pgen);
          String mname = iv.getMethodName(pgen);
          Type[] args = iv.getArgumentTypes(pgen);
          Type instance_type = iv.getReferenceType(pgen);
          Type[] new_args = BCELUtil.insert_type(instance_type, args);
          MethodDef orig = new MethodDef(cname + "." + mname, args);
          if (debug_class)
            System.out.printf("looking for %s in map %s%n", orig, current_method_map);
          ReplacementClass call = current_method_map.get(orig);
          if (call != null) {
            call.cnt++;
            String classname = call.method_class;
            String methodname = mname;
            debug_map.log(
                "Replacing method %s.%s (%s) with %s.%s%n",
                cname,
                mname,
                ArraysMDE.toString(args),
                classname,
                methodname);
            il.append(
                ifact.createInvoke(
                    classname, methodname, iv.getReturnType(pgen), new_args, Const.INVOKESTATIC));
          }
          return (il);
        }

      default:
        return (null);
    }
  }

  /**
   * Replace instruction ih in list il with the instructions in new_il. If new_il is null, do
   * nothing.
   *
   * @param il the instruction list
   * @param ih the instruction
   * @param new_il the new instructions to substitute
   */
  protected static void replace_instructions(
      InstructionList il, InstructionHandle ih, InstructionList new_il) {

    if ((new_il == null) || new_il.isEmpty()) {
      return;
    }

    // If there is only one new instruction, just replace it in the handle
    if (new_il.getLength() == 1) {
      ih.setInstruction(new_il.getEnd().getInstruction());
      return;
    }

    // Get the start and end instruction of the new instructions
    InstructionHandle new_end = new_il.getEnd();
    InstructionHandle new_start = il.insert(ih, new_il);

    // Move all of the branches from the old instruction to the new start
    il.redirectBranches(ih, new_start);

    // Move other targets to the new instuctions.
    if (ih.hasTargeters()) {
      for (InstructionTargeter it : ih.getTargeters()) {
        if (it instanceof LineNumberGen) {
          it.updateTarget(ih, new_start);
        } else if (it instanceof LocalVariableGen) {
          it.updateTarget(ih, new_end);
        } else if (it instanceof CodeExceptionGen) {
          CodeExceptionGen exc = (CodeExceptionGen) it;
          if (exc.getStartPC() == ih) exc.updateTarget(ih, new_start);
          else if (exc.getEndPC() == ih) exc.updateTarget(ih, new_end);
          else if (exc.getHandlerPC() == ih) exc.setHandlerPC(new_start);
          else System.out.printf("Malformed CodeException: %s%n", exc);
        } else {
          System.out.printf("unexpected target %s%n", it);
        }
      }
    }

    // Remove the old handle. There should be no targeters left to it.
    try {
      il.delete(ih);
    } catch (Exception e) {
      throw new Error("Can't delete instruction", e);
    }
  }

  /**
   * Returns true iff mgen is a constructor
   *
   * @return true iff mgen is a constructor
   */
  private boolean is_constructor(MethodGen mgen) {

    if (mgen.getName().equals("<init>") || mgen.getName().equals("")) {
      // log ("method '%s' is a constructor%n", mgen.getName());
      return (true);
    } else return (false);
  }

  public boolean is_local_variable_type_table(Attribute a) {
    return (get_attribute_name(a).equals("LocalVariableTypeTable"));
  }

  /**
   * Returns the attribute name for the specified attribute.
   *
   * @param a the attribute
   * @return the name for the attribute
   */
  public String get_attribute_name(Attribute a) {

    int con_index = a.getNameIndex();
    Constant c = pgen.getConstant(con_index);
    String att_name = ((ConstantUtf8) c).getBytes();
    return (att_name);
  }

  /** Class that reports tokenizing errors from the map file. */
  static class MapFileErrorHandler extends StrTok.ErrorHandler {
    LineNumberReader lr;
    File map_file;

    public MapFileErrorHandler(LineNumberReader lr, File map_file) {
      this.lr = lr;
      this.map_file = map_file;
    }

    @Override
    public void tok_error(String s) {
      throw new RuntimeException(
          String.format("Error on line %d of %s: %s", lr.getLineNumber(), map_file, s));
    }
  }

  /**
   * Parse a method declaration. The declaration should be in the following format:
   *
   * <p>fully-qualified-method-name (args)
   *
   * <p>where the arguments are comma separated and all arguments other than primitives should have
   * fully-qualified names. Arrays are indicating by trailing brackets. For example:
   *
   * <p>int int[] int[][] java.lang.String java.util.Date[]
   *
   * <p>The arguments are translated into BCEL types and a MethodDef is returned.
   */
  private MethodDef parse_method(StrTok st) {

    // Get the method name
    String method_name = st.need_word();

    // Get the opening paren
    st.need("(");

    // Read the arguments
    ArrayList<String> args = new ArrayList<>();
    String tok = st.nextToken();
    if (tok != ")") { // interned
      st.pushBack();
      do {
        tok = st.need_word();
        args.add(tok);
      } while (st.nextToken() == ","); // interned
      st.pushBack();
      st.need(")");
    }

    // Convert the arguments to Type
    Type[] targs = new Type[args.size()];
    for (int ii = 0; ii < args.size(); ii++) {
      targs[ii] = BCELUtil.classname_to_type(args.get(ii));
    }

    return new MethodDef(method_name, targs);
  }

  /**
   * Reads the file that specifies method calls that should be replaced by other method calls. The
   * file is of the form:
   *
   * <p>[regex] [orig-method-def] [new-method-name]
   *
   * <p>where the orig_method-def is of the form:
   *
   * <p>fully-qualified-method-name (args)
   *
   * <p>Blank lines and // comments are ignored. The orig-method-def is replaced by a call to
   * new-method-name with the same arguments in any classfile that matches the regular expressions.
   * All method names and argument types should be fully-qualified.
   *
   * @param map_file the file with map of method substitutions
   * @throws IOException if file has missing regex
   */
  public void read_map_file(File map_file) throws IOException {

    LineNumberReader lr = new LineNumberReader(new FileReader(map_file));
    MapFileErrorHandler mfeh = new MapFileErrorHandler(lr, map_file);
    Pattern current_regex = null;
    Map<MethodDef, ReplacementClass> map = new LinkedHashMap<>();
    for (String line = lr.readLine(); line != null; line = lr.readLine()) {
      line = line.replaceFirst("//.*$", "");
      if (line.trim().length() == 0) continue;
      if (line.startsWith(" ")) {
        if (current_regex == null)
          throw new IOException("No current class regex on line " + lr.getLineNumber());
        StrTok st = new StrTok(line, mfeh);
        st.stok.wordChars('.', '.'); // make '.' a word constituent
        MethodDef md = parse_method(st);
        String replacement_method_class = st.need_word(); // words including '.'
        map.put(md, new ReplacementClass(replacement_method_class));
      } else {
        if (current_regex != null) {
          MethodMapInfo mmi = new MethodMapInfo(current_regex, map);
          map_calls_file.add(mmi);
          map = new LinkedHashMap<>();
        }
        current_regex = Pattern.compile(line);
      }
    }
    if (current_regex != null) {
      MethodMapInfo mmi = new MethodMapInfo(current_regex, map);
      map_calls_file.add(mmi);
    }

    dump_map_calls_file();
  }

  /** Dumps out the map list to the debug_map logger */
  public static void dump_map_calls_file() {
    if (debug_map.enabled()) {
      for (MethodMapInfo mmi : map_calls_file) {
        debug_map.log("Class re '%s': %n", mmi.class_regex);
        for (MethodDef md : mmi.map.keySet()) {
          ReplacementClass mi = mmi.map.get(md);
          debug_map.log("  %s - %s [%d replacements]%n", md, mi.method_class, mi.cnt);
        }
      }
    }
  }

  /** Adds a shutdown hook that prints out the results of the method maps */
  public void add_map_file_shutdown_hook() {

    // Add a shutdown hook to printout some debug information
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                for (MethodMapInfo mmi : map_calls_file) {
                  dump_map_calls_file();
                }
              }
            });
  }
}
