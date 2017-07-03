package de.hhu.phil.uparse.parser.disco;

import de.hhu.phil.uparse.treebank.Grammar;

/**
 * Gap transition from Coavoux & Crabbe (2017)
 * @author wmaier
 *
 */
public class CoavouxGapTransition extends DiscoTransition {

	@Override
	public State extend(State state, double d) {
		return null;
	}

	@Override
	public boolean isLegal(State state, Grammar g) {
		return false;
	}

}
