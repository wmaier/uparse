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
import de.hhu.phil.uparse.treebank.Constants;
import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.treebank.bin.HeadSide;
import de.hhu.phil.uparse.ui.UparseOptions;

public class DShiftTransitionifier extends SwapTransitionifier {

	public DShiftTransitionifier(UparseOptions opts) {
		super(opts);
	}

	@Override
	public void getTransitions(List<Tree> preTerminals, List<DiscoTransition> result, List<Tree> stack) throws ParserException {
		while (preTerminals.size() > 0 || stack.size() > 0) {
			if (stack.size() == 1 && stack.get(0).getLabel().label.equals(Constants.DEFAULT_ROOT)) {
				result.add(new FinishTransition());
				if (!opts.noidle)
					result.add(new IdleTransition());
				stack.remove(0);
			}
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
			if (preTerminals.size() > 0) {
				Tree parent = stack.get(stack.size() - 1).parent();
				Tree nextLc = parent.children().get(1).preTerminals().get(0);
				int dist = 0;
				while (!nextLc.equals(preTerminals.get(dist))) {
					++dist;
				}
				result.add(new DShiftTransition(dist));
				stack.add(preTerminals.get(dist));
				preTerminals.remove(dist);
			}
		}
	}

}
