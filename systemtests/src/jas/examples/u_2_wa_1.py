#
# jython examples for jas.
# $Id: u_2_wa_1.py 1283 2007-07-29 15:09:03Z kredel $
#

from jas import SolvableRing
from jas import SolvableIdeal

from edu.jas.application import Ideal

# U_t, dim 2 and WA_1 example

rs1 = """
# solvable polynomials, U_t, dim_2 and Weyl algebra A_1:
Rat(D,X,a,b) G|2|
RelationTable
(
 ( b ), ( a ), ( a b + %s a )
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
# solvable polynomials, U_t, dim_2 and Weyl algebra A_1:
Rat(a,b,D,X) G|2|
RelationTable
(
 ( b ), ( a ), ( a b + %s a )
 ( X ), ( D ), ( D X + 1 )
)
""";

rs2c = """
# solvable polynomials, U_t, dim_2:
Rat(a,b) G
RelationTable
(
 ( b ), ( a ), ( a b + %s a )
)
""";


ps = """
(
 ( a - X^%s ),
 ( b - D X + %s )
)
""";


for t in (2,3,5,7,11,13,17,19,23,27,31,37,43):
  #for t in (5,7):
  r1  = SolvableRing( rs1 % t );
  r1c = SolvableRing( rs1c );
  #print "SolvableRing: " + str(r1);
  #print "SolvableRing: " + str(r1c);
  #print;
  it = SolvableIdeal( r1, ps % (t,t) );
  #print "SolvableIdeal: " + str(it);
  #print;
  # compute I_{\phi_t} \cap WA_1^opp
  x = it.leftGB();
  print "seq left x:", x;
  y = Ideal(x.pset).intersect(r1c.ring);
  len = y.list.size();
  print "seq left y: ", y;
  print "seq left y len: ", len;
  #print;
  #-------------------------------------
  r2 = SolvableRing( rs2 % t );
  r2c = SolvableRing( rs2c % t );
  #print "SolvableRing: " + str(r2);
  #print "SolvableRing: " + str(r2c);
  #print;
  ikt = SolvableIdeal( r2, ps % (t,t) );
  #print "SolvableIdeal: " + str(ikt);
  print;
  # compute ker(\phi_t)
  x = ikt.leftGB();
  print "seq left x:", x;
  y = Ideal(x.pset).intersect(r2c.ring);
  len = y.list.size();
  print "seq left y: ", y;
  print "seq left y len: ", len;
  #print;
  #-------------------------------------

