/*
 * CompoundPlan.java
 * 
 * Copyright 2004 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.plans;

import java.util.List;

import edu.gatech.cc.jcrasher.plans.blocks.BlockStatement;


/**
 * CompoundPlan--esentially a list of block statements.
 * 
 * @author		Christoph Csallner
 */
public interface CompoundPlan {
	
	public List<BlockStatement> getBlockStmts();

	public PlanForLocalID getName();
}
