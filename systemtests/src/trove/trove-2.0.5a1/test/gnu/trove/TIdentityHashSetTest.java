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
 * Created: Sat Aug 17 12:13:03 2002
 *
 * @author Eric Friedman
 * @version $Id: TIdentityHashSetTest.java,v 1.1 2006/11/10 23:28:00 robeden Exp $
 */

public class TIdentityHashSetTest extends THashSetTest {
    
    public TIdentityHashSetTest(String name) {
        super(name);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        s = new THashSet(new TObjectIdentityHashingStrategy());
    }

    public void testSomeBadlyWrittenObject() {
      ; // this test doesn't apply to identity sets
    }
} // TIdentityHashSetTests
