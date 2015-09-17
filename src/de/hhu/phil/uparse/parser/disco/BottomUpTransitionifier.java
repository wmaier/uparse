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

import de.hhu.phil.uparse.parser.ParserException;
import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.treebank.TreebankException;
import de.hhu.phil.uparse.ui.UparseOptions;

/**
 * Bottom up transition sequence extraction 
 * @author wmaier
 *
 */
abstract public class BottomUpTransitionifier extends Transitionifier<Tree> {
	
	public BottomUpTransitionifier(UparseOptions opts) {
		super(opts);
	}
	
	public void process(Tree tree) throws TreebankException {
		List<Tree> terms = new ArrayList<>();
		List<DiscoTransition> trans = new ArrayList<>();
		List<Tree> stack = new ArrayList<>();
		try {
			initialize(tree, terms, trans, stack);
			getTransitions(terms, trans, stack);
		} catch (ParserException e) {
			throw new TreebankException(e);
		}
		transitions.add(Collections.unmodifiableList(trans));
	}

	protected static boolean binaryPossible(List<Tree> stack) {
		return stack.size() >= 2 && stack.get(stack.size() - 2).parent().equals(stack.get(stack.size() - 1).parent());
	}
	
	protected static boolean unaryPossible(List<Tree> stack) {
		if (stack.size() < 1) {
			return false;
		}
		Tree last = stack.get(stack.size() - 1);
		return last.parent() != null && last.parent().children().size() == 1;
	}
	
	abstract public void initialize(Tree tree, List<Tree> terminals, List<DiscoTransition> trans, List<Tree> stack);
	
	abstract public void getTransitions(List<Tree> terminals, List<DiscoTransition> result, List<Tree> stack) throws ParserException;
	
}
