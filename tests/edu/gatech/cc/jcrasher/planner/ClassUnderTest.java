/*
 * TypeNeededNode.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.planner;

import static edu.gatech.cc.jcrasher.planner.PlannerImpl.rules;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Vector;

import edu.gatech.cc.jcrasher.Constants.PlanFilter;
import edu.gatech.cc.jcrasher.plans.Plan;
import edu.gatech.cc.jcrasher.plans.PlanForConstructor;
import edu.gatech.cc.jcrasher.plans.PlanForLocalID;
import edu.gatech.cc.jcrasher.plans.PlanForMethod;
import edu.gatech.cc.jcrasher.plans.blocks.Block;
import edu.gatech.cc.jcrasher.plans.blocks.BlockImpl;
import edu.gatech.cc.jcrasher.plans.blocks.BlockStatement;
import edu.gatech.cc.jcrasher.plans.blocks.ExpressionStatement;
import edu.gatech.cc.jcrasher.plans.blocks.LocalVariableDeclarationStatement;

/**
 * TypeNeededNode
 * 
 * Constructs a TypeNode a loaded class under testclasses:
 * 	extract all public non-abstract methods and constructors
 * 	currently 1:1 mapping from function plan to block.
 *
 * Automatic Testing: 
 * Crash java classes by passing inconvenient params
 * 
 * Christoph Csallner
 * 2004-04-22 allowed access from ESC.
 */
public class ClassUnderTest extends TypeNode implements BlockNode {

	protected Class<?> wrappedClass = null;
	
	/**
	 * Constructor to be used from outside JCrasher---to just use
	 * the code-creation API.
	 */
	public ClassUnderTest () {
	}


	/**
	 * Constructor
	 * 
	 * Grab all functions under testclasses.
	 * 
	 * Precond: maxRecursion >0
	 * 
	 *  pCW contains all  values (and constructing functions)
	 * @param remainingRecursion maximal length of function chain to be appended
	 *  planFilter is the invoking function interested i.e. in null?
	 */
	public ClassUnderTest (Class<?> c, int remainingRecursion) {
		assert c != null;
		assert remainingRecursion >0;
		
		wrappedClass = c;
		
		/* collect sub plan spaces */
		List<FunctionNode> childSpaces = new Vector<FunctionNode>();

		/* Crash any declared public constructor iff class non-abstract */
		if (Modifier.isAbstract(c.getModifiers()) == false) {			
			for (Constructor con: c.getDeclaredConstructors()) {//all declared
				if (Modifier.isPublic(con.getModifiers())==true) {
					childSpaces.add(new ConstructorNode(con, remainingRecursion, PlanFilter.ALL));
				}
			}
		}
		
		/* Crash any declared public non-abstract method */
		for (Method meth: c.getDeclaredMethods()) {
			if ((Modifier.isAbstract(meth.getModifiers()) == false)
					&& (Modifier.isPublic(meth.getModifiers())==true)) {
				
				childSpaces.add(new MethodNode(meth, remainingRecursion, PlanFilter.ALL));	
			}
		}	
	
		/* set gathered child plan spaces in super class */
		setChildren(childSpaces.toArray(new FunctionNode[childSpaces.size()]));
	}
	
	
	protected Class<?> getWrappedClass() {
		return wrappedClass;
	}
	
	
	/**
	 * Retrieve block with given index from the underlying class's plan space.
	 * 
	 * Precond: 0 <= planIndex < getPlanSpaceSize()
	 * Postcond: no side-effects
	 */
	public Block getBlock(int planIndex) {
		Block res = null;
		
		/* retrieve function's childrens' plans of given index */
		int childIndex = getChildIndex(planIndex);
		int childPlanIndex = getChildPlanIndex(childIndex, planIndex);	//map to child plan space
		FunctionNode node = (FunctionNode) getChild(childIndex);		
		Plan[] plans = node.getChildrenPlans(childPlanIndex);	//get params' plans
		
		/* check of which kind the child is: hack */
		if (ConstructorNode.class.isAssignableFrom(node.getClass())) {
			ConstructorNode conNode = (ConstructorNode) node;
			res = getTestBlockForCon(conNode.getCon(), plans);
		}
		else {
			MethodNode methNode = (MethodNode) node;
			res = getTestBlockForMeth(methNode.getMeth(), plans);
		}
		
		assert res != null;
		return res;
	}
	
	
	
