#
# jython examples for jas.
# $Id: u_sl_3_prod.py 641 2006-02-19 11:13:27Z kredel $
#

from jas import SolvableRing
from jas import SolvableIdeal


# U(sl_3) example

rs = """
# solvable polynomials, U(sl_3):
Rat(Xa,Xb,Xc,Ya,Yb,Yc,Ha,Hb) G
RelationTable
(
 ( Xb ), ( Xa ), ( Xa Xb - Xc ),
 ( Ya ), ( Xa ), ( Xa Ya - Ha ),
 ( Yc ), ( Xa ), ( Xa Yc + Yb ),
 ( Ha ), ( Xa ), ( Xa Ha + 2 Xa ),
 ( Hb ), ( Xa ), ( Xa Hb - Xa),

 ( Yb ), ( Xb ), ( Xb Yb - Hb ),
 ( Yc ), ( Xb ), ( Xb Yc - Ya ),
 ( Ha ), ( Xb ), ( Xb Ha - Xb ),
 ( Hb ), ( Xb ), ( Xb Hb + 2 Xb ),

 ( Ya ), ( Xc ), ( Xc Ya + Xb ),
 ( Yb ), ( Xc ), ( Xc Yb - Xa ),
 ( Yc ), ( Xc ), ( Xc Yc - Ha - Hb ),
 ( Ha ), ( Xc ), ( Xc Ha + Xc ),
 ( Hb ), ( Xc ), ( Xc Hb + Xc ),

 ( Yb ), ( Ya ), ( Ya Yb + Yc ),
 ( Ha ), ( Ya ), ( Ya Ha - 2 Ya ),
 ( Hb ), ( Ya ), ( Ya Hb + Ya ),

 ( Ha ), ( Yb ), ( Yb Ha + Yb ),
 ( Hb ), ( Yb ), ( Yb Hb - 2 Yb ),

 ( Ha ), ( Yc ), ( Yc Ha - Yc ),
 ( Hb ), ( Yc ), ( Yc Hb - Yc )
 
)
""";

r = SolvableRing( rs );
print "SolvableRing: " + str(r);
print;


ps = """
(
 ( Xb + Yb )
)
""";
# ( Xa + Xb + Xc + Ya + Yb + Yc + Ha + Hb )

f = SolvableIdeal( r, ps );
print "SolvIdeal: " + str(f);
print;


fl = f.list;
print "fl: ", fl;

p = fl[0];
print "p: ", p;
print;

from java.lang import System
p2 = p;
n = 15;
t = System.currentTimeMillis();
for i in range(1,n):
    p2 = p2.multiply(p);
    t1 = System.currentTimeMillis() -t;
    print "one product in %s ms" % t1;

print "p^%s.length: " % n, p2.length();
print;

p2 = p;
t = System.currentTimeMillis();
for i in range(1,n):
    p2 = p2.multiply(p);
    t1 = System.currentTimeMillis() -t;
    print "one product in %s ms" % t1;

print "p^%s.length: " % n, p2.length();
print;


ps = """
(
 ( Xa ),
 ( Xb ),
 ( Xc ),
 ( Ya ),
 ( Yb ),
 ( Yc ),
 ( Ha ),
 ( Hb )
)
""";

f = SolvableIdeal( r, ps );
#print "SolvableIdeal: " + str(f);
#print;

fl = f.list;
Yb = fl[4];
p1 = Yb;
Xb = fl[1];
p2 = Xb;

n = 10;
t = System.currentTimeMillis();
for i in range(1,n):
    p1 = p1.multiply(Yb);
    p2 = p2.multiply(Xb);
    p  = p1.multiply(p2);
    t1 = System.currentTimeMillis() -t;
    print "products in %s ms" % t1;

print "Xb^%s * Yb^%s: " % (n,n); #, p;
print;
pp = p;

p1 = Yb;
p2 = Xb;
t = System.currentTimeMillis();
for i in range(1,n):
    p1 = p1.multiply(Yb);
    p2 = p2.multiply(Xb);
    p  = p1.multiply(p2);
    t1 = System.currentTimeMillis() -t;
    print "products in %s ms" % t1;

print "Xb^%s * Yb^%s: " % (n,n); #, p;
print;

print "pp == p: ", (pp == p);
print;

#print "SolvIdeal: " + str(f);
#print;
