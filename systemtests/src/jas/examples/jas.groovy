/*
 * groovy interface to jas.
 * $Id: $
 */

package edu.jas.groovy

import java.lang.System
import java.io.StringReader

import edu.jas.structure.*
import edu.jas.arith.*
import edu.jas.poly.*
import edu.jas.ring.*
import edu.jas.module.*
import edu.jas.vector.*
import edu.jas.application.*
import edu.jas.util.*
import edu.jas.ufd.*
//import edu.jas.*
//import edu.*
//PrettyPrint.setInternal();
import edu.jas.kern.ComputerThreads;

import org.apache.log4j.BasicConfigurator;

//t = new ComputerThreads()
//println "t = " + t

//p = t.getPool()
//println "p = " + p

//l = new BasicConfigurator()
//println "l = " + l
//l.configure()

def startLog = {
    BasicConfigurator.configure();
}

//startLog();

def terminate = {
    ComputerThreads.terminate();
}


class Ring {

    def ring;
    def pset;

    def Ring(ringstr="",ring=null) {
        if ( ring == null ) {
           def sr = new StringReader( ringstr );
           def tok = new GenPolynomialTokenizer(sr);
           this.pset = tok.nextPolynomialSet();
           this.ring = this.pset.ring;
        } else {
           this.ring = ring;
        }
    }

    String toString() {
        return ring.toString();
    }

    def ideal(ringstr="",list=null) {
        return new Ideal(this,ringstr,list);
    }
}



class Ideal {

    def ring;
    def list;
    def pset;

    def Ideal(ring,ringstr="",list=null) {
        this.ring = ring;
        if ( list == null ) {
           def sr = new StringReader( ringstr );
           def tok = new GenPolynomialTokenizer(ring.pset.ring,sr);
           this.list = tok.nextPolynomialList();
        } else {
           this.list = list;
        }
        this.pset = new OrderedPolynomialList(ring.ring,this.list);
    }

    String toString() {
        return pset.toString();
    }

    def GB() {
        def s = this.pset;
        def F = s.list;
        def t = System.currentTimeMillis();
        def G = new GroebnerBaseSeq().GB(F);
        t = System.currentTimeMillis() - t;
        println "sequential executed in ${t} ms"; 
        return new Ideal(this.ring,"",G);
    }

    def isGB() {
        def s = this.pset;
        def F = s.list;
        def t = System.currentTimeMillis();
        def b = new GroebnerBaseSeq().isGB(F);
        t = System.currentTimeMillis() - t;
        println "isGB executed in ${t} ms"; 
        return b;
    }

}
