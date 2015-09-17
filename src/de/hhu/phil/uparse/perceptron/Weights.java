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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import de.hhu.phil.uparse.misc.Numberer;
import de.hhu.phil.uparse.treebank.Grammar;
import de.hhu.phil.uparse.ui.UparseOptions;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * The actual feature weights. Uses fastutil.
 * @author wmaier
 *
 */
public class Weights<T> implements Serializable {
	
	private UparseOptions opts;

	private Object2ObjectMap<String, FeatureWeights> data;

	// update counter
	protected int i = 0;
	
	// transition index
	public Numberer<T> index;

	// grammar 
	public Grammar gram;
	
	final private boolean isAveraged;
	
	public Weights(UparseOptions opts) {
		this(opts, false);
	}

	public Weights(UparseOptions opts, boolean isAveraged) {
		this.isAveraged = isAveraged; 
		this.index = new Numberer<T>();
		this.gram = new Grammar(opts);
		this.opts = opts;
		data = new Object2ObjectOpenHashMap<>();
	}

	public float[] weight(Collection<String> features) {
		float[] scores = new float[index.size()];
		for (String feature : features) {
			if (!data.containsKey(feature)) {
				continue;
			}
			if (isAveraged || data.get(feature).used > opts.minupdate) {
				data.get(feature).sumScoresForAllTransitions(scores);
			}
		}
		return scores;
	}
	

	public void applyUpdate(List<String> goldFeatures, int goldTransition, List<String> predFeatures,
			int predTransition, float delta) {
		++i;
		increment(goldFeatures, goldTransition, delta);
		increment(predFeatures, predTransition, -delta);
	}

	private void increment(List<String> features, int transition, float delta) {
		for (String feat : features) {
			if (!data.containsKey(feat)) {
				data.put(feat, new FeatureWeights());
			}
			data.get(feat).increment(transition, delta, i);
		}
	}
	
	public Weights<T> average() {
		System.err.print("averaging (this may take a while)... ");
		Weights<T> result = new Weights<T>(opts, true);
		result.data = new Object2ObjectOpenHashMap<>();
		result.index = index;
		result.gram = gram;
		int cnt = 0;
		for (String feat : data.keySet()) {
			FeatureWeights dw = data.get(feat);
			if (dw.used > opts.minupdate) {
				cnt++;
				FeatureWeights fw = new FeatureWeights();
				for (int trans : index.indices()) {
					float averaged = dw.getAveraged(trans, i);
					if (!Float.isNaN(averaged)) {
						fw.increment(trans, averaged, 0);
					}
				}
				result.data.put(feat, fw);
			}
		}
		System.err.println("done, averaged " + cnt + " features.");
		return result;
	}

	public void save(File tempFile) throws IOException {
		System.err.print("serializing weights... ");
		FileOutputStream fs = new FileOutputStream(tempFile);
		ObjectOutputStream out = new ObjectOutputStream(fs);
		out.writeObject(this);
		out.close();
		fs.close();
		System.err.println("done.");
	}

	@SuppressWarnings("unchecked")
	public static <T> Weights<T> load(File file) throws IOException {
		System.err.print("deserializing weights... ");
		Weights<T> weights = null;
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		try {
			weights = (Weights<T>) in.readObject();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		in.close();
		System.err.println("done.");
		return weights;
	}

	private static final long serialVersionUID = -5378171789739930494L;

}
