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

import de.hhu.phil.uparse.ui.UparseOptions;


public class TreeReaderFactory {

	public static TreeReader<Tree> getConstituencyTreebankReader(String treeFormat, 
			UparseOptions opts) {

		if ("export".equals(treeFormat)) {
			return new ExportReader(opts);
		}
		
		if ("brackets".equals(treeFormat)) {
			return new BracketsReader(opts);
		}
		
		if ("terminals".equals(treeFormat)) {
			return new TerminalsReader(opts);
		}
		
		throw new IllegalArgumentException("Unknown treebank format " + opts.treeFormat);

	}

}
