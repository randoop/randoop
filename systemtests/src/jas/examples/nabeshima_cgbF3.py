#
# jython examples for jas.
# $Id: nabeshima_cgbF3.py 2170 2008-10-03 21:50:07Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate


# Nabashima, ISSAC 2007, example F3
# integral function coefficients

r = Ring( "IntFunc(c, b, a, d) (x) L" );
print "Ring: " + str(r);
print;

ps = """
(
 ( { a } x^4 + { c } x^2 + { b } ),
 ( { b } x^3 + x^2 + 2 ),
 ( { c } x^2 + { d } x )
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
