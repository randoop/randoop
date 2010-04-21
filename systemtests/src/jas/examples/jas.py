'''jython interface to JAS.
'''

# $Id: jas.py 2225 2008-11-17 20:45:12Z kredel $

from java.lang           import System
from java.io             import StringReader
from java.util           import ArrayList

from org.apache.log4j    import BasicConfigurator;

from edu.jas.structure   import RingElem, RingFactory, Power
from edu.jas.arith       import BigInteger, BigRational, BigComplex, BigDecimal
from edu.jas.poly        import GenPolynomial, GenPolynomialRing,\
                                GenSolvablePolynomial, GenSolvablePolynomialRing,\
                                GenPolynomialTokenizer, OrderedPolynomialList, PolyUtil,\
                                TermOrderOptimization, TermOrder, PolynomialList
from edu.jas.ps          import UnivPowerSeries, UnivPowerSeriesRing,\
                                PowerSeriesMap, Coefficients  
from edu.jas.ring        import DGroebnerBaseSeq, EGroebnerBaseSeq,\
                                GroebnerBaseDistributed, GBDist, GroebnerBaseParallel,\
                                GroebnerBaseSeq, GroebnerBaseSeqPairSeq,\
                                GroebnerBasePseudoRecSeq, GroebnerBasePseudoSeq,\
                                ReductionSeq, GroebnerBaseSeqPairParallel,\
                                RGroebnerBasePseudoSeq, RGroebnerBaseSeq,\
                                SolvableGroebnerBaseParallel, SolvableGroebnerBaseSeq
from edu.jas.module      import ModGroebnerBaseAbstract, ModSolvableGroebnerBaseAbstract,\
                                SolvableSyzygyAbstract, SyzygyAbstract
from edu.jas.vector      import OrderedModuleList, ModuleList
from edu.jas.application import ComprehensiveGroebnerBaseSeq, PolyUtilApp,\
                                Residue, ResidueRing, Ideal, Quotient, QuotientRing
from edu.jas.kern        import ComputerThreads;
from edu.jas.ufd         import GreatestCommonDivisorSubres, PolyUfdUtil, GCDFactory
from edu.jas.util        import ExecutableServer
from edu.jas             import structure, arith, poly, ps, ring, module, vector,\
                                application, util, ufd
from edu                 import jas
#PrettyPrint.setInternal();

from org.python.core     import PyInstance, PyList, PyTuple,\
                                PyInteger, PyLong, PyFloat


def startLog():
    '''Configure the log4j system and start logging.
    '''
    BasicConfigurator.configure();

def terminate():
    '''Terminate the running thread pools.
    '''
    ComputerThreads.terminate();


class Ring:
    '''Represents a JAS polynomial ring: GenPolynomialRing.

    Methods to create ideals and ideals with parametric coefficients.
    '''

    def __init__(self,ringstr="",ring=None):
        '''Ring constructor.
        '''
        if ring == None:
           sr = StringReader( ringstr );
           tok = GenPolynomialTokenizer(sr);
           self.pset = tok.nextPolynomialSet();
           self.ring = self.pset.ring;
        else:
           self.ring = ring;
        self.engine = GCDFactory.getProxy(self.ring.coFac);

    def __str__(self):
        '''Create a string representation.
        '''
        return str(self.ring);

    def ideal(self,ringstr="",list=None):
        '''Create an ideal.
        '''
        return Ideal(self,ringstr,list);

    def paramideal(self,ringstr="",list=None,gbsys=None):
        '''Create an ideal in a polynomial ring with parameter coefficients.
        '''
        return ParamIdeal(self,ringstr,list,gbsys);

    def gens(self):
        '''Get list of generators of the polynomial ring.
        '''
        L = self.ring.univariateList();
        c = self.ring.coFac;
        nv = None;
        try:
            nv = c.nvar;
        except:
            pass
        #print "type(coFac) = ", type(self.ring.coFac);
        #if isinstance(c,GenPolynomial): # does not work
        if nv:
            Lp = c.univariateList();
            #Ls = [ GenPolynomial(self.ring,l) for l in Lp ];
            i = 0;
            for l in Lp:
                L.add( i, GenPolynomial(self.ring,l) );
                i += 1;
        N = [ RingElem(e) for e in L ];
        return N;

    def one(self):
        '''Get the one of the polynomial ring.
        '''
        return RingElem( self.ring.getONE() );

    def zero(self):
        '''Get the zero of the polynomial ring.
        '''
        return RingElem( self.ring.getZERO() );

    def random(self,k=5,l=7,d=3,q=0.3):
        '''Get a random polynomial.
        '''
        r = self.ring.random(k,l,d,q);
        if self.ring.coFac.isField():
            r = r.monic();
        return RingElem( r );

    def element(self,polystr):
        '''Create an element from a string.
        '''
        I = Ideal(self, "( " + polystr + " )");
        list = I.pset.list;
        if len(list) > 0:
            return RingElem( list[0] );

    def gcd(self,a,b):
        '''Compute the greatest common divisor of a and b.
        '''
        if isinstance(a,RingElem):
            a = a.elem;
        else:
            a = self.element( str(a) );
            a = a.elem;
        if isinstance(b,RingElem):
            b = b.elem;
        else:
            b = self.element( str(b) );
            b = b.elem;
        return RingElem( self.engine.gcd(a,b) );


