/*
 * $Header: /home/cvspublic/jakarta-commons/chain/apps/mailreader/src/java/org/apache/commons/chain/mailreader/struts/CommandAction.java,v 1.3 2004/06/01 00:49:17 husted Exp $
 * $Revision: 1.3 $
 * $Date: 2004/06/01 00:49:17 $
 *
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.commons.chain.mailreader.struts;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.web.servlet.ServletWebContext;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Process Commands using a {@link org.apache.commons.chain.web.servlet.ServletWebContext}
 * {@link Context}.</p>
 */
public class CommandAction extends Action {

    /**
     * <p>
     * Return the relevant {@link Command} from the default
     * {@link Catalog}.
     * </p>
     * @return Command for this helper
     */
    protected Command getCommand(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {

        Catalog catalog = (Catalog) request.getSession().getServletContext().getAttribute("catalog");
        String name = mapping.getName();
        Command command = catalog.getCommand(name);
        return command;

    }

    /**
     * <p>
     * Return the relevant {@link Context} for this request.
     * </p>
     * @return Context for this request
     */
    protected Context getContext(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {

        ServletContext application = request.getSession().getServletContext();
        Context context = new ServletWebContext(application, request, response);
        return context;

    }

    /**
     * <p>
     * Token representing a nominal outcome ["success"].
     * </p>
     */
    protected static String SUCCESS = "success";

    /**
     * <p>Convenience method to return nominal location.</p>
     * @param mapping Our ActionMapping
     * @return ActionForward named "success" or null
     */
    protected ActionForward findLocation(ActionMapping mapping, boolean stop) {
        if (stop) return mapping.getInputForward();
        return mapping.findForward(SUCCESS);
    }

    // See super class for JavaDoc
    public ActionForward execute(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {

        Command command = getCommand(mapping, form, request, response);
        Context context = getContext(mapping, form, request, response);

        boolean stop = command.execute(context);

        return findLocation(mapping, stop);

    }

}
