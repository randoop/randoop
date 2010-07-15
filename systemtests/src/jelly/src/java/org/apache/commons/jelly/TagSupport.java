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
package org.apache.commons.jelly;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.jelly.util.TagUtils;

/** <p><code>TagSupport</code> an abstract base class which is useful to
  * inherit from if developing your own tag.</p>
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.35 $
  */

public abstract class TagSupport implements Tag {

    /** the parent of this tag */
    protected Tag parent;

    /** the body of the tag */
    protected Script body;
    /** The current context */

    protected Boolean shouldTrim;
    protected boolean hasTrimmed;

    protected JellyContext context;

    /** whether xml text should be escaped */
    private boolean escapeText = true;

    /**
     * Searches up the parent hierarchy from the given tag
     * for a Tag of the given type
     *
     * @param from the tag to start searching from
     * @param tagClass the type of the tag to find
     * @return the tag of the given type or null if it could not be found
     */
    public static Tag findAncestorWithClass(Tag from, Class tagClass) {
        // we could implement this as
        //  return findAncestorWithClass(from,Collections.singleton(tagClass));
        // but this is so simple let's save the object creation for now
        while (from != null) {
            if (tagClass.isInstance(from)) {
                return from;
            }
            from = from.getParent();
        }
        return null;
    }

    /**
     * Searches up the parent hierarchy from the given tag
     * for a Tag matching one or more of given types.
     *
     * @param from the tag to start searching from
     * @param tagClasses a Collection of Class types that might match
     * @return the tag of the given type or null if it could not be found
     */
    public static Tag findAncestorWithClass(Tag from, Collection tagClasses) {
        while (from != null) {
            for(Iterator iter = tagClasses.iterator();iter.hasNext();) {
                Class klass = (Class)(iter.next());
                if (klass.isInstance(from)) {
                    return from;
                }
            }
            from = from.getParent();
        }
        return null;
    }

    /**
     * Searches up the parent hierarchy from the given tag
     * for a Tag matching one or more of given types.
     *
     * @param from the tag to start searching from
     * @param tagClasses an array of types that might match
     * @return the tag of the given type or null if it could not be found
     * @see #findAncestorWithClass(Tag,Collection)
     */
    public static Tag findAncestorWithClass(Tag from, Class[] tagClasses) {
        return findAncestorWithClass(from,Arrays.asList(tagClasses));
    }

    public TagSupport() {
    }

    public TagSupport(boolean shouldTrim) {
        setTrim( shouldTrim );
    }

    /**
     * Sets whether whitespace inside this tag should be trimmed or not.
     * Defaults to true so whitespace is trimmed
     */
    public void setTrim(boolean shouldTrim) {
        if ( shouldTrim ) {
            this.shouldTrim = Boolean.TRUE;
        }
        else {
            this.shouldTrim = Boolean.FALSE;
        }
    }

    public boolean isTrim() {
        if ( this.shouldTrim == null ) {
            Tag parent = getParent();
            if ( parent == null ) {
                return true;
            }
            else {
                if ( parent instanceof TagSupport ) {
                    TagSupport parentSupport = (TagSupport) parent;

                    this.shouldTrim = ( parentSupport.isTrim() ? Boolean.TRUE : Boolean.FALSE );
                }
                else {
                    this.shouldTrim = Boolean.TRUE;
                }
            }
        }

        return this.shouldTrim.booleanValue();
    }

    /** @return the parent of this tag */
    public Tag getParent() {
        return parent;
    }

    /** Sets the parent of this tag */
    public void setParent(Tag parent) {
        this.parent = parent;
    }

    /** @return the body of the tag */
    public Script getBody() {
        if (! hasTrimmed) {
            hasTrimmed = true;
            if (isTrim()) {
                trimBody();
            }
        }
        return body;
    }

    /** Sets the body of the tag */
    public void setBody(Script body) {
        this.body = body;
        this.hasTrimmed = false;
    }

    /** @return the context in which the tag will be run */
    public JellyContext getContext() {
        return context;
    }

    /** Sets the context in which the tag will be run */
    public void setContext(JellyContext context) throws JellyTagException {
        this.context = context;
    }

    /**
     * Invokes the body of this tag using the given output
     */
    public void invokeBody(XMLOutput output) throws JellyTagException {
        getBody().run(context, output);
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    /**
     * Searches up the parent hierarchy for a Tag of the given type.
     * @return the tag of the given type or null if it could not be found
     */
    protected Tag findAncestorWithClass(Class parentClass) {
        return findAncestorWithClass(getParent(), parentClass);
    }

    /**
     * Searches up the parent hierarchy for a Tag of one of the given types.
     * @return the tag of the given type or null if it could not be found
     * @see #findAncestorWithClass(Collection)
     */
    protected Tag findAncestorWithClass(Class[] parentClasses) {
        return findAncestorWithClass(getParent(),parentClasses);
    }

    /**
     * Searches up the parent hierarchy for a Tag of one of the given types.
     * @return the tag of the given type or null if it could not be found
     */
    protected Tag findAncestorWithClass(Collection parentClasses) {
        return findAncestorWithClass(getParent(),parentClasses);
    }

    /**
     * Executes the body of the tag and returns the result as a String.
     *
     * @return the text evaluation of the body
     */
    protected String getBodyText() throws JellyTagException {
        return getBodyText(escapeText);
    }

    /**
     * Executes the body of the tag and returns the result as a String.
     *
     * @param shouldEscape Signal if the text should be escaped.
     *
     * @return the text evaluation of the body
     */
    protected String getBodyText(boolean shouldEscape) throws JellyTagException {
        StringWriter writer = new StringWriter();
        invokeBody(XMLOutput.createXMLOutput(writer, shouldEscape));
        return writer.toString();
    }


    /**
     * Find all text nodes inside the top level of this body and
     * if they are just whitespace then remove them
     */
    protected void trimBody() {
        TagUtils.trimScript(body);
    }

    /**
     * Returns whether the body of this tag will be escaped or not.
     */
    public boolean isEscapeText() {
        return escapeText;
    }

    /**
     * Sets whether the body of the tag should be escaped as text (so that &lt; and &gt; are
     * escaped as &amp;lt; and &amp;gt;), which is the default or leave the text as XML.
     */
    public void setEscapeText(boolean escapeText) {
        this.escapeText = escapeText;
    }
}
