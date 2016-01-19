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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.hhu.phil.uparse.eval.EvaluationException;
import de.hhu.phil.uparse.eval.EvaluatorFactory;
import de.hhu.phil.uparse.eval.ParseEvaluator;
import de.hhu.phil.uparse.misc.Timer;
import de.hhu.phil.uparse.misc.Utils;
import de.hhu.phil.uparse.parser.ParserException;
import de.hhu.phil.uparse.perceptron.Updates;
import de.hhu.phil.uparse.perceptron.Weights;
import de.hhu.phil.uparse.treebank.ExportWriter;
import de.hhu.phil.uparse.treebank.NodeLabel;
import de.hhu.phil.uparse.treebank.TraceHandlerFactory;
import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.treebank.TreeReader;
import de.hhu.phil.uparse.treebank.TreeReaderFactory;
import de.hhu.phil.uparse.treebank.TreeWriter;
import de.hhu.phil.uparse.treebank.TreeWriterFactory;
import de.hhu.phil.uparse.treebank.Treebank;
import de.hhu.phil.uparse.treebank.TreebankException;
import de.hhu.phil.uparse.treebank.TreebankProcessor;
import de.hhu.phil.uparse.treebank.bin.BinarizerFactory;
import de.hhu.phil.uparse.treebank.bin.HeadMarkerFactory;
import de.hhu.phil.uparse.ui.UparseOptions;

public class DiscoParser {

	private UparseOptions opts;

	private Weights<DiscoTransition> weights;

	public DiscoParser(UparseOptions opts) {
		this.opts = opts;
	}