class Ideal:
    '''Represents a JAS polynomial ideal: PolynomialList and Ideal.

    Methods for Groebner bases, ideal sum, intersection and others.
    '''

    def __init__(self,ring,polystr="",list=None):
        '''Ideal constructor.
        '''
        self.ring = ring;
        if list == None:
           sr = StringReader( polystr );
           tok = GenPolynomialTokenizer(ring.pset.ring,sr);
           self.list = tok.nextPolynomialList();
        else:
           self.list = pylist2arraylist(list);
        self.pset = OrderedPolynomialList(ring.ring,self.list);

    def __str__(self):
        '''Create a string representation.
        '''
        return str(self.pset);

    def paramideal(self):
        '''Create an ideal in a polynomial ring with parameter coefficients.
        '''
        return ParamIdeal(self.ring,"",self.list);

    def GB(self):
        '''Compute a Groebner base.
        '''
        s = self.pset;
        cofac = s.ring.coFac;
        F = s.list;
        t = System.currentTimeMillis();
        if cofac.isField():
            G = GroebnerBaseSeq().GB(F);
        else:
            v = None;
            try:
                v = cofac.vars;
            except:
                pass
            if v == None:
                G = GroebnerBasePseudoSeq(cofac).GB(F);
            else:
                G = GroebnerBasePseudoRecSeq(cofac).GB(F);
        t = System.currentTimeMillis() - t;
        print "sequential GB executed in %s ms" % t; 
        return Ideal(self.ring,"",G);

    def isGB(self):
        '''Test if this is a Groebner base.
        '''
        s = self.pset;
        F = s.list;
        t = System.currentTimeMillis();
        b = GroebnerBaseSeq().isGB(F);
        t = System.currentTimeMillis() - t;
        print "isGB executed in %s ms" % t; 
        return b;

    def parGB(self,th):
        '''Compute in parallel a Groebner base.
        '''
        s = self.pset;
        F = s.list;
        bbpar = GroebnerBaseSeqPairParallel(th);
        t = System.currentTimeMillis();
        G = bbpar.GB(F);
        t = System.currentTimeMillis() - t;
        bbpar.terminate();
        print "parallel-new %s executed in %s ms" % (th, t); 
        return Ideal(self.ring,"",G);

    def parOldGB(self,th):
        '''Compute in parallel a Groebner base.
        '''
        s = self.pset;
        F = s.list;
        bbpar = GroebnerBaseParallel(th);
        t = System.currentTimeMillis();
        G = bbpar.GB(F);
        t = System.currentTimeMillis() - t;
        bbpar.terminate();
        print "parallel-old %s executed in %s ms" % (th, t); 
        return Ideal(self.ring,"",G);

    def distGB(self,th=2,machine="examples/machines.localhost",port=7114):
        '''Compute on a distributed system a Groebner base.
        '''
        s = self.pset;
        F = s.list;
        t = System.currentTimeMillis();
        # G = GroebnerBaseDistributed.Server(F,th);
        #G = GBDist(th,machine,port).execute(F);
        gbd = GBDist(th,machine,port);
        t1 = System.currentTimeMillis();
        G = gbd.execute(F);
        t1 = System.currentTimeMillis() - t1;
        gbd.terminate(0);
        t = System.currentTimeMillis() - t;
        print "distributed %s executed in %s ms (%s ms start-up)" % (th,t1,t-t1); 
        return Ideal(self.ring,"",G);

    def distClient(self,port=8114):
        '''Client for a distributed computation.
        '''
        s = self.pset;
        es = ExecutableServer( port );
        es.init();
        return None;

    def NF(self,reducer):
        '''Compute a normal form of this ideal with respect to reducer.
        '''
        s = self.pset;
        F = s.list;
        G = reducer.list;
        t = System.currentTimeMillis();
        N = ReductionSeq().normalform(G,F);
        t = System.currentTimeMillis() - t;
        print "sequential NF executed in %s ms" % t; 
        return Ideal(self.ring,"",N);

    def intersect(self,ring):
        '''Compute the intersection of this and the given polynomial ring.
        '''
        s = jas.application.Ideal(self.pset);
        N = s.intersect(ring.ring);
        return Ideal(self.ring,"",N.getList());

    def sum(self,other):
        '''Compute the sum of this and the ideal.
        '''
        s = jas.application.Ideal(self.pset);
        t = jas.application.Ideal(other.pset);
        N = s.sum( t );
        return Ideal(self.ring,"",N.getList());

    def optimize(self):
        '''Optimize the term order on the variables.
        '''
        p = self.pset;
        o = TermOrderOptimization.optimizeTermOrder(p);
        r = Ring("",o.ring);
        return Ideal(r,"",o.list);

    def toInteger(self):
        '''Convert rational coefficients to integer coefficients.
        '''
        p = self.pset;
        l = p.list;
        r = p.ring;
        ri = GenPolynomialRing( BigInteger(), r.nvar, r.tord, r.vars );
        pi = PolyUtil.integerFromRationalCoefficients(ri,l);
        r = Ring("",ri);
        return Ideal(r,"",pi);

    def toModular(self,mf):
        '''Convert integer coefficients to modular coefficients.
        '''
        p = self.pset;
        l = p.list;
        r = p.ring;
        rm = GenPolynomialRing( mf, r.nvar, r.tord, r.vars );
        pm = PolyUtil.fromIntegerCoefficients(rm,l);
        r = Ring("",rm);
        return Ideal(r,"",pm);

    def squarefreeFactors(self):
        '''Compute squarefree factors of first polynomial.
        '''
        s = self.pset;
        F = s.list;
        p = F[0]; # only first polynomial
        t = System.currentTimeMillis();
        f = GreatestCommonDivisorSubres().squarefreeFactors(p);
        t = System.currentTimeMillis() - t;
        #print "squarefee part %s " % f;
        #S = ArrayList();
        #S.add(f);
        print "squarefee factors executed in %s ms" % t; 
        return f;


