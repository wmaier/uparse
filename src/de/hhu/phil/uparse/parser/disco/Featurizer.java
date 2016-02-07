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


import java.util.LinkedList;
import java.util.List;

import de.hhu.phil.uparse.parser.ParserException;
import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.treebank.Tree.GapType;
import de.hhu.phil.uparse.treebank.TreebankException;
import de.hhu.phil.uparse.ui.UparseOptions;

/**
 * The actual feature extraction.
 * @author wmaier
 *
 */
public class Featurizer {

	private final static String[] SEPARATORS = {",", ";", ":"};

	private enum Child {

		LEFT, RIGHT, UNARY, UNKNOWN;

		public static Child is(char d) {
			switch (d) {
			case 'l':
				return Child.LEFT;
			case 'r':
				return Child.RIGHT;
			case 'u':
				return Child.UNARY;
			}
			return Child.UNKNOWN;
		}

	}

	private static String featureFromUni(String template, State state,
			Tree[] topOfStack, Tree[] topOfQueue, String separator)
			throws ParserException {
		String[] sp = template.split("_");
		int pos = Integer.valueOf(String.valueOf(sp[0].charAt(1)));
		Tree p = null;
		if (sp[0].startsWith("s")) {
			if (topOfStack[pos] == null) {
				p = null;
			} else {
				if (sp[0].length() >= 3) {
					Child c = Child.is(sp[0].charAt(2));
					if (!topOfStack[pos].isPreTerminal()) {
						List<Tree> children = topOfStack[pos].children();
						if (children.size() == 1) {
							if (c == Child.UNARY)
								p = children.get(0);
						} else if (children.size() == 2) {
							if (c == Child.LEFT)
								p = children.get(0);
							else if (c == Child.RIGHT)
								p = children.get(1);
						} else {
							throw new ParserException("found node with > 2 children");
						}
					}
					if (sp[0].length() == 4 && p != null) {
						Child gc = Child.is(sp[0].charAt(3));
						if (!p.isPreTerminal()) {
							List<Tree> children = p.children();
							if (children.size() == 1) {
								if (gc == Child.UNARY) {
									p = children.get(0);
								} else {
									p = null;
								}
							} else if (children.size() == 2) {
								if (gc == Child.LEFT) {
									p = children.get(0);
								} else if (gc == Child.RIGHT) {
									p = children.get(1);
								} else {
									p = null;
								}
							} else {
								throw new ParserException("found node with > 2 children");
							}
						} else {
							p = null;
						}
					}
				} else {
					p = topOfStack[pos];
				}
			}
		} else if (sp[0].startsWith("q")) {
			p = topOfQueue[pos];
		} else if (sp[0].startsWith("d")) {
			if (sp[0].length() != 2 || !Character.isDigit(sp[0].charAt(1))) {
				throw new ParserException("wrong dist shift feature spec: " + sp[0]);
			}
			if (state.lastShiftDist + Integer.valueOf(sp[0].charAt(1)) < state.todo.size()) {
				p = state.sentence.get(state.todo.get(state.lastShiftDist + Integer.valueOf(sp[0].charAt(1))));
			} else {
				p = null;
			}
		}
		StringBuffer feature = new StringBuffer();
		for (int i = 1; i < sp.length; ++i) {
			String what = sp[i];
			try {
				if (i != 1) {
					feature.append("_");
				}
				if (what.equals("t") || what.equals("w")) {
					if (p == null) {
						feature.append("NULL");
					} else {
						Tree lexicalHead = p.getLexicalHead();
						if (what.equals("t")) {
							feature.append(lexicalHead.getLabel().label);
						} else if (what.equals("w")) {
							feature.append(lexicalHead.children().get(0)
									.getLabel().label);
						}
					}
				} else if (what.equals("c")) {
					if (p == null) {
						feature.append("NULL");
					} else {
						feature.append(p.getLabel().label);
					}
				} else if (what.equals("x")) {
					// feature that encodes gap type: n (NONE), p (PASS), s (SOURCE)
					if (p == null) {
						feature.append("NULL");
					} else {
						GapType gt = p.gapType();
						if (gt.equals(GapType.NONE)) {
							feature.append("n");
						} else if (gt.equals(GapType.PASS)) {
							feature.append("p");
						} else if (gt.equals(GapType.SOURCE)) {
							feature.append("s");
						}
					}
				} else if (what.equals("y")) {
					// sum of gap lengths in tree (i.e., sum of compoundswap-lengths)
					if (p == null) {
						feature.append("NULL");
					} else {
						feature.append(p.gapLengthSum());
					}
				} else if (what.equals("p")) {
					// unique separator punctuation
					if (topOfStack[0] == null || topOfStack[1] == null) {
						feature.append("NULL");
					} else {
						Tree s0h = topOfStack[0].getLexicalHead();
						Tree s1h = topOfStack[1].getLexicalHead();
						int counter = 0;
						if (s0h.nodeNumber() < s1h.nodeNumber()) {
							for (int ipos = s0h.nodeNumber() + 1; ipos < s1h.nodeNumber(); ++ipos) {
								Tree interm = state.sentence.get(ipos - 1);
								String intermlabel = interm.children().get(0).getLabel().label;
								if (separator.equals(intermlabel))
									counter++;
							}
						} else if (s0h.nodeNumber() > s1h.nodeNumber()) {
							for (int ipos = s1h.nodeNumber() + 1; ipos < s0h.nodeNumber(); ++ipos) {
								Tree interm = state.sentence.get(ipos - 1);
								String intermlabel = interm.children().get(0).getLabel().label;
								if (separator.equals(intermlabel))
									counter++;
							}
						}
						if (counter == 0) {
							feature.append("NULL");
						} else {
							feature.append(separator);
						}
					}
				} else if (what.equals("q")) {
					// counter of all separator punctuations
					if (topOfStack[0] == null || topOfStack[1] == null) {
						feature.append("NULL");
					} else {
						Tree s0h = topOfStack[0].getLexicalHead();
						Tree s1h = topOfStack[1].getLexicalHead();
						int counter = 0;
						if (s0h.nodeNumber() < s1h.nodeNumber()) {
							for (int ipos = s0h.nodeNumber() + 1; ipos < s1h.nodeNumber(); ++ipos) {
								Tree interm = state.sentence.get(ipos - 1);
								String intermlabel = interm.children().get(0).getLabel().label;
								for (int j = 0; j < SEPARATORS.length; ++j) {
									if (intermlabel.equals(SEPARATORS[j])) {
										++counter;
									}
								}
							}
						} else if (s0h.nodeNumber() > s1h.nodeNumber()) {
							for (int ipos = s1h.nodeNumber() + 1; ipos < s0h.nodeNumber(); ++ipos) {
								Tree interm = state.sentence.get(ipos - 1);
								String intermlabel = interm.children().get(0).getLabel().label;
								for (int j = 0; j < SEPARATORS.length; ++j) {
									if (intermlabel.equals(SEPARATORS[j])) {
										++counter;
									}
								}
							}
						}
						if (counter == 0) {
							feature.append("NULL");
						} else {
							feature.append(counter);
						}
					}
				} 
			} catch (TreebankException e) {
				throw new ParserException(e);
			}
		}
		return feature.toString();
	}
	
