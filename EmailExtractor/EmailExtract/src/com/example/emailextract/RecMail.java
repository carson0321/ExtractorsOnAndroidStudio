package com.example.emailextract;



import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;


public class RecMail extends AsyncTask<Object, Object, Object>{
	private String address="redmine.selab.centos@gmail.com";
	private String password="Selab305!";
	protected Object doInBackground(Object... args) {
		try {
			Properties props = new Properties();
			props.put("mail.imap.ssl.enable", "true"); // required for Gmail
			props.put("mail.imap.sasl.enable", "true");
			props.put("mail.imap.sasl.mechanisms", "XOAUTH2");
			props.put("mail.imap.auth.login.disable", "true");
			props.put("mail.imap.auth.plain.disable", "true");
			Session session = Session.getInstance(props);
			//session.setDebug(true);	
			Store store = session.getStore("imaps");
			Log.v("sdCard",Environment.getExternalStorageDirectory().getPath());
			String path = Environment.getExternalStorageDirectory().getPath();
			File dir = new File(path + "/Email/");
	        if (!dir.exists()){
	            dir.mkdir();
	        }
			
			dir.mkdir();
			store.connect("imap.gmail.com", address, password);
	        Folder sentBox = store.getFolder("[Gmail]/Sent Mail");
	        sentBox.open(Folder.READ_ONLY);
	        Log.v("sentBox Num",""+sentBox.getMessageCount());
	        int i;
	        for (i = 1 ; i <=sentBox.getMessageCount() ; i++ ){
	        	File file = new File(dir, i+".txt");
		        Message msg = sentBox.getMessage(i);
		        Writer out = new OutputStreamWriter(new FileOutputStream(file));
		        
		        Multipart mp = (Multipart) msg.getContent();
	        	BodyPart bp = mp.getBodyPart(0);
	        	Log.v("Origin",bp.getContent().toString());
	        	String outputString = new StringCutter().cut(bp.getContent().toString());
	        	Log.v("Content",outputString);
	        
	        	out.write(outputString);
	        
		        out.close();
	        }   
		} catch (Exception mex) {
			mex.printStackTrace();
		}
		return null;
	}
	
}
