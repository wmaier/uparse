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
package de.hhu.phil.uparse.treebank.dep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import de.hhu.phil.uparse.treebank.TreeReader;
import de.hhu.phil.uparse.treebank.Treebank;
import de.hhu.phil.uparse.treebank.TreebankException;
import de.hhu.phil.uparse.ui.UparseOptions;

public class ConllUReader implements TreeReader<DepTree> {

	UparseOptions opts;

	public ConllUReader(UparseOptions opts) {
		this.opts = opts;
	}
	

	@Override
	public Treebank<DepTree> read(InputStream inputStream) throws IOException {
		Treebank<DepTree> depbank = new Treebank<>();
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
					DepTree tree;
					try {
						tree = parseTerminals(sentence);
					} catch (TreebankException e) {
						throw new IOException(e);
					}
					tree.setId(sentenceNumber);
					if (sentenceNumber % 1000 == 0) {
						System.err.print(".");
					}
					depbank.add(tree);
					sentence.clear();
				} else {
					sentence.add(line);
				}
			}
		}
		// if there is no empty line at the end of the file
		if (sentence.size() > 0) {
			DepTree tree;
			try {
				tree = parseTerminals(sentence);
			} catch (TreebankException e) {
				throw new IOException(e);
			}
			tree.setId(sentenceNumber);
			if (sentenceNumber % 1000 == 0) {
				System.err.print(".");
			}
			depbank.add(tree);
			sentence.clear();
		}
		bufferedReader.close();
		fileReader.close();
		return depbank;
	}

	
	/*
	 * This does not parse the ranges notation yet
	 */
	private DepTree parseTerminals(ArrayList<String> sentence)
			throws TreebankException {
		int size = sentence.size();
		for (String word : sentence) {
			String[] splitLine = word.split("\\s+");
			if (splitLine[0].indexOf('-') > -1)
				size--;
		}
		DepTree tree = new DepTree(size);
		for (String word : sentence) {
			String[] splitLine = word.split("\\s+");
			/*
			 * From http://universaldependencies.github.io/docs/format.html:
			 *
			 *  ID: Word index, integer starting at 1 for each new sentence; may be a range for tokens with multiple words.
			 *  FORM: Word form or punctuation symbol.
			 *  LEMMA: Lemma or stem of word form.
			 *  CPOSTAG: Universal part-of-speech tag drawn from our revised version of the Google universal POS tags.
			 *  POSTAG: Language-specific part-of-speech tag; underscore if not available.
			 *  FEATS: List of morphological features from the universal feature inventory or from a defined language-specific extension; underscore if not available.
			 *  HEAD: Head of the current token, which is either a value of ID or zero (0).
			 *  DEPREL: Universal Stanford dependency relation to the HEAD (root iff HEAD = 0) or a defined language-specific subtype of one.
			 *  DEPS: List of secondary dependencies (head-deprel pairs).
			 *  MISC: Any other annotation.

			 */
			if (splitLine[0].indexOf('-') > -1) {
				// discard spans for the moment
				continue;
			}
			DepNode node = new DepNode();
			node.setId(Integer.valueOf(splitLine[0]));
			node.setField("form", splitLine[1]);
			node.setField("lemma", splitLine[2]);
			node.setField("cpostag", splitLine[3]);
			node.setField("postag", splitLine[4]);
			node.setField("feats", splitLine[5]);
			node.setField("head", splitLine[6]);
			node.setField("deprel", splitLine[7]);
			node.setField("deps", splitLine[8]);
			node.setField("misc", splitLine[9]);
			int head = Integer.valueOf(node.getField("head"));
			tree.setNode(node, node.getId());
			tree.setHead(node.getId(), head, node.getField("deprel"));
		}
		return tree;
	}

}
