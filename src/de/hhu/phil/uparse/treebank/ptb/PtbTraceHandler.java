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
package de.hhu.phil.uparse.treebank.ptb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hhu.phil.uparse.treebank.NodeLabel;
import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.treebank.TreeProcessor;
import de.hhu.phil.uparse.treebank.TreebankException;
import de.hhu.phil.uparse.ui.UparseOptions;

/**
 * Trace handler for the Penn Treebank. Can selectively remove traces nodes and
 * coindexation, and do slash annotation. Mapping between traces and coindices 
 * is verified.
 * 
 * @author wmaier
 *
 */
public class PtbTraceHandler implements TreeProcessor<Tree> {
	
	private UparseOptions opts;

	public PtbTraceHandler(UparseOptions opts) {
		this.opts = opts;
	}

	@Override
	public void process(Tree tree) throws TreebankException {
		List<String> keep = opts.ptbKeepTraces;
		boolean keepall = opts.ptbKeepAllTraces;
		List<String> slash = opts.ptbSlashTraces;
		boolean doSlash = slash.size() > 0;
		List<Tree> traces = new ArrayList<>();
		for (Tree preTerminal : tree.preTerminals()) {
			if (preTerminal.isTrace()) {
				traces.add(preTerminal);
			}
		}
		Map<String,List<Tree>> indexToTraces = new HashMap<>();
		Map<String,List<Tree>> indexToFillers = new HashMap<>();
		// map indices to trace nodes and deleted indices from labels
		for (Tree trace : traces) {
			String coindex = trace.getLabel().getField("coindex");
			if (keepall || keep.contains(trace.getLabel().label)) {
				if (coindex.length() > 0) {
					if (!indexToTraces.containsKey(coindex)) {
						indexToTraces.put(coindex, new ArrayList<>());
					}
					indexToTraces.get(coindex).add(trace);
				}
			} else {
				tree.deleteTerminal(trace);
			}
		}
		// map indices to non-terminals (except preterminals)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
		for (Tree node : tree.preorder()) {
			if (node.isPreTerminal()) {
				continue;
			}
			String coindex = node.getLabel().getField("coindex");
			if (coindex.length() > 0) {
				if (!indexToFillers.containsKey(coindex)) {
					indexToFillers.put(coindex, new ArrayList<>());
				}
				indexToFillers.get(coindex).add(node);
			}
		}
		// do slash annotation
	    if (doSlash) {
	        // check if one index maps to several nonterms aka uniqueness of fillers
	    	boolean hasNonUniqueFillerIndices = false;
	    	for (String index : indexToFillers.keySet()) {
	    		hasNonUniqueFillerIndices |= indexToFillers.get(index).size() > 1;
	    	}
	    	if (hasNonUniqueFillerIndices) {
	    		// if necessary compute new mapping for later annotation
	    		System.err.println(tree.getId() + ": fillers not unique!");
	    		for (String index : indexToFillers.keySet()) {
	    			System.err.print(index + ": ");
	    			for (Tree filler : indexToFillers.get(index)) {
	    				System.err.print(filler.getLabel().label + " ");
	    			}
	    			System.err.println();
	    		}
	    		HashMap<Tree,Tree> traceFiller = new HashMap<>();
	    		int newIndex = 1;
	    		for (String index : indexToTraces.keySet()) {
	    			List<Tree> fillers = indexToFillers.get(index);
	    			for (Tree trace : indexToTraces.get(index)) {
	    				boolean mappingFound = false;
	    				Tree cursor = trace;
	    				while (cursor.parent() != null) {
	    					for (Tree filler : fillers) {
	    						if (cursor.parent() == filler) {
	    							// dominance
	    							filler.getLabel().setField("newIndex", String.valueOf(newIndex));
	    							traceFiller.put(trace, filler);
	    							newIndex++;
	    							mappingFound = true;
	    						} else {
	    							// c-command
	    							for (Tree child : cursor.parent().children()) {
	    								for (Tree myfiller : fillers) {
	    									if (child == myfiller) {
	    										child.getLabel().setField("newIndex", String.valueOf(newIndex));
	    										traceFiller.put(trace, myfiller);
	    										newIndex++;
	    										mappingFound = true;
	    										break;
	    									}
	    								}
	    								if (mappingFound) break;
	    							}
	    						}
	    						if (mappingFound) break;
	    					}
	    					if (mappingFound) break;
	    					cursor = cursor.parent();
	    				}
	    				if (cursor.parent() == null && !mappingFound) {
	    					throw new TreebankException("no mapping found");
	    				}
	    			}
	    		}
	    		newIndex = 1;
	    		Map<String,List<Tree>> newIndexToTraces = new HashMap<>();
	    		Map<String,List<Tree>> newIndexToFillers = new HashMap<>();
	    		for (Tree trace : traceFiller.keySet()) {
	    			Tree filler = traceFiller.get(trace);
	    			String index = filler.getLabel().getField("newIndex");
	    			if (!newIndexToTraces.containsKey(index)) {
	    				newIndexToTraces.put(index, new ArrayList<>());
	    			}
	    			newIndexToTraces.get(index).add(trace);
	    			if (!newIndexToFillers.containsKey(index)) {
	    				newIndexToFillers.put(index, new ArrayList<>());
	    			}
	    			newIndexToFillers.get(index).add(filler);
	    		}
	    		indexToTraces = newIndexToTraces;
	    		indexToFillers = newIndexToFillers;
	    	}
	    	

	        // check if there is no filler for some trace, fillers with index and no                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
	        // trace are no problem!
	    	boolean traceNoFiller = false;
	    	for (String index : indexToTraces.keySet()) {
	    		traceNoFiller |= !indexToFillers.containsKey(index);
	    	}
	    	if (traceNoFiller) {
	    		System.err.println(tree.getId() + ": no filler for trace");
	    		List<String> toDelete = new ArrayList<>();
	    		for (String index : indexToTraces.keySet()) {
	    			if (!(indexToFillers.containsKey(index))) {
	    				toDelete.add(index);
	    			}
	    		}
	    		for (String index : toDelete) {
	    			for (Tree trace : indexToTraces.get(index)) {
	    				tree.deleteTerminal(trace);
	    			}
	    			indexToTraces.remove(index);
	    		}
	        }

	    	// resolve traces and annotate stuff                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
	        for (String coindex : indexToTraces.keySet()) {
	        	for (Tree trace : indexToTraces.get(coindex)) {
	        		if (slash.size() > 0) {
	        			if (!(slash.contains(trace.getLabel().label))) {
	        				continue;
	        			}
	        		}
	        		// always has length 1 (see above)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
	        		Tree filler = indexToFillers.get(coindex).get(0);
	        		Tree goal = Tree.lca(filler, trace);
	        		if (goal == null) {
	        			// c-command
	        			if (Tree.isAncestor(trace, filler)) {
	        				goal = filler;
	        			} else {
	                        throw new TreebankException("filler neither c-commands nor dominates");
	        			}
	        		}
	        		// annotate path from filler to goal
	        		String annot = filler.getLabel().label;
	        		Tree cursor = filler;
	        		while (cursor != goal) {
	        			if (cursor != filler) {
	        				NodeLabel cursorLabel = cursor.getLabel();
	        				cursorLabel.addToListField("slash", annot);
	        			}
	        			cursor = cursor.parent();
	        		}
	        		cursor = trace;
	        		while (cursor != goal) {
	        			if (cursor != trace) {
	        				NodeLabel cursorLabel = cursor.getLabel();
	        				cursorLabel.addToListField("slash", annot);
	        			}
	        			cursor = cursor.parent();
	        		}
	        		if (opts.ptbUniqueSlash) {
	        			break;
	        		}
	        	}
	        }
	    }
	}

}
