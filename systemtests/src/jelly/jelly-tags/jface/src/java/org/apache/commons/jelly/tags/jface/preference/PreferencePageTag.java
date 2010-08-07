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

import java.io.IOException;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Composite;

/**
 * This Tag creates a JFace PreferencePage
 *
 * Provides a concrete preference store implementation based on an internal java.util.Properties object
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class PreferencePageTag extends TagSupport {

    /**
     * Implementation of a FieldEditorPreferencePage
     * method createFieldEditors is called on Dialog.open()
     */
    public class PreferencePageImpl extends FieldEditorPreferencePage {
        private PreferenceStore preferenceStore;

        public PreferencePageImpl(String title) {
            super(title, FieldEditorPreferencePage.GRID);
            try {
                preferenceStore = new PreferenceStore(filename);
                preferenceStore.load();
                setPreferenceStore(preferenceStore);
            } catch (IOException e) {
                log.error(e);
            }
        }

        public void addField(FieldEditor editor) {
            super.addField(editor);
        }

        protected void createFieldEditors() {
            try {
                invokeBody(output);
            } catch (JellyTagException e) {
                log.error(e);
            }
        }

        public Composite getFieldEditorParent() {
            return super.getFieldEditorParent();
        }

        public IPreferenceStore getPreferenceStore() {
            return preferenceStore;
        }
    }

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(PreferencePageTag.class);

    /** Filename of the store */
    private String filename;

    /** Jelly XMLOutput */
    private XMLOutput output;

    /** Current PreferencePageImpl */
    private PreferencePageImpl page;

    /** Title of both PreferenceNode and PreferencePage */
    private String title;

    /*
     * @see org.apache.commons.jelly.Tag#doTag(org.apache.commons.jelly.XMLOutput)
     */
    public void doTag(XMLOutput output) throws JellyTagException {
        // check location
        PreferenceDialogTag dialogTag =
            (PreferenceDialogTag) findAncestorWithClass(PreferenceDialogTag.class);
        if (dialogTag == null) {
            throw new JellyTagException("This tag must be nested within a <preferenceDialog>");
        }

        // check for missing attributes
        if (filename == null) {
            throw new MissingAttributeException("filename");
        }
        if (title == null) {
            throw new MissingAttributeException("title");
        }

        // build new PreferenceNode with same title as the PreferencePage
        PreferenceDialog dialog = dialogTag.getPreferenceDialog();
        PreferenceNode node = new PreferenceNode(title);

        // build new PreferencePage
        page = new PreferencePageImpl(title);

        // add node to PreferenceManager
        node.setPage(page);
        dialog.getPreferenceManager().addToRoot(node);

        // used by PreferencePageImpl
        this.output = output;
    }

    /**
     * Get the PreferencePageImpl
     * @return PreferencePageImpl
     */
    public PreferencePageImpl getPreferencePageImpl() {
        return page;
    }

    /**
     * Sets the filename.
     * @param filename The filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Sets the title.
     * @param title The title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

}
