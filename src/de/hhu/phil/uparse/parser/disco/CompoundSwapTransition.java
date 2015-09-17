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

public class CompoundSwapTransition extends DiscoTransition implements Serializable {
	
	private int size = 0;

	public State extend(State state, double scoreDelta) {
		LinkedList<Tree> stack = new LinkedList<>(state.stack);
		LinkedList<Integer> todo = new LinkedList<>(state.todo);
		for (int i = 1; i <= size; ++i) {
			Tree swapped = stack.remove(1);
			todo.addFirst(swapped.nodeNumber() - 1);
		}
		LinkedList<DiscoTransition> trans = new LinkedList<>(state.transitions);
		trans.push(this);
		return new State(stack, trans, state.sentence, todo, state.score + scoreDelta, false);
	}

	public boolean isLegal(State state, Grammar g) {
		if (state.todo.isEmpty()) {
			return false;
		}
		if (state.complete) {
			return false;
		}
		if (state.stack.size() < 2 + size) {
			return false;
		}
		for (int i = 1; i <= size; ++i) {
			if (!state.stack.get(i).isPreTerminal()) {
				return false;
			}
			if (state.stack.get(i).nodeNumber() > state.stack.get(0).nodeNumber()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + size;
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
		CompoundSwapTransition other = (CompoundSwapTransition) obj;
		if (size != other.size)
			return false;
		return true;
	}

	public String toString() {
		return "CSWAP-" + String.valueOf(size);
	}

	public void grow() {
		++size;
	}

	private static final long serialVersionUID = 8846368737894172872L;
	
}
