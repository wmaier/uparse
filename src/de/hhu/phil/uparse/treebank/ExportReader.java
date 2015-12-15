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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import de.hhu.phil.uparse.ui.UparseOptions;

public class ExportReader implements TreeReader<Tree> {

	private UparseOptions opts;

	public ExportReader(UparseOptions opts) {
		this.opts = opts;
		if (!opts.labelFormat.equals("none")) {
			System.err.println("export format: splitting labels!");
		}
	}

	@Override
	public Treebank<Tree> read(InputStream inputStream) throws IOException {
		Treebank<Tree> treebank = new Treebank<Tree>();
		InputStreamReader fileReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		boolean isInSentence = false;
		ArrayList<String> sentence = new ArrayList<>();
		int sentenceNumber = 0;
		while ((line = bufferedReader.readLine()) != null) {
			if (!isInSentence) {
				if (line.startsWith("#BOS")) {
					isInSentence = true;
					String[] splitLine = line.split("\\s+");
					sentenceNumber = Integer.valueOf(splitLine[1]);
				}
			} else {
				if (line.startsWith("#EOS")) {
					isInSentence = false;
					Tree tree;
					try {
						tree = parseExportSentence(sentence, opts);
					} catch (TreebankException e) {
						throw new IOException(e);
					}
					tree.setId(sentenceNumber);
					if (sentenceNumber % 1000 == 0) {
						System.err.print(".");
					}
					treebank.add(tree);
					sentence.clear();
				} else {
					sentence.add(line);
				}
			}
		}
		bufferedReader.close();
		fileReader.close();
		return treebank;
	}

	public static Tree parseExportSentence(ArrayList<String> sentence, UparseOptions opts)
			throws TreebankException {
		HashMap<Integer, Tree> nodes = new HashMap<>();
		HashMap<Integer, ArrayList<Integer>> idsToChildIds = new HashMap<Integer, ArrayList<Integer>>();
		int terminalCount = 0;
		for (String line : sentence) {
			String[] sp = line.trim().split("\\s+");
			if (sp[0].length() == 4 && sp[0].charAt(0) == '#'
					&& Character.isDigit(sp[0].charAt(1))
					&& Character.isDigit(sp[0].charAt(2))
					&& Character.isDigit(sp[0].charAt(3))) {
				int nodeNumber = Integer.valueOf(sp[0].substring(1));
				int parent = Integer.valueOf(sp[4]);
				if (!idsToChildIds.containsKey(parent)) {
					idsToChildIds.put(parent, new ArrayList<Integer>());
				}
				idsToChildIds.get(parent).add(nodeNumber);
				NodeLabel label = null;
				label = NodeLabel.fromString(sp[1] + "-" + sp[3], opts);
				label.setField("morph", sp[2]);
				Tree node = new Tree(label);
				node.setNodeNumber(nodeNumber);
				nodes.put(nodeNumber, node);
			} else {
				int terminalNumber = ++terminalCount;
				NodeLabel terminalLabel = new NodeLabel(sp[0]);
				terminalLabel.setField("morph", sp[2]);
				terminalLabel.setField("edge", sp[3]);
				Tree terminalNode = new Tree(terminalLabel);
				terminalNode.setNodeNumber(terminalNumber);
				int preTerminalNumber = terminalNumber;
				NodeLabel preTerminalLabel = NodeLabel.fromString(sp[1] + "-"
						+ sp[3], opts);
				preTerminalLabel.setField("morph", sp[2]);
				Tree preTerminalNode = new Tree(preTerminalLabel);
				preTerminalNode.setNodeNumber(preTerminalNumber);
				preTerminalNode.addChild(terminalNode);
				int parent = Integer.valueOf(sp[4]);
				if (!idsToChildIds.containsKey(parent)) {
					idsToChildIds.put(parent, new ArrayList<Integer>());
				}
				idsToChildIds.get(parent).add(preTerminalNumber);
				nodes.put(preTerminalNumber, preTerminalNode);
			}
		}
		NodeLabel rootLabel = new NodeLabel(Constants.DEFAULT_ROOT);
		Tree root = new Tree(rootLabel);
		root.setNodeNumber(0);
		nodes.put(0, root);
		for (int currentNodeNumber : idsToChildIds.keySet()) {
			Tree parent = nodes.get(currentNodeNumber);
			for (int childNumber : idsToChildIds.get(currentNodeNumber)) {
				Tree child = nodes.get(childNumber);
				parent.addChild(child);
				child.setParent(parent);
			}
		}
		return root;
	}

}
