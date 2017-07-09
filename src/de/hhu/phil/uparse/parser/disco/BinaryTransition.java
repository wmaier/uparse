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
import de.hhu.phil.uparse.treebank.bin.HeadSide;

public class BinaryTransition extends DiscoTransition implements Serializable  {
	
	private static final long serialVersionUID = -2889868028136350874L;

	private String label;
	
	private HeadSide headSide;
	
	public BinaryTransition(String label, HeadSide headSide) {
		this.label = label;
		this.headSide = headSide;
	}
	
	public HeadSide getSide() {
		return headSide;
	}
	
	@Override
	public State extend(State state, double scoreDelta) {
		LinkedList<Tree> stack = new LinkedList<>(state.stack);
	    Tree right = stack.get(0);
	    Tree left = stack.get(state.splitPoint);
	    stack.remove(state.splitPoint);
	    stack.pop();

	    Tree binaryNode = new Tree(new NodeLabel(label));
	    binaryNode.addChild(left);
	    binaryNode.addChild(right);
	    stack.push(binaryNode);
	    
	    if (headSide == HeadSide.LEFT) {
	    	left.setIsHead(true);
	    	binaryNode.setLexicalHead(left);
	    } else if (headSide == HeadSide.RIGHT) {
	    	right.setIsHead(true);
	    	binaryNode.setLexicalHead(right);
	    } else {
	    	throw new IllegalStateException("no head side in binary transition");
	    }
	    
	    LinkedList<DiscoTransition> trans = new LinkedList<DiscoTransition>(state.transitions);
	    trans.push(this);
	    
	    return new State(stack, trans, state.sentence, new LinkedList<>(state.todo), 
	    		state.score + scoreDelta, false, state.lastShiftDist, 1);    
	}
	
	public boolean isLegal(State state, Grammar g) {
		if (state.stack.size() > 0) {
			if (state.stack.get(0).getLabel().label.equals(Constants.DEFAULT_ROOT)) {
				return false;
			}
		}
		if (!state.todo.isEmpty() && label.equals(Constants.DEFAULT_ROOT)) {
			return false;
		}
		if (state.complete) {
			return false;
		}
		//if (state.stack.size() < 2) {
		if (state.stack.size() <= state.splitPoint) {
			return false;
		} else {
			Tree r = state.stack.get(0);
			//Tree l = state.stack.get(1);
			Tree l = state.stack.get(state.splitPoint);
			String fullLabel = label.startsWith("@") ? label.substring(1) : label;
			if (r.getLabel().label.startsWith("@")
					&& l.getLabel().label.startsWith("@")) {
				return false;
			}
			if (r.getLabel().label.startsWith("@")) {
				if (!(r.getLabel().label.substring(1).equals(fullLabel))) {
					return false;
				}
				if (headSide != HeadSide.RIGHT) {
					return false;
				}
			}
			if (l.getLabel().label.startsWith("@")) { 
				if (!(l.getLabel().label.substring(1).equals(fullLabel))) {
					return false;
				}
				if (headSide != HeadSide.LEFT) {
					return false;
				}
			}
			if (state.stack.size() == 2) {
				if (label.startsWith("@")) {
					if (state.todo.isEmpty()) {
						return false;
					}
					if (headSide != HeadSide.LEFT) {
						return false;
					}
				}
			} else {
				// stack size > 2
				Tree third = state.stack.get(2);
				if (third.getLabel().label.startsWith("@")) {
					if (label.startsWith("@")) {
						if (state.todo.isEmpty()) {
							return false;
						}
						if (headSide != HeadSide.LEFT) {
							return false;
						}
					}
				}
			}
			String llabel = l.getLabel().label;
			String rlabel = r.getLabel().label;
			if (!(g.isBinaryCompletion(label, llabel, rlabel))) {
				return false;
			}
			
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((headSide == null) ? 0 : headSide.hashCode());
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
		BinaryTransition other = (BinaryTransition) obj;
		if (headSide != other.headSide)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	public String toString() {
		return "B-" + headSide + "-" + label;
	}

}
