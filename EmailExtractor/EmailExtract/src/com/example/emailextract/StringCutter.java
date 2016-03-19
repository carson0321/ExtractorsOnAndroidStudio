package com.example.emailextract;

public class StringCutter {
	private String[] urls= {"http","ftp","www"};
	private boolean hasSpace ;
	private boolean hasNext ;
	private boolean hasHiven;
	private boolean hasApostrophe ;
	private boolean hasAt ;
	private boolean testing;
	private boolean isUrl ;
	
	public String cut(String input){
		input=input.toLowerCase();
		init();
		String output= "";
    	int templength = 0 ;
    	String tempString ="";
    	for (char tempchar : input.toCharArray() ){
    		if(isUrl){
    			if(tempchar == ' '  ) {
    				isUrl = false;
    				hasSpace = true;
    				hasHiven = false ;
        			hasApostrophe = false ;
        			hasAt = false ;
    			}
    			else if( tempchar=='\n' || tempchar =='\r' ){
    				isUrl = false;
    				hasNext = true ;
        			hasSpace = true;
        			hasHiven = false ;
        			hasApostrophe = false ;
        			hasAt = false ;
    			}
    			output = output+tempchar;
    		}
    		else if ( (int)tempchar<=122 &&  (int)tempchar>=97  ){
    			if (hasSpace == true){
    				testing = true;
    				tempString = "";
    				templength = 0; 
    			}
    			hasSpace = false;
    			hasNext = false;
    			if (hasHiven == true){
    				output= output+ '-';
    			}
    			else if(hasApostrophe ==true ){
    				output= output+ '\'';
    			}
    			else if(hasAt ==true ){
    				output= output+ '@';
    				isUrl= true;
    			}
    			output = output+tempchar;

    		}
    		else if(tempchar == '-' && hasSpace == false  ){
    			hasHiven = true; 
    		}
    		else if(tempchar == '@' && hasSpace == false  ){
    			hasAt = true; 
    		}
    		else if(tempchar == '\'' && hasSpace == false  ){
    			hasApostrophe = true; 
    		}
    		else if(tempchar == '\'' && hasSpace == false  ){
    			hasApostrophe = true; 
    		}
    		else if( (tempchar=='\n' || tempchar =='\r') && hasNext == false  ){
    			hasNext = true ;
    			hasSpace = true;
    			hasHiven = false ;
    			hasApostrophe= false;
    			hasAt= false ;
    			output = output+'\n';
    		}
    		else if( hasSpace == false  ){
    			hasSpace = true ;
    			hasHiven = false ;
    			hasApostrophe= false;
    			hasAt= false ;
    			output = output+' ';
    		}
    		
    		if (testing == true  ){
    			tempString += tempchar;
    			templength ++;
    			for (String target : urls){
    				if(tempString.equals(target)){
    					isUrl = true;
    					testing = false ; 
    				}
    			}
    			if (templength >=6) testing = false; 
    		}
    	}

		return output;
	}
	private void init(){
		hasSpace = true;
		hasNext = false;
		hasHiven = false;
		hasApostrophe = false ;
		hasAt = false ;
		testing = true;
		isUrl = false;			
	}

}
