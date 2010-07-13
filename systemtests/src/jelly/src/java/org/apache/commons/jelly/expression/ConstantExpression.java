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

import org.apache.commons.jelly.JellyContext;

/** <p><code>ConstantExpression</code> represents a constant expression.</p>
  *
  * <p> In other words, {@link #evaluate} returns a value independent of the context. </p>
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.9 $
  */
public class ConstantExpression extends ExpressionSupport {

    /** The value of this expression */
    private Object value;

    /** Base constructor
     */
    public ConstantExpression() {
    }

    /** Convenience constructor sets <code>value</code> property.
     */
    public ConstantExpression(Object value) {
        this.value = value;
    }

    public String toString() {
        return super.toString() + "[value=" + value +"]";
    }

    public String getExpressionText() {
        return value == null ? "null" : value.toString();
    }

    /**
      * Evaluate expression against given context.
      *
      * @param context evaluate expression against this context
      * @return current value of <code>value</code> property
      */
    public Object evaluate(JellyContext context) {
        return value;
    }

    /** Gets the constant value of this expression */
    public Object getValue() {
        return value;
    }

    /** Sets the constant value of this expression */
    public void setValue(Object value) {
        this.value = value;
    }
}
