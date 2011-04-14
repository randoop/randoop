package randoop.experiments;

import randoop.ExecutableSequence;
import randoop.util.Histogram;

public class SizeEqualizer {
  
  private static final int maxbin = 30;
  private static final int binsize = 5;
  
  public Histogram<Integer> hist;
  
  public SizeEqualizer() {
    hist = new Histogram<Integer>();
  }

  public boolean add(ExecutableSequence s) {
    int size = s.executedSize();
    int bin = bin(size);
    if (bin == maxbin) {
      hist.addToCount(maxbin, size);
      return true;
    }
    if (hist.getCount(bin) < hist.getCount(maxbin)) {
      hist.addToCount(bin, size);
      return true;
    }
    return false;
  }
  
  private int bin(int nodes) {
    for (int bin = binsize ; bin < maxbin ; bin = bin + binsize) {
      if (nodes <= bin) {
        return bin;
      }
    }
    return maxbin;
  }
  
}
