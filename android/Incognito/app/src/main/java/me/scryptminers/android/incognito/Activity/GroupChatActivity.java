package me.scryptminers.android.incognito.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import me.scryptminers.android.incognito.Adapter.CustomChatAdapter;
import me.scryptminers.android.incognito.Adapter.CustomGroupChatAdapter;
import me.scryptminers.android.incognito.Database.ChatDatabaseHelper;
import me.scryptminers.android.incognito.Model.Group;
import me.scryptminers.android.incognito.Model.GroupMessage;
import me.scryptminers.android.incognito.Model.Message;
import me.scryptminers.android.incognito.R;
import me.scryptminers.android.incognito.Service.GroupMessageService;
import me.scryptminers.android.incognito.Service.KeyMessageService;
import me.scryptminers.android.incognito.Util.PGP;
import me.scryptminers.android.incognito.Util.SharedValues;

public class GroupChatActivity extends AppCompatActivity {
    private String groupName,userEmail;
    private ImageView buttonSend;
    private ListView listViewGroupChat;
    private EditText messageToSend;
    private CustomGroupChatAdapter customGroupChatAdapter;
    private BroadcastReceiver broadcastReceiver;
    private boolean isRegistered;
    public static List<GroupMessage> groupMessages;
    String[] elementNames= new String[5];
    ChatDatabaseHelper db;
    Intent msgIntent;
    Intent keyIntent;
    String grpkey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        groupName = getIntent().getStringExtra("GROUP_NAME");
        userEmail = SharedValues.getValue("USER_EMAIL");
        getSupportActionBar().setTitle(groupName);
        buttonSend = (ImageView) findViewById(R.id.groupSend);

        msgIntent = new Intent(this, GroupMessageService.class);
        msgIntent.putExtra("GROUP_NAME", groupName);
        startService(msgIntent);

        listViewGroupChat = (ListView) findViewById(R.id.listView_groupChat);
        db = new ChatDatabaseHelper(getApplicationContext());
        groupMessages = new ArrayList<GroupMessage>();
        groupMessages = db.getAllGroupMessages(userEmail,groupName);
        listViewGroupChat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        //grpkey = db.getGroupKey(groupName);
        //Log.e("Group Key",grpkey);

        customGroupChatAdapter = new CustomGroupChatAdapter(getApplicationContext(), R.layout.right_message_row,groupMessages);
        listViewGroupChat.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listViewGroupChat.setAdapter(customGroupChatAdapter);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("GroupMessage","In Onreceive");
                customGroupChatAdapter.notifyDataSetChanged();
            }
        };


        messageToSend = (EditText) findViewById(R.id.groupMessageToSend);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage(messageToSend.getText().toString(),groupName);
               // grpkey = db.getGroupKey(groupName);
                /*grpkey = SharedValues.getValue(groupName+"_KEY");
                Log.e("Group Key",grpkey);
                String plaintextMessage = messageToSend.getText().toString();
                messageToSend.setText("");
                GroupMessage grpmsg = new GroupMessage(plaintextMessage,userEmail,groupName,"left");
                groupMessages.add(grpmsg);
                db.addGroupMessage(grpmsg);
                byte[] gkey = android.util.Base64.decode(grpkey,android.util.Base64.DEFAULT);
                //byte[] gkey = new byte[0];
                // gkey = grpkey.getBytes("UTF-8");

                SecretKey encryptionKey = new SecretKeySpec(gkey,0,gkey.length,"AES");
                byte[] rawAESkey = encryptionKey.getEncoded();
               // byte[] encodedkey = android.util.Base64.decode(grpkey, android.util.Base64.DEFAULT);
                //SecretKey originalKey = new SecretKeySpec(encodedkey, 0, encodedkey.length, "AES");
                try {
                    //byte[] encryptedMessage = PGP.encryptGroupMessage(messageToSend.getText().toString(),rawAESkey);
                    byte[] encryptedMessage = PGP.encryptGroupMessage(plaintextMessage,encryptionKey);
                    String message = Base64.toBase64String(encryptedMessage);
                    customGroupChatAdapter.notifyDataSetChanged();
                    listViewGroupChat.invalidate();
                    SendGroupMessageTask sendGroupMessageTask = new SendGroupMessageTask(new GroupMessage(message,userEmail,groupName,"left"));
                    sendGroupMessageTask.execute();

                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }*/
            }
        });

        //to scroll the list view to bottom on data change
        customGroupChatAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listViewGroupChat.setSelection(customGroupChatAdapter.getCount() - 1);
            }
        });
    }
    private boolean sendChatMessage(String message, String groupName){

        // from to message direction
        //db.addGroupMessage(groupMessage);
        messageToSend.setText("");

        String encrypted_message = "";
        String[] groupMembers = db.getAllGroupMembers(groupName);
        Log.d("Group Members", Arrays.toString(groupMembers));
        //String admin = db.getAdmin(groupName);
        //String[] grpMembers = new String[groupMembers.length-1];
        String[] encryptedGroupMessage = new String[groupMembers.length];
        // cv@cv.com,vc@vc.com,h@h.com
        GroupMessage groupMessage = new GroupMessage();
        for(int i = 0;i<groupMembers.length;i++){
            if(!groupMembers[i].matches(userEmail)) {

                String receiverPublicKey = db.getFriendPublicKey(groupMembers[i]);
                String friendEmail = groupMembers[i];
                groupMessage = new GroupMessage(message,userEmail,friendEmail,groupName,"left");
                encrypted_message = PGP.encryptMessage(message, receiverPublicKey);
                SendGroupMessageTask sendGroupMessageTask = new SendGroupMessageTask(new GroupMessage(encrypted_message,userEmail,friendEmail,groupName,"left"));
                sendGroupMessageTask.execute();
            }
        }

        groupMessages.add(groupMessage);
        customGroupChatAdapter.notifyDataSetChanged();
        listViewGroupChat.invalidate();
        /*//Convert String Array to comma Separated String
        StringBuilder builder = new StringBuilder();
        for (String msg : encryptedGroupMessage) {
            builder.append(msg).append("%%%");
        }

        builder.deleteCharAt(builder.length() - 1);
        encrypted_message = builder.toString();
        Log.e("enc msg", encrypted_message);*/
        //userMap.put("members",builder.toString());
        //SendGroupMessageTask sendGroupMessageTask = new SendGroupMessageTask(new GroupMessage(encrypted_message,userEmail,groupName,"left"));
        //sendGroupMessageTask.execute();
        //encrypted_message = PGPFunctions.generateEncryptedMessage(message,receiverPublicKey);


        return true;
    }

    public class SendGroupMessageTask extends AsyncTask<String, Void, Boolean> {

        private GroupMessage message;
        private RequestQueue requestQueue;
        private JsonObjectRequest jsonObjectRequest;
        private final String URL="https://scryptminers.me/sendTeamMessage";

        public SendGroupMessageTask(GroupMessage message) {
            this.message = message;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String,String> userMap = new HashMap<>();
            userMap.put("from",SharedValues.getValue("USER_EMAIL"));
            userMap.put("to",message.getTo());
            userMap.put("message",message.getMessage());
            userMap.put("group_name",message.getgroupName());
            /*userMap.put("sender",SharedValues.getValue("USER_EMAIL"));
            userMap.put("receiver",message.getTo());
            userMap.put("message",message.getMessage());*/


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
                        Toast.makeText(GroupChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onStart() {
        super.onStart();
        if (!isRegistered) {
            registerReceiver(broadcastReceiver, new IntentFilter("UpdateGroupMessage"));
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
