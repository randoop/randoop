package generation;

/**
 * Emulates an example from ejml library in Pascali corpus.
 * Results in reflection exception due to bad types after application of copy method.
 */
public class Dim6Matrix implements Matrix {

  public double a1, a2, a3, a4, a5, a6;

  public Dim6Matrix() {}

  public Dim6Matrix(Dim6Matrix dim6Matrix) {
    this.a1 = dim6Matrix.a1;
    this.a2 = dim6Matrix.a2;
    this.a3 = dim6Matrix.a3;
    this.a4 = dim6Matrix.a4;
    this.a5 = dim6Matrix.a5;
    this.a6 = dim6Matrix.a6;
  }

  @Override
  public <T extends Matrix> T copy() {
    return (T)new Dim6Matrix(this);
  }
}
