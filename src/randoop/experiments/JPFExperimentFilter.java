package randoop.experiments;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import randoop.util.DefaultReflectionFilter;
import randoop.util.ReflectionFilter;

public class JPFExperimentFilter implements ReflectionFilter {

  private DefaultReflectionFilter baseFilter;

  public JPFExperimentFilter(Pattern omitmethods) {
    baseFilter = new DefaultReflectionFilter(omitmethods);
  }

  public boolean canUse(Class<?> c) {
    return baseFilter.canUse(c);
  }

  public boolean canUse(Method m) {
    // Causes JPF to break.
    if (m.getDeclaringClass().equals(java.util.TimeZone.class) && m.getName().equals("getAvailableIDs")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to break.
    if (m.getDeclaringClass().equals(java.util.TimeZone.class) && m.getName().equals("getTimeZone")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to report an uncaught exception and terminate.
    if (m.getDeclaringClass().equals(java.util.Currency.class) && m.getName().equals("getInstance")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.getDeclaringClass().equals(javax.xml.transform.TransformerFactory.class) && m.getName().equals("newInstance")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.getDeclaringClass().equals(javax.xml.parsers.DocumentBuilderFactory.class) && m.getName().equals("newInstance")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.getDeclaringClass().equals(javax.xml.parsers.SAXParserFactory.class) && m.getName().equals("newInstance")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.getDeclaringClass().equals(javax.xml.xpath.XPathFactory.class) && m.getName().equals("newInstance")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.getDeclaringClass().equals(javax.xml.datatype.DatatypeFactory.class) && m.getName().equals("newInstance")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.getDeclaringClass().equals(javax.xml.validation.SchemaFactory.class) && m.getName().equals("newInstance")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().equals("public static void org.apache.commons.chain.CatalogFactory.clear()")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.chain.CatalogFactory.getInstance()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.XMLOutput.createDummyXMLOutput()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.Jelly.getJellyBuildDate()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.Jelly.getJellyVersion()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.tags.Resources.getMessage") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.logging.LogSource.getLogNames()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.logging.LogSource.getInstance") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.logging.LogSource.makeNewLogInstance") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.logging.LogSource.setLogImplementation") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }

    return baseFilter.canUse(m);
  }

  public boolean canUse(Constructor<?> m) {
    if (m.getDeclaringClass().equals(java.util.Formatter.class) && m.getParameterTypes().length == 0) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().equals("public org.apache.commons.chain.impl.ContextBase()")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().equals("public org.apache.commons.chain.impl.CatalogFactoryBase()")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().equals("public org.apache.commons.chain.web.portlet.PortletWebContext()")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().equals("public org.apache.commons.chain.web.ChainServlet()")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().equals("public org.apache.commons.chain.web.ChainListener()")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().equals("public org.apache.commons.chain.web.servlet.ServletWebContext()")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().equals("public org.apache.commons.chain.web.servlet.ChainProcessor()")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().equals("public org.apache.commons.chain.web.faces.FacesWebContext()")) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.impl.DefaultTagLibraryResolver()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.tags.core.WhileTag()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.servlet.JellyServletContext()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.servlet.JellyServlet()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.parser.XMLParser()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.tags.core.WhitespaceTag()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.tags.core.SetTag()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.tags.core.JellyTag()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.tags.core.ThreadTag()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.tags.core.CoreTagLibrary()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.tags.core.ParseTag()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.tags.Resources()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.Jelly()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }

    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.XMLOutput()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.impl.DynamicTagLibrary") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.impl.DynamicTag()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.impl.StaticTagScript()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.impl.Embedded()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.impl.TagScript()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.tags.core.ForEachTag()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.util.ClassLoaderUtils()") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.jelly.JellyContext") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }

    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.logging.impl.Jdk13LumberjackLogger") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.logging.impl.Jdk14Logger") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }
    // Causes JPF to throw an exception even though the driver wraps a catch(Throwable) around the code.
    if (m.toString().indexOf("org.apache.commons.logging.impl.SimpleLog") != -1) {
      System.out.println("Will not use " + m.toString());
      return false;
    }


    return baseFilter.canUse(m);
  }

}
