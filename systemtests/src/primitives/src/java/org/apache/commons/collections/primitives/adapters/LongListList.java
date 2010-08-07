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

import org.apache.commons.collections.primitives.LongList;

/**
 * Adapts an {@link LongList LongList} to the
 * {@link List List} interface.
 * <p />
 * This implementation delegates most methods
 * to the provided {@link LongList LongList} 
 * implementation in the "obvious" way.
 *
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:21 $
 * @author Rodney Waldhoff 
 */
final public class LongListList extends AbstractLongListList implements Serializable {
    
    /**
     * Create a {@link List List} wrapping
     * the specified {@link LongList LongList}.  When
     * the given <i>list</i> is <code>null</code>,
     * returns <code>null</code>.
     * 
     * @param list the (possibly <code>null</code>) 
     *        {@link LongList LongList} to wrap
     * @return a {@link List List} wrapping the given 
     *         <i>list</i>, or <code>null</code> when <i>list</i> is
     *         <code>null</code>.
     */
    public static List wrap(LongList list) {
        if(null == list) {
            return null;
        } else if(list instanceof Serializable) {
            return new LongListList(list);
        } else {
            return new NonSerializableLongListList(list);
        }
    }
    
    /**
     * Creates a {@link List List} wrapping
     * the specified {@link LongList LongList}.
     * @see #wrap
     */
    public LongListList(LongList list) {
        _list = list;
    }
    
    protected LongList getLongList() {
        return _list;
    }    
    
    private LongList _list = null;
}
