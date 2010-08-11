#
# jython examples for jas.
# $Id: u_sl_2_wa_1.py 1283 2007-07-29 15:09:03Z kredel $
#

from jas import SolvableRing
from jas import SolvableIdeal

from edu.jas.application import Ideal


# U_sl_2 and WA_1 example

rs1 = """
# solvable polynomials, U_sl_2 and Weyl algebra A_1:
Rat(D,X,e,f,h) G|3|
RelationTable
(
 ( f ), ( e ), ( e f - h ),
 ( h ), ( e ), ( e h + 2 e ),
 ( h ), ( f ), ( f h - 2 f ), 
 ( X ), ( D ), ( D X + 1 )
)
""";

rs1c = """
# solvable polynomials, Weyl algebra A_1:
Rat(D,X) G
RelationTable
(
 ( X ), ( D ), ( D X + 1 )
)
""";


rs2 = """
# solvable polynomials, U_sl_2 and Weyl algebra A_1:
Rat(e,f,h,D,X) G|2|
RelationTable
(
 ( f ), ( e ), ( e f - h ),
 ( h ), ( e ), ( e h + 2 e ),
 ( h ), ( f ), ( f h - 2 f ), 
 ( X ), ( D ), ( D X + 1 )
)
""";

rs2c = """
# solvable polynomials, U_sl_2:
Rat(e,f,h) G
RelationTable
(
 ( f ), ( e ), ( e f - h ),
 ( h ), ( e ), ( e h + 2 e ),
 ( h ), ( f ), ( f h - 2 f )
)
""";


ps = """
(
 ( e - X ),
 ( f + D^2 X ),
 ( h - 2 D X )
)
""";


r1  = SolvableRing( rs1 );
r1c = SolvableRing( rs1c );
#print "SolvableRing: " + str(r1);
#print "SolvableRing: " + str(r1c);
print;
it = SolvableIdeal( r1, ps );
print "SolvableIdeal: " + str(it);
print;
# compute I_{\phi_t} \cap WA_1^opp
x = it.leftGB();
print "seq left x:", x;
y = Ideal(x.pset).intersect(r1c.ring);
len = y.list.size();
print "seq left y: ", y;
print "seq left y len: ", len;
print;
#-------------------------------------
r2 = SolvableRing( rs2 );
r2c = SolvableRing( rs2c );
#print "SolvableRing: " + str(r2);
#print "SolvableRing: " + str(r2c);
print;
ikt = SolvableIdeal( r2, ps );
print "SolvableIdeal: " + str(ikt);
print;
# compute ker(\phi_t)
x = ikt.leftGB();
print "seq left x:", x;
y = Ideal(x.pset).intersect(r2c.ring);
len = y.list.size();
print "seq left y: ", y;
print "seq left y len: ", len;
print;
#-------------------------------------

