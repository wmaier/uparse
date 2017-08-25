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

import java.util.List;
import java.util.PriorityQueue;

import de.hhu.phil.uparse.parser.ParserException;
import de.hhu.phil.uparse.perceptron.Updates;
import de.hhu.phil.uparse.perceptron.Weights;
import de.hhu.phil.uparse.ui.UparseOptions;

/**
 * As in Collins & Roark (2004).
 * @author wmaier
 *
 */
public class EarlyUpdateTrainer extends Trainer<DiscoTransition> {

	public EarlyUpdateTrainer(UparseOptions opts) {
		super(opts);
	}
	
	@Override
	public boolean trainTree(State goldFromState, List<DiscoTransition> goldTransitions,
			Updates<DiscoTransition> updates, Weights<DiscoTransition> weights) throws ParserException {
		int pqSize = opts.trainBeamSize + 1;
		PriorityQueue<State> agenda = new PriorityQueue<>(pqSize);
		agenda.add(goldFromState);
		double minScore;
		for (int i = 0; i < goldTransitions.size(); ++i) {
			DiscoTransition goldTransition = goldTransitions.get(i);
			PriorityQueue<State> candidates = new PriorityQueue<>(pqSize);
			State bestToState = null;
			State bestFromState = null;
			minScore = -Double.MAX_VALUE;
			for (State fromState : agenda) {
				for (State toState : State.getExtensions(fromState, 
						Featurizer.getFeatures(fromState, opts), 
						opts.trainBeamSize, weights, minScore)) {
					candidates.add(toState);
					minScore = candidates.peek().score;
					if (candidates.size() > opts.trainBeamSize) {
						candidates.poll();
					}
					if (bestToState == null || bestToState.score < toState.score) {
						bestToState = toState;
						bestFromState = fromState;
					}
				}
			}
			State goldToState = goldTransition.extend(goldFromState, 0.0);
			if (bestToState == null) {
				// got stuck, no further legal transitions found
				break;
			}
			if (!goldToState.transitions.equals(bestToState.transitions)) {
				float updateDelta = 1.0f;
				if (goldTransition instanceof GapTransition) {
					updateDelta *= opts.swapImportance;
				}
				List<String> goldFeatures = Featurizer.getFeatures(goldFromState, opts);
				int goldTransitionIndex = weights.index.getNumber(goldTransition);
				List<String> parserFeatures = Featurizer.getFeatures(bestFromState, opts);
				int parserTransitionIndex = weights.index.getNumber(bestToState.transitions.peek());
				updates.addUpdate(goldFeatures, goldTransitionIndex, 
						parserFeatures, parserTransitionIndex, updateDelta);
				//updates.add(new Update(goldFeatures, goldTransitionIndex, parserFeatures, parserTransitionIndex, updateDelta));
				boolean goldStateOnAgenda = false;
				for (State candidate : candidates) {
					if (candidate.transitions.equals(goldToState.transitions)) {
						goldStateOnAgenda = true;
						break;
					}
				}
				if (!goldStateOnAgenda) {
					break;
				}
			} 
			goldFromState = goldToState;
			agenda = candidates;
		}
		return goldTransitions.size() == 0;
	}

}
