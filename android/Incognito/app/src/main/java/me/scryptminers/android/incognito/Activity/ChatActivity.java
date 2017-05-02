package me.scryptminers.android.incognito.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.scryptminers.android.incognito.Adapter.CustomChatAdapter;
import me.scryptminers.android.incognito.Database.ChatDatabaseHelper;
import me.scryptminers.android.incognito.Model.Message;
import me.scryptminers.android.incognito.Model.User;
import me.scryptminers.android.incognito.R;
import me.scryptminers.android.incognito.Service.MessageService;
import me.scryptminers.android.incognito.Util.PGP;
import me.scryptminers.android.incognito.Util.PGPFunctions;
import me.scryptminers.android.incognito.Util.SharedValues;

public class ChatActivity extends AppCompatActivity {
    private String friendName;
    private String friendEmail, userEmail;
    private ImageView buttonSend;
    private ListView listViewChat;
    private EditText messageToSend;
    private CustomChatAdapter customChatAdapter;
    public static List<Message> msgs;
    private BroadcastReceiver broadcastReceiver;
    private boolean isRegistered;
    String[] elementNames= new String[5];
    ChatDatabaseHelper db;
    Intent msgIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        friendName = getIntent().getStringExtra("FRIEND_NAME");
        friendEmail = getIntent().getStringExtra("FRIEND_EMAIL");
        userEmail = SharedValues.getValue("USER_EMAIL");
        getSupportActionBar().setTitle(friendName);
        buttonSend = (ImageView) findViewById(R.id.send);
        Log.e("FRIEND",friendEmail);
        msgIntent = new Intent(this, MessageService.class);
        msgIntent.putExtra("FRIEND_EMAIL", friendEmail);
        startService(msgIntent);

        listViewChat = (ListView) findViewById(R.id.listView_chat);
        db = new ChatDatabaseHelper(getApplicationContext());
        msgs = new ArrayList<Message>();
        msgs = db.getAllMessages(userEmail,friendEmail);
        listViewChat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        customChatAdapter = new CustomChatAdapter(getApplicationContext(), R.layout.right_message_row,msgs);
        listViewChat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listViewChat.setAdapter(customChatAdapter);
        //listViewChat.invalidate();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
               // msgs.clear();
                //msgs = db.getAllMessages(userEmail);
                Log.d("Message","In Onreceive");
                customChatAdapter.notifyDataSetChanged();
                //listViewChat.invalidate();
                //listViewChat.setSelection(customChatAdapter.getCount() - 1);
            }
        };

        //ReceiveMessageTask receiveMessageTask= new ReceiveMessageTask();
        //receiveMessageTask.execute("dsds");

        messageToSend = (EditText) findViewById(R.id.messageToSend);
       /* messageToSend.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage(messageToSend.getText().toString());
                }
                return false;
            }
        });*/
        //ReceiveMessageTask receiveMessageTask = new ReceiveMessageTask();
        //receiveMessageTask.execute();
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage(messageToSend.getText().toString());
            }
        });

        //listViewChat.setAdapter(customChatAdapter);

        //to scroll the list view to bottom on data change
        customChatAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listViewChat.setSelection(customChatAdapter.getCount() - 1);
            }
        });
    }
    private boolean sendChatMessage(String message){

        // from to message direction
        Message msg = new Message(userEmail,friendEmail,message,"left");
        db.addMessage(msg);
        messageToSend.setText("");
        //customChatAdapter.notifyDataSetChanged();
        //msgs.clear();
        //msgs = db.getAllMessages(userEmail);
        msgs.add(msg);
        customChatAdapter.notifyDataSetChanged();
        listViewChat.invalidate();
        String receiverPublicKey = db.getFriendPublicKey(friendEmail);
        String encrypted_message = "";

            //encrypted_message = PGPFunctions.generateEncryptedMessage(message,receiverPublicKey);
            encrypted_message = PGP.encryptMessage(message,receiverPublicKey);

        SendMessageTask sendMessageTask = new SendMessageTask(new Message(userEmail,friendEmail,encrypted_message,"left"));
        sendMessageTask.execute();

        return true;
    }

    public class SendMessageTask extends AsyncTask<String, Void, Boolean> {

        private final Message message;
        private RequestQueue requestQueue;
        private JsonObjectRequest jsonObjectRequest;
        private final String URL="https://scryptminers.me/send";

        public SendMessageTask(Message message) {
            this.message = message;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String,String> userMap = new HashMap<>();
            userMap.put("sender",SharedValues.getValue("USER_EMAIL"));
            userMap.put("message",message.getMessage());
            userMap.put("receiver",friendEmail);

            try {
                // Simulate network access.
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, new JSONObject(userMap), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String message_received  = response.getString("message");
                            //customChatAdapter.add(new Message(friendName,message_received,"left"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                requestQueue.add(jsonObjectRequest);
                while (!jsonObjectRequest.hasHadResponseDelivered())
                    Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.d("error",e.toString());
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {

                //refresh chat adapter list view
            } else {

            }
        }

    }

    /*public class ReceiveMessageTask extends AsyncTask<String, Void, Boolean> {

        private RequestQueue requestQueue;
        private JsonObjectRequest jsonObjectRequest;
        private final String URL="https://scryptminers.me/getMessage";

        public ReceiveMessageTask() {

        }

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String,String> userMap = new HashMap<>();
            userMap.put("from",friendEmail);
            userMap.put("to",SharedValues.getValue("USER_EMAIL"));
            userMap.put("lastread","0");
            try {
                // Simulate network access.
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, new JSONObject(userMap), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            //String message_received  = response.getString("message");
                            ChatDatabaseHelper db = new ChatDatabaseHelper(getApplicationContext());
                            String receiverPublicKey = db.getFriendPublicKey(friendEmail);
                            for (int i = 0, size = jsonArray.length(); i < size; i++)
                            {
                                JSONObject objectInArray = jsonArray.getJSONObject(i);
                                String ciphertext = objectInArray.getString("message");
                                String message = PGP.decryptMessage(ciphertext,SharedValues.getValue("PRIVATE_KEY"));
                                msgs.add(new Message(friendEmail,userEmail,message,"right"));
                            }
                            listViewChat.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                requestQueue.add(jsonObjectRequest);
                while (!jsonObjectRequest.hasHadResponseDelivered())
                    Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.d("error",e.toString());
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                listViewChat.invalidate();
                //refresh chat adapter list view
            } else {

            }
        }

        @Override
        protected void onCancelled() {
        }
    }
*/
    @Override
    public void onStart() {
        super.onStart();
        if (!isRegistered) {
            registerReceiver(broadcastReceiver, new IntentFilter("Update"));
            isRegistered = true;

/*            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // msgs.clear();
                    //msgs = db.getAllMessages(userEmail);
                    customChatAdapter.notifyDataSetChanged();
                    listViewChat.invalidate();
                    //listViewChat.setSelection(customChatAdapter.getCount() - 1);
                }
            };*/

        }

    }

    /*@Override
    protected void onPostResume() {
        super.onPostResume();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // msgs.clear();
                //msgs = db.getAllMessages(userEmail);
                customChatAdapter.notifyDataSetChanged();
                listViewChat.invalidate();
                //listViewChat.setSelection(customChatAdapter.getCount() - 1);
            }
        };
    }*/

    @Override
    public void onStop() {
        super.onStop();
        if (isRegistered) {
            unregisterReceiver(broadcastReceiver);
            isRegistered = false;
        }
        stopService(msgIntent);
    }
}
