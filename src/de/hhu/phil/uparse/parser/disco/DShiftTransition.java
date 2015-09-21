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
import java.util.List;

import de.hhu.phil.uparse.treebank.Grammar;
import de.hhu.phil.uparse.treebank.NodeLabel;
import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.treebank.bin.HeadSide;

public class DShiftTransition extends DiscoTransition implements Serializable {
	
	private int dist;
	
	public DShiftTransition(int dist) {
		this.dist = dist;
	}

	public State extend(State state, double scoreDelta) {
		NodeLabel preTermLabel = state.sentence.get(state.todo.get(dist)).getLabel();
		NodeLabel newLabel = new NodeLabel(preTermLabel.label);
		Tree preTerminalNode = new Tree(newLabel);
		NodeLabel newTerminalLabel = new NodeLabel(state.sentence.get(state.todo.get(dist)).children().get(0).getLabel().label);
		Tree terminalNode = new Tree(newTerminalLabel);
		preTerminalNode.addChild(terminalNode);
		preTerminalNode.setNodeNumber(state.sentence.get(state.todo.get(dist)).nodeNumber());
		terminalNode.setNodeNumber(state.sentence.get(state.todo.get(dist)).nodeNumber());
		terminalNode.setIsHead(true);
		LinkedList<Tree> stack = new LinkedList<>(state.stack);
		stack.push(preTerminalNode);
		LinkedList<DiscoTransition> trans = new LinkedList<>(state.transitions);
		trans.push(this);
		List<Integer> newtodo = new LinkedList<>(state.todo);
		newtodo.remove(dist);
		return new State(stack, trans, state.sentence, newtodo, state.score + scoreDelta, false, dist);
	}

	public boolean isLegal(State state, Grammar g) {
		if (state.todo.isEmpty()) {
			return false;
		}
		if (state.complete) {
			return false;
		}
		if (this.dist >= state.todo.size()) {
			return false;
		}
		if (state.stack.size() > 0 && state.transitions.size() > 0
				&& state.stack.peek().getLabel().label.startsWith("@")
				&& state.transitions.peek() instanceof BinaryTransition
				&& ((BinaryTransition) state.transitions.peek()).getSide()
					== HeadSide.RIGHT) {
			return false;
		}
		return true;
	}
		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dist;
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
		DShiftTransition other = (DShiftTransition) obj;
		if (dist != other.dist)
			return false;
		return true;
	}

	public String toString() {
		return "SHIFT";
	}
	
	private static final long serialVersionUID = 8846368737894172872L;
	
}
