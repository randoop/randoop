#
# jython examples for jas.
# $Id: syzy3.py 1268 2007-07-29 11:05:03Z kredel $
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

from edu.jas.module import SyzygyAbstract;
from edu.jas.vector import ModuleList;
from edu.jas.module import ModGroebnerBaseAbstract;

R = SyzygyAbstract().resolution( f.pset );

for i in range(0,R.size()): 
   print "\n %s. resolution" % (i+1);
   print "\n ", R[i];

