package tw.edu.ntu.csie.selab.facebookOutboxExtractor;

import java.text.Normalizer;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private static String userID = null;
    private static String fileName = "messageOutput.txt", messageData = null;
    private UiLifecycleHelper uiHelper;
    private TextView messageInfoTextView;
    private Button messageRequestButton, fileRequestButton;

    //Set up the layout
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        //Login button
        LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
        authButton.setFragment(this);
        authButton.setReadPermissions(Arrays.asList("read_mailbox"));

        //Display message information
        messageInfoTextView = (TextView) view.findViewById(R.id.MessageInfoTextView);

        //Update message
        messageRequestButton = (Button) view.findViewById(R.id.messageRequestButton);
        messageRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doMessageRequest();
            }
        });

        //Produce file
        fileRequestButton = (Button) view.findViewById(R.id.fileRequestButton);
        fileRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("File Request");
                if (messageData != null) {
                    if (FileUtils.isExternalStorageWritable()) {
                        // "\\s" mean that white space
//                        String englishOnlyString = Normalizer.normalize(messageData, Normalizer.Form.NFD).
//                                replaceAll("[^a-zA-Z0-9 \\s]+", "");
                        //Other way
//                        String englishOnlyString = messageData.replaceAll("[^a-zA-Z0-9 \\s]+", "");

//                        FileUtils.writeToFile(fileName, englishOnlyString);
                        FileUtils.writeToFile(fileName, messageData);
                        dialog.setMessage("Write successfully!");
                    } else dialog.setMessage("Write fail!");
                } else dialog.setMessage("Write fail!");
                dialog.setPositiveButton(R.string.ok_label,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialoginterface, int i) {
                            }
                        });
                dialog.show();
            }
        });

        return view;
    }


    //Passing in the callback variable
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    //Respond to session state changes (handle login and logout)
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");
            messageRequestButton.setVisibility(View.VISIBLE);
            fileRequestButton.setVisibility(View.VISIBLE);
            messageInfoTextView.setVisibility(View.VISIBLE);

        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
            messageRequestButton.setVisibility(View.INVISIBLE);
            fileRequestButton.setVisibility(View.INVISIBLE);
            messageInfoTextView.setVisibility(View.INVISIBLE);
        }
    }

    //add logic to listen for the changes
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };


    /**
     * creates the Facebook session and opens it automatically
     * if a cached token is available
     */

    @Override
    public void onResume() {
        super.onResume();
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }


    //Do Button to display message
    @SuppressWarnings("deprecation")
    private void doMessageRequest() {
        //Request user data and show the results
        Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    // Fetch the parsed user ID
                    userID = buildUserID(user);
                    Log.i(TAG, "ID = " + userID);
                }
            }
        });

        //Request facebook message data and show the results
        new Request(
                Session.getActiveSession(),
                "/me/inbox",
                null,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        GraphObject graphObject = response.getGraphObject();
                        messageInfoTextView.setText(buildMessageInfoDisplay(graphObject));
                    }
                }
        ).executeAsync();
    }


    //Show information in Display
    private String buildMessageInfoDisplay(GraphObject data) {
        StringBuilder messageInfo = new StringBuilder("");
        if (data != null) {
            JSONObject jsonObject = data.getInnerJSONObject();
            try {
                //Level 1 JSON loop
                JSONArray dataArray = jsonObject.getJSONArray("data");
                for (int dataSize = 0; dataSize < dataArray.length(); dataSize++) {
                    JSONObject dataObject = dataArray.getJSONObject(dataSize);

                    //Level 2 JSON loop
                    JSONObject messageObject = dataObject.getJSONObject("comments");

                    //Level 3 JSON loop
                    JSONArray messageArray = messageObject.getJSONArray("data");
                    for (int messageSize = messageArray.length() - 1; messageSize >= 0; messageSize--) {

                        //All message can be fetched
//						JSONObject message = messageArray.getJSONObject(messageSize);
//						messageInfo.append(message.getString("message") + "\n");

                        //Level 4 JSON loop fetch the message of sending
                        JSONObject message = messageArray.getJSONObject(messageSize);
                        JSONObject messageFromObject = message.getJSONObject("from");
                        String messageFromID = messageFromObject.getString("id");
                        if (messageFromID.matches(userID)) {
							/*Can use optString instead of getString which just returns null
							  if value doesn't exist, instead of throwing an exception.*/
                            if ((message.has("message") && !message.isNull("message"))){
                                //This line handle a message include white space ()
//                                messageInfo.append(message.getString("message") + "\n");

                                //Other way to implement a message which has no white space
                                String noSpaceMessage = message.getString("message").
                                        replaceAll("[^a-zA-Z0-9 \\s]+", "");

                                String totalMessage = "";
                                String [] mergeString = noSpaceMessage.split("\\s");
                                for(int spaceNumber = 0 ; spaceNumber < mergeString.length; spaceNumber++)
                                    totalMessage += mergeString[spaceNumber] + " ";
                                if(totalMessage.length()>=3)
                                    messageInfo.append(totalMessage + "\n");
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        messageData = messageInfo.toString();
        return messageData;
    }

    // Accept User ID
    private String buildUserID(GraphUser user) {
        StringBuilder userInfo = new StringBuilder("");

        userInfo.append(user.getId());
        return userInfo.toString();
    }
}