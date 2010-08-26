package boundary;

import java.io.IOException;

public class InnerTypes {
	
  public static class NamedStaticInnerType {
    public NamedStaticInnerType(int x, int y) {
    }

    void NamedStaticInnerType(int x, int y) {
    }
    
    public static class NamedStaticInnerType2 {
      public NamedStaticInnerType2(int x, int y) {
      }

      void NamedStaticInnerType2(int x, int y) {
      }
    }
    
    public static class NamedNonStaticInnerType2 {
      public NamedNonStaticInnerType2(int x) {
      }
      
      void NamedNonStaticInnerType2(int x) {
      }
    }
  }
  
  public static class NamedNonStaticInnerType {
    public NamedNonStaticInnerType(int x) {
    }
		
    void NamedNonStaticInnerType(int x) {
    }
    
    public static class NamedStaticInnerType2 {
      public NamedStaticInnerType2(int x, int y) {
      }

      void NamedStaticInnerType2(int x, int y) {
      }
    }
    
    public static class NamedNonStaticInnerType2 {
      public NamedNonStaticInnerType2(int x) {
      }
      
      void NamedNonStaticInnerType2(int x) {
      }
    }
  }

  public void nestedAnonymousClasses() {
    new Runnable() {

      @Override
      public void run() {
        new Appendable() {
          @Override
          public Appendable append(CharSequence csq, int start, int end)
              throws IOException {
            new Runnable() {
              @Override
              public void run() {
              }
            };
            return null;
          }

          @Override
          public Appendable append(char c) throws IOException {
            new Runnable() {
              @Override
              public void run() {
                new Runnable() {
                  @Override
                  public void run() {
                  }
                };
              }
            };
            return null;
          }

          @Override
          public Appendable append(CharSequence csq) throws IOException {
            new Runnable() {
              @Override
              public void run() {
              }
            };
            return null;
          }
        };
      }
    };
  }

  public void sameName1() {
    class Runner implements Runnable {

      @Override
      public void run() {
      }
    }

    Runner r = new Runner();
    r.run();
  }

  public void sameName2() {
    class Runner implements Runnable {

      @Override
      public void run() {
      }
    }

    Runner r = new Runner();
    r.run();
  }

}
