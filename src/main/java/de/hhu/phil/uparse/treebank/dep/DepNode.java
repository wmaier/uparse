/*
 *  This file is part of uparse.
 *  
 *  Copyright 2015 Wolfgang Maier 
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
package de.hhu.phil.uparse.treebank.dep;

import java.util.HashMap;
import java.util.Map;

import de.hhu.phil.uparse.treebank.Constants;


public class DepNode {
	
	private Map<String,String> fields;
	
	private int id; 
	
	public DepNode() {
		fields = new HashMap<>();
	}

	public DepNode(DepNode depNode) {
		id = depNode.id;
		fields = new HashMap<>(depNode.fields);
	}

	public String getField(String field) {
		return fields.containsKey(field) ? fields.get(field) : Constants.DEFAULT_FIELD;
	}

	public void setField(String field, String val) {
		this.fields.put(field, val);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
