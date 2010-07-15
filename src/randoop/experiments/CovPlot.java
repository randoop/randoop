package randoop.experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import randoop.util.Files;

public class CovPlot {
  
  private List<Integer> cov;
  
  public CovPlot(String fileName) {
    List<String> lines = null;
    try {
      lines = Files.readWhole(fileName);
    } catch (IOException e) {
      throw new Error(e);
    }
    assert lines != null;
  
    cov = new ArrayList<Integer>();
    for (String line : lines) {
      String[] split = line.split("\\s");
      assert split.length == 2;
      cov.add(Integer.parseInt(split[0]));
    }
  }

  public int getIndex(int covnumber) {
    for (int i = 0 ; i < cov.size() ; i++) {
      int currcov = cov.get(i);
      if (currcov >= covnumber) {
        return i;
      }
    }
    return Integer.MIN_VALUE;
  }
  
  public int getMaxCov() {
    return cov.get(cov.size() - 1);
  }

  public int size() {
    return cov.size();
  }
}
