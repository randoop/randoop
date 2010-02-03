package randoop.experiments;

import java.awt.Color;

public class GradientColor {
  
  private double min;
  private double max;

  public GradientColor(double min2, double max2) {
    if (min2 > max2) throw new IllegalArgumentException("min must be <= max.");
    if (min2 <= 0) throw new IllegalArgumentException("min must be > 0.");
    this.min = min2;
    this.max = max2;
  }
  
  public Color getColor(double val) {
    
    if (val == 0) {
      return Color.WHITE;
    }
    
    if (val < min || val > max)
      throw new IllegalArgumentException("val out of range, val=" + val + ",min=" + min + ",max=" + max);
    double ratio = (val - min) / (max - min);
    
    int red = (int)(getRedFactor(ratio) * 255);
    int green = (int)(getGreenFactor(ratio) * 255);
    int blue = (int)(getBlueFactor(ratio) * 255);
    assert red >= 0 && red <= 255 : red;
    assert green >= 0 && green <= 255 : green;
    assert blue >= 0 && blue <= 255 : blue;
    
    return new Color(red, green, blue);
  }
  
  private double getRedFactor(double ratio) {
    assert ratio >= 0 && ratio <= 1 : ratio;
    if (ratio < 0.33) {
      return 0;
    }
    return (ratio - 0.33) / 0.67;
  }

  private double getBlueFactor(double ratio) {
    assert ratio >= 0 && ratio <= 1 : ratio;
    if (ratio > 0.66) {
      return 0;
    }
    return (0.66 - ratio) / 0.66;
  }

  private double getGreenFactor(double ratio) {
    assert ratio >= 0 && ratio <= 1 : ratio;
    if (ratio < 0.5) {
      return ratio * 2;
    } else {
      return 2 - ratio*2;
    }
  }

}
