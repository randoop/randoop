#
# jython examples for jas.
# $Id: wa_32.py 802 2006-03-20 21:11:37Z kredel $
#

from jas import SolvableRing
from jas import SolvableIdeal


# WA_32 example

rs = """
# solvable polynomials, Weyl algebra A_3,2:
Rat(a,b,e1,e2,e3) L
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

from edu.jas.ring   import SolvableGroebnerBaseSeq;

if SolvableGroebnerBaseSeq().isLeftGB( rg.list ):
   print "is left GB";
else:
   print "is not left GB";


rg = f.parLeftGB(2); # 2 threads
print "par left GB:", rg;
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


rg = f.parTwosidedGB(2);
print "par twosided GB:", rg;
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
