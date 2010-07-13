#
# jython examples for jas.
# $Id: u_sl_2.py 641 2006-02-19 11:13:27Z kredel $
#

from jas import SolvableRing
from jas import SolvableIdeal


# U(sl_2) example

rs = """
# solvable polynomials, U(sl_2):
Rat(e,f,h) G
RelationTable
(
 ( f ), ( e ), ( e f - h ),
 ( h ), ( e ), ( e h + 2 e ),
 ( h ), ( f ), ( f h - 2 f ) 
)
""";

r = SolvableRing( rs );
print "SolvableRing: " + str(r);
print;


ps = """
(
 ( e^2 + f^3 )
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


#rg = f.rightGB();
#print "seq right GB:", rg;
#print;

