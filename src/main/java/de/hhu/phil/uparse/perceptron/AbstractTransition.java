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
package de.hhu.phil.uparse.perceptron;

import java.io.Serializable;

/**
 * Basic property of a perceptron decision is that it has a score.
 * 
 * @author wmaier
 *
 */
public class AbstractTransition implements Comparable<AbstractTransition>, Serializable {

	private double score;

	@Override
	final public int compareTo(AbstractTransition o) {
		return Double.compare(score, o.getScore());
	}

	final public double getScore() {
		return score;
	}

	private static final long serialVersionUID = -3757339708474248166L;
	
}