	public static String featureFromBi(String template, State state, Tree[] topOfStack, Tree[] topOfQueue, String sep) throws ParserException {
		String biFeature = template;
		String[] splitTemplate = template.split("#");
		String left = featureFromUni(splitTemplate[0], state, topOfStack, topOfQueue, sep);
		String right = featureFromUni(splitTemplate[1], state, topOfStack, topOfQueue, sep);
		biFeature += "-" + left + "-" + right;
		return biFeature;
	}
	
	public static List<String> getFeatures(State state,
			UparseOptions opts)
			throws ParserException {
		LinkedList<String> result = new LinkedList<>();

		Tree[] topOfStack = new Tree[4];
		for (int i = 0; i < 4 && i < state.stack.size(); ++i) {
			topOfStack[i] = state.stack.get(i);
		}
		Tree[] topOfQueue = new Tree[4];
		for (int i = 0; i < state.todo.size() && i < 4; ++i) {
			topOfQueue[i] = state.sentence.get(state.todo.get(i));
		}
		DiscoTransition[] topOfTrans = new DiscoTransition[4];
		for (int i = 0; i < 4 && i < state.transitions.size(); ++i) {
			topOfTrans[i] = state.transitions.get(i);
		}
		for (String template : Features.uni) {
			String feature = featureFromUni(template, state, topOfStack, topOfQueue, "");
			if (feature != null) {
				result.add(template + "-" + feature);
			}
		}
		// for distshift transition support (works quite well)
		if (opts.features.contains("fullqueue")) {
			for (int i = 4; i < state.todo.size(); ++i) {
				Tree pt = state.sentence.get(state.todo.get(i));
				result.add("q" + String.valueOf(i) + "_w_t-" + pt.children().get(0).getLabel().label
						+ "_" + pt.getLabel().label);
			}
		}
		if (opts.features.contains("extended")) {
			for (String template : Features.extended) {
				String feature = featureFromUni(template, state, topOfStack,
						topOfQueue, "");
				if (feature != null) {
					result.add(template + "-" + feature);
				}
			}
		}
		if (opts.features.contains("disco")) {
			for (String template : Features.disco) {
				String feature = featureFromUni(template, state, topOfStack,
						topOfQueue, "");
				if (feature != null) {
					result.add(template + "-" + feature);
				}
			}
			for (String template : Features.discoBi) {
				result.add(featureFromBi(template, state, topOfStack, topOfQueue, ""));
			}
		}
		if (opts.features.contains("separator")) {
			for (String template : Features.separator) {
				if (template.endsWith("p")) {
					for (String sep : SEPARATORS) {
						String feature = featureFromUni(template, state, topOfStack,
								topOfQueue, sep);
						if (feature != null) {
							result.add(template + "-" + feature);
						}
					}
				} else if (template.endsWith("q")) {
					String feature = featureFromUni(template, state, topOfStack,
							topOfQueue, "");
					if (feature != null) {
						result.add(template + "-" + feature);
					}
				}
			}
			for (String template : Features.separatorBi) {
				if (template.endsWith("p")) {
					for (String sep : SEPARATORS) {
						result.add(featureFromBi(template, state, topOfStack, topOfQueue, sep));
					}
				} else if (template.endsWith("q")) {
					result.add(featureFromBi(template, state, topOfStack, topOfQueue, ""));
				}
			}
		}
		for (String template : Features.bi) {
			result.add(featureFromBi(template, state, topOfStack, topOfQueue, ""));
		}
		for (String template : Features.tri) {
			String triFeature = template;
			String[] splitTemplate = template.split("#");
			String left = featureFromUni(splitTemplate[0], state, topOfStack,
					topOfQueue, "");
			String middle = featureFromUni(splitTemplate[1], state, topOfStack,
					topOfQueue, "");
			String right = featureFromUni(splitTemplate[2], state, topOfStack,
					topOfQueue, "");
			triFeature += "-" + left + "-" + middle + "-" + right;
			result.add(triFeature);
		}
		return result;
	}

}
