#
# jython examples for jas.
# $Id: polypower.py 1094 2007-05-24 20:56:35Z kredel $
#

from java.lang import System
from java.lang import Integer

from jas import Ring
from jas import Ideal

# sparse polynomial powers

#r = Ring( "Mod 1152921504606846883 (x,y,z) G" );
#r = Ring( "Rat(x,y,z) G" );
#r = Ring( "C(x,y,z) G" );
r = Ring( "Z(x,y,z) L" );

print "Ring: " + str(r);
print;

ps = """
( 
 ( 1 + x^2147483647 + y^2147483647 + z^2147483647 )
 ( 1 + x + y + z )
 ( 10000000001 + 10000000001 x + 10000000001 y + 10000000001 z )
) 
""";

f = Ideal( r, ps );
print "Ideal: " + str(f);
print;

pset = f.pset;
print "pset:", pset;
print;

plist = pset.list;
print "plist:", plist;
print;

p = plist[0];
#p = plist[2];
print "p:", p;
print;

q = p;
for i in range(1,20):
    q = q.multiply(p);

print "q:", q.length();
print;

q1 = q.sum( r.ring.getONE() );
#print "q1:", q1;
print "q1:", q1.length();
print;

t = System.currentTimeMillis();
q2 = q.multiply(q1);
t = System.currentTimeMillis() - t;

print "q2:", q2.length();
print "t:",t;
print;


print "Integer.MAX_VALUE = ", Integer.MAX_VALUE;
