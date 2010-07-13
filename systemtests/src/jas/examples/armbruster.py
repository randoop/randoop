#
# jython examples for jas.

from jas import Module
from jas import SubModule

# Armbruster module example

r = Module( "Rat(u,v,l) L" );
print "Module: " + str(r);
print;


ps = """
(
(     ( 1 ),         ( 2 ),       ( 0 ),       ( l^2 ) ),
(     ( 0 ),   ( l + 3 v ),       ( 0 ),         ( u ) ),
(     ( 1 ),         ( 0 ),       ( 0 ),       ( l^2 ) ),
( ( l + v ),         ( 0 ),       ( 0 ),         ( u ) ),
(   ( l^2 ),         ( 0 ),       ( 0 ),         ( v ) ),
(     ( u ),         ( 0 ),       ( 0 ), ( v l + v^2 ) ),
(     ( 1 ),         ( 0 ), ( l + 3 v ),         ( 0 ) ),
(   ( l^2 ),         ( 0 ),     ( 2 u ),         ( v ) ),
(     ( 0 ),         ( 1 ),   ( l + v ),         ( 0 ) ),
(     ( 0 ),       ( l^2 ),       ( u ),         ( 0 ) ),
(     ( 0 ),         ( v ),   ( u l^2 ),         ( 0 ) ),
(     ( 0 ), ( v l + v^2 ),     ( u^2 ),         ( 0 ) )
) 
""";

f = SubModule( r, ps );
print "SubModule: " + str(f);
print;

#from edu.jas.module import *
#print "SubModule: " + str(ModuleList.getModuleList(4,f.pset));
#print;

rg = f.GB();
print "seq Output:", rg;
print;

#print "SubModule: " + str(ModuleList.getModuleList(4,rg));
#print;

