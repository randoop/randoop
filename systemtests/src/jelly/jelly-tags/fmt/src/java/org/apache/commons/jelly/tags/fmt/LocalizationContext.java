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
package org.apache.commons.jelly.tags.fmt;

import java.util.ResourceBundle;
import java.util.Locale;


/**
 * Class representing an I18N localization context.
 *
 * <p> An I18N localization context has two components: a resource bundle and
 * the locale that led to the resource bundle match.
 *
 * <p> The resource bundle component is used by &lt;fmt:message&gt; for mapping
 * message keys to localized messages, and the locale component is used by the
 * &lt;fmt:message&gt;, &lt;fmt:formatNumber&gt;, &lt;fmt:parseNumber&gt;, &lt;fmt:formatDate&gt;,
 * and &lt;fmt:parseDate&gt; actions as their formatting locale.
 *
 * @see javax.servlet.jsp.jstl.fmt.LocalizationContext
 *
 * @author <a href="mailto:willievu@yahoo.com">Willie Vu</a>
 * @version 1.1
 */
public class LocalizationContext {

    // the localization context's resource bundle
    final private ResourceBundle bundle;

    // the localization context's locale
    final private Locale locale;

    /**
     * Constructs an empty I18N localization context.
     */
    public LocalizationContext() {
        bundle = null;
        locale = null;
    }

    /**
     * Constructs an I18N localization context from the given resource bundle
     * and locale.
     *
     * <p> The specified locale is the application- or browser-based preferred
     * locale that led to the resource bundle match.
     *
     * @param bundle The localization context's resource bundle
     * @param locale The localization context's locale
     */
    public LocalizationContext(ResourceBundle bundle, Locale locale) {
        this.bundle = bundle;
        this.locale = locale;
    }

    /**
     * Constructs an I18N localization context from the given resource bundle.
     *
     * <p> The localization context's locale is taken from the given
     * resource bundle.
     *
     * @param bundle The resource bundle
     */
    public LocalizationContext(ResourceBundle bundle) {
        this.bundle = bundle;
        this.locale = bundle.getLocale();
    }

    /**
     * Gets the resource bundle of this I18N localization context.
     *
     * @return The resource bundle of this I18N localization context, or null
     * if this I18N localization context is empty
     */
    public ResourceBundle getResourceBundle() {
        return bundle;
    }

    /**
     * Gets the locale of this I18N localization context.
     *
     * @return The locale of this I18N localization context, or null if this
     * I18N localization context is empty, or its resource bundle is a
     * (locale-less) root resource bundle.
     */
    public Locale getLocale() {
        return locale;
    }
}
