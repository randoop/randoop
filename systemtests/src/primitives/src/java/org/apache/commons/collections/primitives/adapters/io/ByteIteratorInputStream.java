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

import java.io.InputStream;

import org.apache.commons.collections.primitives.ByteIterator;

/**
 * Adapts an {@link ByteIterator} to the {@link InputStream} interface.
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/25 20:46:34 $
 * @author Rodney Waldhoff
 */
public class ByteIteratorInputStream extends InputStream {

    public ByteIteratorInputStream(ByteIterator in) {
        this.iterator= in;
    }

    public int read() {
        if(iterator.hasNext()) {
            return (0xFF & iterator.next());
        } else {
            return -1;
        }
    }

    public static InputStream adapt(ByteIterator in) {
        return null == in ? null : new ByteIteratorInputStream(in);
    }
    
    private ByteIterator iterator = null;

}
