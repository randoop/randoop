#
# jython examples for jas.
# $Id: solvable.py 801 2006-03-20 21:10:02Z kredel $
#

from jas import SolvableRing
from jas import SolvableIdeal


# WA_32 solvable polynomial example

rs = """
# solvable polynomials, Weyl algebra A_3,2:
Rat(a,b,e1,e2,e3) G|3|
RelationTable
(
 ( e3 ), ( e1 ), ( e1 e3 - e1 ),
 ( e3 ), ( e2 ), ( e2 e3 - e2 )
)
""";

r = SolvableRing( rs );
print "SolvableRing: " + str(r);
print;


ps = """
(
 ( e1 e3^3 + e2^10 - a ),
 ( e1^3 e2^2 + e3 ),
 ( e3^3 + e3^2 - b )
)
""";

f = SolvableIdeal( r, ps );
print "SolvableIdeal: " + str(f);
print;


rg = f.leftGB();
print "seq left GB:", rg;
print;


rg = f.twosidedGB();
print "seq twosided GB:", rg;
print;


rg = f.rightGB();
print "seq right GB:", rg;
print;
