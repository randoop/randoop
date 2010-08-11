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

package org.apache.commons.jelly.tags.ant;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MapTagSupport;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.impl.BeanSource;
import org.apache.commons.jelly.impl.StaticTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskAdapter;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.types.DataType;

/**
 * Tag supporting ant's Tasks as well as
 * dynamic runtime behaviour for 'unknown' tags.
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 */
public class AntTag extends MapTagSupport implements TaskSource {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(AntTag.class);

    private static final Class[] addTaskParamTypes = { String.class };

    /** store the name of the manifest tag for special handling */
    private static final String ANT_MANIFEST_TAG = "manifest";

    /** The name of this tag. */
    protected String tagName;

    /** The general object underlying this tag. */
    protected Object object;

    /** Task, if this tag represents a task. */
    protected Task task;


    /** Construct with a project and tag name.
     *
     *  @param tagName The name on the tag.
     */
    public AntTag(String tagName) {
        this.tagName = tagName;
    }

    public String toString() {
        return "[AntTag: name=" + getTagName() + "]";
    }

    // TaskSource interface
    //-------------------------------------------------------------------------

    /** Retrieve the general object underlying this tag.
     *
     *  @return The object underlying this tag.
     */
    public Object getTaskObject() {
        return this.object;
    }

    /**
     * Allows nested tags to set a property on the task object of this tag
     */
    public void setTaskProperty(String name, Object value) throws JellyTagException {
        Object object = getTaskObject();
        if ( object != null ) {
            setBeanProperty( object, name, value );
        }
    }

