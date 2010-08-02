/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.commons.logging.simple;


import java.util.ArrayList;
import java.util.List;
import java.text.DateFormat;
import org.apache.commons.logging.impl.SimpleLog;


/**
 * <p>Decorated instance of SimpleLog to expose internal state and
 * support buffered output.</p>
 */

public class DecoratedSimpleLog extends SimpleLog {


    // ------------------------------------------------------------ Constructor


    public DecoratedSimpleLog(String name) {
        super(name);
    }


    // ------------------------------------------------------------- Properties

    public DateFormat getDateTimeFormatter() {
        return (dateFormatter);
    }


    public String getDateTimeFormat() {
        return (dateTimeFormat);
    }


    public String getLogName() {
        return (logName);
    }


    public boolean getShowDateTime() {
        return (showDateTime);
    }


    public boolean getShowShortName() {
        return (showShortName);
    }


    // ------------------------------------------------------- Protected Methods


    // Cache logged messages
    protected void log(int type, Object message, Throwable t) {

        super.log(type, message, t);
        cache.add(new LogRecord(type, message, t));

    }


    // ---------------------------------------------------------- Public Methods


    // Cache of logged records
    protected ArrayList cache = new ArrayList();


    // Clear cache
    public void clearCache() {
        cache.clear();
    }


    // Return cache
    public List getCache() {
        return (this.cache);
    }


}
