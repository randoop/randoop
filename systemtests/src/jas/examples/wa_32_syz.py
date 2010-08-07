#
# jython examples for jas.
# $Id: wa_32_syz.py 1268 2007-07-29 11:05:03Z kredel $
#

from jas import SolvableRing
from jas import SolvableIdeal

from edu.jas.ring   import SolvableGroebnerBaseSeq;
from edu.jas.poly   import OrderedPolynomialList;
from edu.jas.vector import ModuleList;
from edu.jas.module import SolvableSyzygyAbstract;

# WA_32 example

rs = """
# solvable polynomials, Weyl algebra A_3,2:
Rat(a,b,e1,e2,e3) G3|
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


Z = SolvableSyzygyAbstract().leftZeroRelationsArbitrary( f.list );
#Z = SolvableSyzygyAbstract().leftZeroRelations( g );
Zp = ModuleList( r.ring, Z );
print "seq left syz Output:", Zp;
print;
if SolvableSyzygyAbstract().isLeftZeroRelation( Zp.list, f.list ):
   print "is left syzygy";
else:
   print "is not left syzygy";


Zr = SolvableSyzygyAbstract().rightZeroRelationsArbitrary( f.list );
#Z = SolvableSyzygyAbstract().rightZeroRelations( g );
Zpr = ModuleList( r.ring, Zr );
print "seq right syz Output:", Zpr;
print;
if SolvableSyzygyAbstract().isRightZeroRelation( Zpr.list, f.list ):
   print "is right syzygy";
else:
   print "is not right syzygy";




rg = f.leftGB();
print "seq left Output:", rg;
print;
if SolvableGroebnerBaseSeq().isLeftGB( rg.list ):
   print "is left GB";
else:
   print "is not left GB";
g = rg.list;


rg = f.twosidedGB();
print "seq twosided Output:", rg;
print;
if SolvableGroebnerBaseSeq().isTwosidedGB( rg.list ):
   print "is twosided GB";
else:
   print "is not twosided GB";


rgb = SolvableGroebnerBaseSeq().rightGB( f.list );
rp = OrderedPolynomialList( r.ring, rgb );
print "seq right Output:", rp;
print;
if SolvableGroebnerBaseSeq().isRightGB( rgb ):
   print "is right GB";
else:
   print "is not right GB";

