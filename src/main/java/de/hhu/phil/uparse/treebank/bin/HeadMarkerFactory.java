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
package de.hhu.phil.uparse.treebank.bin;

import java.io.IOException;

import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.treebank.TreebankProcessor;
import de.hhu.phil.uparse.treebank.negra.NegraHeadMarker;
import de.hhu.phil.uparse.treebank.ptb.PtbHeadMarker;
import de.hhu.phil.uparse.ui.UparseOptions;

public class HeadMarkerFactory {

	public static TreebankProcessor<Tree> getHeadMarker(UparseOptions opts) throws IOException {

		if ("negra".equals(opts.heads)) {
			return TreebankProcessor.fromTreeProcessor(new NegraHeadMarker());
		} 
		
		if ("ptb".equals(opts.heads)) {
			return TreebankProcessor.fromTreeProcessor(new PtbHeadMarker());
		} 
		
		if ("left".equals(opts.heads)) {
			return TreebankProcessor.fromTreeProcessor(new LeftHeadMarker());
		}
		
		throw new IllegalArgumentException("unknown head marker " + opts.heads);

	}

}
