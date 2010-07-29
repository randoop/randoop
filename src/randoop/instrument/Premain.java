package randoop.instrument;

// import harpoon.ClassFile.HMethod;

import java.lang.instrument.*;
import java.lang.reflect.Member;
import java.io.*;
import java.io.File;
import java.util.*;
import java.util.jar.*;
import java.net.URL;

import plume.SimpleLog;
import plume.Option;
import plume.Options;

public class Premain {

  @Option ("print debug information")
  public static boolean debug = false;

  @Option ("print progress information")
  public static boolean verbose = false;

  @Option ("file containing methods calls to map to substitute methods")
  public static File map_calls = null;

  @Option ("Use first BCEL on classpath rather than PAG's version")
  public static boolean default_bcel = true;

  /**
   * This method is the entry point of the java agent.  Its main
   * purpose is to set up the transformer so that when classes from
   * the target app are loaded, they are first transformed.
   */
  public static void premain (String agentArgs, Instrumentation inst)
    throws IOException {

    System.out.format ("In premain, agentargs ='%s', " +
                       "Instrumentation = '%s'%n", agentArgs, inst);


    // Parse our arguments
    Options options = new Options (Premain.class);
    String[] target_args = options.parse_or_usage (agentArgs);
    if (target_args.length > 0) {
      System.err.printf ("Unexpected agent arguments %s%n",
                         Arrays.toString (target_args));
      System.exit (1);
    }


    // Setup the transformer
    Object transformer = null;
    if (default_bcel) {
      transformer = new Instrument();
    } else { // use a special classloader to ensure our files are used
      ClassLoader loader = new BCELLoader();
      try {
        transformer
          = loader.loadClass ("randoop.instrument.Instrument").newInstance();
        @SuppressWarnings("unchecked")
        Class<Instrument> c = (Class<Instrument>) transformer.getClass();
        // System.out.printf ("Classloader of tranformer = %s%n",
        //                    c.getClassLoader());
      } catch (Exception e) {
        throw new RuntimeException ("Unexpected error loading Instrument", e);
      }
    }

    // Read the map file
    if (map_calls != null) {
      Instrument instrument = (Instrument) transformer;
      instrument.read_map_file (map_calls);
      instrument.add_map_file_shutdown_hook();
    }

    // Instrument transformer = new Instrument();
    inst.addTransformer ((ClassFileTransformer) transformer);

  }

  /**
   * Reads purity file.  Each line should contain exactly one method.
   * Care must be taken to supply the correct format.
   *
   * From the Sun JDK API:
   *
   * "The string is formatted as the method access modifiers, if any,
   * followed by the method return type, followed by a space, followed
   * by the class declaring the method, followed by a period, followed
   * by the method name, followed by a parenthesized, comma-separated
   * list of the method's formal parameter types. If the method throws
   * checked exceptions, the parameter list is followed by a space,
   * followed by the word throws followed by a comma-separated list of
   * the thrown exception types. For example:
   *
   * public boolean java.lang.Object.equals(java.lang.Object)
   *
   * The access modifiers are placed in canonical order as specified
   * by "The Java Language Specification".  This is public, protected
   * or private first, and then other modifiers in the following
   * order: abstract, static, final, synchronized native."
   */
  private static HashSet<String> readPurityFile(File purityFileName,
                                      File pathLoc) throws IOException
  {
    HashSet<String> pureMethods = new HashSet<String>();

    BufferedReader reader = new BufferedReader
        (new FileReader (new File(pathLoc, purityFileName.getPath())));

    if (true)
      System.out.printf("Reading '%s' for pure methods %n", purityFileName);

    for (String line = reader.readLine(); line != null;
         line = reader.readLine()) {
      pureMethods.add(line.trim());
    }

    reader.close();

    return pureMethods;

  }

  /**
   * Classloader for the BCEL code.  Using this classloader guarantees
   * that we get the PAG version of the BCEL code and not a possible
   * incompatible version from elsewhere on the users classpath.  We
   * also load randoop.instrument.Instrument via this (since that class is
   * the user of all of the BCEL classes).  All references to BCEL
   * must be within that class (so that all references to BCEL will
   * get resolved by this classloader).
   *
   * The PAG version of BCEL is identified by the presence of the
   * PAG marker class (org.apache.bcel.PAGMarker).  Other versions of
   * BCEL will not contain this class.  If other versions of BCEL are
   * present, they must appear before the PAG versions in the classpath
   * (so that the users application will see them first).  If only the
   * PAG version is in the classpath, then the normal loader is used
   * for all of the classes.
   */
  public static class BCELLoader extends ClassLoader {

    /** Jar file that contains BCEL.  If null, use the normal classpath **/
    JarFile bcel_jar = null;

    public static final SimpleLog debug = new SimpleLog (verbose);

