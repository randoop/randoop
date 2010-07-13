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
package org.apache.commons.jelly.tags.ant;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * A tag which creates a new FileScanner bean instance that can be used to
 * iterate over fileSets
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.6 $
 */
public class FileScannerTag extends TagSupport implements TaskSource {

    /** The file walker that gets created */
    private FileScanner fileScanner;

    /** the variable exported */
    private String var;

    public FileScannerTag(FileScanner fileScanner) {
        this.fileScanner = fileScanner;
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        fileScanner.setProject(AntTagLibrary.getProject(context));

        fileScanner.clear();

        // run the body first to configure the task via nested
        invokeBody(output);

        // output the fileScanner
        if ( var == null ) {
            throw new MissingAttributeException( "var" );
        }
        context.setVariable( var, fileScanner );

    }

    // TaskSource interface
    //-------------------------------------------------------------------------
    public Object getTaskObject() {
        return fileScanner;
    }

    /**
     * Allows nested tags to set a property on the task object of this tag
     */
    public void setTaskProperty(String name, Object value) throws JellyTagException {
        try {
            BeanUtils.setProperty( fileScanner, name, value );
        }
        catch (IllegalAccessException ex) {
            throw new JellyTagException(ex);
        }
        catch (InvocationTargetException ex) {
            throw new JellyTagException(ex);
        }
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * @return the Ant task
     */
    public FileScanner getFileScanner() {
        return fileScanner;
    }

    /** Sets the name of the variable exported by this tag */
    public void setVar(String var) {
        this.var = var;
    }

}
