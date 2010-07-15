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
package org.apache.commons.collections.primitives.adapters;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.collections.primitives.DoubleCollection;

/**
 * Adapts an {@link DoubleCollection DoubleCollection}
 * to the {@link java.util.Collection Collection}
 * interface.
 * <p />
 * This implementation delegates most methods
 * to the provided {@link DoubleCollection DoubleCollection} 
 * implementation in the "obvious" way.
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:20 $
 * @author Rodney Waldhoff 
 */
final public class DoubleCollectionCollection extends AbstractDoubleCollectionCollection implements Serializable {
    
    /**
     * Create a {@link Collection Collection} wrapping
     * the specified {@link DoubleCollection DoubleCollection}.  When
     * the given <i>collection</i> is <code>null</code>,
     * returns <code>null</code>.
     * 
     * @param collection the (possibly <code>null</code>) 
     *        {@link DoubleCollection DoubleCollection} to wrap
     * @return a {@link Collection Collection} wrapping the given 
     *         <i>collection</i>, or <code>null</code> when <i>collection</i> is
     *         <code>null</code>.
     */
    public static Collection wrap(DoubleCollection collection) {
        if(null == collection) {
            return null;
        } else if(collection instanceof Serializable) {
            return new DoubleCollectionCollection(collection);
        } else {
            return new NonSerializableDoubleCollectionCollection(collection);
        }
    }
    
    /**
     * Creates a {@link Collection Collection} wrapping
     * the specified {@link DoubleCollection DoubleCollection}.
     * @see #wrap
     */
    public DoubleCollectionCollection(DoubleCollection collection) {
        _collection = collection;
    }
    

    protected DoubleCollection getDoubleCollection() {
        return _collection;
    }
        
    private DoubleCollection _collection = null;
}
