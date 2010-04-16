#
# jython examples for jas.
# $Id: katsura.py 641 2006-02-19 11:13:27Z kredel $
#

from jas import Ring
from jas import Ideal

from edu.jas.ring import Katsura;

# katsura examples

knum = 4
tnum = 2;

#r = Ring( "Mod 19 (B,S,T,Z,P,W) L" );
#r = Ring( "Mod 1152921504606846883 (B,S,T,Z,P,W) L" ); # 2^60-93
#r = Ring( "Quat(B,S,T,Z,P,W) L" );
#r = Ring( "Z(B,S,T,Z,P,W) L" );
#r = Ring( "C(B,S,T,Z,P,W) L" );
#r = Ring( "Rat(B,S,T,Z,P,W) L" );
#print "Ring: " + str(r);
#print;

k = Katsura(knum);
r = Ring( k.varList("Rat","G") );
print "Ring: " + str(r);
print;

ps = k.polyList();

f = Ideal( r, ps );
print "Ideal: " + str(f);
print;

rg = f.parGB(tnum);
for th in range(tnum,0,-1):
   rg = f.parGB(th);
   #print "par Output:", rg;
   #print;

rg = f.GB();
#print "seq Output:", rg;
print;


# rg = f.distGB(2);
#print "dist Output:", rg;
#print;

#f.distClient();


