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
import java.util.TimeZone;

/**
 * Support for tag handlers for &lt;setTimeZone&gt;, the time zone setting
 * tag in JSTL.
 * @author <a href="mailto:willievu@yahoo.com">Willie Vu</a>
 * @version $Revision: 1.5 $
 *
 */
public class SetTimeZoneTag extends TagSupport {

    private Expression value;

    private String var;

    private String scope;

    /** Creates a new instance of SetLocaleTag */
    public SetTimeZoneTag() {
    }

    /**
     * Evaluates this tag after all the tags properties have been initialized.
     *
     */
    public void doTag(XMLOutput output) throws JellyTagException {
        TimeZone timeZone = null;

        Object valueInput = null;
        if (this.value != null) {
            valueInput = this.value.evaluate(context);
        }


        if (valueInput == null) {
            timeZone = TimeZone.getTimeZone("GMT");
        }
        else if (valueInput instanceof String) {
            if (((String) valueInput).trim().equals("")) {
                timeZone = TimeZone.getTimeZone("GMT");
            } else {
                timeZone = TimeZone.getTimeZone((String) valueInput);
            }
        } else {
            timeZone = (TimeZone) valueInput;
        }

        if (scope != null) {
            context.setVariable(Config.FMT_TIME_ZONE, scope, timeZone);
        }
        else {
            context.setVariable(Config.FMT_TIME_ZONE, timeZone);
        }
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
