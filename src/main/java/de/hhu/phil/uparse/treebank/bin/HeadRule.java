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

import java.util.Arrays;

public class HeadRule {

	public final static String LEFT_TO_RIGHT = "left-to-right";
	
	public final static String RIGHT_TO_LEFT = "right-to-left";

	private String direction;

	private String[] labels;
	
	public HeadRule(String direction, String[] labels) {
		this.direction = direction;
		this.labels = labels;
	}

	public String getDirection() {
		return direction;
	}

	public String[] getLabels() {
		return labels;
	}

	public void setLabels(String[] labels) {
		this.labels = labels;
	}

	public int numberOfLabels() {
		return labels.length;
	}

	public String toString() {
		String ret = "[";
		ret += direction + ", ";
		ret += Arrays.toString(labels);
		ret += "]";
		return ret;
	}

}
