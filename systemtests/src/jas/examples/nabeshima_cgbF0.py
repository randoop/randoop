#
# jython examples for jas.
# $Id: nabeshima_cgbF0.py 2106 2008-09-06 11:13:23Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate


# Nabashima, ISSAC 2007, example Ex-4.8
# integral function coefficients

r = Ring( "IntFunc(a, b, c) (y,x) L" );
print "Ring: " + str(r);
print;

ps = """
(
 ( { a } x^2 + { b } y^2 ),
 ( { c } x^2 + y^2 ),
 ( { 2 a } x - { 2 c } y )
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