class ParamIdeal:
    '''Represents a JAS polynomial ideal with polynomial coefficients.

    Methods to compute comprehensive Groebner bases.
    '''

    def __init__(self,ring,polystr="",list=None,gbsys=None):
        '''Parametric ideal constructor.
        '''
        self.ring = ring;
        if list == None and polystr != None:
           sr = StringReader( polystr );
           tok = GenPolynomialTokenizer(ring.pset.ring,sr);
           self.list = tok.nextPolynomialList();
        else:
           self.list = pylist2arraylist(list);
        self.gbsys = gbsys;
        self.pset = OrderedPolynomialList(ring.ring,self.list);

    def __str__(self):
        '''Create a string representation.
        '''
        if self.gbsys == None:
            return str(self.pset);
        else:
            return str(self.gbsys);
#            return str(self.pset) + "\n" + str(self.gbsys);

    def optimizeCoeff(self):
        '''Optimize the term order on the variables of the coefficients.
        '''
        p = self.pset;
        o = TermOrderOptimization.optimizeTermOrderOnCoefficients(p);
        r = Ring("",o.ring);
        return ParamIdeal(r,"",o.list);

    def optimizeCoeffQuot(self):
        '''Optimize the term order on the variables of the quotient coefficients.
        '''
        p = self.pset;
        l = p.list;
        r = p.ring;
        q = r.coFac;
        c = q.ring;
        rc = GenPolynomialRing( c, r.nvar, r.tord, r.vars );
        #print "rc = ", rc;        
        lp = PolyUfdUtil.integralFromQuotientCoefficients(rc,l);
        #print "lp = ", lp;
        pp = PolynomialList(rc,lp);
        #print "pp = ", pp;        
        oq = TermOrderOptimization.optimizeTermOrderOnCoefficients(pp);
        oor = oq.ring;
        qo = oor.coFac;
        cq = QuotientRing( qo );
        rq = GenPolynomialRing( cq, r.nvar, r.tord, r.vars );
        #print "rq = ", rq;        
        o = PolyUfdUtil.quotientFromIntegralCoefficients(rq,oq.list);
        r = Ring("",rq);
        return ParamIdeal(r,"",o);

    def toIntegralCoeff(self):
        '''Convert rational function coefficients to integral function coefficients.
        '''
        p = self.pset;
        l = p.list;
        r = p.ring;
        q = r.coFac;
        c = q.ring;
        rc = GenPolynomialRing( c, r.nvar, r.tord, r.vars );
        #print "rc = ", rc;        
        lp = PolyUfdUtil.integralFromQuotientCoefficients(rc,l);
        #print "lp = ", lp;
        r = Ring("",rc);
        return ParamIdeal(r,"",lp);

    def toModularCoeff(self,mf):
        '''Convert integral function coefficients to modular function coefficients.
        '''
        p = self.pset;
        l = p.list;
        r = p.ring;
        c = r.coFac;
        #print "c = ", c;
        cm = GenPolynomialRing( mf, c.nvar, c.tord, c.vars );
        #print "cm = ", cm;
        rm = GenPolynomialRing( cm, r.nvar, r.tord, r.vars );
        #print "rm = ", rm;
        pm = PolyUfdUtil.fromIntegerCoefficients(rm,l);
        r = Ring("",rm);
        return ParamIdeal(r,"",pm);

    def toQuotientCoeff(self):
        '''Convert integral function coefficients to rational function coefficients.
        '''
        p = self.pset;
        l = p.list;
        r = p.ring;
        c = r.coFac;
        #print "c = ", c;
        q = QuotientRing(c);
        #print "q = ", q;
        qm = GenPolynomialRing( q, r.nvar, r.tord, r.vars );
        #print "qm = ", qm;
        pm = PolyUfdUtil.quotientFromIntegralCoefficients(qm,l);
        r = Ring("",qm);
        return ParamIdeal(r,"",pm);

    def GB(self):
        '''Compute a Groebner base.
        '''
        I = Ideal(self.ring,"",self.pset.list);
        g = I.GB();
        return ParamIdeal(g.ring,"",g.pset.list);

    def isGB(self):
        '''Test if this is a Groebner base.
        '''
        I = Ideal(self.ring,"",self.pset.list);
        return I.isGB();

    def CGB(self):
        '''Compute a comprehensive Groebner base.
        '''
        s = self.pset;
        F = s.list;
        t = System.currentTimeMillis();
        if self.gbsys == None:
            self.gbsys = ComprehensiveGroebnerBaseSeq(self.ring.ring.coFac).GBsys(F);
        G = self.gbsys.getCGB();
        t = System.currentTimeMillis() - t;
        print "sequential comprehensive executed in %s ms" % t; 
        return ParamIdeal(self.ring,"",G,self.gbsys);

    def CGBsystem(self):
        '''Compute a comprehensive Groebner system.
        '''
        s = self.pset;
        F = s.list;
        t = System.currentTimeMillis();
        S = ComprehensiveGroebnerBaseSeq(self.ring.ring.coFac).GBsys(F);
        t = System.currentTimeMillis() - t;
        print "sequential comprehensive system executed in %s ms" % t; 
        return ParamIdeal(self.ring,None,F,S);

    def isCGB(self):
        '''Test if this is a comprehensive Groebner base.
        '''
        s = self.pset;
        F = s.list;
        t = System.currentTimeMillis();
        b = ComprehensiveGroebnerBaseSeq(self.ring.ring.coFac).isGB(F);
        t = System.currentTimeMillis() - t;
        print "isCGB executed in %s ms" % t; 
        return b;

    def isCGBsystem(self):
        '''Test if this is a comprehensive Groebner system.
        '''
        s = self.pset;
        S = self.gbsys;
        t = System.currentTimeMillis();
        b = ComprehensiveGroebnerBaseSeq(self.ring.ring.coFac).isGBsys(S);
        t = System.currentTimeMillis() - t;
        print "isCGBsystem executed in %s ms" % t; 
        return b;

    def regularRepresentation(self):
        '''Convert Groebner system to a representation with regular ring coefficents.
        '''
        if self.gbsys == None:
            return None;
        G = PolyUtilApp.toProductRes(self.gbsys.list);
        ring = Ring(None,G[0].ring);
        return ParamIdeal(ring,None,G);

    def regularGB(self):
        '''Compute a Groebner base over a regular ring.
        '''
        s = self.pset;
        F = s.list;
        t = System.currentTimeMillis();
        G = RGroebnerBasePseudoSeq(self.ring.ring.coFac).GB(F);
        t = System.currentTimeMillis() - t;
        print "sequential regular GB executed in %s ms" % t; 
        return ParamIdeal(self.ring,None,G);

    def isRegularGB(self):
        '''Test if this is Groebner base over a regular ring.
        '''
        s = self.pset;
        F = s.list;
        t = System.currentTimeMillis();
        b = RGroebnerBasePseudoSeq(self.ring.ring.coFac).isGB(F);
        t = System.currentTimeMillis() - t;
        print "isRegularGB executed in %s ms" % t; 
        return b;

    def stringSlice(self):
        '''Get each component (slice) of regular ring coefficients separate.
        '''
        s = self.pset;
        b = PolyUtilApp.productToString(s);
        return b;


