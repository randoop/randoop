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
package org.apache.commons.chain.web.faces;


import java.util.Locale;
import javax.faces.context.FacesContext;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.web.AbstractSetLocaleCommand;


/**
 * <p>Concrete implementation of {@link AbstractSetLocaleCommand} for
 * the JavaServer Faces API.</p>
 */

public class FacesSetLocaleCommand extends AbstractSetLocaleCommand {


    // ------------------------------------------------------- Protected Methods


    protected void setLocale(Context context, Locale locale) {

    FacesContext fcontext = (FacesContext)
        context.get("context");
    fcontext.getViewRoot().setLocale(locale);

    }


}
