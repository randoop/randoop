package randoop.experiments;

import java.awt.Paint;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class XYRenderer extends XYLineAndShapeRenderer {
  private static final long serialVersionUID = 1L;
  private Paint[] colors;
  
  public XYRenderer(Paint[][] colors) {
    super(false, true);
    assert colors.length == colors[0].length;
    assert colors.length == TempStatsComputer.maxsize;
    this.colors = new Paint[colors.length * colors.length];
    int count = 0;
    for (int i = 0 ; i < colors.length ; i++) {
      for (int j = 0 ; j < colors.length ; j++) { // Correcteness of test depends on assertions.
        this.colors[count++] = colors[i][j];
      }
    }
  }
  
  public Paint getItemPaint(int row, int column) {
    assert row == 0;
    return colors[column];
  }

}
