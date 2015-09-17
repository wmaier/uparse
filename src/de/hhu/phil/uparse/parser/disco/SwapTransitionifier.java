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

import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.ui.UparseOptions;

abstract public class SwapTransitionifier extends BottomUpTransitionifier {

	public SwapTransitionifier(UparseOptions opts) {
		super(opts);
	}

	@Override
	public void initialize(Tree tree, List<Tree> terminals, List<DiscoTransition> trans, List<Tree> stack) {
		terminals.addAll(tree.preTerminals());
		Tree first = terminals.remove(0);
		stack.add(first);
		trans.add(new ShiftTransition());
	}
	
}
