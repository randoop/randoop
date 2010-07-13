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
package org.apache.commons.jelly.ant.tag;

import junit.framework.AssertionFailedError;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.BeanSource;
import org.apache.commons.jelly.tags.ant.AntTagLibrary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.types.Path;

/**
 * A mock tag which is used for testing the Ant nested properties behaviour
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.9 $
 */
public class DummyTag extends TagSupport implements BeanSource {

    /** The Log to which logging calls will be made. */
    private static Log log = LogFactory.getLog(DummyTag.class);

    private String var;

    private boolean calledCreatepath;
    private boolean calledSetClasspath;
    private boolean calledSetFlag;

    private Path classpath;
    private boolean flag;

    public DummyTag() {
    }

    // BeanSource interface
    //-------------------------------------------------------------------------
    public Object getBean() {
        return this;
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {

        if (! calledSetFlag) {
            throw new AssertionFailedError("call to setFlag() was not made");
        }

        calledCreatepath = false;
        calledSetClasspath = false;

        invokeBody(output);

        if (! calledCreatepath) {
            throw new AssertionFailedError("call to createClasspath() was not made");
        }

        if (! calledSetClasspath) {
            throw new AssertionFailedError("call to setClasspath() was not made");
        }
        log.info( "Called with classpath: " + classpath );

        if (var != null) {
            context.setVariable(var, classpath);
        }
    }

    // Ant Task-like nested property methods
    //-------------------------------------------------------------------------
    public Path createClasspath() {
        log.info("called createClasspath()");
        calledCreatepath = true;
        return new Path( AntTagLibrary.getProject(context) );
    }

    public void setClasspath(Path classpath) {
        log.info("called setClasspath()");
        calledSetClasspath = true;
        this.classpath = classpath;
    }

    public void setFlag(boolean flag)
    {
        log.info("called setFlag()");
        calledSetFlag = true;
        this.flag = flag;

    }

    // Tag properties
    //-------------------------------------------------------------------------

    public void setVar(String var) {
        this.var = var;
    }
}
