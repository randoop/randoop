#
# jython examples for jas.
# $Id: getstart.py 802 2006-03-20 21:11:37Z kredel $
#

from jas import Ring
from jas import Ideal

# trinks 7 example

r = Ring( "Rat(B,S,T,Z,P,W) L" );

print "Ring: " + str(r);
print;

ps = """
( 
 ( 45 P + 35 S - 165 B - 36 ), 
 ( 35 P + 40 Z + 25 T - 27 S ), 
 ( 15 W + 25 S P + 30 Z - 18 T - 165 B**2 ), 
 ( - 9 W + 15 T P + 20 S Z ), 
 ( P W + 2 T Z - 11 B**3 ), 
 ( 99 W - 11 B S + 3 B**2 ),
 ( B**2 + 33/50 B + 2673/10000 )
) 
""";

f = Ideal( r, ps );
print "Ideal: " + str(f);
print;

g = f.GB();
print "Groebner base:", g;
print;
