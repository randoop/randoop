#
# jython examples for jas.
# $Id: nabeshima_cgbF2.py 1977 2008-08-03 10:40:23Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate


# Nabashima, ISSAC 2007, example F2
# integral function coefficients

r = Ring( "IntFunc(b, a) (x,y) G" );
print "Ring: " + str(r);
print;

ps = """
(
 ( { a } x^2 y^3 + { b } y + y ),
 ( x^2 y^2 + x y + 2 ),
 ( { a } x^2 + { b } y + 2 )
) 
""";

#startLog();

f = r.paramideal( ps );
print "ParamIdeal: " + str(f);
print;

gs = f.CGBsystem();
print "CGBsystem: " + str(gs);
print;

#sys.exit();

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
