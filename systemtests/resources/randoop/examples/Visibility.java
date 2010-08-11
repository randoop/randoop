package examples;

class NonVisible {
  public void randoopOmittedMethod() {};
}

public class Visibility {
  public NonVisible getNonVisible() {
    return new NonVisible();
  }  
  public void eatNonVisible(NonVisible nonVisible) {
    
  }
}

