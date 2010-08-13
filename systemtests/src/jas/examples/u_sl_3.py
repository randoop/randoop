#
# jython examples for jas.
# $Id: u_sl_3.py 641 2006-02-19 11:13:27Z kredel $
#

from jas import SolvableRing
from jas import SolvableIdeal


# U(sl_3) example

rs = """
# solvable polynomials, U(sl_3):
Rat(Xa,Xb,Xc,Ya,Yb,Yc,Ha,Hb) G
RelationTable
(
 ( Xb ), ( Xa ), ( Xa Xb - Xc ),
 ( Ya ), ( Xa ), ( Xa Ya - Ha ),
 ( Yc ), ( Xa ), ( Xa Yc + Yb ),
 ( Ha ), ( Xa ), ( Xa Ha + 2 Xa ),
 ( Hb ), ( Xa ), ( Xa Hb - Xa),

 ( Yb ), ( Xb ), ( Xb Yb - Hb ),
 ( Yc ), ( Xb ), ( Xb Yc - Ya ),
 ( Ha ), ( Xb ), ( Xb Ha - Xb ),
 ( Hb ), ( Xb ), ( Xb Hb + 2 Xb ),

 ( Ya ), ( Xc ), ( Xc Ya + Xb ),
 ( Yb ), ( Xc ), ( Xc Yb - Xa ),
 ( Yc ), ( Xc ), ( Xc Yc - Ha - Hb ),
 ( Ha ), ( Xc ), ( Xc Ha + Xc ),
 ( Hb ), ( Xc ), ( Xc Hb + Xc ),

 ( Yb ), ( Ya ), ( Ya Yb + Yc ),
 ( Ha ), ( Ya ), ( Ya Ha - 2 Ya ),
 ( Hb ), ( Ya ), ( Ya Hb + Ya ),

 ( Ha ), ( Yb ), ( Yb Ha + Yb ),
 ( Hb ), ( Yb ), ( Yb Hb - 2 Yb ),

 ( Ha ), ( Yc ), ( Yc Ha - Yc ),
 ( Hb ), ( Yc ), ( Yc Hb - Yc )
 
)
""";

r = SolvableRing( rs );
print "SolvableRing: " + str(r);
print;


ps = """
(
 ( Xa + Hb ),
 ( Xb + Ha Hb )
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

