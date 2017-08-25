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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class HeadRuleReader extends BufferedReader {

	public HeadRuleReader(String string) throws UnsupportedEncodingException,
			FileNotFoundException {
		this(new File(string));
	}

	public HeadRuleReader(File f) throws UnsupportedEncodingException,
			FileNotFoundException {
		this(new FileInputStream(f));
	}

	public HeadRuleReader(InputStream inputStream) throws UnsupportedEncodingException {
		super(new InputStreamReader(inputStream));
	}

	public HeadRules getHeadRules() throws IOException {
		HeadRules headRules = new HeadRules();
		String line = "";
		while ((line = super.readLine()) != null) {
			line = line.trim();
			if (line.length() > 7 && line.charAt(0) != '%') {
				String[] split = line.split("\\s+");
				if (split.length < 2) {
					continue;
				}
				if (!(split[1].equals(HeadRule.LEFT_TO_RIGHT) || split[1]
						.equals(HeadRule.RIGHT_TO_LEFT))) {
					continue;
				}
				String direction = split[1];
				String[] labels = Arrays.copyOfRange(split, 2, split.length);
				for (int i = 0; i < labels.length; ++i) {
					labels[i] = labels[i].toUpperCase();
				}
				HeadRule rule = new HeadRule(direction, labels);
				headRules.addRule(split[0].toUpperCase(), rule);
			}
		}
		return headRules;
	}

}
