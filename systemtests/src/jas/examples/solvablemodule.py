#
# jython examples for jas.
# $Id: solvablemodule.py 802 2006-03-20 21:11:37Z kredel $
#

from jas import SolvableModule
from jas import SolvableSubModule

# Quantum plane example

rsan = """
AN[ (i) (i^2 + 1) ] (Y,X,x,y) G
RelationTable
(
 ( y ), ( x ), ( _i_ x y )
 ( X ), ( Y ), ( _i_ Y X )
)
""";

rsc = """
C(Y,X,x,y) G |2|
RelationTable
(
 ( y ), ( x ), ( 0i1 x y )
 ( X ), ( Y ), ( 0i1 Y X )
)
""";

r = SolvableModule( rsc );
print "SolvableModule: " + str(r);
print;


ps = """
(
 ( ( x + 1 ), ( y ) ),
 ( ( x y ), ( 0 ) ),
 ( ( x - X ), ( x - X ) ),
 ( ( y - Y ), ( y - Y ) )
)
""";

f = SolvableSubModule( r, ps );
print "SolvableSubModule: " + str(f);
print;


flg = f.leftGB();
print "seq left GB:", flg;
print;


ftg = f.twosidedGB();
print "seq twosided GB:", ftg;
print;

