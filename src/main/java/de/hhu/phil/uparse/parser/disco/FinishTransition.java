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

/**
 * Finish transition as in Zhang & Clark.
 * @author wmaier
 *
 */
public class FinishTransition extends DiscoTransition implements Serializable {
	
	@Override
	public State extend(State state, double scoreDelta) {
		LinkedList<DiscoTransition> newTransitions = new LinkedList<>(state.transitions);  
	    newTransitions.push(this);
		return new State(state.stack, newTransitions, state.sentence, state.todo, 
				state.score + scoreDelta, true, state.lastShiftDist);    
	}
	
	public boolean isLegal(State state, Grammar g) {
		if (state.complete) {
	      return false;
	    }
	    if (!(state.stack.size() == 1 && state.stack.get(0).getLabel().label.equals(Constants.DEFAULT_ROOT))) {
	      return false;
	    }
	    if (!state.todo.isEmpty()) {
	    	return false;
	    }
	    return true;
	  }

	@Override
	public int hashCode() {
		// random prime
		return 15485863;
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

	@Override
	public String toString() {
		return "FINISH";
	}

	private static final long serialVersionUID = -7699633173234695530L;

}
