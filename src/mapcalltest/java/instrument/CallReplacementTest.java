package instrument;

import static org.junit.Assert.assertTrue;

import input.SystemExitClass;
import javax.swing.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import randoop.SystemExitCalledError;

/** Tests the mapcall agent. */
public class CallReplacementTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void systemExitTest() {

    SystemExitClass obj = new SystemExitClass(5);
    obj.checkValue(5);
    assertTrue("this should pass", true);

    int value = 0;
    thrown.expect(SystemExitCalledError.class);
    thrown.expectMessage("System exit with status " + value + " ignored");
    obj.checkValue(value);
  }

  /* code borrowed from {@code createAndShowGUI()} in Oracle example components.DialogDemo */
  /* this test fails in Travis b/c creating a JFrame in headless environment.  Not sure why replacement not working.
  @Test
  public void swingTest() {
    thrown = ExpectedException.none();

    JFrame frame = new JFrame("TestFrame");

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    //Create and set up the content pane.
    DialogDemo newContentPane = new DialogDemo(frame);
    newContentPane.setOpaque(true); //content panes must be opaque
    frame.setContentPane(newContentPane);

    //Display the window.
    frame.pack();

    frame.setVisible(true);
  }
  */
}