	public void train() throws ParserException {
		Trainer<DiscoTransition> trainer = TrainerFactory.getTrainer(opts);
		// read train and dev treebanks
		Treebank<Tree> trainTreebank = null;
		Treebank<Tree> devTreebank = null;
		weights = new Weights<DiscoTransition>(opts);
		Transitionifier<Tree> transitionifier = TransitionifierFactory.getTransitionsifier(opts.transitionifier, opts);
		TreeReader<Tree> treebankReader = TreeReaderFactory.getConstituencyTreebankReader(opts.treeFormat, opts);
		try {
			System.err.println("preparing training data from " + opts.trainTreebank);
			System.err.print("reading treebank... ");
			List<TreebankProcessor<Tree>> trainingProcessors = new ArrayList<>();
			if (!opts.traceHandler.equals("none") && opts.ptbTracesBeforeBin) {
				trainingProcessors.add(TraceHandlerFactory.getTraceHandler(opts));
			}
			trainingProcessors.add(HeadMarkerFactory.getHeadMarker(opts));
			trainingProcessors.add(BinarizerFactory.getBinarizer(opts));
			if (!opts.traceHandler.equals("none") && !opts.ptbTracesBeforeBin) {
				trainingProcessors.add(TraceHandlerFactory.getTraceHandler(opts));
			}
			trainingProcessors.add(TreebankProcessor.fromTreeProcessor(weights.gram));
			trainingProcessors.add(transitionifier);
			InputStream trainTreebankFile = new FileInputStream(opts.trainTreebank);
			trainTreebank = treebankReader.read(trainTreebankFile);
			System.err.println("done.");
			System.err.print("marking heads, binarizing, extracting grammar and transitions... ");
			trainTreebank.apply(trainingProcessors);
			System.err.println("done.");
			if (opts.dumpTraining) {
				PrintStream ps = new PrintStream(new FileOutputStream("./training-dump"));
				for (Tree t : trainTreebank) {
					ExportWriter.printTree(t, ps);
				}
				System.exit(1);
			}
			System.err.print("numbering transitions... ");
			for (List<DiscoTransition> transitionList : transitionifier.getTransitions()) {
				ArrayList<DiscoTransition> myTransitionList = new ArrayList<>(transitionList);
				for (DiscoTransition transition : myTransitionList) {
					weights.index.getNumber(transition);
				}
			}
			System.err.println("done.");
 			System.err.print("dumping training treebank...");
			BufferedWriter w = new BufferedWriter(new FileWriter(opts.trainTreebank + ".out"));
			TreeWriter<Tree> tw = TreeWriterFactory.getTreeWriter("export");
			for (Tree tree : trainTreebank) {
				tw.writeTree(tree, w);
			}
			System.err.println("done.");
		} catch (IOException | TreebankException e) {
			System.err.println("could not prepare training data.");
			throw new ParserException(e);
		}
		try {
			System.err.println("preparing development data from " + opts.devTreebank);
			System.err.print("reading treebank... ");
			InputStream devTreebankFile = new FileInputStream(opts.devTreebank);
			List<TreebankProcessor<Tree>> devProcessors = new ArrayList<>();
			if (!opts.traceHandler.equals("none")) {
				opts.ptbKeepAllTraces = false;
				opts.ptbKeepTraces = new ArrayList<>();
				opts.ptbSlashTraces = new ArrayList<>();
				devProcessors.add(TraceHandlerFactory.getTraceHandler(opts));
			}
			devTreebank = treebankReader.read(devTreebankFile);
			devTreebank.apply(devProcessors);
			System.err.println("done.");
			System.err.print("dumping dev treebank...");
			BufferedWriter w = new BufferedWriter(new FileWriter(opts.devTreebank + ".out"));
			TreeWriter<Tree> tw = TreeWriterFactory.getTreeWriter("export");
			for (Tree tree : devTreebank) {
				tw.writeTree(tree, w);
			}
			System.err.println("done.");
		} catch (IOException | TreebankException e) {
			System.err.println("could not prepare dev data");
			throw new ParserException(e);
		}
		// training
		System.err.println("\nready to train!");
		if (opts.stallStop == 0) {
			System.err.println("will do " + opts.iterations + " iterations.");
			if (opts.reportDevResults) {
				System.err.println("will report results on dev for each iteration.");
			}
		} else {
			System.err.println("number of iterations will be determined by performance on dev set.");
		}
		Updates<DiscoTransition> updates = new Updates<>();
		double bestScore = Double.NEGATIVE_INFINITY;
		int bestScoreIterationNum = -1;
		File bestWeightsFile = null;
		Timer timer = new Timer();
		for (int currentIterationNum = 0; currentIterationNum < opts.iterations; ++currentIterationNum) {
			long seed = System.nanoTime();
			List<Integer> order = IntStream
					.range(0, trainTreebank.size())
					.boxed()
					.collect(Collectors.toList());
			Collections.shuffle(order, new Random(seed));
			System.err.println("\niteration " + (currentIterationNum + 1));
			// train single tree
			float totalTrainingTime = 0.0f;
			for (int treeIndex = 0; treeIndex < trainTreebank.size(); ++treeIndex) {
				int next = opts.randomize ? order.get(treeIndex) : treeIndex;
				Tree tree = trainTreebank.get(next);
				List<DiscoTransition> transitions = transitionifier.getTransitions().get(next);
				Utils.printProgress(treeIndex);
				timer.start();
				trainer.trainTree(getStartState(tree), transitions, updates, weights);
				//weights.update(updates);
				updates.applyTo(weights);
				totalTrainingTime += timer.secondsElapsed();
				updates.clear();
			}
			System.err.println("\n[" + totalTrainingTime + " seconds ("	+ (trainTreebank.size() / totalTrainingTime) + " sent/sec)]");

			if (!(opts.reportDevResults || opts.stallStop != 0)) {
				System.err.println("skip dev parsing");
				continue;
			}
			
			// parse dev
			System.err.println("\nparsing dev set...");
			Treebank<Tree> devResults = new Treebank<>();
			float totalDevparseTime = 0.0f;
			for (int treeIndex = 0; treeIndex < devTreebank.size(); ++treeIndex) {
				State startState = getStartState(devTreebank.get(treeIndex));
				Utils.printProgress(treeIndex);
				timer.start();
				Tree result = parseTree(startState, weights);
				totalDevparseTime += timer.secondsElapsed();
				if (result != null) {
					DiscoParser.postprocess(result);
					result.setId(treeIndex);
				}
				devResults.add(result);
			}
			System.err.println();
			System.err.println("[" + totalDevparseTime + " sec. ("	+ devTreebank.size() / totalDevparseTime + " sent/sec)]");
			
			// evaluate dev
			System.err.println("\nevaluating parsed dev set...");
			try {
				ParseEvaluator<Tree> e = EvaluatorFactory.getConstituencyEvaluator(opts);
				e.scoreTreebanks(devTreebank, devResults);
				double result = e.getScore();
				System.err.println(e.getScoreDescription());
				if (opts.stallStop != 0) {
					if (result <= bestScore) {
						int stall = currentIterationNum - bestScoreIterationNum;
						System.err.println("no improvement since " + stall	+ " iterations (vs. " + bestScore + ")");
						if (opts.stallStop != 0 && stall >= opts.stallStop) {
							System.err.println("stopping training");
							break;
						}
					} else {
						System.err.println("new highest score is " + result	+ " (was " + bestScore + ")");
						bestScore = result;
						bestScoreIterationNum = currentIterationNum;
						if (bestWeightsFile != null) {
							bestWeightsFile.delete();
						}
						bestWeightsFile = File.createTempFile(opts.getIdentifier(), "uparse");
						bestWeightsFile.deleteOnExit();
						System.err.println("saving intermediate weights!");
						weights.save(bestWeightsFile);
					}
				}
			} catch (EvaluationException e) {
				System.err.println("Could not complete dev evaluation");
				throw new ParserException(e);
			} catch (IOException e1) {
				System.err.println("Could not save intermediate weights");
				throw new ParserException(e1);
			}
		}
		System.err.println("training iterations finished.");
		if (opts.stallStop != 0) {
			try {
				weights = Weights.load(bestWeightsFile);
			} catch (IOException e) {
				System.err.println("could not load intermediate weights for averaging");
				throw new ParserException(e);
			}
		}
		weights = weights.average();
		System.err.println("training fully completed.");
	}

