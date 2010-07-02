/*
 * Copyright 2002,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jelly.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.io.UnsupportedEncodingException;

import javax.swing.ButtonGroup;
import javax.swing.DebugGraphics;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import junit.framework.TestSuite;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.test.BaseJellyTest;

/** Tests many swing tags for basic functionality.
 * @author Hans Gilde
 *
 */
public class TestSwingTags extends BaseJellyTest {

    /**
     * @param name
     */
    public TestSwingTags(String name) {
        super(name);
    }

    public static TestSuite suite() throws Exception {
        return new TestSuite(TestSwingTags.class);
    }

    /** Tests some basic Swing tag functions like creating components
     * , adding them to the parent container and setting bean values.
     * @throws Exception
     */
    public void testBasicComponentFunctions() throws Exception {
        if (!isAWTAvailable()) return;
        runSwingScript("test.simple");
        JellyContext context = getJellyContext();
        JFrame frame = (JFrame) context.getVariable("frame");
        assertEquals(new Dimension(100,100), frame.getSize());
        assertEquals(new Point(200,200), frame.getLocation());
        JPanel panel = (JPanel) componentByName(frame.getContentPane(), "panel");
        JButton button = (JButton) componentByName(panel, "button");
        assertNotNull(button);
        assertEquals(new Color(0x11,0x22,0x33), button.getBackground());
        assertEquals(new Color(0x44,0x55,0x66), button.getForeground());
        assertEquals(DebugGraphics.FLASH_OPTION|DebugGraphics.LOG_OPTION, panel.getDebugGraphicsOptions());
        assertEquals(DebugGraphics.BUFFERED_OPTION, button.getDebugGraphicsOptions());
    }

    /** Tests the GridbagLayout tags, making sure that the constraints are
     * set properly including inheritance and basedOn.
     * @throws Exception
     */
    public void testGridBagBasic() throws Exception {
        if (!isAWTAvailable()) return;
        runSwingScript("test.gbc");
        JellyContext context = getJellyContext();
        JFrame frame = (JFrame) context.getVariable("frame");
        JButton button = (JButton) componentByName(frame.getContentPane(), "button");
        JButton button2 = (JButton) componentByName(frame.getContentPane(), "button2");
        GridBagLayout layout = (GridBagLayout) frame.getContentPane().getLayout();
        GridBagConstraints constraints = layout.getConstraints(button);

        // this is failing
        assertEquals(GridBagConstraints.NORTH,constraints.anchor);
        assertEquals(GridBagConstraints.VERTICAL, constraints.fill);
        assertEquals(3, constraints.gridheight);
        assertEquals(2, constraints.gridwidth);
        assertEquals(4, constraints.gridx);
        assertEquals(5, constraints.gridy);
        assertEquals(7, constraints.ipadx);
        assertEquals(8, constraints.ipady);
        assertEquals(0.3, constraints.weightx, 0);
        assertEquals(new Insets(1,2,3,4), constraints.insets);
        assertEquals(0.6, constraints.weighty, 0);

        GridBagConstraints constraints2 = layout.getConstraints(button2);
        assertEquals(1, constraints2.gridx);
        assertEquals(2, constraints2.gridy);
        assertEquals(2, constraints2.ipadx);
        assertEquals(9, constraints2.ipady);
        assertEquals(new Insets(3,4,5,6), constraints2.insets);
    }

    public void testGridBag14() throws Exception {
        if (!isAWTAvailable()) return;
        if (System.getProperty("java.version").startsWith("1.4")) {
            runSwingScript("test.gbc14");
            JellyContext context = getJellyContext();
            JFrame frame = (JFrame) context.getVariable("frame");
            JButton button = (JButton) componentByName(frame.getContentPane(), "button");
            GridBagLayout layout = (GridBagLayout) frame.getContentPane().getLayout();
            GridBagConstraints constraints = layout.getConstraints(button);
            //note that 21 is the JDK 1.4 value of GridBagConstraint.LINE_START
            assertEquals(21,constraints.anchor);
        }
    }

    public void testGridBagFail(){
        if (!isAWTAvailable()) return;
        try {
            runSwingScript("test.gbcBad");
        } catch (Exception e) {
            //success
            return;
        }
        fail("Should have thrown an exception for a bad GBC anchor");
    }

    public void testButtonGroup() throws Exception {
        if (!isAWTAvailable()) return;
        runSwingScript("test.buttonGroup");
        JellyContext context = getJellyContext();
        ButtonGroup bg = (ButtonGroup) context.getVariable("bg");
        assertEquals(3, bg.getButtonCount());
        assertNotNull(bg.getSelection());
    }

    public void testInvalidBeanProperty() throws Exception {
        if (!isAWTAvailable()) return;
        try {
            runSwingScript("test.invalidProperty");
        } catch (Exception e) {
            //success
            return;
        }
        fail("Should have thrown an exception due to an invalid bean property.");
    }

    protected void runSwingScript(String testName) throws Exception {
        setUpScript("swingTags.jelly");
        Script script = getJelly().compileScript();
        getJellyContext().getVariables().clear();
        getJellyContext().setVariable(testName,Boolean.TRUE);
        script.run(getJellyContext(),getXMLOutput());
    }

    /** Searches a container for a component with a given name. Searches only
     * the immediate container, not child containers.
     * @param container the Container to search in
     * @param name the name to look for
     * @return the first component with the given name
     * @throws Exception if the name isn't found
     */
    protected static Component componentByName(Container container, String name) throws Exception{
        Component[] components = container.getComponents();

        for (int i=0;i<components.length;i++) {
            Component component = components[i];
            if (component.getName().equals(name)) {
                return component;
            }
        }

        throw new Exception("Component " + name + " not found in container " + container);
    }

    /**
     * @return true if we are running with AWT present
     */
    private boolean isAWTAvailable() {
        return !Boolean.getBoolean("java.awt.headless");
    }
    /* (non-Javadoc)
     * @see org.apache.commons.jelly.core.BaseJellyTest#getXMLOutput()
     */
    protected XMLOutput getXMLOutput() {
        try {
            return XMLOutput.createXMLOutput(System.out);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
