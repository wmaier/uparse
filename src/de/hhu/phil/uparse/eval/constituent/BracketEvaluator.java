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
package de.hhu.phil.uparse.eval.constituent;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hhu.phil.uparse.eval.EvaluationException;
import de.hhu.phil.uparse.eval.ParseEvaluator;
import de.hhu.phil.uparse.treebank.Constants;
import de.hhu.phil.uparse.treebank.Tree;
import de.hhu.phil.uparse.treebank.Treebank;
import de.hhu.phil.uparse.ui.UparseOptions;

/**
 * evalb implementation
 * @author wmaier
 *
 */
public class BracketEvaluator implements ParseEvaluator<Tree> {

	private Map<String, String> equivalencies = new HashMap<String, String>();

	private boolean withRoot = false;

	private int totalMatch;

	private int totalAnswer;

	private int totalGold;
	
	private int missing;

	private int totalSent;
	
	private boolean considerUnparsed;

	public BracketEvaluator(UparseOptions opts) {
		this.equivalencies = new HashMap<String, String>();
		for (String labeleq : opts.evallabelequivalencies) {
			String[] labels = labeleq.split(":");
			String first = labels[0];
			for (int i = 1; i < labels.length; ++i) {
				equivalencies.put(labels[i], first);
			}
		}
		this.withRoot = opts.evalwithroot;
		this.considerUnparsed = opts.considerUnparsed;
	}

	private List<DiscoBracket> getBrackets(Tree tree) {
		List<DiscoBracket> result = new ArrayList<>();
		for (Tree subtree : tree.preorder()) {
			if (subtree.isPreTerminal() || subtree.children().size() == 0) {
				continue;
			}
			if (!withRoot
					&& subtree.getLabel().label.equals(Constants.DEFAULT_ROOT)) {
				continue;
			}
			BitSet cover = new BitSet();
			List<Tree> preTerminals = subtree.preTerminals();
			for (Tree preTerminal : preTerminals) {
				cover.set(preTerminal.nodeNumber());
			}
			String label = subtree.getLabel().label;
			if (equivalencies.containsKey(label)) {
				label = equivalencies.get(label);
			}
			result.add(new DiscoBracket(label, cover));
		}
		return result;
	}

	@Override
	public void scoreTreebanks(Treebank<Tree> gold, Treebank<Tree> answer)
			throws EvaluationException {
		int missing = 0;
		int totalMatch = 0;
		int totalGold = 0;
		int totalAnswer = 0;
		for (int i = 0; i < gold.size(); ++i) {
			int localGold = 0;
			int localAnswer = 0;
			int localMatch = 0;
			Tree goldTree = gold.get(i);
			Tree answerTree = answer.get(i);
			if (goldTree == null) {
				throw new EvaluationException("gold tree no. " + i + " was null, aborting");
			}
			List<DiscoBracket> goldBrackets = getBrackets(goldTree);
			localGold = goldBrackets.size();
			if (answerTree == null && !considerUnparsed) {
				missing++;
				continue;
			}
			List<DiscoBracket> answerBrackets = answerTree != null ? 
					answerBrackets = getBrackets(answerTree) : new ArrayList<DiscoBracket>();
			localAnswer = answerBrackets.size();
			for (DiscoBracket answerBracket : answerBrackets) {
				for (DiscoBracket goldBracket : goldBrackets) {
					if (!goldBracket.seen) {
						if (goldBracket.equals(answerBracket)) {
							goldBracket.seen = true;
							localMatch++;
						}
					}
				}
			}
			totalMatch += localMatch;
			totalGold += localGold;
			totalAnswer += localAnswer;
		}
		this.totalMatch = totalMatch;
		this.totalAnswer = totalAnswer;
		this.totalGold = totalGold;
		this.missing = missing;
		this.totalSent = gold.size();
	}

	@Override
	public boolean treeEquals(Tree a, Tree b) {
		ArrayList<Tree> aPreorder = a.preorder();
		ArrayList<Tree> bPreorder = b.preorder();
		if (aPreorder.size() != bPreorder.size()) {
			return false;
		}
		for (int i = 0; i < aPreorder.size(); ++i) {
			Tree aTreeI = aPreorder.get(i);
			Tree bTreeI = bPreorder.get(i);
			if (!aTreeI.getLabel().equals(bTreeI.getLabel())) {
				return false;
			}
			List<Tree> aPreTerms = aTreeI.preTerminals();
			List<Tree> bPreTerms = bTreeI.preTerminals();
			if (aPreTerms.size() != bPreTerms.size()) {
				return false;
			}
			for (int j = 0; j < aPreTerms.size(); ++j) {
				if (!aPreTerms.get(j).getLabel()
						.equals(bPreTerms.get(j).getLabel())) {
					return false;
				}
			}
		}
		return true;
	}

	public double getPrecision() {
		return totalMatch / new Double(totalAnswer);
	}

	public double getRecall() {
		return totalMatch / new Double(totalGold);
	}

	public double getF1() {
		double prec = getPrecision();
		double rec = getRecall();
		return 2 * prec * rec / new Double(prec + rec);
	}

	@Override
	public double getScore() {
		double result = getF1();
		return result;
	}

	@Override
	public String getScoreDescription() {
		String result = "total no. of sentences: " + totalSent + ", evaluated: " + (totalSent - missing)
				+ ", no parse: " + missing + "\n";
		result = "gold: " + totalGold + ", answer: " + totalAnswer + ", match: " + totalMatch + "\n";
		result += "precision: " + getPrecision() + "\n";
		result += "recall   : " + getRecall() + "\n";
		result += "f1       : " + getF1() + "\n";
		return result;
	}


}
