package selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.builders;

import java.util.ArrayList;
import java.util.List;

public class SuffixTokenBuilder {
	
	public String readLine (String sentence){
		List<String> childNodes = new ArrayList<String>(); 
		StringBuilder a = new StringBuilder();
	    
	//	System.out.println("sentence: " + sentence.toString());
		String[] word = sentence.split(" ");
    //	System.out.println("word: " + word.toString());
		System.out.println("wordLength: " + word.length);
		for (int i = 0; i < word.length; ++i){
			childNodes = new ArrayList<String>();
			for(int j = i; j < word.length; ++j)
			{
				childNodes.add(word[j]+"");
				//System.out.println("childNodes[J]" + word[j]);
			}
			System.out.println(childNodes);
			a.append(childNodes);
			
			//childNodes2 = childNodes2 + childNodes;
		}
		return a.toString();
	
	}
	//Output:   1) please call me asap
			 // 2) call me asap
			 // 3) me asap
			 // 4) asap
	
	
	
}
