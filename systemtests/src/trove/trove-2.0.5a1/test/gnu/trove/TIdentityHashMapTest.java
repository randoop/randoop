///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001-2006, Eric D. Friedman All Rights Reserved.
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
 *
 * Created: Sat Aug 17 11:22:30 2002
 *
 * @author Eric Friedman
 * @version $Id: TIdentityHashMapTest.java,v 1.1 2006/11/10 23:28:00 robeden Exp $
 */

public class TIdentityHashMapTest extends THashMapTest  {
    
    public TIdentityHashMapTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        map = new THashMap(new TObjectIdentityHashingStrategy());
        count = 0;
    }

    public void testIdentityHash() throws Exception {
        Integer i1, i2;
        i1 = new Integer(1);
        i2 = new Integer(1);

        map.put(i1,i1);
        assertTrue(map.containsKey(i1));
        assertTrue(! map.containsKey(i2));
    }

    public void testBadlyWrittenKey() {
       ; // this test not applicable in identity hashing
    }
} // TIdentityHashMapTests
