package selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.visitor;

import java.io.File;

public interface Visitor {
	
	abstract void visitTree(File file) throws Exception;

}
