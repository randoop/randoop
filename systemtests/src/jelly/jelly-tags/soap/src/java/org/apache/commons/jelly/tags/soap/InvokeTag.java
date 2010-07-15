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
package org.apache.commons.jelly.tags.soap;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * Invokes a web service
 *
 * @author <a href="mailto:jim@bnainc.net">James Birchfield</a>
 * @version $Revision: 1.6 $
 */
public class InvokeTag extends TagSupport {

    private String var;
    private String endpoint = null;
    private String namespace = null;
    private String method = null;
    private String username;
    private String password;
    private Service service;
    private Object params;

    public InvokeTag() {
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if (endpoint == null) {
            throw new MissingAttributeException("endpoint");
        }
        if (namespace == null) {
            throw new MissingAttributeException("namespace");
        }
        if (method == null) {
            throw new MissingAttributeException("method");
        }

        Object[] params = getParamArray();
        if (params == null) {
            params = new Object[]{ getBodyText() };
        } else {
            // invoke body just in case we have nested tags
            invokeBody(output);
        }

        Service service = getService();
        if (service == null) {
            service = createService();
        }

        Object answer = null;
        try {
            Call call = (Call) service.createCall();

            // @todo Jelly should have native support for URL and QName
            // directly on properties
            call.setTargetEndpointAddress(new java.net.URL(endpoint));
            call.setOperationName(new QName(namespace, method));

            if ( username != null && !username.equals("") ) {
                call.setUsername( username );
                call.setPassword( password );
            }
            
            answer = call.invoke(params);
        } catch (MalformedURLException e) {
            throw new JellyTagException(e);
        } catch (ServiceException e) {
            throw new JellyTagException(e);
        } catch (RemoteException e) {
            throw new JellyTagException(e);
        }

        if (var != null) {
            context.setVariable(var, answer);
        } else {
            // should turn the answer into XML events...
            throw new JellyTagException( "Not implemented yet; should stream results as XML events. Results: " + answer );
        }
    }


    // Properties
    //-------------------------------------------------------------------------
    /**
     * Sets the end point to which the invocation will occur
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Sets the namespace of the operation
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Returns the service to be used by this web service invocation.
     * @return Service
     */
    public Service getService() {
        return service;
    }

    /**
     * Sets the service to be used by this invocation.
     * If none is specified then a default is used.
     */
    public void setService(Service service) {
        this.service = service;
    }

    /**
     * Sets the name of the variable to output the results of the SOAP call to.
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Sets the parameters for this SOAP call. This can be an array or collection of
     * SOAPBodyElements or types.
     */
    public void setParams(Object params) {
        this.params = params;
    }

    /**
     * Set the password for the SOAP call.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Set the username for the SOAP call.
     */
    public void setUsername(String username)
    {
        this.username = username;
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Factory method to create a new default Service instance
     */
    protected Service createService() {
        return new Service();
    }

    /**
     * Performs any type coercion on the given parameters to form an Object[]
     * or returns null if no parameter has been specified
     */
    protected Object[] getParamArray() {
        if (params == null) {
            return null;
        }
        if (params instanceof Object[]) {
            return (Object[]) params;
        }
        if (params instanceof Collection) {
            Collection coll = (Collection) params;
            return coll.toArray();
        }
        // lets just wrap the current object inside an array
        return new Object[] { params };
    }
}
