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
package de.hhu.phil.uparse.ui;

import java.io.IOException;

import de.hhu.phil.uparse.eval.EvaluationException;
import de.hhu.phil.uparse.parser.Parser;
import de.hhu.phil.uparse.parser.ParserException;
import de.hhu.phil.uparse.parser.ParserFactory;
import de.hhu.phil.uparse.treebank.TreebankException;


public class Uparse {

	public static void main(String[] args) throws OptionException,
			ParserException, EvaluationException, IOException, TreebankException {
		UparseOptions options = null;
		if (args.length == 1) {
			if (args[0].startsWith("-generate")) {
				OptionTools.generateParameterTemplate("./parameters",
						UparseOptions.commentedUsage, UparseOptions.class);
				System.exit(0);
			} else if (args[0].equals("-h") || args[0].equals("--help")) {
				OptionTools.usage(UparseOptions.uncommentedUsage,
						UparseOptions.class);
			} else {
				args = OptionTools.readCommandLineParamsFromFile(args[0]);
			}
		} else {
			System.err.println("please specify a parameter file, '-generate'"
					+ " generates a sample file.");
		}
		options = OptionTools.parseCommandLineParams(args, UparseOptions.class);
		Parser parser = ParserFactory.getParser(options.parser, options);
		parser.run();
	}

}
