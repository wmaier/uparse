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

import java.io.File;
import java.io.IOException;

import de.hhu.phil.uparse.eval.EvaluationException;
import de.hhu.phil.uparse.parser.ParserException;
import de.hhu.phil.uparse.ui.UparseOptions;

public class DiscoParserPipeline implements de.hhu.phil.uparse.parser.Parser {

	private UparseOptions opts;

	public DiscoParserPipeline(UparseOptions options) {
		this.opts = options;
	}

	public void run() throws ParserException, EvaluationException, IOException {
		DiscoParser model = new DiscoParser(opts);
		if (!"".equals(opts.trainTreebank) && !"".equals(opts.devTreebank)) {
			model.train();
			model.saveWeights(new File(opts.model));
		} else if (!"".equals(opts.model)) {
			model.loadWeights(new File(opts.model));
		} else {
			throw new ParserException("must specify train/dev combination or model");
		}
		if (!"".equals(opts.testTreebank) && !"".equals(opts.outputFile)) {
			System.err.println("parsing...");
			model.parse();
			System.err.println("done.");
		} else {
			System.err.println("No test data or output file provided, done.");
		}
	}

}
