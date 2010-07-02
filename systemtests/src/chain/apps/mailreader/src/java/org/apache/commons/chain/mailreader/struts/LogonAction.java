package org.apache.commons.chain.mailreader.struts;

import org.apache.struts.action.*;
import org.apache.struts.webapp.example.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Run LogonCommand and confirm success.
 */
public class LogonAction extends MailReaderAction {

    protected ActionForward checkState(ActionMapping mapping,
                                       ActionForm form,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws Exception {

        Object user = request.getSession().getAttribute(Constants.USER_KEY);
        if (user == null) {
            ActionMessages errors = new ActionMessages();
            errors.add(
                    ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("error.password.mismatch"));
            saveErrors(request, errors);
            return findInput(mapping);
        }

        return null;

    }


}
