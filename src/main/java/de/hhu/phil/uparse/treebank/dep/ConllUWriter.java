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

import java.io.IOException;
import java.io.Writer;

import de.hhu.phil.uparse.treebank.TreeWriter;
import de.hhu.phil.uparse.treebank.Treebank;

public class ConllUWriter implements TreeWriter<DepTree> {
	
	public static String conllUFormat(DepNode node) {
		return String.format("%d %s %s %s %s %s %s %s %s %s",
				node.getId(),
				node.getField("form"),
				node.getField("lemma"),
				node.getField("cpostag"),
				node.getField("postag"),
				node.getField("feats"),
				node.getField("head"),
				node.getField("deprel"),
				node.getField("deps"),
				node.getField("misc"));
	}
	
	public void writeTree(DepTree result, Writer fw) throws IOException {
		for (DepNode node : result) {
			if (node == null)
				continue;
			fw.write(conllUFormat(node) + "\n");
		}
		fw.write("\n");
	}

	@Override
	public void writeTreebank(Treebank<DepTree> treebank, Writer writer) throws IOException {
		for (DepTree dt : treebank) {
			writeTree(dt, writer);
			writer.write("\n");
		}
	}

}
