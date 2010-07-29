#
# jython examples for jas.
# $Id: quantum.py 801 2006-03-20 21:10:02Z kredel $
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

from edu.jas.module   import ModSolvableGroebnerBaseAbstract;

if ModSolvableGroebnerBaseAbstract().isLeftGB( flg.mset ):
   print "is left GB";
else:
   print "is not left GB";



ftg = f.twosidedGB();
print "seq twosided GB:", ftg;
print;

if ModSolvableGroebnerBaseAbstract().isLeftGB( ftg.mset ):
   print "twosided GB is left GB";
else:
   print "twosided GB is not left GB";

if ModSolvableGroebnerBaseAbstract().isRightGB( ftg.mset ):
   print "twosided GB is right GB";
else:
   print "twosided GB is not right GB";

if ModSolvableGroebnerBaseAbstract().isTwosidedGB( ftg.mset ):
   print "is twosided GB";
else:
   print "is not twosided GB";


from jas import startLog
startLog();

frg = f.rightGB();
print "seq right GB:", frg;
print;

if ModSolvableGroebnerBaseAbstract().isRightGB( frg.mset ):
   print "is right GB";
else:
   print "is not right GB";
