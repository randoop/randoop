#
# jython examples for jas.
# $Id: nabeshima_cgbF7.py 1977 2008-08-03 10:40:23Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate


# Nabashima, ISSAC 2007, example F7
# integral function coefficients

r = Ring( "IntFunc(a, b) (y,x) L" );
print "Ring: " + str(r);
print;

ps = """
(
 ( x^3 - { a } ),
 ( y^4 - { b } ),
 ( x + y - { a } z )
) 
""";

#startLog();

f = r.paramideal( ps );
print "ParamIdeal: " + str(f);
print;

gs = f.CGBsystem();
print "CGBsystem: " + str(gs);
print;

bg = gs.isCGBsystem();
if bg:
    print "isCGBsystem: true";
else:
    print "isCGBsystem: false";
print;

#sys.exit();

gs = f.CGB();
print "CGB: " + str(gs);
print;

bg = gs.isCGB();
if bg:
    print "isCGB: true";
else:
    print "isCGB: false";
print;

terminate();
#------------------------------------------
#sys.exit();
