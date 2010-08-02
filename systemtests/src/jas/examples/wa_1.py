#
# jython examples for jas.
# $Id: wa_1.py 641 2006-02-19 11:13:27Z kredel $
#

from jas import SolvableRing
from jas import SolvableIdeal


# WA_1 example

rs = """
# solvable polynomials, Weyl algebra A_1:
Rat(p,t,x,d) G
RelationTable
(
 ( d ), ( x ), ( x d + 1 )
)
""";

r = SolvableRing( rs );
print "SolvableRing: " + str(r);
print;


ps = """
(
 ( x^7 ),
 ( x d + 7 )
)
""";

i7 = SolvableIdeal( r, ps );
print "SolvableIdeal: " + str(i7);
print;

i7rg = i7.leftGB();
print "seq left i7 Output:", i7rg;
print;


ps = """
(
 ( d^7 ),
 ( x d - 7 + 1 )
)
""";

j7 = SolvableIdeal( r, ps );
print "SolvableIdeal: " + str(j7);
print;

j7rg = j7.leftGB();
print "seq left i7 Output:", j7rg;
print;

