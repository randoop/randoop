package randoop.instrument;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import org.junit.Test;

/** Tests for {@link ReplaceCallAgent}. */
public class ReplaceCallAgentTest {

  @Test
  public void getJarPathFromURLTest() throws MalformedURLException {
    URL url = new URL("jar:file:/home/u/my%20tools/replacecall.jar!/randoop/instrument/A.class");
    assertEquals(
        Paths.get("/home/u/my tools/replacecall.jar").toString(),
        ReplaceCallAgent.getJarPathFromURL(url));
  }
}
