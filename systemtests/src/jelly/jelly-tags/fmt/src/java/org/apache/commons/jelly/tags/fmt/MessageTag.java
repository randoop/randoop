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
package org.apache.commons.jelly.tags.fmt;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.expression.Expression;

import org.xml.sax.SAXException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * Support for tag handlers for &lt;message&gt;, the lookup up
 * localized message tag in JSTL.
 * @author <a href="mailto:willievu@yahoo.com">Willie Vu</a>
 * @version 1.1
 *
 * @task decide how to implement setResponseLocale
 */
public class MessageTag extends TagSupport {

    private static final String UNDEFINED_KEY = "???";

    private Expression key;
    private Expression bundle;

    private LocalizationContext locCtxt;

    private String var;

    private String scope;

    private List params;

    /** Creates a new instance of MessageTag */
    public MessageTag() {
        params = new ArrayList();
    }

    /**
     * Adds an argument (for parametric replacement) to this tag's message.
     */
    public void addParam(Object arg) {
        params.add(arg);
    }

    public void doTag(XMLOutput output) throws JellyTagException {

        Object keyInput = null;
        if (this.key != null) {
            keyInput = this.key.evaluate(context);
            // process <param> sub tags
            invokeBody(output);
        }
        else {
            // get key from body
            keyInput = getBodyText();
        }


        if ((keyInput == null) || keyInput.equals("")) {
            try {
                output.write("??????");
            } catch (SAXException e) {
                throw new JellyTagException(e);
            }
            return;
        }

        Object bundleInput = null;
        if (this.bundle != null) {
            bundleInput = this.bundle.evaluate(context);
        }
        if (bundleInput != null && bundleInput instanceof LocalizationContext) {
            locCtxt = (LocalizationContext) bundleInput;
        }

        String prefix = null;
        if (locCtxt == null) {
            Tag t = findAncestorWithClass(this, BundleTag.class);
            if (t != null) {
                // use resource bundle from parent <bundle> tag
                BundleTag parent = (BundleTag) t;
                locCtxt = parent.getLocalizationContext();
                prefix = parent.getPrefixAsString();
            } else {
                locCtxt = BundleTag.getLocalizationContext(context);
            }
        } else {
            // localization context taken from 'bundle' attribute
            if (locCtxt.getLocale() != null) {
                // TODO
                // SetLocaleSupport.setResponseLocale(pageContext,
                // locCtxt.getLocale());
            }
        }

        String message = UNDEFINED_KEY + keyInput + UNDEFINED_KEY;
        if (locCtxt != null) {
            ResourceBundle bundle = locCtxt.getResourceBundle();
            if (bundle != null) {
                try {
                    // prepend 'prefix' attribute from parent bundle
                    if (prefix != null) {
                        keyInput = prefix + keyInput;
                    }
                    message = bundle.getString(keyInput.toString());
                    // Perform parametric replacement if required
                    if (!params.isEmpty()) {
                        Object[] messageArgs = params.toArray();
                        MessageFormat formatter = new MessageFormat("");
                        if (locCtxt.getLocale() != null) {
                            formatter.setLocale(locCtxt.getLocale());
                        }
                        formatter.applyPattern(message);
                        message = formatter.format(messageArgs);
                    }
                } catch (MissingResourceException mre) {
                    message = UNDEFINED_KEY + keyInput + UNDEFINED_KEY;
                }
            }
        }


        if (scope != null) {
            if (var != null) {
                context.setVariable(var, scope, message);
            }
            else {
                throw new JellyTagException( "If 'scope' is specified, 'var' must be defined for this tag" );
            }
        }
        else {
            if (var != null) {
                context.setVariable(var, message);
            }
            else {
                // write the message
                try {
                    output.write(message);
                } catch (SAXException e) {
                    throw new JellyTagException(e);
                }
            }
        }
    }

    /** Setter for property key.
     * @param key New value of property key.
     *
     */
    public void setKey(Expression key) {
        this.key = key;
    }

    /** Setter for property bundle.
     * @param bundle New value of property bundle.
     *
     */
    public void setBundle(Expression bundle) {
        this.bundle = bundle;
    }

    /** Setter for property var.
     * @param var New value of property var.
     *
     */
    public void setVar(String var) {
        this.var = var;
    }

    /** Setter for property scope.
     * @param scope New value of property scope.
     *
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

}
