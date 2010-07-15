#
# jython examples for jas.
# $Id: cgbmmn15.py 1977 2008-08-03 10:40:23Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate

# rational function coefficients
# IP (alpha,beta,gamma,epsilon,theta,eta)
# (c3,c2,c1) /G/
#r = Ring( "IntFunc(alpha,beta,gamma,epsilon,theta,eta)(c3,c2,c1) G" );
# ( { alpha } c1 - { beta } c1**2 - { gamma } c1 c2 + { epsilon } c3 ),
# ( - { gamma } c1 c2 + { epsilon + theta } c3 - { gamma } c2 ),
# ( { gamma } c2 c3 + { eta } c2 - { epsilon + theta } c3 )

r = Ring( "IntFunc(a,b,g,e,t,eta)(c3,c2,c1) G" );
print "Ring: " + str(r);
print;

ps = """
(
 ( { a } c1 - { b } c1**2 - { g } c1 c2 + { e } c3 ),
 ( - { g } c1 c2 + { e + t } c3 - { g } c2 ),
 ( { g } c2 c3 + { eta } c2 - { e + t } c3 )
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
#sys.exit();

