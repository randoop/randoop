#
# jython examples for jas.
# $Id: raksanyi.py 1924 2008-07-20 11:33:50Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal

# Raksanyi & Walter example
# rational function coefficients

r = Ring( "RatFunc(a1, a2, a3, a4) (x1, x2, x3, x4) G" );
print "Ring: " + str(r);
print;

ps = """
(
 ( x4 - { a4 - a2 } ),
 ( x1 + x2 + x3 + x4 - { a1 + a3 + a4 } ),
 ( x1 x3 + x1 x4 + x2 x3 + x3 x4 - { a1 a4 + a1 a3 + a3 a4 } ),
 ( x1 x3 x4 - { a1 a3 a4 } )
) 
""";

f = r.ideal( ps );
print "Ideal: " + str(f);
print;

rg = f.GB();
rg = f.GB();
print "GB:", rg;
print;

from edu.jas.kern import ComputerThreads;
ComputerThreads.terminate();
#sys.exit();