    // Tag interface
    //-------------------------------------------------------------------------
    public void doTag(XMLOutput output) throws JellyTagException {

        Project project = getAntProject();
        String tagName = getTagName();
        Object parentObject = findBeanAncestor();
        Object parentTask = findParentTaskObject();

        // lets assume that Task instances are not nested inside other Task instances
        // for example <manifest> inside a <jar> should be a nested object, where as
        // if the parent is not a Task the <manifest> should create a ManifestTask
        //
        // also its possible to have a root Ant tag which isn't a task, such as when
        // defining <fileset id="...">...</fileset>

        Object nested = null;
        if (parentObject != null && !( parentTask instanceof TaskContainer) ) {
            nested = createNestedObject( parentObject, tagName );
        }

        if (nested == null) {
            task = createTask( tagName );

            if (task != null) {

                if ( log.isDebugEnabled() ) {
                    log.debug( "Creating an ant Task for name: " + tagName );
                }

                // the following algorithm follows the lifetime of a tag
                // http://jakarta.apache.org/ant/manual/develop.html#writingowntask
                // kindly recommended by Stefan Bodewig

                // create and set its project reference
                if ( task instanceof TaskAdapter ) {
                    setObject( ((TaskAdapter)task).getProxy() );
                }
                else {
                    setObject( task );
                }

                // set the task ID if one is given
                Object id = getAttributes().remove( "id" );
                if ( id != null ) {
                    project.addReference( (String) id, task );
                }

                // ### we might want to spoof a Target setting here

                // now lets initialize
                task.init();

                // now lets invoke the body to call all the createXXX() or addXXX() methods
                String body = getBodyText();

                // now lets set any attributes of this tag...
                setBeanProperties();

                // now lets set the addText() of the body content, if its applicaable
                Method method = MethodUtils.getAccessibleMethod( task.getClass(),
                                                                 "addText",
                                                                 addTaskParamTypes );
                if (method != null) {
                    Object[] args = { body };
                    try {
                        method.invoke(this.task, args);
                    }
                    catch (IllegalAccessException e) {
                        throw new JellyTagException(e);
                    }
                    catch (InvocationTargetException e) {
                        throw new JellyTagException(e);
                    }
                }

                // now lets set all the attributes of the child elements
                // XXXX: to do!

                // now we're ready to invoke the task
                // XXX: should we call execute() or perform()?
                task.perform();
            }
        }

        if (task == null) {

            if (nested == null) {

                if ( log.isDebugEnabled() ) {
                    log.debug( "Trying to create a data type for tag: " + tagName );
                }
                nested = createDataType( tagName );
            }
            else {
                if ( log.isDebugEnabled() ) {
                    log.debug( "Created nested property tag: " + tagName );
                }
            }

            if ( nested != null ) {
                setObject( nested );

                // set the task ID if one is given
                Object id = getAttributes().remove( "id" );
                if ( id != null ) {
                    project.addReference( (String) id, nested );
                }

                // TODO: work out why we always set the name attribute.
                // See JELLY-105.
//                try{
//                    PropertyUtils.setProperty( nested, "name", tagName );
//                }
//                catch (Exception e) {
//                    log.warn( "Caught exception setting nested name: " + tagName, e );
//                }

                // now lets invoke the body
                String body = getBodyText();

                // now lets set any attributes of this tag...
                setBeanProperties();

                // now lets add it to its parent
                if ( parentObject != null ) {
                    IntrospectionHelper ih = IntrospectionHelper.getHelper( parentObject.getClass() );
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("About to set the: " + tagName
                                + " property on: " + parentObject + " to value: "
                                + nested + " with type: " + nested.getClass()
                            );
                        }

                        ih.storeElement( project, parentObject, nested, tagName.toLowerCase() );
                    }
                    catch (Exception e) {
                        log.warn( "Caught exception setting nested: " + tagName, e );
                    }

                    // now try to set the property for good measure
                    // as the storeElement() method does not
                    // seem to call any setter methods of non-String types
                    try {
                        BeanUtils.setProperty( parentObject, tagName, nested );
                    }
                    catch (Exception e) {
                        log.debug("Caught exception trying to set property: " + tagName + " on: " + parentObject);
                    }
                }
            }
            else {
                log.warn("Could not convert tag: " + tagName + " into an Ant task, data type or property");

                // lets treat this tag as static XML...
                StaticTag tag = new StaticTag("", tagName, tagName);
                tag.setParent( getParent() );
                tag.setBody( getBody() );

                tag.setContext(context);

                for (Iterator iter = getAttributes().entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String name = (String) entry.getKey();
                    Object value = entry.getValue();

                    tag.setAttribute(name, value);
                }

                tag.doTag(output);
            }
        }
    }


    // Properties
    //-------------------------------------------------------------------------
    public String getTagName() {
        return this.tagName;
    }

    /** Set the object underlying this tag.
     *
     *  @param object The object.
     */
    public void setObject(Object object) {
        this.object = object;
    }

    public Project getAntProject() {
        Project project = AntTagLibrary.getProject(context);
        if (project == null) {
            throw new NullPointerException("No Ant Project object is available");
        }
        return project;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Sets the properties on the Ant task
     */
    public void setBeanProperties() throws JellyTagException {
        Object object = getTaskObject();
        if ( object != null ) {
            Map map = getAttributes();
            for ( Iterator iter = map.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = (String) entry.getKey();
                Object value = entry.getValue();
                setBeanProperty( object, name, value );
            }
        }
    }

    public void setAttribute(String name, Object value) {
        if ( value == null ) {
            // should we send in null?
            super.setAttribute( name, "" );
        }
        else {
            if ( value instanceof Expression )
            {
                super.setAttribute( name, ((Expression) value).evaluateRecurse(context) );
            }
            else
            {
                super.setAttribute( name, value.toString() );
            }
        }
    }

    public void setBeanProperty(Object object, String name, Object value) throws JellyTagException {
        if ( log.isDebugEnabled() ) {
            log.debug( "Setting bean property on: "+  object + " name: " + name + " value: " + value );
        }

        IntrospectionHelper ih = IntrospectionHelper.getHelper( object.getClass() );

        if ( value instanceof String ) {
            try {
                ih.setAttribute( getAntProject(), object, name.toLowerCase(), (String) value );
                return;
            }
            catch (Exception e) {
                // ignore: not a valid property
            }
        }

        try {

            ih.storeElement( getAntProject(), object, value, name );
        }
        catch (Exception e) {

            try {
                // let any exceptions bubble up from here
                BeanUtils.setProperty( object, name, value );
            }
            catch (IllegalAccessException ex) {
                throw new JellyTagException(ex);
            }
            catch (InvocationTargetException ex) {
                throw new JellyTagException(ex);
            }
        }
    }


    /**
     * Creates a nested object of the given object with the specified name
     */
    public Object createNestedObject(Object object, String name) {
        Object dataType = null;
        if ( object != null ) {
            IntrospectionHelper ih = IntrospectionHelper.getHelper( object.getClass() );

            if ( ih != null ) {
                try {
                    dataType = ih.createElement( getAntProject(), object, name.toLowerCase() );
                } catch (BuildException be) {
                    if (object instanceof Tag)
                    {
                        if (log.isDebugEnabled()) {
                            log.debug("Failed attempt to create an ant datatype for a jelly tag", be);
                        }
                    } else {
                        log.error(be);
                    }
                }
            }
        }

        if ( dataType == null ) {
            dataType = createDataType( name );
        }

        return dataType;
    }

    public Object createDataType(String name) {

        Object dataType = null;

        Class type = (Class) getAntProject().getDataTypeDefinitions().get(name);

        if ( type != null ) {

            Constructor ctor = null;
            boolean noArg = false;

            // DataType can have a "no arg" constructor or take a single
            // Project argument.
            try {
                ctor = type.getConstructor(new Class[0]);
                noArg = true;
            }
            catch (NoSuchMethodException nse) {
                try {
                    ctor = type.getConstructor(new Class[] { Project.class });
                    noArg = false;
                } catch (NoSuchMethodException nsme) {
                    log.info("datatype '" + name
                        + "' didn't have a constructor with an Ant Project", nsme);
                }
            }

            if (noArg) {
                dataType = createDataType(ctor, new Object[0], name, "no-arg constructor");
            }
            else {
                dataType = createDataType(ctor, new Object[] { getAntProject() }, name, "an Ant project");
            }
            if (dataType != null) {
                ((DataType)dataType).setProject( getAntProject() );
            }
        }

        return dataType;
    }

    /**
     * @return an object create with the given constructor and args.
     * @param ctor a constructor to use creating the object
     * @param args the arguments to pass to the constructor
     * @param name the name of the data type being created
     * @param argDescription a human readable description of the args passed
     */
    private Object createDataType(Constructor ctor, Object[] args, String name, String argDescription) {
        try {
            Object datatype = ctor.newInstance(args);
            return datatype;
        } catch (InstantiationException ie) {
            log.error("datatype '" + name + "' couldn't be created with " + argDescription, ie);
        } catch (IllegalAccessException iae) {
            log.error("datatype '" + name + "' couldn't be created with " + argDescription, iae);
        } catch (InvocationTargetException ite) {
            log.error("datatype '" + name + "' couldn't be created with " + argDescription, ite);
        }
        return null;
    }

    /**
     * @param taskName
     * @return
     * @throws JellyTagException
     */
    public Task createTask(String taskName) throws JellyTagException {
        return createTask( taskName,
                           (Class) getAntProject().getTaskDefinitions().get( taskName ) );
    }

    public Task createTask(String taskName,
                           Class taskType) throws JellyTagException {

        if (taskType == null) {
            return null;
        }

        Object o = null;
        try {
            o = taskType.newInstance();
        } catch (InstantiationException e) {
            throw new JellyTagException(e);
        }
        catch (IllegalAccessException e) {
            throw new JellyTagException(e);
        }

        Task task = null;
        if ( o instanceof Task ) {
            task = (Task) o;
        }
        else {
            TaskAdapter taskA=new TaskAdapter();
            taskA.setProxy( o );
            task=taskA;
        }

        task.setProject(getAntProject());
        task.setTaskName(taskName);

        return task;
    }

    /**
     * Attempts to look up in the parent hierarchy for a tag that implements the
     * TaskSource interface, which returns an Ant Task object or that implements
     * BeanSource interface which creates a bean,
     * or will return the parent tag, which is also a bean.
     */
    protected Object findBeanAncestor() throws JellyTagException {
        Tag tag = getParent();
        while (tag != null) {
            if (tag instanceof BeanSource) {
                BeanSource beanSource = (BeanSource) tag;
                return beanSource.getBean();
            }
            if (tag instanceof TaskSource) {
                TaskSource taskSource = (TaskSource) tag;
                return taskSource.getTaskObject();
            }
            tag = tag.getParent();
        }
        return getParent();
    }

    /**
     * Walks the hierarchy until it finds a parent TaskSource and returns its source or returns null
     */
    protected Object findParentTaskObject() throws JellyTagException {
        Tag tag = getParent();
        while (tag != null) {
            if (tag instanceof TaskSource) {
                TaskSource source = (TaskSource) tag;
                return source.getTaskObject();
            }
            tag = tag.getParent();
        }
        return null;
    }

}
