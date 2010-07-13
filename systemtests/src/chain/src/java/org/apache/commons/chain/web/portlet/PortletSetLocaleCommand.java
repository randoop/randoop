/*
 * Copyright 1999-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.chain.web.portlet;


import java.util.Locale;
import javax.portlet.PortletResponse;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.web.AbstractSetLocaleCommand;


/**
 * <p>Concrete implementation of {@link AbstractSetLocaleCommand} for
 * the Portlet API.</p>
 */

public class PortletSetLocaleCommand extends AbstractSetLocaleCommand {


    // ------------------------------------------------------- Protected Methods


    protected void setLocale(Context context, Locale locale) {

    PortletResponse response = (PortletResponse)
        context.get("response");
    //  response.setLocale(locale);
    // Not supported by the Portlet API

    }


}
