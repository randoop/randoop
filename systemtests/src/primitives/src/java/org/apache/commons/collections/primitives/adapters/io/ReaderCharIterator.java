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

import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;

import org.apache.commons.collections.primitives.CharIterator;

/**
 * Adapts a {@link Reader} to the {@link CharIterator} interface.
 * 
 * @version $Revision: 1.3 $ $Date: 2004/02/25 20:46:34 $
 * @author Rodney Waldhoff
 */
public class ReaderCharIterator implements CharIterator {

    public ReaderCharIterator(Reader in) {
        this.reader = in;
    }

    public static CharIterator adapt(Reader in) {
        return null == in ? null : new ReaderCharIterator(in);
    }
    
    public boolean hasNext() {
        ensureNextAvailable();
        return (-1 != next);
    }

    public char next() {
        if(!hasNext()) {
            throw new NoSuchElementException("No next element");
        } else {
            nextAvailable = false;
            return (char)next;
        }
    }
    
    /**
     * Not supported.
     * @throws UnsupportedOperationException
     */
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("remove() is not supported here");
    }

    private void ensureNextAvailable() {
        if(!nextAvailable) {
            readNext();
        }
    }

    private void readNext() {
        try {
            next = reader.read();
            nextAvailable = true;
        } catch(IOException e) {
            // TODO: Use a tunnelled exception instead? 
            // See http://radio.weblogs.com/0122027/2003/04/01.html#a7, for example            
            throw new RuntimeException(e.toString());
        }
    }
    
    private Reader reader = null;
    private boolean nextAvailable = false;
    private int next;

}
