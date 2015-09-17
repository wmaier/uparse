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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hhu.phil.uparse.ui.UparseOptions;

public class NodeLabel {

	public String label;
	
	private Map<String,String> fields;
	
	private Map<String,List<String>> listFields;

	public NodeLabel() {
		this("--");
	}

	public NodeLabel(String label) {
		this.label = label;
		this.setFields(new HashMap<>());
		this.listFields = new HashMap<>();
	}
	
	public String getField(String field) {
		if (getFields().containsKey(field)) {
			return getFields().get(field);
		}
		return "";
	}
	
	public void setField(String field, String value) {
		getFields().put(field, value);
	}
	
	public List<String> getListField(String field) {
		if (listFields.containsKey(field)) {
			return listFields.get(field);
		}
		return new ArrayList<>();
	}
	
	public List<String> getSortedListField(String field) {
		List<String> result = getListField(field);
		Collections.sort(result);
		return result;
	}
	
	public void addToListField(String field, String value) {
		if (!(listFields.containsKey(field))) {
			listFields.put(field, new ArrayList<String>());
		}
		listFields.get(field).add(value);
	}
	
	public boolean removeFromListField(String field, String value) {
		if (listFields.containsKey(field)) {
			if (listFields.get(field).contains(value)) {
				listFields.get(field).remove(value);
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return label;
	}

	/**
	 * Build a label from label string. Depending on options, try to split of edge labels 
	 * @param label The label string
	 * @param opts The UparseOptions instance
	 * @return The finished label
	 * @throws TreebankException
	 */
	public static NodeLabel fromString(String label, UparseOptions opts) throws TreebankException {
		NodeLabel result = new NodeLabel();
		if ("ptb".equals(opts.labelFormat)) {
			int index = label.lastIndexOf("-");
			String coindex = "";
			if (index > -1 && index < label.length() - 1) {
				boolean allDigit = true;
				for (int i = index + 1; i < label.length(); ++i) {
					allDigit &= Character.isDigit(label.charAt(i));
				}
				if (allDigit) {
					coindex = label.substring(index + 1);
					label = label.substring(0, index);
				}
			}
			String gapindex = "";
			index = label.lastIndexOf("=");
			if (index > -1) {
				boolean allDigit = true;
				for (int i = index + 1; i < label.length(); ++i) {
					allDigit &= Character.isDigit(label.charAt(i));
				}
				if (allDigit) {
					gapindex = label.substring(index + 1);
					label = label.substring(0, index);
				}
			}
			String edge = "--";
			if (!opts.useFunctionTags) {
				index = label.indexOf("-");
				if (index > 0) {
					edge = label.substring(index + 1);
					label = label.substring(0, index);
				}
			}
			result.label = label;
			result.setField("edge", edge);
			result.setField("gapindex", gapindex);
			result.setField("coindex", coindex);
		} else if ("generic".equals(opts.labelFormat)) {
			// generic
			if (!opts.useFunctionTags) {
				if (label.endsWith("---")) {
					label = label.substring(0, label.length() - 3);
				}
				String edge = "--";
				int index = label.lastIndexOf("-");
				if (index > -1) {
					edge = label.substring(index + 1);
					label = label.substring(0, index);
				}
				result.setField("edge", edge);
			}
			result.label = label;
		} else if ("none".equals(opts.labelFormat)) {
			result.label = label;
		} else {
			throw new TreebankException("unknown label format");
		}
		return result;
	}

	public Map<String,String> getFields() {
		return fields;
	}

	public void setFields(Map<String,String> fields) {
		this.fields = fields;
	}

	public void setListField(String key, List<String> value) {
		listFields.put(key, value);
	}

}
