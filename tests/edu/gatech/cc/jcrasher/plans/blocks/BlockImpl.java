/*
 * BlockImpl.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans.blocks;

import java.lang.reflect.Member;

import edu.gatech.cc.jcrasher.Constants;
import edu.gatech.cc.jcrasher.plans.PlanForLocalID;

/**
 * BlockImpl
 * 
 * ADT hiding a code block intended to crash some method or constructor
 * - some statements to generate needed instances
 * - some statements to invoke methods/ constructors under testclasses
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2005-08-29 Remove redundant package names from generated source code
 */
public class BlockImpl implements Block {
	
	/* method or constructor this block is intended to execute. */
	protected Member testee = null;
	
	/* Block ::= {BlockStatement*} 
	 * Sequence of statements, intended to crash a method/ constructor */
	protected BlockStatement[] blockStmts = null;
	
	/* Strictly monotonic increasing counter of local identifiers */
	protected int localIDcount = 0;
	
	
	/**
	 * Constructor
	 */
	public BlockImpl(Member m) {
		assert m!=null;
		testee = m;
	}
	
	public Member getTestee() {
		return testee;
	}
	
	/**
	 * setty
	 * 
	 * @param pBlockStmts (empty) list, but never null
	 */
	public void setBlockStmts(BlockStatement[] pBlockStmts) {
		assert pBlockStmts != null;
		
		blockStmts = pBlockStmts;
	}
	

	
	/**
	 * To be called from above, like CodeWriter
	 *
	 * @param pIdent ident on which to put block closing bracket
	 * 			opening bracket is appended on line of parent construct
	 * @return a specialized representation of the testclasses block like:
	 * 
	 * 	<bla-construct> {
	 * 		A.m(null);
	 * 	}
	 */
	public String toString(String pIdent, Class<?> pClass) {
		StringBuffer sb = new StringBuffer(" {");
		
		/* Get sequence of stmt strings */
		for (BlockStatement blockStmt: blockStmts) {
			sb.append(Constants.LS +pIdent);
			sb.append("\t" +blockStmt.toString(pClass));
		}		
		sb.append(Constants.LS +pIdent +"}");
		
		return sb.toString();
		//TODO make context (current package) known in testclasses case
		//			so its element can strip the package name themselves
	}
	
	
	/**
	 * To be called from below, like BlockStatement
	 * 
	 * @return an unused local identifier encoding pClass, e.g.
	 *         (i1, s2, i3) for a sequence like: (int, String[], Integer)
	 */
	public PlanForLocalID getNextID(Class<?> pClass) {
		assert pClass != null;
		PlanForLocalID res = null;
		localIDcount += 1;  //new id
		
		if (pClass.equals(Void.class)) {  //added 2004-08-03
			return new PlanForLocalID(pClass, "<void" +Integer.toString(localIDcount) +">");
		}
		
		/* Leaf type represents array */
		Class<?> leafType = pClass;
  		while (leafType.isArray() == true) {
			leafType = leafType.getComponentType();
		}
		
		/* simple name (of component type) */
		String leafName = leafType.getName();
		
		if (leafType.isPrimitive() == false) {
			/* Remove "p.Enc$" from "Enc$Nested" in class "p.Enc.Nested" */
			if (leafType.getDeclaringClass() != null) {
				String enclosingName = leafType.getDeclaringClass().getName();	//Enc
				leafName = leafName.substring(enclosingName.length()+1, leafName.length());
			}
			
			/* top level class */
			else {
				/* Extract C from p.q.C */	
				String[] leafNameParts = leafName.split("\\.");
				leafName = leafNameParts[leafNameParts.length-1];
			}
		}
				
		res = new PlanForLocalID(pClass,
			leafName.toLowerCase().charAt(0) +Integer.toString(localIDcount));
		
		assert res != null;
		return res;		
	}
}
