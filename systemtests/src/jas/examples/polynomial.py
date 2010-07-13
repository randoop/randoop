#
# jython examples for jas.
# $Id: polynomial.py 2225 2008-11-17 20:45:12Z kredel $
#

from java.lang import System
from java.lang import Integer

from jas import Ring
from jas import Ideal
from jas import terminate
from jas import startLog

# polynomial examples: gcd

#r = Ring( "Mod 1152921504606846883 (x,y,z) L" );
#r = Ring( "Rat(x,y,z) L" );
#r = Ring( "C(x,y,z) L" );
r = Ring( "Z(x,y,z) L" );

print "Ring: " + str(r);
print;

[x,y,z] = r.gens();

one = r.one();
a = r.random();
b = r.random();
c = abs(r.random());
#c = 1; 
#a = 0;

f = x * a + b * y**2 + one * z**7;

print "a = ", a;
print "b = ", b;
print "c = ", c;
print "f = ", f;
print;

ac = a * c;
bc = b * c;

print "ac = ", ac;
print "bc = ", bc;
print;

t = System.currentTimeMillis();
g = r.gcd(ac,bc);
t = System.currentTimeMillis() - t;
print "g = ", g;
#print "gcd time =", t, "milliseconds";

d = r.gcd(g,c);
if cmp(c,d) == 0:
    print "gcd time =", t, "milliseconds,", "isGcd(c,d): true" ;
else:
    print "gcd time =", t, "milliseconds,", "isGcd(c,d): ",  cmp(c,d);
print;

#d = g / c;
#m = g % c;
#print "d = ", d;
#print "m = ", m;
#print;

#startLog();
terminate();
