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
package org.apache.commons.jelly.core;

/**
 * A sample bean that throws exceptions when its methods are invoked.
 */
public class ExceptionBean {

    public ExceptionBean() {
    }

    public void instanceMethod( String msg) throws Exception {
        throw new Exception( msg );
    }

    public static void staticMethod( String msg) throws Exception {
        throw new Exception( msg );
    }

}
