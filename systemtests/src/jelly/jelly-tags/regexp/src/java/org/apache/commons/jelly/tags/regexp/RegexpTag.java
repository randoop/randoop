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
package org.apache.commons.jelly.tags.regexp;

import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.MalformedPatternException;

/**
 * Base class for tags using the Oro Regexp library.
 *
 * @author <a href="mailto:christian@inx-soft.com">Christian Amor Kvalheim</a>
 * @version $Revision: 1.1 $
 */
public abstract class RegexpTag extends TagSupport {
    private Perl5Matcher patternMatcher = new Perl5Matcher();
    private Pattern pattern;
    private String var;
    private String text;
    private String scope;

    protected final String getText() {
        return text;
    }

    protected final Pattern getPattern() {
        return pattern;
    }

    protected final Perl5Matcher getPatternMatcher() {
        return patternMatcher;
    }

    public final void setExpr(String expr) throws MalformedPatternException {
        Perl5Compiler patternCompiler = new Perl5Compiler();
        pattern = patternCompiler.compile(expr);
    }

    public final void setText(String text) {
        this.text = text;
    }

    // Sets the variable name to define for this expression
    public final void setVar(String var) {
        this.var = var;
    }

    /**
     * Sets the variable scope for this variable. For example setting this value to 'parent' will
     * set this value in the parent scope. When Jelly is run from inside a Servlet environment
     * then other scopes will be available such as 'request', 'session' or 'application'.
     *
     * Other applications may implement their own custom scopes.
     */
    public final void setScope(String scope) {
        this.scope = scope;
    }

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        // Check required properties
        if (getText() == null || getText().length() == 0)
            throw new MissingAttributeException("text must be provided");

        if (pattern == null)
            throw new MissingAttributeException("expr must be provided");

        if (var == null || var.length() == 0)
            throw new MissingAttributeException("var must be provided");

        // Evaluate pattern against text string
        boolean result = getResult();
        String resultString = result ? "true" : "false";

        if (var != null) {
            if (scope != null) {
                context.setVariable(var, scope, resultString);
            } else {
                context.setVariable(var, resultString);
            }
        }
    }

    protected abstract boolean getResult();
}
