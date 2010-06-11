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

public class AddressTypeHandler extends org.apache.ws.jaxme.impl.JMHandlerImpl {
  public static class NameTypeHandler extends org.apache.ws.jaxme.impl.JMHandlerImpl {
    /** The current level of nested elements. 0, if outside the root element.
     * 
     */
    private int __level;
  
    /** The current state. The following values are valid states:
     *  0 = Before parsing the element
     *  1 = Parsing an unknown element
     *  2 = After parsing the element
     *  3 = While parsing the child element {http://ws.apache.org/jaxme/examples/misc/address}First
     *  4 = While parsing the child element {http://ws.apache.org/jaxme/examples/misc/address}Middle
     *  5 = While parsing the child element {http://ws.apache.org/jaxme/examples/misc/address}Last
     *  6 = While parsing the child element {http://ws.apache.org/jaxme/examples/misc/address}Initials
     * 
     */
    private int __state;
  
    /** The current handler for parsing child elements or simple content.
     * 
     */
    private org.apache.ws.jaxme.JMHandler __handler;
  
  
    public void startDocument() throws org.xml.sax.SAXException {
      __level = 0;
      __state = 0;
      __state = 0;
      __handler = null;
    }
  
    protected org.apache.ws.jaxme.examples.misc.address.AddressType.NameType newResult() throws org.xml.sax.SAXException {
      return new org.apache.ws.jaxme.examples.misc.address.impl.AddressTypeImpl.NameTypeImpl();
    }
  
    public void startElement(java.lang.String pNamespaceURI, java.lang.String pLocalName, java.lang.String pQName, org.xml.sax.Attributes pAttr) throws org.xml.sax.SAXException {
      switch (__level++) {
        case 0:
          setResult(newResult());
          if (pAttr != null) {
            for (int _1 = 0;  _1 < pAttr.getLength();  _1++) {
              super.addAttribute(pAttr.getURI(_1), pAttr.getLocalName(_1), pAttr.getValue(_1));
            }
          }
          break;
        case 1:
          if ("http://ws.apache.org/jaxme/examples/misc/address".equals(pNamespaceURI)) {
            if ("First".equals(pLocalName)) {
              switch (__state) {
                case 0:
                  __state = 3;
                  __handler = getData().getAtomicHandler();
                  __handler.startDocument();
                  __handler.startElement(pNamespaceURI, pLocalName, pQName, pAttr);
                  break;
                default:
                  validationEvent(javax.xml.bind.ValidationEvent.WARNING, "The element " + pQName + " was unexpected at this place.", org.apache.ws.jaxme.ValidationEvents.EVENT_UNEXPECTED_CHILD_STATE);
                  break;
              }
            } else if ("Middle".equals(pLocalName)) {
              switch (__state) {
                case 3:
                case 4:
                  __state = 4;
                  __handler = getData().getAtomicHandler();
                  __handler.startDocument();
                  __handler.startElement(pNamespaceURI, pLocalName, pQName, pAttr);
                  break;
                default:
                  validationEvent(javax.xml.bind.ValidationEvent.WARNING, "The element " + pQName + " was unexpected at this place.", org.apache.ws.jaxme.ValidationEvents.EVENT_UNEXPECTED_CHILD_STATE);
                  break;
              }
            } else if ("Last".equals(pLocalName)) {
              switch (__state) {
                case 3:
                case 4:
                  __state = 5;
                  __handler = getData().getAtomicHandler();
                  __handler.startDocument();
                  __handler.startElement(pNamespaceURI, pLocalName, pQName, pAttr);
                  break;
                default:
                  validationEvent(javax.xml.bind.ValidationEvent.WARNING, "The element " + pQName + " was unexpected at this place.", org.apache.ws.jaxme.ValidationEvents.EVENT_UNEXPECTED_CHILD_STATE);
                  break;
              }
            } else if ("Initials".equals(pLocalName)) {
              switch (__state) {
                case 5:
                  __state = 6;
                  __handler = getData().getAtomicHandler();
                  __handler.startDocument();
                  __handler.startElement(pNamespaceURI, pLocalName, pQName, pAttr);
                  break;
                default:
                  validationEvent(javax.xml.bind.ValidationEvent.WARNING, "The element " + pQName + " was unexpected at this place.", org.apache.ws.jaxme.ValidationEvents.EVENT_UNEXPECTED_CHILD_STATE);
                  break;
              }
            }
          }
          break;
        default:
          if (__handler == null) {
            super.startElement(pNamespaceURI, pLocalName, pQName, pAttr);
          } else {
            __handler.startElement(pNamespaceURI, pLocalName, pQName, pAttr);
          }
      }
    }
  
    public void endElement(java.lang.String pNamespaceURI, java.lang.String pLocalName, java.lang.String pQName) throws org.xml.sax.SAXException {
      if (__handler == null) {
        if (__level > 1) {
          super.endElement(pNamespaceURI, pLocalName, pQName);
        }
      } else {
        __handler.endElement(pNamespaceURI, pLocalName, pQName);
      }
      switch (--__level) {
        case 0:
          break;
        case 1:
          org.apache.ws.jaxme.examples.misc.address.AddressType.NameType _1 = (org.apache.ws.jaxme.examples.misc.address.AddressType.NameType) getResult();
          switch (__state) {
            case 3:
              if (__handler != null) {
                __handler.endDocument();
              }
              _1.setFirst(((java.lang.String) __handler.getResult()));
              break;
            case 4:
              if (__handler != null) {
                __handler.endDocument();
              }
              _1.getMiddle().add(__handler.getResult());
              break;
            case 5:
              if (__handler != null) {
                __handler.endDocument();
              }
              _1.setLast(((java.lang.String) __handler.getResult()));
              break;
            case 6:
              if (__handler != null) {
                __handler.endDocument();
              }
              _1.setInitials(((java.lang.String) __handler.getResult()));
              break;
            default:
              throw new java.lang.IllegalStateException("Illegal state: " + __state);
          }
      }
    }
  
