#
# jython examples for jas.
# $Id: pppj2006.py 1094 2007-05-24 20:56:35Z kredel $
#

import sys;

from jas import Ring
from jas import Ideal

# pppj 2006 paper examples

r = Ring( "Z(x1,x2,x3) L" );
print "Ring: " + str(r);
print;


ps = """
( 
 ( 3 x1^2 x3^4 + 7 x2^5 - 61 )
) 
""";

#f = Ideal( r, ps );
#print "Ideal: " + str(f);
#print;

f = r.ideal( ps );
print "Ideal: " + str(f);
print;

from java.lang import System
from java.io import StringReader

from edu.jas.structure   import *
from edu.jas.arith       import *
from edu.jas.poly        import *

from org.apache.log4j import BasicConfigurator;
BasicConfigurator.configure();

pps = """
 3 x1^2 x3^4 + 7 x2^5 - 61
""";

ri = r.ring;

print "ri = " + str(ri);

pol = r.pset;
print "pol = " + str(pol);

pol = ri.parse( pps );

print "pol = " + str(pol);
