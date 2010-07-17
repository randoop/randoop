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
 
 
package org.apache.commons.logging;

import junit.framework.*;


/**
  *
  * @author Sean C. Sullivan
  * @version $Revision:  $
  * 
  */
public abstract class AbstractLogTest extends TestCase {

    public AbstractLogTest(String testName) {
        super(testName);
    }

    
    public abstract Log getLogObject();

	public void testLoggingWithNullParameters()
	{
		Log log = this.getLogObject();
		
		assertNotNull(log);
		

		log.debug(null);
		
		log.debug(null, null);
		
		log.debug(log.getClass().getName() + ": debug statement");
		
		log.debug(log.getClass().getName() + ": debug statement w/ null exception", new RuntimeException());
		

		log.error(null);
		
		log.error(null, null);
		
		log.error(log.getClass().getName() + ": error statement");
		
		log.error(log.getClass().getName() + ": error statement w/ null exception", new RuntimeException());
		

		log.fatal(null);
		
		log.fatal(null, null);
		
		log.fatal(log.getClass().getName() + ": fatal statement");
		
		log.fatal(log.getClass().getName() + ": fatal statement w/ null exception", new RuntimeException());
		

		log.info(null);
		
		log.info(null, null);
		
		log.info(log.getClass().getName() + ": info statement");
		
		log.info(log.getClass().getName() + ": info statement w/ null exception", new RuntimeException());
		

		log.trace(null);
		
		log.trace(null, null);
		
		log.trace(log.getClass().getName() + ": trace statement");
		
		log.trace(log.getClass().getName() + ": trace statement w/ null exception", new RuntimeException());
		

		log.warn(null);
		
		log.warn(null, null);
		
		log.warn(log.getClass().getName() + ": warn statement");
		
		log.warn(log.getClass().getName() + ": warn statement w/ null exception", new RuntimeException());
	}    
}
