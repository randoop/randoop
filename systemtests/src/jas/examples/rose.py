#
# jython examples for jas.
# $Id: rose.py 1359 2007-09-09 12:00:29Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal

# example from rose (modified)

#r = Ring( "Mod 19 (U3,U4,A46) L" );
#r = Ring( "Mod 1152921504606846883 (U3,U4,A46) L" ); # 2^60-93
#r = Ring( "Quat(U3,U4,A46) L" );
#r = Ring( "Z(U3,U4,A46) L" );
#r = Ring( "C(U3,U4,A46) L" );

r = Ring( "Rat(A46,U3,U4) G" );
print "Ring: " + str(r);
print;

ps = """
(   
 ( U4^4 - 20/7 A46^2 ), 
 ( A46^2 U3^4 + 7/10 A46 U3^4 + 7/48 U3^4 - 50/27 A46^2 - 35/27 A46 - 49/216 ), 
 ( A46^5 U4^3 + 7/5 A46^4 U4^3 + 609/1000 A46^3 
 U4^3 + 49/1250 A46^2 U4^3 - 27391/800000 A46 U4^3 
 - 1029/160000 U4^3 + 3/7 A46^5 U3 U4^2 + 3/5 A46^6 
 U3 U4^2 + 63/200 A46^3 U3 U4^2 + 147/2000 A46^2 
 U3 U4^2 + 4137/800000 A46 U3 U4^2 - 7/20 A46^4 
 U3^2 U4 - 77/125 A46^3 U3^2 U4 - 23863/60000 A46^2 
 U3^2 U4 - 1078/9375 A46 U3^2 U4 - 24353/1920000 
  U3^2 U4 - 3/20 A46^4 U3^3 - 21/100 A46^3 U3^3 
 - 91/800 A46^2 U3^3 - 5887/200000 A46 U3^3 
 - 343/128000 U3^3 ) 
) 
""";

f = Ideal( r, ps );
print "Ideal: " + str(f);
print;

rg = f.GB();
#print "seq Output:", rg;
#print;

#sys.exit();

rg = f.parGB(2);
#print "par Output:", rg;
#print;

rg = f.parOldGB(2);
#print "par-old Output:", rg;
#print;

#f.distClient(); # starts in background
#rg = f.distGB(2);
#print "dist Output:", rg;
#print;

#sys.exit();