	/**
	 * Generalized planning creates a block 
	 * 	given a constructor and plans to invoke its needed types.
	 * - Declare needed instance variables and initialize them with chain-plans
	 * - Invoke constructor under testclasses on these local variables
	 * 
	 * 2004-04-22 changed to public to allow access from ESC extension.
	 */
	public Block getTestBlockForCon(Constructor pCon, Plan[] curPlans) {
		assert pCon != null;
		assert curPlans != null;
		
		Block b = new BlockImpl(pCon);	//context for this combination
		
		/* Simple version: one stmt for each instance and exec fct */				
		BlockStatement[] bs = new BlockStatement[curPlans.length+1];
		
		/* Keep track of new created local instances: all needed */
		PlanForLocalID[] ids = new PlanForLocalID[curPlans.length];
		Class<?>[] paramsTypes = pCon.getParameterTypes();	
		
		/* Generate local variable for each needed instance (-plan) */				
		for (int i=0; i<curPlans.length; i++) {					
			ids[i] = b.getNextID(paramsTypes[i]);	//A a = ...
			bs[i] = new LocalVariableDeclarationStatement(ids[i], curPlans[i]);
		}

		/*
		 * BlockStatement for constructor under testclasses
		 */			
		/* Generate identifer to execute the con under testclasses */
		PlanForLocalID returnID = b.getNextID(pCon.getDeclaringClass());	//A a = ...
		
		/* Generate conPlan for con under testclasses, which references all local identifiers */
		PlanForConstructor conPlan = null;
		if (rules.getWrapper(pCon.getDeclaringClass()).isInnerClass() == true) {	//first dim is enclosing instance
			Plan[] paramPlans = new Plan[curPlans.length-1];
			for (int j=0; j<paramPlans.length; j++) {
				paramPlans[j] = ids[j+1];
			}
			conPlan = new PlanForConstructor(pCon, paramPlans, ids[0]);
		}
		else {	//Non-inner class constructor with >=1 arguments
			conPlan = new PlanForConstructor(pCon, ids);
		}
		
		/* Last statement */
		bs[curPlans.length] = new LocalVariableDeclarationStatement(
			returnID, conPlan);	
			
		/* 
		 * Assemble and append generated block
		 */
		b.setBlockStmts(bs);
		return b;
	}
	
	
	/**
	 * Generalized planning creates a block 
	 * 	given a method and plans to invoke its needed types.
	 * - Declare needed instance variables and initialize them with chain-plans
	 * - Invoke method under testclasses on these local variables
	 * 
	 * 2004-04-22 changed to public to allow access from ESC extension.
	 */
	public Block getTestBlockForMeth(Method pMeth, Plan[] curPlans) {
		assert pMeth != null;
		assert curPlans != null;
		
		Block b = new BlockImpl(pMeth);	//context for this combination
		
		/* Simple version: one stmt for each instance and exec fct */				
		BlockStatement[] bs = new BlockStatement[curPlans.length+1];
		
		/* Keep track of new created local param-instances */
		Plan[] paramPlans = null;
		if (Modifier.isStatic(pMeth.getModifiers()) == false) {	//first dim is victim instance
			paramPlans = new Plan[curPlans.length-1];
			for (int j=0; j<paramPlans.length; j++) {
				paramPlans[j] = curPlans[j+1];
			}
		}
		else {
			paramPlans = curPlans;
		}
		
		Class<?>[] paramsTypes = pMeth.getParameterTypes();	
		PlanForLocalID[] paramIDs = new PlanForLocalID[paramPlans.length];
		
		/* Generate local variable for each needed param instance (-plan) */				
		for (int i=0; i<paramIDs.length; i++) {					
			paramIDs[i] = b.getNextID(paramsTypes[i]);	//A a = ...
			bs[i] = new LocalVariableDeclarationStatement(paramIDs[i], paramPlans[i]);
		}
		
		
		/*
		 * StatementExpressiong for method under testclasses
		 * - TODO: generate local var iff non-void meth
		 */							
		/* Generate conPlan for meth under testclasses, which references all param identifiers */
		PlanForMethod conPlan = null;				
		if (Modifier.isStatic(pMeth.getModifiers()) == false) {	//first dim is victim instance
			
			/* Generate local variable for victim */
			PlanForLocalID vID = b.getNextID(pMeth.getDeclaringClass());	//A a = ...
			bs[curPlans.length-1] = new LocalVariableDeclarationStatement(vID, curPlans[0]);
			
			conPlan = new PlanForMethod(pMeth, paramIDs, vID);
		}
		else {	//Static method with >=1 arguments
			conPlan = new PlanForMethod(pMeth, paramIDs);
		}
		
		/* Last statement */
		bs[curPlans.length] = new ExpressionStatement(conPlan);	

			
		/* Assemble and append generated block */
		b.setBlockStmts(bs);
		return b;	
	}	
}
