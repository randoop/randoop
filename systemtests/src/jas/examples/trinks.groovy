/*
 * groovy trinks example.
 * $Id: $
 */

import edu.jas.groovy.Ring
import edu.jas.groovy.Ideal

def r = new Ring( "Rat(B,S,T,Z,P,W) L" );
println "Ring: " + r.toString();
println "";


def ps = """
( 
 ( 45 P + 35 S - 165 B - 36 ), 
 ( 35 P + 40 Z + 25 T - 27 S ), 
 ( 15 W + 25 S P + 30 Z - 18 T - 165 B**2 ), 
 ( - 9 W + 15 T P + 20 S Z ), 
 ( P W + 2 T Z - 11 B**3 ), 
 ( 99 W - 11 B S + 3 B**2 )
 , ( B**2 + 33/50 B + 2673/10000 )
) 
""";

// ( B**2 + 33/50 B + 2673/10000 )

def f = r.ideal( ps );
println "Ideal: " + f
println ""

def rg = f.GB();
println "seq Output: " + rg
println ""


//terminate()

a = new edu.jas.arith.BigRational(2,3)
println "a = " + a

b = a.fromInteger(5)
println "b = " + b

c = a.sum(b)
println "c = " + c

pp = new edu.jas.structure.Power(a)

d = pp.positivePower(c,10)
println "d = " + d

d = pp.power(c,10)
println "d = " + d

//println "plus = " + (a+b)