class SolvableRing:
    '''Represents a JAS solvable polynomial ring: GenSolvablePolynomialRing.

    Has a method to create solvable ideals.
    '''

    def __init__(self,ringstr="",ring=None):
        '''Solvable polynomial ring constructor.
        '''
        if ring == None:
           sr = StringReader( ringstr );
           tok = GenPolynomialTokenizer(sr);
           self.pset = tok.nextSolvablePolynomialSet();
           self.ring = self.pset.ring;
        else:
           self.ring = ring;
        if not self.ring.isAssociative():
           print "warning: ring is not associative";

    def __str__(self):
        '''Create a string representation.
        '''
        return str(self.ring);

    def ideal(self,ringstr="",list=None):
        '''Create a solvable ideal.
        '''
        return SolvableIdeal(self,ringstr,list);

    def gens(self):
        '''Get list of generators of the solvable polynomial ring.
        '''
        L = self.ring.univariateList();
        c = self.ring.coFac;
        nv = None;
        try:
            nv = c.nvar;
        except:
            pass
        #print "type(coFac) = ", type(self.ring.coFac);
        #if isinstance(c,GenPolynomial): # does not work
        if nv:
            Lp = c.univariateList();
            #Ls = [ GenPolynomial(self.ring,l) for l in Lp ];
            i = 0;
            for l in Lp:
                L.add( i, GenPolynomial(self.ring,l) );
                i += 1;
        N = [ RingElem(e) for e in L ];
        return N;

    def one(self):
        '''Get the one of the solvable polynomial ring.
        '''
        return RingElem( self.ring.getONE() );

    def zero(self):
        '''Get the zero of the solvable polynomial ring.
        '''
        return RingElem( self.ring.getZERO() );


class SolvableIdeal:
    '''Represents a JAS solvable polynomial ideal.

    Methods for left, right two-sided Groebner basees and others.
    '''

    def __init__(self,ring,ringstr="",list=None):
        '''Constructor for an ideal in a solvable polynomial ring.
        '''
        self.ring = ring;
        if list == None:
           sr = StringReader( ringstr );
           tok = GenPolynomialTokenizer(ring.ring,sr);
           self.list = tok.nextSolvablePolynomialList();
        else:
           self.list = pylist2arraylist(list);
        self.pset = OrderedPolynomialList(ring.ring,self.list);

    def __str__(self):
        '''Create a string representation.
        '''
        return str(self.pset);

    def leftGB(self):
        '''Compute a left Groebner base.
        '''
        s = self.pset;
        F = s.list;
        t = System.currentTimeMillis();
        G = SolvableGroebnerBaseSeq().leftGB(F);
        t = System.currentTimeMillis() - t;
        print "executed leftGB in %s ms" % t; 
        return SolvableIdeal(self.ring,"",G);

    def isLeftGB(self):
        '''Test if this is a left Groebner base.
        '''
        s = self.pset;
        F = s.list;
        t = System.currentTimeMillis();
        b = SolvableGroebnerBaseSeq().isLeftGB(F);
        t = System.currentTimeMillis() - t;
        print "isLeftGB executed in %s ms" % t; 
        return b;

    def twosidedGB(self):
        '''Compute a two-sided Groebner base.
        '''
        s = self.pset;
        F = s.list;
        t = System.currentTimeMillis();
        G = SolvableGroebnerBaseSeq().twosidedGB(F);
        t = System.currentTimeMillis() - t;
        print "executed twosidedGB in %s ms" % t; 
        return SolvableIdeal(self.ring,"",G);

    def isTwosidedGB(self):
        '''Test if this is a two-sided Groebner base.
        '''
        s = self.pset;
        F = s.list;
        t = System.currentTimeMillis();
        b = SolvableGroebnerBaseSeq().isTwosidedGB(F);
        t = System.currentTimeMillis() - t;
        print "isTwosidedGB executed in %s ms" % t; 
        return b;

    def rightGB(self):
        '''Compute a right Groebner base.
        '''
        s = self.pset;
        F = s.list;
        t = System.currentTimeMillis();
        G = SolvableGroebnerBaseSeq().rightGB(F);
        t = System.currentTimeMillis() - t;
        print "executed rightGB in %s ms" % t; 
        return SolvableIdeal(self.ring,"",G);

    def isRightGB(self):
        '''Test if this is a right Groebner base.
        '''
        s = self.pset;
        F = s.list;
        t = System.currentTimeMillis();
        b = SolvableGroebnerBaseSeq().isRightGB(F);
        t = System.currentTimeMillis() - t;
        print "isRightGB executed in %s ms" % t; 
        return b;

    def intersect(self,ring):
        '''Compute the intersection of this and the polynomial ring.
        '''
        s = jas.application.SolvableIdeal(self.pset);
        N = s.intersect(ring.ring);
        return SolvableIdeal(self.ring,"",N.getList());

    def sum(self,other):
        '''Compute the sum of this and the other ideal.
        '''
        s = jas.application.SolvableIdeal(self.pset);
        t = jas.application.SolvableIdeal(other.pset);
        N = s.sum( t );
        return SolvableIdeal(self.ring,"",N.getList());

    def parLeftGB(self,th):
        '''Compute a left Groebner base in parallel.
        '''
        s = self.pset;
        F = s.list;
        bbpar = SolvableGroebnerBaseParallel(th);
        t = System.currentTimeMillis();
        G = bbpar.leftGB(F);
        t = System.currentTimeMillis() - t;
        bbpar.terminate();
        print "parallel %s leftGB executed in %s ms" % (th, t); 
        return Ideal(self.ring,"",G);

    def parTwosidedGB(self,th):
        '''Compute a two-sided Groebner base in parallel.
        '''
        s = self.pset;
        F = s.list;
        bbpar = SolvableGroebnerBaseParallel(th);
        t = System.currentTimeMillis();
        G = bbpar.twosidedGB(F);
        t = System.currentTimeMillis() - t;
        bbpar.terminate();
        print "parallel %s twosidedGB executed in %s ms" % (th, t); 
        return Ideal(self.ring,"",G);


