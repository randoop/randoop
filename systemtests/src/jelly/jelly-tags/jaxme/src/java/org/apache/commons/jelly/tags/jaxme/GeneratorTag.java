/**
* Copyright 2004 The Apache Software Foundation.
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
package org.apache.commons.jelly.tags.jaxme;

import java.io.File;
import java.io.IOException;

import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.JellyTagException;

import org.apache.ws.jaxme.generator.Generator;
import org.apache.ws.jaxme.generator.SchemaReader;
import org.apache.ws.jaxme.generator.sg.impl.JAXBSchemaReader;
import org.apache.ws.jaxme.generator.sg.SchemaSG;
import org.apache.ws.jaxme.generator.impl.GeneratorImpl;
import org.apache.ws.jaxme.js.JavaSourceFactory;

/** 
 * Generates java objects using JaxMe.
 * This object can be marshalled into xml and the results unmarshalled 
 * using JaxMe.
 *
 * @author <a href="mailto:joe@ispsoft.de">Jochen Wiedmann</a>
 * @author <a href="mailto:commons-dev at jakarta.apache.org">Jakarta Commons Development Team</a>
 * @version $Revision: 1.2 $
 */
public class GeneratorTag extends TagSupport {
        
    private String schemaUrl;
    private String target;
    
    public String getSchemaUrl() {
        return schemaUrl;
    }
        
    /**
     * Defines the schema against which the java object representations
     * should be generated.
     */
    public void setSchemaUrl(String schemaUrl) {
        this.schemaUrl = schemaUrl;
    }
    
    public String getTarget() {
        return target;
    }
    
    /**
     * Defines the target directory into which 
     * the generated objects will be placed.
     */
    public void setTarget(String target) {
        this.target = target;
    }
    
        
    private File getSchemaFile() throws JellyTagException {
        return new File(schemaUrl);
    }
    
    private File getTargetDirectory() throws JellyTagException {
        return new File(target);
    }
    
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
    
        if (schemaUrl == null) {
            throw new MissingAttributeException( "schemaUrl" );
        }
        
        if (target == null) {
            throw new MissingAttributeException( "target" );
        }    
        
        
        
        Generator generator = new GeneratorImpl();
        
        JAXBSchemaReader reader = new JAXBSchemaReader();
        generator.setSchemaReader(reader);
        reader.setGenerator(generator);
        generator.setTargetDirectory(getTargetDirectory());
        
        System.out.println("Target: " + getTargetDirectory());
        
        try
        {
            SchemaSG schemaSG = generator.generate(getSchemaFile());
        }
        catch (Exception e) 
        {
            throw new JellyTagException(e);
        }
    }
}
