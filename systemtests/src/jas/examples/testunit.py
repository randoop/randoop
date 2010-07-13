#
# test functionality of the jython interface to jas.
# $Id: testunit.py 802 2006-03-20 21:11:37Z kredel $
#

# test ideal Groebner bases 
execfile("examples/trinksTest.py")

# test module Groebner bases 
execfile("examples/armbruster.py")

# test solvable Groebner bases 
execfile("examples/wa_32.py")

# test solvable module Groebner bases 
execfile("examples/solvablemodule.py")


import sys;
sys.exit();
