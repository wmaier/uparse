/*
 *  This file is part of uparse.
 *  
 *  Copyright 2017 Wolfgang Maier 
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

import java.util.LinkedList;
import java.util.List;

import de.hhu.phil.uparse.treebank.Grammar;
import de.hhu.phil.uparse.treebank.Tree;

/**
 * Gap transition from Coavoux & Crabbe (2017)
 * @author wmaier
 *
 */
public class CoavouxGapTransition extends DiscoTransition {

	@Override
	public State extend(State state, double scoreDelta) {
		LinkedList<Tree> stack = new LinkedList<>(state.stack);
		LinkedList<DiscoTransition> trans = new LinkedList<>(state.transitions);
		trans.push(this);
		List<Integer> newtodo = new LinkedList<>(state.todo);
		return new State(stack, trans, state.sentence, newtodo, state.score + scoreDelta, false, 
				state.lastShiftDist, state.splitPoint + 1);
	}

	@Override
	public boolean isLegal(State state, Grammar g) {
		if (state.stack.size() - state.splitPoint < 2) {
			return false;
		}
		if (state.splitPoint < state.stack.size()) {
			if (state.stack.get(state.splitPoint).getLabel().label.startsWith("@")) {
				boolean hasTempLeft = false;
				for (int i = state.splitPoint + 1; i < state.stack.size(); ++i) {
					if (state.stack.get(i).getLabel().label.startsWith("@")) {
						hasTempLeft = true;
						break;
					}
				}
				if (!hasTempLeft) return false;
					
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return 139101213;
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
		return "CGAP";
	}
	
	private static final long serialVersionUID = -876742053765366008L;

}
