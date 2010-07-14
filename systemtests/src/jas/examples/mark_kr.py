#
# jython examples for jas.
# $Id: mark_kr.py 1446 2007-10-14 11:24:34Z kredel $
#

#import sys;

from jas import Ring
from jas import Ideal
from jas import startLog

# mark, d-gb diplom example, due to kandri-rody 1984
#
# The MAS DIIPEGB implementation contains an error because the output e-GB
# is not correct. Also the cited result from k-r contains this error.
# The polynomial 
#
# ( 2 x * y^2 - x^13 + 2 x^11 -   x^9 + 2 x^7 - 2 x^3 ),
#
# is in the DIIPEGB output, but it must be
#
# ( 2 x * y^2 - x^13 + 2 x^11 - 3 x^9 + 2 x^7 - 2 x^3 ),
#
# Test by adding the polynomials to the input.
# Frist polynomial produces a different e-GB. 
# Second polynomial reproduces the e-GB with the second polynomial. 

r = Ring( "Z(x,y) L" );
print "Ring: " + str(r);
print;

ps = """
( 
 ( y**6 + x**4 y**4 - x**2 y**4 - y**4 - x**4 y**2 + 2 x**2 y**2 + x**6 - x**4 ),
 ( 2 x**3 y**4 - x y**4 - 2 x**3 y**2 + 2 x y**2 + 3 x**5 - 2 x** 3 ),
 ( 3 y**5 + 2 x**4 y**3 - 2 x**2 y**3 - 2 y**3 - x**4 y + 2 x**2 y )
) 
""";

f = r.ideal( ps );
print "Ideal: " + str(f);
print;

from edu.jas.ring import EGroebnerBaseSeq;
from edu.jas.ring import DGroebnerBaseSeq;

egbs = EGroebnerBaseSeq();
dgbs = DGroebnerBaseSeq();

#startLog();

eg = egbs.GB( f.list );
rg = r.ideal(list=eg);
print "seq e-GB:", rg;
print "is e-GB:", egbs.isGB(eg);
print;


#startLog();

dg = dgbs.GB( f.list );
rg = r.ideal(list=dg);
print "seq d-GB:", rg;
print "is d-GB:", dgbs.isGB(dg);
print;

print "d-GB == e-GB:", eg.equals(dg);
