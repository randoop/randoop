/*
 * LocalVariableDeclarationStatement.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans.blocks;

import randoop.Globals;
import edu.gatech.cc.jcrasher.JCrasher;
import edu.gatech.cc.jcrasher.plans.Plan;
import edu.gatech.cc.jcrasher.plans.PlanForLocalID;
import edu.gatech.cc.jcrasher.writer.CodeGenFct;

/**
 * LocalVariableDeclarationStatement
 * 
 * ADT hiding a code statement to generate a needed instance.
 * 
 * We need only a subset as follows:
 * 
 * LocalVariableDeclarationStatement ::= 
 * 		Type VariableDeclaratorId = VariableInitializer;
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2002-06-12 last changed
 */
public class LocalVariableDeclarationStatement implements BlockStatement {
	/* identifier should be unique in the current context
	 * if obtained via block.getNextID() */
	protected PlanForLocalID identifier = null;  //Type VariableDeclaratorId	
	protected Plan varInitPlan = null;  //VariableInitializer
	
	/**
	 * Constructor
	 */
	public LocalVariableDeclarationStatement(PlanForLocalID pID, Plan pPlan) {
		assert pID != null;
		assert pPlan != null;		
		Class<?> idType = pID.getReturnType();
		Class<?> planType = pPlan.getReturnType();
		assert idType.isAssignableFrom(planType)	// type compatibility
			|| (idType.isPrimitive() == true);
		
		identifier = pID;
		varInitPlan = pPlan;
	}
	
	
	/**
	 * @return a specialized representation of the statement like 
	 * - A a = new A(null);
	 * - B b = a.m(0);
	 */
	public String toString(Class<?> testee) {
		String res = null;
		String className = CodeGenFct.getName(identifier.getReturnType(), testee);
		
		res = 
			className +" " +identifier.toString(testee) +" = " +varInitPlan.toString(testee) +";";
        
		if (JCrasher.jcrasherSupressTestsThatUseNull) {
		    if (!identifier.getReturnType().isPrimitive()) {
		        res = res + Globals.lineSep + "if (" + identifier.toString(testee) + " == null) return;";
		    }
		}
				
		assert res != null;
		return res;
	}
}