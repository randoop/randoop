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

package org.apache.commons.jelly.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.XMLOutput;

/**
 * Servlet for handling display of Jelly-fied XML files. Modelled after VelocityServlet.
 *
 * @author Kelvin Tan
 * @version $Revision: 1.7 $
 */
public class JellyServlet extends HttpServlet {
    /**
     * The HTTP request object context key.
     */
    public static final String REQUEST = "request";

    /**
     * The HTTP response object context key.
     */
    public static final String RESPONSE = "response";

    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException {

        doRequest(request, response);
    }

    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException {

        doRequest(request, response);
    }

    /**
     * Handles all requests
     * @param req HttpServletRequest object containing client request
     * @param res HttpServletResponse object for the response
     * @throws ServletException
     * @throws IOException
     */
    protected void doRequest(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {

        JellyContext context = createContext(req, res);
        try {
            URL script = getScript(req);
            runScript(script, context, req, res);
        }
        catch (Exception e) {
            error(req, res, e);
        }
    }

    /**
     * @see org.apache.velocity.servlet.VelocityServlet#createContext
     * @param req
     * @param res
     * @return
     */
    protected JellyContext createContext(
        HttpServletRequest req,
        HttpServletResponse res) {

        JellyContext ctx = new JellyServletContext(getServletContext());
        ctx.setVariable(REQUEST, req);
        ctx.setVariable(RESPONSE, res);
        return ctx;
    }

    /**
     * <p>
     * Either use the query parameter "script", or the URI itself
     * to denote the script to run.
     * </p>
     * <p>
     * Example: script=index.jelly or http://localhost:8080/foo/index.jelly.
     * </p>
     *
     * @see org.apache.velocity.servlet.VelocityServlet#getTemplate
     * @param req
     * @return
     * @throws MalformedURLException
     */
    protected URL getScript(HttpServletRequest req)
        throws MalformedURLException {

        String scriptUrl = req.getParameter("script");
        if (scriptUrl == null) {
            scriptUrl = req.getPathInfo();
        }
        URL url = getServletContext().getResource(scriptUrl);
        if (url == null) {
            throw new IllegalArgumentException("Invalid script url:" + scriptUrl);
        }
        return url;
    }

    /**
     * @see org.apache.velocity.servlet.VelocityServlet#mergeTemplate
     * @param script
     * @param context
     * @param req
     * @param res
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws JellyException
     */
    protected void runScript(
        URL script,
        JellyContext context,
        HttpServletRequest req,
        HttpServletResponse res)
        throws IOException, UnsupportedEncodingException, JellyException {

        ServletOutputStream output = res.getOutputStream();
        XMLOutput xmlOutput = XMLOutput.createXMLOutput(output);
        context.runScript(script, xmlOutput);
        xmlOutput.flush();
        xmlOutput.close();
        output.flush();
    }

    /**
     * Invoked when there is an error thrown in any part of doRequest() processing.
     * <br><br>
     * Default will send a simple HTML response indicating there was a problem.
     *<br><br>
     * Ripped from VelocityServlet.
     *
     * @param request original HttpServletRequest from servlet container.
     * @param response HttpServletResponse object from servlet container.
     * @param cause  Exception that was thrown by some other part of process.
     */
    protected void error(
        HttpServletRequest request,
        HttpServletResponse response,
        Exception cause)
        throws ServletException, IOException {

        StringBuffer html = new StringBuffer();
        html.append("<html>");
        html.append("<title>Error</title>");
        html.append("<body bgcolor=\"#ffffff\">");
        html.append("<h2>JellyServlet : Error processing the script</h2>");
        html.append("<pre>");
        String why = cause.getMessage();
        if (why != null && why.trim().length() > 0) {
            html.append(why);
            html.append("<br>");
        }

        StringWriter sw = new StringWriter();
        cause.printStackTrace(new PrintWriter(sw));

        html.append(sw.toString());
        html.append("</pre>");
        html.append("</body>");
        html.append("</html>");
        response.getOutputStream().print(html.toString());
    }
}
