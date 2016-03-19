package selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.builders;

import  selab.csie.ntu.tw.personalcorpusextractor.prediction_tree.suffixtree.tree.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.SortedSet;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ScoredObject;

public class DocumentBuilder{

	private FrequencyBuilder table;
	private SuffixTokenBuilder suffixBuilder;
	private CollocationsBuilder collocBuilder;
	private SignificanceBuilder sigBuilder;

	public CollocationsBuilder getCollocBuilder() {
		return collocBuilder;
	}
	public void setCollocBuilder(CollocationsBuilder collocBuilder) {
		this.collocBuilder = collocBuilder;
	}

	public SuffixTokenBuilder getSuffixBuilder() {
		return suffixBuilder;
	}
	public void setSuffixBuilder(SuffixTokenBuilder suffixBuilder) {
		this.suffixBuilder = suffixBuilder;
	}
	
	public SignificanceBuilder getSigBuilder() {
		return sigBuilder;
	}
	public void setSigBuilder(SignificanceBuilder sigBuilder) {
		this.sigBuilder = sigBuilder;
	}
	
	public DocumentBuilder(){
		this.table  = 	new FrequencyBuilder();
	}	
	
	public void readDocuments(File inCorpus) throws IOException {
		System.out.print("STEP1: Reading Input Corpus.... ");
		System.out.println("Done!"); System.out.println("");
		DocumentBuilder sc = new DocumentBuilder();
		
		PrintWriter outFrequency;
		outFrequency = new PrintWriter(new FileWriter("complex2gram.txt"));
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(inCorpus));
			String line;
			
			System.out.print("STEP2: Tokenizing and Parsing.... ");
				while((line=br.readLine()) != null){
					sc.scanToTable(line, 2); //nGram = 2
				}
			sc.printTable(outFrequency);
			br.close();
			outFrequency.close();
		} catch(IOException e){
			e.printStackTrace();
		}
		System.out.println("Done!"); System.out.println("");
	}
	
	public void tokenizeDocuments(File inCorpus) throws IOException {
		setSuffixBuilder(new SuffixTokenBuilder());
	}
	
	public void calculateCollocations(File inCorpus, PrintWriter out) throws IOException{
	   setCollocBuilder(new CollocationsBuilder());
       int nodeCount=1;
       String [] collocationStrings = new String [500];
      // public void caculateCollocationFrequencies(File inCorpus, PrintWriter out) throws Exception{	
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
        		   
        	   out.append(word[1]+"="+Frequency[i]+"\n");
               out.append(collocationStrings[i]+"="+Frequency[i]+"\n"); 
           }
	}
	
	public void calculateSignificances(){
		setSigBuilder(new SignificanceBuilder());
	}
	
	public void scanToTable(String phrase, int window_size){  //window_size = no. of nGrams
		String[] tmp = phrase.split(" ");
		for(int i = 0; i < window_size; ++i){
			for(int j = 0; i+j < tmp.length; ++j)
			{
				String str = tmp[j];
				for(int k = 0; k < i; ++k)
					str=str+" "+tmp[j+k+1];
				//System.out.println(str+ " " + i);
				table.addToMix(str);
			}
		}
	}
	
	public void printTable(PrintWriter out)
	{
		table.printMix(out);
	}
	
	public Map<String, Float> getTable(){
		return table.getMap();
	}

}