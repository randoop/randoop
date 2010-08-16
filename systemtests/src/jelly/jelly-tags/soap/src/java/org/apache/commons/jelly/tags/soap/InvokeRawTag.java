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

package org.apache.commons.jelly.tags.soap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * Invokes a web service
 * 
 * @author <a href="mailto:jim@bnainc.net">James Birchfield</a>
 * @version $Revision: 1.1 $
 */
public class InvokeRawTag extends TagSupport
{

    private String var;
    private String endpoint = null;
    private String soapAction;

    public InvokeRawTag()
    {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output)
        throws MissingAttributeException, JellyTagException
    {
        if (endpoint == null)
        {
            throw new MissingAttributeException("endpoint");
        }

        String request = getBodyText();

        String answer = null;
        try
        {
            // Prepare HTTP post
            PostMethod post = new PostMethod(endpoint);

            // Request content will be retrieved directly 
            // from the input stream
            post.setRequestBody(new StringInputStream(request));

            // Per default, the request content needs to be buffered
            // in order to determine its length.
            // Request body buffering can be avoided when
            // = content length is explicitly specified
            // = chunk-encoding is used
            if (request.length() < Integer.MAX_VALUE)
            {
                post.setRequestContentLength((int) request.length());
            }
            else
            {
                post.setRequestContentLength(
                    EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
            }

            // Specify content type and encoding
            // If content encoding is not explicitly specified
            // ISO-8859-1 is assumed
            post.setRequestHeader(
                "Content-type",
                "text/xml; charset=ISO-8859-1");
            
            // Set the SOAPAction header
            if ( soapAction == null )
            {
                post.setRequestHeader( "SOAPAction", "");
            }
            else
            {
                post.setRequestHeader( "SOAPAction", soapAction);
            }
            
            // Get HTTP client
            HttpClient httpclient = new HttpClient();
            // Execute request
            int result = httpclient.executeMethod(post);

            answer = post.getResponseBodyAsString();
            
            // Release current connection to the connection pool once you are done
            post.releaseConnection();
        }
        catch (MalformedURLException e)
        {
            throw new JellyTagException(e);
        }
        catch (RemoteException e)
        {
            throw new JellyTagException(e);
        }
        catch (HttpException e)
        {
            throw new JellyTagException(e);
        }
        catch (IOException e)
        {
            throw new JellyTagException(e);
        }

        if (var != null)
        {
            context.setVariable(var, answer);
        }
        else
        {
            // should turn the answer into XML events...
            throw new JellyTagException(
                "Not implemented yet; should stream results as XML events. Results: "
                    + answer);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    /**
     * Sets the end point to which the invocation will occur
     */
    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    /**
     * Sets the name of the variable to output the results of the SOAP call to.
     */
    public void setVar(String var)
    {
        this.var = var;
    }
    
    /**
     * The SOAPAction HTTP header.
     */
    public void setSoapAction(String action)
    {
        soapAction = action;
    }

}
