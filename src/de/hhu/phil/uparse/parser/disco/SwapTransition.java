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

import de.hhu.phil.uparse.treebank.Grammar;
import de.hhu.phil.uparse.treebank.Tree;

public class SwapTransition extends DiscoTransition implements Serializable {
	
	public State extend(State state, double scoreDelta) {
		LinkedList<Tree> stack = new LinkedList<>(state.stack);
		Tree swapped = stack.remove(1);
		LinkedList<DiscoTransition> trans = new LinkedList<>(state.transitions);
		trans.push(this);
		LinkedList<Integer> todo = new LinkedList<>(state.todo);
		todo.addFirst(swapped.nodeNumber() - 1);
		return new State(stack, trans, state.sentence, todo, state.score + scoreDelta, false);
	}

	public boolean isLegal(State state, Grammar g) {
		if (state.todo.isEmpty()) {
			return false;
		}
		if (state.complete) {
			return false;
		}
		if (state.stack.size() < 3) {
			return false;
		}
		if (!state.stack.get(1).isPreTerminal()) {
			return false;
		}
		if (state.stack.get(1).nodeNumber() > state.stack.get(0).nodeNumber()) {
			return false;
		}
		return true;
	}
		
	@Override
	public int hashCode() {
		// random prime
		return 15486041;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	public String toString() {
		return "SWAP";
	}

	private static final long serialVersionUID = 8846368737894172872L;
	
}
