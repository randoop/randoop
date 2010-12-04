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

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.expression.Expression;
import java.util.Locale;

/**
 * Support for tag handlers for &lt;setLocale&gt;, the locale setting
 * tag in JSTL.
 * @author <a href="mailto:willievu@yahoo.com">Willie Vu</a>
 * @version 1.2
 *
 * @task decide how to implement setResponseLocale
 */
public class SetLocaleTag extends TagSupport {

    private static final char HYPHEN = '-';
    private static final char UNDERSCORE = '_';

    private Expression value;

    private Expression variant;

    private String scope;

    /** Creates a new instance of SetLocaleTag */
    public SetLocaleTag() {
    }

    /**
     * Evaluates this tag after all the tags properties have been initialized.
     *
     */
    public void doTag(XMLOutput output) throws JellyTagException {
        Locale locale = null;

        Object valueInput = null;
        if (this.value != null) {
            valueInput = this.value.evaluate(context);
        }
        Object variantInput = null;
        if (this.variant != null) {
            variantInput = this.variant.evaluate(context);
        }

        if (valueInput == null) {
            locale = Locale.getDefault();
        } else if (valueInput instanceof String) {
            if (((String) valueInput).trim().equals("")) {
                locale = Locale.getDefault();
            } else {
                locale = parseLocale((String) valueInput, (String) variantInput);
            }
        } else {
            locale = (Locale) valueInput;
        }

        if (scope != null) {
            context.setVariable(Config.FMT_LOCALE, scope, locale);
        }
        else {
            context.setVariable(Config.FMT_LOCALE, locale);
        }
        //setResponseLocale(jc, locale);
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    public void setVariant(Expression variant) {
        this.variant = variant;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    //*********************************************************************
    // Public utility methods

    /**
     * See parseLocale(String, String) for details.
     */
    public static Locale parseLocale(String locale) {
        return parseLocale(locale, null);
    }

    /**
     * Parses the given locale string into its language and (optionally)
     * country components, and returns the corresponding
     * <tt>java.util.Locale</tt> object.
     *
     * If the given locale string is null or empty, the runtime's default
     * locale is returned.
     *
     * @param locale the locale string to parse
     * @param variant the variant
     *
     * @return <tt>java.util.Locale</tt> object corresponding to the given
     * locale string, or the runtime's default locale if the locale string is
     * null or empty
     *
     * @throws IllegalArgumentException if the given locale does not have a
     * language component or has an empty country component
     */
    public static Locale parseLocale(String locale, String variant) {

        Locale ret = null;
        String language = locale;
        String country = null;
        int index = -1;

        if (((index = locale.indexOf(HYPHEN)) > -1)
        || ((index = locale.indexOf(UNDERSCORE)) > -1)) {
            language = locale.substring(0, index);
            country = locale.substring(index+1);
        }

        if ((language == null) || (language.length() == 0)) {
            throw new IllegalArgumentException("Missing language");
        }

        if (country == null) {
            if (variant != null)
                ret = new Locale(language, "", variant);
            else
                ret = new Locale(language, "");
        } else if (country.length() > 0) {
            if (variant != null)
                ret = new Locale(language, country, variant);
            else
                ret = new Locale(language, country);
        } else {
            throw new IllegalArgumentException("Missing country");
        }

        return ret;
    }

    /**
     * Returns the locale specified by the named scoped attribute or context
     * configuration parameter.
     *
     * <p> The named scoped attribute is searched in the page, request,
     * session (if valid), and application scope(s) (in this order). If no such
     * attribute exists in any of the scopes, the locale is taken from the
     * named context configuration parameter.
     *
     * @param jc the page in which to search for the named scoped
     * attribute or context configuration parameter
     * @param name the name of the scoped attribute or context configuration
     * parameter
     *
     * @return the locale specified by the named scoped attribute or context
     * configuration parameter, or <tt>null</tt> if no scoped attribute or
     * configuration parameter with the given name exists
     */
    static Locale getLocale(JellyContext jc, String name) {
        Locale loc = null;

        Object obj = jc.getVariable(name);
        if (obj != null) {
            if (obj instanceof Locale) {
                loc = (Locale) obj;
            } else {
                loc = parseLocale((String) obj);
            }
        }

        return loc;
    }

    /*
     * Returns the formatting locale to use with the given formatting action
     * in the given page.
     *
     * @param jc The context containing the formatting action
     * @param fromTag The formatting action
     * @param format <tt>true</tt> if the formatting action is of type
     * <formatXXX> (as opposed to <parseXXX>), and <tt>false</tt> otherwise
     * (if set to <tt>true</tt>, the formatting locale that is returned by
     * this method is used to set the response locale).
     *
     * @param avail the array of available locales
     *
     * @return the formatting locale to use
     */
    static Locale getFormattingLocale(JellyContext jc,
    Tag fromTag,
    boolean format,
    Locale[] avail) {

        LocalizationContext locCtxt = null;

        // Get formatting locale from enclosing <fmt:bundle>
        Tag parent = findAncestorWithClass(fromTag, BundleTag.class);
        if (parent != null) {
        /*
         * use locale from localization context established by parent
         * <fmt:bundle> action, unless that locale is null
         */
            locCtxt = ((BundleTag) parent).getLocalizationContext();
            if (locCtxt.getLocale() != null) {
                if (format) {
                    //setResponseLocale(jc, locCtxt.getLocale());
                }
                return locCtxt.getLocale();
            }
        }

        // Use locale from default I18N localization context, unless it is null
        if ((locCtxt = BundleTag.getLocalizationContext(jc)) != null) {
            if (locCtxt.getLocale() != null) {
                if (format) {
                    //setResponseLocale(jc, locCtxt.getLocale());
                }
                return locCtxt.getLocale();
            }
        }

    /*
     * Establish formatting locale by comparing the preferred locales
     * (in order of preference) against the available formatting
     * locales, and determining the best matching locale.
     */
        Locale match = null;
        Locale pref = getLocale(jc, Config.FMT_LOCALE);
        if (pref != null) {
            // Preferred locale is application-based
            match = findFormattingMatch(pref, avail);
        }
        if (match == null) {
            //Use fallback locale.
            pref = getLocale(jc, Config.FMT_FALLBACK_LOCALE);
            if (pref != null) {
                match = findFormattingMatch(pref, avail);
            }
        }
        if (format && (match != null)) {
            //setResponseLocale(jc, match);
        }

        return match;
    }

    /*
     * Returns the best match between the given preferred locale and the
     * given available locales.
     *
     * The best match is given as the first available locale that exactly
     * matches the given preferred locale ("exact match"). If no exact match
     * exists, the best match is given as the first available locale that
     * matches the preferred locale's language component and does not have any
     * country component ("language match").
     *
     * @param pref the preferred locale
     * @param avail the available formatting locales
     *
     * @return Available locale that best matches the given preferred locale,
     * or <tt>null</tt> if no match exists
     */
    private static Locale findFormattingMatch(Locale pref, Locale[] avail) {
        Locale match = null;

        for (int i=0; i<avail.length; i++) {
            if (pref.equals(avail[i])) {
                // Exact match
                match = avail[i];
                break;
            } else {
                if (pref.getLanguage().equals(avail[i].getLanguage())
                && ("".equals(avail[i].getCountry()))) {
                    // Language match
                    if (match == null) {
                        match = avail[i];
                    }
                }
            }
        }

        return match;
    }
}
