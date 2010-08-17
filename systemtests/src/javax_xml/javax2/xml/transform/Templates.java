/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)Templates.java	1.9 03/01/23
 */
package javax2.xml.transform;

import java.util.Properties;

import javax2.xml.transform.TransformerException;


/**
 * An object that implements this interface is the runtime representation of processed
 * transformation instructions.
 *
 * <p>Templates must be threadsafe for a given instance
 * over multiple threads running concurrently, and may
 * be used multiple times in a given session.</p>
 */
public interface Templates {

    /**
     * Create a new transformation context for this Templates object.
     *
     * @return A valid non-null instance of a Transformer.
     *
     * @throws TransformerConfigurationException if a Transformer can not be created.
     */
    Transformer newTransformer() throws TransformerConfigurationException;

    /**
     * Get the static properties for xsl:output.  The object returned will
     * be a clone of the internal values. Accordingly, it can be mutated
     * without mutating the Templates object, and then handed in to
     * {@link javax2.xml.transform.Transformer#setOutputProperties}.
     *
     * <p>The properties returned should contain properties set by the stylesheet,
     * and these properties are "defaulted" by default properties specified by
     * <a href="http://www.w3.org/TR/xslt#output">section 16 of the
     * XSL Transformations (XSLT) W3C Recommendation</a>.  The properties that
     * were specifically set by the stylesheet should be in the base
     * Properties list, while the XSLT default properties that were not
     * specifically set should be in the "default" Properties list.  Thus,
     * getOutputProperties().getProperty(String key) will obtain any
     * property in that was set by the stylesheet, <em>or</em> the default
     * properties, while
     * getOutputProperties().get(String key) will only retrieve properties
     * that were explicitly set in the stylesheet.</p>
     *
     * <p>For XSLT,
     * <a href="http://www.w3.org/TR/xslt#attribute-value-templates">Attribute
     * Value Templates</a> attribute values will
     * be returned unexpanded (since there is no context at this point).  The
     * namespace prefixes inside Attribute Value Templates will be unexpanded,
     * so that they remain valid XPath values.  (For XSLT 1.0, this is not
     * a problem since Attribute Value Templates are not allowed for xsl:output
     * attributes.  However, the will be allowed in versions after 1.1.)</p>
     *
     * @return A Properties object, never null.
     */
    Properties getOutputProperties();
}
