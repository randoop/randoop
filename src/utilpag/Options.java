package utilpag;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class<?> that reads and sets command line options.  The Option
 * annotation is used to identify fields that are associated with a
 * command line option.  Both the short (-) and long option formats
 * (--) are supported.  The name of the option is the name of the
 * field (with some exceptions noted below).  The primitive types
 * boolean, int, and double are supported.  All reference types that
 * have a constructor with a single string parameter are supported as
 * well.  Pattern (created with a factory) is supported as a special
 * case.  Primitives can also be represented as wrappers (Boolean,
 * Integer, Double).  Use of the wrappers allows a primitive argument
 * not to have a default value. <p>
 *
 * Lists of any valid reference type are also supported.  If an option
 * as a list, it can be specified multiple times and each entry will
 * be added to the list.  Non list options overwrite the previous value
 * if specified multiple times.  Lists must be initialized to a valid
 * list before using Options on that list. <p>
 *
 * The option annotation (@see Option) specifies the optional short
 * name, optional type name, and long description.  The long name is taken
 * from the name of the variable. <p>
 *
 * On the command line, the values for options are specified in the
 * form '--long=value', '-short=value', '--long value' or '-short
 * value'.  The value is mandatory for all options except booleans.
 * Booleans are set to true if no value is specified. <p>
 *
 * Any non-options (entries that don't begin with --) in the
 * argument list are returned. <p>
 *
 * Limitations: <ul>
 *
 *  <li> Short options are only supported as separate entries (eg, -a -b)
 *  and not as a single group (eg -ab).
 *
 *  <li> Types without a string constructor and the other primitive types
 *  are not supported.
 *
 *  <li>  Non option information could be supported in the same manner
 *  as options which would be cleaner than simply returning all of the
 *  non-options as an array
 * </ul>
 **/
public class Options {

  /** Information about an option **/
  private class OptionInfo {

    /** Field containing the value of the option **/
    Field field;

    /** Option information for the field **/
    Option option;

    /** Object containing the field.  Null if the field is static **/
    Object obj;

    /** Short (one character) argument name **/
    String short_name;

    /** Long argument name **/
    String long_name;

    /** Argument description **/
    String description;

    /** JavaDoc description **/
    String jdoc;

    /**
     * Name of the argument type.  Defaults to the type of the field, but
     * user can override this in the option string
     */
    String type_name;

    /**
     * Class<?> type of this field.  If the field is a list, the basetype
     * of the list.
     */
    Class<?> base_type;

    /** Default value of the option as a string **/
    String default_str = null;

    /** If the option is a list, this references that list **/
    List list = null;

    /** Constructor that takes one String for the type **/
    Constructor constructor = null;

    /** Factory that takes a string (some classes don't have a string const) */
    Method factory = null;

    /** If true, this OptionInfo is not output when printing documentation
     *  (see Options.usage()
     */
	private boolean isHidden;

    /**
     * Create the specified option.  If obj is null, the field must be
     * static.  The short name, type name, and description are taken
     * from the option annotation.  The long name is the name of the
     * field.  The default value is the current value of the field.
     * @param isHidden
     */
    OptionInfo (Field field, Option option, Object obj, boolean isHidden) {
      this.field = field;
      this.option = option;
      this.obj = obj;
      this.base_type = field.getType();
      this.isHidden = isHidden;

      // The long name is the name of the field
      long_name = field.getName();
      if (use_dashes)
        long_name = long_name.replace ('_', '-');

      // Get the default value (if any)
      Object default_obj = null;
      try {
        default_obj = field.get (obj);
        if (default_obj != null)
          default_str = default_obj.toString();
      } catch (Exception e) {
        throw new Error ("Unexpected error getting default for " + field, e);
      }

      // Handle lists.  When a list argument is specified multiple times,
      // each argument value is appended to the list.
      Type gen_type = field.getGenericType();
      if (gen_type instanceof ParameterizedType) {
        ParameterizedType pt = (ParameterizedType) gen_type;
        Type raw_type = pt.getRawType();
        if (!raw_type.equals (List.class))
          throw new Error ("Unsupported option type " + pt);
        this.list = (List) default_obj;
        if (this.list == null)
          throw new Error ("List option " + field + " must be initialized");
        // System.out.printf ("list default = %s%n", list);
        this.base_type = (Class<?>) pt.getActualTypeArguments()[0];

        // System.out.printf ("Param type for %s = %s%n", field, pt);
        // System.out.printf ("raw type = %s, type = %s%n", pt.getRawType(),
        //                   pt.getActualTypeArguments()[0]);
      }

      // Get the short name, type name, and description from the annotation
      String[] opt_results = parse_option (option.value());
      short_name = opt_results[0];
      type_name = opt_results[1];
      if (type_name == null) {
        type_name = type_short_name (base_type);
      }
      description = opt_results[2];

      // Get a constructor for non-primitive base types
      if (!base_type.isPrimitive()) {
        try {
          if (base_type == Pattern.class) {
            factory = base_type.getMethod ("compile", String.class);
          } else { // look for a string constructor
            constructor = base_type.getConstructor (String.class);
          }
        } catch (Exception e) {
          throw new Error ("Option " + field
                           + " does not have a string constructor", e);
        }
      }
    }

    /**
     * Returns whether or not this option has a required argument
     */
    public boolean argument_required() {
      Class<?> type = field.getType();
      return ((type != Boolean.TYPE) && (type != Boolean.class));
    }

    /**
     * Returns a short synopsis of the option in the form
     * -s --long=<type>
     **/
    public String synopsis() {
      String name = "--" + long_name;
      if (short_name != null)
        name = String.format ("-%s %s", short_name, name);
      name += String.format ("=%s", type_name);
      return (name);
    }

    /**
     * Returns a one line description of the option
     */
    @Override
    public String toString() {
      String short_name_str = "";
      if (short_name != null)
        short_name_str = "-" + short_name + " ";
      return String.format ("%s--%s field %s", short_name_str, long_name,
                            field);
    }

    /** Returns the class that declares this option **/
    public Class<?> get_declaring_class() {
      return field.getDeclaringClass();
    }
  }

  /**
   * Ignore options after the first non-option
   * @see #ignore_options_after_arg(boolean)
   **/
  private boolean ignore_options_after_arg = false;

  /** All of the non-argument options as a single string **/
  private String options_str = "";

  /** First specified class **/
  private Class<?> main_class = null;

  /** List of all of the defined options **/
  private List<OptionInfo> options = new ArrayList<OptionInfo>();

  /** Map from option names (with leading dashes) to option information **/
  private Map<String,OptionInfo> name_map
    = new LinkedHashMap<String,OptionInfo>();

  /**
   * Convert underscores to dashes in long options.  The underscore name
   * will work as well, but the dashed name will be used in the usage
   */
  private boolean use_dashes = true;

  /** Synopsis of usage (e.g., prog [options] arg1, arg2, ...) **/
  String usage_synopsis = "";

  // Debug loggers
  private SimpleLog debug_options = new SimpleLog (false);

  /**
   * Setup option processing on the specified array of objects or
   * classes.  If an element is a class, each of the options must be
   * static.  Otherwise the element is assumed to be an object that
   * contains some option fields.  In that case, the fields can be either
   * static or instance fields.  The names must be unique over all of
   * the elements
   */
  public Options (Object... classes) {
    this ("", classes);
  }

  /**
   * Setup option processing on the specified array of objects or
   * classes.  If an element is a class, each of the options must be
   * static.  Otherwise the element is assumed to be an object that
   * contains some option fields.  In that case, the fields can be either
   * static or instance fields.  The names must be unique over all of
   * the elements
   */
  public Options (String usage_synopsis, Object... classes) {

    this.usage_synopsis = usage_synopsis;

    // Loop through each specified object or class
    for (Object obj : classes) {

      if (obj instanceof Class) {

        if (main_class == null)
          main_class = (Class<?>) obj;

        Field[] fields = ((Class<?>) obj).getDeclaredFields();
        for (Field f : fields) {
          debug_options.log ("Considering field %s with annotations %s%n", f,
                             Arrays.toString(f.getDeclaredAnnotations()));
          Option option = f.getAnnotation (Option.class);
          if (option == null)
            continue;
          boolean isHidden = false;
         if ( f.getAnnotation(Invisible.class) != null)
        	  isHidden = true;
          if (!Modifier.isStatic (f.getModifiers()))
            throw new Error ("non-static option " + f + " in class " + obj);
          options.add (new OptionInfo (f, option, null, isHidden));
        }

      } else { // must be an object that contains option fields

        if (main_class == null)
          main_class = obj.getClass();

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field f : fields) {
          Option option = f.getAnnotation (Option.class);
          if (option == null)
            continue;
          boolean isHidden = false;
          if (f.getAnnotation(Invisible.class) != null)
        	  isHidden = true;
          options.add (new OptionInfo (f, option, obj, isHidden));
        }
      }
    }

    // Add each option to the option name map
    for (OptionInfo oi : options) {
      if (oi.short_name != null) {
        if (name_map.containsKey ("-" + oi.short_name))
          throw new Error ("short name " + oi + " appears twice");
        name_map.put ("-" + oi.short_name, oi);
      }
      if (name_map.containsKey ("--" + oi.long_name))
        throw new Error ("long name " + oi + " appears twice");
      name_map.put ("--" + oi.long_name, oi);
      if (use_dashes && oi.long_name.contains ("-"))
        name_map.put ("--" + oi.long_name.replace ('-', '_'), oi);
    }
  }

  /**
   * Set whether or not options after the first non option (first argument)
   * should be treated as arguments and not as options.  Useful if the
   * arguments are actually arguments/options for another program or for
   * some other reason include items that start with '-' or '--'
   */
  public void ignore_options_after_arg (boolean val) {
    ignore_options_after_arg = val;
  }

  /**
   * Parses a command line that is encoded in a single string.  Quoted
   * string with both single and double quotes are supported.
   * @see #parse(String[])
   */
  public String[] parse (String args) throws ArgException {

    // Split the args string on whitespace boundaries accounting for quoted
    // strings.
    List<String> arg_list = new ArrayList<String>();
    if (args == null)
      args = "";
    args = args.trim();
    String arg = "";
    char active_quote = 0;
    for (int ii = 0; ii < args.length(); ii++) {
      char ch = args.charAt (ii);
      if ((ch == '\'') || (ch == '"')) {
        arg+= ch;
        ii++;
        while ((ii < args.length()) && (args.charAt(ii) != ch))
          arg += args.charAt(ii++);
        arg += ch;
      } else if (Character.isWhitespace (ch)) {
        // System.out.printf ("adding argument '%s'%n", arg);
        arg_list.add (arg);
        arg = "";
        while ((ii < args.length()) && Character.isWhitespace(args.charAt(ii)))
          ii++;
        if (ii < args.length())
          ii--;
      } else { // must be part of current argument
        arg += ch;
      }
    }
    if (!arg.equals (""))
      arg_list.add (arg);

    return parse (arg_list.toArray (new String[arg_list.size()]));
  }

  /**
   * Parses a command line and sets the options accordingly.  Any
   * non-option arguments are returned.  Any unknown option or other
   * errors throws an ArgException
   */
  public String[] parse (String[] args) throws ArgException {

    List<String> non_options = new ArrayList<String>();
    boolean ignore_options = false;

    // Loop through each argument
    for (int ii = 0; ii < args.length; ii++) {
      String arg = args[ii];
      if (arg.startsWith ("--") && !ignore_options) {
        int eq_pos = arg.indexOf ('=');
        String arg_name = arg;
        String arg_value = null;
        if (eq_pos > 0) {
          arg_name = arg.substring (0, eq_pos);
          arg_value = arg.substring (eq_pos+1);
        }
        OptionInfo oi = name_map.get (arg_name);
        if (oi == null) {
	  for (String use : usage()) {
	    System.out.printf ("  %s%n", use);
	  }
          throw new ArgException ("unknown argument '" + arg + "'");
        }
        if (oi.argument_required() && (arg_value == null)) {
          ii++;
          if (ii >= args.length)
            throw new ArgException ("option %s requires an argument", arg);
          arg_value = args[ii];
        }
        set_arg (oi, arg_name, arg_value);
      } else if (arg.startsWith ("-") && !ignore_options) {
        int eq_pos = arg.indexOf ('=');
        String arg_name = arg;
        String arg_value = null;
        if (eq_pos > 0) {
          arg_name = arg.substring (0, eq_pos);
          arg_value = arg.substring (eq_pos+1);
        }
        OptionInfo oi = name_map.get (arg_name);
        if (oi == null)
          throw new ArgException ("unknown argument '" + arg + "'");
        if (oi.argument_required() && (arg_value == null)) {
          ii++;
          if (ii >= args.length)
            throw new ArgException ("option %s requires an argument", arg);
          arg_value = args[ii];
        }
        set_arg (oi, arg_name, arg_value);
      } else { // not an option
        if (ignore_options_after_arg)
          ignore_options = true;
        non_options.add (arg);
      }

    }
    return (non_options.toArray (new String[non_options.size()]));
  }

  /**
   * Set the specified option to the value specified in arg_value.  Throws
   * an ArgException if there are any errors
   */
  public void set_arg (OptionInfo oi, String arg_name, String arg_value)
    throws ArgException {

    Field f = oi.field;
    Class<?> type = oi.base_type;

    // Keep track of all of the options specified
    if (options_str.length() > 0)
      options_str += " ";
    options_str += arg_name;
    if (arg_value != null) {
      if (arg_value.contains (" "))
        options_str += " '" + arg_value + "'";
      else
        options_str += " " + arg_value;
    }
    // Argument values are required for everything but booleans
    if ((arg_value == null) && (type != Boolean.TYPE)
        && (type != Boolean.class))
      throw new ArgException ("Variable required for option " + arg_name);

    try {
      if (type.isPrimitive()) {
        if (type == Boolean.TYPE) {
            boolean val = false;
            if (arg_value == null) {
              val = true;
            } else {
              arg_value = arg_value.toLowerCase();
              if (arg_value.equals ("true") || (arg_value.equals ("t")))
                val = true;
              else if (arg_value.equals ("false") || arg_value.equals ("f"))
                val = false;
              else
                throw new ArgException ("Bad boolean value for %s: %s", arg_name,
                                        arg_value);
            }
            arg_value = (val) ? "true" : "false";
            f.setBoolean (oi.obj, val);
        } else if (type == Integer.TYPE) {
          int val = 0;
          try {
            val = Integer.decode (arg_value);
          } catch (Exception e) {
            throw new ArgException ("Invalid integer (%s) for argument %s",
                                    arg_value, arg_name);
          }
          f.setInt (oi.obj, val);
        } else if (type == Double.TYPE) {
          Double val = 0.0;
          try {
            val = Double.valueOf (arg_value);
          } catch (Exception e) {
            throw new ArgException ("Invalid double (%s) for argument %s",
                                    arg_value, arg_name);
          }
          f.setDouble (oi.obj, val);
        } else { // unexpected type
          throw new Error ("Unexpected type " + type);
        }
      } else { // reference type

        // Create an instance of the correct type by passing the argument value
        // string to the constructor.  The only expected error is some sort
        // of parse error from the constructor
        Object val = null;
        try {
          if (oi.constructor != null)
            val = oi.constructor.newInstance (arg_value);
          else
            val = oi.factory.invoke (null, arg_value);
        } catch (Exception e) {
          throw new ArgException ("Invalid argument (%s) for argument %s",
                                  arg_value, arg_name);
        }

        // Set the value
        if (oi.list != null)
          oi.list.add (val); // unchecked cast
        else
          f.set (oi.obj, val);
      }
    } catch (ArgException ae) {
      throw ae;
    } catch (Exception e) {
      throw new Error ("Unexpected error ", e);
    }
  }


  /**
   * Returns an array of Strings describing the usage of the command line
   * options defined in options (one element per option)
   **/
  public String[] usage () {

    List<String> uses = new ArrayList<String>();

    // Create the usage string
		for (OptionInfo oi : options) {
			if (oi.isHidden)
				continue;

			String use;
			if (oi.list != null) {
				// An option that can be given multiple times.
				use = String.format("%s - %s [Can be given multiple times]", oi.synopsis(), oi.description);
			} else {
				// An option given only once.
				String default_str = "[no default]";

				if (oi.default_str != null)
					default_str = String.format("[default %s]", oi.default_str);
				use = String.format("%s - %s %s", oi.synopsis(), oi.description, default_str);
			}
			uses.add(use);
		}

    return uses.toArray (new String[uses.size()]);

  }

  public void toStringHTML(PrintStream out) {

	  out.println("<ul>");
		// Create the usage string
		for (OptionInfo oi : options) {
			if (oi.isHidden)
				continue;
			String default_str = oi.default_str == null ? "no default." : oi.default_str;
			String formatString = "<li> <b><tt>%s</tt></b> <br> <i>Default:</i> <tt> %s </tt> <br> %s";
			String use = String.format(formatString, oi.synopsis(), default_str, oi.description);
			out.println(use);
			out.flush();
		}
		out.println("</ul>");
	}

  /**
	 * Returns a string containing all of the options that were set and their
	 * arguments (essentially the contents of args[] without all non-options
	 * removed)
	 */
  public String get_options_str() {
    return (options_str);
  }

  /**
   * Returns a short name for the specified type for use in messages
   */
  private static String type_short_name (Class<?> type) {

    if (type.isPrimitive())
      return type.getName();
    else if (type == File.class)
      return "filename";
    else if (type == Pattern.class)
      return "regex";
    else
      return UtilMDE.unqualified_name (type.getName()).toLowerCase();
  }

  /**
   * returns a string containing the current setting for each option
   */
  public String settings () {

    String out = "";

    // Determine the length of the longest name
    int max_len = 0;
    for (OptionInfo oi : options) {
      int len = oi.long_name.length();
      if (len > max_len)
        max_len = len;
    }

    // Create the settings string
    for (OptionInfo oi : options) {
      String use = String.format ("%-" + max_len + "s = ", oi.long_name);
      try {
        use += oi.field.get (oi.obj);
      } catch (Exception e) {
        throw new Error ("unexpected exception reading field " + oi.field, e);
      }
      out += String.format ("%s%n", use);
    }

    return (out);
  }

  /**
   * Returns all of the defined options on separate lines
   */
  @Override
public String toString() {

    String out = "";

    for (OptionInfo oi: options) {
      out += String.format ("%s%n", oi);
    }
    return (out);
  }


  /**
   * Exceptions encountered during argument processing
   */
  public static class ArgException extends Exception {
    static final long serialVersionUID = 20051223L;
    public ArgException (String s) { super (s); }
    public ArgException (String format, Object... args) {
      super (String.format (format, args));
    }
  }

  /**
   * Parse an option value and return its three components (short_name,
   * type_name, and description).  The short_name and type_name are null
   * if they are not specified in the string.  There are always three
   * elements in the array
   */
  private static String[] parse_option (String val) {

    // Get the short name, long name, and description
    String short_name = null;
    String type_name = null;
    String description = null;

    // Get the short name (if any)
    if (val.startsWith("-")) {
      short_name = val.substring (1, 2);
      description = val.substring (3);
    } else {
      short_name = null;
      description = val;
    }

    // Get the type name (if any)
    if (description.startsWith ("<")) {
      type_name = description.substring (1).replaceFirst (">.*", "");
      description = description.replaceFirst ("<.*> ", "");
    }

    // Return the result
    return new String[] {short_name, type_name, description};
  }

  /**
   * Parses a command line and sets the options accordingly.  If an error
   * occurs, prints the usage and terminates the program.  The program is
   * terminated rather than throwing an error to create cleaner output.
   * This method splits the argument string into command line arguments,
   * respecting single and double quotes, then calls parse_or_usage(String[]).
   * @return all non-option arguments
   * @see #parse_or_usage(String[])
   */
  public String[] parse_or_usage (String args) {

    String non_options[] = null;

    try {
      non_options = parse (args);
    } catch (ArgException ae) {
      String message = ae.getMessage();
      if (message != null)
        System.out.println (message);
      print_usage ();
      System.exit (-1);
      // throw new Error ("usage error: ", ae);
    }
    return (non_options);
  }


  /**
   * Parses a command line and sets the options accordingly.  If an error
   * occurs, prints the usage and terminates the program.  The program is
   * terminated rather than throwing an error to create cleaner output.
   * @return all non-option arguments
   * @see #parse(String[])
   */
  public String[] parse_or_usage (String[] args) {

    String non_options[] = null;

    try {
      non_options = parse (args);
    } catch (ArgException ae) {
      String message = ae.getMessage();
      if (message != null)
        System.out.println (message);
      print_usage ();
      System.exit (-1);
      // throw new Error ("usage error: ", ae);
    }
    return (non_options);
  }

  /**
   * Prints usage information.
   */
  public void print_usage (PrintStream ps) {
    ps.printf ("Usage: %s%n", usage_synopsis);
    for (String use : usage()) {
      ps.printf ("  %s%n", use);
    }
  }

  /**
   * Prints, to standard output, usage information.
   */
  public void print_usage () {
    print_usage (System.out);
  }



  /**
   * Test  class with some defined arguments
   */
  public static class Test {

    @Option ("generic") List<Pattern> lp = new ArrayList<Pattern>();
    @Option ("-a <filename> argument 1") String arg1 = "/tmp/foobar";
    @Option ("argument 2") String arg2;
    @Option ("-d double value") double temperature;
    @Option ("-f the input file") File input_file;
  }

  /**
   * Simple example
   */
  public static void main (String[] args) {

    Test t = new Test();
    Options options = new Options ("test", new Test());
    System.out.printf ("Options:%n%s", options);
	String non_options[] = null;

	try {
	  non_options = options.parse (args);
	} catch (ArgException ae) {
	  System.out.printf ("%s, Usage: %s%n", ae.getMessage(), options.usage_synopsis);
	  for (String use : options.usage()) {
	    System.out.printf ("  %s%n", use);
	  }
	  System.exit (-1);
	  // throw new Error ("usage error: ", ae);
	}
    String[] parse_and_usage = (non_options);
    System.out.printf ("Results:%n%s", options.settings());
  }
}
