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
package org.apache.commons.jelly.tags.swing;

import java.awt.Component;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;

/** Implements a ButtonGroup. This tag acts like a Swing component
 * except that adding a component other than an AbstractButton, will be passed
 * through to the parent tag. This is meant to make the
 * buttonGroup easier to use like this:
 * <pre>
 * &lt;panel&gt;
 *  &lt;buttonGroup&gt;
 *      &lt;panel&gt;
 *          &lt;radioButton/&gt;
 *      &lt;/panel&gt;
 *      &lt;panel&gt;
 *          &lt;radioButton/&gt;
 *      &lt;/panel&gt;
 *  &lt;/buttonGroup&gt;
 * &lt;/panel&gt;
 * </pre>
 *
 * <p> Note that the following construct will silently fail, and shame on s/he who even tried it:
 * <pre>
  * &lt;panel&gt;
 *  &lt;buttonGroup&gt;
 *      &lt;font .../&gt;
 *      &lt;panel&gt;
 *          &lt;radioButton/&gt;
 *      &lt;/panel&gt;
 *      &lt;panel&gt;
 *          &lt;radioButton/&gt;
 *      &lt;/panel&gt;
 *  &lt;/buttonGroup&gt;
 * &lt;/panel&gt;
 * </pre>
 * </p>
 *
 * @author Hans Gilde
 *
 */
public class ButtonGroupTag extends ComponentTag {

    /**If the child is an AbstractButton, add it to the button group. Otherwise,
     * pass through to the parent component tag.
     * @throws JellyTagException
     * @see org.apache.commons.jelly.tags.swing.ContainerTag#addChild(java.awt.Component, java.lang.Object)
     */
    public void addChild(Component component, Object constraints) throws JellyTagException {
        if (component instanceof AbstractButton) {
            getButtonGroup().add((AbstractButton) component);
        } else {
            if ( component != null ) {
                ContainerTag parentTag = (ContainerTag) findAncestorWithClass( ContainerTag.class );
                if ( parentTag != null ) {
                    parentTag.addChild(component, getConstraint());
                }
                else {
                    throw new JellyTagException( "This buttonGroup tag must be nested within a Swing component tag." );
                }
            }
        }
    }

    /**Creates a new buttonGroup.
     * @see org.apache.commons.jelly.tags.core.UseBeanTag#newInstance(java.lang.Class, java.util.Map, org.apache.commons.jelly.XMLOutput)
     */
    protected Object newInstance(Class theClass, Map attributes,
            XMLOutput output) throws JellyTagException {
        return new ButtonGroup();
    }

    protected ButtonGroup getButtonGroup() {
        return (ButtonGroup) getBean();
    }
}
