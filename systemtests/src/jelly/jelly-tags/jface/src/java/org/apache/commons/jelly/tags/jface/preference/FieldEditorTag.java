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
package org.apache.commons.jelly.tags.jface.preference;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.core.UseBeanTag;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * This Tag creates a JFace FieldEditor
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class FieldEditorTag extends UseBeanTag {

    public FieldEditorTag(Class arg0) {
        super(arg0);
    }

    /*
     * @see org.apache.commons.jelly.Tag#doTag(org.apache.commons.jelly.XMLOutput)
     */
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        PreferencePageTag tag = (PreferencePageTag) findAncestorWithClass(PreferencePageTag.class);
        if (tag == null) {
            throw new JellyTagException("This tag must be nested inside a <preferencePage>");
        }

        // get new instance of FieldEditor
        PreferencePageTag.PreferencePageImpl page = tag.getPreferencePageImpl();
        getAttributes().put("parentComposite", page.getFieldEditorParent());

        // add fieldEditor to PreferencePage
        Object fieldEditor = newInstance(getDefaultClass(), getAttributes(), output);
        if (fieldEditor instanceof FieldEditor) {
            page.addField((FieldEditor) fieldEditor);
        }

    }

    /*
     * @see org.apache.commons.jelly.tags.core.UseBeanTag#newInstance(java.lang.Class, java.util.Map, org.apache.commons.jelly.XMLOutput)
     */
    protected Object newInstance(Class theClass, Map attributes, XMLOutput output)
        throws JellyTagException {

        if (theClass == null) {
            throw new JellyTagException("No Class available to create the FieldEditor");
        }

        String name = (String) attributes.get("name");
        if (name == null) {
            throw new MissingAttributeException("name");
        }

        String labelText = (String) attributes.get("labelText");
        if (labelText == null) {
            throw new MissingAttributeException("labelText");
        }

        Composite parentComposite = (Composite) attributes.get("parentComposite");
        if (parentComposite == null) {
            throw new MissingAttributeException("parentComposite");
        }

        // let's try to call a constructor
        try {
            Class[] types = { String.class, String.class, Composite.class };
            Constructor constructor = theClass.getConstructor(types);
            if (constructor != null) {
                Object[] arguments = { name, labelText, parentComposite };
                return constructor.newInstance(arguments);
            }
            return theClass.newInstance();

        } catch (NoSuchMethodException e) {
            throw new JellyTagException(e);
        } catch (InstantiationException e) {
            throw new JellyTagException(e);
        } catch (IllegalAccessException e) {
            throw new JellyTagException(e);
        } catch (InvocationTargetException e) {
            throw new JellyTagException(e);
        }
    }

}
