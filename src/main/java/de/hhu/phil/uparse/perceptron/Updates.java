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

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * A single update for the weight vector containing two transitions
 * (correct and incorrect) and their respective features.
 * 
 * @author wmaier
 *
 */
public class Updates<T> {
	
	private ObjectList<Update> updates;
	
	public Updates() {
		this.updates = new ObjectArrayList<Update>(30);
	}
	
	public void addUpdate(List<String> goldFeatures, int goldTransition, 
			List<String> predFeatures, int predTransition, float delta) {
		updates.add(new Update(goldFeatures, goldTransition, 
				predFeatures, predTransition, delta));
	}

	public void cut(int maxDistUpdatesIndex) {
		for (int i = maxDistUpdatesIndex; i < this.updates.size(); ++i) {
			updates.remove(i);
		}
	}

	public void clear() {
		updates.clear();
	}

	public int size() {
		return updates.size();
	}
	
	public void applyTo(Weights<T> w) {
		for (Update update : updates) {
			w.applyUpdate(update.goldFeatures, update.goldTransition,
					update.predFeatures, update.predTransition, update.delta);
		}
	}

	private class Update {
		
		private List<String> goldFeatures;
		private int goldTransition;
		private List<String> predFeatures;
		private int predTransition;
		public float delta;
	
		public Update(List<String> goldFeatures, int goldTrans,
				List<String> predFeatures, int predTrans,
				float updateDelta) {
			this.goldFeatures = goldFeatures;
			this.goldTransition = goldTrans;
			this.predFeatures = predFeatures;
			this.predTransition = predTrans;
			this.delta = updateDelta;
		}
	}

}