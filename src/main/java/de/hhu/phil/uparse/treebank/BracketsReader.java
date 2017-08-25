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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.hhu.phil.uparse.ui.UparseOptions;

public class BracketsReader implements TreeReader<Tree> {

	private UparseOptions opts;
	
	public BracketsReader(UparseOptions opts) {
		this.opts = opts;
	}
	
	public static final Map<Character, String> BRACKETS;
	static {
		BRACKETS = new HashMap<>();
		BRACKETS.put('(', "-LRB-");
		BRACKETS.put(')', "-RRB-");
	}
	
	@Override
	public Treebank<Tree> read(InputStream inputStream) throws IOException {
		Treebank<Tree> result = new Treebank<Tree>();
		LinkedList<Tree> queue = new LinkedList<Tree>();
		int cnt = 1;
		int state = 0;
		int level = 0;
		int termCnt = 0;
		StringBuffer tokenbuf = new StringBuffer();
		StringBuffer whitespacebuf = new StringBuffer();
		int r;
		while ((r = inputStream.read()) != -1) {
			char c = (char) r;
			// holds tokens
			String tval = tokenbuf.toString();
			// holds whitespace
			String wval = whitespacebuf.toString();
			if (tval.length() > 0 && wval.length() > 0) {
				throw new IOException("something went wrong in the lexer");
			}
			if (BRACKETS.keySet().contains(c)) {
				if (tval.length() > 0) {
					if (state == 0) {
					} else if (state == 1 || state == 9) {
						// phrase label, 9 when root label, 1 otherwise
						// take epsilon into account
						NodeLabel label;
						try {
							if (tokenbuf.toString().equals(Constants.EPSILON)) {
								label = new NodeLabel(tokenbuf.toString());
							} else {
								label = NodeLabel.fromString(tokenbuf.toString(), opts);
							}
						} catch (TreebankException e) {
							throw new IOException(e);
						}
						queue.get(queue.size() - 1).setLabel(label);
	                    state = 2;
					} else if (state == 3) {
						// terminal-preterminal situation
						NodeLabel terminalLabel;
						Tree preTerminal = queue.get(queue.size() - 1);
						if (preTerminal.getLabel().label.equals(Constants.EPSILON)) {
							// trace! flip this!
							terminalLabel = preTerminal.getLabel();
							try {
								preTerminal.setLabel(NodeLabel.fromString(tokenbuf.toString(), opts));
							} catch (TreebankException e) {
								throw new IOException(e);
							}
						} else {
							terminalLabel = new NodeLabel(tokenbuf.toString());
						}
						Tree terminal = new Tree(terminalLabel);
						terminal.setParent(preTerminal);
						terminal.parent().setNodeNumber(termCnt);
						terminal.setNodeNumber(termCnt);
						preTerminal.addChild(terminal);
						termCnt++;
						state = 4;
					} else if (state == 2) {
						throw new IOException("expected whitespace or (, got token");
					} else if (state == 4) {
						throw new IOException("expected whitespace or ), got token");
					} else if (state == 5) {
						throw new IOException("expected whitespace, ( or ), got token");
					} else {
						throw new IllegalStateException();
					}
					tokenbuf = new StringBuffer();
				}
				if (wval.length() > 0) {
					if (state == 0 || state == 1 || state == 3 || state == 4 || state == 5 || state == 9) {
					} else if (state == 2) {
						// WS between POS and word
						state = 3;
					} else {
						throw new IllegalStateException();
					}
					whitespacebuf = new StringBuffer();
				}
				// yield bracket
				if ("-LRB-".equals(BRACKETS.get(c))) {
					if (state == 0 || state == 2 || state == 3 || state == 5) {
						// beginning of sentence or phrase
						level += 1;
						queue.add(new Tree(new NodeLabel()));
						state = state == 0 ? 9 : 1;
					} else if (state == 9) {
						// root label empty
						level += 1;
						queue.get(queue.size() - 1).getLabel().label = Constants.DEFAULT_ROOT;
						queue.add(new Tree(new NodeLabel()));
						state = 1;
					} else if (state == 1) {
	                    throw new IOException("expected whitespace or label, got (");
					} else if (state == 4) {
						throw new IOException("expected whitespace or ), got (");
					} else {
						throw new IllegalStateException();
					}
				}
				if ("-RRB-".equals(BRACKETS.get(c))) {
					if (state == 0) {
					} else if (state == 2 || state == 5 || state == 4) {
						if (state == 2) {
							throw new IOException("pos tags cannot be empty");
						}
						level -= 1;
						if (queue.size() > 1) {
							// close phrase
							queue.get(queue.size() - 2).addChild(queue.get(queue.size() - 1));
							queue.get(queue.size() - 1).setParent(queue.get(queue.size() - 2));
							queue.removeLast();
						}
						if (level == 0) {
							// close sentence
							queue.get(0).setId(cnt);
							cnt++;
							result.add(queue.get(0));
							termCnt = 1;
							queue = new LinkedList<>();
							state = 0;
						} else {
							state = 5;
						}
					} else if (state == 1) {
						throw new IOException("expected label, got )");
					} else if (state == 3 || state == 9) {
	                    throw new IOException("expected whitespace, label or (, got )");
					} else {
						throw new IllegalStateException();
					}
				}
			} else if (Character.isWhitespace(c)) {
				if (tval.length() > 0) {
					if (state == 0) {
					} else if (state == 1 || state == 9) {
						// phrase label, 9 when root label, 1 otherwise
						NodeLabel label;
						try {
							label = NodeLabel.fromString(tokenbuf.toString(), opts);
						} catch (TreebankException e) {
							throw new IOException(e);
						}
						queue.get(queue.size() - 1).setLabel(label);
						state = 2;
					} else if (state == 3) {
						NodeLabel terminalLabel = new NodeLabel();
						terminalLabel.label = tokenbuf.toString();
						Tree terminal = new Tree(terminalLabel);
						queue.get(queue.size() - 1).addChild(terminal);
						terminal.setParent(queue.get(queue.size() - 1));
						terminal.setNodeNumber(termCnt);
						termCnt++;
						state = 4;
					} else if (state == 2) {
						throw new IOException("expected whitespace or (, got token");
					} else if (state == 4) {
						throw new IOException("expected whitespace or ), got token");
					} else if (state == 5) {
						throw new IOException("expected whitespace, ( or ), got token");
					} else {
						throw new IllegalStateException();
					}
					tokenbuf = new StringBuffer();
				}
				whitespacebuf.append(String.valueOf(c));
			} else {
				if (wval.length() > 0) {
					if (state == 0 || state == 1 || state == 3 || state == 4 || state == 5 || state == 9) {
					} else if (state == 2) {
						// WS between POS and word
						state = 3;
					} else {
						throw new IllegalStateException();
					}
					whitespacebuf = new StringBuffer();
				}
				tokenbuf.append(String.valueOf(c));
			}
		}
		
		return result;
	}

}
