package selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.tree;

import android.util.Log;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import  selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.builders.*;
import tw.edu.ntu.selab.query_refinement_system.PersonalOntologyBuilder;
import tw.edu.ntu.selab.query_refinement_system.exceptions.OntologyUpdateException;

import com.aliasi.lm.TokenizedLM;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ScoredObject;
import com.google.common.collect.*;

public class PhraseSuffix_Tree {
	
	   final int oo = Integer.MAX_VALUE/2;
       PhraseSuffix_Node [] nodes;
       String[] text = new String[5000];
       static int root, position = -1,
               currentNode,
               needSuffixLink,
               remainder;
       int foundNodesCt = 0;
       int[] foundNodes = new int[1000];
       int nodesToIgnoreCt = 0;
       int[] nodesToIgnore = new int[1000];
       int[] nodesToIgnoreParent = new int[1000];
       String[] nodesToIgnoreKey = new String[1000];
       String[] tempPrediction = new String[1000];
       int active_node, active_length, active_edge;
       ArrayList<Integer> hasLink= new ArrayList<Integer>(); 
       public int suggestionNo; 
       String message = "";
       int suggestionKey = 0;
       
       public TreeMap<String, Integer> collocationTree = new TreeMap<String, Integer>();
       public TreeMap<Double, String> suggestionsMap = new TreeMap<Double, String>();
       public Multimap<Double,String> suggestionsMultimap = TreeMultimap.create(Ordering.natural().reverse(),Ordering.natural());
       
       public PhraseSuffix_Tree(int length) {
           nodes = new PhraseSuffix_Node[2* length + 2];
           text = new String[length];
           root = active_node = newNode(-1, -1);
       }

       private void addSuffixLink(int node) {
           if (needSuffixLink > 0){
               nodes[needSuffixLink].link = node;
               hasLink.add(node);
           }
           needSuffixLink = node;
           
       }

       String active_edge() {
           return text[active_edge];
       }

       boolean walkDown(int next) {
           if (active_length >= nodes[next].edgeLength()) {
               active_edge += nodes[next].edgeLength();
               active_length -= nodes[next].edgeLength();
               active_node = next;
               return true;
           }
           return false;
       }

       int newNode(int start, int end) {
           nodes[++currentNode] = new PhraseSuffix_Node(start, end);
           return currentNode;
       }

       public void addWord(String c) throws Exception {
           Log.v("addWord line: ", c);
           text[++position] = c;
           needSuffixLink = -1;
           remainder++;
           while(remainder > 0) {
           	//System.out.println("active pt: node "+active_node+" length: "+active_length+" edge: "+active_edge);
               if (active_length == 0) active_edge = position;
               if (!nodes[active_node].next.containsKey(active_edge()) ){
                   int leaf = newNode(position, oo);
                   nodes[active_node].next.put(active_edge(), leaf); 
                   addSuffixLink(active_node); //rule 2
               } else {
                   int next = nodes[active_node].next.get(active_edge());
                   if (walkDown(next)) continue;   //observation 2
                   if (text[nodes[next].start + active_length].equals(c) ) { //observation 1
                       active_length++;
                       addSuffixLink(active_node); // observation 3
                      	break;
                   }
                   int split = newNode(nodes[next].start, nodes[next].start + active_length);
                   nodes[active_node].next.put(active_edge(), split);
                   int leaf = newNode(position, oo);
                   nodes[split].next.put(c, leaf);
                   nodes[next].start += active_length;
                   nodes[split].next.put(text[nodes[next].start], next);
                   addSuffixLink(split); //rule 2
               }
               remainder--;
               
               if (active_node == root && active_length > 0) {  //rule 1
                   active_length--;
                   active_edge = position - remainder + 1;
               } else
                   active_node = nodes[active_node].link > 0 ? nodes[active_node].link : root; //rule 3
           }
       }
       public void sep(){
       	 remainder = 0;
       	 active_node = root;
       	 active_length = 0;
       	 active_edge = 0;
       	 for(int i = 0; i < hasLink.size(); i++)
       		 nodes[hasLink.get(i)].link=0;
       	 for(int i = 1; i < nodes.length; ++i)
       	 {
       		 if(nodes[i] == null)
       			 break;
       		 nodes[i].end=Math.min(position + 1, nodes[i].end);
       	 }
   
       }
       /**************************************************************************************************
        * Display Tree Actions
       		printing the Suffix Tree in a format understandable by graphviz. The output is written into
       		st.dot file. In order to see the suffix tree as a PNG image, run the following command:
       		dot -Tpng -O st.dot
        **************************************************************************************************/
       void printEdges(int x, PrintWriter out, PersonalOntologyBuilder builder) {
           Log.v("EdgeString: ","Inside");
           for (int child : nodes[x].next.values()) {
        	   if(nodes[child]!=null){
        		  // out.println("\tnode"+x+" -> node"+child+" [label=\""+edgeString(child)+"\",weight=3]");
                   out.println(edgeString(child));
                   Log.v("EdgeString: ", "\tnode" + x + " -> node" + child + " [label=\"" + edgeString(child) + "\",weight=3]");
                   try {
                       builder.updateOntology(Collections.singletonList(edgeString(child)), "Facebook", null);
                   } catch (OntologyUpdateException e) {
                       e.printStackTrace();
                   }
                   printEdges(child, out, builder);
        	   }
           }
       }

//    void printEdges2(int x) {
//        Log.v("EdgeString: ","Inside");
//        for (int child : nodes[x].next.values()) {
//            if(nodes[child]!=null){
//               // out.println("\tnode"+x+" -> node"+child+" [label=\""+edgeString(child)+"\",weight=3]");
//                Log.v("EdgeString: ",edgeString(child));
//                printEdges2(child);
//            }
//        }
//    }

