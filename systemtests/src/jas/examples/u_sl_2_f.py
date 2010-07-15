#
# jython examples for jas.

from jas import SolvableRing
from jas import SolvableIdeal


# U(sl_2_f) example

rs = """
# solvable polynomials, U(sl_2_f):
Rat(f,h) G
RelationTable
(
 ( h ), ( f ), ( f h - 2 f ) 
)
""";

r = SolvableRing( rs );
print "SolvableRing: " + str(r);
print;


ps = """
(
 ( h^2 + f^3 )
)
""";

f = SolvableIdeal( r, ps );
print "SolvableIdeal: " + str(f);
print;


rg = f.leftGB();
print "seq left Output:", rg;
print;


rg = f.twosidedGB();
print "seq twosided Output:", rg;
print;

