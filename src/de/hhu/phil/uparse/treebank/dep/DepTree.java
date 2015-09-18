/*
 *  This file is part of uparse.
 *  
 *  Copyright 2015 Wolfgang Maier 
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
package de.hhu.phil.uparse.treebank.dep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class DepTree implements Iterable<DepNode>, Iterator<DepNode> {

	private int[] heads;
	
	private DepNode[] nodes;
	
	private String[] labels;
	
	private int id;
	
	private int size; 
	
	private int iteratorPos = 0;

	public DepTree(int length) {
		size = length + 1;
		heads = new int[length + 1];
		Arrays.fill(heads, -1);
		nodes = new DepNode[length + 1];
		labels = new String[length + 1];
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setNode(DepNode node, int pos) {
		nodes[pos] = node;
	}

	public DepNode getNode(int pos) {
		return nodes[pos];
	}

	public void setHead(int dep, int head, String label) {
		heads[dep] = head;
		labels[dep] = label;
		nodes[dep].setField("deprel", label);
		nodes[dep].setField("head", String.valueOf(head));

	}
	
	public int getHead(int dep) {
		return heads[dep];
	}
	
	public List<Integer> getDep(int head) {
		List<Integer> deps = new ArrayList<>();
		for (int i = 0; i < heads.length; ++i) {
			if (heads[i] == head) {
				deps.add(i);
			}
		}
		return deps;
	}

	public String getLabel(int dep) {
		return labels[dep];
	}
	
	public String getSentence() {
		StringBuffer sb = new StringBuffer();
		for (int i = 1; i < nodes.length; ++i) {
			DepNode n = nodes[i];
			sb.append(n.getField("form") + " " );
		}
		return sb.toString();
	}

	public DepTree emptyCopy() {
		DepTree result = new DepTree(this.size - 1);
		result.nodes = Arrays.copyOf(nodes, nodes.length);
		for (int i = 1; i < result.nodes.length; ++i) {
			result.nodes[i] = new DepNode(result.nodes[i]);
		}
		return result;
	}

	public int size() {
		return size; 
	}

	@Override
	public DepNode next() {
		return nodes[iteratorPos++];
	}

	@Override
	public boolean hasNext() {
		return iteratorPos < size; 
	}

	@Override
	public Iterator<DepNode> iterator() {
		iteratorPos = 0;
		return this;
	}

	
}
