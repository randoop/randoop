/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)ErrorListener.java	1.7 03/01/23
 */
package javax2.xml.transform;

/**
 * <p>To provide customized error handling, implement this interface and
 * use the setErrorListener method to register an instance of the implmentation
 * with the {@link javax2.xml.transform.Transformer}. The Transformer then reports
 * all errors and warnings through this interface.</p>
 *
 * <p>If an application does <em>not</em>
 * register an ErrorListener, errors are reported to System.err.</p>
 *
 * <p>For transformation errors, a Transformer must use this interface
 * instead of throwing an exception: it is up to the application
 * to decide whether to throw an exception for different types of
 * errors and warnings.  Note however that the Transformer is not required
 * to continue with the transformation after a call to fatalError.</p>
 *
 * <p>Transformers may use this mechanism to report XML parsing errors
 * as well as transformation errors.</p>
 */
public interface ErrorListener {

    /**
     * Receive notification of a warning.
     *
     * <p>{@link javax2.xml.transform.Transformer} can use this method to report
     * conditions that are not errors or fatal errors.  The default behaviour
     * is to take no action.</p>
     *
     * <p>After invoking this method, the Transformer must continue with
     * the transformation. It should still be possible for the
     * application to process the document through to the end.</p>
     *
     * @param exception The warning information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax2.xml.transform.TransformerException if the application
     * chooses to discontinue the transformation.
     *
     * @see javax2.xml.transform.TransformerException
     */
    public abstract void warning(TransformerException exception)
        throws TransformerException;

    /**
     * Receive notification of a recoverable error.
     *
     * <p>The transformer must continue to try and provide normal transformation
     * after invoking this method.  It should still be possible for the
     * application to process the document through to the end if no other errors
     * are encountered.</p>
     *
     * @param exception The error information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax2.xml.transform.TransformerException if the application
     * chooses to discontinue the transformation.
     *
     * @see javax2.xml.transform.TransformerException
     */
    public abstract void error(TransformerException exception)
        throws TransformerException;

    /**
     * Receive notification of a non-recoverable error.
     *
     * <p>The transformer must continue to try and provide normal transformation
     * after invoking this method.  It should still be possible for the
     * application to process the document through to the end if no other errors
     * are encountered, but there is no guarantee that the output will be
     * useable.</p>
     *
     * @param exception The error information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax2.xml.transform.TransformerException if the application
     * chooses to discontinue the transformation.
     *
     * @see javax2.xml.transform.TransformerException
     */
    public abstract void fatalError(TransformerException exception)
        throws TransformerException;
}