	   String edgeString(int node) {
	      String[] s= Arrays.copyOfRange(text, nodes[node].start, Math.min(position + 1, nodes[node].end));
	      String a = "";
	      int count = 0;
	      for(String t:s){
	   	   ++count;
	    	   a+=t;
	   	   if(count != s.length) a+=" ";
	      }
	      return a;
	   }
       public void printAllPhrases(int nodeNumber, String str){
    	   if (nodes[nodeNumber].next.isEmpty()){
    		   System.out.println(str);
    		   return;
    	   }
    	   else
    		   for(Map.Entry<String, Integer> entry : nodes[nodeNumber].next.entrySet()){
    			   String s="";
    			   for(int i = nodes[entry.getValue()].start; i < nodes[entry.getValue()].end; i++)
    			   {
    				   s+=text[i];
//    				   if(i != nodes[entry.getValue()].end - 1)
    					   s+=" ";
    			   }
    			   printAllPhrases(entry.getValue(), str+s);
    		   }
    	   
       }
     
       /**************************************************************************************************
        Calculating Collocations and its Frequency counts.
        **************************************************************************************************/
       int nodeCount=1;
       public static String [] collocationStrings = new String [500];
       public void caculateCollocationFrequencies(File inCorpus, PrintWriter out) throws Exception{	
    	   TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
    	   TokenizedLM backgroundModel = CollocationsBuilder.buildModel(inCorpus, tokenizerFactory, CollocationsBuilder.NGRAM);
    	   backgroundModel.sequenceCounter().prune(3);
    	   SortedSet<ScoredObject<String[]>> coll = backgroundModel.collocationSet(CollocationsBuilder.NGRAM_REPORTING_LENGTH, CollocationsBuilder.MIN_COUNT,CollocationsBuilder.MAX_COUNT);
           CollocationsBuilder.report(coll);
           
           
           for(int i=0; i < CollocationsBuilder.collocationCount; i++){
        	   nodeCount++;
        	   System.out.print("node1 -> " + "node" + nodeCount + " [label=\""+collocationStrings[i]+"\",weight=3]\n");
        	   
        	   double [] Normalization= new double [500];
        	   double [] Frequency= new double [500];
        	   
        	   System.out.print("Score: "+PhraseSuffix_Tree.collocationStrings[i]+" with :"+CollocationsBuilder.collcationSignificance[i]);
        	   Normalization[i]=CollocationsBuilder.collcationSignificance[i]/CollocationsBuilder.totalScorce;
        	   System.out.print("\tNormalization: "+Normalization[i]);
        	   Frequency[i]=Normalization[i]/2;
        	   System.out.println("\tFrequency: "+Frequency[i]);
        	   String [] word=collocationStrings[i].split(" ");
        	   
        //	   while((out.append(c)) != null){
        		   out.append(word[1]+"="+Frequency[i]+"\n");
            	   out.append(collocationStrings[i]+"="+Frequency[i]+"\n"); 
        	//   }
        	  // out.println(word[1]+"="+Frequency[i]);
        	 //  out.println(collocationStrings[i]+"="+Frequency[i]);
            		
           }

       } 
//       void printLeaves(int x, PrintWriter out) {
//           if (nodes[x].next.size() == 0)
//               out.println("\tnode"+x+" [label=\"\",shape=point]");
//           else {
//               for (int child : nodes[x].next.values())
//                   printLeaves(child, out);
//           }
//       }
//
//       void printInternalNodes(int x, PrintWriter out) {
//           if (x != root && nodes[x].next.size() > 0)
//               out.println("\tnode"+x+" [label=\"\",style=filled,fillcolor=lightgrey,shape=circle,width=.07,height=.07]");
//
//           for (int child : nodes[x].next.values())
//               printInternalNodes(child, out);
//       }
       
       
       /**************************************************************************************************
        * Telescoping Actions
        **************************************************************************************************/
              String [] telescopeStrings = new String[500];
       int count=0;
       int [] parentNode = new int[500];
       int [] childNode = new int[500];
       public void telescope(int x) {
    	   if (x != root && nodes[x].next.size() == 1){
			   parentNode[count]=x;
//    		   System.out.println("node: "+parentNode[count]+" start: "+nodes[x].start+" end: "+nodes[x].end+ " context: " + edgeString(parentNode[count]));
    		   for (int child : nodes[x].next.values()){
    			   telescopeStrings[count]=edgeString(parentNode[count])+" "+edgeString(child);
//    			   System.out.println(telescopeStrings[count]);
//    			   System.out.println("node"+x+" -> node"+child+" [label=\""+edgeString(child)+"\",weight=3]");
    			   childNode[count]=child;
    			   count++;
    		   }
           }
    	   for (int child : nodes[x].next.values()){
        	   telescope(child);
           }
       }
       boolean telescoping=false;
       public void traveralNode (int x) {
    	   for (int child : nodes[x].next.values()){
    		   if(nodes[child]!=null){
    			   for(int i = 0 ; i < count ; i++){
    				   if(child==parentNode[i]){
        				   System.out.println("node"+x+" -> node"+child+" [label=\""+telescopeStrings[i]+"\",weight=3]");
                           Log.v("Telecoped: ", "node"+x+" -> node"+child+" [label=\""+telescopeStrings[i]+"\",weight=3]");
        				   nodes[childNode[i]]=null;
        				   telescoping=true;
        			   }
    			   }
    			   if (!telescoping)  System.out.println("node"+x+" -> node"+child+" [label=\""+edgeString(child)+"\",weight=3]");
    			   telescoping=false;
    			   traveralNode(child);
    		   }
           }
    	   
    	   //update new frequency to parent node 
       }
       public void printTelescopeTree(PrintWriter out,   PersonalOntologyBuilder builder) {
           System.out.println("PrintTelescope");
           Log.v("EdgeString: ", "printTelescope");
    	  // out.println("digraph origin {");
    	  // out.println("\trankdir = LR;");
          // out.println("\tedge [arrowsize=0.4,fontsize=10]");
          // out.println("\tnode1 [label=\"\",style=filled,fillcolor=lightgrey,shape=circle,width=.1,height=.1];");
//           out.println("//------leaves------");
//           printLeaves(root, out);
//           out.println("//------internal nodes------");
//           printInternalNodes(root, out);
           //out.println("//------edges------");
           printEdges(root, out, builder);
         //  printEdges2(root);
//         out.println("//------suffix links------");
//         printSLinks(root);
           //out.println("}");
           telescope(root);
           traveralNode(root);
           System.out.println();
       }

