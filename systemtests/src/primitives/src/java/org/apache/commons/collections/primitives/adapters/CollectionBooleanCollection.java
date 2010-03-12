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
 * Adapts a {@link Boolean Boolean}-valued {@link java.util.Collection
 * Collection} to the {@link
 * org.apache.commons.collections.primitives.BooleanCollection
 * BooleanCollection} interface.
 * <p/>
 * This implementation delegates most methods to the provided
 * {@link java.util.Collection Collection} implementation in the "obvious" way.
 * 
 * @since Commons Primitives 1.1
 * @version $Revision: 1.2 $ $Date: 2004/04/14 22:23:40 $
 */
final public class CollectionBooleanCollection
        extends AbstractCollectionBooleanCollection implements Serializable {
    /**
     * Create an {@link
     * org.apache.commons.collections.primitives.BooleanCollection
     * BooleanCollection} wrapping the specified {@link java.util.Collection
     * Collection}.  When the given <i>collection</i> is <code>null</code>,
     * returns <code>null</code>.
     * 
     * @param collection the (possibly <code>null</code>) {@link
     * java.util.Collection} to wrap
     * @return an {@link
     * org.apache.commons.collections.primitives.BooleanCollection
     * BooleanCollection} wrapping the given <i>collection</i>, or <code>null
     * </code> when <i>collection</i> is <code>null</code>.
     */
    public static BooleanCollection wrap(Collection collection) {
        if(null == collection) {
            return null;
        } else if(collection instanceof Serializable) {
            return new CollectionBooleanCollection(collection);
        } else {
            return new NonSerializableCollectionBooleanCollection(collection);
        }
    }

    /**
     * Creates an {@link
     * org.apache.commons.collections.primitives.BooleanCollection
     * BooleanCollection} wrapping the specified {@link java.util.Collection
     * Collection}.
     * @see #wrap
     */
    public CollectionBooleanCollection(Collection collection) {
        _collection = collection;
    }
    
    protected Collection getCollection() {
        return _collection;
    }
 
    private Collection _collection = null;         
}
