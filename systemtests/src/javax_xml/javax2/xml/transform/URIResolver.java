/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)URIResolver.java	1.8 03/01/23
 */
package javax2.xml.transform;

/**
 * <p>An object that implements this interface that can be called by the processor
 * to turn a URI used in document(), xsl:import, or xsl:include into a Source object.
 */
public interface URIResolver {

    /**
     * Called by the processor when it encounters
     * an xsl:include, xsl:import, or document() function.
     *
     * @param href An href attribute, which may be relative or absolute.
     * @param base The base URI in effect when the href attribute
     * was encountered.
     *
     * @return A Source object, or null if the href cannot be resolved,
     * and the processor should try to resolve the URI itself.
     *
     * @throws TransformerException if an error occurs when trying to
     * resolve the URI.
     */
    public Source resolve(String href, String base)
        throws TransformerException;
}
