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
package org.apache.commons.jelly.tags.jface;

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.impl.TagFactory;
import org.apache.commons.jelly.tags.jface.preference.FieldEditorTag;
import org.apache.commons.jelly.tags.jface.preference.PreferenceDialogTag;
import org.apache.commons.jelly.tags.jface.preference.PreferencePageTag;
import org.apache.commons.jelly.tags.jface.window.ApplicationWindowTag;
import org.apache.commons.jelly.tags.jface.wizard.WizardDialogTag;
import org.apache.commons.jelly.tags.jface.wizard.WizardPageTag;
import org.apache.commons.jelly.tags.swt.SwtTagLibrary;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.xml.sax.Attributes;

/**
 * A Jelly custom tag library that creates JFace user interfaces
 * This taglib extends the SWT tag lib
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class JFaceTagLibrary extends SwtTagLibrary {

    public JFaceTagLibrary() {

        // Viewer tags
        registerViewerTag("tableViewer", TableViewer.class);
        registerViewerTag("tableTreeViewer", TableTreeViewer.class);
        registerViewerTag("treeViewer", TreeViewer.class);
        registerViewerTag("checkboxTreeViewer", CheckboxTreeViewer.class);

        // Event tags
        registerTag("doubleClickListener", DoubleClickListenerTag.class);
        registerTag("selectionChangedListener", SelectionChangedListenerTag.class);

        // Window tags
        registerWindowTag("applicationWindow", ApplicationWindow.class);

        // ContributionManager tags
        registerMenuManager("menuManager", MenuManagerTag.class);

        // Action tags
        registerActionTag("action", ActionTag.class);

        // ContributionItem tags
        registerContributionItemTag("separator", Separator.class);

        // Wizard tags
        registerWizardDialogTag("wizardDialog", WizardDialogTag.class);
        registerWizardPageTag("wizardPage", WizardPageTag.class);

        // Preference tags
        registerPreferenceDialogTag("preferenceDialog", PreferenceDialogTag.class);
        registerTag("preferencePage", PreferencePageTag.class);
        registerFieldEditorTag("booleanFieldEditor", BooleanFieldEditor.class);
        registerFieldEditorTag("colorFieldEditor", ColorFieldEditor.class);
        registerFieldEditorTag("directoryFieldEditor", DirectoryFieldEditor.class);
        registerFieldEditorTag("fileFieldEditor", FileFieldEditor.class);
        registerFieldEditorTag("fontFieldEditor", FontFieldEditor.class);
        registerFieldEditorTag("integerFieldEditor", IntegerFieldEditor.class);
        //registerFieldEditorTag("radioGroupFieldEditor", RadioGroupFieldEditor.class);
        //registerFieldEditorTag("stringButtonFieldEditor", StringButtonFieldEditor.class);
        registerFieldEditorTag("stringFieldEditor", StringFieldEditor.class);

    }

    /**
     * @param string
     * @param class1
     */
    private void registerMenuManager(String name, final Class theClass) {
        registerTagFactory(name, new TagFactory() {
            /**
             * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
             */
            public Tag createTag(String name, Attributes attributes) throws JellyException {
                return new MenuManagerTag();
            }
        });

    }

    /**
     * Register a widget tag for the given name
     *
     * @param name
     * @param widgetClass
     */
    protected void registerViewerTag(String name, Class widgetClass) {
        registerViewerTag(name, widgetClass, SWT.NULL);
    }

    /**
     * Register a widget tag for the given name
     *
     * @param name
     * @param widgetClass
     * @param style
     */
    protected void registerViewerTag(String name, final Class theClass, final int style) {
        registerTagFactory(name, new TagFactory() {
            /**
             * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
             */
            public Tag createTag(String name, Attributes attributes) throws JellyException {
                return new ViewerTag(theClass, style);
            }
        });
    }

    /**
     * Register a widget tag for the given name
     *
     * @param name
     * @param widgetClass
     * @param style
     */
    protected void registerWindowTag(String name, final Class theClass) {
        registerTagFactory(name, new TagFactory() {
            /**
             * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
             */
            public Tag createTag(String name, Attributes attributes) throws JellyException {
                return new ApplicationWindowTag(theClass);
            }
        });
    }

    /**
     * Register an action tag for the given name
     */
    protected void registerActionTag(String name, final Class theClass) {
        registerTagFactory(name, new TagFactory() {
            /**
             * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
             */
            public Tag createTag(String name, Attributes attributes) throws JellyException {
                return new ActionTag(theClass);
            }
        });
    }

    /**
       * Register a contribution item tag for the given name
       */
    protected void registerContributionItemTag(String name, final Class theClass) {
        registerTagFactory(name, new TagFactory() {
            /**
             * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
             */
            public Tag createTag(String name, Attributes attributes) throws JellyException {
                return new ContributionItemTag(theClass);
            }
        });
    }

    /**
     * @param name
     * @param theClass
     */
    protected void registerPreferenceDialogTag(String name, final Class theClass) {
        registerTagFactory(name, new TagFactory() {
            /**
             * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
             */
            public Tag createTag(String name, Attributes attributes) throws JellyException {
                return new PreferenceDialogTag(theClass);
            }
        });
    }

    /**
     * @param name
     * @param theClass
     */
    protected void registerFieldEditorTag(String name, final Class theClass) {
        registerTagFactory(name, new TagFactory() {
            /**
             * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
             */
            public Tag createTag(String name, Attributes attributes) throws JellyException {
                return new FieldEditorTag(theClass);
            }
        });
    }

    /**
     * @param name
     * @param theClass
     */
    protected void registerWizardDialogTag(String name, final Class theClass) {
        registerTagFactory(name, new TagFactory() {
            /**
             * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
             */
            public Tag createTag(String name, Attributes attributes) throws JellyException {
                return new WizardDialogTag(theClass);
            }
        });
    }

    protected void registerWizardPageTag(String name, final Class theClass) {
        registerTagFactory(name, new TagFactory() {
            /**
             * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
             */
            public Tag createTag(String name, Attributes attributes) throws JellyException {
                return new WizardPageTag(theClass);
            }
        });
    }

    /**
     * Register a widget tag for the given name
     */
    protected void registerWidgetTag(String name, Class widgetClass) {
        registerWidgetTag(name, widgetClass, SWT.NULL);
    }

    /**
     * Register a widget tag for the given name
     */
    protected void registerWidgetTag(String name, final Class widgetClass, final int style) {
        registerTagFactory(name, new TagFactory() {
            /**
             * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
             */
            public Tag createTag(String name, Attributes attributes) throws JellyException {
                return new JFaceWidgetTag(widgetClass, style);
            }
        });
    }

    /**
     * Register a layout tag for the given name
     */
    protected void registerLayoutTag(String name, final Class layoutClass) {
        registerTagFactory(name, new TagFactory() {
            /**
             * @see org.apache.commons.jelly.impl.TagFactory#createTag(java.lang.String, org.xml.sax.Attributes)
             */
            public Tag createTag(String name, Attributes attributes) throws JellyException {
                return new JFaceLayoutTag(layoutClass);
            }
        });
    }

}
