#
# jython examples for jas.
# $Id: powerseries.py 2199 2008-11-05 21:46:41Z kredel $
#

import sys;

from jas import QQ
from jas import DD
from jas import Ring
from jas import SeriesRing
from jas import startLog

from edu.jas.ps import Coefficients
from edu.jas.ps import PowerSeriesMap

# example for power series
#
#

#
# rational number examples
#

psr = SeriesRing("Q(y)");
print "psr:", psr;
print;

one = psr.one();
print "one:", one;
print;

zero = psr.zero();
print "zero:", zero;
print;

r1 = psr.random(4);
print "r1:", r1;
print;
print "r1:", r1;
print;
print "r1-r1:", r1-r1;
print;

r2 = psr.random(4);
print "r2:", r2;
print;
print "r2:", r2;
print;
print "r2-r2:", r2-r2;
print;

#sys.exit();

r3 = r1 + r2;
print "r3:", r3;
print;

r4 = r1 * r2 + one;
print "r4:", r4;
print;

e = psr.exp();
print "e:", e;
print;

r5 = r1 * r2 + e;
print "r5:", r5;
print;

y = psr.gens();
print "y:", y;
print;

r6 = one - y;
print "r6:", r6;
print;

r7 = one / r6;
print "r7:", r7;
print;


s = psr.sin();
print "s:", s;
print;

r8 = psr.gcd(y,s);
print "r8:", r8;
print;



s1 = s.evaluate( QQ(0) );
print "s1:", s1;
print;

c = psr.cos();
print "c:", c;
print;

c1 = c.evaluate( QQ(0) );
print "c1:", c1;
print;

s2c2 = s*s+c*c; # sin^2 + cos^2 = 1
print "s2c2:", s2c2;
print;


#
# floating point examples
#

dr = SeriesRing(cofac=DD());
print "dr:", dr;
print;

de = dr.exp();
print "de:", de;
print;

d0 = de.evaluate( DD(0) );
print "d0:", d0;
print;

d1 = de.evaluate( DD(0.5) );
print "d1:", d1;
print;

d01 = de.evaluate( DD(0.000000000000000001) );
print "d01:", d01;
print;


def f(a):
    return a*a;

ps = psr.create(f);
print "ps:", ps;
print;


def g(a):
    return a+a;

ps1 = psr.create(g);
print "ps1:", ps1;
print;

ps2 = ps * ps1;
print "ps2:", ps2;
print;


def h(a):
    return psr.ring.coFac.fromInteger( 2*a );

ps3 = psr.create(jfunc=h);
print "ps3:", ps3;
print;

ps4 = ps3 * ps1;
print "ps4:", ps4;
print;


# does not work, since get() is not known
def k(a):
    if a > 0:
        return get(a-1).multiply( psr.ring.coFac.fromInteger( 2*a ) );
    else:
        return psr.ring.coFac.fromInteger( 2*a );

ps5 = psr.create(jfunc=k);
print "ps5:", ps5;
print;


class coeff( Coefficients ):
    def __init__(self,cofac):
        self.coFac = cofac;
    def generate(self,i):
        if i == 0:
            return self.coFac.getZERO();
        else:
            if i == 1:
                return self.coFac.getONE();
            else:
                c = self.get( i-2 ).negate();
                return c.divide( self.coFac.fromInteger(i) ).divide( self.coFac.fromInteger(i-1) );

ps6 = psr.create( clazz=coeff(psr.ring.coFac) );
print "ps6:", ps6;
print;

ps7 = ps6 - s;
print "ps7:", ps7;
print;


class cosmap( PowerSeriesMap ):
    def __init__(self,cofac):
        self.coFac = cofac;
    def map(self,ps):
        return ps.negate().integrate( self.coFac.getZERO() ).integrate( self.coFac.getONE() );

ps8 = psr.fixPoint( cosmap( psr.ring.coFac ) );
print "ps8:", ps8;
print;

ps9 = ps8 - c;
print "ps9:", ps9;
print;


# conversion from polynomials

pr = Ring("Q(y) L");
print "pr:", pr;
print;

[yp] = pr.gens();

one = pr.one();
p1 = one;
p2 = one - yp;

ps1 = psr.from(p1);
ps2 = psr.from(p2);

# rational function as power series:
ps3 = ps1 / ps2;

print "p1:", p1;
print "p2:", p2;
print "ps1:", ps1;
print "ps2:", ps2;
print "ps3:", ps3;
print;


p1 = one * 2 + yp**3 - yp**5;
p2 = one - yp**2 + yp**4;

ps1 = psr.from(p1);
ps2 = psr.from(p2);

# rational function as power series:
ps3 = ps1 / ps2;

ps4 = ps3.integrate( QQ(1) );
ps5 = ps3.differentiate();

print "p1:", p1;
print "p2:", p2;
print "ps1:", ps1;
print "ps2:", ps2;
print "ps3:", ps3;
print "ps4:", ps4;
print "ps5:", ps5;
print;


#sys.exit();
