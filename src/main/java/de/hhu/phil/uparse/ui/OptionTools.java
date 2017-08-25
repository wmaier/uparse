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
package de.hhu.phil.uparse.ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptionTools {
	
	public static String[] readCommandLineParamsFromFile(String fileName)
			throws OptionException {
		List<String> params = new ArrayList<>();
		String line = "";
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(fileName);
			br = new BufferedReader(fr);
			line = br.readLine();
			while (line != null) {
				int commentPosition = line.indexOf("#");
				if (commentPosition > -1) {
					line = line.substring(0, commentPosition);
				}
				for (String splitString : line.trim().split("\\s+")) {
					if (splitString.length() > 0) {
						params.add(splitString);
					}
				}
				line = br.readLine();
			}
			br.close();
			fr.close();
		} catch (IOException e) {
			throw new OptionException(e);
		}
		String[] result = new String[params.size()];
		for (int i = 0; i < params.size(); ++i) {
			result[i] = params.get(i);
		}
		return result;
	}

	public static <T extends Options> T parseCommandLineParams(String[] args, Class<T> clazz)
			throws OptionException {
		T result;
		try {
			result = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e1) {
			e1.printStackTrace();
			throw new OptionException(e1.getMessage());
		}
		Map<String, Field> nameToField = new HashMap<>();
		Field[] fields = result.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; ++i) {
			Field field = fields[i];
			if (field.isAnnotationPresent(Option.class)) {
				Option option = field.getAnnotation(Option.class);
				String name = option.name();
				if (nameToField.containsKey(name)) {
					throw new OptionException(name + " declared more than once");
				}
				nameToField.put(name, field);
			}
		}
		for (int i = 0; i < args.length; ++i) {
			String arg = args[i];
			if (nameToField.containsKey(arg)) {
				Field field = nameToField.get(arg);
				try {
					Type type = field.getGenericType();
					if (type instanceof ParameterizedType) {
						ParameterizedType ptype = (ParameterizedType) type;
						Type rawType = ptype.getRawType();
						Type typeArg = ptype.getActualTypeArguments()[0];
						if (rawType == ArrayList.class) {
							if (typeArg == String.class) {
								if (i == args.length - 1) {
									throw new OptionException("No argument for "
											+ arg);
								}
								ArrayList<String> arguments = new ArrayList<>();
								for (int j = i + 1; j < args.length
										&& !args[j].startsWith("-"); ++j) {
									arguments.add(args[j]);
								}
								field.set(result, arguments);
							}
						}
					} else {
						if (type == Boolean.TYPE) {
							if (i < args.length - 1
									&& !args[i + 1].startsWith("-")) {
								throw new OptionException(
										"Argument for switch given: "
												+ field.getName());
							}
							field.set(result, true);
						} else if (type == String.class) {
							if (i == args.length - 1
									|| args[i + 1].startsWith("-")) {
								throw new OptionException("No argument for "
										+ arg);
							}
							String argument = args[++i];
							field.set(result, argument);
						} else if (type == Integer.TYPE) {
							if (i == args.length - 1
									|| args[i + 1].startsWith("-")) {
								throw new OptionException("No argument for "
										+ arg);
							}
							String sArgument = args[++i];
							int argument = Integer.valueOf(sArgument);
							field.set(result, argument);
						} else {
							throw new OptionException("Unknown argument type "
									+ field.getGenericType());
						}
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
					throw new OptionException(e);
				}
			}
		}
		
		// set presets
		Map<String,Map<String,Enum<?>>> presets = result.getPresets();
		for (Field field : result.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Option.class)) {
				Option option = field.getAnnotation(Option.class);
				if (option.isPreset()) {
					if (presets.containsKey(option.name())) {
						String presetName;
						try {
							presetName = (String) field.get(result);
							Enum<?> preset = presets.get(option.name()).get(presetName);
							for (Field presetField : preset.getClass().getDeclaredFields()) {
								if (!presetField.isEnumConstant() && !presetField.isSynthetic()) {
									Field aField = result.getClass().getDeclaredField(presetField.getName());
									aField.set(result, presetField.get(preset));
								}
							}
						} catch (IllegalArgumentException | IllegalAccessException 
								| NoSuchFieldException | SecurityException e) {
							e.printStackTrace();
							throw new OptionException(e);
						}
					}
				}
			}
		}

		return result;
	}


	public static <T> void generateParameterTemplate(String dest,
			String commentedUsage, Class<T> clazz) throws OptionException {
		String usage = commentedUsage;
		T defaultOpts;
		try {
			defaultOpts = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e1) {
			e1.printStackTrace();
			throw new OptionException(e1.getMessage());
		}
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Option.class)) {
				Option option = field.getAnnotation(Option.class);
				String paramName = option.name().substring(1);
				try {
					usage += "# " + option.help() + "\n";
					if (option.mandatory()) {
						usage += "# (mandatory)\n";
					}
					String defaultRepr = field.get(defaultOpts).toString();
					if (defaultRepr.equals("false")) {
						defaultRepr = "";
						usage += "# -" + paramName + "\n";
					} else if (defaultRepr.equals("true")) {
						defaultRepr = "";
						usage += "-" + paramName + "\n";
					} else if (defaultRepr.equals("[]")) { 
						defaultRepr = "";
						usage += "# -" + paramName + " " + defaultRepr + "\n";
					} else {
						usage += "-" + paramName + " " + defaultRepr + "\n";
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
					throw new OptionException(e);
				}
			}
		}
		try {
			FileWriter fw = null;
			fw = new FileWriter(dest);
			fw.write(usage);
			fw.close();
		} catch (IOException e1) {
			throw new OptionException(e1);
		}
		System.err.println("parameter file 'parameters' generated");
	}

	public static <T> void usage(String uncommentedUsage, Class<T> clazz) throws OptionException {
		String usage = uncommentedUsage;
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Option.class)) {
				Option option = field.getAnnotation(Option.class);
				String paramName = option.name().toUpperCase().substring(1);
				if (paramName.length() > 5) {
					paramName = paramName.substring(0, 5);
				}
				String optionString = option.name() + " [" + paramName + "]";
				int numSpaces = 30 - optionString.length();
				for (int i = 0; i < numSpaces; ++i) {
					optionString += " ";
				}
				optionString += option.help();
				if (option.mandatory()) {
					optionString += " (mandatory)";
				}
				usage += optionString + "\n";
			}
		}
		System.err.println(usage);
	}

}
