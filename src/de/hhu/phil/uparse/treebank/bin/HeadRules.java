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
package de.hhu.phil.uparse.treebank.bin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HeadRules {

	private HashMap<String, List<HeadRule>> rules;

	public HeadRules() {
		rules = new HashMap<String, List<HeadRule>>();
	}

	public String toString() {
		String ret = "";
		for (String label : rules.keySet()) {
			ret += label + "\n";
			for (HeadRule hr : rules.get(label)) {
				ret += "     " + hr + "\n";
			}
		}
		return ret;
	}

	public void addRule(String label, HeadRule hr) {
		if (!rules.containsKey(label))
			rules.put(label, new ArrayList<HeadRule>());
		rules.get(label).add(hr);
	}

	public List<HeadRule> getRules(String lhs) {
		if (rules.containsKey(lhs))
			return rules.get(lhs);
		return null;
	}

}
