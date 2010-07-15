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
package org.apache.commons.jelly.tags.jface.wizard;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.core.UseBeanTag;
import org.apache.commons.jelly.tags.jface.preference.PreferencePageTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 *  This Tag creates a JFace WizardPage
 *
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster</a>
 */
public class WizardPageTag extends UseBeanTag {

    /**
     * Implementation of a WizardPage
     * method createControl is called on Dialog.open()
     */
    public class WizardPageImpl extends WizardPage {
        private Composite parentComposite;

        public WizardPageImpl(String title) {
            super(title);
        }

        public void createControl(Composite parent) {
            // set initial parent Control to avoid a NPE during invokeBody
            setControl(parent);

            // create page contents
            try {
                invokeBody(output);
            } catch (JellyTagException e) {
                log.error(e);
            }

            // parentComposite should be first Composite child
            if (parentComposite != null) {
                setControl(parentComposite);
            }
        }

        public Control getParentControl() {
            return parentComposite;
        }
        public void setParentComposite(Composite parentComposite) {
            this.parentComposite = parentComposite;
        }

    }

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(PreferencePageTag.class);

    /** Jelly XMLOutput */
    private XMLOutput output;

    /**
     * @param theClass
     */
    public WizardPageTag(Class theClass) {
        super(theClass);
    }

    /*
     * @see org.apache.commons.jelly.Tag#doTag(org.apache.commons.jelly.XMLOutput)
     */
    public void doTag(XMLOutput output) throws JellyTagException {
        // check location
        WizardDialogTag wizardTag = (WizardDialogTag) findAncestorWithClass(WizardDialogTag.class);
        if (wizardTag == null) {
            throw new JellyTagException("This tag must be nested within a <wizardDialog>");
        }

        // check for missing attributes
        String title = (String) getAttributes().get("title");
        if (title == null) {
            throw new MissingAttributeException("title");
        }

        // get WizardPageImpl
        WizardPageImpl page = new WizardPageImpl(title);
        setBean(page);
        setBeanProperties(page, getAttributes());

        String var = (String) getAttributes().get("var");
        processBean(var, page);

        // get Wizard
        WizardDialogTag.WizardDialogImpl dialog = wizardTag.getWizardDialogImpl();
        Wizard wizard = (Wizard) dialog.getWizard();

        // add WizardPage to the Wizard
        wizard.addPage(page);

        // used by implementing page
        this.output = output;
    }

    /**
     * Get the WizardPageImpl
     * @return WizardPageImpl
     */
    public WizardPageImpl getWizardPageImpl() {
        Object bean = getBean();
        if (bean instanceof WizardPageImpl) {
            return (WizardPageImpl) bean;
        }
        return null;
    }

}
