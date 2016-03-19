package selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.tree;

import java.util.TreeMap;

public class PhraseSuffix_Node {
	
 final int oo = Integer.MAX_VALUE/2;
 boolean Significance=false;
 int start, end = oo, link;
 double frequencyCount;    
 
 public TreeMap<String, Integer> next = new TreeMap<String, Integer>();

 public PhraseSuffix_Node(int start, int end) {
     this.start = start;
     this.end = end;
 }

 public int edgeLength() {
     return Math.min(end, PhraseSuffix_Tree.position + 1) - start;
 }
 
 public void setFrequency(double freqCt) {
	 this.frequencyCount = freqCt;
 }
 public double getFrequency() {
	 return this.frequencyCount;
 }

}