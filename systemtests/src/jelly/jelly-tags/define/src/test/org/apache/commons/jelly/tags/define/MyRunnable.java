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
package org.apache.commons.jelly.tags.define;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import org.apache.tools.ant.types.FileSet;

/**
 * An example Runnable bean that is framework neutral and just performs
 * some useful function.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.4 $
 */
public class MyRunnable implements Runnable {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(MyRunnable.class);

    private int x;
    private String y;
    private List fileSets = new ArrayList();

    public MyRunnable() {
    }


    // Adder methods
    //-------------------------------------------------------------------------
    /*

    Commented out method to remove test-only dependency on ant

    public void addFileset(FileSet fileSet) {
        fileSets.add(fileSet);
    }
    */

    // Runnable interface
    //-------------------------------------------------------------------------
    public void run() {
        log.info( "About to do something where x = " + getX() + " y = " + getY() );
        log.info( "FileSets are: " + fileSets );
    }


    // Properties
    //-------------------------------------------------------------------------
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
