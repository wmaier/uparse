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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hhu.phil.uparse.ui.UparseOptions;

/**
 * CNF-CFG implementation for filtering of parser
 * @author wmaier
 *
 */
public class Grammar implements TreeProcessor<Tree>, Serializable {

	// l -> r -> p
	private Map<String, Map<String,Set<String>>> binary;

	// u -> p
	private Map<String, Set<String>> unary;
	
	private UparseOptions opts;
	
	public Grammar(UparseOptions opts) {
		binary = new HashMap<>();
		unary = new HashMap<>();
		this.opts = opts;
	}
	
	@Override
	public void process(Tree tree) {
		for (Tree subtree : tree.preorder()) {
			if (subtree.isPreTerminal() || subtree.isTerminal()) {
				continue;
			}
			List<Tree> children = subtree.children();
			if (children.size() == 2) {
				String l = children.get(0).getLabel().label;
				String r = children.get(1).getLabel().label;
				String p = subtree.getLabel().label;
				if (!binary.containsKey(l)) {
					binary.put(l, new HashMap<>());
				}
				if (!binary.get(l).containsKey(r)) {
					binary.get(l).put(r, new HashSet<>());
				}
				binary.get(l).get(r).add(p);
			} else if (children.size() == 1) {
				String u = children.get(0).getLabel().label;
				String p = subtree.getLabel().label;
				if (!unary.containsKey(u)) {
					unary.put(u, new HashSet<>());
				}
				unary.get(u).add(p);
			} else {
				throw new IllegalStateException("Trees must be binarized");
			}
		}
	}
	
	/**
	 * Check if a certain label can be generated from two given child labels.
	 * @param p parent, the label to be generated
	 * @param l left child
	 * @param r right child
	 * @return
	 */
	public boolean isBinaryCompletion(String p, String l, String r) {
		if (!opts.grammar) {
			return true;
		}
		return binary.containsKey(l) && binary.get(l).containsKey(r) && binary.get(l).get(r).contains(p);
	}

	/**
	 * Check if a certain label can be generated from a single child label
	 * @param p parent
	 * @param u unary child
	 * @return
	 */
	public boolean isUnaryCompletion(String p, String u) {
		if (!opts.grammar) {
			return true;
		}
		if (!unary.containsKey(u)) {
			return false;
		}
		return unary.get(u).contains(p);
	}

	private static final long serialVersionUID = 206376857064174248L;

}
