package selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import selab.csie.ntu.tw.personalcorpusextractor.ExtractorSelector;
import selab.csie.ntu.tw.personalcorpusextractor.R;

/**
 * Created by CarsonWang on 2015/6/17.
 */
public class EmailPhrases_Builder extends AsyncTask<Object, Object, Object> implements Phrases_Builder {

    private final String fileName = "BagOfWordEmail";
    private AlertDialog.Builder dialog;

    private String address="redmine.selab.centos@gmail.com";
    private String password="Selab305!";

    private static int count = 0;

    private static String messageData;

    private Session session;
    private Store store;

    @Override
    protected Object doInBackground(Object... args) {
        try {
            Properties props = new Properties();
            props.put("mail.imap.ssl.enable", "true"); // required for Gmail
            props.put("mail.imap.sasl.enable", "true");
            props.put("mail.imap.sasl.mechanisms", "XOAUTH2");
            props.put("mail.imap.auth.login.disable", "true");
            props.put("mail.imap.auth.plain.disable", "true");
            session = Session.getInstance(props);
            //session.setDebug(true);
            store = session.getStore("imaps");
            messageData = "";
            getResult();
        } catch (Exception mex) {
            mex.printStackTrace();
        }
        return null;
    }
    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        dialog = new AlertDialog.Builder(ExtractorSelector.getInstance());
    }
    @Override
    protected void onPostExecute(Object result){
        super.onPostExecute(result);
        dialog = new AlertDialog.Builder(ExtractorSelector.getInstance());
        dialog.setTitle("File Request");
        if (!messageData.isEmpty()) dialog.setMessage("Write successfully!");
        else dialog.setMessage("Write fail!");
        dialog.setPositiveButton(R.string.ok_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(
                            DialogInterface dialoginterface, int i) {
                    }
                });
        dialog.show();
    }

    private void regexString(String allMessage,String origin){
        String emailRegex = "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";
        String urlRegexWithHttp = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]" +
                "*[-a-zA-Z0-9+&@#/%=~_|]";
        String urlRegexWithoutHttp = "[-a-zA-Z0-9+&@#/%?=~_|!:,.;]" +
                "*[-a-zA-Z0-9+&@#/%=~_|]";

        Pattern emailPattern = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher matchEmail = emailPattern.matcher(origin);
        List <String> allEmail = new ArrayList<>();
        while (matchEmail.find()) allEmail.add(matchEmail.group());
//        while (matchEmail.find()) allEmail.add(
//                allMessage.substring(matchEmail.start(0),matchEmail.end(0)));

        Pattern urlPatternWithHttp = Pattern.compile(urlRegexWithHttp,Pattern.CASE_INSENSITIVE);
        Matcher matchUrlWithHttp = urlPatternWithHttp.matcher(origin);
        List <String> allUrl = new ArrayList<>();
        while (matchUrlWithHttp.find()) allUrl.add(matchUrlWithHttp.group());
//        while (matchUrl.find()) allUrl.add(
//                allMessage.substring(matchUrl.start(0),matchUrl.end(0)));
        Pattern urlPatternWithoutHttp = Pattern.compile(urlRegexWithoutHttp,Pattern.CASE_INSENSITIVE);
        Matcher matchUrlWithoutHttp = urlPatternWithoutHttp.matcher(origin);
        while (matchUrlWithoutHttp.find()){
            if(matchUrlWithoutHttp.group().toString().contains(".") &&
                    !matchUrlWithoutHttp.group().toString().contains("@") )
                allUrl.add(matchUrlWithoutHttp.group());
        }

        allMessage = allMessage.replaceAll("[^a-zA-Z0-9!,.:;? ]+","");
        String []regex = allMessage.split("[!,.:;?]+");

        String outputString ="";
        for(String line : regex){
            if(line.length()!=0)
                outputString += line+"\n";
        }
        String allEmailString = "";
        String allUrlString = "";
        for(String a: allEmail) allEmailString += a + "\n";
        for(String a: allUrl) allUrlString += a + "\n";
        if(allEmailString.length()!=0) outputString = outputString + allEmailString;
        if(allUrlString.length()!=0) outputString = outputString + allUrlString;

        Log.v("Content",outputString);
        messageData += outputString;
    }

    public Phrases_Product getResult(){
        Log.v("sdCard",Environment.getExternalStorageDirectory().getPath());
        String path = Environment.getExternalStorageDirectory().getPath();
        File dir = new File(path + "/EmailExtractor/");
        if (!dir.exists()){
            dir.mkdir();
        }
        try{
            store.connect("imap.gmail.com", address, password);
            Folder sentBox = store.getFolder("[Gmail]/Sent Mail");
            sentBox.open(Folder.READ_ONLY);
            Log.v("sentBox Num",""+sentBox.getMessageCount());

            int msgCount;
            for(msgCount = 1 ; msgCount <= sentBox.getMessageCount();msgCount++){
                Message msg = sentBox.getMessage(msgCount);
                Multipart mp = (Multipart) msg.getContent();
                BodyPart bp = mp.getBodyPart(0);
                Log.v("Origin",bp.getContent().toString());

                String allMessage = "";
                String []message =  bp.getContent().toString().split("\r\n");
                for(String line : message){
                    allMessage += line;
                }
                regexString(allMessage,bp.getContent().toString());
                Log.v("OriginRank", allMessage);
            }
            File file = new File(dir, fileName + count + ".txt");
            Writer out = new OutputStreamWriter(new FileOutputStream(file));
            out.write(messageData);
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
