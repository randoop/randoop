#
# jython examples for jas.
# $Id: logic.py 1770 2008-04-28 20:51:09Z kredel $
#

from jas import Ring
from jas import Ideal

# logic example from Kreutzer JdM 2008

r = Ring( "Mod 2 (a,f,p,u) G" );
print "Ring: " + str(r);
print;

ks = """
(
 ( a^2 - a ),
 ( f^2 - f ),
 ( p^2 - p ),
 ( u^2 - u )
)
""";

ps = """
(
 ( p f + p ),
 ( p u + p + u + 1 ),
 ( a + u + 1 ),
 ( a + p + 1 )
)
""";


k = r.ideal( ks );
p = r.ideal( ps );

f = k.sum( p );

print "Ideal: " + str(f);
print;

rg = f.GB();
print "Output:", rg;
print;


