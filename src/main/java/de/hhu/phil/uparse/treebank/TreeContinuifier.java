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
package de.hhu.phil.uparse.treebank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hhu.phil.uparse.treebank.Tree.GapType;

public class TreeContinuifier implements TreeProcessor<Tree> {

	private int threshold = 0;
	
	// left, right, rightd, label, dist, random
	public String mode;
	
	public HashMap<String,String> labels;
	
	public List<Tree> reordered;
	
	public boolean dumpTree;
	
	public TreeContinuifier(String mode, ArrayList<String> labelDirections, boolean dumpTree, int threshold) throws TreebankException {
		this.mode = mode;
		labels = new HashMap<>();
		if (labelDirections != null) {
			for (String labelDirection : labelDirections) {
				// is there a colon?
				int ind = labelDirection.indexOf(":");
				if (ind == -1) {
					throw new TreebankException("cannot understand label/direction specification: " 
							+ labelDirection);
				}
				String label = labelDirection.substring(0, ind);
				String sdirection = labelDirection.substring(ind + 1);
				if (!("left".equals(sdirection) || "right".equals(sdirection) || "rightd".equals(sdirection)
						|| "random".equals(sdirection) || "dist".equals(sdirection))) {
					throw new TreebankException("illegal direction spec: " + labelDirection);
				}
				labels.put(label, sdirection);
			}
		}
		this.dumpTree = dumpTree;
		this.threshold = threshold;
	}
	
	public List<Tree> reorder(Tree tree) throws TreebankException {
		List<Tree> result = null;
		List<Tree> children = tree.children();
		if (children.size() > 2) {
			throw new TreebankException("tree must be binarized");
		}
		if (tree.isPreTerminal()) {
			return tree.preTerminals();
		}
		if (children.size() == 1) {
			result = reorder(children.get(0));
		}
		if (children.size() == 2) {
			Tree left = children.get(0);
			Tree right = children.get(1);
			String theMode = mode;
			if ("label".equals(theMode)) {
				theMode = "rightd";
				String label = tree.getLabel().label;
				if (labels.containsKey(tree.getLabel().label)) {
					theMode = labels.get(label);
				}
			}
			if ("left".equals(theMode)) {
				result = reorder(left);
				result.addAll(reorder(right));
			} else if ("right".equals(theMode)) {
				result = reorder(right);
				result.addAll(reorder(left));
			} else if ("rightd".equals(theMode)) {
				if (tree.gapType().equals(GapType.SOURCE)) {
					result = reorder(right);
					result.addAll(reorder(left));
				} else {
					result = reorder(left);
					result.addAll(reorder(right));
				}
			} else if ("dist".equals(theMode)) {
				int leftGap = left.gapLengthSum();
				int rightGap = right.gapLengthSum();
				if (tree.gapType().equals(GapType.SOURCE) && (rightGap > threshold || leftGap > threshold)) {
					result = reorder(right);
					result.addAll(reorder(left));
				} else {
					result = reorder(left);
					result.addAll(reorder(right));
				}
			} else if ("random".equals(theMode)) {
				boolean decision = true;
				if (Math.random() < 0.5) {
					decision = false;
				}
				if (decision) {
					result = reorder(right);
					result.addAll(reorder(left));
				} else {
					result = reorder(left);
					result.addAll(reorder(right));
				}
			}
		}
		return result;
	}
	
	@Override
	public void process(Tree tree) throws TreebankException {
		reordered = reorder(tree);
		if (dumpTree) {
			List<Tree> terminals = tree.preTerminals();
			int[] map = new int[terminals.size()];
			for (int i = 0; i < reordered.size(); ++i) {
				for (int j = 0; j < reordered.size(); ++j) {
					int terminalNumber = terminals.get(i).nodeNumber();
					int rTerminalNumber = reordered.get(j).nodeNumber();
					if (terminalNumber == rTerminalNumber) {
						map[i] = rTerminalNumber;
						break;
					}
				}
			}
			for (int i = 0; i < terminals.size(); ++i) {
				terminals.get(i).setNodeNumber(map[i]);
			}
		}
	}

}
