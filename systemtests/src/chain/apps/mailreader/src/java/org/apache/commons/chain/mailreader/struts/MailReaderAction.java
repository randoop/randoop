/*
 * $Header: /home/cvspublic/jakarta-commons/chain/apps/mailreader/src/java/org/apache/commons/chain/mailreader/struts/MailReaderAction.java,v 1.5 2004/06/01 00:50:36 husted Exp $
 * $Revision: 1.5 $
 * $Date: 2004/06/01 00:50:36 $
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

import org.apache.commons.chain.Context;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.commons.chain.mailreader.MailReader;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts.webapp.example.Constants;
import org.apache.struts.webapp.example.User;
import org.apache.struts.webapp.example.UserDatabase;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>Process Commands using a {@link org.apache.commons.chain.mailreader.MailReader}
 * {@link Context}.</p>
 */
public class MailReaderAction extends Action {

    /**
     * <p>Convert {@link ActionForm} to {@link Context}.</p>
     * @param form Our ActionForm (conventonal or dynamic)
     * @return Context based on ActionForm values
     */
    protected Context getInput(ActionForm form) throws Exception {

        Map input;
        if (form instanceof DynaActionForm) {
            DynaActionForm dyna = (DynaActionForm) form;
            input = dyna.getMap();
        } else
            try {
                input = BeanUtils.describe(form);
            } catch (Throwable t) {
                input = new HashMap(); // FIXME: Lame resolution
            }
        return new ContextBase(input);

    }

    // See interface for JavaDoc
    public MailReader getContext(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request)
            throws Exception {

        Locale locale = getLocale(request);
        Context input = getInput(form);
        UserDatabase database = (UserDatabase)
                request.getSession().getServletContext().getAttribute(Constants.DATABASE_KEY);
        return new MailReader(locale, input, database);

    }

    /**
     * <p>
     * Operations to perform prior to executing business command.
     * If operations fail, return an appropriate {@link ActionForward}.
     * If operations succeed, return <code>null</code>.
     * </p>
     * @return ActionForward to follow, or null
     */
    protected ActionForward preExecute(ActionMapping mapping,
                                       ActionForm form,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws Exception {

        // override to provide functionality
        return null;
    }


    /**
     * <p>
     * Return the relevant {@link org.apache.commons.chain.Command} from the default
     * {@link org.apache.commons.chain.Catalog}.
     * </p>
     * @return Command for this helper
     */
    protected Command getCatalogCommand(ActionMapping mapping,
                                        ActionForm form,
                                        HttpServletRequest request) throws Exception {

        Catalog catalog = (Catalog) request.getSession().getServletContext().getAttribute("catalog");
        String name = mapping.getName();
        return catalog.getCommand(name);

    }

    /**
     * <p>Transfer input properties (back) to ActionForm.</p>
     */
    protected void conformInput(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                MailReader mailReader) throws Exception {

        BeanUtils.copyProperties(form, mailReader.getInput());

    }

    /**
     * <p>Transfer framework properties (back) to framework objects.</p>
     */
    protected void conformState(ActionMapping mapping,
                                ActionForm form,
                                HttpServletRequest request,
                                MailReader mailReader) throws Exception {

        setLocale(request, mailReader.getLocale());
        User user = mailReader.getUser();
        request.getSession().setAttribute(Constants.USER_KEY, user);

    }


    /**
     * <p>
     * Validate state, setting messages and returning ActionForward
     * if appropriate.
     * </p>
     */
    protected ActionForward checkState(ActionMapping mapping,
                                       ActionForm form,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws Exception {

        // override to provide functionality
        return null;

    }

    /**
     * <p>
     * Operations to perform prior to executing business command.
     * </p>
     */
    protected ActionForward postExecute(ActionMapping mapping,
                                        ActionForm form,
                                        HttpServletRequest request,
                                        HttpServletResponse response,
                                        MailReader mailReader) throws Exception {

        conformInput(mapping, form, request, mailReader);
        conformState(mapping, form, request, mailReader);

        // TODO: Expose any output
        // TODO: Expose any status messages,

        ActionForward location = checkState(mapping, form, request, response);
        if (null != location) return location;

        return null;

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
    protected ActionForward findSuccess(ActionMapping mapping) {
        return mapping.findForward(SUCCESS);
    }


    /**
     * <p>
     * Convenience method to return the Input forward.
     * Assumes the InputForward option and input property is set.
     * Otherwise, returns <code>null</code>.
     * </p>
     * @param mapping Our ActionMapping
     * @return a forward named "success" or null.
     */
    protected ActionForward findInput(ActionMapping mapping) {
        return mapping.getInputForward();
    }


    // See super class for JavaDoc
    public ActionForward execute(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {

        ActionForward location;
        MailReader mailReader = getContext(mapping, form, request);

        location = preExecute(mapping, form, request, response);
        if (location != null) return location;

        Command command = getCatalogCommand(mapping, form, request);
        boolean stop = command.execute(mailReader);

        location = postExecute(mapping, form, request, response, mailReader);
        if (location != null) return location;

        return findSuccess(mapping);

    }

}
