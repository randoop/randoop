/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package components;

import java.awt.*;
import javax.swing.*;

/* Rule.java is used by ScrollDemo.java. */

public class Rule extends JComponent {
    public static final int INCH = Toolkit.getDefaultToolkit().
            getScreenResolution();
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int SIZE = 35;

    public int orientation;
    public boolean isMetric;
    private int increment;
    private int units;

    public Rule(int o, boolean m) {
        orientation = o;
        isMetric = m;
        setIncrementAndUnits();
    }

    public void setIsMetric(boolean isMetric) {
        this.isMetric = isMetric;
        setIncrementAndUnits();
        repaint();
    }

    private void setIncrementAndUnits() {
        if (isMetric) {
            units = (int)((double)INCH / (double)2.54); // dots per centimeter
            increment = units;
        } else {
            units = INCH;
            increment = units / 2;
        }
    }

    public boolean isMetric() {
        return this.isMetric;
    }

    public int getIncrement() {
        return increment;
    }

    public void setPreferredHeight(int ph) {
        setPreferredSize(new Dimension(SIZE, ph));
    }

    public void setPreferredWidth(int pw) {
        setPreferredSize(new Dimension(pw, SIZE));
    }

    protected void paintComponent(Graphics g) {
        Rectangle drawHere = g.getClipBounds();

        // Fill clipping area with dirty brown/orange.
        g.setColor(new Color(230, 163, 4));
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

        // Do the ruler labels in a small font that's black.
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(Color.black);

        // Some vars we need.
        int end = 0;
        int start = 0;
        int tickLength = 0;
        String text = null;

        // Use clipping bounds to calculate first and last tick locations.
        if (orientation == HORIZONTAL) {
            start = (drawHere.x / increment) * increment;
            end = (((drawHere.x + drawHere.width) / increment) + 1)
                  * increment;
        } else {
            start = (drawHere.y / increment) * increment;
            end = (((drawHere.y + drawHere.height) / increment) + 1)
                  * increment;
        }

        // Make a special case of 0 to display the number
        // within the rule and draw a units label.
        if (start == 0) {
            text = Integer.toString(0) + (isMetric ? " cm" : " in");
            tickLength = 10;
            if (orientation == HORIZONTAL) {
                g.drawLine(0, SIZE-1, 0, SIZE-tickLength-1);
                g.drawString(text, 2, 21);
            } else {
                g.drawLine(SIZE-1, 0, SIZE-tickLength-1, 0);
                g.drawString(text, 9, 10);
            }
            text = null;
            start = increment;
        }

        // ticks and labels
        for (int i = start; i < end; i += increment) {
            if (i % units == 0)  {
                tickLength = 10;
                text = Integer.toString(i/units);
            } else {
                tickLength = 7;
                text = null;
            }

            if (tickLength != 0) {
                if (orientation == HORIZONTAL) {
                    g.drawLine(i, SIZE-1, i, SIZE-tickLength-1);
                    if (text != null)
                        g.drawString(text, i-3, 21);
                } else {
                    g.drawLine(SIZE-1, i, SIZE-tickLength-1, i);
                    if (text != null)
                        g.drawString(text, 9, i+3);
                }
            }
        }
    }
}
