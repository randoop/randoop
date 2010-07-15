#
# jython examples for jas.
# $Id: nabeshima_cgbF4.py 2171 2008-10-03 21:50:35Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate


# Nabashima, ISSAC 2007, example F4
# integral function coefficients

r = Ring( "IntFunc(a, b, c, d) (y, x) L" );
print "Ring: " + str(r);
print;

ps = """
(
 ( { a } x^3 y + { c } x y^2 ),
 ( x^4 y + { 3 d } y ),
 ( { c } x^2 + { b } x y ),
 ( x^2 y^2 + { a } x^2 ),
 ( x^5 + y^5 )
) 
""";

#startLog();

f = r.paramideal( ps );
print "ParamIdeal: " + str(f);
print;

gs = f.CGBsystem();
print "CGBsystem: " + str(gs);
print;

sys.exit();

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
