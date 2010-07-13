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


import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;


/**
 * <p>Digester rule that will dynamically register a new set of rules
 * for a specified element name and default implementation class.  This
 * allows "alias" elements to be created for {@link Chain} and {@link Command}
 * implementation classes that are commonly used.  Besides factoring out
 * the class names to make changes easier, this also makes configuration
 * files <strong>much</strong> easier to read and write.</p>
 *
 * @version $Revision: 1.2 $ $Date: 2004/11/30 05:52:23 $
 */

class ConfigDefineRule extends Rule {


    // ----------------------------------------------------------- Constructors


    /**
     * <p>Construct a new instance of this rule that will in turn
     * dynamically register appropriate rules for a new alias element.</p>
     *
     * @param nameAttribute Name of the attribute containing the name
     *  of the new element for which rules should generated
     * @param classAttribute Name of the attribute containing the
     *  implementation class for the new chain or command
     */
    public ConfigDefineRule(String nameAttribute, String classAttribute) {
        super();
        this.nameAttribute = nameAttribute;
        this.classAttribute = classAttribute;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>The name of the attribute under which we can retrieve the
     * fully qualified class name of the implementation class for this
     * new element.</p>
     */
    private String classAttribute = null;


    /**
     * <p>The name of the attribute under which we can retrieve the name
     * this element for which rules should be created.</p>
     */
    private String nameAttribute = null;


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Register new rules for the specified name and class.</p>
     *
     * @param namespace the namespace URI of the matching element, or an
     *   empty string if the parser is not namespace aware or the element has
     *   no namespace
     * @param name the local name if the parser is namespace aware, or just
     *   the element name otherwise
     * @param attributes The attribute list of this element
     */
    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {

        // Extract the actual name and implementation class to use
        String nameValue = attributes.getValue(nameAttribute);
        String classValue = attributes.getValue(classAttribute);

        // Add rules for this new element
        digester.addObjectCreate("*/" + nameValue, classValue);
        digester.addSetProperties("*/" + nameValue);
        digester.addRule("*/" + nameValue,
                         new ConfigRegisterRule(nameAttribute));

    }


}