class Module:
    '''Represents a JAS module over a polynomial ring.

    Method to create sub-modules.
    '''

    def __init__(self,modstr="",ring=None):
        '''Module constructor.
        '''
        if ring == None:
           sr = StringReader( modstr );
           tok = GenPolynomialTokenizer(sr);
           self.mset = tok.nextSubModuleSet();
        else:
           self.mset = ModuleList(ring,None);
        self.ring = self.mset.ring;

    def __str__(self):
        '''Create a string representation.
        '''
        return str(self.mset);

    def submodul(self,modstr="",list=None):
        '''Create a sub-module.
        '''
        return Submodule(self,modstr,list);


class SubModule:
    '''Represents a JAS sub-module over a polynomial ring.

    Methods to compute Groebner bases.
    '''

    def __init__(self,module,modstr="",list=None):
        '''Constructor for a sub-module.
        '''
        self.module = module;
        if list == None:
           sr = StringReader( modstr );
           tok = GenPolynomialTokenizer(module.ring,sr);
           self.list = tok.nextSubModuleList();
        else:
           self.list = pylist2arraylist(list);
        self.mset = OrderedModuleList(module.ring,self.list);
        self.cols = self.mset.cols;
        self.rows = self.mset.rows;
        #print "cols = %s" % self.cols;
        #self.pset = self.mset.getPolynomialList();

    def __str__(self):
        '''Create a string representation.
        '''
        return str(self.mset); # + "\n\n" + str(self.pset);

    def GB(self):
        '''Compute a Groebner base.
        '''
        t = System.currentTimeMillis();
        G = ModGroebnerBaseAbstract().GB(self.mset);
        t = System.currentTimeMillis() - t;
        print "executed module GB in %s ms" % t; 
        return SubModule(self.module,"",G.list);

    def isGB(self):
        '''Test if this is a Groebner base.
        '''
        t = System.currentTimeMillis();
        b = ModGroebnerBaseAbstract().isGB(self.mset);
        t = System.currentTimeMillis() - t;
        print "module isGB executed in %s ms" % t; 
        return b;


class SolvableModule:
    '''Represents a JAS module over a solvable polynomial ring.

    Method to create solvable sub-modules.
    '''

    def __init__(self,modstr="",ring=None):
        '''Solvable module constructor.
        '''
        if ring == None:
           sr = StringReader( modstr );
           tok = GenPolynomialTokenizer(sr);
           self.mset = tok.nextSolvableSubModuleSet();
        else:
           self.mset = ModuleList(ring,None);
        self.ring = self.mset.ring;

    def __str__(self):
        '''Create a string representation.
        '''
        return str(self.mset);

    def solvsubmodul(self,modstr="",list=None):
        '''Create a solvable sub-module.
        '''
        return Submodule(self,modstr,list);


