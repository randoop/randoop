#
# jython examples for jas.
# $Id: syzsolv.py 641 2006-02-19 11:13:27Z kredel $
#

from jas import SolvableRing
from jas import SolvableIdeal

# ? example

rs = """
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
print "SolvIdeal: " + str(f);
print;

from edu.jas.module import SolvableSyzygyAbstract;

R = SolvableSyzygyAbstract().resolution( f.pset );

for i in range(0,R.size()): 
   print "\n %s. resolution" % (i+1);
   print "\n", R[i];

