package generation;

/**
 * Emulates an example from ejml library in Pascali corpus.
 * Results in reflection exception due to bad types after application of copy method.
 */
public class Dim5Matrix implements Matrix {

  public double a1, a2, a3, a4, a5;

  public Dim5Matrix() {}

  public Dim5Matrix(Dim5Matrix dim5Matrix) {
    this.a1 = dim5Matrix.a1;
    this.a2 = dim5Matrix.a2;
    this.a3 = dim5Matrix.a3;
    this.a4 = dim5Matrix.a4;
    this.a5 = dim5Matrix.a5;
  }

  @Override
  public <T extends Matrix> T copy() {
    return (T)new Dim5Matrix(this);
  }

}
