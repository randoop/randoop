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
import org.apache.commons.jelly.XMLOutput;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * A tag that uses Velocity to render a specified template with the
 * JellyContext storing the results in either a variable in the
 * JellyContext or in a specified file.
 *
 * @author <a href="mailto:pete-apache-dev@kazmier.com">Pete Kazmier</a>
 * @version $Id: MergeTag.java,v 1.4 2004/09/09 12:23:16 dion Exp $
 */
public class MergeTag extends VelocityTagSupport
{
    private static final String ENCODING = "ISO-8859-1";

    private String var;
    private String name;
    private String basedir;
    private String template;
    private String inputEncoding;
    private String outputEncoding;
    private boolean readOnly = true;

    // -- Tag interface -----------------------------------------------------

    public void doTag( final XMLOutput output ) throws JellyTagException
    {
        if ( basedir == null || template == null )
        {
            throw new JellyTagException(
                    "This tag must define 'basedir' and 'template'" );
        }

        if ( name != null )
        {
            try {
                Writer writer = new OutputStreamWriter(
                        new FileOutputStream( name ),
                        outputEncoding == null ? ENCODING : outputEncoding );
                mergeTemplate( writer );
                writer.close();
            }
            catch (IOException e) {
                throw new JellyTagException(e);
            }
        }
        else if ( var != null )
        {
            StringWriter writer = new StringWriter();
            mergeTemplate( writer );
            context.setVariable( var, writer.toString() );
        }
        else
        {
            throw new JellyTagException(
                    "This tag must define either 'name' or 'var'" );
        }
    }

    // -- Properties --------------------------------------------------------

    /**
     * Sets the var used to store the results of the merge.
     *
     * @param var The var to set in the JellyContext with the results of
     * the merge.
     */
    public void setVar( String var )
    {
        this.var = var;
    }

    /**
     * Sets the file name for the merged output.
     *
     * @param name The name of the output file that is used to store the
     * results of the merge.
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * Sets the base directory used for loading of templates by the
     * Velocity file resource loader.
     *
     * @param basedir The directory where templates can be located by
     * the Velocity file resource loader.
     */
    public void setBasedir( String basedir )
    {
        this.basedir = basedir;
    }

    /**
     * Sets the filename of the template used to merge with the
     * JellyContext.
     *
     * @param template The filename of the template to be merged.
     */
    public void setTemplate( String template )
    {
        this.template = template;
    }

    /**
     * Sets the read-only flag for this adapter which prevents
     * modifications in the Velocity context from propogating to the
     * JellyContext.
     *
     * @param readOnly <tt>true</tt> prevents modifications from
     * propogating (the default), or <tt>false</tt> which permits
     * modifications.
     */
    public void setReadOnly( boolean readOnly )
    {
        this.readOnly = readOnly;
    }

    /**
     * Sets the output encoding mode which defaults to ISO-8859-1 used
     * when storing the results of a merge in a file.
     *
     * @param encoding  The file encoding to use when writing the
     * output.
     */
    public void setOutputEncoding( String encoding )
    {
        this.outputEncoding = encoding;
    }

    /**
     * Sets the input encoding used in the specified template which
     * defaults to ISO-8859-1.
     *
     * @param encoding  The encoding used in the template.
     */
    public void setInputEncoding( String encoding )
    {
        this.inputEncoding = encoding;
    }

    // -- Implementation ----------------------------------------------------

    /**
     * Merges the Velocity template with the Jelly context.
     *
     * @param writer The output writer used to write the merged results.
     * @throws Exception If an exception occurs during the merge.
     */
    private void mergeTemplate( Writer writer ) throws JellyTagException
    {
        JellyContextAdapter adapter = new JellyContextAdapter( getContext() );
        adapter.setReadOnly( readOnly );

        try {
            getVelocityEngine( basedir ).mergeTemplate(
                template,
                inputEncoding == null ? ENCODING : inputEncoding,
                adapter,
                writer );
        }
        catch (ResourceNotFoundException e) {
            throw new JellyTagException(e);
        }
        catch (ParseErrorException e) {
            throw new JellyTagException(e);
        }
        catch (MethodInvocationException e) {
            throw new JellyTagException(e);
        }
        catch (Exception e) {
            throw new JellyTagException(e);
        }
    }
}

