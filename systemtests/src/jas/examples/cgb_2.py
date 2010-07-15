#
# jython examples for jas.
# $Id: cgb_2.py 1977 2008-08-03 10:40:23Z kredel $
#

import sys;

from jas import Ring
from jas import ParamIdeal
from jas import startLog
from jas import terminate


# 2 univariate polynomials of degree 2 example for comprehensive GB
# integral/rational function coefficients

#r = Ring( "RatFunc(u,v) (x,y) L" );
r = Ring( "IntFunc(a2, a1, a0, b2, b1, b0) (x) L" );
print "Ring: " + str(r);
print;

ps = """
(
 ( { a2 } x^2 + { a1 } x + { a0 } ),
 ( { b2 } x^2 + { b1 } x + { b0 } )
) 
""";

f = r.paramideal( ps );
print "ParamIdeal: " + str(f);
print;

#sys.exit();

#startLog();

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

#sys.exit();

bg = gs.isCGB();
if bg:
    print "isCGB: true";
else:
    print "isCGB: false";
print;

terminate();
#sys.exit();


