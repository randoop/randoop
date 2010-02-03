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

import org.apache.commons.collections.primitives.FloatCollection;

/**
 * Adapts a {@link java.lang.Number Number}-valued
 * {@link java.util.Collection Collection} to the
 * {@link FloatCollection FloatCollection} interface.
 * <p />
 * This implementation delegates most methods
 * to the provided {@link Collection Collection} 
 * implementation in the "obvious" way.
 * 
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
final public class CollectionFloatCollection extends AbstractCollectionFloatCollection implements Serializable {
    /**
     * Create an {@link FloatCollection FloatCollection} wrapping
     * the specified {@link Collection Collection}.  When
     * the given <i>collection</i> is <code>null</code>,
     * returns <code>null</code>.
     * 
     * @param collection the (possibly <code>null</code>) {@link Collection} to wrap
     * @return an {@link FloatCollection FloatCollection} wrapping the given 
     *         <i>collection</i>, or <code>null</code> when <i>collection</i> is
     *         <code>null</code>.
     */
    public static FloatCollection wrap(Collection collection) {
        if(null == collection) {
            return null;
        } else if(collection instanceof Serializable) {
            return new CollectionFloatCollection(collection);
        } else {
            return new NonSerializableCollectionFloatCollection(collection);
        }
    }

    /**
     * Creates an {@link FloatCollection FloatCollection} wrapping
     * the specified {@link Collection Collection}.
     * @see #wrap
     */
    public CollectionFloatCollection(Collection collection) {
        _collection = collection;
    }
    
    protected Collection getCollection() {
        return _collection;
    }
 
    private Collection _collection = null;         
}
