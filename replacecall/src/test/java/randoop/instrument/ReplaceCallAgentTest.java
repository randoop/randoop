package randoop.instrument;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.MalformedInputException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for the {@link ReplaceCallAgent}. */
public class ReplaceCallAgentTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void loadExclusionsTest() throws IOException {
    String contents = "// comment\njava.lang\n\n  org.example.  \n";
    assertEquals(
        new LinkedHashSet<>(Arrays.asList("java.lang.", "org.example.")),
        ReplaceCallAgent.loadExclusions(new StringReader(contents), "exclusions.txt"));
  }

  @Test
  public void loadExclusionsInvalidEncodingTest() throws IOException {
    byte[] latin1Bytes = "java.lang\norg.café\n".getBytes(ISO_8859_1);
    thrown.expect(IOException.class);
    thrown.expectMessage("invalidencoding.txt: invalid character encoding");
    thrown.expectCause(isA(MalformedInputException.class));
    ReplaceCallAgent.loadExclusions(
        new InputStreamReader(new ByteArrayInputStream(latin1Bytes), UTF_8.newDecoder()),
        "invalidencoding.txt");
  }
}
