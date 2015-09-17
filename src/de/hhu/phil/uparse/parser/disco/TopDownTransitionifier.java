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

import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.treebank.TreebankException;
import de.hhu.phil.uparse.treebank.bin.HeadSide;
import de.hhu.phil.uparse.ui.UparseOptions;


public class TopDownTransitionifier extends Transitionifier<Tree> {

	public TopDownTransitionifier(UparseOptions opts) {
		super(opts);
	}
	
	private void getTransitions(Tree tree, List<DiscoTransition> trans) throws TreebankException {
		if (tree.isPreTerminal()) {
			trans.add(new ShiftTransition());
		} else {
			if (tree.isRoot()) {
				trans.add(new IdleTransition());
				trans.add(new FinishTransition());
			}
			List<Tree> children = tree.children();
			if (children.size() == 1) {
				Tree uChild = children.get(0);
				trans.add(new UnaryTransition(tree.getLabel().label));
				getTransitions(uChild, trans);
			} else if (children.size() == 2) {
				HeadSide side = null;
				Tree leftChild = children.get(0);
				Tree rightChild = children.get(1);
				if (leftChild.isHead() && !rightChild.isHead()) {
					side = HeadSide.LEFT;
				} else if (!leftChild.isHead() && rightChild.isHead()) {
					side = HeadSide.RIGHT;
				} else {
					throw new TreebankException("no head marked or two heads marked");
				}
				trans.add(new BinaryTransition(tree.getLabel().label, side));
				getTransitions(rightChild, trans);
				getTransitions(leftChild, trans);
			} else {
				throw new TreebankException("more than 2 or no children during transitionification");
			}
		}
	}

	@Override
	public void process(Tree tree) throws TreebankException {
		List<DiscoTransition> trans = new ArrayList<DiscoTransition>();
		getTransitions(tree, trans);
		Collections.reverse(trans);
		transitions.add(trans);
	}

}
