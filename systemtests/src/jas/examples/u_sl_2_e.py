#
# jython examples for jas.

from jas import SolvableRing
from jas import SolvableIdeal


# U(sl_2_e) example

rs = """
# solvable polynomials, U(sl_2_e):
Rat(e,h) G
RelationTable
(
 ( h ), ( e ), ( e h + 2 e )
)
""";

r = SolvableRing( rs );
print "SolvableRing: " + str(r);
print;


ps = """
(
 ( e^2 + h^3 )
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

