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
package org.apache.commons.chain.config;


import java.net.URL;
import org.apache.commons.chain.Catalog;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;
import org.xml.sax.InputSource;


/**
 * <p>Class to parse the contents of an XML configuration file (using
 * Commons Digester) that defines and configures commands and command chains
 * to be registered in a {@link Catalog}.  Advanced users can configure the
 * detailed parsing behavior by configuring the properties of an instance
 * of this class prior to calling the <code>parse()</code> method.  It
 * is legal to call the <code>parse()</code> method more than once, in order
 * to parse more than one configuration document.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2004/10/18 01:07:41 $
 */
public class ConfigParser {


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>The <code>Digester</code> to be used for parsing.</p>
     */
    private Digester digester = null;


    /**
     * <p>The <code>RuleSet</code> to be used for configuring our Digester
     * parsing rules.</p>
     */
    private RuleSet ruleSet = null;


    /**
     * <p>Should Digester use the context class loader?
     */
    private boolean useContextClassLoader = true;


    // ------------------------------------------------------------- Properties


    /**
     * <p>Return the <code>Digester</code> instance to be used for
     * parsing, creating one if necessary.</p>
     */
    public Digester getDigester() {

        if (digester == null) {
            digester = new Digester();
            RuleSet ruleSet = getRuleSet();
            digester.setNamespaceAware(ruleSet.getNamespaceURI() != null);
            digester.setUseContextClassLoader(getUseContextClassLoader());
            digester.setValidating(false);
            digester.addRuleSet(ruleSet);
        }
        return (digester);

    }


    /**
     * <p>Return the <code>RuleSet</code> to be used for configuring
     * our <code>Digester</code> parsing rules, creating one if necessary.</p>
     */
    public RuleSet getRuleSet() {

        if (ruleSet == null) {
            ruleSet = new ConfigRuleSet();
        }
        return (ruleSet);

    }


    /**
     * <p>Set the <code>RuleSet</code> to be used for configuring
     * our <code>Digester</code> parsing rules.</p>
     *
     * @param ruleSet The new RuleSet to use
     */
    public void setRuleSet(RuleSet ruleSet) {

        this.digester = null;
        this.ruleSet = ruleSet;

    }


    /**
     * <p>Return the "use context class loader" flag.  If set to
     * <code>true</code>, Digester will attempt to instantiate new
     * command and chain instances from the context class loader.</p>
     */
    public boolean getUseContextClassLoader() {

        return (this.useContextClassLoader);

    }


    /**
     * <p>Set the "use context class loader" flag.</p>
     *
     * @param useContextClassLoader The new flag value
     */
    public void setUseContextClassLoader(boolean useContextClassLoader) {

        this.useContextClassLoader = useContextClassLoader;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Parse the XML document at the specified URL, using the configured
     * <code>RuleSet</code>, registering top level commands into the specified
     * {@link Catalog}.  Use this method <strong>only</strong> if you have
     * <strong>NOT</strong> included any <code>factory</code> element in your
     * configuration resource, and wish to supply the catalog explictly.</p>
     *
     * @param catalog {@link Catalog} into which configured chains are
     *  to be registered
     * @param url <code>URL</code> of the XML document to be parsed
     *
     * @exception Exception if a parsing error occurs
     *
     * @deprecated Use parse(URL) on a configuration resource with "factory"
     *  element(s) embedded
     */
    public void parse(Catalog catalog, URL url) throws Exception {

        // Prepare our Digester instance
        Digester digester = getDigester();
        digester.clear();
        digester.push(catalog);

        // Prepare our InputSource
        InputSource source = new InputSource(url.toExternalForm());
        source.setByteStream(url.openStream());

        // Parse the configuration document
        digester.parse(source);

    }


    /**
     * <p>Parse the XML document at the specified URL using the configured
     * <code>RuleSet</code>, registering catalogs with nested chains and
     * commands as they are encountered.  Use this method <strong>only</strong>
     * if you have included one or more <code>factory</code> elements in your
     * configuration resource.</p>
     *
     * @param url <code>URL</code> of the XML document to be parsed
     *
     * @exception Exception if a parsing error occurs
     */
    public void parse(URL url) throws Exception {

        // Prepare our Digester instance
        Digester digester = getDigester();
        digester.clear();

        // Prepare our InputSource
        InputSource source = new InputSource(url.toExternalForm());
        source.setByteStream(url.openStream());

        // Parse the configuration document
        digester.parse(source);

    }


}
