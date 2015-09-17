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
package de.hhu.phil.uparse.perceptron;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Weights for all transitions with respect to a single feature. In other words
 * reflects how often that feature helped for each possible transition.
 * @author wmaier
 *
 */
public class FeatureWeights implements Serializable {

	private int[] ind;
	
	public float[] data;
	
	public float[] totals;
	
	public int used;
	
	private final static int DEFAULT_SIZE = 1;
	
	public FeatureWeights() {
		ind = new int[DEFAULT_SIZE];
		data = new float[DEFAULT_SIZE];
		totals = new float[DEFAULT_SIZE];
		used = 0;
	}
	
	private void grow() {
		ind = Arrays.copyOf(ind, ind.length + 1);
		data = Arrays.copyOf(data, data.length + 1);
		totals = Arrays.copyOf(totals, totals.length + 1);
	}
	
	public void sumScoresForAllTransitions(float[] scores) {
		for (int i = 0; i < ind.length; ++i) {
			scores[ind[i]] += data[i];
		}
	}
	
	public void increment(int trans, float inc, int updateCounter) {
		used++;
		for (int i = 0; i < ind.length; ++i) {
			if (ind[i] == trans) {
				data[i] += inc;
				totals[i] += inc * updateCounter;
				return;
			}
		}
		grow();
		ind[ind.length - 1] = trans;
		data[data.length - 1] = inc;
		totals[data.length - 1] = inc * updateCounter;
	}


	public float getAveraged(int trans, int updateCount) {
		for (int i = 0; i < ind.length; ++i) {
			if (ind[i] == trans) {
				return data[i] - totals[i] / updateCount;
			}
		}
		return Float.NaN;
	}

	private static final long serialVersionUID = 416741701853707310L;
	
}
