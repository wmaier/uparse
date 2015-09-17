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
package de.hhu.phil.uparse.misc;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.io.Serializable;
import java.util.Set;

/**
 * maps T stuff to numbers 
 * @author wmaier
 *
 * @param <T>
 */
public class Numberer<T> implements Serializable {
	
	public final static int INITIAL_CAPACITY = 1000;

	transient private int cnt;
	
	private Int2ObjectMap<T> index2object;
	
	// we don't save this 
	transient private Object2IntMap<T> object2index;
	
	public Numberer() {
		this(INITIAL_CAPACITY);
	}
	
	public Numberer(int capacity) {
		cnt = 0;
		index2object = new Int2ObjectArrayMap<T>(capacity);
		object2index = new Object2IntArrayMap<>(capacity);
	}
	
	public int getNumber(T t) {
		if (!object2index.containsKey(t)) {
			object2index.put(t, cnt);
			index2object.put(cnt, t);
			cnt++;
		}
		return object2index.get(t);
	}
	
	public T getObject(int i) {
		return index2object.get(i);
	}
	
	public int size() {
		return index2object.size();
	}

	public Set<Integer> indices() {
		return index2object.keySet();
	}
	
	private static final long serialVersionUID = 3379459790339878976L;

}
