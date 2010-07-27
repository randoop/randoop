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
package org.apache.commons.collections.primitives.adapters.io;

import java.io.Reader;

import org.apache.commons.collections.primitives.CharIterator;

/**
 * Adapts a {@link CharIterator} to the {@link Reader} interface.
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:34 $
 * @author Rodney Waldhoff
 */
public class CharIteratorReader extends Reader {

    public CharIteratorReader(CharIterator in) {
        this.iterator= in;
    }

    public int read(char[] buf, int off, int len) {
        if(iterator.hasNext()) {
            int count = 0;
            while(iterator.hasNext() && count < len) {
                buf[off + count] = iterator.next();
                count++;
            }
            return count;
        } else {
            return -1;
        }
    }
    
    public void close() {        
    }

    public static Reader adapt(CharIterator in) {
        return null == in ? null : new CharIteratorReader(in);
    }
    
    private CharIterator iterator = null;

}
