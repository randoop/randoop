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
package org.apache.commons.jelly.tags.swt;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;

/**
 * Class to create a {@link GC} instance within Jelly SWT.
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @version CVS $Id: GCTag.java,v 1.2 2004/09/09 12:20:26 dion Exp $
 */
public class GCTag extends TagSupport {

    /** Drawable name */
    private Drawable drawable;

    /** Variable name */
    private String var;

    /**
     * Obtain the {@link Drawable} name for this {@link GC}.
     *
     * @return a {@link GC} {@link Drawable}
     */
    public Drawable getDrawable() {
        return this.drawable;
    }

    /**
     * Set the {@link Drawable} name for this {@link GC}.
     *
     * @param drawable a {@link GC} {@link Drawable}
     */
    public void setDrawable(final Drawable drawable) {
        this.drawable = drawable;
    }

    /**
     * Sets the variable name.
     *
     * @param var the variable name of this {@link GC} instance
     */
    public void setVar(final String var) {
        this.var = var;
    }

    /**
     * Obtain the variable name.
     *
     * @return the variable name of this {@link GC} instance
     */
    public String getVar() {
        return this.var;
    }

    // Tag interface
    //-------------------------------------------------------------------------

    /**
     * Creates a {@link GC} instance and stores it in the Context under a
     * particular variable name. Note, {@link GC} objects can only be created on
     * {@link Drawable} objects.
     *
     * @param output {@link XMLOutput} reference
     * @throws JellyTagException if an error occurs
     * @see org.apache.commons.jelly.Tag#doTag(org.apache.commons.jelly.XMLOutput)
     */
    public void doTag(final XMLOutput output) throws JellyTagException {
        // invoke by body just in case some nested tag configures me
        invokeBody(output);

        final boolean nullDrawable = drawable == null;
        final boolean drawableParent = drawable instanceof Drawable;

        if (nullDrawable || !drawableParent) {
            throw new JellyTagException(
                "This tag must specify a Drawable attribute (ie. Image or Control)"
            );
        }

        if (var == null) {
            throw new JellyTagException("This tag requires a context variable name");
        }

        // store the GC in the context
        context.setVariable(var, new GC(drawable));
    }
}