class SolvableSubModule:
    '''Represents a JAS sub-module over a solvable polynomial ring.

    Methods to compute left, right and two-sided Groebner bases.
    '''

    def __init__(self,module,modstr="",list=None):
        '''Constructor for sub-module over a solvable polynomial ring.
        '''
        self.module = module;
        if list == None:
           sr = StringReader( modstr );
           tok = GenPolynomialTokenizer(module.ring,sr);
           self.list = tok.nextSolvableSubModuleList();
        else:
           self.list = pylist2arraylist(list);
        self.mset = OrderedModuleList(module.ring,self.list);
        self.cols = self.mset.cols;
        self.rows = self.mset.rows;

    def __str__(self):
        '''Create a string representation.
        '''
        return str(self.mset); # + "\n\n" + str(self.pset);

    def leftGB(self):
        '''Compute a left Groebner base.
        '''
        t = System.currentTimeMillis();
        G = ModSolvableGroebnerBaseAbstract().leftGB(self.mset);
        t = System.currentTimeMillis() - t;
        print "executed left module GB in %s ms" % t; 
        return SolvableSubModule(self.module,"",G.list);

    def isLeftGB(self):
        '''Test if this is a left Groebner base.
        '''
        t = System.currentTimeMillis();
        b = ModSolvableGroebnerBaseAbstract().isLeftGB(self.mset);
        t = System.currentTimeMillis() - t;
        print "module isLeftGB executed in %s ms" % t; 
        return b;

    def twosidedGB(self):
        '''Compute a two-sided Groebner base.
        '''
        t = System.currentTimeMillis();
        G = ModSolvableGroebnerBaseAbstract().twosidedGB(self.mset);
        t = System.currentTimeMillis() - t;
        print "executed in %s ms" % t; 
        return SolvableSubModule(self.module,"",G.list);

    def isTwosidedGB(self):
        '''Test if this is a two-sided Groebner base.
        '''
        t = System.currentTimeMillis();
        b = ModSolvableGroebnerBaseAbstract().isTwosidedGB(self.mset);
        t = System.currentTimeMillis() - t;
        print "module isTwosidedGB executed in %s ms" % t; 
        return b;

    def rightGB(self):
        '''Compute a right Groebner base.
        '''
        t = System.currentTimeMillis();
        G = ModSolvableGroebnerBaseAbstract().rightGB(self.mset);
        t = System.currentTimeMillis() - t;
        print "executed module rightGB in %s ms" % t; 
        return SolvableSubModule(self.module,"",G.list);

    def isRightGB(self):
        '''Test if this is a right Groebner base.
        '''
        t = System.currentTimeMillis();
        b = ModSolvableGroebnerBaseAbstract().isRightGB(self.mset);
        t = System.currentTimeMillis() - t;
        print "module isRightGB executed in %s ms" % t; 
        return b;


class SeriesRing:
    '''Represents a JAS power series ring: UnivPowerSeriesRing.

    Methods for power series arithmetic.
    '''

    def __init__(self,ringstr="",truncate=None,ring=None,cofac=None,name="z"):
        '''Ring constructor.
        '''
        if ring == None:
            if len(ringstr) > 0:
                sr = StringReader( ringstr );
                tok = GenPolynomialTokenizer(sr);
                pset = tok.nextPolynomialSet();
                ring = pset.ring;
                vname = ring.vars;
                name = vname[0];
                cofac = ring.coFac;
            if isinstance(cofac,RingElem):
                cofac = cofac.elem;
            if truncate == None:
                self.ring = UnivPowerSeriesRing(cofac,name);
            else:
                self.ring = UnivPowerSeriesRing(cofac,truncate,name);
        else:
           self.ring = ring;

    def __str__(self):
        '''Create a string representation.
        '''
        return str(self.ring);

    def gens(self):
        '''Get the generator of the power series ring.
        '''
        return RingElem( self.ring.getONE().shift(1) );

    def one(self):
        '''Get the one of the power series ring.
        '''
        return RingElem( self.ring.getONE() );

    def zero(self):
        '''Get the zero of the power series ring.
        '''
        return RingElem( self.ring.getZERO() );

    def random(self,n):
        '''Get a random power series.
        '''
        return RingElem( self.ring.random(n) );

    def exp(self):
        '''Get the exponential power series.
        '''
        return RingElem( self.ring.getEXP() );

    def sin(self):
        '''Get the sinus power series.
        '''
        return RingElem( self.ring.getSIN() );

    def cos(self):
        '''Get the cosinus power series.
        '''
        return RingElem( self.ring.getCOS() );

    def tan(self):
        '''Get the tangens power series.
        '''
        return RingElem( self.ring.getTAN() );

    def create(self,ifunc=None,jfunc=None,clazz=None):
        '''Create a power series with given generating function.

        ifunc(int i) must return a value which is used in RingFactory.fromInteger().
        jfunc(int i) must return a value of type ring.coFac.
        clazz must implement the Coefficients abstract class.
        '''
        class coeff( Coefficients ):
            def __init__(self,cofac):
                self.coFac = cofac;
            def generate(self,i):
                if jfunc == None:
                    return self.coFac.fromInteger( ifunc(i) );
                else:
                    return jfunc(i);
        if clazz == None:
            ps = UnivPowerSeries( self.ring, coeff(self.ring.coFac) );
        else:
            ps = UnivPowerSeries( self.ring, clazz );
        return RingElem( ps );

    def fixPoint(self,psmap):
        '''Create a power series as fixed point of the given mapping.

        psmap must implement the PowerSeriesMap interface.
        '''
        ps = self.ring.fixPoint( psmap );
        return RingElem( ps );

    def gcd(self,a,b):
        '''Compute the greatest common divisor of a and b.
        '''
        if isinstance(a,RingElem):
            a = a.elem;
        if isinstance(b,RingElem):
            b = b.elem;
        return RingElem( a.gcd(b) );

    def from(self,a):
        '''Convert a GenPolynomial to a power series.
        '''
        if isinstance(a,RingElem):
            a = a.elem;
        return RingElem( self.ring.fromPolynomial(a) );


