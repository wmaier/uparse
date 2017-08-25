/*
 *  This file is part of uparse.
 *  
 *  Copyright 2014, 2015 Wolfgang Maier 
 * 
 *  uparse is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  uparse is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with uparse.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.hhu.phil.uparse.parser.disco;

import java.io.Serializable;
import java.util.LinkedList;

import de.hhu.phil.uparse.treebank.Constants;
import de.hhu.phil.uparse.treebank.Grammar;
import de.hhu.phil.uparse.treebank.NodeLabel;
import de.hhu.phil.uparse.treebank.Tree;

public class UnaryTransition extends DiscoTransition implements Serializable {
	
	private String label;
	
	public UnaryTransition(String label) {
		this.label = label;
	}
	
	@Override
	public State extend(State state, double scoreDelta) {
		LinkedList<Tree> stack = new LinkedList<Tree>(state.stack);
		Tree top = stack.pop();
		NodeLabel unaryLabel = new NodeLabel(label);
		Tree unaryNode = new Tree(unaryLabel);
		top.setIsHead(true);
		unaryNode.addChild(top);
	    stack.push(unaryNode);
	    LinkedList<DiscoTransition> trans = new LinkedList<>(state.transitions);
	    trans.push(this);
	    return new State(stack, trans, state.sentence, new LinkedList<>(state.todo), 
	    		state.score + scoreDelta, false, state.lastShiftDist);    
	}
	
	public boolean isLegal(State state, Grammar g) {
		if (state.stack.size() > 0) {
			if (state.stack.get(0).getLabel().label.equals(Constants.DEFAULT_ROOT)) {
				return false;
			}
		}
		if (!(state.stack.size() == 1 && state.todo.isEmpty())
				&& label.equals(Constants.DEFAULT_ROOT)) {
			return false;
		}
		if (state.complete) {
			return false;
		}
		if (state.stack.isEmpty()) {
			return false;
		}
		if (state.stack.peek().getLabel().label.equals(label)) {
			return false;
		}
		if (state.stack.peek().getLabel().label.startsWith("@") 
				&& !label.equals(state.stack.peek().getLabel().label.substring(1))) {
			return false;
		}
		// no more than two subsequent unaries (as in Zhu et al. 2013)
		if (state.transitions.size() >= 2
			&& state.transitions.get(0) instanceof UnaryTransition
			&& state.transitions.get(1) instanceof UnaryTransition) {
				return false;
		}
		if (!(g.isUnaryCompletion(label, state.stack.peek().getLabel().label))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnaryTransition other = (UnaryTransition) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "U-" + label;
	}

	private static final long serialVersionUID = 6155957459460111613L;
	
}
