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
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.expression.Expression;
import java.util.Locale;

/**
 * Support for tag handlers for &lt;setLocale&gt;, the bundle setting
 * tag in JSTL.
 * @author <a href="mailto:willievu@yahoo.com">Willie Vu</a>
 * @version $Revision: 1.5 $
 *
 */
public class SetBundleTag extends TagSupport {

    private String var;

    private Expression basename;

    private String scope;

    /** Creates a new instance of SetBundleTag */
    public SetBundleTag() {
    }

    /**
     * Evaluates this tag after all the tags properties have been initialized.
     *
     */
    public void doTag(XMLOutput output) throws JellyTagException {
        Object basenameInput = null;
        if (this.basename != null) {
            basenameInput = this.basename.evaluate(context);
        }

        LocalizationContext locCtxt = BundleTag.getLocalizationContext(
            context, (String) basenameInput);

        String varname = (var != null) ? var : Config.FMT_LOCALIZATION_CONTEXT;

        if (scope != null) {
            context.setVariable(varname, scope, locCtxt);
        }
        else {
            context.setVariable(varname, locCtxt);
        }
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setBasename(Expression basename) {
        this.basename = basename;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
