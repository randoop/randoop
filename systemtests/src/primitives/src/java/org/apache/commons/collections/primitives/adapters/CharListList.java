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

import org.apache.commons.collections.primitives.CharList;

/**
 * Adapts an {@link CharList CharList} to the
 * {@link List List} interface.
 * <p />
 * This implementation delegates most methods
 * to the provided {@link CharList CharList} 
 * implementation in the "obvious" way.
 *
 * @since Commons Primitives 1.0
 * @version $Revision: 1.4 $ $Date: 2004/02/25 20:46:20 $
 * @author Rodney Waldhoff 
 */
final public class CharListList extends AbstractCharListList implements Serializable {
    
    /**
     * Create a {@link List List} wrapping
     * the specified {@link CharList CharList}.  When
     * the given <i>list</i> is <code>null</code>,
     * returns <code>null</code>.
     * 
     * @param list the (possibly <code>null</code>) 
     *        {@link CharList CharList} to wrap
     * @return a {@link List List} wrapping the given 
     *         <i>list</i>, or <code>null</code> when <i>list</i> is
     *         <code>null</code>.
     */
    public static List wrap(CharList list) {
        if(null == list) {
            return null;
        } else if(list instanceof Serializable) {
            return new CharListList(list);
        } else {
            return new NonSerializableCharListList(list);
        }
    }
    
    /**
     * Creates a {@link List List} wrapping
     * the specified {@link CharList CharList}.
     * @see #wrap
     */
    public CharListList(CharList list) {
        _list = list;
    }
    
    protected CharList getCharList() {
        return _list;
    }    
    
    private CharList _list = null;
}
