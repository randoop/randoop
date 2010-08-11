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
package org.apache.commons.jelly.tags.validate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.util.ClassLoaderUtils;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.xml.sax.SAXException;

/**
 * This tag creates a new Verifier of a schema as a variable
 * so that it can be used by a &lt;validate&gt; tag.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.10 $
 */
public class VerifierTag extends TagSupport {

    /** the variable name to export the Verifier as */
    private String var;

    /** The URI to load the schema from */
    private String uri;

    /** The file to load the schema from */
    private File file;

    /** The system ID to use when parsing the schema */
    private String systemId;

    /** The factory used to create new schema verifier objects */
    private VerifierFactory factory;

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(final XMLOutput output) throws MissingAttributeException, JellyTagException {
        if ( var == null ) {
            throw new MissingAttributeException("var");
        }

        InputStream in = null;
        if ( uri != null ) {
            in = context.getResourceAsStream( uri );
            if ( in == null ) {
                throw new JellyTagException( "Could not find resource for uri: " + uri );
            }
        } else if (file != null) {
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new JellyTagException(e);
            }
        } else {
            String text = getBodyText();
            in = new ByteArrayInputStream( text.getBytes() );
        }

        Verifier verifier = null;
        try {
            Schema schema = null;
            if (systemId != null) {
                schema = getFactory().compileSchema(in, systemId);
            }
            else if ( uri != null ) {
                schema = getFactory().compileSchema(in, uri);
            }
            else{
                schema = getFactory().compileSchema(in);
            }

            if ( schema == null ) {
                throw new JellyTagException( "Could not create a valid schema" );
            }

            verifier = schema.newVerifier();
        }
        catch (VerifierConfigurationException e) {
            throw new JellyTagException(e);
        }
        catch (SAXException e) {
            throw new JellyTagException(e);
        }
        catch (IOException e) {
            throw new JellyTagException(e);
        }

        context.setVariable(var, verifier);
    }

    // Properties
    //-------------------------------------------------------------------------

    /**
     * Sets the name of the variable that will be set to the new Verifier
     *
     * @jelly:required
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Sets the URI of the schema file to parse. If no URI and no file is
     * specified then the body of this tag is used as the source of the schema
     *
     * @jelly:optional
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Sets the {@link File} of the schema to parse. If no URI and no file is
     * specified then the body of this tag is used as the source of the schema
     *
     * @jelly:optional
     */
    public void setFile(File aFile) {
        file = aFile;
    }

    /**
     * Sets the system ID used when parsing the schema
     *
     * @jelly:optional
     */
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    /**
     * Sets the factory used to create new schema verifier objects.
     * If none is provided then the default MSV factory is used.
     *
     * @jelly:optional
     */
    public void setFactory(VerifierFactory factory) {
        this.factory = factory;
    }

    public VerifierFactory getFactory() throws JellyTagException {
        if ( factory == null ) {
            try {
                ClassLoader loader = ClassLoaderUtils.getClassLoader(null, true, getClass());
                factory = (VerifierFactory)loader.loadClass(
                    "com.sun.msv.verifier.jarv.TheFactoryImpl").newInstance();
            } catch (ClassNotFoundException e) {
                throw new JellyTagException(e);
            } catch (InstantiationException e) {
                throw new JellyTagException(e);
            } catch (IllegalAccessException e) {
                throw new JellyTagException(e);
            }
        }
        return factory;
    }

    // Implementation methods
    //-------------------------------------------------------------------------


}
