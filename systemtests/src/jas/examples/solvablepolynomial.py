#
# jython examples for jas.
# $Id: solvablepolynomial.py 2224 2008-11-16 19:30:06Z kredel $
#

from java.lang import System
from java.lang import Integer

from jas import SolvableRing
from jas import SolvableIdeal


# WA_32 solvable polynomial example

rs = """
# solvable polynomials, Weyl algebra A_3,2:
#Rat(a,b,e1,e2,e3) G|3|
Quat(a,b,e1,e2,e3) G|3|
RelationTable
(
 ( e3 ), ( e1 ), ( e1 e3 - e1 ),
 ( e3 ), ( e2 ), ( e2 e3 - e2 )
)
""";

r = SolvableRing( rs );
print "SolvableRing: " + str(r);
print;

[a,b,e1,e2,e3] = r.gens();
print "gens =", [ str(f) for f in r.gens() ];

f1 = e1 * e3**3 + e2**10 - a;
f2 = e1**3 * e2**2 + e3;
f3 = e3**3 + e3**2 - b;

f4 = ( e3**2 * e2**3 + e1 )**3;

#print "f1 = ", f1;
#print "f2 = ", f2;
#print "f3 = ", f3;
print "f4 = ", f4;

F = [ f1, f2, f3 ];
print "F =", [ str(f) for f in F ];
print

I = r.ideal( list=F );
print "SolvableIdeal: " + str(I);
print;

rg = I.leftGB();
print "seq left GB:", rg;
print "isLeftGB: ", rg.isLeftGB();
print;

rg = I.twosidedGB();
print "seq twosided GB:", rg;
print "isTwosidedGB: ", rg.isTwosidedGB();
print;

rg = I.rightGB();
print "seq right GB:", rg;
print "isRightGB: ", rg.isRightGB();
print;