def pylist2arraylist(list):
    '''Convert a Python list to a Java ArrayList.

    If list is a Python list, it is converted, else list is left unchanged.
    '''
    #print "list type(%s) = %s" % (list,type(list));
    if isinstance(list,PyList):
       L = ArrayList();
       for e in list:
           if isinstance(e,RingElem):
               L.add( e.elem );
           else:
               L.add( e );
       list = L;
    #print "list type(%s) = %s" % (list,type(list));
    return list


def makeJasArith(item):
    '''Construct a jas.arith object.
    If item is a python tuple or list then a BigRational, BigComplex is constructed. 
    If item is a python float then a BigDecimal is constructed. 
    '''
    #print "item type(%s) = %s" % (item,type(item));
    if isinstance(item,PyInteger) or isinstance(item,PyLong):
        return BigInteger( item );
    if isinstance(item,PyFloat): # ?? what to do ??
        return BigDecimal( str(item) );
    if isinstance(item,PyTuple) or isinstance(item,PyList):
        if len(item) > 2:
            print "len(item) > 2, remaining items ignored";
        #print "item[0] type(%s) = %s" % (item[0],type(item[0]));
        isc = isinstance(item[0],PyTuple) or isinstance(item[0],PyList)
        if len(item) > 1:
            isc = isc or isinstance(item[1],PyTuple) or isinstance(item[1],PyList);
        if isc:
            if len(item) > 1:
                re = makeJasArith( item[0] );
                if not re.isField():
                    re = BigRational( re.val );
                im = makeJasArith( item[1] );
                if not im.isField():
                    im = BigRational( im.val );
                jasArith = BigComplex( re, im );
            else:
                re = makeJasArith( item[0] );
                jasArith = BigComplex( re );
        else:
            if len(item) > 1:
                jasArith = BigRational( item[0], item[1] );
            else:
                jasArith = BigRational( item[0] );
        return jasArith;
    print "unknown item type(%s) = %s" % (item,type(item));
    return item;


def QQ(d=0,n=1):
    '''Create JAS BigRational as ring element.
    '''
    if isinstance(d,PyTuple) or isinstance(d,PyList):
        if n != 1:
            print "%s ignored" % n;
        if len(d) > 1:
            n = d[1];
        d = d[0];
    if isinstance(d,RingElem):
        d = d.elem;
    if isinstance(n,RingElem):
        n = n.elem;
    if n == 1:
        if d == 0:
            r = BigRational();
        else:
            r = BigRational(d);
    else:
        r = BigRational(d,n);
    return RingElem(r);


def RF(d,n=1):
    '''Create JAS rational function Quotient as ring element.
    '''
    if isinstance(d,PyTuple) or isinstance(d,PyList):
        if n != 1:
            print "%s ignored" % n;
        if len(d) > 1:
            n = d[1];
        d = d[0];
    if isinstance(d,RingElem):
        d = d.elem;
    if isinstance(n,RingElem):
        n = n.elem;
    qr = QuotientRing(d.ring);
    if n == 1:
        r = Quotient(qr,d);
    else:
        r = Quotient(qr,d,n);
    return RingElem(r);


def CC(re=BigRational(),im=BigRational()):
    '''Create JAS BigComplex as ring element.
    '''
    if isinstance(re,PyTuple) or isinstance(re,PyList):
        if isinstance(re[0],PyTuple) or isinstance(re[0],PyList):
            if len(re) > 1:
                im = QQ( re[1] );
            re = QQ( re[0] );
        else:
            re = QQ(re);
#        re = makeJasArith( re );
    if isinstance(im,PyTuple) or isinstance(im,PyList):
        im = QQ( im );
#        im = makeJasArith( im );
    if isinstance(re,RingElem):
        re = re.elem;
    if isinstance(im,RingElem):
        im = im.elem;
    if im.isZERO():
        if re.isZERO():
            c = BigComplex();
        else:
            c = BigComplex(re);
    else:
        c = BigComplex(re,im);
    return RingElem(c);


def DD(d=0):
    '''Create JAS BigDecimal as ring element.
    '''
    if isinstance(d,RingElem):
        d = d.elem;
    if isinstance(d,PyFloat):
        d = str(d);
    #print "d type(%s) = %s" % (d,type(d));
    if d == 0:
       r = BigDecimal();
    else:
       r = BigDecimal(d);
    return RingElem(r);


def coercePair(a,b):
    '''Coerce type a to type b or type b to type a.
    '''
    if not a.isPolynomial() and b.isPolynomial():
        s = b.coerce(a);
        o = b;
    else:
        s = a;
        o = a.coerce(b);
    return [s,o];
    