	public Tree parseTree(State startState, Weights<DiscoTransition> weights)
			throws ParserException {
		int pqSize = opts.beamSize + 1;
		State bestState = null;
		PriorityQueue<State> beam = new PriorityQueue<>(pqSize);
		beam.add(startState);
		double minScore;
		while (beam.size() > 0) {
			PriorityQueue<State> candidates = new PriorityQueue<>(pqSize);
			minScore = -Double.MAX_VALUE;
			for (State fromState : beam) {
				for (State toState : State.getExtensions(fromState, Featurizer.getFeatures(fromState, opts), opts.beamSize, weights, minScore)) {
					if (bestState == null || toState.score > bestState.score) {
						bestState = toState;
					}
					candidates.add(toState);
					minScore = candidates.peek().score;
					if (candidates.size() > opts.beamSize) {
						candidates.poll();
					}
				}
			}
			if (bestState != null && bestState.complete) {
				return bestState.stack.peek();
			}
			beam = candidates;
			bestState = null;
		}
		return null;
	}

	public void parse() throws ParserException, EvaluationException {
		System.err.println("parsing " + opts.testTreebank + "... ");
		Treebank<Tree> results = new Treebank<>();
		TreeReader<Tree> tr = TreeReaderFactory.getConstituencyTreebankReader(opts.testTreeFormat, 
				opts);
		FileInputStream f = null;
		BufferedInputStream bs = null;
		FileWriter fw = null;
		System.err.print("reading treebank... ");
		Treebank<Tree> goldTreebank = null;
		try {
			f = new FileInputStream(opts.testTreebank);
			bs = new BufferedInputStream(f);
			goldTreebank = tr.read(bs);
			if (!opts.traceHandler.equals("none")) {
				opts.ptbKeepAllTraces = false;
				opts.ptbKeepTraces = new ArrayList<>();
				opts.ptbSlashTraces = new ArrayList<>();
				TreebankProcessor<Tree> tp = TraceHandlerFactory.getTraceHandler(opts);
				goldTreebank.apply(Collections.singletonList(tp));
			}
		} catch (IOException | TreebankException e) {
			System.err.println("could not open treebank for parsing");
			throw new ParserException(e);
		}
		try {
			fw = new FileWriter(opts.outputFile);
		} catch (IOException e) {
			System.err.println("could not file for parser output");
			throw new ParserException(e);
		}
		System.err.println("done.");
		System.err.print("dumping test treebank...");
		BufferedWriter w;
		try {
			w = new BufferedWriter(new FileWriter(opts.testTreebank + ".out"));
			TreeWriter<Tree> tw = TreeWriterFactory.getTreeWriter("export");
			for (Tree tree : goldTreebank) {
				tw.writeTree(tree, w);
			}
		} catch (IOException e1) {
			try {
				// close output file
				fw.close();
			} catch (IOException e) {
				throw new ParserException(e);
			}
			throw new ParserException(e1);
		}
		System.err.println("done.");
		System.err.println("Ready to parse!");
		ExportWriter ew = new ExportWriter();
		float parsingTime = 0.0f;
		Timer timer = new Timer();
		for (int treeIndex = 0; treeIndex < goldTreebank.size(); ++treeIndex) {
			Tree goldTree = goldTreebank.get(treeIndex);
			State startState = getStartState(goldTree);
			//String sentence = getSentence(startState);
			Utils.printProgress(treeIndex);
			timer.start();
			Tree result = parseTree(startState, weights);
			parsingTime += timer.secondsElapsed();
			if (result == null) {
				if (opts.considerUnparsed) {
					result = goldTree.emptyTree();
				}
			} else {
				DiscoParser.postprocess(result);
				result.setId(treeIndex + 1);
				try {
					ew.writeTree(result, fw);
				} catch (IOException e) {
					System.err.println("Could not write parser output");
					throw new ParserException(e);
				}
			}
			results.add(result);
		}
		System.err.println("done, parsed " + goldTreebank.size() + " sentences");
		System.err.println("[" + parsingTime + " seconds ("	+ (goldTreebank.size() / parsingTime) + " sent/sec)]");
		try {
			bs.close();
			f.close();
			fw.close();
		} catch (IOException e) {
			System.err.println("could not close io during parsing");
			throw new ParserException(e);
		}
		if (!opts.noEvaluate) {
			System.err.println("evaluating...");
			try {
				ParseEvaluator<Tree> e = EvaluatorFactory.getConstituencyEvaluator(opts);
				e.scoreTreebanks(goldTreebank, results);
				System.err.println(e.getScoreDescription());
			} catch (EvaluationException e) {
				System.err.println("could not evaluate parser output");
				throw new ParserException(e);
			}
		}
	}

