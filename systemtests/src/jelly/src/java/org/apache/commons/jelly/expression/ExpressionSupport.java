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
package org.apache.commons.jelly.expression;

import java.util.Collections;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.collections.iterators.SingletonIterator;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.lang.StringUtils;

/** <p><code>ExpressionSupport</code>
  * an abstract base class for Expression implementations
  * which provides default implementations of some of the
  * typesafe evaluation methods.</p>
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.15 $
  */
public abstract class ExpressionSupport implements Expression {

    protected static final Iterator EMPTY_ITERATOR = Collections.EMPTY_LIST.iterator();

    // inherit javadoc from interface
    public String evaluateAsString(JellyContext context) {
        Object value = evaluateRecurse(context);
        // sometimes when Jelly is used inside Maven the value
        // of an expression can actually be an expression.
        // e.g. ${foo.bar} can lookup "foo.bar" in a Maven context
        // which could actually be an expression

        if ( value != null ) {
            return value.toString();
        }
        return null;
    }


    // inherit javadoc from interface
    public Object evaluateRecurse(JellyContext context) {
        Object value = evaluate(context);
        if (value instanceof Expression) {
            Expression expression = (Expression) value;
            return expression.evaluateRecurse(context);
        }
        return value;
    }

    // inherit javadoc from interface
    public boolean evaluateAsBoolean(JellyContext context) {
        Object value = evaluateRecurse(context);
        if ( value instanceof Boolean ) {
            Boolean b = (Boolean) value;
            return b.booleanValue();
        }
        else if ( value instanceof String ) {
            // return Boolean.getBoolean( (String) value );
            String str = (String) value;

            return ( str.equalsIgnoreCase( "on" )
                 ||
                 str.equalsIgnoreCase( "yes" )
                 ||
                 str.equals( "1" )
                 ||
                 str.equalsIgnoreCase( "true" ) );

        }
        return false;
    }

    // inherit javadoc from interface
    public Iterator evaluateAsIterator(JellyContext context) {
        Object value = evaluateRecurse(context);
        if ( value == null ) {
            return EMPTY_ITERATOR;
        } else if ( value instanceof Iterator ) {
            return (Iterator) value;
        } else if ( value instanceof List ) {
            List list = (List) value;
            return list.iterator();
        } else if ( value instanceof Map ) {
            Map map = (Map) value;
            return map.entrySet().iterator();
        } else if ( value.getClass().isArray() ) {
            return new ArrayIterator( value );
        } else if ( value instanceof Enumeration ) {
            return new EnumerationIterator((Enumeration ) value);
        } else if ( value instanceof Collection ) {
          Collection collection = (Collection) value;
          return collection.iterator();
        } else if ( value instanceof String ) {
           String[] array = StringUtils.split((String) value, "," );
           array = StringUtils.stripAll( array );
           return new ArrayIterator( array );
        } else {
            // XXX: should we return single iterator?
            return new SingletonIterator( value );
        }
    }
}
