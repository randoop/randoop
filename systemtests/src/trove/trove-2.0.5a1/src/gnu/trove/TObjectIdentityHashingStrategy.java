///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2002, Eric D. Friedman All Rights Reserved.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////

package gnu.trove;

/**
 * This object hashing strategy uses the System.identityHashCode
 * method to provide identity hash codes.  These are identical to the
 * value produced by Object.hashCode(), even when the type of the
 * object being hashed overrides that method.
 * 
 * Created: Sat Aug 17 11:13:15 2002
 *
 * @author Eric Friedman
 * @version $Id: TObjectIdentityHashingStrategy.java,v 1.4 2007/06/11 15:26:44 robeden Exp $
 */

public final class TObjectIdentityHashingStrategy<T> implements TObjectHashingStrategy<T> {
    /**
     * Delegates hash code computation to the System.identityHashCode(Object) method.
     *
     * @param object for which the hashcode is to be computed
     * @return the hashCode
     */
    public final int computeHashCode(T object) {
        return System.identityHashCode(object);
    }

    /**
     * Compares object references for equality.
     *
     * @param o1 an <code>Object</code> value
     * @param o2 an <code>Object</code> value
     * @return true if o1 == o2
     */
    public final boolean equals(T o1, T o2) {
        return o1 == o2;
    }
} // TObjectIdentityHashingStrategy
