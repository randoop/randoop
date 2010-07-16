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

package org.apache.commons.jelly.tags.jetty;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.XMLOutput;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.StringBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * The actual http handler implementation for an http context in an http server
 *
 * @author  rtl
 * @version $Id: JellyResourceHttpHandler.java,v 1.3 2002/07/14 12:38:22 dion Exp $
 */
class JellyResourceHttpHandler extends AbstractHttpHandler {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(JellyResourceHttpHandler.class);

    /** The name of the var to check if setHandled should not be set to true . */
    private static final String OVERRIDE_SET_HANDLED_VAR = "overrideSetHandled";

    /** The list of  tags registered to handle a request method  */
    private Map _tagMap;

    /** The place where to output the results of the tag body */
    private XMLOutput _xmlOutput;

    /** Creates a new instance of JellyResourceHttpHandler */
    public JellyResourceHttpHandler( XMLOutput xmlOutput ) {
        _tagMap = new HashMap();
        _xmlOutput = xmlOutput;
    }

    /*
     * register this tag as the handler for the specified method
     *
     * @param tag the tag to be registered
     * @param method the name of the http method which this tag processes
     */
    public void registerTag(Tag tag, String method){
        _tagMap.put(method.toLowerCase(), tag);
    }

    /*
     * handle an http request
     *
     * @param pathInContext the path of the http request
     * @param pathParams the parameters (if any) of the http request
     * @param request the actual http request
     * @param response the place for any response
     *
     * @throws HttpException when an error occurs
     * @throws IOException when an error occurs
     */
    public void handle(String pathInContext,
                       String pathParams,
                       HttpRequest request,
                       HttpResponse response)
        throws HttpException, IOException
    {
        Tag handlerTag = (Tag) _tagMap.get(request.getMethod().toLowerCase());
        if (null != handlerTag) {
            // setup the parameters in the jelly context
            JellyContext jellyContext = handlerTag.getContext();
            jellyContext.setVariable( "pathInContext", pathInContext);
            jellyContext.setVariable( "pathParams", pathParams);
            jellyContext.setVariable( "request", request);
            jellyContext.setVariable( "requestBody", getRequestBody(request));
            jellyContext.setVariable( "response", response);

            try {
                handlerTag.invokeBody(_xmlOutput);
                // only call set handled if tag has not requested an override
                // if it has requested an override then reset the request
                if (null == jellyContext.getVariable(OVERRIDE_SET_HANDLED_VAR)) {
                    request.setHandled(true);
                    response.commit();
                } else {
                    jellyContext.removeVariable(OVERRIDE_SET_HANDLED_VAR);
                }
            } catch (Exception ex ) {
                throw new HttpException(HttpResponse.__500_Internal_Server_Error,
                                        "Error invoking method handler tag: " + ex.getLocalizedMessage());
            }
        } else {
            log.info("No handler for request:" +
                      request.getMethod() + " path:" +
                      response.getHttpContext().getContextPath() +
                      pathInContext);
        }

        return;
    }

    public String getRequestBody(HttpRequest request) throws IOException {

        // read the body as a string from the input stream
        InputStream is = request.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        StringBuffer sb = new StringBuffer();
        char[] buffer = new char[1024];
        int len;

        while ((len = isr.read(buffer, 0, 1024)) != -1)
          sb.append(buffer, 0, len);

        if (sb.length() > 0)
          return sb.toString();
        else
          return null;

    }
}

