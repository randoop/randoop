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

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.expression.Expression;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.TimeZone;

/**
 * Support for tag handlers for &lt;timeZone&gt;, the time zone loading
 * tag in JSTL.
 *
 * @author <a href="mailto:willievu@yahoo.com">Willie Vu</a>
 * @version $Revision: 1.5 $
 *
 * @task decide how to implement setResponseLocale
 */
public class TimeZoneTag extends TagSupport {

    private TimeZone timeZone;
    private Expression value;                    // 'value' attribute


    //*********************************************************************
    // Constructor and initialization

    public TimeZoneTag() {
    }

    //*********************************************************************
    // Collaboration with subtags

    public TimeZone getTimeZone() {
        return timeZone;
    }


    //*********************************************************************
    // Tag logic

    /**
     * Evaluates this tag after all the tags properties have been initialized.
     *
     */
    public void doTag(XMLOutput output) throws JellyTagException {
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

        invokeBody(output);
    }


    //*********************************************************************
    // Package-scoped utility methods

    /*
     * Determines and returns the time zone to be used by the given action.
     *
     * <p> If the given action is nested inside a &lt;timeZone&gt; action,
     * the time zone is taken from the enclosing &lt;timeZone&gt; action.
     *
     * <p> Otherwise, the time zone configuration setting
     * <tt>javax.servlet.jsp.jstl.core.Config.FMT_TIME_ZONE</tt>
     * is used.
     *
     * @param jc the page containing the action for which the
     * time zone needs to be determined
     * @param fromTag the action for which the time zone needs to be
     * determined
     *
     * @return the time zone, or <tt>null</tt> if the given action is not
     * nested inside a &lt;timeZone&gt; action and no time zone configuration
     * setting exists
     */
    static TimeZone getTimeZone(JellyContext jc, Tag fromTag) {
        TimeZone tz = null;

        Tag t = findAncestorWithClass(fromTag, TimeZoneTag.class);
        if (t != null) {
            // use time zone from parent <timeZone> tag
            TimeZoneTag parent = (TimeZoneTag) t;
            tz = parent.getTimeZone();
        } else {
            // get time zone from configuration setting
            Object obj = jc.getVariable(Config.FMT_TIME_ZONE);
            if (obj != null) {
                if (obj instanceof TimeZone) {
                    tz = (TimeZone) obj;
                } else {
                    tz = TimeZone.getTimeZone((String) obj);
                }
            }
        }

        return tz;
    }

    /** Setter for property value.
     * @param value New value of property value.
     *
     */
    public void setValue(Expression value) {
        this.value = value;
    }
}
