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
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ExportWriter implements TreeWriter<Tree> {

	@Override
	public void writeTreebank(Treebank<Tree> treebank, Writer writer)
			throws IOException {
		for (Tree tree : treebank) {
			writeTree(tree, writer);
		}
	}

	public void writeTree(Tree tree, Writer writer, boolean slash) throws IOException {
		String result = "#BOS " + tree.getId() + "\n";
		computeExportNumbering(tree);
		for (Tree preTerminal : tree.preTerminals()) {
			result += exportFormat(preTerminal);
		}
		ArrayList<Tree> allNodes = tree.preorder();
		allNodes.sort((a, b) -> a.nodeNumber() - b.nodeNumber());
		for (Tree node : allNodes) {
			if (node.parent() == null || node.isPreTerminal()
					|| node.isTerminal()) {
				continue;
			}
			result += exportFormat(node, slash);
		}
		result += "#EOS " + tree.getId() + "\n";
		writer.write(result);
	}
	
	@Override
	public void writeTree(Tree tree, Writer writer) throws IOException {
		writeTree(tree, writer, false);
	}
	
	public String exportFormat(Tree tree, boolean slash) {
		String word = null;
		if (tree.isPreTerminal()) {
			word = tree.children().get(0).getLabel().label;
		} else {
			word = "#" + String.valueOf(tree.nodeNumber());
		}
		String result = word + exportTabs(word.length());
		result += tree.getLabel().label;
		if (slash) {
			result += "-" + tree.getLabel().getListField("slash").toString();
		}
		// if (tree.isHead()) result += "'";
		result += "\t";
		String morph = tree.getLabel().getField("morph");
		if (morph.length() == 0) {
			morph = "--";
		}
		result += morph;
		result += exportTabs(tree.getLabel().getField("morph").length() + 8);
		String edge = tree.getLabel().getField("edge");
		if (edge.length() == 0) {
			edge = "--";
		}
		result +=  edge + "\t";
		result += tree.parent().nodeNumber();
		result += "\n";
		return result;
	}

	public String exportFormat(Tree tree) {
		return exportFormat(tree, false);
	}

	public static String exportTabs(int length) {
		if (length < 8) {
			return "\t\t\t";
		} else if (length < 16) {
			return "\t\t";
		} else {
			return "\t";
		}
	}

	public static void computePathLengths(
			HashMap<Integer, ArrayList<Tree>> pathLengthToTrees, Tree tree) {
		if (tree.isPreTerminal()) {
			return;
		}
		int level = 0;
		for (Tree it : tree.preTerminals()) {
			int pathLength = 0;
			Tree pathElement = it;
			while (pathElement != tree) {
				pathElement = pathElement.parent();
				pathLength++;
			}
			level = level > pathLength ? level : pathLength;
		}
		if (!pathLengthToTrees.containsKey(level)) {
			pathLengthToTrees.put(level, new ArrayList<Tree>());
		}
		pathLengthToTrees.get(level).add(tree);
		for (Tree child : tree.children()) {
			computePathLengths(pathLengthToTrees, child);
		}
	}

	public static void computeExportNumbering(Tree tree) {
		HashMap<Integer, ArrayList<Tree>> levels = new HashMap<Integer, ArrayList<Tree>>();
		computePathLengths(levels, tree);
		int nonterminalNumber = 500;
		ArrayList<Integer> keys = new ArrayList<Integer>(levels.size());
		for (int levelNum : levels.keySet()) {
			Collections.sort(levels.get(levelNum),
					(a, b) -> (a.preTerminals().get(0).nodeNumber() - b
							.preTerminals().get(0).nodeNumber()));
			keys.add(levelNum);
		}
		keys.sort((a, b) -> a - b);
		for (int levelNumber : keys) {
			for (Tree theTree : levels.get(levelNumber)) {
				if (theTree != tree) {
					theTree.setNodeNumber(nonterminalNumber++);
				}
			}
		}
	}

	public static void printTree(Tree tree) {
		StringWriter w = new StringWriter();
		ExportWriter ew = new ExportWriter();
		try {
			ew.writeTree(tree, w);
		} catch (IOException e) {
			System.err.println("Could not write tree");
		}
		System.err.println(w.getBuffer().toString());
	}

	public static void printTreeWithSlash(Tree tree) {
		StringWriter w = new StringWriter();
		ExportWriter ew = new ExportWriter();
		try {
			ew.writeTree(tree, w, true);
		} catch (IOException e) {
			System.err.println("Could not write tree");
		}
		System.err.println(w.getBuffer().toString());		
	}

}
