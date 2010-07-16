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

import org.apache.commons.collections.primitives.BooleanCollection;

/**
 * Adapts an {@link org.apache.commons.collections.primitives.BooleanCollection
 * BooleanCollection} to the {@link java.util.Collection Collection} interface.
 * <p />
 * This implementation delegates most methods to the provided {@link
 * org.apache.commons.collections.primitives.BooleanCollection
 * BooleanCollection} implementation in the "obvious" way.
 * 
 * @since Commons Primitives 1.1
 * @version $Revision: 1.2 $ $Date: 2004/04/14 22:23:40 $
 */
final public class BooleanCollectionCollection extends
        AbstractBooleanCollectionCollection implements Serializable
{
    
    /**
     * Create a {@link java.util.Collection Collection} wrapping the specified
     * {@link org.apache.commons.collections.primitives.BooleanCollection
     * BooleanCollection}.  When the given <i>collection</i> is <code>null
     * </code>, returns <code>null</code>.
     * 
     * @param collection the (possibly <code>null</code>) 
     *        {@link org.apache.commons.collections.primitives.BooleanCollection
     *        BooleanCollection} to wrap
     * @return a {@link java.util.Collection Collection} wrapping the given
     *         <i>collection</i>, or <code>null</code> when <i>collection</i> is
     *         <code>null</code>.
     */
    public static Collection wrap(BooleanCollection collection) {
        if(null == collection) {
            return null;
        } else if(collection instanceof Serializable) {
            return new BooleanCollectionCollection(collection);
        } else {
            return new NonSerializableBooleanCollectionCollection(collection);
        }
    }
    
    /**
     * Creates a {@link java.util.Collection Collection} wrapping the specified
     * {@link org.apache.commons.collections.primitives.BooleanCollection
     * BooleanCollection}.
     * @see #wrap
     */
    public BooleanCollectionCollection(BooleanCollection collection) {
        _collection = collection;
    }
    

    protected BooleanCollection getBooleanCollection() {
        return _collection;
    }
        
    private BooleanCollection _collection = null;
}
