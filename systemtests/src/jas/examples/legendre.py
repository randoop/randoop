#
# jython examples for jas.
# $Id: legendre.py 2112 2008-09-06 19:53:48Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate

from edu.jas.arith import BigRational

# Legendre polynomial example
# P(0) = 1
# P(1) = x
# P(n) = 1/n [ (2n-1) * x * P(n-1) - (n-1) * P(n-2) ]

r = Ring( "Q(x) L" );
#r = Ring( "C(x) L" );
print "Ring: " + str(r);
print;

# sage like: with generators for the polynomial ring
[x] = r.gens();

one = r.one();

N = 10;
P = [one,x];
for n in range(2,N):
    p = (2*n-1) * x * P[n-1] - (n-1) * P[n-2];
    r = (1,n); # no rational numbers in python
    #r = [(1,n)]; # no complex rational numbers in python
    #r = ((1,n),(0,1)); # no complex rational numbers in python
    p = r * p; 
    P.append( p );

for n in range(0,N):
    print "P[%s] = %s" % (n,P[n]);

print;

#sys.exit();
