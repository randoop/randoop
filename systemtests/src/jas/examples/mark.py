#
# jython examples for jas.
# $Id: mark.py 1429 2007-10-13 12:58:29Z kredel $
#

#import sys;

from jas import Ring
from jas import Ideal
from jas import startLog

# mark, d-gb diplom example

r = Ring( "Z(x,y,z) L" );
print "Ring: " + str(r);
print;

ps = """
( 
 ( z + x y**2 + 4 x**2 + 1 ),
 ( y**2 z + 2 x + 1 ),
 ( x**2 z + y**2 + x )
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

dg = dgbs.GB( f.list );
rg = r.ideal(list=dg);
print "seq d-GB:", rg;
print "is d-GB:", dgbs.isGB(dg);
print;

print "d-GB == e-GB:", eg.equals(dg);
