#
# jython examples for jas.
# $Id: chebyshev.py 2106 2008-09-06 11:13:23Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate

# chebyshev polynomial example
# T(0) = 1
# T(1) = x
# T(n) = 2 * x * T(n-1) - T(n-2)

r = Ring( "Z(x) L" );
print "Ring: " + str(r);
print;

# sage like: with generators for the polynomial ring
[x] = r.gens();

one = r.one();
x2 = 2 * x;

N = 10;
T = [one,x];
for n in range(2,N):
    t = x2 * T[n-1] - T[n-2];
    T.append( t );

for n in range(0,N):
    print "T[%s] = %s" % (n,T[n]);

print;

#sys.exit();
