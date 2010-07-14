package org.apache.commons.jelly.tags.velocity;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
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

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.velocity.app.VelocityEngine;

/**
 * Support methods for the Velocity tag library.  Currently this is only
 * used to get an instance of the VelocityEngine.  For each unique base
 * directory specified, a new VelocityEngine instance is stored in the
 * context (as the author hasn't figured out how to change the resource
 * loader of an already init'd VelocityEngine).
 *
 * @author <a href="mailto:pete-apache-dev@kazmier.com">Pete Kazmier</a>
 * @version $Id: VelocityTagSupport.java,v 1.4 2004/09/09 12:23:16 dion Exp $
 */
public abstract class VelocityTagSupport extends TagSupport
{
    /** The VelocityEngine variable name in the JellyContext.  */
    public static final String VELOCITY_ENGINE_VAR_NAME =
            "org.apache.maven.jelly.tags.velocity.VelocityEngine";

    /**
     * Gets or creates a VelocityEngine if one doesn't already exist for
     * the specified base directory.
     *
     * @return A VelocityEngine with a file resource loader configured
     * for the specified base directory.
     */
    public VelocityEngine getVelocityEngine( String basedir ) throws JellyTagException
    {
        VelocityEngine ve = ( VelocityEngine ) getContext().getVariable(
                keyName( basedir ) );

        if ( ve == null )
        {
            ve = new VelocityEngine();
            ve.setProperty( VelocityEngine.RUNTIME_LOG_LOGSYSTEM, this );
            ve.setProperty( VelocityEngine.FILE_RESOURCE_LOADER_PATH, basedir );

            try {
                ve.init();
            }
            catch (Exception e) {
                throw new JellyTagException(e);
            }

            getContext().setVariable( keyName( basedir ), ve );
        }

        return ve;
    }

    /**
     * Constructs the name of the key used to reference the
     * VelocityEngine for the specified base directory.
     *
     * @param basedir The base directory used by the VelocityEngine
     * @return The key used to reference the VelocityEngine that has
     * been initialized with the specified base directory.
     */
    private String keyName( String basedir )
    {
        return new StringBuffer()
            .append( VELOCITY_ENGINE_VAR_NAME )
            .append( '.' )
            .append( basedir )
            .toString();
    }
}
