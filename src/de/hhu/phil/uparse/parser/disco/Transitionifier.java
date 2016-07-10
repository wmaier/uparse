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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hhu.phil.uparse.treebank.TreeProcessor;
import de.hhu.phil.uparse.treebank.Treebank;
import de.hhu.phil.uparse.treebank.TreebankException;
import de.hhu.phil.uparse.treebank.TreebankProcessor;
import de.hhu.phil.uparse.ui.UparseOptions;

/**
 * Abstract class for extraction of transition sequences.
 * 
 * @author wmaier
 *
 */
abstract public class Transitionifier<T> implements TreebankProcessor<T>, TreeProcessor<T> {

	protected List<List<DiscoTransition>> transitions;
	
	protected UparseOptions opts;
	
	public Transitionifier(UparseOptions opts) {
		transitions = new ArrayList<>();
		this.opts = opts;
	}
	
	public List<List<DiscoTransition>> getTransitions() {
		return Collections.unmodifiableList(transitions);
	}
	
	@Override
	abstract public void process(T tree) throws TreebankException;

	@Override
	public void process(Treebank<T> tb) throws TreebankException {
		for (T t : tb) {
			process(t);
		}
	}
	
	
}
