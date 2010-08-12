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
import java.util.List;

import org.apache.commons.collections.primitives.DoubleList;

/**
 * Adapts an {@link DoubleList DoubleList} to the
 * {@link List List} interface.
 * <p />
 * This implementation delegates most methods
 * to the provided {@link DoubleList DoubleList} 
 * implementation in the "obvious" way.
 *
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
final public class DoubleListList extends AbstractDoubleListList implements Serializable {
    
    /**
     * Create a {@link List List} wrapping
     * the specified {@link DoubleList DoubleList}.  When
     * the given <i>list</i> is <code>null</code>,
     * returns <code>null</code>.
     * 
     * @param list the (possibly <code>null</code>) 
     *        {@link DoubleList DoubleList} to wrap
     * @return a {@link List List} wrapping the given 
     *         <i>list</i>, or <code>null</code> when <i>list</i> is
     *         <code>null</code>.
     */
    public static List wrap(DoubleList list) {
        if(null == list) {
            return null;
        } else if(list instanceof Serializable) {
            return new DoubleListList(list);
        } else {
            return new NonSerializableDoubleListList(list);
        }
    }
    
    /**
     * Creates a {@link List List} wrapping
     * the specified {@link DoubleList DoubleList}.
     * @see #wrap
     */
    public DoubleListList(DoubleList list) {
        _list = list;
    }
    
    protected DoubleList getDoubleList() {
        return _list;
    }    
    
    private DoubleList _list = null;
}