    public BCELLoader() throws IOException {

      String bcel_classname = "org.apache.bcel.Constants";
      String pag_marker_classname = "org.apache.bcel.PAGMarker";

      List<URL> bcel_urls = get_resource_list (bcel_classname);
      List<URL> pag_urls = get_resource_list (pag_marker_classname);

      if (pag_urls.size() == 0) {
        System.err.printf("%nBCEL must be in the classpath.  "
                           + "Normally it is found in daikon.jar .%n");
        System.exit(1);
      }
      if (bcel_urls.size() < pag_urls.size()) {
        System.err.printf("%nCorrupted BCEL library, bcel %s, pag %s%n",
                          bcel_urls, pag_urls);
        System.exit(1);
      }

      // No need to do anything if only our versions of bcel are present
      if (bcel_urls.size() == pag_urls.size())
        return;

      int bcel_index = 0;
      int pag_index = 0;
      while (bcel_index < bcel_urls.size()) {
        URL bcel = bcel_urls.get(bcel_index);
        URL pag = pag_urls.get(pag_index);
        if (!pag.getProtocol().equals ("jar")) {
          System.err.printf("%nPAG BCEL must be in jar file. "
                            + " Found at %s%n", pag);
          System.exit(1);
        }
        if (same_location (bcel, pag)) {
          if (bcel_index == pag_index) {
            URL first_bcel = bcel;
            while ((pag != null) && same_location (bcel, pag)) {
              bcel = bcel_urls.get(++bcel_index);
              pag_index++;
              pag = (pag_index < pag_urls.size())
                ? pag_urls.get(pag_index) : null;
            }
            System.err.printf ("%nPAG BCEL (%s) appears before target BCEL "
              + "(%s).%nPlease reorder classpath to put randoop.jar at "
              + "the end.%n",first_bcel, bcel);
            System.exit(1);
          } else {
            bcel_jar = new JarFile (extract_jar_path (pag));
            debug.log ("PAG BCEL found in jar %s%n", bcel_jar.getName());
            break;
          }
        } else { // non pag bcel found
          debug.log ("Found non-pag BCEL at %s%n", bcel);
          bcel_index++;
        }
      }
    }

    /**
     * Returns whether or not the two URL represent the same location
     * for org.apache.bcel.  Two locations match if they refer to the
     * same jar file or the same directory in the filesystem.
     */
    private boolean same_location (URL url1, URL url2) {
      if (!url1.getProtocol().equals (url2.getProtocol()))
        return false;

      if (url1.getProtocol().equals ("jar")) {
        // System.out.printf ("url1 = %s, file=%s, path=%s, protocol=%s, %s%n",
        //                  url1, url1.getFile(), url1.getPath(),
        //                  url1.getProtocol(), url1.getClass());
        // System.out.printf ("url2 = %s, file=%s, path=%s, protocol=%s, %s%n",
        //                    url2, url2.getFile(), url2.getPath(),
        //                    url2.getProtocol(), url1.getClass());
        String jar1 = extract_jar_path (url1);
        String jar2 = extract_jar_path (url2);
        return (jar1.equals (jar2));
      } else if (url1.getProtocol().equals ("file")) {
        String loc1 = url1.getFile().replaceFirst ("org\\.apache\\.bcel\\..*$",
                                                   "");
        String loc2 = url2.getFile().replaceFirst ("org\\.apache\\.bcel\\..*$",
                                                   "");
        return (loc1.equals (loc2));
      } else {
        assert false : "unexpected protocol " + url1.getProtocol();
      }

      return (false);
    }

    /**
     * Returns the pathname of a jar file specified in the URL.  The
     * protocol must be 'jar'.  Only file jars are supported.
     */
    private String extract_jar_path (URL url) {
      assert url.getProtocol().equals ("jar") : url.toString();

      // Remove the preceeding 'file:' and trailing '!filename'
      String path = url.getFile();
      path = path.replaceFirst ("^[^:]*:", "");
      path = path.replaceFirst ("![^!]*$", "");

      return path;
    }

    /**
     * Get all of the URLs that match the specified name in the
     * classpath.  The name should be in normal classname format (eg,
     * org.apache.bcel.Constants).  An empty list is returned if no
     * names match.
     */
    List<URL> get_resource_list (String classname) throws IOException {

      String name = classname_to_resource_name (classname);
      Enumeration<URL> enum_urls = ClassLoader.getSystemResources (name);
      List<URL> urls = new ArrayList<URL>();
      while (enum_urls.hasMoreElements()) {
        urls.add (enum_urls.nextElement());
      }
      return  (urls);
    }

    /**
     * Changs a class name in the normal format (eg, org.apache.bcel.Constants)
     * to that used to lookup resources (eg. org/apache/bcel/Constants.class)
     */
    private String classname_to_resource_name (String name) {
      return (name.replace (".", "/") + ".class");
    }

    protected Class<?> loadClass (String name, boolean resolve)
      throws java.lang.ClassNotFoundException {

      // If we are not loading from our jar, just use the normal mechanism
      if (bcel_jar == null)
        return super.loadClass (name, resolve);

      // Load non-bcel files via the normal mechanism
      if (!name.startsWith ("org.apache.bcel")
          && (!name.startsWith ("daikon.chicory.Instrument"))) {
        // System.out.printf ("loading standard %s%n", name);
        return super.loadClass (name, resolve);
      }

      // If we've already loaded the class, just return that one
      Class<?> c = findLoadedClass (name);
      if (c != null) {
        if (resolve)
          resolveClass (c);
        return c;
      }

      // Find our version of the class and return it.
      try {
        InputStream is = null;
        if (name.startsWith ("daikon.chicory.Instrument")) {
          String resource_name = classname_to_resource_name (name);
          URL url = ClassLoader.getSystemResource (resource_name);
          is = url.openStream();
        } else { //  Read the BCEL class from the jar file
          String entry_name = classname_to_resource_name (name);
          JarEntry entry = bcel_jar.getJarEntry (entry_name);
          assert entry != null : "Can't find " + entry_name;
          is = bcel_jar.getInputStream (entry);
        }
        int available = is.available();
        byte[] bytes = new byte[available];
        int total = 0;
        while (total < available) {
          int len = is.read (bytes, total, available-total);
          total += len;
        }
        assert total == bytes.length : "only read " + total;
        assert is.read() == -1 : "more data left in stream";
        // System.out.printf ("Defining class %s size %d%n", name, available);
        c = defineClass(name, bytes, 0, bytes.length);
        if (resolve)
          resolveClass (c);
        return c;
      } catch (Exception e) {
        throw new RuntimeException ("Unexpected exception loading class "
                                    + name, e);
      }
    }
  }
}
