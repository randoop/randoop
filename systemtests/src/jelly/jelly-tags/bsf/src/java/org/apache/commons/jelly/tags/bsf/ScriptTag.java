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
package org.apache.commons.jelly.tags.bsf;

import java.util.Iterator;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.LocationAware;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFManager;
import org.apache.bsf.BSFException;

/**
 * A tag which evaluates its body using the current scripting language
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.5 $
 */
public class ScriptTag extends TagSupport implements LocationAware {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(ScriptTag.class.getName() + ".evaluating");

    private BSFEngine engine;
    private BSFManager manager;
    private String elementName;
    private String fileName;
    private int columnNumber;
    private int lineNumber;

    public ScriptTag(BSFEngine engine, BSFManager manager) {
        this.engine = engine;
        this.manager = manager;
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        String text = getBodyText();

        log.debug(text);

        // XXXX: unfortunately we must sychronize evaluations
        // so that we can swizzle in the context.
        // maybe we could create an expression from a context
        // (and so create a BSFManager for a context)
        synchronized (getRegistry()) {
            getRegistry().setJellyContext(context);

            try {
                // XXXX: hack - there must be a better way!!!
                for ( Iterator iter = context.getVariableNames(); iter.hasNext(); ) {
                    String name = (String) iter.next();
                    Object value = context.getVariable( name );
                    manager.declareBean( name, value, value.getClass() );
                }
                engine.exec(fileName, lineNumber, columnNumber, text);
            }
            catch (BSFException e) {
                throw new JellyTagException("Error occurred with script: " + e, e);
            }
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    /**
     * @return int
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * @return String
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * @return BSFEngine
     */
    public BSFEngine getEngine() {
        return engine;
    }

    /**
     * @return String
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return int
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Sets the columnNumber.
     * @param columnNumber The columnNumber to set
     */
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    /**
     * Sets the elementName.
     * @param elementName The elementName to set
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    /**
     * Sets the engine.
     * @param engine The engine to set
     */
    public void setEngine(BSFEngine engine) {
        this.engine = engine;
    }

    /**
     * Sets the fileName.
     * @param fileName The fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Sets the lineNumber.
     * @param lineNumber The lineNumber to set
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    private JellyContextRegistry getRegistry()
    {
        return (JellyContextRegistry) this.manager.getObjectRegistry();
    }
}
