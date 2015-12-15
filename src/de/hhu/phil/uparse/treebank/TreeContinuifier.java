package de.hhu.phil.uparse.treebank;

import java.util.HashMap;
import java.util.List;

public class TreeContinuifier implements TreeProcessor<Tree> {

	// left, right, label, dist, random
	public String mode;
	
	public HashMap<String,String> labels;
	
	public TreeContinuifier(String mode, HashMap<String,String> labels) {
		this.mode = mode;
		this.labels = labels;
	}
	
	public TreeContinuifier(String mode) {
		this(mode, null);
	}
	
	@Override
	public void process(Tree tree) throws TreebankException {
		if ("left".equals(mode)) {
			for (Tree node : tree.preorder()) {
				List<Tree> children = node.children();
				if (children.size() == 0) {
					// do nothing
				} else if (children.size() == 1) {
					// do nothing
				} else if (children.size() == 2) {
					Tree left = children.get(0);
					Tree right = children.get(1);
					List<Integer> yieldLeft = left.yield();
					List<Integer> yieldRight = right.yield();
				} else {
					throw new TreebankException("tree must be binarized");
				}
			}
		}
	}

}
