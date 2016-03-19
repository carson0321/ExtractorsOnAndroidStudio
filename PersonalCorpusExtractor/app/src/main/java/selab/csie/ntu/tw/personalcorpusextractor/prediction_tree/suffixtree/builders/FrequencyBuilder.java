package selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.builders;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

public class FrequencyBuilder {
	
	private Map<String, Float> mixed;
	private float total = 1000; //From 500 word corpus and collocations of 500
	
	public FrequencyBuilder(){
		mixed = new TreeMap<String, Float>();
	}

	public void addToMix(String word){
		if(mixed.containsKey(word)) {
			mixed.put(word, mixed.get(word)+1/total);
		}
		else {
			mixed.put(word, 1/total);
		}
	}
	
	public void printMix(PrintWriter out){
		for(Map.Entry<String, Float> entry : mixed.entrySet()) {
			String key = entry.getKey();
			Float value = entry.getValue();
			//System.out.println(key + " = " + value);
			//System.out.println(key);
			out.println(key + "=" + value);
			//out.println(key);
		}
	}
	
	public Map<String, Float> getMap(){
		return mixed;
	}
}