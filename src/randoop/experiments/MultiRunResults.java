package randoop.experiments;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import randoop.Globals;




public class MultiRunResults {


  Set<String> numericPropertyNames;
  List<String> runNames;
  List<Properties> runs;

  private static MathContext mc = new MathContext(2);

  private String format(String s) {
    try {
      double d = Double.parseDouble(s);
      return format(d);
    } catch (Exception e) {
      return s;
    }
  }

  private String format(double d) {

    if (Double.isNaN(d)) {
      return "NaN";
    }
    try {
      BigDecimal dec = new BigDecimal(d, mc);
      return dec.toPlainString();
    } catch (NumberFormatException e) {
      throw new Error(e);
    }
  }

  public MultiRunResults() {
    this.numericPropertyNames = new LinkedHashSet<String>();
    runs = new ArrayList<Properties>();
    runNames = new ArrayList<String>();
  }

  private void findNumericProperties(Properties run) {
    for (Enumeration<?> namesEnum = run.propertyNames() ; namesEnum.hasMoreElements() ; ) {
      String propertyName = (String)namesEnum.nextElement();
      String propertyVariable = run.getProperty(propertyName);
      try {
        Double.parseDouble(propertyVariable);
        numericPropertyNames.add(propertyName);
      } catch (Exception e) {
        // Do not add to numericPropertyNames.
      }
    }
  }

  // Checks the following properties about a run, and throws
  // an exception if they do not hold:
  // + The property names in run are exactly those in this.runs.get(0)
  // + All propertie names that are in this.numericPropertyNames have
  //   values that represent Doubles.
  private void checkRun(Properties run) {
    for (Enumeration<?> namesEnum = run.propertyNames() ; namesEnum.hasMoreElements() ; ) {
      String propertyName = (String)namesEnum.nextElement();
      String propertyVariable = run.getProperty(propertyName);
      if (runs.get(0).getProperty(propertyName) == null) {
        throw new IllegalArgumentException("Property " + propertyName + " not in first run's properties");
      }
      if (numericPropertyNames.contains(propertyVariable)) {
        try {
          Double.parseDouble(propertyVariable);
        } catch (Exception e) {
          throw new IllegalArgumentException("Property is not Double-valued: " + propertyName + "=" + propertyVariable);
        }
      }
    }
  }

  public void addRunResults(String runName, Properties run) {
    if (run == null || runName == null)
      throw new IllegalArgumentException("parameters cannot be null.");
    if (this.runs.isEmpty()) {
      findNumericProperties(run);
      runNames.add(runName);
      runs.add(run);
      return;
    }
    checkRun(run);
    runNames.add(runName);
    runs.add(run);
  }

  private Properties computeTotals() {

    Map<String,Double> totals = new LinkedHashMap<String,Double>();
    for (String numericProperty : this.numericPropertyNames) {
      totals.put(numericProperty, 0.0);
    }
    for (Properties run : this.runs) {
      for (String numericProperty : this.numericPropertyNames) {
        double valueForRun = Double.parseDouble(run.getProperty(numericProperty));
        totals.put(numericProperty, totals.get(numericProperty) + valueForRun);
      }
    }
    Properties retval = new Properties();
    for (String numericProperty : this.numericPropertyNames) {
      retval.setProperty(numericProperty, Double.toString(totals.get(numericProperty)));
    }
    return retval;
  }

  public static enum OutputFormat { PROPERTIES, LATEX }

  @Override
  public String toString() {
    return toString("", OutputFormat.PROPERTIES);
  }

  public String toString(String definitionPrefix, OutputFormat format) {
    StringBuilder b = new StringBuilder();
    for (int i = 0 ; i < this.runs.size() ; i++) {
      Properties run = this.runs.get(i);
      String runName = this.runNames.get(i);
      b.append(oneRun(runName, run, format));
    }
    b.append(oneRun(definitionPrefix + "total", computeTotals(), format));
    return b.toString();
  }

  private String oneRun(String runName, Properties run, OutputFormat format) {
    StringBuilder b = new StringBuilder();
    for (Enumeration<?> namesEnum = run.propertyNames() ; namesEnum.hasMoreElements() ; ) {
      String propertyName = (String)namesEnum.nextElement();
      String propertyVariable = run.getProperty(propertyName);
      if (format == OutputFormat.LATEX) {
        b.append("\\def\\" + runName + propertyName + "{" + format(propertyVariable) + "}" + Globals.lineSep);
      } else {
        assert format == OutputFormat.PROPERTIES;
        b.append(runName + propertyName + "=" + format(propertyVariable) + Globals.lineSep);
      }
    }
    return b.toString();
  }
}
