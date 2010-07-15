package randoop.experiments;

import java.text.DecimalFormat;

public class CalculateCovTimeRatios {
  
  private static DecimalFormat format = new DecimalFormat("#.###");

  public static void main(String[] args) {

    System.out.println(",oc,om,fc,fm");
    for (String arg : args) {
//      System.out.println("@@@" + args);
      calculateForOne(arg);
    }
  }

  private static void calculateForOne(String expname) {
    
    CovPlot oc = new CovPlot("oc-" + expname + ".covplot"); 
    CovPlot om = new CovPlot("om-" + expname + ".covplot"); 
    CovPlot fc = new CovPlot("fc-" + expname + ".covplot"); 
    CovPlot fm = new CovPlot("fm-" + expname + ".covplot");

    System.out.print(expname);
    System.out.print("\t\t");

//    System.out.println("@@@" + om.getMaxCov());
//    System.out.println("@@@" + fc.getIndex(om.getMaxCov()));
//    System.out.println("@@@" + om.getIndex(om.getMaxCov()));
    System.out.print(format.format((1+fc.getIndex(om.getMaxCov()))/(double)(1+om.getIndex(om.getMaxCov()))));
    System.out.print("\t");

    System.out.print(format.format((1+fc.getIndex(oc.getMaxCov()))/(double)(1+oc.getIndex(oc.getMaxCov()))));
    System.out.print("\t");
    
    assert format.format((1+fc.getIndex(fc.getMaxCov()))/(double)(1+fc.getIndex(fc.getMaxCov()))).equals("1");

    System.out.print(format.format((1+fc.getIndex(fm.getMaxCov()))/(double)(1+fm.getIndex(fm.getMaxCov()))));
    System.out.println();
    
  }

}
