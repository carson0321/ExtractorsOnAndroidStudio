package selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.builders;

import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
public class SignificanceBuilder {
	

	
	private boolean coOccurrence(float ab, float a, float b){
		return ab > a * b;
	}
	private boolean isUnique(float ab, float abc){
		int y = 3;
		return ab >= y * abc;
	}
	private boolean comparability(float ab, float a){
		float z = 2;
		return ab >= (a/z);
	}
	public HashSet<String> markSignificant(final Map<String,Float>  table){
		 HashSet<String> sig = new HashSet<String>();
		 String tmpS = null;
		 float tmpF = 0;
		 boolean isSignificant = true;
		 for(Map.Entry<String, Float> entry : table.entrySet()) {
				if(entry.getKey().split(" ").length == 1){
			 		if(tmpS != null && isSignificant)
						sig.add(tmpS.split(" ")[1]);
				}
				else if(entry.getKey().split(" ").length == 2)
				{
					isSignificant = true;
					tmpS = entry.getKey();
					//System.out.println(tmpS);
					tmpF = entry.getValue();
					String[] s = tmpS.split(" ");
					String s1 = s[0];
					String s2 = s[1];
					if(!coOccurrence(tmpF, table.get(s1), table.get(s2)) || !comparability(tmpF, table.get(s1)))
						isSignificant = false;
				}
				else if(isSignificant != false){
					if(!isUnique(tmpF, entry.getValue()))
						isSignificant = false;
				}
			}
		 return sig;
	}
	public void threshold(Map<String, Float> table, float thr){
		 for(Iterator<Map.Entry<String, Float>> it = table.entrySet().iterator(); it.hasNext(); ) {
		      Map.Entry<String, Float> entry = it.next();
	    	  if( entry.getValue() <= thr) {
		    	 it.remove();
		      }
		    }
		
	}
}
