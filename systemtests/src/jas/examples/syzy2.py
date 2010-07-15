#
# jython examples for jas.
# $Id: syzy2.py 1268 2007-07-29 11:05:03Z kredel $
#

from jas import Ring
from jas import Ideal

# ? example

r = Ring( "Rat(x,y,z) L" );
print "Ring: " + str(r);
print;

ps = """
( 
 ( z^3 - y ),
 ( y z - x ),
 ( y^3 - x^2 z ),
 ( x z^2 - y^2 )
) 
""";

f = Ideal( r, ps );
print "Ideal: " + str(f);
print;

rg = f.GB();
print "seq Output:", rg;
print;

from edu.jas.module import SyzygyAbstract;
from edu.jas.vector import ModuleList;
from edu.jas.module import ModGroebnerBaseAbstract;

s = SyzygyAbstract().zeroRelations( rg.list );
sl = ModuleList(rg.pset.ring,s);

print "syzygy:", sl;
print;

z = SyzygyAbstract().isZeroRelation( s, rg.list );

print "is zero s ?",
if z:
    print "true"
else:
    print "false"
print;

zg = sl;

for i in range(1,len(r.ring.vars)+1): 
   print "\n %s. resolution" % i;

   sl = zg;
   mg = ModGroebnerBaseAbstract().GB(sl);
   print "Mod GB: ", mg;
   print;

   zg = SyzygyAbstract().zeroRelations(mg);
   print "syzygies of Mod GB: ", zg;
   print;

   if ModGroebnerBaseAbstract().isGB( mg ):
       print "is GB";
   else:
       print "is not GB";

   if SyzygyAbstract().isZeroRelation(zg,mg):
       print "is Syzygy";
   else:
       print "is not Syzygy";

   if not zg.list:
       break;