    public void printTelescopeTree2() {
        System.out.println("PrintTelescope");
        Log.v("EdgeString: ", "printTelescope");
        //out.println("digraph origin {");
        //out.println("\trankdir = LR;");
        //out.println("\tedge [arrowsize=0.4,fontsize=10]");
        //out.println("\tnode1 [label=\"\",style=filled,fillcolor=lightgrey,shape=circle,width=.1,height=.1];");
//           out.println("//------leaves------");
//           printLeaves(root, out);
//           out.println("//------internal nodes------");
//           printInternalNodes(root, out);
        //out.println("//------edges------");
     //   printEdges(root, out);
     //   printEdges2(root);
//         out.println("//------suffix links------");
//         printSLinks(root);
        //out.println("}");
        telescope(root);
        traveralNode(root);
        System.out.println();
    }
       
       
       /**************************************************************************************************
        * Significance Actions
        **************************************************************************************************/   
       public void signSignificance(){
           Log.v("signSignificance() ", "Inside");
        	for(int i = 1; i < nodes.length;++i){
        		if(nodes[i]==null) break;
        		if (nodes[i].next.size() == 0)   nodes[i].Significance=true;
        	}
       }
       public void printSignificanceNodes(){
           Log.v(" printSignificanceNodes() ", "Inside");
          	for(int i = 1; i < nodes.length;++i){
          		if(nodes[i]==null) break;
          		if (nodes[i].Significance){
//          			System.out.println("node: "+i);
          		}
          	}
       }
       
//Terence's Code to Label Low Frequency and Remove Subsequent Nodes: *********************************************************************************************************************
       public void printFullTree(PrintWriter out) {
           out.println("digraph {");
           out.println("\trankdir = LR;");
           out.println("\tedge [arrowsize=0.4,fontsize=10]");
           out.println("\tnode1 [label=\"\",style=filled,fillcolor=lightgrey,shape=circle,width=.1,height=.1];");
//           out.println("//------leaves------");
//           printFullLeaves(root, out);
//           out.println("//------internal nodes------");
//           printFullInternalNodes(root, out);
           out.println("//------edges------");
         //  printEdges(root, out);
//           out.println("//------suffix links------");
//           printFullSLinks(root, out);
           out.println("}");
       } 
       
