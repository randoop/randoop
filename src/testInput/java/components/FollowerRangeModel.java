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
 * Implements a model whose data is actually in another model (the
 * "source model").  The follower model adjusts the values obtained
 * from the source model (or set in the follower model) to be in
 * a different unit of measure.
 *
 */
public class FollowerRangeModel extends ConverterRangeModel
                                implements ChangeListener {
    ConverterRangeModel sourceModel; //the real model

    /** Creates a FollowerRangeModel that gets its state from sourceModel. */
    public FollowerRangeModel(ConverterRangeModel sourceModel) {
        this.sourceModel = sourceModel;
        sourceModel.addChangeListener(this);
    }

    //The only method in the ChangeListener interface.
    public void stateChanged(ChangeEvent e) {
        fireStateChanged();
    }

    public int getMaximum() {
        int modelMax = sourceModel.getMaximum();
        double multiplyBy = sourceModel.getMultiplier()/this.getMultiplier();
        return (int)(modelMax * multiplyBy);
    }

    public void setMaximum(int newMaximum) {
        sourceModel.setMaximum((int)(newMaximum *
                     (this.getMultiplier()/sourceModel.getMultiplier())));
    }

    public int getValue() {
        return (int)getDoubleValue();
    }

    public void setValue(int newValue) {
        setDoubleValue((double)newValue);
    }

    public double getDoubleValue() {
        return sourceModel.getDoubleValue()
               * sourceModel.getMultiplier()
               / this.getMultiplier();
    }

    public void setDoubleValue(double newValue) {
        sourceModel.setDoubleValue(
                     newValue * this.getMultiplier()
                     / sourceModel.getMultiplier());
    }

    public int getExtent() {
        return super.getExtent();
    }

    public void setExtent(int newExtent) {
        super.setExtent(newExtent);
    }

    public void setRangeProperties(int value,
                                   int extent,
                                   int min,
                                   int max,
                                   boolean adjusting) {
        double multiplyBy = this.getMultiplier()/sourceModel.getMultiplier();
        sourceModel.setRangeProperties(value*multiplyBy,
                                     extent, min,
                                     (int)(max*multiplyBy),
                                     adjusting);
    }
}
