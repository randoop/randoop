#
# jython examples for jas.
# $Id: nabeshima_cgbF6.py 1977 2008-08-03 10:40:23Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate


# Nabashima, ISSAC 2007, example F6
# integral function coefficients

r = Ring( "IntFunc(a, b,c, d) (x) L" );
print "Ring: " + str(r);
print;

ps = """
(
 ( x^4 + { a } x^3 + { b } x2 + { c } x + { d } ),
 ( 4 x^3 + { 3 a } x^2 + { 2 b } x + { c } )
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

