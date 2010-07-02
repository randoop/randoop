/*
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.commons.chain;


import java.util.Map;


/**
 * <p>A {@link Context} represents the state information that is
 * accessed and manipulated by the execution of a {@link Command} or a
 * {@link Chain}.  Specialized implementations of {@link Context} will
 * typically add JavaBeans properties that contain typesafe accessors
 * to information that is relevant to a particular use case for this
 * context, and/or add operations that affect the state information
 * that is saved in the context.</p>
 *
 * <p>Implementations of {@link Context} must also implement all of the
 * required and optional contracts of the <code>java.util.Map</code>
 * interface.</p>
 *
 * <p>It is strongly recommended, but not required, that JavaBeans
 * properties added to a particular {@link Context} implementation exhibit
 * <em>Attribute-Property Transparency</em>.  In other words,
 * a value stored via a call to <code>setFoo(value)</code> should be visible
 * by calling <code>get("foo")</code>, and a value stored
 * via a call to <code>put("foo", value)</code> should be
 * visible by calling <code>getFoo()</code>.  If your {@link Context}
 * implementation class exhibits this featue, it becomes easier to reuse the
 * implementation in multiple environments, without the need to cast to a
 * particular implementation class in order to access the property getter
 * and setter methods.</p>
 *
 * <p>To protect applications from evolution of this interface, specialized
 * implementations of {@link Context} should generally be created by extending
 * the provided base class ({@link org.apache.commons.chain.impl.ContextBase})
 * rather than directly implementing this interface.</p>
 *
 * <p>Applications should <strong>NOT</strong> assume that
 * {@link Context} implementations, or the values stored in its
 * attributes, may be accessed from multiple threads
 * simultaneously unless this is explicitly documented for a particular
 * implementation.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6 $ $Date: 2004/02/25 00:01:07 $
 */

public interface Context extends Map {


}