       void printFullLeaves(int x, PrintWriter out) {
           if (nodes[x].next.size() == 0)
               out.println("\tnode"+x+" [label=\"\",shape=point]");
           else {
               for (int child : nodes[x].next.values())
            	   printFullLeaves(child, out);
           }
       }

       void printFullInternalNodes(int x,PrintWriter out) {
           if (x != root && nodes[x].next.size() > 0)
               out.println("\tnode"+x+" [label=\"\",style=filled,fillcolor=lightgrey,shape=circle,width=.07,height=.07]");
           for (int child : nodes[x].next.values())
        	   printFullInternalNodes(child, out);
       }
       
       void printFullSLinks(int x ,PrintWriter out) {
           if (nodes[x].link > 0)
               out.println("\tnode"+x+" -> node"+nodes[x].link+" [label=\"\",weight=1,style=dotted]");
           for (int child : nodes[x].next.values())
        	   printFullSLinks(child, out);
       }
       
       public void labelLowFrequency(String lowfreqPhrase) {
	   		 searchTree_Basic(root, lowfreqPhrase);   
	   		 searchTree_Advanced(root, lowfreqPhrase);   
       }
       
       String firstWord(int node) {
           String[] s= Arrays.copyOfRange(text, nodes[node].start, nodes[node].start+1);
           String a = "";
           int count = 0;
           for(String t:s){
        	   ++count;
         	   a+=t;
        	   if(count != s.length) a+=" ";
           }
           return a;
        }
       
       String secondWord(int node) {
           String[] s= Arrays.copyOfRange(text, nodes[node].start, nodes[node].start+2);
           
           System.out.println("SecondSTART: " + nodes[node].start);
           System.out.println("SecondMIN: " + nodes[node].start+2);
           
           String a = "";
           int count = 0;
           for(String t:s){
        	   ++count;
         	   a+=t;
        	   if(count != s.length) a+=" ";
           }
           return a;
        }
       

       /**************************************************************************************************
        Search Tree
        **************************************************************************************************/
       void searchTree(int x, String searchWord) {
          	for (int child : nodes[x].next.values()) {
          		System.out.println("Displaying nodes No:" + child + "Value: "+ edgeString(child));
          		if (edgeString(child).equals(searchWord)) { 
          			System.out.println("Searching for:"+searchWord+ " Found Node is:" + child);
          		}
          		searchTree(child, searchWord);
          	}
          }
          
