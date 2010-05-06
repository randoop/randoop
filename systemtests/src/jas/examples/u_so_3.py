#
# jython examples for jas.
# $Id: u_so_3.py 802 2006-03-20 21:11:37Z kredel $
#

from jas import SolvableRing
from jas import SolvableIdeal


# U(so_3) example

rs = """
# solvable polynomials, U(so_3):
Rat(x,y,z) G
RelationTable
(
 ( y ), ( x ), ( x y - z ),
 ( z ), ( x ), ( x z + y ),
 ( z ), ( y ), ( y z - x ) 
)
""";

r = SolvableRing( rs );
print "SolvableRing: " + str(r);
print;


ps = """
(
 ( x^2 + y^3 )
)
""";

f = SolvableIdeal( r, ps );
print "SolvableIdeal: " + str(f);
print;


rg = f.leftGB();
print "seq left GB:", rg;
print;

from edu.jas.ring   import SolvableGroebnerBaseSeq;

if SolvableGroebnerBaseSeq().isLeftGB( rg.list ):
   print "is left GB";
else:
   print "is not left GB";



rg = f.twosidedGB();
print "seq twosided GB:", rg;
print;

if SolvableGroebnerBaseSeq().isLeftGB( rg.list ):
   print "twosided GB is left GB";
else:
   print "twosided GB is not left GB";

if SolvableGroebnerBaseSeq().isRightGB( rg.list ):
   print "twosided GB is right GB";
else:
   print "twosided GB is not right GB";

if SolvableGroebnerBaseSeq().isTwosidedGB( rg.list ):
   print "is twosided GB";
else:
   print "is not twosided GB";



rg = f.rightGB();
print "seq right GB:", rg;
print;

if SolvableGroebnerBaseSeq().isRightGB( rg.list ):
   print "is right GB";
else:
   print "is not right GB";

