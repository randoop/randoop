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
 * @version	$Id: CompoundPlan.java,v 1.1.1.1 2007-05-30 23:24:30 cpacheco Exp $
 */
public interface CompoundPlan {
	
	public List<BlockStatement> getBlockStmts();

	public PlanForLocalID getName();
}
