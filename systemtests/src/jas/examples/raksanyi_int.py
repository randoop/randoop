#
# jython examples for jas.
# $Id: raksanyi_int.py 2065 2008-08-15 19:06:59Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate

# Raksanyi & Walter example
# rational function coefficients

r = Ring( "IntFunc(a1, a2, a3, a4) (x1, x2, x3, x4) L" );
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

f = r.paramideal( ps );
print "Ideal: " + str(f);
print;

#startLog();

rg = f.GB();
rg = f.GB();
print "Ideal: " + str(rg);
print;

bg = rg.isGB();
if bg:
    print "isGB: true";
else:
    print "isGB: false";
print;

terminate();
#sys.exit();
