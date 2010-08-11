#
# jython examples for jas.
# $Id: syz.py 1268 2007-07-29 11:05:03Z kredel $
#

from jas import Ring
from jas import Ideal

# trinks 7 example

r = Ring( "Rat(B,S,T,Z,P,W) L" );
print "Ring: " + str(r);
print;

ps = """
( 
 ( 45 P + 35 S - 165 B - 36 ), 
 ( 35 P + 40 Z + 25 T - 27 S ), 
 ( 15 W + 25 S P + 30 Z - 18 T - 165 B**2 ), 
 ( - 9 W + 15 T P + 20 S Z ), 
 ( P W + 2 T Z - 11 B**3 ), 
 ( 99 W - 11 B S + 3 B**2 ),
 ( B**2 + 33/50 B + 2673/10000 )
) 
""";

f = Ideal( r, ps );
print "Ideal: " + str(f);
print;


#Katsura equations for N = 3:

#r = Ring( "Rat(u0,u1,u2,u3) L" );
#print "Ring: " + str(r);
#print;

ps = """
(
 u3*u3 + u2*u2 + u1*u1 + u0*u0 + u1*u1 + u2*u2 + u3*u3 - u0,
 u3*0 + u2*u3 + u1*u2 + u0*u1 + u1*u0 + u2*u1 + u3*u2 - u1,
 u3*0 + u2*0 + u1*u3 + u0*u2 + u1*u1 + u2*u0 + u3*u1 - u2,
 u3 + u2 + u1 + u0 + u1 + u2 + u3 - 1
)
""";

#f = Ideal( r, ps );
#print "Ideal: " + str(f);
#print;

rg = f.GB();
print "seq Output:", rg;
print;

from edu.jas.module import SyzygyAbstract;
from edu.jas.module import ModGroebnerBaseAbstract;
from edu.jas.vector import ModuleList;

s = SyzygyAbstract().zeroRelations( rg.list );
sl = ModuleList(rg.pset.ring,s);

print "syzygy:", sl;
print;

z = SyzygyAbstract().isZeroRelation( s, rg.list );

print "is Syzygy ?",
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

   if not zg:
       break;

