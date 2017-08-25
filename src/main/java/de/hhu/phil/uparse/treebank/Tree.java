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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Tree {
	
	public enum GapType {
		// no gap
		NONE, 
		// gap is passed down from above
		PASS, 
		// gap is introduced at the root of this tree, i.e., 
		// tree itself is continuous, children are not
		SOURCE
	}
	
	private int id = -1;
	
	private Tree parent;
	
	private List<Tree> children;
	
	private int nodeNumber;
	
	private NodeLabel data;
	
	private Tree lexicalHead;

	private boolean isHead;
	
	public Tree() {
		this(new NodeLabel());
	}
	
	public Tree(NodeLabel label) {
		data = label;
		children = new ArrayList<>();
		parent = null;
		lexicalHead = null;
		isHead = false;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<Tree> preorder() {
		ArrayList<Tree> result = new ArrayList<>();
		result.add(this);
		if (children.size() > 0) {
			for (Tree child : children()) {
				result.addAll(child.preorder());
			}
		}
		return result;
	}

	public Tree deleteTerminal(Tree leaf) {
		/*Delete a leaf node and recursively all of its ancestors                                                                                                                                                                                                                                                                        
		    which do not have siblings. Root of the tree with the leaf                                                                                                                                                                                                                                                                        
		    must be given as well. Return the first node with siblings                                                                                                                                                                                                                                                                        
		    or the (given) root.*/
		Tree root = this;
		while (!root.isRoot()) {
			root = root.parent();
		}
		List<Tree> preTerminals = root.preTerminals();
		int num = leaf.nodeNumber();
		Tree parent = leaf.parent();
		while (parent != null && (leaf.isPreTerminal() || leaf.isTerminal())) {
			parent.children.remove(leaf);
			leaf = parent;
			parent = leaf.parent;
		}
		// shift numbering
		for (Tree preTerminal : preTerminals) {
			int nodeNumber = preTerminal.nodeNumber();
			if (nodeNumber > num) {
				preTerminal.setNodeNumber(nodeNumber - 1);
			}
		}
		return leaf;
	}

	public boolean isTerminal() {
		return children.size() == 0;
	}

	public boolean isPreTerminal() {
		return children.size() == 1 && children.get(0).children.size() == 0;
	}

	public List<Tree> preTerminals() {
		LinkedList<Tree> result = new LinkedList<>();
		if (isPreTerminal()) {
			result.add(this);
		} else {
			for (Tree child : children) {
				result.addAll(child.preTerminals());
			}
		}
		result.sort((t1, t2) -> (t1.nodeNumber - t2.nodeNumber));
		return result;
	}	
	
	// true if this is a preterminal and its label OR the label of the terminal below is empty
	public boolean isTrace() {
		return isPreTerminal() && children().get(0).getLabel().label.equals(Constants.EPSILON);
	}

	public List<Tree> children() {
		children.sort((t1, t2) -> (t1.preTerminals().get(0).nodeNumber() 
				- t2.preTerminals().get(0).nodeNumber()));
		return Collections.unmodifiableList(children);
	}
	
	public void setChildren(List<Tree> children) {
		this.children = children;
	}

	public void addChild(Tree child) {
		children.add(child);
	}

	public void removeChild(Tree child) {
		children.remove(child);
	}

	public Tree parent() {
		return parent;
	}
	
	public void setParent(Tree parent) {
		this.parent = parent;
	}
	
	public boolean isRoot() {
		return parent == null;
	}

	public int nodeNumber() {
		return nodeNumber;
	}

	public void setNodeNumber(int snum) {
		this.nodeNumber = snum;
	}
	
	public NodeLabel getLabel() {
		return data;
	}

	public void setLabel(NodeLabel label) {
		this.data = label;
	}

	public Tree getLexicalHead() throws TreebankException {
		if (lexicalHead == null) {
			if (children.size() == 0) {
				throw new TreebankException("Cannot return lexical head for terminal");
			}
			if (isPreTerminal()) {
				lexicalHead = this;
			} else {
				Tree headChild = null;
				for (Tree child : children()) {
					if (child.isHead) {
						if (headChild != null) {
							throw new TreebankException("More than one head marked");
						} else {
							headChild = child;
						}
					}
				}
				if (headChild == null) {
					throw new TreebankException("Heads are supposed to be marked");
				}
				lexicalHead = headChild.getLexicalHead();
			}
		}
		return lexicalHead;
	}

	public void setLexicalHead(Tree lexicalHead) {
		this.lexicalHead = lexicalHead;
	}

	public boolean isLexicalHeadSet() {
		return this.lexicalHead != null;
	}

	public boolean isHead() {
		return isHead;
	}

	public void setIsHead(boolean isHead) {
		this.isHead = isHead;
	}
	
	public boolean hasGaps() {
		List<Tree> preTerminals = preTerminals();
		int pos = preTerminals.get(0).nodeNumber();
		for (Tree pt : preTerminals.subList(1, preTerminals.size())) {
			if (pt.nodeNumber > pos + 1) {
				return true;
			}
			pos++;
		}
		return false;
	}
	
	public int gapLengthSum() {
		int sum = 0;
		List<Tree> preTerminals = preTerminals();
		int pos = preTerminals.get(0).nodeNumber();
		for (Tree pt : preTerminals.subList(1, preTerminals.size())) {
			sum += pt.nodeNumber - pos - 1;
			pos++;
		}
		return sum;
	}
	
	public GapType gapType() {
		if (isPreTerminal()) {
			return GapType.NONE;
		}
		List<Tree> preTerminals = preTerminals();
		int pos = preTerminals.get(0).nodeNumber();
		for (Tree pt : preTerminals.subList(1, preTerminals.size())) {
			if (pt.nodeNumber > pos + 1) {
				return GapType.PASS;
			}
			pos++;
		}
		for (Tree child : children()) {
			if (!child.isPreTerminal() && child.hasGaps()) {
				return GapType.SOURCE;
			}
		}
		return GapType.NONE;
	}

	public Tree emptyTree() {
		Tree t = new Tree();
		for (Tree pt : preTerminals()) {
			t.addChild(pt);
			pt.setParent(t);
		}
		return t;
	}

	public static Tree lca(Tree treeA, Tree treeB) {
		List<Tree> domA = new ArrayList<>();
		domA.add(treeA);
		Tree parent = treeA;
		while (parent.parent() != null) {
			parent = parent.parent();
			domA.add(parent);
		}
		List<Tree> domB = new ArrayList<>();
		domB.add(treeB);
		parent = treeB;
		while (parent.parent() != null) {
			parent = parent.parent();
			domB.add(parent);
		}
		for (Tree a : domA) {
			for (Tree b : domB) {
				if (a == b) {
					return a;
				}
			}
		}
		return null;
	}

	public static boolean isAncestor(Tree tree, Tree ancestor) {
		Tree cursor = tree.parent;
		while (cursor != null) {
			if (cursor == ancestor) {
				return true;
			}
			cursor = cursor.parent();
		}
		return false;
	}

	@Override
	public String toString() {
		return getLabel().toString();
	}

	public String getSentence() {
		String result = "";
		for (Tree pt : preTerminals()) {
			result += pt.children().get(0).getLabel().label + " ";
		}
		return result;
	}

	
}
