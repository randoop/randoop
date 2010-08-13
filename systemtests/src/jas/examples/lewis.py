#
# jython examples for jas.
# $Id: lewis.py 2167 2008-10-03 21:48:26Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate

#startLog();

# Lewis example 
# integral function coefficients

#r = Ring( "IntFunc(a1,a2,a3,b1,b2,b3,c1,c2,c3,d1,d2,d3,e1,e2,e3) (t1,t2,t3) G" );
r = Ring( "RatFunc(a1,a2,a3,b1,b2,b3,c1,c2,c3,d1,d2,d3,e1,e2,e3) (t1,t2,t3) G" );
print "Ring: " + str(r);
print;

ps = """
(
( { a1 } * t1^2 * t2^2 + { b1 } * t1^2 + { 2 c1 } * t1 * t2 + { d1 } * t2^2 + { e1 } ),
( { a2 } * t2^2 * t3^2 + { b2 } * t2^2 + { 2 c2 } * t2 * t3 + { d2 } * t3^2 + { e2 } ),
( { a3 } * t1^2 * t3^2 + { b3 } * t1^2 + { 2 c3 } * t1 * t3 + { d3 } * t3^2 + { e3 } )
) 
""";

#a1 = e2 + s22
#+ s27
#− s25
#− 2e ∗ s2 + 2e ∗ s7 − 2s2 ∗ s7


f = r.ideal( ps );
print "ParamIdeal: " + str(f);
print;

sys.exit();

#startLog();

rg = f.GB();
#rg = f.GB();
print "GB:", rg;
print;

bg = rg.isGB();
print "isGB:", bg;
print;

#startLog();
terminate();
#sys.exit();

