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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.treebank.TreeProcessor;

public class RuleBasedHeadMarker implements TreeProcessor<Tree> {

	private HeadRules headRules;

	public RuleBasedHeadMarker(InputStream inputStream) throws IOException {
		HeadRuleReader headRuleReader = new HeadRuleReader(inputStream);
		headRules = headRuleReader.getHeadRules();
		headRuleReader.close();
	}

	@Override
	public void process(Tree tree) {
		for (Tree n : tree.preorder()) {
			String parentLabel = n.getLabel().label;
			List<Tree> children = n.children();
			if (children.size() > 0) {
				String[] childrenLabels = new String[children.size()];
				for (int i = 0; i < children.size(); ++i) {
					childrenLabels[i] = children.get(i).getLabel().label;
				}
				int headpos = getHead(parentLabel, childrenLabels);
				children.get(headpos).setIsHead(true);
			}
		}
	}

	private int getHead(String lhs, String[] rhs) {
		List<HeadRule> rules = headRules.getRules(lhs);
		if (rules != null) {
			for (HeadRule hrule : headRules.getRules(lhs)) {
				// if no labels are provided 
				if (hrule.getLabels().length == 0) {
					if (hrule.getDirection().equals(HeadRule.LEFT_TO_RIGHT)) {
						return rhs.length - 1;
					} else {
						return 0;
					}
				} 
				// otherwise look for labels among children
				for (String label : hrule.getLabels()) {
					if (hrule.getDirection().equals(HeadRule.LEFT_TO_RIGHT)) {
						// left to right
						for (int rhspos = 0; rhspos < rhs.length; ++rhspos) {
							if (matches(rhs[rhspos], label)) {
								return rhspos;
							}
						}
					} else {
						// right to left
						for (int rhspos = rhs.length - 1; rhspos >= 0; --rhspos) {
							if (matches(rhs[rhspos], label)) {
								return rhspos;
							}
						}
					}
				}
			}
		}
		return 0;
	}

	protected boolean matches(String tag, String tagPattern) {
		return tag.equals(tagPattern);
	}

}
