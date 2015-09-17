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
package de.hhu.phil.uparse.treebank;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Iterator;
import java.util.List;


public class Treebank<T> implements Iterator<T>, Iterable<T> {

	private ObjectArrayList<T> trees;
	
	private int iteratorPos = 0;
	
	public Treebank() {
		trees = new ObjectArrayList<>();
	}
	
	@Override
	public Iterator<T> iterator() {
		this.iteratorPos = 0;
		return this;
	}

	@Override
	public boolean hasNext() {
		return iteratorPos < trees.size(); 
	}

	@Override
	public T next() {
		return trees.get(iteratorPos++);
	}

	public void add(T tree) {
		trees.add(tree);
	}

	public T get(int i) {
		return trees.get(i);
	}

	public int size() {
		return trees.size();
	}

	public void apply(List<TreebankProcessor<T>> toDo) throws TreebankException {
		for (TreebankProcessor<T> processor : toDo) {
			processor.process(this);
		}
	}

}
