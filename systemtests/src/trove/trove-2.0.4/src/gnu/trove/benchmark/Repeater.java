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

package gnu.trove.benchmark;

class Repeater implements Timer {
    int _count;
    Operation _operation;

    Repeater(Operation o) {
        _count = o.getIterationCount();
        _operation = o;
    }

    public Result run() {
        long theirs = theirs();
        long ours = ours();
        Result r = new Result();
        r.setTheirs(theirs);
        r.setOurs(ours);
        r.setIterations(_count);
        r.setDescription(_operation.toString());
        return r;
    }

    public long theirs() {
        long then = System.currentTimeMillis();
        for (int i = 0; i < _count; i++) {
            _operation.theirs();
        }
        long now = System.currentTimeMillis();
        return (now -then);
    }

    public long ours() {
        long then = System.currentTimeMillis();
        for (int i = 0; i < _count; i++) {
            _operation.ours();
        }
        long now = System.currentTimeMillis();
        return (now -then);
    }

    public Operation getOperation() {
        return _operation;
    }
}
