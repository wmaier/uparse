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
import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.treebank.bin.HeadSide;
import de.hhu.phil.uparse.ui.UparseOptions;

/**
 * Extracts transition sequences top-down and then reverses the sequence.
 * Works only for continuous trees.
 * @author wmaier
 *
 */
public class CoavouxGapTransitionifier extends BottomUpTransitionifier {

	public CoavouxGapTransitionifier(UparseOptions opts) {
		super(opts);
	}

	@Override
	public void initialize(Tree tree, List<Tree> terminals, List<DiscoTransition> trans, List<Tree> stack) {
		terminals.addAll(tree.preTerminals());
		Tree first = terminals.remove(0);
		stack.add(first);
		trans.add(new ShiftTransition());
	}
	
	protected static int gapPossible(List<Tree> stack) {
		if (stack.size() >= 2) {
			Tree p = stack.get(stack.size() - 1).parent();
			int cnt = 0;
			for (int i = stack.size() - 2; i >= 0; --i) {
				if (stack.get(i).parent() == p) {
					return cnt;
				}
			}
		} 
		return -1;
	}

	@Override
	public void getTransitions(List<Tree> terminals, List<DiscoTransition> result, List<Tree> stack)
			throws ParserException {
		while (binaryPossible(stack) || unaryPossible(stack)) {
			while (binaryPossible(stack)) {
				HeadSide side = null;
				Tree rightChild = stack.remove(stack.size() - 1);
				Tree leftChild = stack.remove(stack.size() - 1);
				if (leftChild.isHead() && !rightChild.isHead()) {
					side = HeadSide.LEFT;
				} else if (!leftChild.isHead() && rightChild.isHead()) {
					side = HeadSide.RIGHT;
				} else {
					throw new ParserException("no head marked or two heads marked");
				}
				Tree parent = leftChild.parent();
				stack.add(parent);
				result.add(new BinaryTransition(parent.getLabel().label, side));
			}
			while (unaryPossible(stack)) {
				Tree last = stack.get(stack.size() - 1);
				last = last.parent();
				stack.remove(stack.get(stack.size() - 1));
				stack.add(last);
				result.add(new UnaryTransition(last.getLabel().label));
			}
		}	
		int gap = gapPossible(stack);
		if (gap > 0) {
			result.add(new CoavouxGapTransition());
		}
		if (terminals.size() > 0) {
			result.add(new ShiftTransition());
			stack.add(terminals.get(0));
			terminals.remove(0);
		}
	}

}
