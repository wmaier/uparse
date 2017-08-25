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
package de.hhu.phil.uparse.eval;

import de.hhu.phil.uparse.eval.constituent.BracketEvaluator;
import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.ui.UparseOptions;

public class EvaluatorFactory {

	public static ParseEvaluator<Tree> getConstituencyEvaluator(UparseOptions opts) throws EvaluationException {
		
		System.err.println("using evaluator " + opts.evaluator);
		
		if ("evalb".equals(opts.evaluator)) {
			return new BracketEvaluator(opts);
		}

		throw new EvaluationException("unknown evaluation method " + opts.evaluator);
		
	}
	
}
