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

package org.apache.commons.jelly;

/** 
 * <p><code>JellyException</code> is the root of all Jelly exceptions.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.2 $
 */
public class MissingAttributeException extends JellyTagException {

    private String missingAttribute;
    
    // ### we may wish to implement a localized messag for this error
        
    public MissingAttributeException(String missingAttribute) {
        super( "You must define an attribute called '" + missingAttribute + "' for this tag." );
        this.missingAttribute = missingAttribute;
    }
    
    public String getMissingAttribute() {
        return missingAttribute;
    }
}