       String secondWordOnly(int node) {
           String[] s= Arrays.copyOfRange(text, nodes[node].start+1, Math.min(position + 1, nodes[node].end));
           String secondWord = "";
           int countb = 0;
           for(String t1:s){
        	   ++countb;
        	   if (countb == 2)
         	   {   System.out.println("REAL second word: " + t1 );   }
        	   secondWord = t1;
        	   break;
           }
           return secondWord;
        }
       public void searchTree_Basic(int x, String searchWord) {
	       	for (int child : nodes[x].next.values()) {
	       		if (edgeString(child).equals(searchWord)) { 
	       				 nodesToIgnore[nodesToIgnoreCt] = child;
		 				 nodesToIgnoreKey[nodesToIgnoreCt] = firstWord(child);
		 				 nodesToIgnoreParent[nodesToIgnoreCt] = 1;
		 				 nodesToIgnoreCt++;	
	       	}
	       		searchTree_Basic(child, searchWord);
       	}
       }
       public void searchTree_Advanced(int x, String searchWord) {
        	String[] word; 
       	
		       	for (int child : nodes[x].next.values()) {
		       	    	word = searchWord.split(" ");
		       	 	
			       	 	if (word[0].equals(firstWord(child))) {
			       	 		//System.out.println("We got a MATCH! at Node:"+child + " with children count of: "+nodes[child].next.size());      
			       	 		//continue to check for the first word in the child branch:
			       	 		///	System.out.println("Its children are nodes: "+nodes[child].next.values());           	 
			       	 		
			       	 			for(Map.Entry<String,Integer> entry : nodes[child].next.entrySet() ) {
				       	 			  String key = entry.getKey();
				       	 			  Integer value = entry.getValue();
				       	 			  //System.out.println("With Key:" +key + " => Value:" + value);
			       	 			  
					       	 			if (word[1].equals(firstWord(value))) {
					       	 				 //System.out.println("We got a VALUE MATCH! at Node:"+ value );
					       	 				 nodesToIgnore[nodesToIgnoreCt] = value;
					       	 				 nodesToIgnoreKey[nodesToIgnoreCt] = key;
					       	 				 nodesToIgnoreParent[nodesToIgnoreCt] = child;
					       	 				 nodesToIgnoreCt++;	
					       	   				 //System.out.println("nodesToIgnore1: "+ nodesToIgnore[nodesToIgnoreCt] + " with ParentNode:  " + nodesToIgnoreParent[nodesToIgnoreCt] + "and Key: "+ nodesToIgnoreKey[nodesToIgnoreCt] );
					       	 			}
			       	 			}
			       	 	}
			       		searchTree_Advanced(child, searchWord);
		       	}
       }
       
       /**************************************************************************************************
       Query Tree
       **************************************************************************************************/
       public void queryTree(int x, String searchWord) {
    	   int suggestionKey = 0;
    	   //String prediction = new String;
    	   String prediction = null;
	       	for (int child : nodes[x].next.values()) {
	       		//if (edgeString(child).startsWith(searchWord)) {
	       		if (edgeString(child).equals(searchWord)) { 
	       		  //System.out.println("----------------------------------------------------------------");
	       		  //System.out.println("Searching for: <"+searchWord+ "> Found at Node: <" + child +"> "+"FreqCt: "+ nodes[child].getFrequency());
	       		  //System.out.println("First word: " + firstWord(child));
	             // System.out.println("Second word: " + secondWord(child));
		          //System.out.println("Second word only: " + secondWordOnly(child));
		  	       	for(Map.Entry<String,Integer> entry : nodes[child].next.entrySet() ) {
		 	 			  //String key = entry.getKey();
		 	 			  Integer value = entry.getValue();
		 	 			  //System.out.println("With Key:" +key + " => Value:" + value);
		 	 			suggestionKey++;
		 	 			  tempPrediction[suggestionKey] = edgeString(value);
		 	 			  if (secondWordOnly(child)!="null") {
		 	 				  prediction = secondWordOnly(child) +" "+ edgeString(value);
		 	 			  }
		 	 			  else prediction = edgeString(value);
		 	 			
		 	 			  //	for(Entry<Double, String> suggestionsMapEntry : suggestionsMap.entrySet()) {
		 		 			//  System.out.println(suggestionsMapEntry.getKey() + " => " +suggestionsMapEntry.getValue());
		 		 			//  if (!suggestionsMap.containsValue(prediction)) {
		 		 				suggestionsMap.put(nodes[value].getFrequency(), prediction);
		 		 				suggestionsMultimap.put(nodes[value].getFrequency(), prediction);
		 		 			//  }
		 		 			//}
		 	 			//  System.out.println("Press Key: "+suggestionCt+" to select: "+ prediction +" temp: "+ tempPrediction[suggestionCt]);
		  	       	}
	       		}
	       		//System.out.println("Next phrase prediction in order: ");
	       		queryTree(child, searchWord);
      	}
	       	suggestionNo = suggestionKey; 
	        //suggestionsMap = new TreeMap<Double, String>();
      }
       public void queryPredsuggestionsMap() {
//			System.out.println("queryPredsuggestionsMap");
//	    	   for(Entry<Double, String> suggestionsMapEntry : suggestionsMap.entrySet()) {
//		 			  System.out.println(suggestionsMapEntry.getKey() + " => " +suggestionsMapEntry.getValue());
//		 		}
//	    	suggestionsMap.clear();
	    	// Iterating over entire Mutlimap
	    	  for(Double keys : suggestionsMultimap.keySet()) {
	    		  Collection<String> suggestions = suggestionsMultimap.get(keys) ;
	    		 System.out.println("Suggestion: Rank="+keys + " "+suggestions);
//	    		  String result = String.format("#.00", keys); 
//	    		  for (String suggestions1:suggestions) 
//	    	       { 
//	    	       	result = result+" "+suggestions1; 
//	    	       } 
//	    	       System.out.println(result); 
	    	  }
	    	  suggestionsMultimap.clear();
       }
       
