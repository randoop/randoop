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
package org.apache.ws.jaxme.examples.misc.address;

public class ObjectFactory {
  private org.apache.ws.jaxme.impl.JAXBContextImpl jaxbContext;

  private java.util.Map properties;


  public ObjectFactory() throws javax.xml.bind.JAXBException {
    jaxbContext = (org.apache.ws.jaxme.impl.JAXBContextImpl) javax.xml.bind.JAXBContext.newInstance("org.apache.ws.jaxme.examples.misc.address");
  }

  public java.lang.Object newInstance(java.lang.Class pElementInterface) throws javax.xml.bind.JAXBException {
    return jaxbContext.getElement(pElementInterface);
  }

  public java.lang.Object getProperty(java.lang.String pName) {
    if (properties == null) {
      return null;
    }
    return properties.get(pName);
  }

  public void setProperty(java.lang.String pName, java.lang.Object pValue) {
    if (properties == null) {
      properties = new java.util.HashMap();
    }
    properties.put(pName, pValue);
  }

  public org.apache.ws.jaxme.examples.misc.address.Address createAddress() throws javax.xml.bind.JAXBException {
    return (org.apache.ws.jaxme.examples.misc.address.Address) newInstance(org.apache.ws.jaxme.examples.misc.address.Address.class);
  }

  public org.apache.ws.jaxme.examples.misc.address.AddressType createAddressType() throws javax.xml.bind.JAXBException {
    return (org.apache.ws.jaxme.examples.misc.address.AddressType) newInstance(org.apache.ws.jaxme.examples.misc.address.AddressType.class);
  }

}
