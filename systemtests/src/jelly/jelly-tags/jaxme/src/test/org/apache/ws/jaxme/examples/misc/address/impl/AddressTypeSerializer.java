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

package org.apache.ws.jaxme.examples.misc.address.impl;

public class AddressTypeSerializer extends org.apache.ws.jaxme.impl.JMXmlSerializerImpl {
  public static class NameTypeSerializer extends org.apache.ws.jaxme.impl.JMXmlSerializerImpl {
    final static javax.xml.namespace.QName __ser_First_qname = new javax.xml.namespace.QName("http://ws.apache.org/jaxme/examples/misc/address", "First");
  
    final static javax.xml.namespace.QName __ser_Middle_qname = new javax.xml.namespace.QName("http://ws.apache.org/jaxme/examples/misc/address", "Middle");
  
    final static javax.xml.namespace.QName __ser_Last_qname = new javax.xml.namespace.QName("http://ws.apache.org/jaxme/examples/misc/address", "Last");
  
    final static javax.xml.namespace.QName __ser_Initials_qname = new javax.xml.namespace.QName("http://ws.apache.org/jaxme/examples/misc/address", "Initials");
  
  
    protected void marshalChilds(org.apache.ws.jaxme.JMXmlSerializer.Data pData, java.lang.Object pObject) throws org.xml.sax.SAXException {
      org.apache.ws.jaxme.examples.misc.address.AddressType.NameType _1 = (org.apache.ws.jaxme.examples.misc.address.AddressType.NameType) pObject;
      java.lang.String _2 = _1.getFirst();
      if (_2 != null) {
        marshalAtomicChild(pData, __ser_First_qname, _1.getFirst());
      }
      java.util.List _3 = _1.getMiddle();
      for (int _4 = 0;  _4 < (_3).size();  _4++) {
        java.lang.String _5 = (java.lang.String)_3.get(_4);
        if (_5 != null) {
          marshalAtomicChild(pData, __ser_Middle_qname, (java.lang.String)_3.get(_4));
        }
      }
      java.lang.String _6 = _1.getLast();
      if (_6 != null) {
        marshalAtomicChild(pData, __ser_Last_qname, _1.getLast());
      }
      java.lang.String _7 = _1.getInitials();
      if (_7 != null) {
        marshalAtomicChild(pData, __ser_Initials_qname, _1.getInitials());
      }
    }
  
  }

  private org.apache.ws.jaxme.examples.misc.address.impl.AddressTypeSerializer.NameTypeSerializer __ser_Name;

  final static javax.xml.namespace.QName __ser_Name_qname = new javax.xml.namespace.QName("http://ws.apache.org/jaxme/examples/misc/address", "Name");


  protected org.xml.sax.helpers.AttributesImpl getAttributes(org.apache.ws.jaxme.JMXmlSerializer.Data pData, java.lang.Object pElement) throws org.xml.sax.SAXException {
    org.xml.sax.helpers.AttributesImpl _1 = super.getAttributes(pData, pElement);
    org.apache.ws.jaxme.examples.misc.address.AddressType _2 = (org.apache.ws.jaxme.examples.misc.address.AddressType) pElement;
    java.lang.String _3 = _2.getId();
    if (_3 != null) {
      _1.addAttribute("", "id", getAttributeQName(pData, "", "id"), "CDATA", _2.getId());
    }
    return _1;
  }

  public void init(org.apache.ws.jaxme.impl.JAXBContextImpl pFactory) throws javax.xml.bind.JAXBException {
    super.init(pFactory);
    __ser_Name = new org.apache.ws.jaxme.examples.misc.address.impl.AddressTypeSerializer.NameTypeSerializer();
    __ser_Name.init(pFactory);
  }

  protected void marshalChilds(org.apache.ws.jaxme.JMXmlSerializer.Data pData, java.lang.Object pObject) throws org.xml.sax.SAXException {
    org.apache.ws.jaxme.examples.misc.address.AddressType _1 = (org.apache.ws.jaxme.examples.misc.address.AddressType) pObject;
    org.apache.ws.jaxme.examples.misc.address.AddressType.NameType _2 = _1.getName();
    if (_2 != null) {
      __ser_Name.marshal(pData, __ser_Name_qname, _1.getName());
    }
  }

}