    public void characters(char[] pChars, int pOffset, int pLen) throws org.xml.sax.SAXException {
      if (__handler == null) {
        super.characters(pChars, pOffset, pLen);
      } else {
        __handler.characters(pChars, pOffset, pLen);
      }
    }
  
  }

  /** The current level of nested elements. 0, if outside the root element.
   * 
   */
  private int __level;

  /** The current state. The following values are valid states:
   *  0 = Before parsing the element
   *  1 = Parsing an unknown element
   *  2 = After parsing the element
   *  3 = While parsing the child element {http://ws.apache.org/jaxme/examples/misc/address}Name
   * 
   */
  private int __state;

  /** The current handler for parsing child elements or simple content.
   * 
   */
  private org.apache.ws.jaxme.JMHandler __handler;

  private org.apache.ws.jaxme.JMHandler __handler_Name;


  public void startDocument() throws org.xml.sax.SAXException {
    __level = 0;
    __state = 0;
    __state = 0;
    __handler = null;
  }

  public void addAttribute(java.lang.String pURI, java.lang.String pLocalName, java.lang.String pValue) throws org.xml.sax.SAXException {
    if (pURI == null) {
      pURI = "";
    }
    org.apache.ws.jaxme.examples.misc.address.AddressType _1 = (org.apache.ws.jaxme.examples.misc.address.AddressType) getResult();
    if ("".equals(pURI)) {
      if ("id".equals(pLocalName)) {
        _1.setId(pValue);
        return;
      }
    }
    super.addAttribute(pURI, pLocalName, pValue);
  }

  protected org.apache.ws.jaxme.examples.misc.address.AddressType newResult() throws org.xml.sax.SAXException {
    try {
      return (org.apache.ws.jaxme.examples.misc.address.AddressType) getData().getFactory().getElement(org.apache.ws.jaxme.examples.misc.address.AddressType.class);
    } catch (javax.xml.bind.JAXBException _1) {
      throw new org.xml.sax.SAXException(_1);
    }
  }

  public void startElement(java.lang.String pNamespaceURI, java.lang.String pLocalName, java.lang.String pQName, org.xml.sax.Attributes pAttr) throws org.xml.sax.SAXException {
    switch (__level++) {
      case 0:
        setResult(newResult());
        if (pAttr != null) {
          for (int _1 = 0;  _1 < pAttr.getLength();  _1++) {
            addAttribute(pAttr.getURI(_1), pAttr.getLocalName(_1), pAttr.getValue(_1));
          }
        }
        break;
      case 1:
        if ("http://ws.apache.org/jaxme/examples/misc/address".equals(pNamespaceURI)) {
          if ("Name".equals(pLocalName)) {
            switch (__state) {
              case 0:
                __state = 3;
                __handler = getHandlerForName();
                __handler.startDocument();
                __handler.startElement(pNamespaceURI, pLocalName, pQName, pAttr);
                break;
              default:
                validationEvent(javax.xml.bind.ValidationEvent.WARNING, "The element " + pQName + " was unexpected at this place.", org.apache.ws.jaxme.ValidationEvents.EVENT_UNEXPECTED_CHILD_STATE);
                break;
            }
          }
        }
        break;
      default:
        if (__handler == null) {
          super.startElement(pNamespaceURI, pLocalName, pQName, pAttr);
        } else {
          __handler.startElement(pNamespaceURI, pLocalName, pQName, pAttr);
        }
    }
  }

  public void endElement(java.lang.String pNamespaceURI, java.lang.String pLocalName, java.lang.String pQName) throws org.xml.sax.SAXException {
    if (__handler == null) {
      if (__level > 1) {
        super.endElement(pNamespaceURI, pLocalName, pQName);
      }
    } else {
      __handler.endElement(pNamespaceURI, pLocalName, pQName);
    }
    switch (--__level) {
      case 0:
        break;
      case 1:
        org.apache.ws.jaxme.examples.misc.address.AddressType _1 = (org.apache.ws.jaxme.examples.misc.address.AddressType) getResult();
        switch (__state) {
          case 3:
            if (__handler != null) {
              __handler.endDocument();
            }
            _1.setName(((org.apache.ws.jaxme.examples.misc.address.AddressType.NameType) __handler.getResult()));
            break;
          default:
            throw new java.lang.IllegalStateException("Illegal state: " + __state);
        }
    }
  }

  public void characters(char[] pChars, int pOffset, int pLen) throws org.xml.sax.SAXException {
    if (__handler == null) {
      super.characters(pChars, pOffset, pLen);
    } else {
      __handler.characters(pChars, pOffset, pLen);
    }
  }

  public void init(org.apache.ws.jaxme.JMHandler.Data pData) throws javax.xml.bind.JAXBException {
    super.init(pData);
    if (__handler_Name != null) {
      __handler_Name.init(pData);
    }
  }

  protected org.apache.ws.jaxme.JMHandler getHandlerForName() throws org.xml.sax.SAXException {
    if (__handler_Name == null) {
      try {
        __handler_Name = new org.apache.ws.jaxme.examples.misc.address.impl.AddressTypeHandler.NameTypeHandler();
        __handler_Name.init(getData());
      } catch (javax.xml.bind.JAXBException _1) {
        throw new org.xml.sax.SAXException(_1);
      }
    }
    return __handler_Name;
  }

}
