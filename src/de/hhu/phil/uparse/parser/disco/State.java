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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.hhu.phil.uparse.perceptron.Weights;
import de.hhu.phil.uparse.treebank.Tree;

public class State implements Comparable<State> {

	public double score;
	
	public LinkedList<Tree> stack;
	
	public LinkedList<DiscoTransition> transitions;
	
	public List<Tree> sentence;
	
	public boolean complete;
	
	// since we do discontinuous parsing, a single pointer is not enough
	public List<Integer> todo;
	
	public int lastShiftDist;

	public State(LinkedList<Tree> stack, LinkedList<DiscoTransition> trans,
			List<Tree> list, List<Integer> todo, double score, boolean complete,
			int lastdshift) {
		this.stack = stack;
		this.transitions = trans;
		this.sentence = list;
		this.todo = todo;
		this.score = score;
		this.complete = complete;
		this.lastShiftDist = lastdshift;
	}

	/*public State(LinkedList<Tree> stack, LinkedList<DiscoTransition> trans,
			List<Tree> list, List<Integer> todo, double score, boolean complete) {
		this(stack, trans, list, todo, score, complete, 0);
	}*/

	public String toString() {
		return "[" + stack.toString() + ", " + transitions.toString() + ", " + todo + ", " + complete + "]";
	}

	@Override
	public int compareTo(State o) {
		return Double.compare(score, o.score);
	}

	public static Collection<State> getExtensions(State state,
			List<String> features, int beamsize, 
			Weights<DiscoTransition> weights, double minScore) {
		float[] scores = weights.weight(features);
		List<Integer> sortedIndices = IntStream
				.range(0, scores.length)
				.filter(i -> scores[i] + state.score >= minScore)
				.boxed()
				.sorted((i, j) -> Float.compare(scores[j], scores[i]))
				.collect(Collectors.toList());
		ArrayDeque<State> extensions = new ArrayDeque<>();
		for (int i = 0; i < sortedIndices.size() && extensions.size() < beamsize; ++i) {
			int index = sortedIndices.get(i);
			if (weights.index.getObject(index).isLegal(state, weights.gram)) {
				extensions.add(weights.index.getObject(index).extend(state, scores[index]));
			}
		}
		return extensions;
	}

}	