	private static State getStartState(Tree tree) {
		LinkedList<Tree> input = new LinkedList<>(tree.preTerminals());
		List<Integer> todo = IntStream.range(0, input.size()).boxed().collect(Collectors.toList());
		return new State(new LinkedList<>(), new LinkedList<>(), 
				Collections.unmodifiableList(input), todo, 0, false, 0);
	}

	protected static String getSentence(State state) {
		String result = "";
		for (Tree preTerminal : state.sentence) {
			result += preTerminal.children().get(0).getLabel().label + "/"
					+ preTerminal.getLabel().label + " ";
		}
		return result;
	}

	public void saveWeights(File file) throws IOException {
		weights.save(file);
	}

	public void loadWeights(File file) throws IOException {
		weights = Weights.load(file);
	}

	public static void postprocess(Tree subtree) {
		if (subtree.isPreTerminal()) {
			subtree.children().get(0).setParent(subtree);
		} else {
			List<Tree> children = new ArrayList<>(subtree.children());
			for (Tree child : children) {
				postprocess(child);
				NodeLabel label = child.getLabel();
				if (label.label.startsWith("@")) {
					for (Tree schild : child.children()) {
						schild.setParent(subtree);
						subtree.addChild(schild);
					}
					subtree.removeChild(child);
				} else {
					child.setParent(subtree);
				}
			}
		}
	}

}
