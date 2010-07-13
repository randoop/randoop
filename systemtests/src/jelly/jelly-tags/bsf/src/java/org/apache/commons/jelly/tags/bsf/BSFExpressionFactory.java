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

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

/** Represents a factory of BSF expressions
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.6 $
  */
public class BSFExpressionFactory implements ExpressionFactory {

    /** The logger of messages */
    private Log log = LogFactory.getLog( getClass() );

    private String language = "javascript";
    private BSFManager manager;
    private BSFEngine engine;
    private JellyContextRegistry registry = new JellyContextRegistry();

    public BSFExpressionFactory() {
    }

    // Properties
    //-------------------------------------------------------------------------

    /** @return the BSF language to be used */
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /** @return the BSF Engine to be used by this expression factory */
    public BSFEngine getBSFEngine() throws BSFException {
        if ( engine == null ) {
            engine = createBSFEngine();
        }
        return engine;
    }

    public void setBSFEngine(BSFEngine engine) {
        this.engine = engine;
    }

    public BSFManager getBSFManager() {
        if ( manager == null ) {
            manager = createBSFManager();
            manager.setObjectRegistry( registry );
        }
        return manager;
    }

    public void setBSFManager(BSFManager manager) {
        this.manager = manager;
        manager.setObjectRegistry( registry );
    }

    // ExpressionFactory interface
    //-------------------------------------------------------------------------
    public Expression createExpression(String text) throws JellyException {
        try {
            return new BSFExpression( text, getBSFEngine(), getBSFManager(), registry );
        } catch (BSFException e) {
            throw new JellyException("Could not obtain BSF engine",e);
        }
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /** Factory method */
    protected BSFEngine createBSFEngine() throws BSFException {
        return getBSFManager().loadScriptingEngine( getLanguage() );
    }

    /** Factory method */
    protected BSFManager createBSFManager() {
        BSFManager answer = new BSFManager();
        return answer;
    }
}
