#
# jython examples for jas.
# $Id: hawes2mod.py 2056 2008-08-11 20:34:05Z kredel $
#
## \begin{PossoExample}
## \Name{Hawes2}
## \Parameters{a;b;c}
## \Variables{x;y[2];z[2]}
## \begin{Equations}
## x+2y_1z_1+3ay_1^2+5y_1^4+2cy_1 \&
## x+2y_2z_2+3ay_2^2+5y_2^4+2cy_2 \&
## 2 z_2+6ay_2+20 y_2^3+2c \&
## 3 z_1^2+y_1^2+b \&
## 3z_2^2+y_2^2+b \&
## \end{Equations}
## \end{PossoExample}


import sys;

from jas import Ring
from jas import Ideal
from jas import startLog
from jas import terminate

from edu.jas.arith import ModIntegerRing

#startLog();

# Hawes & Gibson example 2
# rational function coefficients

r = Ring( "RatFunc(a, c, b) (y2, y1, z1, z2, x) G" );
print "Ring: " + str(r);
print;

ps = """
(
 ( x + 2 y1 z1 + { 3 a } y1^2 + 5 y1^4 + { 2 c } y1 ),
 ( x + 2 y2 z2 + { 3 a } y2^2 + 5 y2^4 + { 2 c } y2 ), 
 ( 2 z2 + { 6 a } y2 + 20 y2^3 + { 2 c } ), 
 ( 3 z1^2 + y1^2 + { b } ), 
 ( 3 z2^2 + y2^2 + { b } ) 
) 
""";

f = r.paramideal( ps );
print "Ideal: " + str(f);
print;

fi = f.toIntegralCoeff();
print "Ideal: " + str(fi);
print;

#mf = ModIntegerRing( str(2**60-93), True );
mf = ModIntegerRing( str(19), True );
fm = fi.toModularCoeff(mf);
print "Ideal: " + str(fm);
print;

fmq = fm.toQuotientCoeff();
print "Ideal: " + str(fmq);
print;

rg = fmq.GB();
print "GB:", rg;
print;

bg = rg.isGB();
print "isGB:", bg;
print;

terminate();
#sys.exit();

