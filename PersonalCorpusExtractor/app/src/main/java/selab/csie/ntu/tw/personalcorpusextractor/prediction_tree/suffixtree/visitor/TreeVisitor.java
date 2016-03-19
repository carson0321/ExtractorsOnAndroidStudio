package selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.visitor;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder.FacebookPhrases_Builder;
import  selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.tree.*;

public class TreeVisitor implements Visitor {

	/*
	 *Start of Program
	 */
	public static void main(String[] args) throws Exception
	{
		System.out.println("Starting Phrase Prediction Program.... ");

		TreeVisitor concreteVist = new TreeVisitor();
			try {
			//	concreteVist.visitTree();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/*
	 *Suffix Tree Search and Traversal
	 */
	public void visitTree(File inputCorpus) throws Exception{



		//Telescoped Version
		File file = new File("simple.txt"); //this should only have one sentence
		BufferedReader in = new BufferedReader(new FileReader(file));

		PhraseSuffix_Tree st = new PhraseSuffix_Tree(500000);
		PrintWriter out = new PrintWriter(new FileWriter("st.dot"));

		String phrase;
		while((phrase=in.readLine()) != null){
			String [] word=phrase.split(" ");
			for(int i = 0; i < word.length; i++){
//				if(i==word.length-1) word[i]+="$";
				st.addWord(word[i]);
			}
			st.sep();
		}
		in.close();
		st.signSignificance();
		st.printSignificanceNodes();
		//st.printTelescopeTree(out);	//print out all the combinations but remove one word branches
		out.close();



//		/*
//		 *Suffix Tree Setup and Input
//		 */
//
//		//String path = Environment.getExternalStorageDirectory().getPath();
//		//File inputCorpus = new File(path +"input5000.txt");
//		//File inputCorpus = new File(path + "/" + FacebookPhrases_Builder.getFileName());
//      //  File inputCorpus = new File("BagOfWordFacebook0.txt");
//		TreeBuilder tb = new TreeElement();
//		//PhraseSuffix_Tree st = tb.buildTree(500000);
//
//        String path = Environment.getExternalStorageDirectory().getPath();
//        File dir = new File(path + "/");
//        if (!dir.exists()){
//            dir.mkdir();
//        }
//
//            File fileoutCollocations = new File(path + "/" + "complex2gram.txt");
////            FileOutputStream fout = new FileOutputStream(file);
////            fout.write(data.getBytes());
////            fout.close();
//
//        PrintWriter outCollocations = new PrintWriter(fileoutCollocations);
//		tb.collectFrequenciesFromFile(inputCorpus);
//		tb.calculateCollocations(st, inputCorpus, outCollocations);
//		tb.addToTree(st);
//        tb.updateTree(st);
//		outCollocations.close();
//
//		/*
//		 *Suffix Tree Search and Traversal
//		 */
//		BufferedReader inSystem = new BufferedReader(new InputStreamReader(System.in));
//		String searchWord;
//	 	System.out.print("Enter a phrase: ");
//	 	searchWord=inSystem.readLine();
//	 	st.setInitialMessage(searchWord);
//		st.queryTree(1, searchWord);
//	  	if (st.suggestionNo == 0)  System.out.println("No Prediction Yet!");
//	 	System.out.println("");
//	 	st.querySuggestionKey();
//	 	System.out.println("");
//	 	st.queryPredsuggestionsMap();
//	 	searchWord=inSystem.readLine();
//	 	System.out.println("");
//	 	 do {
//	 		st.queryPredictionTable(1, Integer.parseInt(searchWord));
//	 	 	System.out.println("");
//	 	 	st.querySuggestionKey();
//	 	 	System.out.println("");
//	 	 	st.queryPredsuggestionsMap();
//		 	searchWord=inSystem.readLine();
//	 	} while (Integer.parseInt(searchWord)!=0);
//	 	st.getMessage();
	 }
}
