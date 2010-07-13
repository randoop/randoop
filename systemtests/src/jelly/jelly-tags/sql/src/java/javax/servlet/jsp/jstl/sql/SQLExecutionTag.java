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

package javax.servlet.jsp.jstl.sql;

/**
 * <p>This interface allows tag handlers implementing it to receive
 * values for parameter markers ("?") in their SQL statements.</p>
 *
 * <p>This interface is implemented by both &lt;sql:query&gt; and
 * &lt;sql:update&gt;. Its <code>addSQLParameter()</code> method
 * is called by nested parameter actions (such as &lt;sql:param&gt;)
 * to substitue <code>PreparedStatement<code> parameter values for
 * "?" parameter markers in the SQL statement of the enclosing
 * <code>SQLExecutionTag</code> action.</p>
 *
 * <p>The given parameter values are converted to their corresponding
 * SQL type (following the rules in the JDBC specification) before
 * they are sent to the database.</p>
 *
 * <p>Keeing track of the index of the parameter values being added
 * is the responsibility of the tag handler implementing this
 * interface</p>
 *
 * <p>The <code>SQLExcecutionTag</code> interface is exposed in order
 * to support custom parameter actions which may retrieve their
 * parameters from any source and process them before substituting
 * them for a parameter marker in the sQL statement of the
 * enclosing <code>SQLExecutionTag</code> action</p>
 *
 * @author Justyna Horwat
 */
public interface SQLExecutionTag {

    /**
     * Adds a PreparedStatement parameter value
     *
     * @param the PreparedStatement parameter value
     */
    public void addSQLParameter(Object value);
}
