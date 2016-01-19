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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UparseOptions extends Options implements Serializable {
	
    @Override
	public Map<String, Map<String, Enum<?>>> getPresets() {
    	return presets;
	}

	private static final Map<String, Map<String, Enum<?>>> presets;
	static
    {
        presets = new HashMap<String, Map<String, Enum<?>>>();
        presets.put("-tb", new HashMap<String, Enum<?>>());
        presets.get("-tb").put("ptb", PennTreebankPreset.I);
        presets.get("-tb").put("negra", NegraTreebankPreset.I);
        presets.get("-tb").put("none", NonePreset.I);
    }
		
	public static enum PennTreebankPreset {
		I;
		final public String treeFormat = "brackets";
		final public String labelFormat = "ptb";
		final public String heads = "ptb"; 
		final public String traceHandler = "ptb";
	}
	
	public static enum NegraTreebankPreset {
		I;
		final public String treeFormat = "export";
		final public String labelFormat = "generic";
		final public String heads = "negra";
		final public String traceHandler = "none";
	}

	public static enum NonePreset {
		I;
	}
	
	private static final long serialVersionUID = -2020673786673095894L;

	// PRESETS
	
	@Option(name = "-tb", help = "PRESET: tree and label format, head marking, trace handling (none*, negra, ptb)", isPreset = true)
	public String tb = "none";
	
	// locations

	@Option(name = "-train", help = "use this treebank as train set")
	public String trainTreebank = "";

	@Option(name = "-dev", help = "use this treebank as dev set")
	public String devTreebank = "";

	@Option(name = "-test", help = "use this treebank as test set")
	public String testTreebank = "";

	@Option(name = "-model", help = "save or load model from here")
	public String model = "";

	@Option(name = "-out", help = "write parser output here")
	public String outputFile = "";

	// treebank stuff
	
	@Option(name = "-treeFormat", help = "format of train and dev set trees (export*, brackets)")
	public String treeFormat = "export";

	@Option(name = "-testTreeFormat", help = "format of test set trees (export*, brackets, terminals) ")
	public String testTreeFormat = "export";

	@Option(name = "-labelFormat", help = "node label format (none, generic*, ptb)")
	public String labelFormat = "generic";
	
	@Option(name = "-useFunctionTags", help = "use full tags with function tags for parsing")
	public boolean useFunctionTags = false;
	
	@Option(name = "-traceHandler", help = "trace handling (none*, ptb)")
	public String traceHandler = "none";

	// TODO: trace management
	// @Option(name = "-ptbTracesBeforeBin", help = "do trace handling before binarization (or *afterwards)")
	public boolean ptbTracesBeforeBin = true;
	
	// @Option(name = "-ptbKeepTraces", help = "types of traces to keep (if present), default is deletion")
	public ArrayList<String> ptbKeepTraces = new ArrayList<>();
	
	// @Option(name = "-ptbKeepAllTraces", help = "keep all traces (if present), overrides ptbKeepTraces")
	public boolean ptbKeepAllTraces = false;
	
	// TODO: slash annotation 
	//@Option(name = "-ptbSlashTraces", help = "out of kept traces, slash-annotate those")
	public ArrayList<String> ptbSlashTraces = new ArrayList<>();
	
	//@Option(name = "-ptbUniqueSlash", help = "only slash-annotate the FIRST filler with a certain index (important for *RNR*, etc.)")
	public boolean ptbUniqueSlash = false;

	// binarization
	
	@Option(name = "-heads", help = "head marking algorithm for train/dev (negra, ptb, left*)")
	public String heads = "left";
	
	@Option(name = "-binarizer", help = "binarization algorithm (head*)")
	public String binarizer = "head";

	// transitions
	
	@Option(name = "-transitionifier", help = "how to get transitions (sswap, cswap*, dshift, topdown)")
	public String transitionifier = "cswap";
	
	@Option(name = "-continuifierMode", help = "continuifier mode")
	public String continuifierMode = "left";
	
	@Option(name = "-continuifierDistanceThreshold", help = "threshold for continuifier 'dist' mode")
	public int distanceThreshold = 0;
	
	@Option(name = "-noIdle", help = "no IDLE padding transition (default false) (Zhu et al. 2013)")
	public boolean noidle = false;
	
	// debug
	
	@Option(name = "-dumpTraining", help = "dump processed training set")
	public boolean dumpTraining = false;
	
	// parser stuff

	// for the future
	// @Option(name = "-parser", help = "parser type (disco*)")
	public String parser = "disco";

	@Option(name = "-randomize", help = "randomize tree order in training set")
	public boolean randomize = false;

	@Option(name = "-iterations", help = "maximum training iterations (20*)")
	public int iterations = 15;

	@Option(name = "-beamSize", help = "beam size in the parser (4*)")
	public int beamSize = 4;
	
	@Option(name = "-trainBeamSize", help = "override -beamSize during training (4*)")
	public int trainBeamSize = 4;
	
	//@Option(name = "-updateMode", help = "perceptron update mode (early*, maxviolation, latest)")
	public String updateMode = "early";
	
	@Option(name = "-stallStop", help = "stop training if no improvement on dev after this many iterations, 0 means no stopping (0*)")
	public int stallStop = 5;
	
	@Option(name = "-features", help = "special feature sets, default nothing (extended, separator, disco)")
	public ArrayList<String> features = new ArrayList<>();

	@Option(name = "-minUpdate", help = "sparse perceptron minUpdate (Goldberg & Elhadad, 2011) (0*)")
	public int minupdate = 0;

	@Option(name = "-grammar", help = "use grammar as filter")
	public boolean grammar = false;
	
	// disco stuff

	@Option(name = "-swapImportance", help = "count features in swap transitions n times (1*)")
	public int swapImportance = 1;
	
	// eval
	
	// evalb is default for the moment
	// @Option(name = "-evaluator", help = "evaluation mode (evalb*)")
	public String evaluator = "evalb";
	
	@Option(name = "-reportDevResults", help = "parse dev even if -stallStop is 0")
	public boolean reportDevResults = false;
	
	@Option(name = "-evalLabelEquivalencies", help = "list of label equivalences (equivalent labels colon-separated)")
	public ArrayList<String> evallabelequivalencies = new ArrayList<>();
	
	@Option(name = "-evalWithRoot", help = "include root label in evaluation")
	public boolean evalwithroot = false;
	
	@Option(name = "-considerUnparsed", help = "consider unparsed sentences in evaluation")
	public boolean considerUnparsed = true;

	@Option(name = "-noEvaluate", help = "switch off automatic evaluation on test set")
	public boolean noEvaluate = false;

	public final static String uncommentedUsage = "Usage: de.hhu.phil.uparse.ui.Uparse [PARAMS]\n\n"
			+ "Shift-reduce parser. If a training set and a dev treebank are\n"
			+ "specified, we train a new model and save it, otherwise we\n"
			+ "try to load a model. Then we parse the test set and evaluate it.\n"
			+ "Use -generate to generate a sample parameter file.\n"
			+ "\n";

	public final static String commentedUsage = "# Usage: de.hhu.phil.uparse.ui.Uparse [PARAMS]\n"
			+ "# Shift-reduce parser. If a training set and a dev treebank are\n"
			+ "# specified, we train a new model and save it, otherwise we\n"
			+ "# try to load a model. Then we parse the test set and evaluate it.\n"
			+ "# \n";

	
	// build an identifier for this parameter combination
	public String getIdentifier() {
		String featureString = "";
		for (String feature : features) {
			featureString += "-" + feature;
		}
		return heads + "-i" + iterations + "-b" + beamSize + "-p" + parser + 
				"-f" + featureString + "-mu" + minupdate + "-imp" + swapImportance;
	}

}
