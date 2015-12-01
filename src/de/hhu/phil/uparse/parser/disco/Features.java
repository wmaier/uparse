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
package de.hhu.phil.uparse.parser.disco;

public class Features {
	
	/*
	 * Baseline is from Zhang and Clark (2011), extended 
	 * and separator are from Zhu et al. (2013).
	 * 
	 * Disco is from Maier (2015).
	 */
	
	/*
	 * baseline *****************************
	 */

	// unigrams
	public static String[] uni = { "s0_t_c", "s0_w_c", "s1_t_c", "s1_w_c",
			"s2_t_c", "s2_w_c", "s3_t_c", "s3_w_c", "q0_w_t", "q1_w_t",
			"q2_w_t", "q3_w_t", "s0l_w_c", "s0r_w_c", "s0u_w_c", "s1l_w_c",
			"s1r_w_c", "s1u_w_c" };

	// bigrams
	public static String[] bi = { "s0_w#s1_w", "s0_w#s1_c", "s0_c#s1_w",
			"s0_c#s1_c", "s0_w#q0_w", "s0_w#q0_t", "s0_c#q0_w", "s0_c#q0_t",
			"q0_w#q1_w", "q0_w#q1_t", "q0_t#q1_w", "q0_t#q1_t", "s1_w#q0_w",
			"s1_w#q0_t", "s1_c#q0_w", "s1_c#q0_t", };

	// _trigrams
	public static String[] tri = { "s0_c#s1_c#s2_c", "s0_w#s1_c#s2_c",
			"s0_c#s1_w#s2_c", "s0_c#s1_c#s2_w", "s0_c#s1_c#q0_t",
			"s0_w#s1_c#q0_t", "s0_c#s1_w#q0_t", "s0_c#s1_c#q0_w" };
	
	/*
	 * extended *******************************
	 */

	public static String[] extended = { "s0ll_w_c", "s0lr_w_c", "s0lu_w_c",
			"s0rl_w_c", "s0rr_w_c", "s0ru_w_c", "s0ul_w_c", "s0ur_w_c",
			"s0uu_w_c", "s1ll_w_c", "s1lr_w_c", "s1lu_w_c", "s1rl_w_c",
			"s1rr_w_c", "s1ru_w_c" };
	
	/*
	 * disco ************************************
	 */

	public static String[] disco = { 
		"s0_x_w_c", "s1_x_w_c", "s2_x_w_c", "s3_x_w_c", 
		"s0_x_t_c", "s1_x_t_c", "s2_x_t_c", "s3_x_t_c",
		"s0_x_y", "s1_x_y", "s2_x_y", "s3_x_y"
	};

	public static String[] discoBi = { 
		"s0_x#s1_c", "s0_x#s1_w", "s0_x#s1_x", "s0_w#s1_x", "s0_c#s1_x",
		"s0_x#s2_c", "s0_x#s2_w", "s0_x#s2_x", "s0_w#s2_x", "s0_c#s2_x",
		"s0_y#s1_y", "s0_y#s2_y", "s0_x#q0_t", "s0_x#q0_w",
	};
	
	/*
	 * separator *********************************
	 */

	public static String[] separator = { "s0_w_p", "s0_w_c_p", "s0_w_q",
			"s0_w_c_q", "s1_w_p", "s1_w_c_p", "s1_w_q", "s1_w_c_q" };

	public static String[] separatorBi = { "s0_c#s1_c_p", "s0_c#s1_c_q" };
	
	
}
