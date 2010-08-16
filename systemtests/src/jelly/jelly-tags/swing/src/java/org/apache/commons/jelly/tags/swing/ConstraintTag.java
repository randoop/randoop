/*
* Copyright 2002-2004 The Apache Software Foundation
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
package org.apache.commons.jelly.tags.swing;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.jelly.DynaBeanTagSupport;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.TagFactory;
import org.xml.sax.Attributes;

/** This class represents a layout-manager constraints as passed in
    * the second argument of {@link Container#add(Component,Object)}.
    *    <p>
    *    In essence, it looks really like nothing else than a bean-class...
    *    with {@link #getConstraintObject}.
    *    Probably a shorter java-source is do-able.
    *    <p>
    *    TODO: this class should probably be extended with special treatment for dimensios
    *    using the converter package.
    */
public class ConstraintTag extends DynaBeanTagSupport {

/*    TODO: make a gridbagconstraintTag class which supports an attribute "parent"
                        (or... startWith) which is another gridbagconstraintTag whose gridbagconstraint
                        is cloned then attributes are set
                        This tag should also support the attributes such as fill=BOTH
                        and anchor=NORTHEAST...
                        Wooops... need to define setters ?? let's see if BeanUtils does it on public vars
                        And... have an insets?? A child ?
    */

    protected Factory factory;
    protected String var = null;
    protected Object bean = null;

        public static class HereFactory extends BeanFactory implements TagFactory {
            public HereFactory(Class c) { super(c); }
            public Tag createTag(String name, Attributes attributes) {
                return new ConstraintTag ( this );
                // still scratching my head about "this" usage...
            }
        } // class HereFactory
        public static class ConstantFactory implements TagFactory, Factory {
            public ConstantFactory(Object c) { this.constant = c;}
            private Object constant;
            public Object newInstance() { return constant; }
            public Tag createTag(String name, Attributes attributes) throws JellyException {
                return new ConstraintTag ( this );
            }
        } // class ConstatnStringFactory

        // we could be able to make factories that create their tags in parametrized
        // subclasses of the tag depending on the name and attributes
        // it would useful, for example, to make a cardLayout's <card name="">


    public ConstraintTag (Factory factory) {
        this.factory = factory;
    }

    protected void createBean ( Factory factory ) throws InstantiationException {
        bean = factory.newInstance();
    }

    // --------------------------------------------- ATTRIBUTES

    public void beforeSetAttributes (  ) throws JellyTagException {
        try {
            createBean(factory);
        } catch (InstantiationException e) {
            throw new JellyTagException(e.toString());
        }
    }


    public void setAttribute ( String name, Object value ) throws JellyTagException {
        // no real need for DynaBeans or ?
        if ( "var".equals(name) ) {
            var = value.toString();
        } else {

            try {
              BeanUtils.setProperty( bean, name, value );
            } catch (IllegalAccessException e) {
                throw new JellyTagException(e.toString());
            } catch (InvocationTargetException e) {
                throw new JellyTagException(e.toString());
            }

        }
    }
// --------------------------------------------------
    /** Children invocation... just nothing...
        */
    public void doTag ( XMLOutput output ) throws JellyTagException {
        if ( var != null ) context.setVariable ( var, getBean() );
        invokeBody ( output );
        // nothing else to do... the getConstraintObject method should have been called.
    }

    // ----------------------------------------------
    public Object getBean() {
        return bean;
    }

    /** Returns the attached constraint object.
        */
    public Object getConstraintObject() {
        return getBean();
    }
} // class ConstraintTag
