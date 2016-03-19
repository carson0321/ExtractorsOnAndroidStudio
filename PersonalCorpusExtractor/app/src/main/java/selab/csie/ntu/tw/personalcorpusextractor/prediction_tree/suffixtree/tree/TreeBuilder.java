package selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.tree;

import java.io.File;
import java.io.PrintWriter;

public interface TreeBuilder {
	
	public void acceptVisitors(PrintWriter out);
	public PhraseSuffix_Tree buildTree(int nodeSize);
	public void collectFrequenciesFromFile(File inCorpus);
	public void addToTree(PhraseSuffix_Tree st) throws Exception;
	public void updateTree(PhraseSuffix_Tree st) throws Exception;
	public void calculateCollocations(PhraseSuffix_Tree st, File inCorpus, PrintWriter out) throws Exception;
	
}