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

/*
 * Works in 1.1+Swing, 1.4, and all releases in between.
 * Used by the Converter example.
 */

import javax.swing.*;
import javax.swing.event.*;

/**
 * Based on the source code for DefaultBoundedRangeModel,
 * this class stores its value as a double, rather than
 * an int.  The minimum value and extent are always 0.
 **/
public class ConverterRangeModel implements BoundedRangeModel {
    protected ChangeEvent changeEvent = null;
    protected EventListenerList listenerList = new EventListenerList();

    protected int maximum = 10000;
    protected int minimum = 0;
    protected int extent = 0;
    protected double value = 0.0;
    protected double multiplier = 1.0;
    protected boolean isAdjusting = false;

    public ConverterRangeModel() {
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
        fireStateChanged();
    }

    public int getMaximum() {
        return maximum;
    }

    public void setMaximum(int newMaximum) {
        setRangeProperties(value, extent, minimum, newMaximum, isAdjusting);
    }

    public int getMinimum() {
        return (int)minimum;
    }

    public void setMinimum(int newMinimum) {
        System.out.println("In ConverterRangeModel setMinimum");
        //Do nothing.
    }

    public int getValue() {
        return (int)getDoubleValue();
    }

    public void setValue(int newValue) {
        setDoubleValue((double)newValue);
    }

    public double getDoubleValue() {
        return value;
    }

    public void setDoubleValue(double newValue) {
        setRangeProperties(newValue, extent, minimum, maximum, isAdjusting);
    }

    public int getExtent() {
        return (int)extent;
    }

    public void setExtent(int newExtent) {
        //Do nothing.
    }

    public boolean getValueIsAdjusting() {
        return isAdjusting;
    }

    public void setValueIsAdjusting(boolean b) {
        setRangeProperties(value, extent, minimum, maximum, b);
    }

    public void setRangeProperties(int newValue,
                                   int newExtent,
                                   int newMin,
                                   int newMax,
                                   boolean newAdjusting) {
        setRangeProperties((double)newValue,
                           newExtent,
                           newMin,
                           newMax,
                           newAdjusting);
    }

    public void setRangeProperties(double newValue,
                                   int unusedExtent,
                                   int unusedMin,
                                   int newMax,
                                   boolean newAdjusting) {
        if (newMax <= minimum) {
            newMax = minimum + 1;
        }
        if (Math.round(newValue) > newMax) { //allow some rounding error
            newValue = newMax;
        }

        boolean changeOccurred = false;
        if (newValue != value) {
            value = newValue;
            changeOccurred = true;
        }
        if (newMax != maximum) {
            maximum = newMax;
            changeOccurred = true;
        }
        if (newAdjusting != isAdjusting) {
            maximum = newMax;
            isAdjusting = newAdjusting;
            changeOccurred = true;
        }

        if (changeOccurred) {
            fireStateChanged();
        }
    }

    /*
     * The rest of this is event handling code copied from
     * DefaultBoundedRangeModel.
     */
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    protected void fireStateChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }
}