class RingElem:
    '''Proxy for JAS ring elements.

    Methods to be used as + - * ** / %.
    '''

    def __init__(self,elem):
        '''Constructor for ring element.
        '''
        if isinstance(elem,RingElem):
            self.elem = elem.elem;
        else:
            self.elem = elem;
        try:
            self.ring = self.elem.ring;
        except:
            self.ring = self.elem;

    def __str__(self):
        '''Create a string representation.
        '''
        return str(self.elem); 

    def zero(self):
        '''Zero element of this ring.
        '''
        if self.isFactory():
            return RingElem( self.elem.getZERO() );
        else:
            return RingElem( self.elem.ring.getZERO() );

    def isZERO(self):
        '''Test if this is the zero element of the ring.
        '''
        return self.elem.isZERO();

    def one(self):
        '''One element of this ring.
        '''
        if self.isFactory():
            return RingElem( self.elem.getONE() );
        else:
            return RingElem( self.elem.ring.getONE() );

    def isONE(self):
        '''Test if this is the one element of the ring.
        '''
        return self.elem.isONE();

    def __abs__(self):
        '''Absolute value.
        '''
        return RingElem( self.elem.abs() ); 

    def __neg__(self):
        '''Negative value.
        '''
        return RingElem( self.elem.negate() ); 

    def __pos__(self):
        '''Positive value.
        '''
        return self; 

    def coerce(self,other):
        '''Coerce other to self.
        '''
        #print "self  type(%s) = %s" % (self,type(self));
        #print "other type(%s) = %s" % (other,type(other));
        if isinstance(other,RingElem):
            if self.isPolynomial() and not other.isPolynomial():
                o = self.ring.parse( str(other) );
                return RingElem( o );
            return other;
        #print "--1";
        if isinstance(other,PyTuple) or isinstance(other,PyList):
            # assume BigRational or BigComplex
            # assume self will be compatible with them. todo: check this
            o = makeJasArith(other);
            return RingElem(o);
        #print "--2";
        # test if self.elem is a factory itself
        if self.isFactory():
            if isinstance(other,PyInteger) or isinstance(other,PyLong):
                o = self.elem.fromInteger(other);
            else:
                if isinstance(other,PyFloat): # ?? what to do ??
                    o = self.elem.fromInteger( int(other) );
                else:
                    print "unknown other type(%s) = %s" % (other,type(other));
                    o = other;
            return RingElem(o);
        #print "--3";
        # self.elem has a ring factory
        if isinstance(other,PyInteger) or isinstance(other,PyLong):
            o = self.elem.ring.fromInteger(other);
        else:
            if isinstance(other,PyFloat): # ?? what to do ??
                o = self.elem.ring.fromInteger( int(other) );
            else:
                print "unknown other type(%s) = %s" % (other,type(other));
                o = other;
        #print "--4";
        return RingElem(o);

    def isFactory(self):
        '''Test if this is itself a ring factory.
        '''
        try:
            r = self.elem.ring;
        except:
            return True;
        return False;

    def isPolynomial(self):
        '''Test if this is a polynomial.
        '''
        try:
            nv = self.elem.ring.nvar;
        except:
            return False;
        return True;
##         if isinstance(self.elem,GenPolynomial): # does not work
##             return True;
##         else:
##             return False;

    def __cmp__(self,other):
        '''Compare two ring elements.
        '''
        [s,o] = coercePair(self,other);
        return s.elem.compareTo( o.elem ); 

    def __mul__(self,other):
        '''Multiply two ring elements.
        '''
        [s,o] = coercePair(self,other);
        return RingElem( s.elem.multiply( o.elem ) ); 

    def __rmul__(self,other):
        '''Reversw multiply two ring elements.
        '''
        [s,o] = coercePair(self,other);
        return o.__mul__(self);

    def __add__(self,other):
        '''Add two ring elements.
        '''
        [s,o] = coercePair(self,other);
        return RingElem( s.elem.sum( o.elem ) ); 

    def __radd__(self,other):
        '''Add two ring elements.
        '''
        [s,o] = coercePair(self,other);
        return o.__add__(self);

    def __sub__(self,other):
        '''Subtract two ring elements.
        '''
        [s,o] = coercePair(self,other);
        return RingElem( s.elem.subtract( o.elem ) ); 

    def __rsub__(self,other):
        '''Subtract two ring elements.
        '''
        [s,o] = coercePair(self,other);
        return o.__sub__(self);

    def __div__(self,other):
        '''Divide two ring elements.
        '''
        [s,o] = coercePair(self,other);
        return RingElem( s.elem.divide( o.elem ) ); 

    def __mod__(self,other):
        '''Modular remainder of two ring elements.
        '''
        [s,o] = coercePair(self,other);
        return RingElem( s.elem.remainder( o.elem ) ); 

    def __xor__(self,other):
        '''Can not be used as power.
        '''
        return None;

    def __pow__(self,other):
        '''Power of this to other.
        '''
        #print "self  type(%s) = %s" % (self,type(self));
        #print "pow other type(%s) = %s" % (other,type(other));
        if isinstance(other,PyInteger):
            n = other;
        else:
            if isinstance(other,RingElem): 
                n = other.elem;
                if isinstance(n,BigRational): # does not work
                    n = n.numerator().intValue();
                if isinstance(n,BigInteger):  # does not work
                    n = n.intValue();
        if self.isFactory():
            p = Power(self.elem).power( self.elem, n );
        else:
            p = Power(self.elem.ring).power( self.elem, n );
        return RingElem( p ); 

    def evaluate(self,a):
        '''Evaluate at a for power series.
        '''
        #print "self  type(%s) = %s" % (self,type(self));
        #print "a     type(%s) = %s" % (a,type(a));
        x = None;
        if isinstance(a,RingElem):
            x = a.elem;
        if isinstance(a,PyTuple) or isinstance(a,PyList):
            # assume BigRational or BigComplex
            # assume self will be compatible with them. todo: check this
            x = makeJasArith(a);
        try:
            e = self.elem.evaluate(x);
        except:
            e = None;            
        return RingElem( e );

    def integrate(self,a):
        '''Integrate a power series with constant a.
        '''
        #print "self  type(%s) = %s" % (self,type(self));
        #print "a     type(%s) = %s" % (a,type(a));
        x = None;
        if isinstance(a,RingElem):
            x = a.elem;
        if isinstance(a,PyTuple) or isinstance(a,PyList):
            # assume BigRational or BigComplex
            # assume self will be compatible with them. todo: check this
            x = makeJasArith(a);
        try:
            e = self.elem.integrate(x);
        except:
            e = None;            
        return RingElem( e );

    def differentiate(self):
        '''Differentiate a power series.
        '''
        try:
            e = self.elem.differentiate();
        except:
            e = None;            
        return RingElem( e );
