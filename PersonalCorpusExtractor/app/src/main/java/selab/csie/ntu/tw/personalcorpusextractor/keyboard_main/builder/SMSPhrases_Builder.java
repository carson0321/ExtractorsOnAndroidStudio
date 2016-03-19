package selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import selab.csie.ntu.tw.personalcorpusextractor.ExtractorSelector;
import selab.csie.ntu.tw.personalcorpusextractor.R;

/**
 * Created by CarsonWang on 2015/6/17.
 */
public class SMSPhrases_Builder implements Phrases_Builder {
    private static SMSPhrases_Builder smsPhrases_Builder;
    private final String fileName = "BagOfWordSMS";


    private List<SMSData> smsList;
    private static String messageData;
    private static int count = 0;


    public static SMSPhrases_Builder getMultiInstance(){
        smsPhrases_Builder = new SMSPhrases_Builder();
        return smsPhrases_Builder;
    }
    private SMSPhrases_Builder(){
        handle();
    }

    private void handle(){
        smsList = new ArrayList<>();

        Uri uri = Uri.parse("content://sms/inbox");
        Cursor c= ExtractorSelector.getInstance().getContentResolver().query(uri, null, null ,null,null);
        ExtractorSelector.getInstance().startManagingCursor(c);

        // Read the sms data and store it in the list
        if(c.moveToFirst()) {
            for(int i=0; i < c.getCount(); i++) {
                SMSData sms = new SMSData();
                sms.setBody(c.getString(c.getColumnIndexOrThrow("body")).toString());
                sms.setNumber(c.getString(c.getColumnIndexOrThrow("address")).toString());
                smsList.add(sms);

                c.moveToNext();
            }
        }
        c.close();
        // Set smsList in the ListAdapter
//        setListAdapter(new ListAdapter(this, smsList));
        messageData = "";
        getResult();
    }

    private class SMSData {

        // Number from witch the sms was send
        private String number;
        // SMS text body
        private String body;

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

    }
    private String regexUrlandEmailString(String message){
        String emailRegex = "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";
        String urlRegexWithHttp = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]" +
                "*[-a-zA-Z0-9+&@#/%=~_|]";
        String urlRegexWithoutHttp = "[-a-zA-Z0-9+&@#/%?=~_|!:,.;]" +
                "*[-a-zA-Z0-9+&@#/%=~_|]";

        Pattern emailPattern = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher matchEmail = emailPattern.matcher(message);
        List <String> allEmail = new ArrayList<>();
        while (matchEmail.find()) allEmail.add(matchEmail.group());

        Pattern urlPatternWithHttp = Pattern.compile(urlRegexWithHttp,Pattern.CASE_INSENSITIVE);
        Matcher matchUrlWithHttp = urlPatternWithHttp.matcher(message);
        List <String> allUrl = new ArrayList<>();
        while (matchUrlWithHttp.find()) allUrl.add(matchUrlWithHttp.group());

        Pattern urlPatternWithoutHttp = Pattern.compile(urlRegexWithoutHttp,Pattern.CASE_INSENSITIVE);
        Matcher matchUrlWithoutHttp = urlPatternWithoutHttp.matcher(message);
        while (matchUrlWithoutHttp.find()){
            if(matchUrlWithoutHttp.group().toString().contains(".") &&
                    !matchUrlWithoutHttp.group().toString().contains("@") )
                allUrl.add(matchUrlWithoutHttp.group());
        }
        String result = "";
        String allEmailString = "";
        String allUrlString = "";
        for(String a: allEmail) allEmailString += a + "\n";
        for(String a: allUrl) allUrlString += a + "\n";
        if(allEmailString.length()!=0) result = result + allEmailString;
        if(allUrlString.length()!=0) result = result + allUrlString;

        return result;
    }


    public Phrases_Product getResult(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(ExtractorSelector.getInstance());
        dialog.setTitle("File Request");
        if (!smsList.isEmpty()) {
            if (isExternalStorageWritable()) {
                for(SMSData sms : smsList) {
                    String noSpaceMessage = sms.getBody().replaceAll("[^a-zA-Z0-9 \\s]+", "");
                    String totalMessage = "";
                    String[] mergeString = noSpaceMessage.split("\\s");
                    for(String a :mergeString) totalMessage += a + " ";
                    if (totalMessage.length() >= 3) messageData += totalMessage + "\n";
                    messageData += regexUrlandEmailString(sms.getBody());
                }
                writeToFile(fileName+String.valueOf(count)+".txt", messageData);
                count++;
                dialog.setMessage("Write successfully!");
            } else dialog.setMessage("Write fail!");
        } else  dialog.setMessage("Write fail!");
        dialog.setPositiveButton(R.string.ok_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(
                            DialogInterface dialoginterface, int i) {
                    }
                });
        dialog.show();
        return null;
    }
    //Checks if external storage is available for read and write
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //Write file to external storage
    private void writeToFile(String fileName, String data){
        //Create the directory for the user's public pictures directory
        String path = Environment.getExternalStorageDirectory().getPath();
        File dir = new File(path + "/SMSExtractor");
//        File dir = new File(path + "/");
        if (!dir.exists()){
            dir.mkdir();
        }
        try {
            File file = new File(path + "/SMSExtractor/" + fileName);
//            File file = new File(path + "/" + fileName);
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(data.getBytes());
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
