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

import de.hhu.phil.uparse.ui.UparseOptions;

/**
 * Read parser input with or without tags (column conll format)
 * @author wmaier
 *
 */
public class TerminalsReader implements TreeReader<Tree> {

	UparseOptions opts;

	public TerminalsReader(UparseOptions opts) {
		this.opts = opts;
	}

	@Override
	public Treebank<Tree> read(InputStream inputStream) throws IOException {
		Treebank<Tree> treebank = new Treebank<>();
		InputStreamReader fileReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		boolean isInSentence = false;
		ArrayList<String> sentence = new ArrayList<>();
		int sentenceNumber = 0;
		while ((line = bufferedReader.readLine()) != null) {
			line = line.trim();
			if (!isInSentence) {
				if (!line.isEmpty()) {
					isInSentence = true;
					sentence.add(line);
				}
			} else {
				if (line.isEmpty()) {
					isInSentence = false;
					Tree tree;
					try {
						tree = parseTerminals(sentence);
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

	private Tree parseTerminals(ArrayList<String> sentence)
			throws TreebankException {
		NodeLabel rootLabel = new NodeLabel(Constants.DEFAULT_ROOT);
		Tree root = new Tree(rootLabel);
		root.setNodeNumber(0);
		for (String word : sentence) {
			String[] splitLine = word.split("\\s+");
			Tree wordTree = new Tree(new NodeLabel(splitLine[0]));
			NodeLabel posLabel = new NodeLabel(splitLine.length > 0 ? splitLine[1] : Constants.UNKNOWN);
			Tree posTree = new Tree(posLabel);
			root.addChild(posTree);
			posTree.setParent(root);
			posTree.addChild(wordTree);
			wordTree.setParent(posTree);
		}
		return root;
	}

}
