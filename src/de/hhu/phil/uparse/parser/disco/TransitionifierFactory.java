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

import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.treebank.TreebankException;
import de.hhu.phil.uparse.ui.UparseOptions;

public class TransitionifierFactory {

	public static Transitionifier<Tree> getTransitionsifier(String transitionifier, UparseOptions opts) throws TreebankException {

		// Compound Swap from Maier (2015)
		if ("cswap".equals(transitionifier)) {
			return new CompoundSwapTransitionifier(opts);
		}
		
		// Single Swap from Maier (2015)
		if ("sswap".equals(transitionifier)) {
			return new SingleSwapTransitionifier(opts);
		}
		
		// As compound swap, but using skipshift from Maier & Lichte (2016) instead of swap
		if ("dshift".equals(transitionifier)) {
			return new DShiftTransitionifier(opts);
		}
		
		// Continuifier-based, as in Maier & Lichte (2016)
		if ("dshiftc".equals(transitionifier)) {
			return new DShiftCTransitionifier(opts);
		}
		
		// Top down extraction for continuous parsing
		if ("topdown".equals(transitionifier)) {
			return new TopDownTransitionifier(opts);
		}
		
		throw new IllegalArgumentException("Unknown transitionifier " + transitionifier);
		
	}

}
