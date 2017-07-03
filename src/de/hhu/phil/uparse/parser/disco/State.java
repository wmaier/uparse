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

/**
 * A parser state.
 * 
 * @author wmaier
 *
 */
public class State implements Comparable<State> {

	public double score;
	
	public LinkedList<Tree> stack;
	
	public int splitPoint; // for Coavoux GAP parser. A deque and a stack would be better.
	
	public LinkedList<DiscoTransition> transitions;
	
	public List<Tree> sentence;
	
	public boolean complete;
	
	// This is the "queue". Normally this is just an integer pointing 
	// to a position in the sentence. However, since we do discontinuous 
	// parsing, a single pointer is not enough, we need a "to-do" list 
	// of indices, where we successively remove the indices corresponding
	// to terminals already shifted.
	public List<Integer> todo;
	
	// for certain features
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

	/**
	 * Compute extensions of a state.
	 * @param state The state itself.
	 * @param features The features describing the state.
	 * @param beamsize The beam to consider.
	 * @param weights The global feature vector.
	 * @param minScore A threshold under which to discard resulting extensions.
	 * @return The possible states reachable from the given state.
	 */
	public static Collection<State> getExtensions(State state,
			List<String> features, int beamsize, 
			Weights<DiscoTransition> weights, double minScore) {
		// scores for each possible transition from the state.
		float[] scores = weights.weight(features);
		// build a list of integers (transition ids), sort it by score,
		// filter out all transitions which have a new score lower than the 
		// given threshold
		List<Integer> sortedIndices = IntStream
				.range(0, scores.length)
				.filter(i -> scores[i] + state.score >= minScore)
				.boxed()
				.sorted((i, j) -> Float.compare(scores[j], scores[i]))
				.collect(Collectors.toList());
		ArrayDeque<State> extensions = new ArrayDeque<>();
		// now build all the new states reachable from the given state with 
		// the transition list just computed.
		for (int i = 0; i < sortedIndices.size() && extensions.size() < beamsize; ++i) {
			int index = sortedIndices.get(i);
			if (weights.index.getObject(index).isLegal(state, weights.gram)) {
				extensions.add(weights.index.getObject(index).extend(state, scores[index]));
			}
		}
		return extensions;
	}

}	