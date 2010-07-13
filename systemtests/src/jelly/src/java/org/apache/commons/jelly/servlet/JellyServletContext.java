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

import org.apache.commons.jelly.JellyContext;

import javax.servlet.ServletContext;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author <a href="mailto:kelvint@apache.org">Kelvin Tan</a>
 * @version 1.1
 */
public class JellyServletContext extends JellyContext {

    private ServletContext ctx;

    public JellyServletContext() {
    }

    public JellyServletContext(ServletContext ctx) {
        super();
        this.ctx = ctx;
    }

    public JellyServletContext(JellyContext parent, ServletContext ctx) {
        super(parent);
        this.ctx = ctx;
    }

    /**
     * Allow access of relative URIs when performing &lt;j:include&gt;.
     * @param s
     * @return
     * @throws MalformedURLException
     */
    public URL getResource(String s) throws MalformedURLException {
        return ctx.getResource(s);
    }

    /**
     * Allow access of relative URIs when performing &lt;j:include&gt;.
     * @param s
     * @return
     */
    public InputStream getResourceAsStream(String s) {
        return ctx.getResourceAsStream(s);
    }

    protected JellyContext createChildContext()
    {
        return new JellyServletContext(this, ctx);
    }
}