       public void querySuggestionKey() {
    	   
    	   for (int i = 1; i<=suggestionNo; i++) {
    		   System.out.println("Press Key: "+i+" to select: "+ tempPrediction[i]);
    	   }
    	 //  suggestionCt = 0;
       }
       public void queryPredictionTable(int x, int searchIndex) {
    	   String searchWord;
    	   searchWord = tempPrediction[searchIndex];
    	   message = message + " " + searchWord;
    	   System.out.println("Your Message: "+ message);
    	   System.out.println("-----------------------------------------------------------------------");
    	   System.out.println("");
   		   suggestionNo = 0;   
    	   queryTree(1, searchWord);
    	   if (suggestionNo==0) System.out.println("End of Sentence!");
	    }
       
       public void setInitialMessage(String searchWord) {
    	   message = message + " " + searchWord;
	    }
       
       public String getMessage() {
    	   return message;
	    }
       
       
       /**************************************************************************************************
       Low Frequency Pruning
       **************************************************************************************************/
       public void displayLowFrequencyNodes() {
    	   System.out.println(nodesToIgnoreCt+" Nodes To Ignore due to Low Frequency: ");
   			for(int i = 0; i < nodesToIgnoreCt; ++i)
   			{
   				System.out.println("nodesToIgnore2: "+ nodesToIgnore[i] + " with ParentNode:  " + nodesToIgnoreParent[i] + " and Key: "+ nodesToIgnoreKey[i] );
   			}
       }
       
       public void removeLowFrequencyNodes() {
    	   for(int i = 0; i < nodesToIgnoreCt; ++i)
  			{
  		//	  System.out.println("Removing link from parent node=" + nodesToIgnoreParent[i] + "to child node=" + nodesToIgnore[i] + " with Key: " + nodesToIgnoreKey[i]  );
  			  nodes[nodesToIgnoreParent[i]].next.remove(nodesToIgnoreKey[i]);
  			}
       }
       
       /**************************************************************************************************
       	Update Frequency Count
       **************************************************************************************************/
       public void updateFrequency(int x, String searchWord, double freqCt) {
    	   int suggestionCt= 0;
	       	for (int child : nodes[x].next.values()) {
	       	//	System.out.print("Searching for: <"+searchWord+ "> Found at Node: <" + child +"> ");
	       		if (edgeString(child).equals(searchWord)) { 
	       		//  System.out.println("----------------------------------------------------------------");
	       		//  System.out.print("Searching for: <"+searchWord+ "> Found at Node: <" + child +"> ");
	       		  //System.out.println("First word: " + firstWord(child));
	             // System.out.println("Second word: " + secondWord(child));
		         // System.out.println("Second word only: " + secondWordOnly(child));
  	       		  nodes[child].setFrequency(freqCt);
  	       	//	  System.out.println("Updated Node: "+edgeString(child)+" with freq of: "+nodes[child].getFrequency());
	       		}
	       		updateFrequency(child, searchWord, freqCt);
      	}
	       	suggestionNo = suggestionCt;
      }
       
   } //End of Class PhraseSuffix_Node.java

       
		

