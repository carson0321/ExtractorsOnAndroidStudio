package com.example.emailextract;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {

	  @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        setContentView(R.layout.activity_main);
	        Log.v("test",new StringCutter().cut("abc@yahoo.com can't\n abc-d http://@aa\na") );
	        
	    }



	    public void testing(View view)
	    {
	    	new RecMail().execute(this);
	    }

}
