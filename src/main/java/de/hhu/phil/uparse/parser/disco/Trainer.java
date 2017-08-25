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

import java.util.List;

import de.hhu.phil.uparse.parser.ParserException;
import de.hhu.phil.uparse.perceptron.Updates;
import de.hhu.phil.uparse.perceptron.Weights;
import de.hhu.phil.uparse.ui.UparseOptions;

public abstract class Trainer<T extends DiscoTransition> {
	
	protected UparseOptions opts;
	
	public Trainer(UparseOptions opts) {
		this.opts = opts;
	}
	
	abstract public boolean trainTree(State startState, List<T> goldTransitions, 
			Updates<T> updates, Weights<T> weights) throws ParserException;

}
