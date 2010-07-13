#
# jython examples for jas.
# $Id: nabeshima_cgbF8.py 1977 2008-08-03 10:40:23Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate


# Nabashima, ISSAC 2007, example F8
# modified, take care
# integral function coefficients


#r = Ring( "IntFunc(d, b, c, a) (w,z,y,x) G" );
#r = Ring( "IntFunc(b, c, a) (w,x,z,y) L" );
#r = Ring( "IntFunc(b, c) (z,y,w,x) L" );
#r = Ring( "IntFunc(b) (z,y,w,x) L" );
#r = Ring( "IntFunc(c) (z,y,w,x) L" );
r = Ring( "IntFunc(c) (z,y,w,x) L" );
print "Ring: " + str(r);
print;

ps = """
(
 ( { 1 } x^2 + { 1 } y ),
 ( { 1 } w^2 + z ),
 ( ( x - z )^2 + ( y - w)^2 ),
 ( { 2 } x w - { 2 1 } y )
) 
""";

# ( { 1 } x^2 + { b } y ),
# ( { c } w^2 + z ),
# ( { a } x^2 + { b } y ),
# ( { 2 d } x w - { 2 b } y )


#startLog();

f = r.paramideal( ps );
print "ParamIdeal: " + str(f);
print;

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

bg = gs.isCGB();
if bg:
    print "isCGB: true";
else:
    print "isCGB: false";
print;

terminate();
#------------------------------------------
#sys.exit();

