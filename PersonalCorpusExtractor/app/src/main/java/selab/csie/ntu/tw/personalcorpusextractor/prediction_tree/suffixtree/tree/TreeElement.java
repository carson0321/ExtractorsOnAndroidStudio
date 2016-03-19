package selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import  selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.builders.*;



public class TreeElement implements TreeBuilder{
	
	private  PhraseSuffix_Tree PhraseSuffix_tree;
	
	@Override
	public PhraseSuffix_Tree buildTree(int nodeSize) {
		 this.PhraseSuffix_tree = new PhraseSuffix_Tree(nodeSize);
		 return PhraseSuffix_tree;
	}
	
	@Override
	public void acceptVisitors(PrintWriter out) {
			buildTree(500000);
			PhraseSuffix_tree.printFullTree(out);
	}
	
	@Override
	public void collectFrequenciesFromFile(File inCorpus) {
			FrequencyAggregator fa = new FrequencyAggregator(); 
			try {
				fa.aggregateFrequency(inCorpus);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void addToTree(PhraseSuffix_Tree st) throws Exception {
		System.out.print("STEP5: Constructing Suffix Tree.... ");
		BufferedReader in5 = new BufferedReader(new FileReader("complex2gram.txt"));
		PrintWriter outGraphV = new PrintWriter(new FileWriter("st.dot"));
		
		String phrasePrior;
		while((phrasePrior=in5.readLine()) != null){
				String [] wordPrior=phrasePrior.split("=");
				String wordPrior1 = wordPrior[0];
				String [] wordPriorPrior=wordPrior1.split(" ");
				for(int j = 0; j < wordPriorPrior.length; j++){
					//if(i==word.length-1) word[i]+="$";
					st.addWord(wordPriorPrior[j]);
				}
				st.sep();
		}
		st.printFullTree(outGraphV);
		outGraphV.close();
		in5.close();
		System.out.println("Done!"); 
	}

	@Override
	public void updateTree(PhraseSuffix_Tree st) throws Exception {
		System.out.print("STEP6: Updating Frequency.... ");
		BufferedReader in52 = new BufferedReader(new FileReader("complex2gram.txt"));
		String phrasePriorPrior;
		while((phrasePriorPrior=in52.readLine()) != null){
			String [] word2=phrasePriorPrior.split("=");
				String updatePhrase =  word2[0];
				//System.out.println("Checking: "+word2[0]);
				Double updateFreq = Double.parseDouble(word2[1]);
				String [] wordPriorPrior=updatePhrase.split(" ");
				for(int j = 0; j < wordPriorPrior.length; j++){
					//System.out.println("Checking: "+wordPriorPrior[j]+updateFreq);
						st.updateFrequency(1, wordPriorPrior[j], updateFreq);
			}
		}
		in52.close();
		System.out.println("Done!"); System.out.println("");
	}

	@Override
	public void calculateCollocations(PhraseSuffix_Tree st, File inCorpus, PrintWriter out) throws Exception {
		System.out.print("STEP4: Calculating Collocations.... ");
		DocumentBuilder docBuilder = new DocumentBuilder();
		docBuilder.calculateCollocations(inCorpus, out);
        System.out.println("Done!");
	}

}
