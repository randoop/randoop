///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
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

//////////////////////////////////////////////////
// THIS IS A GENERATED CLASS. DO NOT HAND EDIT! //
//////////////////////////////////////////////////


/**
 * Iterator for long collections.
 *
 * @author Eric D. Friedman
 * @version $Id: PIterator.template,v 1.1 2006/11/10 23:28:00 robeden Exp $
 */

public class TLongIterator extends TPrimitiveIterator {
    /** the collection on which the iterator operates */
    private final TLongHash _hash;

    /**
     * Creates a TLongIterator for the elements in the specified collection.
     */
    public TLongIterator(TLongHash hash) {
	super(hash);
	this._hash = hash;
    }

    /**
     * Advances the iterator to the next element in the underlying collection
     * and returns it.
     *
     * @return the next long in the collection
     * @exception NoSuchElementException if the iterator is already exhausted
     */
    public long next() {
	moveToNextIndex();
	return _hash._set[_index];
    }
}// TLongIterator
