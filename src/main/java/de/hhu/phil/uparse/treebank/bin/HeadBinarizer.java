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
package de.hhu.phil.uparse.treebank.bin;

import java.util.ArrayList;
import java.util.List;

import de.hhu.phil.uparse.treebank.NodeLabel;
import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.treebank.TreeProcessor;
import de.hhu.phil.uparse.ui.UparseOptions;

public class HeadBinarizer implements TreeProcessor<Tree> {

	private enum Direction {
		LEFT, RIGHT;
	}
	
	public HeadBinarizer(UparseOptions opts) {
	}

	@Override
	public void process(Tree tree) {
		if (tree.isPreTerminal()) {
			return;
		}
		for (Tree child : tree.children()) {
			process(child);
		}
		if (tree.children().size() > 2) {
			Direction direction = Direction.LEFT;
			List<Tree> remainingChildren = new ArrayList<>(tree.children());
			Tree lastTree = tree;
			tree.setChildren(new ArrayList<Tree>());
			String label = tree.getLabel().label;
			Tree child = null;
			Tree binarizationTree = null;
			while (remainingChildren.size() > 2) {
				if (remainingChildren.get(0).isHead()) {
					direction = Direction.RIGHT;
				}
				NodeLabel binarizationLabel = new NodeLabel("@" + label);
				binarizationTree = new Tree(binarizationLabel);
				binarizationTree.setIsHead(true);
				if (direction == Direction.LEFT) {
					child = remainingChildren.get(0);
					remainingChildren.remove(0);
				} else if (direction == Direction.RIGHT) {
					child = remainingChildren.get(remainingChildren.size() - 1);
					remainingChildren.remove(remainingChildren.size() - 1);
				}
				lastTree.addChild(binarizationTree);
				lastTree.addChild(child);
				binarizationTree.setParent(lastTree);
				child.setParent(lastTree);
				lastTree = binarizationTree;
			}
			for (int i = 0; i < 2; ++i) {
				child = remainingChildren.get(i);
				binarizationTree.addChild(child);
				child.setParent(binarizationTree);
			}
		}
	}

	
}
