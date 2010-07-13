/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)DOMResult.java	1.13 03/01/23
 */
package javax2.xml.transform.dom;

import javax2.xml.transform.*;

import java.lang.String;

import java.io.OutputStream;
import java.io.Writer;

import org.w3c.dom.Node;


/**
 * Acts as a holder for a transformation result tree, in the
 * form of a Document Object Model (DOM) tree. If no output DOM source is set,
 * the transformation will create a Document node as the holder
 * for the result of the transformation, which may be retrieved
 * with getNode.
 */
public class DOMResult implements Result {

    /** If {@link javax2.xml.transform.TransformerFactory#getFeature}
     * returns true when passed this value as an argument,
     * the Transformer supports Result output of this type.
     */
    public static final String FEATURE =
        "http://javax2.xml.transform.dom.DOMResult/feature";

    /**
     * Zero-argument default constructor.
     */
    public DOMResult() {}

    /**
     * Use a DOM node to create a new output target. In practice,
     * the node should be a {@link org.w3c.dom.Document} node,
     * a {@link org.w3c.dom.DocumentFragment} node, or a
     * {@link org.w3c.dom.Element} node.  In other words, a node
     * that accepts children.
     *
     * @param n The DOM node that will contain the result tree.
     */
    public DOMResult(Node node) {
        setNode(node);
    }

    /**
     * Create a new output target with a DOM node. In practice,
     * the node should be a {@link org.w3c.dom.Document} node,
     * a {@link org.w3c.dom.DocumentFragment} node, or a
     * {@link org.w3c.dom.Element} node.  In other words, a node
     * that accepts children.
     *
     * @param node The DOM node that will contain the result tree.
     * @param systemID The system identifier which may be used in association
     * with this node.
     */
    public DOMResult(Node node, String systemID) {
        setNode(node);
        setSystemId(systemID);
    }

    /**
     * Set the node that will contain the result DOM tree.  In practice,
     * the node should be a {@link org.w3c.dom.Document} node,
     * a {@link org.w3c.dom.DocumentFragment} node, or a
     * {@link org.w3c.dom.Element} node.  In other words, a node
     * that accepts children.
     *
     * @param node The node to which the transformation
     * will be appended.
     */
    public void setNode(Node node) {
        this.node = node;
    }

    /**
     * Get the node that will contain the result DOM tree.
     * If no node was set via setNode, the node will be
     * set by the transformation, and may be obtained from
     * this method once the transformation is complete.
     *
     * @return The node to which the transformation
     * will be appended.
     */
    public Node getNode() {
        return node;
    }

    /**
     * Method setSystemId Set the systemID that may be used in association
     * with the node.
     *
     * @param systemId The system identifier as a URI string.
     */
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    /**
     * Get the system identifier that was set with setSystemId.
     *
     * @return The system identifier that was set with setSystemId, or null
     * if setSystemId was not called.
     */
    public String getSystemId() {
        return systemId;
    }

    //////////////////////////////////////////////////////////////////////
    // Internal state.
    //////////////////////////////////////////////////////////////////////

    /**
     * The node to which the transformation will be appended.
     */
    private Node node;

    /**
     * The systemID that may be used in association
     * with the node.
     */
    private String systemId;
}
